package util2.integrateExpression;

import java.io.File;
import java.io.IOException;
import java.util.*;

import endrov.data.EvData;
import endrov.ev.*;
import endrov.imageset.*;
import endrov.nuc.*;
import endrov.shell.Shell;
import endrov.util.*;

/**
 * Anterior-posterior expression integration. Whole-embryo corresponds to numSlices=1 
 * @author Johan Henriksson
 *
 */
public class IntExpAP
	{

	
	
	public static void main(String arg[])
		{
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();

		EvData data=EvData.loadFile(new File("/Volumes/TBU_main01/ost4dgood/TB2141_070621_b.ost/"));
		
		int numSubDiv=20;
		String channelName="GFP";
		String expName="exp"; //Neutral name
		
		String newLinName=linFor(numSubDiv,channelName);
		doProfile(data, newLinName,expName,channelName,numSubDiv);
		data.saveData(); 
		
		printProfile(data, newLinName,expName,channelName,numSubDiv, fileFor(data,numSubDiv,channelName));
		
		System.exit(0);
		}
		
	
	public static String linFor(int numSubDiv, String channelName)
		{
		return "AP"+numSubDiv+"-"+channelName;
		}
	
	public static File fileFor(EvData data, int numSubDiv, String channelName)
		{
		//TODO: later, use blobs or similar?
		File datadir=data.io.datadir();
//		return new File(datadir,"AP"+numSubDiv+"-"+channelName);
		return new File(datadir,"AP"+numSubDiv+"-"+channelName+"b"); //TODO temp
		}
	
	
	
	/**
	 * Store profile as array on disk
	 */
	public static void printProfile(EvData data, String newLineName, String expName, String channelName, int numSubDiv, File file)
		{
		Imageset imset=data.getObjects(Imageset.class).get(0);
		EvChannel ch=imset.getChannel(channelName);
		NucLineage lin=(NucLineage)imset.metaObject.get(newLineName);
		try
			{
			StringBuffer outf=new StringBuffer();
			
			here: for(EvDecimal frame:ch.imageLoader.keySet())
				{
				outf.append(""+frame+"\t");
				for(int i=0;i<numSubDiv;i++)
					{
					NucLineage.Nuc nuc=lin.nuc.get("_slice"+i);
					NucExp nexp=nuc.exp.get(expName);
					Double level=nexp.level.get(frame);
					if(level==null)
						continue here;
					outf.append(level);
					outf.append("\t");
					}
				outf.append("\n");
				}
			EvFileUtil.writeFile(file, outf.toString());
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}
	
	public static void doProfile(EvData data, String newLineName, String expName, String channelName, int numSubDiv)
		{
		
		
		
		Imageset imset=data.getObjects(Imageset.class).get(0);

		//For all lineages
		//TODO need to group lineage and shell. introduce a new object?
		NucLineage lin=new NucLineage();
		//imset.getIdObjectsRecursive(NucLineage.class).values().iterator().next();
		Shell shell=imset.getIdObjectsRecursive(Shell.class).values().iterator().next();
		//ExpUtil.clearExp(lin, expName);

		imset.metaObject.put(newLineName, lin);
		
		
		//Virtual nuc for AP
		for(int i=0;i<numSubDiv;i++)
			lin.getCreateNuc("_slice"+i);
		
		
		
		//TreeMap<EvDecimal, Double> bgLevel=new TreeMap<EvDecimal, Double>();
		
		
		HashMap<EvDecimal, EvPixels> distanceMap=new HashMap<EvDecimal, EvPixels>();
		
		
		
		EvChannel ch=imset.getChannel(channelName);
		
		
		
		//For all frames
		System.out.println("num frames: "+imset.getChannel(channelName).imageLoader.size());
		EvDecimal firstframe=ch.imageLoader.firstKey();
		EvDecimal lastFrame=ch.imageLoader.lastKey();
		double expTime=1; //For missing frames, use last frame
		for(EvDecimal frame:ch.imageLoader.keySet())
//			if(frame.less(new EvDecimal("30000")) && frame.greater(new EvDecimal("29000")))
			{
			System.out.println();
			System.out.println(data+"    frame "+frame+" / "+firstframe+" - "+lastFrame);

			//Map<String, Double> expLevel=new HashMap<String, Double>();
			//Map<String, Integer> nucVol=new HashMap<String, Integer>();

			//Get exposure time
			String sExpTime=imset.getMetaFrame(frame).get("exposuretime");
			if(sExpTime!=null)
				expTime=Double.parseDouble(sExpTime);
			else
				System.out.println("No exposure time");
			
			int bgIntegral=0;
			int bgVolume=0;

			int[] sliceExp=new int[numSubDiv];
			int[] sliceVol=new int[numSubDiv];

			//For all z
			EvStack stack=ch.imageLoader.get(frame);
			for(Map.Entry<EvDecimal, EvImage> eim:stack.entrySet())
				{
				EvDecimal curZ=eim.getKey();
				EvImage im=eim.getValue();
				EvPixels pixels=null;
				int[] pixelsLine=null;
							
				//Load images lazily (for AP not really needed)
				if(pixels==null)
					{
					/*
					BufferedImage b=im.getJavaImage();
					pixels=new EvPixels(b);
					pixels=pixels.getReadOnly(EvPixels.TYPE_INT);
					*/
					pixels=im.getPixels().getReadOnly(EvPixelsType.INT);
					pixelsLine=pixels.getArrayInt();

					//Integrate background
					for(int i=0;i<pixels.getWidth();i++)
						bgIntegral+=pixelsLine[i];
					bgVolume+=pixels.getWidth();
					}
				
				
				//Calculate distance mask lazily
				EvPixels lenMap;
				double[] lenMapArr;
				if(distanceMap.containsKey(curZ))
					{
					lenMap=distanceMap.get(curZ);
					lenMapArr=lenMap.getArrayDouble();
					}
				else
					{
					lenMap=new EvPixels(EvPixelsType.DOUBLE, pixels.getWidth(), pixels.getHeight());
					lenMapArr=lenMap.getArrayDouble();

					ImVector2 dirvec=ImVector2.polar(shell.major, shell.angle);
					ImVector2 startpos=dirvec.add(new ImVector2(shell.midx,shell.midy));
					dirvec=dirvec.normalize().mul(-1);

					//Calculate distances
					for(int ay=0;ay<pixels.getHeight();ay++)
						{
						int lineIndex=lenMap.getRowIndex(ay);
						for(int ax=0;ax<pixels.getWidth();ax++)
							{
							//Convert to world coordinates
							ImVector2 pos=new ImVector2(stack.transformImageWorldX(ax),stack.transformImageWorldY(ay));

							//Check if this is within ellipse boundary
							ImVector2 elip=pos.sub(new ImVector2(shell.midx, shell.midy)).rotate(shell.angle); //TODO angle? what?
							double len;
							if(1 >= elip.y*elip.y/(shell.minor*shell.minor) + elip.x*elip.x/(shell.major*shell.major) )
								len=pos.sub(startpos).dot(dirvec)/(2*shell.major);	//xy . dirvecx = cos(alpha) ||xy|| ||dirvecx||
							else
								len=-1;
							lenMapArr[lineIndex+ax]=len;
							}
						}
					}
					

				
				//Integrate this area
				for(int y=0;y<pixels.getHeight();y++)
					{
					int lineIndex=pixels.getRowIndex(y);
					for(int x=0;x<pixels.getWidth();x++)
						{
						int i=lineIndex+x;
						double len=lenMapArr[i];
						if(len>-1)
							{
							int sliceNum=(int)(len*numSubDiv); //may need to bound in addition
							sliceExp[sliceNum]+=pixelsLine[i];
							sliceVol[sliceNum]++;
							}
						}
					}

				
				/*
				//Integrate this area
				int area=0;
				double exp=0;
				for(int y=0;y<pixels.getHeight();y++)
					{
					int lineIndex=pixels.getRowIndex(y);
					for(int x=0;x<pixels.getWidth();x++)
						{
						int v=pixelsLine[lineIndex+x];
						area++;
						exp+=v;
						}
					}
				*/
				}

		
			/**
			 * Store pattern in lineage
			 */
			for(int i=0;i<numSubDiv;i++)
				{
				double avg=(double)sliceExp[i]/(double)sliceVol[i];
				avg/=expTime;
		
				NucLineage.Nuc nuc=lin.getCreateNuc("_slice"+i);
				NucExp exp=nuc.getCreateExp(expName);
				exp.level.put(frame, avg);
				
				}

			
			}

		//Set override start and end times
		for(int i=0;i<numSubDiv;i++)
			{
			NucLineage.Nuc nuc=lin.getCreateNuc("_slice"+i);
			nuc.overrideStart=ch.imageLoader.firstKey();
			nuc.overrideEnd=ch.imageLoader.lastKey();
			}
		
		//TreeSet<EvDecimal> framesSorted=new TreeSet<EvDecimal>(bgLevel.keySet());

		//Normalization is needed before exposure correction to make sure the threshold for
		//detecting jumps always works
		ExpUtil.normalizeSignal(lin, expName, ExpUtil.getSignalMax(lin, expName),0,1); 

		
		//TreeMap<EvDecimal, Tuple<Double,Double>> expCorrects=
		ExpUtil.correctExposureChange(imset, lin, expName, channelName, new TreeSet<EvDecimal>(ch.imageLoader.keySet()));
		
		//This is only for the eye
		double sigMax=ExpUtil.getSignalMax(lin, expName);
		double sigMin=ExpUtil.getSignalMin(lin, expName);
		ExpUtil.normalizeSignal(lin, expName,sigMax,sigMin,1); 

		

		
		
		}
	}
