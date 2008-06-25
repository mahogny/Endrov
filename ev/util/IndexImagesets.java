package util;
import java.io.File;

import evplugin.ev.EV;
import evplugin.imagesetOST.*;

/**
 * Run through all imagesets in a folder and generate imagecache.txt in them
 * @author Johan Henriksson
 */
public class IndexImagesets
	{
	public static void index(File file)
		{
		if(!(new File(file,"imagecache.txt")).exists())
			{
			System.out.println("Indexing imageset "+file.getPath());
			new OstImageset(file);
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
