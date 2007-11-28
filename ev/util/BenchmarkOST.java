package util;

import evplugin.ev.*;
import evplugin.imagesetOST.*;
import java.io.File;

/**
 * Go through all imagesets in a directory and run the MakeQT plugin
 * @author Johan Henriksson
 */
public class BenchmarkOST
	{
	
	
	/**
	 * Entry point
	 * @param arg Command line arguments
	 */
	public static void main(String[] arg)
		{
		//Log.listeners.add(new StdoutLog());
		EV.loadPlugins();

		arg=new String[]{
					"/Volumes/TBU_xeon01_500GB01/ost4dgood/",
					"/Volumes/TBU_xeon01_500GB02/ost3dgood/",
					"/Volumes/TBU_xeon01_500GB02/ost4dgood/"					
		};
		int num=0;
		for(String s:arg)
			for(File file:(new File(s)).listFiles())
				if(file.isDirectory())
					{
					new OstImageset(file.getPath());
					num++;
					if(num==1)
						{
						System.gc();
						return;
						}
					}
		}
	}
