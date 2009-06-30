package util2.plots.travelDist;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Vector3d;

import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.StdoutLog;
import endrov.nuc.NucExp;
import endrov.nuc.NucLineage;
import endrov.util.EvDecimal;

/**
 * Get traveled distance & relative division time error for all nuclei
 * 
 * @author Johan Henriksson
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
		EvLog.listeners.add(new StdoutLog());
		EV.loadPlugins();

		
		System.out.println(linname);
		//Load lineage
		//EvData ost=new EvIODataXML(linname+"/rmd.ostxml");
		EvData ost=EvData.loadFile(new File(linname));
		NucLineage lin=null;
		for(NucLineage evob:ost.getIdObjectsRecursive(NucLineage.class).values())
			if(evob.nuc.size()>10)
				lin=evob;
			else
				System.out.println("hmmm");
		if(lin==null)
			{
			System.out.println("WTF2");
			System.exit(0);
			}
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
				if(!nuc.pos.isEmpty() && nuc.child.size()==2) //Only consider cells with children
					{
					int c=3;
					EvDecimal start = nuc.getFirstFrame().add(c);
					EvDecimal end   = nuc.getLastFrame().subtract(c);
		
		
					if(start.less(end))
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
			
							
							Vector3d startPos=interStart.pos.getPosCopy();
							
							double avLenChild=0;
							int childCount=0;
							for(String cname:nuc.child)
								{
								NucLineage.Nuc child=lin.nuc.get(cname);
								if(!child.pos.isEmpty())
									{
									Vector3d vc=child.pos.get(child.pos.firstKey()).getPosCopy();
									vc.sub(startPos);
									avLenChild+=vc.length();
									childCount++;
									}
								}
							if(childCount==0)
								avLenChild=-1;
							else
								avLenChild/=childCount;
							
							
							Vector3d v=interEnd.pos.getPosCopy();
							v.sub(startPos);
							double straightDistance=v.length();
			
							Vector3d last=interStart.pos.getPosCopy();
			
							double fractalDist=0;
							for(Map.Entry<EvDecimal, NucLineage.NucPos> ee:nuc.pos.entrySet())
								if(ee.getKey().greater(start) && ee.getKey().less(end))
									{
									last.sub(ee.getValue().getPosCopy());
									fractalDist+=last.length();
									last=ee.getValue().getPosCopy();
									r.put(ee.getValue().r);
									}
							last.sub(interEnd.pos.getPosCopy());
							fractalDist+=last.length();
			
							
							double relDev=0;
							NucExp ediv=nuc.exp.get("divDev");
							if(ediv!=null)
								relDev=ediv.level.get(EvDecimal.ZERO)/nuc.getLastFrame().subtract(nuc.getFirstFrame()).doubleValue();
							
							//Write out
							out.println(nucName+"\t"+start+"\t"+end+"\t"+straightDistance+"\t"+fractalDist+"\t"+
									r.getAv()+"\t"+relDev+"\t"+avLenChild);
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
		System.exit(0);
		}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
		{
		EvLog.listeners.add(new StdoutLog());
		EV.loadPlugins();
	
		one("/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost");
/*
		one("/Volumes/TBU_main02/ost4dgood/N2_071116.ost");
		one("/Volumes/TBU_main03/ost4dgood/TB2167_0804016.ost");
		one("/Volumes/TBU_main02/ost4dgood/stdcelegansNew.ost");

		one("/Volumes/TBU_main03/ost4dgood/AnglerUnixCoords.ost");

		one("/Volumes/TBU_main02/ost4dgood/N2_071114.ost");

		one("/Volumes/TBU_main02/ost4dgood/N2greenLED080206.ost");
		one("/Volumes/TBU_main02/ost4dgood/TB2142_071129.ost");
		one("/Volumes/TBU_main02/ost4dgood/TB2164_080118.ost");
	*/	
		System.exit(0);
		//	one("/Volumes/TBU_main02/ostxml/mergedangler01_080522.xml");
		
		
		
		
		
		
		}
	
	}
