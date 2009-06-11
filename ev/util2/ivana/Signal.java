package util2.ivana;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.Log;
import endrov.ev.StdoutLog;
import endrov.imageset.EvChannel;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.Imageset;
import endrov.unsortedImageFilters.threshold.EvOpOtsuThreshold2D;
import endrov.util.EvFileUtil;

public class Signal 
	{
	
	public static EvPixels getTheImage(EvData data)
		{
		if(data==null)
			System.out.println("No such file");
		EvChannel im=data.getIdObjectsRecursive(EvChannel.class).values().iterator().next();
		EvImage evim=im.imageLoader.values().iterator().next().entrySet().iterator().next().getValue();
		EvPixels pixels=evim.getPixels().convertTo(EvPixels.TYPE_DOUBLE, true);
		return pixels;
		}
	
	public static void doDirectory(String basedir)
		{
		try
			{
			StringBuffer sb=new StringBuffer();
			
			for(int cf=2;/*cf<49*/;cf+=2)
				{
				
				File dicf=new File(basedir + "010609_AT2633_D1_NR-"+EV.pad(cf, 4)+"-.tif");
				File sigf=new File(basedir + "010609_AT2633_D1_NR-"+EV.pad(cf+1, 4)+"-.tif");
				
				//Stop if no more files
				if(!dicf.exists())
					break;
				
				System.out.println(dicf);
				EvPixels pixelsDic=getTheImage(EvData.loadFile(dicf));
				double[] pdic=pixelsDic.getArrayDouble();
				EvPixels pixelsSig=getTheImage(EvData.loadFile(sigf));
				double[] psig=pixelsSig.getArrayDouble();
				
				double dicThreshold=EvOpOtsuThreshold2D.findOtsuThreshold(pixelsDic);
				
				int count=0;
				double sum=0;
				for(int i=0;i<pdic.length;i++)
					if(pdic[i]>dicThreshold)
						{
						count++;
						sum+=psig[i];
						}
				sum/=count;
				
				sb.append(cf+"\t"+sum+"\n");
				
				System.out.println("========================================Average "+sum+"\t\tthres "+dicThreshold);
				}
			
			EvFileUtil.writeFile(new File(basedir+"dat.txt"), sb.toString());
			
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}
	

	
	public static void main(String[] args)
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();
		
		doDirectory("/home/ivana/lab_data/lab_journal/2009/data2009_06/Nile_Red_20C/01062009_Nilered/AT2633/AT2633_NR/");
		
		System.exit(0);
		
		}
	}
