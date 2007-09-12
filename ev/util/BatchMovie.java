package util;
import java.io.File;

import evplugin.ev.BatchThread;
import evplugin.ev.CompleteBatch;
import evplugin.ev.EV;
import evplugin.imageset.OstImageset;
import evplugin.makeQT.CalcThread;


/**
 * Go through all imagesets in a directory and run the MakeQT plugin
 * @author Johan Henriksson
 */
public class BatchMovie
	{
	
	public static void makeMovie(File file)
		{
		System.out.println("Converting imageset "+file.getPath());
		OstImageset ost=new OstImageset(file.getPath());
		BatchThread c=new CalcThread(ost, 0, 1000000, 35);
		new CompleteBatch(c);
		}
	
	/**
	 * Entry point
	 * @param args Command line arguments
	 */
	public static void main(String[] arg)
		{
		EV.loadPlugins();

		if(arg.length==0)
			arg=new String[]{"/Volumes/TBU_xeon01_500GB01/final_recordings"};
		for(String s:arg)
			for(File file:(new File(s)).listFiles())
				if(file.isDirectory())
					makeMovie(file);
		}
	}
