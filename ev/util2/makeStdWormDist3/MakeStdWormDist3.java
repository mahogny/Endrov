package util2.makeStdWormDist3;

import java.io.File;
import java.util.*;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import evplugin.data.*;
import evplugin.ev.*;
import evplugin.imageset.Imageset;
import evplugin.nuc.NucLineage;
import evplugin.nuc.NucPair;
import evplugin.nuc.NucLineage.NucInterp;


import util2.makeStdWormDist3.NucStats.NucStatsOne;

//Do not use rigid transforms, use point dist.

public class MakeStdWormDist3
	{
	
	public static boolean allTime=true;
	public static boolean showNeigh=false;
	public static boolean saveNormalized=true;
	public static int NUMTRY=0;

	public static Vector<EvData> worms=new Vector<EvData>();
	public static TreeMap<String, NucLineage> lins=new TreeMap<String, NucLineage>();

	
	public static void loadAllNuc()
		{
		String[] dirs={"/Volumes/TBU_main01/ost4dgood","/Volumes/TBU_main02/ost4dgood","/Volumes/TBU_main03/ost4dgood"};
		for(String dir:dirs)
			{
			for(File f:new File(dir).listFiles())
				if(!f.getName().startsWith("."))
					{
//					String s=f.getPath();
					String s=f.getName();
					EvData ost=new EvDataXML(f.getPath()+"/rmd.xml");
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
								lins.put(s, lin);
								System.out.println("ok:"+s);
								}
	//						else
	//							System.out.println("not ok:"+s);
							}
						}
				
				}
			}
		
		
		}

	public static void loadSelected()
		{
		//These all have timestep 10. NEED TO ADJUST LATER!
		//Load all worms to standardize from
		String[] wnlist={
				"/Volumes/TBU_main02/ost4dgood/N2_071114",
				"/Volumes/TBU_main02/ost4dgood/N2_071116",
				"/Volumes/TBU_main02/ost4dgood/TB2142_071129",
				"/Volumes/TBU_main03/ost4dgood/TB2167_0804014",   //16
				"/Volumes/TBU_main02/ost4dgood/TB2164_080118",
				}; 
		for(String s:wnlist)
			{
			EvData ost=new EvDataXML(s+"/rmd.xml");
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
	
	public static void main(String[] args)
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();
		
		String outputName="/Volumes/TBU_main02/ostxml/model/stdcelegansNew.ostxml";
		
		loadSelected();

		//Get names of nuclei
		TreeSet<String> nucNames=new TreeSet<String>();
		for(NucLineage lin:lins.values())
			nucNames.addAll(lin.nuc.keySet());
		
		//Remove all :-nucs from all lineages, as well as just crap
		for(NucLineage lin:lins.values())
			{
			TreeSet<String> nucstocopynot=new TreeSet<String>();
			for(String n:lin.nuc.keySet())
				if(n.startsWith(":") || 
						n.startsWith("shell") || n.equals("ant") || n.equals("post") || n.equals("venc") || n.equals("germline") ||
						n.equals("P") || n.indexOf('?')>=0 || n.indexOf('_')>=0)
					nucstocopynot.add(n);
			for(String n:nucstocopynot)
				lin.removeNuc(n);
			}
		
		NucStats nucstats=new NucStats();
		
		//Collect tree
		System.out.println("--- collect tree");
		for(NucLineage lin:lins.values())
			{
			//Relative time between AB and P1'
			//Could take child times into account as well to increase resolution
			if(lin.nuc.containsKey("AB") && lin.nuc.containsKey("P1'"))
				nucstats.ABPdiff.add(lin.nuc.get("AB").lastFrame()-lin.nuc.get("P1'").lastFrame());
			
			//Life length and children
			for(String nucname:lin.nuc.keySet())
				{
				NucLineage.Nuc nuc=lin.nuc.get(nucname);
				
				int start=nuc.pos.firstKey();
				int end=nuc.pos.lastKey();
				if(start<1500 || allTime) //For testing
					{
					NucStats.NucStatsOne one=nucstats.get(nucname);
					if(nuc.parent!=null)
						one.parent=nuc.parent;

					//Should only add life time of this cell if it has children, otherwise there is no
					//guarantee that the length is correct.
					if(!nuc.child.isEmpty())
						one.lifetime.add(end-start+1);
					}
				}
			}
		nucstats.deriveLifetime();


		//Normalize lineages
		System.out.println("--- normalize");
		double avsize=0;
		Set<NucLineage> normalizedLin=new HashSet<NucLineage>();
		for(NucLineage lin:lins.values())
			{
			//These define the normalized coord sys
			if(lin.nuc.containsKey("ABa") && lin.nuc.containsKey("ABp") &&
					lin.nuc.containsKey("EMS") && lin.nuc.containsKey("P2'"))
				{
				normalizedLin.add(lin);
				center(lin);
				avsize+=rotate1(lin);
				rotate2(lin);
				rotate3(lin);
				}
			else
				System.out.println("one lin is not ok");
			}
		avsize/=normalizedLin.size();
		System.out.println("avsize: "+avsize);
		for(NucLineage lin:normalizedLin)
			{
			Matrix3d m=new Matrix3d();
			m.setIdentity();
			m.mul(avsize);
			applyMat(lin, m);
			}
		
		

		//Write tree to XML
		NucLineage refLin=nucstats.generateXMLtree();

		
		//Collect distances and radii
		System.out.println("--- collect spatial statistics");
		for(NucLineage lin:lins.values())
			{
			System.out.println("Adding lengths for "+lin);
			for(String thisnucname:lin.nuc.keySet())
				{
//				System.out.println("Adding lengths for "+thisnucname);
				NucStats.NucStatsOne one=nucstats.nuc.get(thisnucname);
				if(one!=null)
					{
					NucLineage.Nuc nuc=lin.nuc.get(thisnucname);
	
					int startFrame=nuc.pos.firstKey();
					int endFrame=nuc.lastFrame();
					int modelLifeLength=one.lifeEnd-one.lifeStart;
					if(nuc.child.isEmpty())
						{
						//If there are no children then life time must be assumed wrong. Then the second best thing is
						//to take the expected life time as the duration
						modelLifeLength=one.lifeEnd-one.lifeStart;
						endFrame=one.lifeEnd;
						
						System.out.println("No children for "+thisnucname);
						}
					
					//cells that stop early, can these mess up?

					
					//Ignore if duration not known. On the other hand, if this is the last cell we can accept it
					if(refLin.nuc.get(thisnucname).child.isEmpty() || !nuc.child.isEmpty()) 
					for(int frame=0;frame<modelLifeLength;frame++)
						{
						//Interpolate
						double iframe=one.interpolTime(startFrame, endFrame, frame);
						Map<NucPair, NucInterp> inter=lin.getInterpNuc(iframe);
						NucInterp thisi=inter.get(new NucPair(lin,thisnucname));
	
						if(thisi!=null && iframe<=endFrame)
							{
							one.addRadius(frame, thisi.pos.r);
							if(normalizedLin.contains(lin))
								one.addCollPos(frame, thisi.pos.getPosCopy());
							
							//Get distances
							if(NUMTRY>0)
								for(NucPair otherpair:inter.keySet())
									if(!otherpair.snd().equals(thisnucname))
										{
										String othernucname=otherpair.snd();
										NucStats.NucStatsOne otherOne=nucstats.get(othernucname);
										NucInterp otheri=inter.get(otherpair);
	
										Vector3d diff=thisi.pos.getPosCopy();
										diff.sub(otheri.pos.getPosCopy());
										double dist=diff.length();
										one.addDistance(frame, othernucname, dist);
										otherOne.addDistance(otherOne.toLocalFrame(one.toGlobalFrame(frame)), thisnucname, dist); //to make it symmetric
										}
							}
						//else
						//	System.out.println(" missing thisi "+thisnucname+ " "+frame);
						}
					}
				}
			}

		
		
		
		
		
		//Fit coordinates
		int maxframe=nucstats.maxFrame();
		int minframe=nucstats.minFrame();
//		minframe=200;
//		maxframe=100;
		System.out.println("--- fitting, from "+minframe+" to "+maxframe);
		for(int frame=minframe;frame<maxframe;frame++)
			{
			Map<String, NucStatsOne> curnuc=nucstats.getAtFrame(frame);
//			System.out.println("num ent "+curnuc.size());
			
			BestFitLength bf=new BestFitLength();
			bf.nuc=curnuc;
			
			for(String s:curnuc.keySet())
				{
				NucStatsOne one=nucstats.get(s);
				one.findNeigh(frame);
				
				if(showNeigh)
					{
					System.out.print(""+s+":: ");
					for(NucStats.Neigh n:one.neigh)
						System.out.print(""+n.name+":"+n.dist+" ");
					System.out.println();
					}
				}
			
			nucstats.prepareCoord(refLin, frame);

			
			//Guess: Get position by averaging over normalized sets
			for(String name:nucstats.nuc.keySet())
				{
				NucStatsOne one=nucstats.nuc.get(name);
				List<Vector3d> poshere=one.collectedPos.get(one.toLocalFrame(frame));
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
					}
				}
			
			//Optimize guess
			if(NUMTRY==1)
				{
				double minEps=1e-4;
				bf.iterate(500, 1000, minEps);
				for(String name:nucstats.nuc.keySet())
					if(nucstats.nuc.get(name).curpos!=null)
						nucstats.nuc.get(name).curpos=new Vector3d(nucstats.nuc.get(name).curpos);
				}
				
				
			//Iterate and randomize guesses, pick the best
				/*
			Double besteps=null;
			for(int curTry=0;curTry<NUMTRY;curTry++)
				{
				double minEps=1e-4;
				bf.iterate(500, 1000, minEps);
				
				if(besteps==null || bf.eps<besteps)
					{
					for(String name:nucstats.nuc.keySet())
						if(nucstats.nuc.get(name).curpos!=null)
							bestpos.put(name, new Vector3d(nucstats.nuc.get(name).curpos));
					besteps=bf.eps;
					}
				
				//Randomize coordinates: new
				for(NucStatsOne one:nucstats.nuc.values())
					one.curpos=new Vector3d(Math.random(),Math.random(),Math.random());
				}
			//Take the best
			if(besteps!=null)
				{
				for(String name:nucstats.nuc.keySet())
					nucstats.nuc.get(name).curpos=bestpos.get(name);
				bf.eps=besteps;
				}
			else
				bf.eps=0;
			*/

			/*
			//Get position by averaging over normalized sets
			for(String name:nucstats.nuc.keySet())
				{
				NucStatsOne one=nucstats.nuc.get(name);
				one.curpos=bestpos.get(name);
				
				List<Vector3d> poshere=one.collectedPos.get(one.toLocalFrame(frame));
				if(poshere!=null && !poshere.isEmpty())
					{
//					one.curposAvg
	//				Vector3d v=new Vector3d();
					one.curposAvg[0].clear();
					one.curposAvg[1].clear();
					one.curposAvg[2].clear();
					for(Vector3d u:poshere)
						{
//						v.add(u);
						one.curposAvg[0].count(u.x);
						one.curposAvg[1].count(u.y);
						one.curposAvg[2].count(u.z);
						}
//					v.scale(1.0/poshere.size());
//					one.curpos=v;
					one.curpos=new Vector3d(one.curposAvg[0].getMean(), one.curposAvg[1].getMean(), one.curposAvg[2].getMean());
					}

				}
*/

/*			double minEps=1e-4;
			bf.iterate(500, 1000, minEps);
*/
			
			
			//Here one could use the other fit TODO
			
			//Write out coordinates
			nucstats.writeCoord(refLin, frame);

			
//			System.out.println("frame: "+frame+"   eps: "+bf.eps);
			}

		
//	Save normalized lineages
		if(saveNormalized)
			{
			EvDataXML output2=new EvDataXML("/Volumes/TBU_main02/ostxml/model/normalize.ostxml");
			output2.metaObject.clear();
			for(Map.Entry<String, NucLineage> e:lins.entrySet())
				output2.metaObject.put(e.getKey(),e.getValue());
			output2.metaObject.put("model", refLin);
//			for(NucLineage lin:lins.values())
//				output2.addMetaObject(lin);
			output2.saveMeta();
			}
		
		
		//Save reference
		EvDataXML output=new EvDataXML(outputName);
		output.metaObject.clear();
		output.addMetaObject(refLin);
		output.saveMeta();
		}
	

	
	public static void applyMat(NucLineage lin, Matrix3d m)
		{
		for(NucLineage.Nuc nuc:lin.nuc.values())
			for(NucLineage.NucPos pos:nuc.pos.values())
				{
				Vector3d v=pos.getPosCopy();
				m.transform(v);
				pos.setPosCopy(v);
				}
		}
	
	public static void center(NucLineage lin)
		{
		NucLineage.Nuc nucABa=lin.nuc.get("ABa");
		NucLineage.NucPos posABa=nucABa.pos.get(nucABa.pos.lastKey());
		Vector3d sub=new Vector3d(posABa.getPosCopy());
		for(NucLineage.Nuc nuc:lin.nuc.values())
			for(NucLineage.NucPos pos:nuc.pos.values())
				{
				Vector3d v=pos.getPosCopy();
				v.sub(sub);
				pos.setPosCopy(v);
				}
		}
	
	
	
	
	
	public static double rotate1(NucLineage lin)
		{
		NucLineage.Nuc nucABa=lin.nuc.get("ABa");
		NucLineage.Nuc nucP2 =lin.nuc.get("P2'");

		NucLineage.NucPos posABa=nucABa.pos.get(nucABa.pos.lastKey());
		NucLineage.NucPos posP2 =nucP2.pos.get(nucP2.pos.lastKey());

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
	public static void rotate2(NucLineage lin)
		{
		NucLineage.Nuc nucABa=lin.nuc.get("ABa");
		NucLineage.Nuc nucP2 =lin.nuc.get("P2'");
		NucLineage.NucPos posABa=nucABa.pos.get(nucABa.pos.lastKey());
		NucLineage.NucPos posP2 =nucP2.pos.get(nucP2.pos.lastKey());

		Vector3d vdir=new Vector3d(posP2.x,posP2.y,posP2.z);
		vdir.sub(new Vector3d(posABa.x,posABa.y,posABa.z));

		double ang=Math.atan2(vdir.z,vdir.x); 
		Matrix3d m=new Matrix3d();
		m.rotY(ang);

		applyMat(lin,m);
		
		System.out.println("pos2 "+nucP2.pos.get(nucP2.pos.lastKey()).getPosCopy());
		}
	public static void rotate3(NucLineage lin)
		{
		NucLineage.Nuc nucABp=lin.nuc.get("ABp");
		NucLineage.Nuc nucEMS=lin.nuc.get("EMS");

		NucLineage.NucPos posABp=nucABp.pos.get(nucABp.pos.lastKey());
		NucLineage.NucPos posEMS=nucEMS.pos.get(nucEMS.pos.lastKey());
		Vector3d vdir=new Vector3d(posEMS.x,posEMS.y,posEMS.z);
		vdir.sub(new Vector3d(posABp.x,posABp.y,posABp.z));
		System.out.println("dir "+vdir);

		double ang=Math.atan2(vdir.z,vdir.y);
		Matrix3d m=new Matrix3d();
		m.rotX(-ang);

		applyMat(lin,m);
		
		NucLineage.Nuc nucP2 =lin.nuc.get("P2'");
		System.out.println("pos3 "+nucP2.pos.get(nucP2.pos.lastKey()).getPosCopy());
		
		Vector3d vdir2=new Vector3d(posEMS.x,posEMS.y,posEMS.z);
		vdir2.sub(new Vector3d(posABp.x,posABp.y,posABp.z));
		System.out.println("dir2 "+vdir2);
		}
	
	
	
			
	
	

	}
