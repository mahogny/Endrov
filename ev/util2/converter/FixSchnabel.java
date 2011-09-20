/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.converter;

import java.io.*;
import java.util.*;


import endrov.data.*;
import endrov.ev.*;
import endrov.particle.Lineage;
import endrov.util.EvDecimal;


//Do not use rigid transforms, use point dist.

//in fitting all to one: possible to store individual rots, average, invert on assembly and hope it cancels

/**
 * Assemble c.e model
 * @author Johan Henriksson
 */
public class FixSchnabel
	{


	
	
	public static SortedMap<EvDecimal,EvDecimal> timeMap=new TreeMap<EvDecimal, EvDecimal>();
	

	public static EvDecimal interpol(EvDecimal frame)
		{
		double keyBefore=timeMap.headMap(frame).lastKey().doubleValue();
		double keyAfter=timeMap.tailMap(frame).firstKey().doubleValue();
		double toBefore=timeMap.get(keyBefore).doubleValue();
		double toAfter=timeMap.get(keyAfter).doubleValue();
		
		
		
		double x=(frame.doubleValue()-keyBefore)/(keyAfter-keyBefore);
		double newframe=x*toAfter+(1-x)*toBefore;
		System.out.println(""+frame+" "+newframe);
		return new EvDecimal(newframe);
		}
	
	
	/**
	 * Entry point
	 */
	public static void main(String[] args)
		{
		EvLog.listeners.add(new EvLogStdout());
		EV.loadPlugins();

		try
			{
			BufferedReader tfile=new BufferedReader(new FileReader("/Volumes/TBU_main03/userdata/jurgen/timepoints_angler_std/timepoints_angler_std.txt"));
			String line;
			while((line=tfile.readLine())!=null)
				{
				StringTokenizer st=new StringTokenizer(line);
				EvDecimal from=new EvDecimal(st.nextToken());
				EvDecimal to=new EvDecimal(st.nextToken());
				timeMap.put(from,to);
				}
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		
		EvData ref=EvData.loadFile("/Volumes/TBU_main02/ostxml/model/stdcelegansNew.ostxml");
		//new EvIODataXML("/Volumes/TBU_main02/ostxml/model/stdcelegansNew.ostxml");
		EvData ost=EvData.loadFile("/Volumes/TBU_main03/ost4dgood/AnglerUnixCoords.ost/rmd.ostxml");
		//new EvIODataXML("/Volumes/TBU_main03/ost4dgood/AnglerUnixCoords.ost/rmd.ostxml");

		
		Lineage reflin=ref.getObjects(Lineage.class).iterator().next();
		Lineage lin=ost.getObjects(Lineage.class).iterator().next();

		for(Map.Entry<String, Lineage.Particle> entry:lin.particle.entrySet())
			{
			Lineage.Particle refnuc=reflin.particle.get(entry.getKey());
			if(refnuc!=null)
				{
				double avr=0;
				for(Lineage.ParticlePos pos:refnuc.pos.values())
					avr+=pos.r;
/*				if(refnuc.pos.size()==0)
					avr=1;
				else*/
					avr/=refnuc.pos.size();
					avr*=7.46;//25;
					
				Map<EvDecimal, Lineage.ParticlePos> newpos=new HashMap<EvDecimal, Lineage.ParticlePos>(entry.getValue().pos);
				entry.getValue().pos.clear();	
				
				for(Map.Entry<EvDecimal, Lineage.ParticlePos> ne:newpos.entrySet())
					{
					ne.getValue().r=avr;
					entry.getValue().pos.put(interpol(ne.getKey()),ne.getValue());
					}
				}
			else
				System.out.println("missing "+entry.getKey());
			}
		
		//Save reference
		/*
		EvIODataXML output=new EvIODataXML("/Volumes/TBU_main03/ost4dgood/AnglerUnixCoords_no_AP_radius.ost/rmd.ostxml");
		output.metaObject.clear();
		output.addMetaObject(lin);
		output.saveMeta();
*/
		try
			{
			EvData output=new EvData();
			output.metaObject.clear();
			output.addMetaObject(lin);
			output.saveDataAs("/Volumes/TBU_main03/ost4dgood/AnglerUnixCoords_no_AP_radius.ost/rmd.ostxml");
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}

		}

	}
	
