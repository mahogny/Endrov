package util2.paperStdCelegans.makeCellContactMap2;

/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */

import java.io.*;
import java.text.NumberFormat;
import java.util.*;

import endrov.core.*;
import endrov.core.log.EvLog;
import endrov.core.log.EvLogStdout;
import endrov.data.EvData;
import endrov.typeLineage.Lineage;
import endrov.typeLineage.util.MakeParticleContactMap;
import endrov.typeParticleContactMap.neighmap.NeighMap;
import endrov.util.*;
import endrov.util.collection.EvParallel;
import endrov.util.collection.Tuple;
import endrov.util.math.EvDecimal;

//stdcelegans vs celegans2008.2?




/**
 * Calculate cell contact map.
 * Output number of contacts for each frame.
 * 
 * about 40min on xeon
 * 
 * @author Johan Henriksson, Jurgen Hench
 *
 */
public class UtilMakeCellContactMap
	{
	public static void main(String[] args)
		{
		try
			{
			EvLog.addListener(new EvLogStdout());
			EndrovCore.loadPlugins();

			NumberFormat percentFormat=NumberFormat.getInstance();
			percentFormat.setMinimumFractionDigits(1);
			percentFormat.setMaximumFractionDigits(1);

		
			

			//bottle neck: building imageset when not needed. fix in Endrov3/OST4
			
			///////////
			//System.out.println("Connecting");
			//String url=ConnectImserv.url;
			//String query="not trash and CCM";
			//TODO make a getDataKeysWithTrash, exclude by default?
			System.out.println("Loading imsets");
			
			TreeMap<String, Lineage> lins=new TreeMap<String, Lineage>();
			
			for(String s:new String[]{"1.ost","2.ost"})
				{
				System.out.println("loading "+s);
				EvData data=EvData.loadFile(new File(s));
	//			Imageset im=data.getObjects(Imageset.class).iterator().next();
				
				lins.put(data.getMetadataName(), data.getIdObjectsRecursive(Lineage.class).values().iterator().next());
				}
			Lineage reflin=EvData.loadFile(new File("celegans2008.2.ost")).getObjects(Lineage.class).iterator().next();
			//////////

			final TreeSet<String> nucNames=new TreeSet<String>(reflin.particle.keySet());

			//Calc neigh
			Map<Lineage,NeighMap> nmaps=EvParallel.map(lins, new FuncAB<Tuple<String,Lineage>, Tuple<Lineage,NeighMap>>(){
			public Tuple<Lineage,NeighMap> func(Tuple<String,Lineage> in)
				{
				NeighMap nm=MakeParticleContactMap.calculateCellMap(in.snd(), nucNames, null, null, new EvDecimal(60));
				return new Tuple<Lineage, NeighMap>(in.snd(), nm);
				}
			});

			//Save neighmaps
			EvData dataOut=new EvData();
			for(Map.Entry<String, Lineage> e:lins.entrySet())
				dataOut.metaObject.put(e.getKey(), nmaps.get(e.getValue()));
			dataOut.saveDataAs(new File("/Volumes/TBU_main03/userdata/newcellcontactsmap.ost"));
			
			

			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		System.out.println("done");
		System.exit(0);
		}
	

	}

