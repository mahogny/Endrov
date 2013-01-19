/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.paperStdCelegans.makeCellContactMap2;

import java.io.*;
import java.util.*;

import endrov.core.*;
import endrov.core.log.EvLog;
import endrov.core.log.EvLogStdout;
import endrov.data.EvData;
import endrov.typeParticleContactMap.neighmap.NeighMap;
import endrov.typeParticleContactMap.neighmap.RenderNeighmap;


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
			EvLog.addListener(new EvLogStdout());
			EndrovCore.loadPlugins();

			EvData dneighmaps=EvData.loadFile(new File("/Volumes/TBU_main03/userdata/newcellcontactsmap.ost"));

			TreeMap<String, NeighMap> nmaps=new TreeMap<String, NeighMap>();
			nmaps.putAll(dneighmaps.getIdObjects(NeighMap.class));
			
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

