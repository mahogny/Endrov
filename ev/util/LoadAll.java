package util;

import endrov.data.*;
import endrov.ev.*;

import java.io.*;

/**
 * Go through and load all imagesets. This will trigger a format update if needed.
 * @author Johan Henriksson
 */
public class LoadAll
	{
	public static void extractRot(File file)
		{
		EvData data=EvData.loadFile(file);
		if(data==null)
			System.out.println("WTF: ==================== "+file);
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
			arg=new String[]{"/Volumes/TBU_main03/daemon/output"};
		for(String s:arg)
			for(File file:(new File(s)).listFiles())
				if(file.isDirectory())
					{
					extractRot(file);
					}
		System.out.println("done");
		}
	}
