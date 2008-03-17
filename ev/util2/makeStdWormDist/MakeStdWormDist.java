package util2.makeStdWormDist;

import java.util.*;

import javax.vecmath.Vector3d;
import util2.makeStdWorm.BestFitRotTransScale;
import util2.makeStdWormDist.NucStats.NucStatsOne;

import evplugin.data.*;
import evplugin.ev.*;
import evplugin.nuc.NucLineage;
import evplugin.nuc.NucPair;
import evplugin.nuc.NucLineage.NucInterp;


//Do not use rigid transforms, use point dist.

public class MakeStdWormDist
	{

	public static void main(String[] args)
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();

		
		EvDataXML output=new EvDataXML("/Volumes/TBU_xeon01_500GB02/ostxml/stdcelegans.xml");
		output.metaObject.clear();
		NucLineage refLin=new NucLineage();
		output.addMetaObject(refLin);
		
		
		//Load all worms to standardize from
		String[] wnlist={
				"/Volumes/TBU_xeon01_500GB02/ost4dgood/N2_071114/rmd.xml",
				"/Volumes/TBU_xeon01_500GB02/ost4dgood/N2_071115/rmd.xml",
				"/Volumes/TBU_xeon01_500GB02/ost4dgood/N2_071116/rmd.xml",
				"/Volumes/TBU_xeon01_500GB02/ost4dgood/N2_071117/rmd.xml",
				"/Volumes/TBU_xeon01_500GB02/ost4dgood/N2_071118/rmd.xml",
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
		
		//Remove all :-nucs from all lineages, as well as just crap
		for(NucLineage lin:lins.values())
			{
			TreeSet<String> nucstocopynot=new TreeSet<String>();
			for(String n:lin.nuc.keySet())
				if(n.startsWith(":") || n.startsWith("shell") || n.equals("ant") || n.equals("post") || n.equals("venc") || n.indexOf('?')>=0 || n.indexOf('_')>=0)
					nucstocopynot.add(n);
			for(String n:nucstocopynot)
				lin.removeNuc(n);
			}
		
		NucStats nucstats=new NucStats();
		
		//Collect tree
		for(NucLineage lin:lins.values())
			{
			for(String nucname:lin.nuc.keySet())
				{
				NucLineage.Nuc nuc=lin.nuc.get(nucname);
				int start=nuc.pos.firstKey();
				int end=nuc.pos.lastKey();
				
				if(start<1300) //For testing
					{
					NucStats.NucStatsOne one=nucstats.get(nucname);
					one.parent=nuc.parent;
					one.lifetime.add(end-start+1);
					}
				}
			}
		nucstats.deriveLifetime();

		//Collect distances and radii
		for(NucLineage lin:lins.values())
			{
			System.out.println("Adding lengths for "+lin);
			for(String thisnucname:lin.nuc.keySet())
				{
				NucStats.NucStatsOne one=nucstats.nuc.get(thisnucname);
				if(one!=null)
					{
					NucLineage.Nuc nuc=lin.nuc.get(thisnucname);
	
					int start=nuc.pos.firstKey();
					int end=nuc.pos.lastKey()+1; //trouble: what if children already exist?
	
					int modlifelen=one.lifeEnd-one.lifeStart;
					for(int frame=0;frame<modlifelen;frame++)
						{
						//Interpolate
						double iframe=one.interpolTime(start, end, frame);
						Map<NucPair, NucInterp> inter=lin.getInterpNuc(iframe);
						NucInterp thisi=inter.get(new NucPair(lin,thisnucname));
	
						if(thisi!=null)
							{
						
							one.addRadius(frame, thisi.pos.r);
							
							//Get distances
							for(NucPair otherpair:inter.keySet())
								{
								String othernucname=otherpair.getRight();
								NucInterp otheri=inter.get(otherpair);
		
								double dx=thisi.pos.x-otheri.pos.x;
								double dy=thisi.pos.y-otheri.pos.y;
								double dz=thisi.pos.z-otheri.pos.z;
								double dist=Math.sqrt(dx*dx+dy*dy+dz*dz);
		
								one.addDistance(frame, othernucname, dist);
								}
							}
						else
							System.out.println(" missing thisi "+thisnucname+ " "+frame);
						}
					}
				}
			}
		
		int maxframe=nucstats.maxFrame();
		for(int frame=0;frame<maxframe;frame++)
			{
			Map<String, NucStatsOne> curnuc=nucstats.getAtFrame(frame);
			
			
			
			for(String s:curnuc.keySet())
				{
				nucstats.get(s).calcAvDist(frame);
				
				
				
				}
			
			
			
			
			}
		
		
		
		//Save reference
//		output.saveMeta();
		}
	
	

	}
