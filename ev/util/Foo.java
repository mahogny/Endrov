package util;


import java.util.*;
import java.io.*;

public class Foo
	{

	
	static HashSet<String> exist=new HashSet<String>();

	
	public static void addDir(String filename)
		{
		File f=new File(filename);
		for(File a:f.listFiles())
			exist.add(a.getName());
		}
	
	public static void checkDir(String filename)
		{
		File f=new File(filename);
		for(File a:f.listFiles())
			if(exist.contains(a.getName()))
				System.out.println("Are in list "+a.getAbsolutePath());
//			else
	//			System.out.println("Not in list "+a.getAbsolutePath());
		}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		addDir("/Volumes/TBU_xeon01_500GB01/final_recordings/");
		addDir("/Volumes/TBU_xeon01_500GB01/daemonoutput_mirror");
		addDir("/Volumes/TBU_xeon01_500GB01/technical_failed_recordings");

		checkDir("/Volumes/TBU_Maxtor/imageset/toburn_mov_done/");
		
		}

	}
