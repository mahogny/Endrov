/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.paperStdCelegans.makeStdWorm;

import java.util.*;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import endrov.data.*;
import endrov.ev.*;
import endrov.lineage.Lineage;
import endrov.lineage.LineageSelParticle;
import endrov.util.EvDecimal;

//with OST3+, frametime concept gone, solved

//Do not use rigid transforms, use point dist.

//in fitting all to one: possible to store individual rots, average, invert on assembly and hope it cancels

/**
 * Assemble c.e model
 * @author Johan Henriksson
 */
public class MakeStdWorm5
	{
	public static boolean showNeigh=false;
	public static boolean saveNormalized=true;
	public static int NUMTRY=0;

	public static SortedMap<String, Lineage> lins=new TreeMap<String, Lineage>();
	public static NucStats nucstats=new NucStats();

	public static EvDecimal frameInc=new EvDecimal(10); //TODO best value?

	public static void loadSelected() throws Exception
		{
		//This function has to be rewritten to use this code again
		
		/*
		System.out.println("Connecting");
		String url=ConnectImserv.url;
		String query="not trash and stdworm";
		EvImserv.EvImservSession session=EvImserv.getSession(new EvImserv.ImservURL(url));
		String[] imsets=session.conn.imserv.getDataKeys(query);
		System.out.println("Loading imsets");
		for(String s:imsets)
			{
			System.out.println("loading "+s);
			EvData data=EvData.loadFile(url+s);
			//Imageset im=data.getObjects(Imageset.class).iterator().next();
			for(Lineage lin:data.getIdObjectsRecursive(Lineage.class).values())
			//for(Lineage lin:data.getObjects(Lineage.class))
				{
				if(lin.particle.containsKey("ABa") && lin.particle.containsKey("ABp") &&
						lin.particle.containsKey("EMS") && lin.particle.containsKey("P2'") && //these are required for the coord sys
						(lin.particle.containsKey("ABal") || lin.particle.containsKey("ABar")) &&
						(lin.particle.containsKey("ABpl") || lin.particle.containsKey("ABpr"))) //these make sense
					{
					lins.put(s, lin); //Limitation: only one lineage per imset allowed
					System.out.println("ok:"+s);
					}
				}
			}
		
		*/
		}
	
	/**
	 * Copy lineage tree: all names and PC relations. no coordinates
	 */
	public static Lineage copyTree(Lineage lin)
		{
		Lineage newlin=new Lineage();
			
		for(Map.Entry<String, Lineage.Particle> e:lin.particle.entrySet())
			{
			Lineage.Particle nuc=e.getValue();
			Lineage.Particle newnuc=newlin.getCreateParticle(e.getKey());
			newnuc.child.addAll(nuc.child);
			newnuc.parents.addAll(nuc.parents);
			}
		return newlin;
		}
	
	
	/**
	 * Normalize lineages in terms of size and rotation
	 */
	public static TreeMap<String, Lineage> normalizeRot(SortedMap<String, Lineage> lins)
		{
		System.out.println("--- normalize rigidbody ---");
		double avsize=0;
		TreeMap<String, Lineage> newLins=new TreeMap<String, Lineage>();
		for(Map.Entry<String, Lineage> le:lins.entrySet())
			{
			Lineage lin=le.getValue();
			//These define the normalized coord sys
			if(lin.particle.containsKey("ABa") && lin.particle.containsKey("ABp") &&
					lin.particle.containsKey("EMS") && lin.particle.containsKey("P2'"))
				{
				//Adjust pos
				newLins.put(le.getKey(),lin);
				center(lin);
				double thisSize=rotate1(lin);
				avsize+=thisSize;
				rotate2(lin);
				rotate3(lin);

				//Adjust radius
				for(Lineage.Particle nuc:lin.particle.values())
					for(Lineage.ParticlePos pos:nuc.pos.values())
						pos.r/=thisSize;
				}
			else
				System.out.println("one lin is not ok");
			}
		avsize/=newLins.size();
		System.out.println("avsize: "+avsize);
		for(Lineage lin:newLins.values())
			{
			//Pos
			Matrix3d m=new Matrix3d();
			m.setIdentity();
			m.mul(avsize);
			applyMat(lin, m);
			
			//Adjust radius
			for(Lineage.Particle nuc:lin.particle.values())
				for(Lineage.ParticlePos pos:nuc.pos.values())
					pos.r*=avsize;
			}
		
		return newLins;
		}
	
	
	
	/**
	 * Normalize lineages in terms of time.
	 * The duration and start of a cell will match the reference 
	 */
	public static SortedMap<String, Lineage> normalizeT(SortedMap<String, Lineage> lins)
		{
		System.out.println("--- normalize T");
		TreeMap<String, Lineage> newLins=new TreeMap<String, Lineage>();
		for(Map.Entry<String, Lineage> le:lins.entrySet())
			{
			Lineage lin=le.getValue();
			Lineage newlin=copyTree(lin);
			newLins.put(le.getKey(), newlin);
			
			for(Map.Entry<String, Lineage.Particle> e:lin.particle.entrySet())
				{
				Lineage.Particle nuc=e.getValue();
				Lineage.Particle newnuc=newlin.getCreateParticle(e.getKey());
				NucStats.NucStatsOne one=nucstats.nuc.get(e.getKey());
				EvDecimal thisDur;
				EvDecimal thisFirstFrame=nuc.getFirstFrame();
				if(nuc.child.isEmpty())
					thisDur=one.getLifeLen();
				else
					thisDur=nuc.getLastFrame().subtract(nuc.getFirstFrame());
				EvDecimal oneLifeLen=one.getLifeLen();
				//potential trouble if no child and thisdur wrong
				for(EvDecimal frame:e.getValue().pos.keySet())
					{
					//This is the optimal place to take different timesteps into account
					EvDecimal newFrame=one.lifeStart.add(oneLifeLen.multiply(frame.subtract(thisFirstFrame)).divide(thisDur));
//					System.out.println("> "+e.getKey()+" "+one.lifeStart+" "+frame+" -> "+newFrame+" // "+one.lifeEnd);
					
					Lineage.ParticlePos pos=nuc.pos.get(frame);
					newnuc.pos.put(newFrame, pos.clone());
					}
				}
			}
		return newLins;
		}
	
	/**
	 * Set end frame of all cells without children to last frame. This stops them from occuring in interpolations.
	 */
	public static void endAllCells(SortedMap<String, Lineage> lins)
		{
		//End all nuc without children for clarity
		for(Lineage lin:lins.values())
			for(Lineage.Particle nuc:lin.particle.values())
				if(nuc.child.isEmpty() && !nuc.pos.isEmpty())
					nuc.overrideEnd=nuc.pos.lastKey();
		}
	
	/**
	 * Get names of nuclei that appear in an interpolated frame
	 */
	public static SortedSet<String> interpNucNames(Map<LineageSelParticle, Lineage.InterpolatedParticle> inter)
		{
		TreeSet<String> names=new TreeSet<String>();
		for(LineageSelParticle p:inter.keySet())
			names.add(p.snd());
		return names;
		}

	/**
	 * Given all loaded lineages, figure out average life span of cells and collect the total lineage tree.
	 */
	public static void assembleTree()
		{
		//Collect tree
		System.out.println("--- collect tree");
		for(Lineage lin:lins.values())
			{
			//Relative time between AB and P1'
			//Could take child times into account as well to increase resolution
			if(lin.particle.containsKey("AB") && lin.particle.containsKey("P1'"))
				nucstats.ABPdiff.add(lin.particle.get("AB").getLastFrame().subtract(lin.particle.get("P1'").getLastFrame()));
			
			//Life length and children
			for(String nucname:lin.particle.keySet())
				{
				Lineage.Particle nuc=lin.particle.get(nucname);
				
				EvDecimal start=nuc.getFirstFrame();
				EvDecimal end=nuc.getLastFrame();
				NucStats.NucStatsOne one=nucstats.get(nucname);
				if(!nuc.parents.isEmpty())
					one.parent=nuc.parents.iterator().next();

				//Should only add life time of this cell if it has children, otherwise there is no
				//guarantee that the length is correct.
				if(!nuc.child.isEmpty())
					one.lifetime.add(end.subtract(start));
//				one.lifetime.add(end.subtract(start).add(1)); //TODO bd really -1? depends on framerate
				}
			}
		nucstats.deriveLifetime();
		}

	
	/**
	 * Helper for rigid transform fitter: write transformed coordinates to a lineage object
	 */
	public static void writeRigidFitCoord(Lineage newlin, BestFitRotTransScale bf, Lineage lin, EvDecimal curframe)
		{
		for(String nucName:bf.lininfo.get(lin).untransformed.keySet())
			{
			Lineage.ParticlePos npos=newlin.getCreateParticle(nucName).getCreatePos(curframe);
			npos.setPosCopy(bf.lininfo.get(lin).transformed.get(nucName));
			npos.r=bf.lininfo.get(lin).untransformedR.get(nucName);
			}
		}
	
	/**
	 * Find the last keyframe ever mentioned in a lineage object
	 */
	public static EvDecimal lastFrameOfLineage(Lineage lin)
		{
		EvDecimal maxframe=null;
		for(Lineage.Particle nuc:lin.particle.values())
			{
			if(maxframe==null || nuc.pos.lastKey().greater(maxframe))
				maxframe=nuc.pos.lastKey();
			}
		return maxframe;
		}
	
	/**
	 * Find the first keyframe ever mentioned in a lineage object
	 */
	public static EvDecimal firstFrameOfLineage(Lineage lin)
		{
		EvDecimal minframe=null;
		for(Lineage.Particle nuc:lin.particle.values())
			{
			if(minframe==null || nuc.getFirstFrame().less(minframe))
				minframe=nuc.getFirstFrame();
			}
		return minframe;
		}
	
	
	/**
	 * Fit nuclei objects to one reference nuclei using rigid body transformations
	 */
	public static void rigidFitOverTime() throws Exception
		{
		//Choose one lineage for rotation reference
//		final Lineage refLin=lins.get("TB2167_0804016.ost");
		final Lineage refLin=lins.get("TB2167_080416");
		if(refLin==null)
			throw new Exception("did not find rot ref");
		final EvDecimal fminframe=firstFrameOfLineage(refLin);
		final EvDecimal fmaxframe=lastFrameOfLineage(refLin);
		
		//Make copies of lineages
		SortedMap<String,Lineage> newlin=new TreeMap<String, Lineage>();
		for(Map.Entry<String, Lineage> e:lins.entrySet())
			newlin.put(e.getKey(),copyTree(e.getValue()));
		

		System.out.println("--- rigid fit ---");
		BestFitRotTransScale firstBF=null;
		BestFitRotTransScale bf=new BestFitRotTransScale();
		
		
		boolean firstTime=true;
		
		//Add lineages
		for(Lineage lin:lins.values())
			bf.addLineage(lin);
		
		
		for(EvDecimal curframe=fminframe;curframe.less(fmaxframe);curframe=curframe.add(frameInc))
//		for(int curframe=fminframe;curframe<1200;curframe++)
			{
			if(curframe.intValue()%30==0)
				System.out.println("frame "+curframe);
			
			//Fit
			bf=new BestFitRotTransScale(bf);
			for(Map.Entry<String, Lineage> entry2:lins.entrySet())
				{
				Lineage lin=entry2.getValue();
				
				//Interpolate for this frame
				Map<LineageSelParticle, Lineage.InterpolatedParticle> interp=lin.interpolateParticles(curframe);
				//Only keep visible nuclei
				Set<LineageSelParticle> visibleNuc=new HashSet<LineageSelParticle>();
				for(Map.Entry<LineageSelParticle, Lineage.InterpolatedParticle> e:interp.entrySet())
					if(e.getValue().isVisible())
						visibleNuc.add(e.getKey());
				interp.keySet().retainAll(visibleNuc);

				//Add coordinates
				for(Map.Entry<LineageSelParticle, Lineage.InterpolatedParticle> entry:interp.entrySet())
					{
					String nucname=entry.getKey().snd();
					bf.lininfo.get(lin).untransformed.put(nucname, entry.getValue().pos.getPosCopy());
					bf.lininfo.get(lin).untransformedR.put(nucname, entry.getValue().pos.r);
					}
				}
			bf.refLin=refLin;
			bf.findCommonNuc();
			
			//how many iteration?
			if(firstTime)
				bf.iterate(1000, 10000, 1e10);
			else
				bf.iterate(400, 10000, 1e10);
			firstTime=false;
			
			//Remember first rotation
			if(firstBF==null)
				firstBF=new BestFitRotTransScale(bf);

			//Write rotated coordinates
			for(Map.Entry<String, Lineage> e:lins.entrySet())
				writeRigidFitCoord(newlin.get(e.getKey()), bf, e.getValue(), curframe);
			}
			
		//Output rotated
		lins=newlin;
		}
	

	
	
	


	/**
	 * Assemble model using averaging.
	 * Calculate variance
	 */
	public static void assembleModel(Lineage refLin)
		{
		//Fit coordinates
		EvDecimal maxframe=nucstats.maxFrame();
		EvDecimal minframe=nucstats.minFrame();
		System.out.println("--- fitting, from "+minframe+" to "+maxframe);
		for(EvDecimal frame=minframe;frame.less(maxframe);frame=frame.add(frameInc))
			{
			if(frame.intValue()%100==0)
				System.out.println(frame);


			for(Map.Entry<String, NucStats.NucStatsOne> onee:nucstats.nuc.entrySet())
				{
				NucStats.NucStatsOne one=onee.getValue();
				if(onee.getValue().existAt(frame))
					{

					List<Vector3d> poshere=one.collectedPos.get(frame);
					if(poshere!=null && !poshere.isEmpty())
						{
						one.curposAvg[0].clear();
						one.curposAvg[1].clear();
						one.curposAvg[2].clear();
						for(Vector3d u:poshere)
							{
							one.curposAvg[0].count(u.x);
							one.curposAvg[1].count(u.y);
							one.curposAvg[2].count(u.z);
							}
						one.curpos=new Vector3d(one.curposAvg[0].getMean(), one.curposAvg[1].getMean(), one.curposAvg[2].getMean());
						Vector3d dv=new Vector3d(one.curpos);
						double sumsquare=0;
						double sumabsr=0;
						for(Vector3d u:poshere)
							{
							Vector3d v=new Vector3d(dv);
							v.sub(u);
							sumsquare+=v.lengthSquared();
							sumabsr+=v.length();
							}
						sumsquare/=poshere.size();
						sumabsr/=poshere.size();
//						one.rvar=sumsquare;
						one.raverror=sumabsr;
						}
					else
						System.out.println("isempty "+onee.getKey()+" @ "+frame);

					}
				else
					one.curpos=null;
				}
			nucstats.writeCoord(refLin, frame);
			}


		}




	

	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////

	
	public static void applyMat(Lineage lin, Matrix3d m)
		{
		for(Lineage.Particle nuc:lin.particle.values())
			for(Lineage.ParticlePos pos:nuc.pos.values())
				{
				Vector3d v=pos.getPosCopy();
				m.transform(v);
				pos.setPosCopy(v);
				}
		}
	
	public static void center(Lineage lin)
		{
		Lineage.Particle nucABa=lin.particle.get("ABa");
		Lineage.ParticlePos posABa=nucABa.pos.get(nucABa.pos.lastKey());
		Vector3d sub=new Vector3d(posABa.getPosCopy());
		for(Lineage.Particle nuc:lin.particle.values())
			for(Lineage.ParticlePos pos:nuc.pos.values())
				{
				Vector3d v=pos.getPosCopy();
				v.sub(sub);
				pos.setPosCopy(v);
				}
		}
	
	
	
	
	public static double rotate1(Lineage lin)
		{
		Lineage.Particle nucABa=lin.particle.get("ABa");
		Lineage.Particle nucP2 =lin.particle.get("P2'");

		Lineage.ParticlePos posABa=nucABa.pos.get(nucABa.pos.lastKey());
		Lineage.ParticlePos posP2 =nucP2.pos.get(nucP2.pos.lastKey());

		Vector3d vdir=new Vector3d(posP2.x,posP2.y,posP2.z);
		vdir.sub(new Vector3d(posABa.x,posABa.y,posABa.z));
		double size=vdir.length();

		//Rotate XY to align X
		//normalize length
		double ang=Math.atan2(vdir.y,vdir.x);
		Matrix3d m=new Matrix3d();
		m.rotZ(-ang);
		m.mul(1.0/size);
		
		applyMat(lin,m);
		
		System.out.println("pos1 "+nucP2.pos.get(nucP2.pos.lastKey()).getPosCopy());
		
		return size;
		}
	public static void rotate2(Lineage lin)
		{
		Lineage.Particle nucABa=lin.particle.get("ABa");
		Lineage.Particle nucP2 =lin.particle.get("P2'");
		Lineage.ParticlePos posABa=nucABa.pos.get(nucABa.pos.lastKey());
		Lineage.ParticlePos posP2 =nucP2.pos.get(nucP2.pos.lastKey());

		Vector3d vdir=new Vector3d(posP2.x,posP2.y,posP2.z);
		vdir.sub(new Vector3d(posABa.x,posABa.y,posABa.z));

		double ang=Math.atan2(vdir.z,vdir.x); 
		Matrix3d m=new Matrix3d();
		m.rotY(ang);

		applyMat(lin,m);
		
		System.out.println("pos2 "+nucP2.pos.get(nucP2.pos.lastKey()).getPosCopy());
		}
	public static void rotate3(Lineage lin)
		{
		Lineage.Particle nucABp=lin.particle.get("ABp");
		Lineage.Particle nucEMS=lin.particle.get("EMS");

		Lineage.ParticlePos posABp=nucABp.pos.get(nucABp.pos.lastKey());
		Lineage.ParticlePos posEMS=nucEMS.pos.get(nucEMS.pos.lastKey());
		Vector3d vdir=new Vector3d(posEMS.x,posEMS.y,posEMS.z);
		vdir.sub(new Vector3d(posABp.x,posABp.y,posABp.z));
		System.out.println("dir "+vdir);

		double ang=Math.atan2(vdir.z,vdir.y);
		Matrix3d m=new Matrix3d();
		m.rotX(-ang);

		applyMat(lin,m);
		
		Lineage.Particle nucP2 =lin.particle.get("P2'");
		System.out.println("pos3 "+nucP2.pos.get(nucP2.pos.lastKey()).getPosCopy());
		
		Vector3d vdir2=new Vector3d(posEMS.x,posEMS.y,posEMS.z);
		vdir2.sub(new Vector3d(posABp.x,posABp.y,posABp.z));
		System.out.println("dir2 "+vdir2);
		}
	
	
	
	
	
	/**
	 * Entry point
	 */
	public static void main(String[] args)
		{
		try
			{
			EvLog.addListener(new EvLogStdout());
			EV.loadPlugins();
			
			String outputName="/Volumes/TBU_main02/ost4dgood/celegans2008.new.ost";
			
			loadSelected();

			//Get names of nuclei
			TreeSet<String> nucNames=new TreeSet<String>();
			for(Lineage lin:lins.values())
				nucNames.addAll(lin.particle.keySet());
			
			//Remove all :-nucs from all lineages, as well as just crap
			for(Lineage lin:lins.values())
				{
				TreeSet<String> nucstocopynot=new TreeSet<String>();
				for(String n:lin.particle.keySet())
					if(n.startsWith(":") || 
							n.startsWith("shell") || n.equals("ant") || n.equals("post") || 
							n.equals("venc") || n.equals("germline") ||n.equals("2ftail") ||
							n.equals("P") || n.indexOf('?')>=0 || n.indexOf('_')>=0)
						nucstocopynot.add(n);
				nucstocopynot.add("int2D");
				for(String n:nucstocopynot)
					lin.removeParticle(n);
				}
			
			
			assembleTree();


			lins=normalizeRot(lins);
			lins=normalizeT(lins);
			endAllCells(lins); //Important for later interpolation, not just visualization
			rigidFitOverTime();
			endAllCells(lins);


			//Write tree to XML
			Lineage combinedLin=nucstats.generateXMLtree();
			
			
			//Collect distances and radii
			System.out.println("--- collect spatial statistics");

			
			
			for(EvDecimal curframe=nucstats.minFrame();curframe.less(nucstats.maxFrame());curframe=curframe.add(frameInc))
				{
				if(curframe.intValue()%100==0)
					System.out.println(curframe);
				for(Lineage lin:lins.values())
					{
					Map<LineageSelParticle, Lineage.InterpolatedParticle> inter=lin.interpolateParticles(curframe);
					for(Map.Entry<LineageSelParticle, Lineage.InterpolatedParticle> ie:inter.entrySet())
						{
						String thisnucname=ie.getKey().snd();
						Lineage.InterpolatedParticle ni=ie.getValue();
						
						NucStats.NucStatsOne one=nucstats.nuc.get(thisnucname);
						if(one!=null)
							{
							one.addRadius(curframe, ni.pos.r);
							one.addCollPos(curframe, ni.pos.getPosCopy());
							}
						else
							System.out.println("no one for "+thisnucname);
						}
					}
				}
			
			assembleModel(combinedLin);

			
			//Save normalized lineages
			if(saveNormalized)
				{
				/*
				EvIODataXML output2=new EvIODataXML("/Volumes/TBU_main02/ostxml/model/normalize3.ostxml");
				output2.metaObject.clear();
				for(Map.Entry<String, Lineage> e:lins.entrySet())
					output2.metaObject.put(e.getKey(),e.getValue());
				output2.metaObject.put("model", combinedLin);
				output2.saveMeta();
				*/
				
				EvData output2=new EvData();
				for(Map.Entry<String, Lineage> e:lins.entrySet())
					output2.metaObject.put(e.getKey(),e.getValue());
				output2.metaObject.put("model", combinedLin);
				output2.saveDataAs("/Volumes/TBU_main02/ost4dgood/celegans2008.new.all.ost");
				}
			

			//Save reference
			/*
			EvIODataXML output=new EvIODataXML(outputName);
			output.metaObject.clear();
			output.addMetaObject(combinedLin);
			output.saveMeta();
			*/
			
			EvData output=new EvData();
			output.addMetaObject(combinedLin);
			output.saveDataAs(outputName);

			
			System.out.println("Done");
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		System.exit(0);
		}

	}
	
	

	
	
	
			
	
//public static Vector<EvData> worms=new Vector<EvData>();
/*
private static void loadAllNuc()
	{
	String[] dirs={"/Volumes/TBU_main01/ost4dgood","/Volumes/TBU_main02/ost4dgood","/Volumes/TBU_main03/ost4dgood"};
	for(String dir:dirs)
		{
		for(File f:new File(dir).listFiles())
			if(!f.getName().startsWith("."))
				{
				String s=f.getName();
				EvData ost=new EvDataXML(f.getPath()+"/rmd.ostxml");
				worms.add(ost);
				for(EvObject evob:ost.metaObject.values())
					{
					if(evob instanceof Lineage)
						{
						Lineage lin=(Lineage)evob;
						if(lin.particle.containsKey("ABa") && lin.particle.containsKey("ABp") &&
								lin.particle.containsKey("EMS") && lin.particle.containsKey("P2'") && //these are required for the coord sys
								(lin.particle.containsKey("ABal") || lin.particle.containsKey("ABar")) &&
								(lin.particle.containsKey("ABpl") || lin.particle.containsKey("ABpr"))) //these make sense
							{
							lins.put(s, lin);
							System.out.println("ok:"+s);
							}
						}
					}
			
			}
		}
	
	
	}
*/



/*		
//These all have timestep 10. NEED TO ADJUST LATER!
//Load all worms to standardize from
String[] wnlist={
		"/Volumes/TBU_main02/ost4dgood/N2_071114.ost",
		"/Volumes/TBU_main02/ost4dgood/N2_071116.ost",
		"/Volumes/TBU_main02/ost4dgood/TB2142_071129.ost",
		"/Volumes/TBU_main03/ost4dgood/TB2167_0804016.ost",  
		"/Volumes/TBU_main02/ost4dgood/TB2164_080118.ost",  
		"/Volumes/TBU_main03/ost4dgood/TB2167_080409b.ost", //Not in CCM. why?
		//N2greenLED080206 is not in the list. why?
		}; 
for(String s:wnlist)
	{
	EvData ost=new EvDataXML(s+"/rmd.ostxml");
//	Imageset ost=(Imageset)EvData.loadFile(new File(s));
//	System.out.println("Timestep "+ost.meta.metaTimestep);
	worms.add(ost);
	for(EvObject evob:ost.metaObject.values())
		{
		if(evob instanceof Lineage)
			{
			Lineage lin=(Lineage)evob;
			if(lin.particle.containsKey("ABa") && lin.particle.containsKey("ABp") &&
					lin.particle.containsKey("EMS") && lin.particle.containsKey("P2'") && //these are required for the coord sys
					(lin.particle.containsKey("ABal") || lin.particle.containsKey("ABar")) &&
					(lin.particle.containsKey("ABpl") || lin.particle.containsKey("ABpr"))) //these make sense
				{
				lins.put(new File(s).getName(), lin);
				System.out.println("ok:"+s);
				}
			}
		}
	}
*/
