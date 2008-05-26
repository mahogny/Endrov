package util2.compareDist;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.vecmath.Vector3d;

import evplugin.data.EvData;
import evplugin.data.EvDataXML;
import evplugin.data.EvObject;
import evplugin.ev.EV;
import evplugin.ev.Log;
import evplugin.ev.StdoutLog;
import evplugin.nuc.NucLineage;
import evplugin.nuc.NucPair;
import evplugin.nuc.NucLineage.NucInterp;

public class Main
	{

	
	public static class AvLen	
		{
		double x=0;
		int count=0;
		public void put(double y)
			{
			x+=y;
			count++;
			}
		public double getAv()
			{
			if(count==0)
				return 0;
			else
				return x/count;
			}
		}
	
	
	/**
	 * Set end frame of all cells without children to last frame. This stops them from occuring in interpolations.
	 */
	public static void endAllCells(NucLineage lin)
		{
		//End all nuc without children for clarity
		for(NucLineage.Nuc nuc:lin.nuc.values())
			if(nuc.child.isEmpty() && !nuc.pos.isEmpty())
				nuc.end=nuc.pos.lastKey();
		}
	
	
	public static void one(String linname)
		{
		//Load lineage
		EvData ost=new EvDataXML(linname+"/rmd.ostxml");
//		EvData ost=new EvDataXML(linname);
		NucLineage lin=null;
		for(EvObject evob:ost.metaObject.values())
			if(evob instanceof NucLineage && ((NucLineage)evob).nuc.size()>10)
				lin=(NucLineage)evob;
		if(lin==null)
			System.out.println("WTF2");
		endAllCells(lin);

		//Load reference tree for naming
		EvData refost=new EvDataXML("/Volumes/TBU_main02/ostxml/model/stdcelegansNew.ostxml");
		NucLineage reflin=null;
		for(EvObject evob:refost.metaObject.values())
			{
			System.out.println(evob.getMetaTypeDesc());
			if(evob instanceof NucLineage)
				reflin=(NucLineage)evob;
			}
		if(reflin==null)
			System.out.println("WTF");

		//Get all names
		Map<String, Map<String, AvLen>> distList=new TreeMap<String, Map<String, AvLen>>();
		List<String> nameList=new LinkedList<String>();
		for(String s:reflin.nuc.keySet())
			{
			//if(s.length()<=5)
				nameList.add(s);
			}
		Collections.sort(nameList);
		for(String s:nameList)
			{
			TreeMap<String, AvLen> a=new TreeMap<String, AvLen>();
			distList.put(s, a);
			for(String t:nameList)
				{
				a.put(t, new AvLen());
				}
			}

		//For all frames interpolate
		int oknuc=0;
		for(int curframe=0;curframe<10000;curframe++)
			{
			if(curframe%50==0)
				System.out.println("frame: "+curframe+" oknuc: "+oknuc);
			Map<NucPair, NucInterp> inter=lin.getInterpNuc(curframe);
			for(Map.Entry<NucPair, NucInterp> ie:inter.entrySet())
				{
				String nuca=ie.getKey().snd();
				NucInterp ai=ie.getValue();
				for(Map.Entry<NucPair, NucInterp> ieb:inter.entrySet())
					{
					String nucb=ieb.getKey().snd();
					NucInterp bi=ieb.getValue();

					//get first and last frame for each nucleus
					NucLineage.Nuc nucac = lin.nuc.get(nuca);
					NucLineage.Nuc nucbc = lin.nuc.get(nucb);
					
					if(nameList.contains(nuca) && nameList.contains(nucb))
						{
						int starta = nucac.pos.firstKey();
						int enda   = nucac.pos.lastKey();
						int startb = nucbc.pos.firstKey();
						int endb   = nucbc.pos.lastKey();
						if(starta<=curframe && enda>=curframe &&
								startb<=curframe && endb>=curframe)
							{
							//System.out.println(""+nuca+" => "+nucb);
							Vector3d v=ai.pos.getPosCopy();
							v.sub(bi.pos.getPosCopy());
	
							double len=v.length();
							distList.get(nuca).get(nucb).put(len);
							distList.get(nucb).get(nuca).put(len);
							oknuc++;
							}
						}
//					else
//					System.out.println("Not in namelist: "+nuca+" "+nucb);
					}
				}
			}

		if(true)
			return;

		//Save in data dir & average
		try
			{
			FileWriter outFile = new FileWriter(linname+"/data/neighdist.txt");
			PrintWriter out = new PrintWriter(outFile);

			for(String s:nameList)
				{
				for(String t:nameList)
					{
					AvLen dlist=distList.get(s).get(t);
					out.print(""+dlist.getAv()+" ");
					}
				out.println();
				}
			out.close();
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}

		//Save name list
		try
			{
			FileWriter outFile = new FileWriter(linname+"/data/neighname.txt");
			PrintWriter out = new PrintWriter(outFile);
			for(String s:nameList)
				out.println(s);
			out.close();
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		System.out.println("done");

		}

	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();

		one("/Volumes/TBU_main02/ost4dgood/N2_071116");
//		one("/Volumes/TBU_main02/ostxml/mergedangler01_080522.xml");
		one("/Volumes/TBU_main03/ost4dgood/TB2167_0804016");
	

		one("/Volumes/TBU_main03/ost4dgood/AnglerUnixCoords");
	
		}

	}
