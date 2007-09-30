package util;
import java.io.File;

import evplugin.ev.*;
import evplugin.imagesetOST.OstImageset;
import evplugin.makeMax.CalcThread;


/**
 * Go through all imagesets in a directory and run the MakeQT plugin
 * @author Johan Henriksson
 */
public class BatchMax
	{
	public static void makeMax(File file)
		{
		System.out.println("Imageset "+file.getPath());
		OstImageset ost=new OstImageset(file.getPath());
//		BatchThread c=new CalcThread(ost, 0, 1000000, "GFP",0.99);		
		BatchThread c=new CalcThread(ost, 0, 1000000, "GFP");
		new CompleteBatch(c);
		//TODO. need to save 
		}
	
	/**
	 * Entry point
	 * @param args Command line arguments
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
