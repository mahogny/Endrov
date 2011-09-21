/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import endrov.data.EvData;
import endrov.ev.EvLog;
import endrov.lineage.Lineage;
import endrov.util.EvDecimal;

/**
 * This is an example of how coordinates can be extracted from Lineages and put into a simpler CSV file.
 * 
 * It can be run from inside Endrov with the command
 * util.NucQuickExtractCoordinates.forall();
 * and will run on each loaded OST-file.
 * 
 * It can also be run from the command line. command line argument: <file to analyze>
 * 
 * @author Johan Henriksson
 *
 */
public class NucQuickExtractCoordinates
	{
	public static void main(String[] args)
		{
		try
			{
			EvData data=EvData.loadFile(new File(args[0]));
			run(data);
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		
		}
	

	/**
	 * Run over all loaded data files
	 */
	public static void forall()
		{
    EvLog.printLog("Exporting for all lineages");
		for(EvData data:EvData.openedData)
			{
			try
				{
				run(data);
				}
			catch (IOException e)
				{
				EvLog.printError(e);
				}
			}
		}
	
	/**
	 * Run on a given data file
	 */
	public static void run(EvData data) throws IOException
		{
		File ddir=data.io.datadir();
		if(ddir==null)
			throw new RuntimeException("Not saved as OST");

		//For all lineages (assuming there is only one)
		for(Lineage lin:data.getIdObjectsRecursive(Lineage.class).values())
			{
			File f=new File(ddir,"quickLin.txt");
			EvLog.printLog(f.toString());
			PrintWriter pw=new PrintWriter(new FileWriter(f));
			
			//For every cell
			for(String nucName:lin.particle.keySet())
				{
				//For every time point
				for(Map.Entry<EvDecimal, Lineage.ParticlePos> e:lin.particle.get(nucName).pos.entrySet())
					{
					//Print coordinate
					Lineage.ParticlePos pos=e.getValue();
					pw.println(nucName+"\t"+e.getKey()+"\t"+pos.x+"\t"+pos.y+"\t"+pos.z+"\t"+pos.r);
					}
				}
			pw.flush();
			pw.close();
			}
	
		
		}
	
	}
