/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util;
import java.io.File;

import endrov.data.EvData;
import endrov.ev.EV;

/**
 * Run through all imagesets in a folder and generate imagecache.txt in them. Will ignore
 * imagesets which already have it.
 * @author Johan Henriksson
 */
public class IndexImagesets
	{
	public static void index(File file)
		{
		if(!(new File(file,"imagecache.txt")).exists())
			{
			System.out.println("Indexing imageset "+file.getPath());
			EvData.loadFile(file);
			}
		}
	
	/**
	 * Entry point
	 * @param arg Command line arguments
	 */
	public static void main(String[] arg)
		{
		EV.loadPlugins();

//		if(arg.length==0)
//			arg=new String[]{"/Volumes/TBU_xeon01_500GB01/needmax/"};
		for(String s:arg)
			for(File file:(new File(s)).listFiles())
				if(file.isDirectory())
					index(file);
		}
	}
