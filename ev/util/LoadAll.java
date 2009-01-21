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
	public static void load(File file)
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

		/**
		 * <rep dir="/Volumes/TBU_main01/ost3dgood" />
    <rep dir="/Volumes/TBU_main01/ost3dfailed" />
    <rep dir="/Volumes/TBU_main01/ost4dgood" />
    <rep dir="/Volumes/TBU_main01/ost4dfailed" />
    <rep dir="/Volumes/TBU_main01/ostxml" />
    <rep dir="/Volumes/TBU_main02/ost3dgood" />
    <rep dir="/Volumes/TBU_main02/ost3dfailed" />
    <rep dir="/Volumes/TBU_main02/ost4dgood" />
    <rep dir="/Volumes/TBU_main02/ost4dfailed" />
    <rep dir="/Volumes/TBU_main02/ostxml" />
    <rep dir="/Volumes/TBU_main03/ost3dgood" />
    <rep dir="/Volumes/TBU_main03/ost3dfailed" />
    <rep dir="/Volumes/TBU_main03/ost4dgood" />
    <rep dir="/Volumes/TBU_main03/ost4dfailed" />
    <rep dir="/Volumes/TBU_main03/ostxml" />
    <rep dir="/home/mahogny/_imagedata/ost" />

		 */
		
		/*/home/tbudev3/imserv*/
		
		if(arg.length==0)
			arg=new String[]{
					"/Volumes/TBU_main01/ost3dgood",
					"/Volumes/TBU_main02/ost3dgood",
					"/Volumes/TBU_main03/ost3dgood"};
		for(String s:arg)
			for(File file:(new File(s)).listFiles())
				if(file.isDirectory())
					{
					load(file);
					}
		System.out.println("done");
		System.exit(0);
		}
	}
