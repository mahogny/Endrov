package util2.converter;

import java.io.*;
import java.util.*;


import endrov.data.*;
import endrov.ev.*;
import endrov.nuc.NucLineage;


//Do not use rigid transforms, use point dist.

//in fitting all to one: possible to store individual rots, average, invert on assembly and hope it cancels

/**
 * Assemble c.e model
 * @author Johan Henriksson
 */
public class FixSchnabel
	{


	
	
	public static SortedMap<Double,Double> timeMap=new TreeMap<Double, Double>();
	

	public static double interpol(double frame)
		{
		double keyBefore=timeMap.headMap(frame).lastKey();
		double keyAfter=timeMap.tailMap(frame).firstKey();
		double toBefore=timeMap.get(keyBefore);
		double toAfter=timeMap.get(keyAfter);
		
		
		
		double x=(frame-keyBefore)/(keyAfter-keyBefore);
		double newframe=x*toAfter+(1-x)*toBefore;
		System.out.println(""+frame+" "+newframe);
		return newframe;
		}
	
	
	/**
	 * Entry point
	 */
	public static void main(String[] args)
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();

		try
			{
			BufferedReader tfile=new BufferedReader(new FileReader("/Volumes/TBU_main03/userdata/jurgen/timepoints_angler_std/timepoints_angler_std.txt"));
			String line;
			while((line=tfile.readLine())!=null)
				{
				StringTokenizer st=new StringTokenizer(line);
				double from=Double.parseDouble(st.nextToken());
				double to=Double.parseDouble(st.nextToken());
				timeMap.put(from,to);
				}
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		
		EvData ref=new EvDataXML("/Volumes/TBU_main02/ostxml/model/stdcelegansNew.ostxml");
		EvData ost=new EvDataXML("/Volumes/TBU_main03/ost4dgood/AnglerUnixCoords.ost/rmd.ostxml");

		
		NucLineage reflin=ref.getObjects(NucLineage.class).iterator().next();
		NucLineage lin=ost.getObjects(NucLineage.class).iterator().next();

		for(Map.Entry<String, NucLineage.Nuc> entry:lin.nuc.entrySet())
			{
			NucLineage.Nuc refnuc=reflin.nuc.get(entry.getKey());
			if(refnuc!=null)
				{
				double avr=0;
				for(NucLineage.NucPos pos:refnuc.pos.values())
					avr+=pos.r;
/*				if(refnuc.pos.size()==0)
					avr=1;
				else*/
					avr/=refnuc.pos.size();
					avr*=7.46;//25;
					
				Map<Integer, NucLineage.NucPos> newpos=new HashMap<Integer, NucLineage.NucPos>(entry.getValue().pos);
				entry.getValue().pos.clear();	
				
				for(Map.Entry<Integer, NucLineage.NucPos> ne:newpos.entrySet())
					{
					ne.getValue().r=avr;
					entry.getValue().pos.put((int)interpol(ne.getKey()),ne.getValue());
					}
				}
			else
				System.out.println("missing "+entry.getKey());
			}
		
		//Save reference
		EvDataXML output=new EvDataXML("/Volumes/TBU_main03/ost4dgood/AnglerUnixCoords_no_AP_radius.ost/rmd.ostxml");
		output.metaObject.clear();
		output.addMetaObject(lin);
		output.saveMeta();
		
		}

	}
	