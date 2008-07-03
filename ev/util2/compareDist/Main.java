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


import endrov.data.EvData;
import endrov.data.EvDataXML;
import endrov.data.EvObject;
import endrov.ev.EV;
import endrov.ev.EvParallel;
import endrov.ev.Log;
import endrov.ev.StdoutLog;
import endrov.nuc.NucLineage;
import endrov.nuc.NucPair;
import endrov.nuc.NucLineage.NucInterp;

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
	
	
	public static class Foo
		{
		int c;
		String lin;
		public Foo(String lin,int c)
			{this.c=c;this.lin=lin;}
		}
	
	
	/**
	 * Set end frame of all cells without children to last frame. This stops them from occuring in interpolations.
	 */
	public static void endAllCells(NucLineage lin)
		{
		//End all nuc without children for clarity
		for(NucLineage.Nuc nuc:lin.nuc.values())
			if(nuc.child.isEmpty() && !nuc.pos.isEmpty())
				nuc.overrideEnd=nuc.pos.lastKey();
		}
	
	
/*	
	public static void shortenLineage(NucLineage lin)
		{
		int c=3;
		for(Map.Entry<String, NucLineage.Nuc> e:lin.nuc.entrySet())
			{
//			String nucName=e.getKey();
			NucLineage.Nuc nuc=e.getValue();
			if(!nuc.pos.isEmpty())
				{
				int start = nuc.pos.firstKey()+c;
				int end   = nuc.pos.lastKey()-c;
	
				
	
				if(start<end)
					{
					NucLineage.NucInterp interStart=nuc.interpolatePos(start);
					NucLineage.NucInterp interEnd=nuc.interpolatePos(end);
//					if(interStart==null || interEnd==null)
//						System.out.println(nucName+" "+interStart+ " "+interEnd+" "+start+" "+end+" "+nuc.end);
	
					nuc.pos.put(start, new NucLineage.NucPos(interStart.pos));
					nuc.pos.put(end, new NucLineage.NucPos(interEnd.pos));
					
					HashSet<Integer> ints=new HashSet<Integer>();
					ints.addAll(nuc.pos.keySet());
					
					
					}
				}
			}
		
		}
*/
	
	public static void one(String linname, int c)
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
	//	shortenLineage(lin);
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

		//For all nuc
		for(Map.Entry<String, NucLineage.Nuc> e:lin.nuc.entrySet())
			{
			String nuca=e.getKey();
			NucLineage.Nuc nuc=e.getValue();
			if(!nuc.pos.isEmpty() && nameList.contains(nuca))
				{
				//In middle of life
				int start = nuc.pos.firstKey();
				int end   = nuc.pos.lastKey();
				int curframe=(int)((start+end)/2);
				NucLineage.NucInterp ai=nuc.interpolatePos(curframe);

				//Look at neigh
				Map<NucPair, NucInterp> inter=lin.getInterpNuc(curframe);
				for(Map.Entry<NucPair, NucInterp> ie:inter.entrySet())
					{
					String nucb=ie.getKey().snd();
					NucInterp bi=ie.getValue();
					
					//The > is for a strict selection between parent and child on the intersection
					//Is this a bug in the interpol framework?
					if(!nucb.equals(nuca) && nameList.contains(nucb) && lin.nuc.get(nucb).pos.lastKey()>curframe)
						{
//						System.out.println("neigh "+nuca+" "+nucb);
						Vector3d v=ai.pos.getPosCopy();
						v.sub(bi.pos.getPosCopy());

						double len=v.length();
						if(c==1)
							{
							len-=ai.pos.r;
							len-=bi.pos.r;
							}
						distList.get(nuca).get(nucb).put(len);
//						distList.get(nucb).get(nuca).put(len); //makes relation symmetric
						}
					
					}
				
				}
			}
		
		//For all frames interpolate
		/*
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
			*/

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
					double dist=dlist.getAv();
//					if(dlist.count<c)
//						dist=0;
					out.print(""+dist+" ");
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
		}

	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();

		Foo[] files=new Foo[]{
				new Foo("/Volumes/TBU_main02/ost4dgood/N2_071116",1),
				new Foo("/Volumes/TBU_main03/ost4dgood/TB2167_0804016",1),
				new Foo("/Volumes/TBU_main03/ost4dgood/AnglerUnixCoords",0),
				new Foo("/Volumes/TBU_main02/ost4dgood/stdcelegansNew",1)
				};
		List<Foo> l=new LinkedList<Foo>();
		for(Foo s:files)
			l.add(s);
//		for(String s:files)
//			one(s);

		EvParallel.map(l,new EvParallel.FuncAB<Foo,Object>(){
			public Object func(Foo in)
				{
				one(in.lin, in.c);
				return null;
				}
		});
		System.out.println("done");
		}

	}
