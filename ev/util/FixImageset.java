package util;

import java.io.File;

import evplugin.ev.EV;

public class FixImageset
	{
	public static void makeGraph(File dir)
		{
		for(File f:dir.listFiles())
			if(f.isDirectory())
				{
				
				String newname=f.getName().substring(4);
				System.out.println(""+newname);
				
				
				makeGraph2(f);
				}
		
		
		}
	
	
	public static void makeGraph2(File dir)
		{
		for(File f:dir.listFiles())
			if(f.isFile() && !f.getName().startsWith("."))
				{
				
				String newname=f.getName().substring(4+8+1);
				System.out.println(""+newname);
				
				
				
				}
		
		
		}
	
	
	
	/**
	 * Entry point
	 * @param arg Command line arguments
	 */
	public static void main(String[] arg)
		{
		EV.loadPlugins();

		if(arg.length==0)
			arg=new String[]{"/Volumes/TBU_G5_500GB01/TYG/OTC_070209-convertedlowcompression/imageset-DIC/DIC/",
					"/Volumes/TBU_G5_500GB01/TYG/OTC_070209-convertedlowcompression/imageset-GFP/GFP"};
		for(String s:arg)
			makeGraph(new File(s));
		}
	}
