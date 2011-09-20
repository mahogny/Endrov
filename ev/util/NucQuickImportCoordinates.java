/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import endrov.data.EvData;
import endrov.ev.EvLog;
import endrov.particle.Lineage;
import endrov.particle.Lineage.ParticlePos;
import endrov.util.EvDecimal;

/**
 * This is an example of how coordinates can be imported from simpler CSV files into Lineages.
 * 
 * It can be run from inside Endrov with the command
 * util.NucQuickImportCoordinates.forall();
 * and will run on each loaded OST-file. There should already exist a lineage object in the OST-file.
 * 
 * It can also be run from the command line. command line argument: <file to analyze>
 * 
 * @author Johan Henriksson
 *
 */
public class NucQuickImportCoordinates
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
    EvLog.printLog("Importing all lineages");
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
			
			BufferedReader fr=new BufferedReader(new FileReader(f));
			String line;
			while((line=fr.readLine())!=null)
				{
				StringTokenizer stok=new StringTokenizer(line,"\t");
				
				String nucName=stok.nextToken();
				EvDecimal time=new EvDecimal(stok.nextToken());
				ParticlePos pos=new ParticlePos();
				pos.x=Double.parseDouble(stok.nextToken());
				pos.y=Double.parseDouble(stok.nextToken());
				pos.z=Double.parseDouble(stok.nextToken());
				pos.r=Double.parseDouble(stok.nextToken());
				Lineage.Particle nuc=lin.getCreateParticle(nucName);
				nuc.pos.put(time, pos);
				}
			}
	
		
		}
	
	}
