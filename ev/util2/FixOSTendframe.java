/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import endrov.core.EndrovCore;
import endrov.core.log.EvLog;
import endrov.core.log.EvLogStdout;
import endrov.data.EvData;
import endrov.data.EvPath;
import endrov.typeLineage.Lineage;
import endrov.util.math.EvDecimal;

/**
 * Get traveled distance & relative division time error for all nuclei
 * 
 * @author Johan Henriksson
 *
 */
public class FixOSTendframe
	{

	/*
	----------------- /Volumes3/TBU_main01/ost4dgood/FR317_070308.ost
	----------------- /Volumes3/TBU_main01/ost4dgood/FR783_C_060814.ost
	----------------- /Volumes3/TBU_main01/ost4dgood/N2_071010.ost
	----------------- /Volumes3/TBU_main01/ost4dgood/PS312_070902_b.ost
	----------------- /Volumes3/TBU_main02/ost4dgood/N2_071116.ost
	----------------- /Volumes3/TBU_main02/ost4dgood/N2greenLED_080206.ost
	----------------- /Volumes3/TBU_main02/ost4dgood/TB2142_071129.ost
	----------------- /Volumes3/TBU_main02/ost4dgood/TB2163_071123.ost
	----------------- /Volumes3/TBU_main02/ost4dgood/TB2164_071206_b.ost
	----------------- /Volumes3/TBU_main02/ost4dgood/TB2164_080118.ost
	----------------- /Volumes3/TBU_main02/ost4dgood/testgreenLED_080115_N2.ost
	----------------- /Volumes3/TBU_main03/ost4dgood/TB2167_080409_b.ost
	----------------- /Volumes3/TBU_main03/ost4dgood/TB2167_080414.ost
	----------------- /Volumes3/TBU_main03/ost4dgood/TB2167_080416.ost
	----------------- /Volumes3/TBU_main03/ost4dgood/TB2167_080418.ost
*/
	
	public static void one(File f)
		{
		EvData data=EvData.loadFile(f);
		boolean change=false;
		for(Map.Entry<EvPath,Lineage> e:data.getIdObjectsRecursive(Lineage.class).entrySet())
			{
			Lineage lin=e.getValue();
			if(!e.getKey().getLeafName().startsWith("AP"))
				for(String nucname:lin.particle.keySet())
					{
					Lineage.Particle nuc=lin.particle.get(nucname);
					
					if(nuc.overrideEnd!=null && nuc.overrideEnd.less(new EvDecimal("10000")))
						{
						change=true;
						nuc.overrideEnd=nuc.overrideEnd.multiply(new EvDecimal("10"));
						}
					}
			}
		if(change)
			{
			System.out.println(f);
			try
				{
				data.saveData();
				}
			catch (IOException e1)
				{
				e1.printStackTrace();
				}
			
			}
		}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		EvLog.addListener(new EvLogStdout());
		EndrovCore.loadPlugins();

		for(File f:new File("/Volumes3/TBU_main01/ost4dgood/").listFiles())
			if(f.getName().endsWith(".ost"))
				one(f);
		for(File f:new File("/Volumes3/TBU_main02/ost4dgood/").listFiles())
			if(f.getName().endsWith(".ost"))
				one(f);
		for(File f:new File("/Volumes3/TBU_main03/ost4dgood/").listFiles())
			if(f.getName().endsWith(".ost"))
				one(f);
		
		System.exit(0);
		
		
		
		
		
		
		}
	
	}
