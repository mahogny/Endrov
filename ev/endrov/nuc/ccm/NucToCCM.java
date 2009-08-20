package endrov.nuc.ccm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import endrov.nuc.NucLineage;
import endrov.nuc.NucSel;
import endrov.nuc.NucVoronoi;
import endrov.util.EvDecimal;
import endrov.util.Tuple;

public class NucToCCM
	{

	
	
	
	
	
	
	
	/**
	 * Main function to do calculations
	 */
	public static CellContactMap calcneigh(NucLineage lin, TreeSet<String> nucNames, EvDecimal frameInc)
		{
		CellContactMap ccm=new CellContactMap();

		//Sort out nucs that are not in the lineage or empty
		nucNames=new TreeSet<String>(nucNames);
		for(String name:new LinkedList<String>(nucNames))
			{
			NucLineage.Nuc nuc=lin.nuc.get(name);
			if(nuc==null || nuc.pos.isEmpty())
				nucNames.remove(name);
			else
				{
				ccm.firstFrame.put(name, nuc.getFirstFrame());
				ccm.lastFrame.put(name, nuc.getLastFrame());
				}
			}
		ccm.allNuc=nucNames;
		
		//Prepare different indexing
		for(String n:nucNames)
			{
			Map<String,SortedSet<EvDecimal>> u=new HashMap<String, SortedSet<EvDecimal>>();
			for(String m:nucNames)
				u.put(m,new TreeSet<EvDecimal>());
			ccm.contactsf.put(n, u);
			}
	
		//Prepare life length
		for(String n:nucNames)
			ccm.lifelen.put(n, 0);
		
		//Go through all frames
		int numframes=0;
		for(EvDecimal curframe=lin.firstFrameOfLineage().fst();curframe.less(lin.lastFrameOfLineage().fst());curframe=curframe.add(frameInc))
			{
			ccm.framesTested.add(curframe);
			numframes++;
	                              				
			//interpolate
			Map<NucSel, NucLineage.NucInterp> inter=lin.getInterpNuc(curframe);
			if(curframe.intValue()%100==0)
				System.out.println(curframe);
			try
				{
				//Eliminate cells not in official list or invisible
				Map<NucSel, NucLineage.NucInterp> interclean=new HashMap<NucSel, NucLineage.NucInterp>();
				for(Map.Entry<NucSel, NucLineage.NucInterp> e:inter.entrySet())
					if(e.getValue().isVisible() && nucNames.contains(e.getKey().snd()))
						interclean.put(e.getKey(), e.getValue());
				inter=interclean;
				
				//Add false nuclei at distance to make voronoi calc possible
				if(!inter.isEmpty())
					{
					double r=3000; //300 is about the embryo. embryo is not centered in reality.
					
					NucLineage.NucInterp i1=new NucLineage.NucInterp();
					i1.pos=new NucLineage.NucPos();
					i1.frameBefore=EvDecimal.ZERO;
					i1.pos.x=r;
	
					NucLineage.NucInterp i2=new NucLineage.NucInterp();
					i2.pos=new NucLineage.NucPos();
					i2.frameBefore=EvDecimal.ZERO;
					i2.pos.x=-r;
	
					NucLineage.NucInterp i3=new NucLineage.NucInterp();
					i3.pos=new NucLineage.NucPos();
					i3.frameBefore=EvDecimal.ZERO;
					i3.pos.y=-r;
	
					NucLineage.NucInterp i4=new NucLineage.NucInterp();
					i4.pos=new NucLineage.NucPos();
					i4.frameBefore=EvDecimal.ZERO;
					i4.pos.y=-r;
	
					inter.put(new NucSel(null,":::1"), i1);
					inter.put(new NucSel(null,":::2"), i2);
					inter.put(new NucSel(null,":::3"), i3);
					inter.put(new NucSel(null,":::4"), i4);
					}
			
				//Get neighbours
				NucVoronoi nvor=new NucVoronoi(inter,true);
				ccm.fcontacts.put(curframe, nvor);
				//TODO if parent neigh at this frame, remove child?
				
				//Turn into more suitable index ordering for later use
				for(Tuple<String, String> e:nvor.getNeighPairSet())
					ccm.addFrame(e.fst(),e.snd(),curframe);
				//Calculate lifelen
				for(Map.Entry<NucSel, NucLineage.NucInterp> e:inter.entrySet())
					ccm.addLifelen(e.getKey().snd());
				
				}
			catch (Exception e)
				{
				}
			}
		
		
		return ccm;
		}
	}
