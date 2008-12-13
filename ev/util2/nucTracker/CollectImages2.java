package util2.nucTracker;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import endrov.basicWindow.EvColor;
import endrov.data.*;
import endrov.ev.*;
import endrov.imageset.EvImage;
import endrov.imageset.Imageset;
import endrov.nuc.NucLineage;
import endrov.util.EvDecimal;


public class CollectImages2
	{
	

	public static NucLineage getLin(EvContainer ost)
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
		
		String channelName="DIC";
//		String channelName="RFP";
		
		boolean doTrue=true;

		
		File outputDir;
		if(doTrue)
			outputDir=new File("/Volumes/TBU_main03/userdata/henriksson/traintrack2/"+channelName+"/true/");
		else
			outputDir=new File("/Volumes/TBU_main03/userdata/henriksson/traintrack2/"+channelName+"/false/");
		
		EvDecimal startFrame=new EvDecimal(0);
		EvDecimal endFrame=new EvDecimal(100000000);
		
		//Load all worms
		String[] wnlist;
		
		if(channelName.equals("DIC"))
			wnlist=new String[]{
					"/Volumes/TBU_main02/ost4dgood/N2_071114.ost",
/*					"/Volumes/TBU_main02/ost4dgood/N2_071115",
					"/Volumes/TBU_main02/ost4dgood/N2_071116",
					"/Volumes/TBU_main02/ost4dgood/N2_071117",
//					"/Volumes/TBU_main02/ost4dgood/N2_071118",
					"/Volumes/TBU_main02/ost4dgood/N2greenLED080206",
					"/Volumes/TBU_main02/ost4dgood/TB2164_080118",
					"/Volumes/TBU_main02/ost4dgood/TB2142_071129",*/
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
				worms.addAll(ost.getObjects(Imageset.class));
			}

		int maxImages=1000;
		
		try
			{
			int id=0;
			
			PrintWriter pw=new PrintWriter(new FileWriter(new File(outputDir.getParent(),"imtrue.txt")));
			
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
								
								BufferedImage scaledIm=new BufferedImage(20,20,BufferedImage.TYPE_BYTE_GRAY);
								Graphics2D sg=(Graphics2D)scaledIm.getGraphics();
								double sgs=20.0/(2*rr);
								sg.scale(sgs, sgs);
								sg.drawImage(subim, 0, 0, null);
								
								double[] samples=new double[20*20];
								scaledIm.getRaster().getSamples(0, 0, 20, 20, 0, samples);
								for(double s:samples)
									pw.print(s+"\t");
								pw.println();
								
									
								
								
								//File file = new File(outputDir,""+id+".png");
								id++;
				        //ImageIO.write(subim, "png", file);
				        
				        if(id==maxImages)
				        	{
				        	pw.close();
				        	System.exit(0);
				        	}
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
