package util2;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import evplugin.ev.*;
import evplugin.imagesetOST.*;
import evplugin.nuc.*;
import java.util.*;

/**
 * Go through all imagesets in a directory and run the MakeQT plugin
 * @author Johan Henriksson
 */
public class BatchRot2
	{
	public static void extractRot(File file)
		{
		String dataDir="/Users/tbudev3/newcode/rotanalys/data/";
		
		HashMap<String,Integer> id=new HashMap<String,Integer>();
		id.put("2ftail", 1000);
		id.put("venc",   1001);
		id.put("gast",   1002);
		id.put("Ep",     1003);
		id.put("EP",     1003);
		id.put("EMS",    1004);

		id.put("post", 2000);
		id.put("ant",  2001);

		id.put("shellup",   2010);
		id.put("shelldown", 2011);
		
		id.put("shellmid1", 2020);
		id.put("shellmid2", 2021);
		
		System.out.println("Imageset "+file.getPath());
		OstImageset ost=new OstImageset(file.getPath());

		for(NucLineage lin:ost.getObjects(NucLineage.class))
				{
				File outname=new File(dataDir+file.getName()+".txt");
				//if(!outname.exists())
					{
					boolean hasContent=false;
					try
						{
						BufferedWriter fp = new BufferedWriter(new FileWriter(outname));
						for(String nucName:lin.nuc.keySet())
							if(id.containsKey(nucName))
								{
								NucLineage.Nuc nuc=lin.nuc.get(nucName);
								int frame=nuc.pos.firstKey();
								NucLineage.NucPos pos=nuc.pos.get(frame);
								fp.write(""+id.get(nucName)+"\t"+frame+"\t"+pos.x+"\t"+pos.y+"\t"+pos.z+"\n");
								hasContent=true;
								}
							else
								System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!nucname "+nucName+" has no key");
						fp.close();
						if(!hasContent)
							outname.delete();
						}
					catch (Exception e)
						{
						e.printStackTrace();
						}
					}
				}
		}
	
	/**
	 * Entry point
	 * @param arg Command line arguments
	 */
	public static void main(String[] arg)
		{
		EV.loadPlugins();

		//int cnt=0;
		
		if(arg.length==0)
			arg=new String[]{"/Volumes/TBU_xeon01_500GB01/final_recordings/"};
		for(String s:arg)
			for(File file:(new File(s)).listFiles())
				if(file.isDirectory())
					{
					extractRot(file);

					//System.gc();
					/*
					cnt++;
					if(cnt==10)
						System.exit(0);
					*/
					}
		}
	}
