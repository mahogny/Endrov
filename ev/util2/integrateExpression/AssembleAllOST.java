package util2.integrateExpression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.StringTokenizer;

import endrov.ev.EV;
import endrov.ev.Log;
import endrov.ev.StdoutLog;
import endrov.util.EvFileUtil;
import endrov.util.Tuple;

/**
 * Put all data onto model
 * @author Johan Henriksson
 *
 */
public class AssembleAllOST
	{
	
	
	
	
	public static void main(String[] args)
		{
		
		
		File htmlOutdir=new File("/Volumes2/TBU_main03/userdata/henriksson/geneProfilesAPT");
		
		
		htmlOutdir.mkdirs();
		
		
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();
		
	//	EvData data=EvData.loadFile(new File("/Volumes/TBU_main01/ost4dgood/TB2141_070621_b.ost/"));
		//doProfile(data);

		
			for(File f:new File("/Volumes2/TBU_main01/ost4dgood").listFiles())
				if(f.getName().endsWith(".ost")) 
					{
					File APfile=new File(new File(f,"data"),"AP20-GFPb");
//					File APfile=new File(new File(f,"data"),"AP20-GFP");
					Tuple<String, String> nameDate=AssembleAllHTML.nameDateFromOSTName(f.getName());
					
					////////////////////////// AP-profile //////////////////////////////
					if(APfile.exists())
						{
						System.out.println(APfile);
						System.out.println(nameDate);
						
						}
					
					
					//////////////////////// T-profile ////////////////////////////
					File Tfile=new File(new File(f,"data"),"AP1-GFPb");
					if(Tfile.exists())
						{
						
						}
					}
		
		System.out.println("done");
		System.exit(0);
		
		}
	}
