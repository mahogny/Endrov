/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util;
import java.io.*;

import endrov.data.EvData;
import endrov.ev.*;


/**
 * Go through all OST imagesets in a directory and resave them. This will cause them to be resaved in the
 * latest version.
 * @author Johan Henriksson
 */
public class BatchOST
	{
	public static void makeOST(File file)
		{
		if(file.getName().endsWith(".ost"))
			{
			System.out.println("----- "+file);
			EvData.loadFile(file);
			}
		
		
		}
	
	public static void main(String[] arg)
		{
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();

		
//		if(arg.length==0)
		arg=new String[]{
				/*
				"/Volumes/TBU_main01/ost3dfailed/","/Volumes/TBU_main01/ost4dfailed/",
				"/Volumes/TBU_main01/ost3dgood/","/Volumes/TBU_main01/ost4dgood",
				"/Volumes/TBU_main02/ost3dfailed/","/Volumes/TBU_main02/ost4dfailed/",
				"/Volumes/TBU_main02/ost3dgood/","/Volumes/TBU_main02/ost4dgood",

				
				"/Volumes/TBU_main03/ost3dfailed/","/Volumes/TBU_main03/ost4dfailed/",
				*/
				"/Volumes/TBU_main03/ost3dgood/","/Volumes/TBU_main03/ost4dgood",

				"/Volumes/TBU_main04/ost3dfailed/","/Volumes/TBU_main04/ost4dfailed/",
				"/Volumes/TBU_main04/ost3dgood/","/Volumes/TBU_main04/ost4dgood",
			};
		for(String s:arg)
			if(new File(s).isDirectory())
				for(File file:(new File(s)).listFiles())
					makeOST(file);
		System.exit(0);
		}

	}
