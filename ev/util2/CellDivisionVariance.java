package util2;

import java.io.File;
import java.util.*;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import endrov.data.*;
import endrov.ev.*;
import endrov.nuc.NucLineage;
import endrov.nuc.NucPair;
import endrov.nuc.NucLineage.NucInterp;


//Do not use rigid transforms, use point dist.

//in fitting all to one: possible to store individual rots, average, invert on assembly and hope it cancels

/**
 * Assemble c.e model
 * @author Johan Henriksson
 */
public class CellDivisionVariance
	{
	public static boolean showNeigh=false;
	public static boolean saveNormalized=true;
	public static int NUMTRY=0;

	public static Vector<EvData> worms=new Vector<EvData>();
	public static SortedMap<String, NucLineage> lins=new TreeMap<String, NucLineage>();

	

	public static void loadSelected()
		{
		//These all have timestep 10. NEED TO ADJUST LATER!
		//Load all worms to standardize from
		String[] wnlist={
				"/Volumes/TBU_main02/ost4dgood/N2_071114.ost",
				"/Volumes/TBU_main02/ost4dgood/N2_071116.ost",
				"/Volumes/TBU_main02/ost4dgood/TB2142_071129.ost",
				"/Volumes/TBU_main03/ost4dgood/TB2167_0804016.ost",  
				"/Volumes/TBU_main02/ost4dgood/TB2164_080118.ost",
				"/Volumes/TBU_main03/ost4dgood/TB2167_080409b.ost",
				}; 
		for(String s:wnlist)
			{
			EvData ost=new EvDataXML(s+"/rmd.ostxml");
//			Imageset ost=(Imageset)EvData.loadFile(new File(s));
//			System.out.println("Timestep "+ost.meta.metaTimestep);
			worms.add(ost);
			for(EvObject evob:ost.metaObject.values())
				{
				if(evob instanceof NucLineage)
					{
					NucLineage lin=(NucLineage)evob;
					if(lin.nuc.containsKey("ABa") && lin.nuc.containsKey("ABp") &&
							lin.nuc.containsKey("EMS") && lin.nuc.containsKey("P2'") && //these are required for the coord sys
							(lin.nuc.containsKey("ABal") || lin.nuc.containsKey("ABar")) &&
							(lin.nuc.containsKey("ABpl") || lin.nuc.containsKey("ABpr"))) //these make sense
						{
						lins.put(new File(s).getName(), lin);
						System.out.println("ok:"+s);
						}
					}
				}
			}
		
		}
	
	

	
	/**
	 * Entry point
	 */
	public static void main(String[] args)
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();
		
		loadSelected();

//		HashMap<String, Double> sumDur=new HashMap<String, Double>();
//		HashMap<String, Double> sumDur2=new HashMap<String, Double>();

		
		
		
		
		
				
		
		}

	}
	
			
	
	