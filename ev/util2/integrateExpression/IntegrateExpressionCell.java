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
import endrov.util.EvDecimal;

/**
 * Cell-level expression level integration.
 * @author Johan Henriksson
 *
 */
public class IntegrateExpressionCell
	{
	
	
	
	/**
	 * Project sphere onto plane. Assumes resx=resy
	 * @param nucRw Radius
	 * @param nucZw Relative z
	 */
	public static Double projectSphere(double nucRw, double nucZw, double imageZw)
		{
		double dz=nucZw-imageZw;
		double tf=nucRw*nucRw-dz*dz;
		if(tf>0)
			return Math.sqrt(tf);
		else
			return null;
		}
	
	
	private static int min2(int a, int b)
		{
		return a<b? a:b;
		}

	private static int max2(int a, int b)
		{
		return a>b? a:b;
		}

	
	public static void main(String arg[])
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();

		EvData data=EvData.loadFile(new File("/Volumes/TBU_main01/ost4dgood/TB2141070621b.ost/"));


		String channelName="GFP";
		String expName="CEH-5";

		Imageset ost=data.getObjects(Imageset.class).get(0);

		//For all lineages
		NucLineage lin=data.getIdObjectsRecursive(NucLineage.class).values().iterator().next();
		
		TreeMap<EvDecimal, Integer> bgLevel=new TreeMap<EvDecimal, Integer>();
		
		
		//For all frames
		System.out.println("num frames: "+ost.getChannel(channelName).imageLoader.size());
		EvDecimal lastFrame=ost.getChannel(channelName).imageLoader.lastKey();
		for(EvDecimal frame:ost.getChannel(channelName).imageLoader.keySet())
			{
			System.out.println();
			System.out.println("frame "+frame+" / "+lastFrame);

			Map<NucPair,NucLineage.NucInterp> inter=lin.getInterpNuc(frame);

			Map<String, Double> expLevel=new HashMap<String, Double>();
			Map<String, Integer> nucVol=new HashMap<String, Integer>();

			//Get exposure time
			String sExpTime=ost.channelImages.get(channelName).metaFrame.get(frame).get("exposure"); //TODO name. write down constants? predef variables?
			double expTime=1;
			if(sExpTime!=null)
				expTime=Double.parseDouble(sExpTime);
			
			int bgIntegral=0;
			int bgVolume=0;

			//For all images
			for(Map.Entry<EvDecimal, EvImage> eim:ost.getChannel(channelName).imageLoader.get(frame).entrySet())
				{
				EvImage im=eim.getValue();
				EvPixels pixels=null;
				int[] line=null;
				double imageZw=eim.getKey().doubleValue();

				
				
				
				//For all nuc
				for(Map.Entry<NucPair,NucLineage.NucInterp> e:inter.entrySet())
//					if(e.getKey().getRight().equals("ABarappaa"))
					//	if(e.getKey().getRight().equals("AB"))
					{
					String nucName=e.getKey().snd();
					NucLineage.NucPos pos=e.getValue().pos;

					Double pr=projectSphere(pos.r, pos.z, imageZw);
					if(pr!=null)
						{
						int midSx=(int)im.transformWorldImageX(pos.x);
						int midSy=(int)im.transformWorldImageY(pos.y);
						int rS=(int)im.scaleWorldImageX(pr);
						if(rS>0)
							{
							if(!expLevel.containsKey(nucName))
								{
								expLevel.put(nucName, 0.0);
								nucVol.put(nucName, 0);
								}
							
							//Load images lazily
							if(pixels==null)
								{
								BufferedImage b=im.getJavaImage();
								pixels=new EvPixels(b);
								pixels=pixels.getReadOnly(EvPixels.TYPE_INT);
								line=pixels.getArrayInt();
								
								//Integrate background
								for(int i=0;i<pixels.getWidth();i++)
									bgIntegral+=line[i];
								bgVolume+=pixels.getWidth();
								}
							
							//Integrate this area
							int sy=max2(midSy-rS,0);
							int ey=min2(midSy+rS,pixels.getHeight());
							int sx=max2(midSx-rS,0);
							int ex=min2(midSx+rS,pixels.getWidth());
							int area=0;
							double exp=0;
							for(int y=sy;y<ey;y++)
								{
								int lineIndex=pixels.getRowIndex(y);
								for(int x=sx;x<ex;x++)
									{
									int dx=x-midSx;
									int dy=y-midSy;
									if(dx*dx+dy*dy<rS*rS)
										{
										int v=line[lineIndex+x];
										area++;
										exp+=v;
										}
									}
								}
							
							//Sum up volume and area
							nucVol.put(nucName,nucVol.get(nucName)+area);
							expLevel.put(nucName,expLevel.get(nucName)+exp);
							}
						}
					}
					

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
				
				
				
//				if(minExpLevel==null || avg<minExpLevel) minExpLevel=avg;
//				if(maxExpLevel==null || avg>maxExpLevel) maxExpLevel=avg;
				
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
