/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
/**
 * 
 */
package endrov.particle.util;

import java.util.*;

import endrov.particle.Lineage;
import endrov.particle.LineageSelParticle;
import endrov.particleContactMap.neighmap.NeighMap;
import endrov.util.EvDecimal;
import endrov.util.Tuple;

/**
 * Calculate a particle contact map
 * @author Johan Henriksson
 *
 */
public class MakeParticleContactMap
	{
	private Map<EvDecimal,LineageVoronoi> fcontacts=new HashMap<EvDecimal, LineageVoronoi>();
	private Lineage lin;

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
	
	/**
	 * Calculate neighbours for one frame. Adds particles at a distance to avoid some really nonbiological contacts. These are also returned (starts with :::) 
	 */
	public static LineageVoronoi calcneighOneFrame(Set<String> nucNames, Map<LineageSelParticle, Lineage.InterpolatedParticle> inter, boolean selfContact) throws Exception
		{
		//Eliminate cells not in official list or invisible
		Map<LineageSelParticle, Lineage.InterpolatedParticle> interclean=new HashMap<LineageSelParticle, Lineage.InterpolatedParticle>();
		for(Map.Entry<LineageSelParticle, Lineage.InterpolatedParticle> e:inter.entrySet())
			if(e.getValue().isVisible() && nucNames.contains(e.getKey().snd()))
				interclean.put(e.getKey(), e.getValue());
		inter=interclean;
		
		//Add false particles at distance to make voronoi calc possible
		if(!inter.isEmpty())
			{
			double r=3000; //300 is about the embryo. embryo is not centered in reality.
			
			Lineage.InterpolatedParticle i1=new Lineage.InterpolatedParticle();
			i1.pos=new Lineage.ParticlePos();
			i1.frameBefore=EvDecimal.ZERO;
			i1.pos.x=r;

			Lineage.InterpolatedParticle i2=new Lineage.InterpolatedParticle();
			i2.pos=new Lineage.ParticlePos();
			i2.frameBefore=EvDecimal.ZERO;
			i2.pos.x=-r;

			Lineage.InterpolatedParticle i3=new Lineage.InterpolatedParticle();
			i3.pos=new Lineage.ParticlePos();
			i3.frameBefore=EvDecimal.ZERO;
			i3.pos.y=-r;

			Lineage.InterpolatedParticle i4=new Lineage.InterpolatedParticle();
			i4.pos=new Lineage.ParticlePos();
			i4.frameBefore=EvDecimal.ZERO;
			i4.pos.y=-r;

			inter.put(new LineageSelParticle(null,":::1"), i1);
			inter.put(new LineageSelParticle(null,":::2"), i2);
			inter.put(new LineageSelParticle(null,":::3"), i3);
			inter.put(new LineageSelParticle(null,":::4"), i4);
			}
		
		//Get neighbours
		return new LineageVoronoi(inter,selfContact);
		}
	
	public void calcneigh(Set<String> nucNames, EvDecimal startFrame, EvDecimal endFrame, EvDecimal frameInc)
		{
		//Defaults
		if(startFrame==null)
			startFrame=lin.firstFrameOfLineage().fst();
		if(endFrame==null)
			endFrame=lin.lastFrameOfLineage().fst();
		if(frameInc==null)
			frameInc=new EvDecimal(1);

		nmap.validity=new NeighMap.Interval(startFrame,endFrame);
		
		//Remember life spans
		for(String nname:contactStart.keySet())
			{
			Lineage.Particle nuc=lin.particle.get(nname);
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
		LineageVoronoi nvor=null;
		for(EvDecimal curframe=startFrame;curframe.lessEqual(endFrame);curframe=curframe.add(frameInc))
			{
			numframes++;
			
			//interpolate
			Map<LineageSelParticle, Lineage.InterpolatedParticle> inter=lin.interpolateParticles(curframe);
			if(curframe.intValue()%100==0)
				System.out.println(curframe);
			try
				{
				nvor=calcneighOneFrame(nucNames, inter, true);
				
				//Get neighbours
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
	public static NeighMap calculateCellMap(Lineage lin, Set<String> nucNames,
			EvDecimal startFrame, EvDecimal endFrame, EvDecimal frameInc)
		{
		MakeParticleContactMap cm=new MakeParticleContactMap();
		cm.lin=lin;
		cm.calcneigh(nucNames, startFrame, endFrame, frameInc);
		return cm.nmap;
		}
	
	}
