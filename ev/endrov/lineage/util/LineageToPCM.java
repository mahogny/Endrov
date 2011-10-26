/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.lineage.util;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.EvLog;
import endrov.ev.EvLogStdout;
import endrov.lineage.Lineage;
import endrov.lineage.LineageSelParticle;
import endrov.particleContactMap.ParticleContactMap;
import endrov.particleContactMap.ParticleContactMapToHTML;
import endrov.util.EvDecimal;
import endrov.util.Tuple;

/**
 * Calculate PCMs from lineages
 * 
 * @author Johan Henriksson
 *
 */
public class LineageToPCM
	{

	
	/**
	 * Main function to do calculations
	 */
	public static ParticleContactMap calcneigh(Lineage lin, Set<String> nucNames, EvDecimal frameInc)
		{
		ParticleContactMap ccm=new ParticleContactMap();
		Map<EvDecimal,LineageVoronoi> fcontacts=new HashMap<EvDecimal, LineageVoronoi>();

		//By default, use all names
		if(nucNames==null)
			nucNames=lin.particle.keySet();
		
		//Sort out nucs that are not in the lineage or empty
		nucNames=new TreeSet<String>(nucNames);
		for(String name:new LinkedList<String>(nucNames))
			{
			Lineage.Particle nuc=lin.particle.get(name);
			if(nuc==null || nuc.pos.isEmpty())
				nucNames.remove(name);
			else
				{
				ccm.getCreateInfo(name).firstFrame=nuc.getFirstFrame();
				ccm.getCreateInfo(name).lastFrame=nuc.getLastFrame();
				}
			}
		
		//Prepare different indexing
		for(String n:nucNames)
			{
			Map<String,SortedSet<EvDecimal>> u=new HashMap<String, SortedSet<EvDecimal>>();
			for(String m:nucNames)
				u.put(m,new TreeSet<EvDecimal>());
			ccm.contactFrames.put(n, u);
			}

		//Go through all frames
		int numframes=0;
		for(EvDecimal curframe=lin.firstFrameOfLineage(false).fst();curframe.less(lin.lastFrameOfLineage(false).fst());curframe=curframe.add(frameInc))
			{
			ccm.framesTested.add(curframe);
			numframes++;
	                              				
			//interpolate
			Map<LineageSelParticle, Lineage.InterpolatedParticle> inter=lin.interpolateParticles(curframe);
			if(curframe.intValue()%100==0)
				System.out.println(curframe);
			try
				{
				//Eliminate cells not in official list or invisible
				Map<LineageSelParticle, Lineage.InterpolatedParticle> interclean=new HashMap<LineageSelParticle, Lineage.InterpolatedParticle>();
				for(Map.Entry<LineageSelParticle, Lineage.InterpolatedParticle> e:inter.entrySet())
					if(e.getValue().isVisible() && nucNames.contains(e.getKey().snd()))
						interclean.put(e.getKey(), e.getValue());
				inter=interclean;
				
				//Add false particle at distance to make voronoi calc possible
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
				LineageVoronoi nvor=new LineageVoronoi(inter,true);
				fcontacts.put(curframe, nvor);
				//TODO if parent neigh at this frame, remove child?
				
				//Turn into more suitable index ordering for later use
				for(Tuple<String, String> e:nvor.getNeighPairSet())
					ccm.addFrame(e.fst(),e.snd(),curframe);
				}
			catch (Exception e)
				{
				//This may happen to cases qhull cannot cope with
				}
			}
		return ccm;
		}
	
	
	
	//public static void calcCCM(EvData data)
	
	public static void main(String[] args)
		{
		EvLog.addListener(new EvLogStdout());
		EV.loadPlugins();
		
		System.out.println("--");
		EvData data=EvData.loadFile(new File("/home/tbudev3/_imageset/celegans2008.2.ost"));
		System.out.println("---");
//		EvData data=EvData.loadFile(new File("/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost"));
		Lineage ref=data.getIdObjectsRecursive(Lineage.class).values().iterator().next();
		
		
		ParticleContactMap ccm=(ParticleContactMap)data.metaObject.get("ccm");

		if(ccm==null)
			{
			ccm=calcneigh(ref, ref.particle.keySet(), new EvDecimal(30));
			data.metaObject.put("ccm", ccm);
			data.saveData();
			}
		
		
		File ccmFile=new File("/home/tbudev3/_imageset/testccm");
		
		HashMap<String,ParticleContactMap> maps=new HashMap<String, ParticleContactMap>();
		maps.put("ref",ccm);
		
		ParticleContactMapToHTML.generateHTML(maps, ccmFile);
		
		
		System.exit(0);
		}
	}
