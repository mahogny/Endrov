package util2.makeStdWormDist5;

import java.util.*;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import util2.ConnectImserv;

import endrov.data.*;
import endrov.ev.*;
import endrov.imageset.Imageset;
import endrov.imagesetImserv.EvImserv;
import endrov.nuc.NucLineage;
import endrov.nuc.NucPair;
import endrov.nuc.NucLineage.NucInterp;
import endrov.util.EvDecimal;

//TODO: all are now the same time!
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

	public static SortedMap<String, NucLineage> lins=new TreeMap<String, NucLineage>();
	public static NucStats nucstats=new NucStats();

	public static void loadSelected() throws Exception
		{
		System.out.println("Connecting");
		String url=ConnectImserv.url;
		String query="not trash and stdworm";
		EvImserv.EvImservSession session=EvImserv.getSession(new EvImserv.ImservURL(url));
		String[] imsets=session.conn.imserv.getDataKeys(query);
		System.out.println("Loading imsets");
		for(String s:imsets)
			{
			System.out.println("loading "+s);
			Imageset im=EvImserv.getImageset(url+s); 
			//TODO: should be able to go trough session to avoid url+s
			for(NucLineage lin:im.getObjects(NucLineage.class))
				{
				if(lin.nuc.containsKey("ABa") && lin.nuc.containsKey("ABp") &&
						lin.nuc.containsKey("EMS") && lin.nuc.containsKey("P2'") && //these are required for the coord sys
						(lin.nuc.containsKey("ABal") || lin.nuc.containsKey("ABar")) &&
						(lin.nuc.containsKey("ABpl") || lin.nuc.containsKey("ABpr"))) //these make sense
					{
					lins.put(s, lin); //TODO: only one per imset allowed
					System.out.println("ok:"+s);
					}
				}
			}
		
		
		}
	
	/**
	 * Copy lineage tree: all names and PC relations. no coordinates
	 */
	public static NucLineage copyTree(NucLineage lin)
		{
		NucLineage newlin=new NucLineage();
			
		for(Map.Entry<String, NucLineage.Nuc> e:lin.nuc.entrySet())
			{
			NucLineage.Nuc nuc=e.getValue();
			NucLineage.Nuc newnuc=newlin.getNucCreate(e.getKey());
			for(String s:nuc.child)
				newnuc.child.add(s);
			newnuc.parent=nuc.parent;
			}
		return newlin;
		}
	
	
	/**
	 * Normalize lineages in terms of size and rotation
	 */
	public static TreeMap<String, NucLineage> normalizeRot(SortedMap<String, NucLineage> lins)
		{
		System.out.println("--- normalize rigidbody ---");
		double avsize=0;
		TreeMap<String, NucLineage> newLins=new TreeMap<String, NucLineage>();
		for(Map.Entry<String, NucLineage> le:lins.entrySet())
			{
			NucLineage lin=le.getValue();
			//These define the normalized coord sys
			if(lin.nuc.containsKey("ABa") && lin.nuc.containsKey("ABp") &&
					lin.nuc.containsKey("EMS") && lin.nuc.containsKey("P2'"))
				{
				//Adjust pos
				newLins.put(le.getKey(),lin);
				center(lin);
				double thisSize=rotate1(lin);
				avsize+=thisSize;
				rotate2(lin);
				rotate3(lin);

				//Adjust radius
				for(NucLineage.Nuc nuc:lin.nuc.values())
					for(NucLineage.NucPos pos:nuc.pos.values())
						pos.r/=thisSize;
				}
			else
				System.out.println("one lin is not ok");
			}
		avsize/=newLins.size();
		System.out.println("avsize: "+avsize);
		for(NucLineage lin:newLins.values())
			{
			//Pos
			Matrix3d m=new Matrix3d();
			m.setIdentity();
			m.mul(avsize);
			applyMat(lin, m);
			
			//Adjust radius
			for(NucLineage.Nuc nuc:lin.nuc.values())
				for(NucLineage.NucPos pos:nuc.pos.values())
					pos.r*=avsize;
			}
		
		return newLins;
		}
	
	
	
	/**
	 * Normalize lineages in terms of time.
	 * The duration and start of a cell will match the reference 
	 */
	public static SortedMap<String, NucLineage> normalizeT(SortedMap<String, NucLineage> lins)
		{
		System.out.println("--- normalize T");
		TreeMap<String, NucLineage> newLins=new TreeMap<String, NucLineage>();
		for(Map.Entry<String, NucLineage> le:lins.entrySet())
			{
			NucLineage lin=le.getValue();
			NucLineage newlin=copyTree(lin);
			newLins.put(le.getKey(), newlin);
			
			for(Map.Entry<String, NucLineage.Nuc> e:lin.nuc.entrySet())
				{
				NucLineage.Nuc nuc=e.getValue();
				NucLineage.Nuc newnuc=newlin.getNucCreate(e.getKey());
				NucStats.NucStatsOne one=nucstats.nuc.get(e.getKey());
				EvDecimal thisDur;
				EvDecimal thisFirstFrame=nuc.pos.firstKey();
				if(nuc.child.isEmpty())
					thisDur=one.getLifeLen();
				else
					thisDur=nuc.lastFrame().subtract(nuc.pos.firstKey());
				EvDecimal oneLifeLen=one.getLifeLen();
				//potential trouble if no child and thisdur wrong
				for(EvDecimal frame:e.getValue().pos.keySet())
					{
					//This is the optimal place to take different timesteps into account
					EvDecimal newFrame=one.lifeStart.add(oneLifeLen.multiply(frame.subtract(thisFirstFrame)).divide(thisDur));
//					System.out.println("> "+e.getKey()+" "+one.lifeStart+" "+frame+" -> "+newFrame+" // "+one.lifeEnd);
					
					NucLineage.NucPos pos=nuc.pos.get(frame);
					newnuc.pos.put(newFrame, new NucLineage.NucPos(pos));
					}
				}
			}
		return newLins;
		}
	
	/**
	 * Set end frame of all cells without children to last frame. This stops them from occuring in interpolations.
	 */
	public static void endAllCells(SortedMap<String, NucLineage> lins)
		{
		//End all nuc without children for clarity
		for(NucLineage lin:lins.values())
			for(NucLineage.Nuc nuc:lin.nuc.values())
				if(nuc.child.isEmpty() && !nuc.pos.isEmpty())
					nuc.overrideEnd=nuc.pos.lastKey();
		}
	
	/**
	 * Get names of nuclei that appear in an interpolated frame
	 */
	public static SortedSet<String> interpNucNames(Map<NucPair, NucLineage.NucInterp> inter)
		{
		TreeSet<String> names=new TreeSet<String>();
		for(NucPair p:inter.keySet())
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
		for(NucLineage lin:lins.values())
			{
			//Relative time between AB and P1'
			//Could take child times into account as well to increase resolution
			if(lin.nuc.containsKey("AB") && lin.nuc.containsKey("P1'"))
				nucstats.ABPdiff.add(lin.nuc.get("AB").lastFrame().subtract(lin.nuc.get("P1'").lastFrame()));
			
			//Life length and children
			for(String nucname:lin.nuc.keySet())
				{
				NucLineage.Nuc nuc=lin.nuc.get(nucname);
				
				EvDecimal start=nuc.pos.firstKey();
				EvDecimal end=nuc.pos.lastKey();
				NucStats.NucStatsOne one=nucstats.get(nucname);
				if(nuc.parent!=null)
					one.parent=nuc.parent;

				//Should only add life time of this cell if it has children, otherwise there is no
				//guarantee that the length is correct.
				if(!nuc.child.isEmpty())
					one.lifetime.add(end.subtract(start).add(1)); //TODO bd really -1? depends on framerate
				}
			}
		nucstats.deriveLifetime();
		}

	
	/**
	 * Helper for rigid transform fitter: write transformed coordinates to a lineage object
	 */
	public static void writeRigidFitCoord(NucLineage newlin, BestFitRotTransScale bf, NucLineage lin, EvDecimal curframe)
		{
		for(String nucName:bf.lininfo.get(lin).untransformed.keySet())
			{
			NucLineage.NucPos npos=newlin.getNucCreate(nucName).getPosCreate(curframe);
			npos.setPosCopy(bf.lininfo.get(lin).transformed.get(nucName));
			npos.r=bf.lininfo.get(lin).untransformedR.get(nucName);
			}
		}
	
	/**
	 * Find the last keyframe ever mentioned in a lineage object
	 */
	public static EvDecimal lastFrameOfLineage(NucLineage lin)
		{
		EvDecimal maxframe=null;
		for(NucLineage.Nuc nuc:lin.nuc.values())
			{
			if(maxframe==null || nuc.pos.lastKey().greater(maxframe))
				maxframe=nuc.pos.lastKey();
			}
		return maxframe;
		}
	
	/**
	 * Find the first keyframe ever mentioned in a lineage object
	 */
	public static EvDecimal firstFrameOfLineage(NucLineage lin)
		{
		EvDecimal minframe=null;
		for(NucLineage.Nuc nuc:lin.nuc.values())
			{
			if(minframe==null || nuc.pos.firstKey().less(minframe))
				minframe=nuc.pos.firstKey();
			}
		return minframe;
		}
	
	
	/**
	 * Fit nuclei objects to one reference nuclei using rigid body transformations
	 */
	public static void rigidFitOverTime() throws Exception
		{
		//Choose one lineage for rotation reference
//		final NucLineage refLin=lins.get("TB2167_0804016.ost");
		final NucLineage refLin=lins.get("TB2167_0804016");
		if(refLin==null)
			throw new Exception("did not find rot ref");
		final EvDecimal fminframe=firstFrameOfLineage(refLin);
		final EvDecimal fmaxframe=lastFrameOfLineage(refLin);
		
		//Make copies of lineages
		SortedMap<String,NucLineage> newlin=new TreeMap<String, NucLineage>();
		for(Map.Entry<String, NucLineage> e:lins.entrySet())
			newlin.put(e.getKey(),copyTree(e.getValue()));
		

		System.out.println("--- rigid fit ---");
		BestFitRotTransScale firstBF=null;
		BestFitRotTransScale bf=new BestFitRotTransScale();
		
		
		boolean firstTime=true;
		
		//Add lineages
		for(NucLineage lin:lins.values())
			bf.addLineage(lin);
		
		int frameIncrement=1; //TODO bd what is the best value? check annotation. can save a lot of space and time!!
		
		for(EvDecimal curframe=fminframe;curframe.less(fmaxframe);curframe=curframe.add(frameIncrement))
//		for(int curframe=fminframe;curframe<1200;curframe++)
			{
			if(curframe.intValue()%30==0)
				System.out.println("frame "+curframe);
			
			//Fit
			bf=new BestFitRotTransScale(bf);
			for(Map.Entry<String, NucLineage> entry2:lins.entrySet())
				{
				NucLineage lin=entry2.getValue();
				
				//Interpolate for this frame
				Map<NucPair, NucLineage.NucInterp> interp=lin.getInterpNuc(curframe);
				//Only keep visible nuclei
				Set<NucPair> visibleNuc=new HashSet<NucPair>();
				for(Map.Entry<NucPair, NucLineage.NucInterp> e:interp.entrySet())
					if(e.getValue().isVisible())
						visibleNuc.add(e.getKey());
				interp.keySet().retainAll(visibleNuc);

				//Add coordinates
				for(Map.Entry<NucPair, NucLineage.NucInterp> entry:interp.entrySet())
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
			for(Map.Entry<String, NucLineage> e:lins.entrySet())
				writeRigidFitCoord(newlin.get(e.getKey()), bf, e.getValue(), curframe);
			}
			
		//Output rotated
		lins=newlin;
		}
	

	
	
	


	/**
	 * Assemble model using averaging.
	 * Calculate variance
	 */
	public static void assembleModel(NucLineage refLin)
		{
		//Fit coordinates
		EvDecimal maxframe=nucstats.maxFrame();
		EvDecimal minframe=nucstats.minFrame();
		System.out.println("--- fitting, from "+minframe+" to "+maxframe);
		EvDecimal frameInc=new EvDecimal(1); //TODO best value?
		for(EvDecimal frame=minframe;frame.less(maxframe);frame=frame.add(frameInc))
			{
			if(frame.intValue()%100==0)
				System.out.println(frame);

//			Map<String, NucStatsOne> curnuc=nucstats.getAtFrame(frame);
//			System.out.println("num ent "+curnuc.size());

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
	
	
	
	
	
	/**
	 * Entry point
	 */
	public static void main(String[] args)
		{
		try
			{
			Log.listeners.add(new StdoutLog());
			EV.loadPlugins();
			
			String outputName="/Volumes/TBU_main02/ostxml/model/stdcelegansNew3.ostxml";
			
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
							n.startsWith("shell") || n.equals("ant") || n.equals("post") || 
							n.equals("venc") || n.equals("germline") ||n.equals("2ftail") ||
							n.equals("P") || n.indexOf('?')>=0 || n.indexOf('_')>=0)
						nucstocopynot.add(n);
				nucstocopynot.add("int2D");
				for(String n:nucstocopynot)
					lin.removeNuc(n);
				}
			
			
			assembleTree();


			lins=normalizeRot(lins);
			lins=normalizeT(lins);
			endAllCells(lins); //Important for later interpolation, not just visualization
			rigidFitOverTime();
			endAllCells(lins);


			//Write tree to XML
			NucLineage combinedLin=nucstats.generateXMLtree();
			
			
			//Collect distances and radii
			System.out.println("--- collect spatial statistics");

			EvDecimal frameInc=new EvDecimal(1); //TODO best value?
			
			
			for(EvDecimal curframe=nucstats.minFrame();curframe.less(nucstats.maxFrame());curframe=curframe.add(frameInc))
				{
				if(curframe.intValue()%100==0)
					System.out.println(curframe);
				for(NucLineage lin:lins.values())
					{
					Map<NucPair, NucInterp> inter=lin.getInterpNuc(curframe);
					for(Map.Entry<NucPair, NucInterp> ie:inter.entrySet())
						{
						String thisnucname=ie.getKey().snd();
						NucInterp ni=ie.getValue();
						
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
				for(Map.Entry<String, NucLineage> e:lins.entrySet())
					output2.metaObject.put(e.getKey(),e.getValue());
				output2.metaObject.put("model", combinedLin);
				output2.saveMeta();
				*/
				
				EvData output2=new EvData();
				for(Map.Entry<String, NucLineage> e:lins.entrySet())
					output2.metaObject.put(e.getKey(),e.getValue());
				output2.metaObject.put("model", combinedLin);
				output2.saveFileAs("/Volumes/TBU_main02/ostxml/model/normalize3.ostxml");
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
			output.saveFileAs(outputName);

			
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
*/
