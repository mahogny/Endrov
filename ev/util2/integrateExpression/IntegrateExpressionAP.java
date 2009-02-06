package util2.integrateExpression;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import endrov.data.EvData;
import endrov.ev.*;
import endrov.imageset.*;
import endrov.nuc.*;
import endrov.shell.Shell;
import endrov.util.EvDecimal;
import endrov.util.Vector2D;

/**
 * Anterior-posterior expression integration. Whole-embryo corresponds to numSlices=1 
 * @author Johan Henriksson
 *
 */
public class IntegrateExpressionAP
	{
/*	
	private static int min2(int a, int b)
		{
		return a<b? a:b;
		}

	private static int max2(int a, int b)
		{
		return a>b? a:b;
		}

	*/
	public static void main(String arg[])
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();

		EvData data=EvData.loadFile(new File("/Volumes/TBU_main01/ost4dgood/TB2141070621b.ost/"));


		String channelName="GFP";
		String expName="CEH-5";
		int numSubDiv=20;

		
		
		
		Imageset imset=data.getObjects(Imageset.class).get(0);

		//For all lineages
		//TODO need to group lineage and shell. introduce a new object?
		NucLineage lin=imset.getIdObjectsRecursive(NucLineage.class).values().iterator().next();
		Shell shell=imset.getIdObjectsRecursive(Shell.class).values().iterator().next();
		
		TreeMap<EvDecimal, Integer> bgLevel=new TreeMap<EvDecimal, Integer>();
		
		
		HashMap<EvDecimal, EvPixels> distanceMap=new HashMap<EvDecimal, EvPixels>();
		
		
		
		
		
		//For all frames
		System.out.println("num frames: "+imset.getChannel(channelName).imageLoader.size());
		EvDecimal lastFrame=imset.getChannel(channelName).imageLoader.lastKey();
		for(EvDecimal frame:imset.getChannel(channelName).imageLoader.keySet())
			{
			System.out.println();
			System.out.println("frame "+frame+" / "+lastFrame);

			Map<String, Double> expLevel=new HashMap<String, Double>();
			Map<String, Integer> nucVol=new HashMap<String, Integer>();

			//Get exposure time
			String sExpTime=imset.channelImages.get(channelName).metaFrame.get(frame).get("exposure"); //TODO name. write down constants? predef variables?
			double expTime=1;
			if(sExpTime!=null)
				expTime=Double.parseDouble(sExpTime);
			
			int bgIntegral=0;
			int bgVolume=0;

			int[] sliceExp=new int[numSubDiv];
			int[] sliceVol=new int[numSubDiv];

			//For all z
			EvChannel ch=imset.channelImages.get(channelName);
			for(Map.Entry<EvDecimal, EvImage> eim:ch.imageLoader.get(frame).entrySet())
				{
				EvDecimal curZ=eim.getKey();
				EvImage im=eim.getValue();
				EvPixels pixels=null;
				int[] pixelsLine=null;
							
				//Load images lazily (for AP not really needed)
				if(pixels==null)
					{
					BufferedImage b=im.getJavaImage();
					pixels=new EvPixels(b);
					pixels=pixels.getReadOnly(EvPixels.TYPE_INT);
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
					lenMap=new EvPixels(EvPixels.TYPE_DOUBLE, pixels.getWidth(), pixels.getHeight());
					lenMapArr=lenMap.getArrayDouble();

					Vector2D dirvec=Vector2D.polar(shell.major, shell.angle);
					Vector2D startpos=dirvec.add(new Vector2D(shell.midx,shell.midy));
					dirvec=dirvec.normalize().mul(-1);

					//Calculate distances
					for(int ay=0;ay<pixels.getHeight();ay++)
						{
						int lineIndex=lenMap.getRowIndex(ay);
						for(int ax=0;ax<pixels.getWidth();ax++)
							{
							//Convert to world coordinates
							Vector2D pos=new Vector2D(im.transformImageWorldX(ax),im.transformImageWorldY(ay));

							//Check if this is within ellipse boundary
							Vector2D elip=pos.sub(new Vector2D(shell.midx, shell.midy)).rotate(shell.angle); //TODO angle? what?
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
							int sliceNum=(int)(len*20); //may need to bound in addition
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

			
			for(int i=0;i<numSubDiv;i++)
				{
				double avg=(double)sliceExp[i]/(double)sliceVol[i];
				avg/=expTime;
				
				/*
				NucExp exp=lin.nuc.get(nucName).getExpCreate(expName);
				if(lin.nuc.get(nucName).pos.lastKey().greaterEqual(frame) && 
						lin.nuc.get(nucName).pos.firstKey().lessEqual(frame)) 
					exp.level.put(frame,avg);*/
				
//				if(minExpLevel==null || avg<minExpLevel) minExpLevel=avg;
//				if(maxExpLevel==null || avg>maxExpLevel) maxExpLevel=avg;
				}
		



			//Store bglevel in list
			if(bgVolume!=0)
				{
				bgLevel.put(frame, bgIntegral/bgVolume);
				System.out.println("BG: "+bgLevel.get(frame));
				}



			
			
			
			//Store value in XML
			for(String nucName:expLevel.keySet())
				{
				double avg=expLevel.get(nucName)/nucVol.get(nucName);
				avg/=expTime;
				//				System.out.println(nucName+" "+avg);
				NucExp exp=lin.nuc.get(nucName).getExpCreate(expName);
				if(lin.nuc.get(nucName).pos.lastKey().greaterEqual(frame) && 
						lin.nuc.get(nucName).pos.firstKey().lessEqual(frame)) 
					exp.level.put(frame,avg);




				}

			}

/*
		//Subtract background. 
		//TODO But using minExpLevel, I don't like it. should use some image average. border? first line?
		double expSize=maxExpLevel-minExpLevel;
		for(NucLineage.Nuc nuc:lin.nuc.values())
			if(nuc.exp.containsKey(expName))
				for(Map.Entry<EvDecimal, Double> e:nuc.exp.get(expName).level.entrySet())
					{
					nuc.exp.get(expName).level.put(e.getKey(), (e.getValue()-minExpLevel)*5);
					}
		
	*/	
		
		//data.saveData();

		}
	}
