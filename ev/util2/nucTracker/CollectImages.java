package util2.nucTracker;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;

import endrov.data.*;
import endrov.ev.*;
import endrov.imageset.EvImage;
import endrov.imageset.Imageset;
import endrov.imagesetOST.OstImageset;
import endrov.nuc.NucLineage;
import endrov.util.EvDecimal;


public class CollectImages
	{
	
	public static boolean doTrue=false;

	public static NucLineage getLin(Imageset ost)
		{
		for(EvObject evob:ost.metaObject.values())
			{
			if(evob instanceof NucLineage)
				{
				NucLineage lin=(NucLineage)evob;
				return lin;
				}
			}
		return null;
		}
	
	public static void main(String[] args)
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();
		
//		String channelName="DIC";
		String channelName="RFP";
		
		
		File outputDir;
		if(doTrue)
			outputDir=new File("/Volumes/TBU_main03/userdata/henriksson/traintrack/"+channelName+"/images/true/");
		else
			outputDir=new File("/Volumes/TBU_main03/userdata/henriksson/traintrack/"+channelName+"/images/false/");
		
		EvDecimal startFrame=new EvDecimal(0);
		EvDecimal endFrame=new EvDecimal(100000000);
		
		//Load all worms
		String[] wnlist;
		
		if(channelName.equals("DIC"))
			wnlist=new String[]{
					"/Volumes/TBU_main02/ost4dgood/N2_071114",
					"/Volumes/TBU_main02/ost4dgood/N2_071115",
					"/Volumes/TBU_main02/ost4dgood/N2_071116",
					"/Volumes/TBU_main02/ost4dgood/N2_071117",
//					"/Volumes/TBU_main02/ost4dgood/N2_071118",
					"/Volumes/TBU_main02/ost4dgood/N2greenLED080206",
					"/Volumes/TBU_main02/ost4dgood/TB2164_080118",
					"/Volumes/TBU_main02/ost4dgood/TB2142_071129",
		}; 
		else
			{
			wnlist=new String[]{
					"/Volumes/TBU_main02/ost4dgood/TB2164_080118",
					"/Volumes/TBU_main02/ost4dgood/TB2142_071129",
			};
			startFrame=new EvDecimal(1500);
			endFrame=new EvDecimal(2000);
			}
		
		
		Vector<Imageset> worms=new Vector<Imageset>();
		for(String s:wnlist)
			{
			Imageset ost=new OstImageset(new File(s));
			if(getLin(ost)!=null)
				worms.add(ost);
			}

		try
			{
			int id=0;
			
			//For all lineages
			for(Imageset ost:worms)
				{
				NucLineage lin=getLin(ost);
				
				//For all nuc
				for(Map.Entry<String, NucLineage.Nuc> e2:lin.nuc.entrySet())
					{
					NucLineage.Nuc nuc=e2.getValue();
					String n=e2.getKey();
					if(!(n.startsWith(":") || n.startsWith("shell") || n.equals("ant") || n.equals("post") || n.equals("venc") || n.equals("P") || n.indexOf('?')>=0 || n.indexOf('_')>=0 || n.equals("2ftail") || n.equals("germline")))
						for(Map.Entry<EvDecimal, NucLineage.NucPos> e:nuc.pos.entrySet())
							{
							EvDecimal frame=e.getKey();
							if(frame.greaterEqual(startFrame) && frame.lessEqual(endFrame))
								{
								NucLineage.NucPos pos=e.getValue();
								
								frame=ost.getChannel(channelName).closestFrame(frame);
								EvDecimal z=ost.getChannel(channelName).closestZ(frame, new EvDecimal(pos.z/**ost.meta.resZ*/));
								EvImage im=ost.getChannel(channelName).getImageLoader(frame, z);
								
								int midx=(int)im.transformWorldImageX(pos.x);
								int midy=(int)im.transformWorldImageY(pos.y);
								int r=(int)im.scaleWorldImageX(pos.r);
//								int rr=r+20;
								int rr=(int)(r*2);
	
								if(!doTrue)
									{
									double ang=Math.random()*2*Math.PI;
									midx+=Math.cos(ang)*r*2;
									midy+=Math.sin(ang)*r*2;
									}
								
								BufferedImage jim=im.getJavaImage();
								BufferedImage subim=new BufferedImage(2*rr, 2*rr, jim.getType());
								
								int ulx=midx-rr;
								int uly=midy-rr;
								subim.getGraphics().drawImage(jim, 0, 0, 2*rr, 2*rr, ulx, uly, ulx+2*rr, uly+2*rr, null);
								
								
								File file = new File(outputDir,""+id+".png");
								id++;
				        ImageIO.write(subim, "png", file);
								}
							
							}
					}
				
				
				
				
				}
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		
		
		
		
		
		}
	
	
			
	
	

	}
