package util2;

import evplugin.data.*;
import evplugin.ev.*;

import java.io.*;

/**
 * Go through and load all imagesets. Useful to update all imagesets.
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
