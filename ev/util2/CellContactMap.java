package util2;

import java.util.*;

import endrov.data.EvData;
import endrov.data.EvDataXML;
import endrov.ev.*;
import endrov.nuc.NucLineage;
import endrov.nuc.NucPair;
import endrov.nuc.NucVoronoi;

public class CellContactMap
	{

	public static class OneLineage
		{
		Map<Integer,NucVoronoi> contacts=new HashMap<Integer, NucVoronoi>();
		NucLineage lin;
		
		public OneLineage(String file)
			{
			//Load lineage
			EvData ost=new EvDataXML(file);
			lin=ost.getObjects(NucLineage.class).iterator().next();
			}
		
		
		public void calcneigh()
			{
			//Go through all frames
			for(int curframe=lin.firstFrameOfLineage();curframe<lin.lastFrameOfLineage();curframe++)
				{
				//interpolate
				Map<NucPair, NucLineage.NucInterp> inter=lin.getInterpNuc(curframe);
				if(curframe%100==0)
					System.out.println(curframe);
				try
					{
					//Get neighbours
					NucVoronoi nvor=new NucVoronoi(inter);
					contacts.put(curframe, nvor);
					}
				catch (Exception e)
					{
//					e.printStackTrace();
					}
				}
			}
		}
	
	
	
	
	
	public static void main(String[] args)
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();

		String[] files=new String[]{"/Volumes/TBU_main02/ost4dgood/stdcelegansNew.ost/rmd.ostxml"};
		
		List<OneLineage> lins=new LinkedList<OneLineage>();
		
		//Load all
		for(String s:files)
			lins.add(new OneLineage(s));
		
		//Calc neigh
		lins=EvParallel.map(lins, new EvParallel.FuncAB<OneLineage, OneLineage>(){
			public OneLineage func(OneLineage in)
				{
				in.calcneigh();
				return in;
				}
		});
		
		//Write out HTML, cell by cell. Reference lineage is the first one in the list
		List<String> nucNames=new LinkedList<String>(lins.get(0).lin.nuc.keySet());
		for(String nucName:nucNames)
			{
			//fill in
			
			
			
			
			}
		
		
		
	
		System.out.println("done");
		System.exit(0);

		}
	}
