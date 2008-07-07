package util;
import java.io.*;

import endrov.imagesetOST.*;

/**
 * Go through all OST imagesets in a directory and resave them. This will cause them to be resaved in the
 * latest version.
 * @author Johan Henriksson
 */
public class BatchOST
	{
	public static void makeOST(File file)
		{
		File rmdFile=new File(file,"rmd.ostxml");
		if(file.isDirectory() && rmdFile.exists() && !file.getName().endsWith(".ost"))
			{
			System.out.println("Converting imageset "+file.getPath());
//			OstImageset ost=new OstImageset(file);
//			ost.saveMeta();
			
			File newFile=new File(file.getParentFile(),file.getName()+".ost");
			file.renameTo(newFile);
			
//			System.out.println(file+" to "+newFile);
			
			}
		else if(file.getName().endsWith(".imserv") && !file.getName().endsWith(".ost.imserv"))
			{
			File newFile=new File(file.getParentFile(),
					file.getName().substring(0,file.getName().length()-".imserv".length())+".ost.imserv");
			
			file.renameTo(newFile);
			
			System.out.println(file+" to "+newFile);
			}
		
		
		}
	
	public static void main(String[] arg)
		{
//		if(arg.length==0)
		arg=new String[]{
				"/Volumes/TBU_main01/ost3dfailed/","/Volumes/TBU_main01/ost4dfailed/",
				"/Volumes/TBU_main01/ost3dgood/","/Volumes/TBU_main01/ost4dgood",

				"/Volumes/TBU_main02/ost3dfailed/","/Volumes/TBU_main02/ost4dfailed/",
				"/Volumes/TBU_main02/ost3dgood/","/Volumes/TBU_main02/ost4dgood",

				
				"/Volumes/TBU_main03/ost3dfailed/","/Volumes/TBU_main03/ost4dfailed/",
				"/Volumes/TBU_main03/ost3dgood/","/Volumes/TBU_main03/ost4dgood"
				};
		for(String s:arg)
			for(File file:(new File(s)).listFiles())
//				if(file.isDirectory())
					makeOST(file);
		System.exit(0);
		}

	}
