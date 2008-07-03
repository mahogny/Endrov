package util2.compareDist;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Vector3d;

import endrov.data.EvData;
import endrov.data.EvDataXML;
import endrov.data.EvObject;
import endrov.ev.EV;
import endrov.ev.Log;
import endrov.ev.StdoutLog;
import endrov.nuc.NucLineage;

/**
 * Get traveled distance for all nuclei
 * @author tbudev3
 *
 */
public class TravelDist
	{

	public static class Avg	
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
				nuc.overrideEnd=nuc.pos.lastKey();
		}

	
	/**
	 * Set end frame of all cells without children to last frame. This stops them from occuring in interpolations.
	 * Matlab edition
	 */
	public void endAllCellsMatlab(NucLineage lin)
		{
		//End all nuc without children for clarity
		for(NucLineage.Nuc nuc:lin.nuc.values())
			if(nuc.child.isEmpty() && !nuc.pos.isEmpty())
				nuc.overrideEnd=nuc.pos.lastKey();
		}


	
	public <B> B getStr(HashMap<String,B> map, int c)
		{
		return map.get(""+c);
		}
	public <B> B getStr(HashMap<String,B> map, String c)
		{
		return map.get(c);
		}
	
	
	
	
	public static void one(String linname)
		{
		System.out.println(linname);
		//Load lineage
		EvData ost=new EvDataXML(linname+"/rmd.ostxml");
		NucLineage lin=null;
		for(EvObject evob:ost.metaObject.values())
			if(evob instanceof NucLineage && ((NucLineage)evob).nuc.size()>10)
				lin=(NucLineage)evob;
		if(lin==null)
			System.out.println("WTF2");
		endAllCells(lin);
	
		//Load reference tree for naming
		/*
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
		 */
	
		//Save in data dir & average
		try
			{
			FileWriter outFile = new FileWriter(linname+"/data/traveldist.txt");
			PrintWriter out = new PrintWriter(outFile);
	
	
	
			//For all nuclei
			for(Map.Entry<String, NucLineage.Nuc> e:lin.nuc.entrySet())
				{
				String nucName=e.getKey();
				NucLineage.Nuc nuc=e.getValue();
				if(!nuc.pos.isEmpty())
					{
					int c=3;
					int start = nuc.pos.firstKey()+c;
					int end   = nuc.pos.lastKey()-c;
		
		
					if(start<end)
						{
						NucLineage.NucInterp interStart=nuc.interpolatePos(start);
						NucLineage.NucInterp interEnd=nuc.interpolatePos(end);
						if(interStart==null || interEnd==null)
							System.out.println(nucName+" "+interStart+ " "+interEnd+" "+start+" "+end+" "+nuc.overrideEnd);
						else
							{
							Avg r=new Avg();
							r.put(interStart.pos.r);
							r.put(interEnd.pos.r);
			
							Vector3d v=interEnd.pos.getPosCopy();
							v.sub(interStart.pos.getPosCopy());
							double straightDistance=v.length();
			
							Vector3d last=interStart.pos.getPosCopy();
			
							double fractalDist=0;
							for(Map.Entry<Integer, NucLineage.NucPos> ee:nuc.pos.entrySet())
								if(ee.getKey()>start && ee.getKey()<end)
									{
									last.sub(ee.getValue().getPosCopy());
									fractalDist+=last.length();
									last=ee.getValue().getPosCopy();
									r.put(ee.getValue().r);
									}
							last.sub(interEnd.pos.getPosCopy());
							fractalDist+=last.length();
			
							//Write out
							out.println(nucName+"\t"+start+"\t"+end+"\t"+straightDistance+"\t"+fractalDist+"\t"+r.getAv());
							}
						}
					}
				}
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
	//	one("/Volumes/TBU_main02/ostxml/mergedangler01_080522.xml");
		one("/Volumes/TBU_main03/ost4dgood/TB2167_0804016");
		one("/Volumes/TBU_main02/ost4dgood/stdcelegansNew");

		one("/Volumes/TBU_main03/ost4dgood/AnglerUnixCoords");

		one("/Volumes/TBU_main02/ost4dgood/N2_071114");

		one("/Volumes/TBU_main02/ost4dgood/N2greenLED080206");
		one("/Volumes/TBU_main02/ost4dgood/TB2142_071129");
		one("/Volumes/TBU_main02/ost4dgood/TB2164_080118");
		
		System.exit(0);
		
		
		
		
		
		
		}
	
	}
