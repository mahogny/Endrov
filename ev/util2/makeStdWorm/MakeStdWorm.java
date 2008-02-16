package util2.makeStdWorm;

import java.util.*;

import javax.vecmath.Vector3d;

import evplugin.data.*;
import evplugin.ev.*;
import evplugin.nuc.NucLineage;
import evplugin.nuc.NucPair;


public class MakeStdWorm
	{

	public static void main(String[] args)
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();

		BestFit bf=new BestFit();
		boolean bfFirstTime=true;
		
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
		NucLineage refLin=lins.get(lins.firstKey());
		HashMap<NucLineage, Integer> linShift=new HashMap<NucLineage, Integer>();
		for(NucLineage lin:lins.values())
			if(lin==refLin)
				linShift.put(lin, 0);
			else
				{
				int shifttot=0;
				int numshift=0;
				
				for(String n:lin.nuc.keySet())
					{
					if(refLin.nuc.containsKey(n))
						{
						int s=lin.nuc.get(n).pos.firstKey()-refLin.nuc.get(n).pos.firstKey();
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
		
		//Remove all :-nucs from all lineages
		for(NucLineage lin:lins.values())
			{
			TreeSet<String> nucstocopynot=new TreeSet<String>();
			for(String n:lin.nuc.keySet())
				if(n.startsWith(":") || n.startsWith("shell") || n.equals("ant") || n.equals("post"))
					nucstocopynot.add(n);
			for(String n:nucstocopynot)
				lin.removeNuc(n);
			}
		
		//Merge nuclei
		for(NucLineage lin:lins.values())
			if(lin!=refLin)
				{
				//Which nuc to add. note: case sensitive
				TreeSet<String> nucstocopy=new TreeSet<String>();
				nucstocopy.addAll(lin.nuc.keySet());
				nucstocopy.removeAll(refLin.nuc.keySet());
				
				//Add tree structure
				for(String n:nucstocopy)
					{
					System.out.println("Adding nucleus: "+n);
					refLin.getNucCreate(n);
					NucLineage.Nuc orig=lin.nuc.get(n);
					refLin.nuc.get(n).child.addAll(refLin.nuc.get(n).child);
					refLin.nuc.get(n).parent=orig.parent;
					}
				for(String n:nucstocopy)
					{
					NucLineage.Nuc orig=lin.nuc.get(n);
					refLin.createParentChild(orig.parent, n);
					}
				for(String n:nucstocopy)
					{
					NucLineage.Nuc nuc=refLin.nuc.get(n);
					if(nuc.parent!=null && !refLin.nuc.containsKey(nuc.parent))
						nuc.parent=null;
					HashSet<String> cs=new HashSet<String>(nuc.child);
					for(String c:cs)
						if(!refLin.nuc.containsKey(c))
							nuc.child.remove(c);
					}
			
				//Add key frames
				HashSet<Integer> framestocopy=new HashSet<Integer>(); //Not shifted
//				for(String n:nucstocopy)
				for(String n:lin.nuc.keySet())
					{
					NucLineage.Nuc nuc=lin.nuc.get(n);
					framestocopy.addAll(nuc.pos.keySet());
					}
				for(Integer framei:framestocopy)
					{
					//Interpolate frame
					int refframe=framei-linShift.get(refLin)+linShift.get(lin);
					//System.out.println("refframe "+refframe);
					Map<NucPair,NucLineage.NucInterp> refi=refLin.getInterpNuc(refframe);
					Map<NucPair,NucLineage.NucInterp> lini=lin.getInterpNuc(framei);
					
					//Which nucs are in common?
					HashSet<String> nucrefi=new HashSet<String>();
					HashSet<String> nuclini=new HashSet<String>();
					for(NucPair p:refi.keySet())
						nucrefi.add(p.getRight());
					for(NucPair p:lini.keySet())
						nuclini.add(p.getRight());
					nucrefi.retainAll(nuclini);
					
					//Get the best fit
					Vector<String> bfNames=new Vector<String>();
					bf.clear();
					for(String n:nucrefi)
						{
						NucLineage.NucPos refpos=refi.get(new NucPair(refLin,n)).pos;
						NucLineage.NucPos linpos=lini.get(new NucPair(lin,n)).pos;
						bf.goalpoint.add(new Vector3d(refpos.x,refpos.y,refpos.z));
						bf.newpoint.add(new Vector3d(linpos.x,linpos.y,linpos.z));
						bfNames.add(n);
						}
					
					if(bfFirstTime)
						{
						bf.iterate(100,50000,0);
//						bf.iterate(100,5000,0);
						bfFirstTime=false;
						}
					
					
					bf.iterate(100,1000,30);
					System.out.println(""+framei+" "+refi.size()+" "+lini.size()+" "+bf.goalpoint.size()+" "+bf.eps+"     "+
							bf.getTx()+" "+bf.getTy()+" "+bf.getTz()+" "+bf.getRx()+" "+bf.getRy()+" "+bf.getRz()+" "+bf.getScale());
					
					/*
					//Copy positions
					for(String n:nucstocopy)
						{
						NucLineage.Nuc linnuc=lin.nuc.get(n);
						if(linnuc.pos.containsKey(framei))
							{
							NucLineage.Nuc refnuc=refLin.nuc.get(n);
							NucLineage.NucPos pos=new NucLineage.NucPos();
							
							NucLineage.NucPos origpos=linnuc.pos.get(framei);
							Vector3d bfpos=bf.transform(new Vector3d(origpos.x,origpos.y,origpos.z));
							pos.x=bfpos.x;
							pos.y=bfpos.y;
							pos.z=bfpos.z;
							pos.r=origpos.r*bf.getScale();
							
							refnuc.pos.put(refframe, pos);
							}
						
						}
*/

					//Update orig
					for(String n:lin.nuc.keySet())
						{
						NucLineage.Nuc linnuc=lin.nuc.get(n);
						
						NucLineage.NucInterp ni=lini.get(new NucPair(lin,n));
						if(ni!=null)
							{
							NucLineage.NucPos ipos=ni.pos;
							
							Vector3d bfpos=bf.transform(new Vector3d(ipos.x,ipos.y,ipos.z));
							ipos.x=bfpos.x;
							ipos.y=bfpos.y;
							ipos.z=bfpos.z;
							ipos.r=ipos.r*bf.getScale();
							linnuc.pos.put(framei, ipos);
							}
						}
					
					
					}
				
				//Remove key frames that overlap with the parent
				
				
				
				
				}
		
		//Save reference
		EvDataXML output=new EvDataXML("/Volumes/TBU_xeon01_500GB02/ostxml/stdcelegans.xml");
		output.metaObject.clear();
		output.addMetaObject(refLin); //assuming it doesn't use a parent pointer
		
		for(NucLineage lin:lins.values())
			if(lin!=refLin)
				output.addMetaObject(lin);
		
		
		output.saveMeta();
		}
	
	

	}
