package util;
import java.io.File;

import endrov.data.EvData;
import endrov.ev.*;
import endrov.imageset.Imageset;
import endrov.makeMax.CalcThread;


/**
 * Go through all imagesets in a directory and create a max channel for GFP
 * @author Johan Henriksson
 */
public class BatchMax
	{
	public static void makeMax(File file)
		{
		System.out.println("Imageset "+file.getPath());
		EvData data=EvData.loadFile(file);
		Imageset im=data.getObjects(Imageset.class).get(0);
		
//		OstImageset ost=new OstImageset(file);
//		BatchThread c=new CalcThread(ost, 0, 1000000, "GFP",0.99);		
		BatchThread c=new CalcThread(im, 0, 1000000, "GFP");
		new CompleteBatch(c);
		//ost.saveMeta();
		//TODO. need to save 
		}
	
	/**
	 * Entry point
	 * @param arg Command line arguments
	 */
	public static void main(String[] arg)
		{
		EV.loadPlugins();

		if(arg.length==0)
			arg=new String[]{"/Volumes/TBU_xeon01_500GB01/needmax/"};
		for(String s:arg)
			for(File file:(new File(s)).listFiles())
				if(file.isDirectory())
					makeMax(file);
		}
	}
