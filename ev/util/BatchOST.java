package util;
import java.io.*;

import evplugin.imageset.*;
import evplugin.imagesetOST.OstImageset;

/**
 * Go through all OST imagesets in a directory and resave them. This will cause them to be resaved in the
 * last version.
 * @author Johan Henriksson
 */
public class BatchOST
	{
	public static void makeOST(File file)
		{
		File rmdFile=new File(file,"rmd.xml");
		if(file.isDirectory() && !rmdFile.exists())
			{
			System.out.println("Converting imageset "+file.getPath());
			OstImageset ost=new OstImageset(file.getPath());
			ost.saveMeta();
			}
		}
	
	public static void main(String[] arg)
		{
		if(arg.length==0)
			arg=new String[]{"/Volumes/TBU_Maxtor/imageset/output_to_back_up/"};
		for(String s:arg)
			for(File file:(new File(s)).listFiles())
				if(file.isDirectory())
					makeOST(file);
		}

	}
