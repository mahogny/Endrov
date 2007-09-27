package util;

import evplugin.ev.*;
import evplugin.embrot.*;
import evplugin.imageset.*;
import java.io.*;

/**
 * Go through all imagesets in a directory and run the MakeQT plugin
 * @author Johan Henriksson
 */
public class BatchRot
	{
	public static void extractRot(File file)
		{
//		System.out.println("")
		Imageset rec=new OstImageset(file.getAbsolutePath());
		CmdEmbrot.dumprot(rec);
		}
	
	/**
	 * Entry point
	 * @param args Command line arguments
	 */
	public static void main(String[] arg)
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();

		
		if(arg.length==0)
			arg=new String[]{"/Volumes/TBU_xeon01_500GB01/final_recordings/","/Volumes/TBU_xeon01_500GB01/daemon/output/","/Volumes/TBU_xeon01_500GB01/daemonoutput_mirror/",
					"/Volumes/TBU_xeon01_500GB01/4D_recordings_until_070909/"};
		for(String s:arg)
			for(File file:(new File(s)).listFiles())
				if(file.isDirectory())
					extractRot(file);
		}
	}
