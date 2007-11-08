package util;

import evplugin.ev.*;
import evplugin.imagesetOST.*;
import evplugin.makeQT.*;

import java.util.*;
import java.io.File;

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
		Vector<CalcThread.MovieChannel> channelNames=new Vector<CalcThread.MovieChannel>();
		ost.channelImages.containsKey("GFPmax")
			channelNames.add(new CalcThread.MovieChannel("GFPmax",false));
		ost.channelImages.containsKey("RFPmax")
			channelNames.add(new CalcThread.MovieChannel("RFPmax",false));
		channelNames.add(new CalcThread.MovieChannel("DIC",true));
		BatchThread c=new CalcThread(ost, 0, 1000000, 35, channelNames, 336, "h.264 (MPEG-4)", "High"); 
		new CompleteBatch(c);
		}
	
	/**
	 * Entry point
	 * @param arg Command line arguments
	 */
	public static void main(String[] arg)
		{
		EV.loadPlugins();

		if(arg.length==0)
			arg=new String[]{
					"/Volumes/TBU_xeon01_500GB01/ost3dfailed/",
					"/Volumes/TBU_xeon01_500GB01/ost3dgood/",
					"/Volumes/TBU_xeon01_500GB01/ost4dfailed/",
					"/Volumes/TBU_xeon01_500GB01/ost4dgood/",
					"/Volumes/TBU_xeon01_500GB02/ost3dfailed/",
					"/Volumes/TBU_xeon01_500GB02/ost3dgood/",
					"/Volumes/TBU_xeon01_500GB02/ost4dfailed/",
					"/Volumes/TBU_xeon01_500GB02/ost4dgood/"
					
		};
		for(String s:arg)
			for(File file:(new File(s)).listFiles())
				if(file.isDirectory())
					makeMovie(file);
		}
	}
