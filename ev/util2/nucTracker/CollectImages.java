/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.nucTracker;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import endrov.data.*;
import endrov.ev.*;
import endrov.imageset.EvImage;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.lineage.Lineage;
import endrov.util.EvDecimal;
import endrov.util.ProgressHandle;


public class CollectImages
	{
	
	public static boolean doTrue=false;

	public static Lineage getLin(EvContainer ost)
		{
		for(EvObject evob:ost.metaObject.values())
			{
			if(evob instanceof Lineage)
				{
				Lineage lin=(Lineage)evob;
				return lin;
				}
			}
		return null;
		}
	
	public static void main(String[] args)
		{
		EvLog.addListener(new EvLogStdout());
		EV.loadPlugins();
		
//		String channelName="DIC";
		String channelName="RFP";
		
		ProgressHandle ph=new ProgressHandle(); 
		
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
			EvData ost=EvData.loadFile(new File(s));
			if(getLin(ost)!=null)
				{
				worms.addAll(ost.getObjects(Imageset.class));
				}
			}

		try
			{
			int id=0;
			
			//For all lineages
			for(Imageset ost:worms)
				{
				Lineage lin=getLin(ost);
				
				//For all nuc
				for(Map.Entry<String, Lineage.Particle> e2:lin.particle.entrySet())
					{
					Lineage.Particle nuc=e2.getValue();
					String n=e2.getKey();
					if(!(n.startsWith(":") || n.startsWith("shell") || n.equals("ant") || n.equals("post") || n.equals("venc") || n.equals("P") || n.indexOf('?')>=0 || n.indexOf('_')>=0 || n.equals("2ftail") || n.equals("germline")))
						for(Map.Entry<EvDecimal, Lineage.ParticlePos> e:nuc.pos.entrySet())
							{
							EvDecimal frame=e.getKey();
							if(frame.greaterEqual(startFrame) && frame.lessEqual(endFrame))
								{
								Lineage.ParticlePos pos=e.getValue();
								
								frame=ost.getChannel(channelName).closestFrame(frame);
								//EvDecimal z=ost.getChannel(channelName).closestZ(frame, new EvDecimal(pos.z/**ost.meta.resZ*/));
								EvStack stack=ost.getChannel(channelName).getStack(ph, frame);
								int closestZ=stack.closestZint(pos.z);
								EvImage im=stack.getInt(closestZ);//ost.getChannel(channelName).getImageLoader(frame, z);
								
								Vector2d mid=stack.transformWorldImage(new Vector2d(pos.x, pos.y));
								int midx=(int)mid.x;//stack.transformWorldImageX(pos.x);
								int midy=(int)mid.y;//stack.transformWorldImageY(pos.y);
								int r=(int)stack.scaleWorldImage(new Vector3d(pos.r,0,0)).x; //hack
//								int rr=r+20;
								int rr=(int)(r*2);
	
								if(!doTrue)
									{
									double ang=Math.random()*2*Math.PI;
									midx+=Math.cos(ang)*r*2;
									midy+=Math.sin(ang)*r*2;
									}
								
								BufferedImage jim=im.getPixels(ph).quickReadOnlyAWT();
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
