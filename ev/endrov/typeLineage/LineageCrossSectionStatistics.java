package endrov.typeLineage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.vecmath.Vector3d;

import endrov.core.EndrovCore;
import endrov.core.log.EvLog;
import endrov.core.log.EvLogStdout;
import endrov.data.EvData;
import endrov.typeLineage.Lineage.InterpolatedParticle;
import endrov.util.math.EvDecimal;

/**
 * Create statistics for lineages
 * 
 * @author Johan Henriksson
 *
 */
public class LineageCrossSectionStatistics
	{
	
	
	/**
	 * For internal use only, sorting nuclei by distance
	 */
	private static class NameDist implements Comparable<NameDist>
		{
		public NameDist(String name, double dist2)
			{
			this.name = name;
			this.dist2 = dist2;
			}

		public String name;
		public double dist2;
		
		public int compareTo(NameDist a)
			{
			return Double.compare(dist2, a.dist2);
			/*if(dist2<a.dist2)
				return -1;
			else if(dist2>a.dist2)
				return 1;
			else
				return 0;*/
			}
		}

	
	public static TreeSet<EvDecimal> getAllPositionFrames(Lineage lin)
		{
		TreeSet<EvDecimal> set=new TreeSet<EvDecimal>();
		for(Lineage.Particle p:lin.particle.values())
			{
			set.addAll(p.pos.keySet());
			if(p.overrideStart!=null)
				set.add(p.overrideStart);
			if(p.overrideEnd!=null)
				set.add(p.overrideEnd);
			}
		return set;
		}

	
	public static Map<String,Vector3d> getVisibleParticles(Lineage lin, EvDecimal frame)
		{
		Map<LineageSelParticle, InterpolatedParticle> allpos2=lin.interpolateParticles(frame);
		Map<String, Vector3d> allpos=new TreeMap<String, Vector3d>();
		for(LineageSelParticle sel:allpos2.keySet())
			if(allpos2.get(sel).isVisible())
				allpos.put(sel.snd(), allpos2.get(sel).pos.getPosCopy());
		return allpos;
		}
	
	
	/**
	 * Get a set containing all daughter cells for a given cell, that exist at a given frame
	 */
	public static Set<String> getLaterCellsFor(Lineage lin, String cell, EvDecimal frame)
		{
		Set<String> set=new TreeSet<String>();
		getLaterCellsFor(set, lin, cell, frame);
		return set;
		}
	private static void getLaterCellsFor(Set<String> set, Lineage lin, String cell, EvDecimal frame)
		{
		EvDecimal last=lin.particle.get(cell).getLastFrame();
		if(last!=null && last.less(frame))
			{
			for(String cName:lin.particle.get(cell).child)
				getLaterCellsFor(set, lin, cName, frame);
			}
		else
			set.add(cell);
		}
	
	
	public static Vector3d getLaterCellPosFor(Lineage lin, String cell, EvDecimal frame)
		{
		Set<Vector3d> set=new HashSet<Vector3d>(); //linked list could/should be enough
		getLaterCellPosFor(set, lin, cell, frame);
		if(set.isEmpty())
			return null;
		else
			{
			Vector3d v=new Vector3d();
			for(Vector3d u:set)
				v.add(u);
			v.scale(1.0/set.size());
			return v;
			}
		}
	private static void getLaterCellPosFor(Set<Vector3d> set, Lineage lin, String cell, EvDecimal frame)
		{
		Lineage.InterpolatedParticle inter=lin.particle.get(cell).interpolatePos(frame);
		if(inter==null)
			{
			for(String cName:lin.particle.get(cell).child)
				getLaterCellPosFor(set, lin, cName, frame);
			}
		else
			set.add(inter.pos.getPosCopy());
		}
	
	
	public static void calculateCrossRatios(Lineage lin, String expName, EvDecimal dt, EvDecimal timeForward)
		{
		/**
		 * 
		 * need: moving average.
		 * 
		 * what about the local transformation matrix?
		 * 
		 * suggestion on splines
		 * 
		 */
		
		
		//TODO!!!
		EvDecimal start=getAllPositionFrames(lin).first();
		EvDecimal end=getAllPositionFrames(lin).last();
		start=new EvDecimal(30*80);
		end=new EvDecimal(2*3600 + 20*60);
		
		
		for(EvDecimal frame=start;frame.less(end);frame=frame.add(dt))
			{
			System.out.println("---frame: "+frame);
			
			Map<String, Vector3d> allpos=getVisibleParticles(lin, frame);

			//Get positions for the next time point, by averaging positions of children if needed
			Map<String, Vector3d> allposNext=new TreeMap<String, Vector3d>();
			for(String n:allpos.keySet())
				{
				Vector3d v=getLaterCellPosFor(lin, n, frame.add(timeForward));
				if(v!=null)
					allposNext.put(n,v);
				else
					System.out.println("Nucleus excluded because of lack of coordinates: "+n);
				}
			

			//Calculate cross ratio for each particle
			for(String thisName:allposNext.keySet())
				{
				
				Vector3d thisPos=allpos.get(thisName);
				
		/*		
				//Calculate distance-weight for each neighbour
			//	TreeMap<String,Double> weights=new TreeMap<String, Double>();
				for(String otherName:allpos.keySet())
					{
					Vector3d otherPos=new Vector3d(allpos.get(otherName));
					otherPos.sub(thisPos);
					weights.put(otherName,Math.exp(-otherPos.lengthSquared()/(sigma*sigma)));
					}
	*/			
				
				//Find the closest neighbours
				ArrayList<NameDist> neighDist=new ArrayList<LineageCrossSectionStatistics.NameDist>();
				for(String otherName:allposNext.keySet())
					//if(!otherName.equals(thisName))
						{
						Vector3d v=new Vector3d(allpos.get(otherName));
						v.sub(thisPos);
						neighDist.add(new NameDist(otherName, v.lengthSquared()));
						}
				Collections.sort(neighDist);
				TreeSet<String> neigh=new TreeSet<String>();
				for(NameDist n:neighDist)
					{
					neigh.add(n.name);
					if(neigh.size()==8)
						break;
					}
				
				//Calculate cross-section for all pairs
				TreeSet<Double> allQprim=new TreeSet<Double>();
				for(String nA:neigh)
					for(String nB:neigh)
						if(!nB.equals(nA))
							for(String nC:neigh)
								if(!nC.equals(nA) && !nC.equals(nB))
									for(String nD:neigh)
										if(!nD.equals(nA) && !nD.equals(nB) && !nD.equals(nC))
											{
											
											Vector3d a=allpos.get(nA);
											Vector3d b=allpos.get(nB);
											Vector3d c=allpos.get(nC);
											Vector3d d=allpos.get(nD);
	
											/*
											Vector3d da=allposNext.get(nA);
											Vector3d db=allposNext.get(nB);
											Vector3d dc=allposNext.get(nC);
											Vector3d dd=allposNext.get(nD);
	
											da.sub(a);
											db.sub(b);
											dc.sub(c);
											dd.sub(d);
											
											double ddt=timeForward.doubleValue();
											da.scale(1.0/ddt);
											db.scale(1.0/ddt);
											dc.scale(1.0/ddt);
											dd.scale(1.0/ddt);
	
											
											double Qprim=calcQprim(a,b,c,d,  da,db,dc,dd);
											allQprim.add(Qprim);
											*/
							
											
											Vector3d a2=allposNext.get(nA);
											Vector3d b2=allposNext.get(nB);
											Vector3d c2=allposNext.get(nC);
											Vector3d d2=allposNext.get(nD);
											
											double q1=Math.sqrt(calcQ2(a, b, c, d));
											double q2=Math.sqrt(calcQ2(a2,b2,c2,d2));
											
											double Qprim=(q2-q1)/(q1+q2);
											allQprim.add(Qprim);
											}
	
				
				double out=allQprim.last();
				lin.particle.get(thisName).getCreateExp(expName).level.put(frame, out);
				}
			
			}
		
		}
	
	
	
	public static double calcQ2(Vector3d a, Vector3d b, Vector3d c, Vector3d d)
		{
		return diff2(a,b)*diff2(c,d) / (diff2(a,c)*diff2(b,d));
		}
	
	
	private static double diff2(Vector3d a, Vector3d b)
		{
		Vector3d v=new Vector3d(a);
		v.sub(b);
		return v.lengthSquared();
		}
	

	/**
	 * Calculate Q'
	 * This code appears to not work(?). or just not tested for the latest data
	 */
	public static double calcQprim(
			Vector3d a, Vector3d b, Vector3d c, Vector3d d,
			Vector3d da, Vector3d db, Vector3d dc, Vector3d dd)
		{
		double Q2upperPrim=0;
		for(int i=0;i<2;i++)
			Q2upperPrim+=diff2(a,b,da,db,1,2,i) * diff2(c,d,dc,dd,1,2,i);
		
		double Q2lowerPrim=0;
		for(int i=0;i<2;i++)
			Q2lowerPrim+=diff2(a,c,da,dc,1,2,i) * diff2(b,d,db,dd,1,2,i);
		
		double Q2upper=diff2(a,b)*diff2(c,d);
		double Q2lower=diff2(a,c)*diff2(b,d);
		double Q2prim=(Q2upperPrim*Q2lower + Q2upper*Q2lowerPrim)/(Q2lower*Q2lower);
		double Qprim=0.5*Q2prim/Math.sqrt(Q2prim);
		
		return Qprim;
		}

	private static double diff2(
			Vector3d a, Vector3d b, 
			Vector3d da, Vector3d db,
			int indexA, int indexB, int diffIndex)
		{
		if(diffIndex==indexA)
			{
			return 
					2*(a.x-b.x)*da.x +
					2*(a.y-b.y)*da.y +
					2*(a.z-b.z)*da.z;
			}
		else if(diffIndex==indexB)
			{
			return 
					-2*(a.x-b.x)*db.x +
					-2*(a.y-b.y)*db.y +
					-2*(a.z-b.z)*db.z;
			}
		else
			{
			return diff2(a,b);
			}
		}

	
	public static void main(String[] args)
		{
		
		
		try
			{
			EvLog.addListener(new EvLogStdout());
			EndrovCore.loadPlugins();
			
//			CD20081007_F47H4_1_14_L1

			
//			EvData data=EvData.loadFile("/home/mahogny/epic/CD20080714_glp-1_5_L2.csv");
			EvData data=EvData.loadFile("/home/mahogny/epic/CD20081007_F47H4_1_14_L1.csv");
			
			
			Lineage lin=data.getObjects(Lineage.class).iterator().next();
			
			
			EvDecimal dt=new EvDecimal(60);
			EvDecimal timeForward=new EvDecimal(2*60);
			calculateCrossRatios(lin,"cr", dt, timeForward);
			
			data.saveDataAs(new File("/home/mahogny/epic/F47H4.ost"));
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		
		
		}
	
	
	}
