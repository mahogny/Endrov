/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
/**
 * 
 */
package endrov.nuc.ccm;

import java.util.*;

import endrov.neighmap.NeighMap;
import endrov.nuc.NucLineage;
import endrov.nuc.NucSel;
import endrov.nuc.NucVoronoi;
import endrov.util.EvDecimal;
import endrov.util.Tuple;

/**
 * Generate a cell contact map from a NucLineage
 * @author Johan Henriksson
 *
 */
public class MakeCellContactMap
	{
	private Map<EvDecimal,NucVoronoi> fcontacts=new HashMap<EvDecimal, NucVoronoi>();
	private NucLineage lin;

	//nuc -> nuc -> startframes
	private Map<String,Map<String,EvDecimal>> contactStart=new TreeMap<String, Map<String,EvDecimal>>();

	private NeighMap nmap=new NeighMap();
	
	
	private void addFrame(String a, String b, EvDecimal f)
		{
		if(a.compareTo(b)>0)
			{
			String c=b;
			b=a;
			a=c;
			}
		if(contactStart.get(a).get(b)==null)
			contactStart.get(b).put(a, f);
		}
	private void stopFrame(String a, String b, EvDecimal endFrame)
		{
		if(a.compareTo(b)>0)
			{
			String c=b;
			b=a;
			a=c;
			}
		EvDecimal start=contactStart.get(a).get(b);
		nmap.getCreateListFor(a, b).add(new NeighMap.Interval(start,endFrame));
		}
	private void lastCheckFrame(String a, String b, EvDecimal endFrame)
		{
		if(a.compareTo(b)>0)
			{
			String c=b;
			b=a;
			a=c;
			}
		EvDecimal start=contactStart.get(a).get(b);
		nmap.getCreateListFor(a, b).add(new NeighMap.Interval(start,endFrame));
		}
	
	
	public void calcneigh(TreeSet<String> nucNames, EvDecimal startFrame, EvDecimal endFrame, EvDecimal frameInc)
		{
		if(startFrame==null)
			startFrame=lin.firstFrameOfLineage().fst();
		if(endFrame==null)
			endFrame=lin.lastFrameOfLineage().fst();
		
		
		if(frameInc==null)
			{
			System.out.println("AIIIEEEE?");
			frameInc=new EvDecimal(1); //TODO what to do?
			}

		nmap.validity=new NeighMap.Interval(startFrame,endFrame);
		
		//Remember life spans
		for(String nname:contactStart.keySet())
			{
			NucLineage.Nuc nuc=lin.nuc.get(nname);
			nmap.lifetime.put(nname, new NeighMap.Interval(nuc.getFirstFrame(),nuc.getLastFrame()));
			}

		
		//Prepare different indexing
		
		for(String n:nucNames)
			{
			Map<String,EvDecimal> u=new HashMap<String,EvDecimal>();
			for(String m:nucNames)
				u.put(m,null);
			contactStart.put(n, u);
			}

		
		
		//Neighbours last iteration
		Set<Tuple<String, String>> lastNeigh=new HashSet<Tuple<String, String>>();
		
		//Go through all frames
		int numframes=0;
		NucVoronoi nvor=null;
		for(EvDecimal curframe=startFrame;curframe.lessEqual(endFrame);curframe=curframe.add(frameInc))
			{
			numframes++;
			/////////////////////////////
//                                  				if(numframes>200)					break;
                                				////////////////////
                
			
			
			//interpolate
			Map<NucSel, NucLineage.NucInterp> inter=lin.getInterpNuc(curframe);
			if(curframe.intValue()%100==0)
				System.out.println(curframe);
			try
				{
				//Eliminate cells not in official list or invisible
				Map<NucSel, NucLineage.NucInterp> interclean=new HashMap<NucSel, NucLineage.NucInterp>();
				//int numRealNuc=interclean.size();
				for(Map.Entry<NucSel, NucLineage.NucInterp> e:inter.entrySet())
					if(e.getValue().isVisible() && nucNames.contains(e.getKey().snd()))
						interclean.put(e.getKey(), e.getValue());
				//int numcleancell=interclean.size();
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
				
//					System.out.println("# inter "+inter.size());
				
				//Get neighbours
				nvor=new NucVoronoi(inter,true);
				fcontacts.put(curframe, nvor);
				//TODO if parent neigh at this frame, remove child?
				
				//Turn into more suitable index ordering for later use
				Set<Tuple<String, String>> newNeigh=nvor.getNeighPairSet();
				for(Tuple<String, String> e:newNeigh)
					addFrame(e.fst(),e.snd(),curframe);
				lastNeigh.removeAll(newNeigh);
				for(Tuple<String, String> e:lastNeigh)
					stopFrame(e.fst(),e.snd(),curframe);
				lastNeigh=newNeigh;

				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			}

		//Make sure to include the last neigh
		if(nvor!=null)
			{
			Set<Tuple<String, String>> newNeigh=nvor.getNeighPairSet();
			for(Tuple<String, String> e:newNeigh)
				lastCheckFrame(e.fst(),e.snd(),endFrame);
			}

		}
	
	
	
	/**
	 * Calculate cell contact map from a lineage, from startFrames <= f <= endFrame.
	 * Contacts for one frame can be obtained by letting startframe=endframe.
	 */
	public static NeighMap calculateCellMap(NucLineage lin, TreeSet<String> nucNames,
			EvDecimal startFrame, EvDecimal endFrame, EvDecimal frameInc)
		{
		MakeCellContactMap cm=new MakeCellContactMap();
		cm.lin=lin;
		cm.calcneigh(nucNames, startFrame, endFrame, frameInc);
		return cm.nmap;
		}
	
	}
