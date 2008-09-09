package util2;

import endrov.data.*;
import endrov.ev.*;

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
		EvData rec=new EvDataXML(new File(file,"rmd.ostxml").getAbsolutePath());
		//Imageset rec=new OstImageset(file.getAbsolutePath());
		CmdEmbrot.dumprot(file.getName(), rec);
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
			arg=new String[]{"/Volumes/TBU_main01/ost4dgood/","/Volumes/TBU_main02/ost4dgood/"};
		for(String s:arg)
			for(File file:(new File(s)).listFiles())
				if(file.isDirectory())
					extractRot(file);
		}
	}
