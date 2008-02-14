package util2.makeStdWorm;

import java.util.*;
import evplugin.data.*;
import evplugin.ev.*;
import evplugin.nuc.NucLineage;


public class MakeStdWorm
	{

	public static void main(String[] args)
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();
		
		//Load all worms to standardize from
		String[] wnlist={
				"/Volumes/TBU_xeon01_500GB02/ost4dgood/N2_071114/rmd.xml",
				//"/Volumes/TBU_xeon01_500GB02/ost4dgood/N2_071115/",
				"/Volumes/TBU_xeon01_500GB02/ost4dgood/N2_071116/rmd.xml",
				//"/Volumes/TBU_xeon01_500GB02/ost4dgood/N2_071117/",
				//"/Volumes/TBU_xeon01_500GB02/ost4dgood/N2_071118/",
				}; 
		Vector<EvData> worms=new Vector<EvData>();
		TreeMap<String, NucLineage> lins=new TreeMap<String, NucLineage>();
		for(String s:wnlist)
			{
			EvData ost=new EvDataXML(s);
			worms.add(ost);
			for(EvObject evob:ost.metaObject.values())
				{
				if(evob instanceof NucLineage)
					{
					lins.put(s, (NucLineage)evob);
					System.out.println("ok");
					}
				}
			}

		//Get names of nuclei
		TreeSet<String> nucNames=new TreeSet<String>();
		for(NucLineage lin:lins.values())
			nucNames.addAll(lin.nuc.keySet());

		//How much to shift each recording?
		NucLineage shiftRefLin=lins.get(lins.firstKey());
		HashMap<NucLineage, Integer> linShift=new HashMap<NucLineage, Integer>();
		for(NucLineage lin:lins.values())
			if(lin==shiftRefLin)
				linShift.put(lin, 0);
			else
				{
				int shifttot=0;
				int numshift=0;
				
				for(String n:lin.nuc.keySet())
					{
					if(shiftRefLin.nuc.containsKey(n))
						{
						int s=lin.nuc.get(n).pos.firstKey()-shiftRefLin.nuc.get(n).pos.firstKey();
						shifttot+=s;
						numshift++;
						}
					}
				linShift.put(lin, shifttot/numshift);
				}
		
		//When do nuc start and stop existing?
		TreeMap<String,Vector<Integer>> nucStart=new TreeMap<String, Vector<Integer>>();
		TreeMap<String,Vector<Integer>> nucEnd=new TreeMap<String, Vector<Integer>>();
		for(String n:nucNames)
			{
			nucStart.put(n, new Vector<Integer>());
			nucEnd.put(n, new Vector<Integer>());
			}
		for(NucLineage lin:lins.values())
			for(String n:lin.nuc.keySet())
				{
				NucLineage.Nuc nuc=lin.nuc.get(n);
				nucStart.get(n).add(nuc.pos.firstKey()-linShift.get(lin));
				nucEnd.get(n).add(nuc.pos.lastKey()-linShift.get(lin));
				}

		//
		
		
		/*
		System.out.println("starts----------");
		for(String n:nucStart.keySet())
			{
			System.out.print(n+" ");
			for(int i:nucEnd.get(n))
				System.out.print(""+i+" ");
			System.out.println();
			}
		*/
		
		//Test average
		/*
		double avg=0;
		int avgn=0;
		for(String n:nucStart.keySet())
			if(nucStart.get(n).size()>1 && n.startsWith("M"))
				{
				avg+=nucStart.get(n).get(1)-nucStart.get(n).get(0);
				avgn++;
				}
		avg/=avgn;
		System.out.println("avg "+avg);
		*/
		
		
		}
	
	
	

	}
