/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.paperStdCelegans.makeCellContactMap2;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;

//import endrov.data.*;
import endrov.data.EvData;
import endrov.ev.*;
import endrov.neighmap.NeighMap;
import endrov.neighmap.RenderNeighmap;

//stdcelegans vs celegans2008.2?



/**
 * 
 * @author Johan Henriksson, Jurgen Hench
 *
 */
public class UtilRenderCellContactMap
	{
	public static void main(String[] args)
		{
		try
			{
			EvLog.listeners.add(new EvLogStdout());
			EV.loadPlugins();

			NumberFormat percentFormat=NumberFormat.getInstance();
			percentFormat.setMinimumFractionDigits(1);
			percentFormat.setMaximumFractionDigits(1);

		
			EvData dneighmaps=EvData.loadFile(new File("/Volumes/TBU_main03/userdata/newcellcontactsmap.ost"));

			TreeMap<String, NeighMap> nmaps=new TreeMap<String, NeighMap>();
			nmaps.putAll(dneighmaps.getIdObjects(NeighMap.class));
			
			/*
			///////////
			System.out.println("Connecting");
			String url=ConnectImserv.url;
			EvImserv.EvImservSession session=EvImserv.getSession(new EvImserv.ImservURL(url));
			//TODO make a getDataKeysWithTrash, exclude by default?
			System.out.println("Loading imsets");
			*/
			//TreeMap<String, NucLineage> lins=new TreeMap<String, NucLineage>();
			
			//NucLineage reflin=EvImserv.getImageset(url+"celegans2008.2").getObjects(NucLineage.class).iterator().next();
			//////////

			//final TreeSet<String> nucNames=new TreeSet<String>(reflin.nuc.keySet());

			File targetDir=new File("/Volumes/TBU_main03/userdata/cellcontactmapNEW/");
			
			RenderNeighmap.render(nmaps, targetDir);
			

			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		System.out.println("done");
		System.exit(0);
		}
	

	}

