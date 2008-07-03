package util;

import endrov.ev.*;
import endrov.filter.FilterSeq;
import endrov.filter.FilterSlice;
import endrov.filterBasic.ContrastBrightnessFilter;
import endrov.imagesetOST.*;
import endrov.makeQT.*;

import java.util.*;
import java.io.File;

/**
 * Go through all imagesets in a directory and run the MakeQT plugin
 * @author Johan Henriksson
 */
public class BatchMovie
	{
	public static boolean first=true;
	public static String getchdesc()
		{
		String s=first?"<channel/> (<frame/>)" : "<channel/>";
		first=false;
		return s;
		}
	
	public static void makeMovie(File file)
		{
		first=true;
		System.out.println("Imageset "+file.getPath());
		long currentTime=System.currentTimeMillis();
		OstImageset ost=new OstImageset(file);
		System.out.println(" timeX "+(System.currentTimeMillis()-currentTime));
		
		FilterSeq fsNull=new FilterSeq();
		FilterSeq fsDIC=new FilterSeq();
		
		FilterSlice filterCB=new ContrastBrightnessFilter();
		fsDIC.addFilter(filterCB);
		
		Vector<CalcThread.MovieChannel> channelNames=new Vector<CalcThread.MovieChannel>();
		if(ost.channelImages.containsKey("GFPmax"))
			channelNames.add(new CalcThread.MovieChannel("GFPmax",fsNull,getchdesc()));
		if(ost.channelImages.containsKey("RFPmax"))
			channelNames.add(new CalcThread.MovieChannel("RFPmax",fsNull,getchdesc()));
		channelNames.add(new CalcThread.MovieChannel("DIC",fsDIC,getchdesc()));
		System.out.println("Now making movie");
		BatchThread c=new CalcThread(ost, 0, 1000000, 35, channelNames, 336, "h.264 (MPEG-4)", "High"); 
		new CompleteBatch(c); //bad to have everything in constructor
		System.out.println("Movie done");
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
					"/Volumes/TBU_xeon01_500GB02/ost3dgood/",
					"/Volumes/TBU_xeon01_500GB02/ost4dgood/"
					
		};
		for(String s:arg)
			for(File file:(new File(s)).listFiles())
				if(file.isDirectory())
					{
					long currentTime=System.currentTimeMillis();
					makeMovie(file);
					System.out.println(" timeY "+(System.currentTimeMillis()-currentTime));
					}
		}
	}
