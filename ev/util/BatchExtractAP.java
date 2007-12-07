package util;

import evplugin.ev.*;
import evplugin.imagesetOST.*;
import evplugin.nuc.*;
import evplugin.data.*;
import evplugin.imageset.*;

import java.util.*;
import java.io.File;
import java.awt.image.*;

/**
 * Go through all imagesets in a directory and run the MakeQT plugin
 * @author Johan Henriksson
 */
public class BatchExtractAP
	{
	
	public static void calcAP(File file)
		{
		System.out.println("Imageset "+file.getPath());
		long currentTime=System.currentTimeMillis();
		OstImageset ost=new OstImageset(file.getPath());
	
		for(EvObject evob:ost.metaObject.values()) // go through all objects in XML file
			{
			if(evob instanceof NucLineage) // if the current object is a Lineage
				{
				NucLineage lin=(NucLineage)evob;
	
				if(lin.nuc.get("post")!=null && lin.nuc.get("ant")!=null)
					{
	
					Imageset.ChannelImages ch=ost.channelImages.get("GFP");
					TreeMap<Integer, TreeMap<Integer, EvImage>> images=ch.imageLoader;
					
					for(int frame:images.keySet())
						{
						TreeMap<Integer, EvImage> zs=images.get(frame);
						for(int z:zs.keySet())
							{
							EvImage evim=zs.get(z);
							
							//get exposure
							double exptime=0;
		    			String exptimes=ch.getFrameMeta(frame, "exposuretime");
		    			if(exptimes!=null)
		    				exptime=Double.parseDouble(exptimes);
		    			else
		    				System.out.println("No exposure time for frame "+frame);

		    			//get image
							BufferedImage bufi=evim.getJavaImage();
							Raster r=bufi.getData();
							int w=bufi.getWidth();
							int h=bufi.getHeight();						
							
							//read a pixel
							int x=0;
							int y=0;
							double[] pix=new double[3];
							r.getPixel(x, y, pix);
							double p=pix[0];
		    			
							
							NucLineage.NucPos antpos=getpos(lin, "ant", frame);
							
							double imx=evim.transformWorldImageX(antpos.x);
							double imy=evim.transformWorldImageY(antpos.y);
							double worldz=z/ost.meta.resZ;
							
							
							
							}
	
						}
	
					}
	
				}
			}
	
	
	
		System.out.println(" timeX "+(System.currentTimeMillis()-currentTime));
		System.out.println("AP done");
		}

	public static NucLineage.NucPos getpos(NucLineage lin, String name, int frame)
		{
		//Get position
		NucLineage.Nuc nuc=lin.nuc.get(name);
		NucLineage.NucPos pos;
		Map<Integer,NucLineage.NucPos> hm=nuc.pos.headMap(frame);
		if(hm.isEmpty())
			pos=nuc.pos.get(nuc.pos.firstKey());
		else
			pos=hm.get(nuc.pos.lastKey());
		return pos;
		}
	
	
	/**
	 * Entry point
	 * @param arg Command line arguments
	 */
	public static void main(String[] arg)
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();
	
		if(arg.length==0)
			arg=new String[]{
					/*					"/Volumes/TBU_xeon01_500GB01/ost3dfailed/",
						"/Volumes/TBU_xeon01_500GB01/ost3dgood/",*/
					"/Volumes/TBU_xeon01_500GB01/ost4dgood/",
					"/Volumes/TBU_xeon01_500GB02/ost4dgood/"
	
		};
		for(String s:arg)
			for(File file:(new File(s)).listFiles())
				if(file.isDirectory())
					{
					long currentTime=System.currentTimeMillis();
					calcAP(file);
					System.out.println(" timeY "+(System.currentTimeMillis()-currentTime));
					}
		}
	}
