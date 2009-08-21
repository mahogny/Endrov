package util2.integrateExpression;

import java.io.File;

import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.util.*;

/**
 * Put all data onto model
 * @author Johan Henriksson
 *
 */
public class ProfileToOST
	{
	
	/**
	 * 
	 * WHAT IS THIS USED FOR??????
	 * 
	 * appears not used
	 * 
	 * I believe it is for putting all data into one OST
	 * 
	 * 
	 */
	
	
	public static void main(String[] args)
		{
		
		
		File htmlOutdir=new File("/Volumes/TBU_main03/userdata/henriksson/geneProfilesAPT");
		
		
		htmlOutdir.mkdirs();
		
		
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();
		
	//	EvData data=EvData.loadFile(new File("/Volumes/TBU_main01/ost4dgood/TB2141_070621_b.ost/"));
		//doProfile(data);

		
		for(File f:new File("/Volumes/TBU_main01/ost4dgood").listFiles())
//			for(File f:new File("/Volumes2/TBU_main01/ost4dgood").listFiles())
				if(f.getName().endsWith(".ost")) 
					{
					File APfile=new File(new File(f,"data"),"AP20-GFPb");
//					File APfile=new File(new File(f,"data"),"AP20-GFP");
					Tuple<String, String> nameDate=ProfileToHTML.nameDateFromOSTName(f.getName());
					
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
