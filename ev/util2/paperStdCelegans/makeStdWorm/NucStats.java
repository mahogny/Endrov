/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.paperStdCelegans.makeStdWorm;

import java.util.*;
import java.util.Map.Entry;
import javax.vecmath.Vector3d;

import endrov.typeLineage.*;
import endrov.typeLineage.Lineage.ParticlePos;
import endrov.util.math.EvDecimal;



public class NucStats
	{

	public TreeMap<String, NucStatsOne> nuc=new TreeMap<String,NucStatsOne>();
	
	public List<EvDecimal> ABPdiff=new LinkedList<EvDecimal>();

	
	/**
	 * Neighbour
	 */
	public class Neigh implements Comparable<Neigh>
		{
		public String name;
		public double weight;
		public double dist;
		public double distVar;
		
		public int compareTo(Neigh other)
			{
			if(dist<other.dist)				return -1;
			else if(dist>other.dist)	return 1;
			else return 0;
			}
		}

	
	public static class StatDouble
		{
		private double x=0;
		private double x2=0;
		private int count=0;
		public void clear()
			{
			x=x2=count=0;
			}
		public void count(double x)
			{
			this.x+=x;
			this.x2+=x*x;
			count++;
			}
		public double getMean()
			{
			return x/count;
			}
		public double getVar()
			{
			double mean=getMean();
			return x2/(count-1.0) - mean*mean*count/(count-1.0);
			}
		public int getCount()
			{
			return count;
			}
		}
	
	/**
	 * Stats about one nuc
	 */
	public class NucStatsOne
		{
		//Stats
		public List<EvDecimal> lifetime=new LinkedList<EvDecimal>();
		public SortedMap<EvDecimal, Map<String,StatDouble>> distance=new TreeMap<EvDecimal, Map<String,StatDouble>>(); //frame rel start, nuc, length
		public SortedMap<EvDecimal, StatDouble> radius=new TreeMap<EvDecimal, StatDouble>();
		
		public SortedMap<EvDecimal, List<Vector3d>> collectedPos=new TreeMap<EvDecimal, List<Vector3d>>();

		//
		public double raverror;
		
		//Derived
		public EvDecimal lifeStart;
		public EvDecimal lifeEnd;
		public String parent;
		public Map<EvDecimal, List<Lineage.ParticlePos>> derivedPos=new TreeMap<EvDecimal, List<Lineage.ParticlePos>>();
		
		//Used in BestFitLength
		public Vector3d curpos=null;
		public Vector3d df=new Vector3d();
		public TreeSet<Neigh> neigh=new TreeSet<Neigh>();
		public StatDouble[] curposAvg=new StatDouble[]{new StatDouble(),new StatDouble(),new StatDouble()};
		
		public boolean existAt(EvDecimal frame)
			{
			return lifeStart.lessEqual(frame) && frame.less(lifeEnd);
			}
		
		
		/**
		 * Decide on average distances, weights and which neighbours to consider
		 */
		public void findNeigh(EvDecimal frame)
			{
			Map<String,StatDouble> thisdistances=distance.get(frame.subtract(lifeStart));
			neigh.clear();
			if(thisdistances==null)
				System.out.println("<<<<no distances!!!>>>>");
			else
				for(String t:thisdistances.keySet())
					if(nuc.containsKey(t) && nuc.get(t).existAt(frame))
						{
						double meanDist=thisdistances.get(t).getMean();
						double s2=thisdistances.get(t).getVar();
						if(s2<0.1)
							s2=0.1;
							
						Neigh n=new Neigh();
						n.name=t;
						n.dist=meanDist;
						n.distVar=s2;
						neigh.add(n);
						
	//					System.out.println(" variance "+n.distVar+" toward "+n.name+ " dist "+n.dist+" -- "+y2sum+" "+meanDist);
						}
			
			//Set weights
			
			//Weights based on variance
			double totalS2=0;
			for(Neigh n:neigh)
				totalS2+=1.0/Math.sqrt(n.distVar);
			for(Neigh n:neigh)
				{
				n.weight=1.0/Math.sqrt(n.distVar)/totalS2;
//				System.out.println("weight "+totalS2);
				}
			
			
			//Uniform weights
//			for(Neigh n:neigh)
//				n.weight=1.0/neigh.size();
			}

		
		
		
		
		public void addRadius(EvDecimal frame, double rs)
			{
			StatDouble rl=radius.get(frame);
			if(rl==null)
				radius.put(frame, rl=new StatDouble());
			rl.count(rs);
			}
		public void addCollPos(EvDecimal frame, Vector3d pos)
			{
			List<Vector3d> rl=collectedPos.get(frame);
			if(rl==null)
				collectedPos.put(frame, rl=new LinkedList<Vector3d>());
			rl.add(pos);
			}
		
		public void addDistance(EvDecimal frame, String nuc, double dist)
			{
			Map<String,StatDouble> rl=distance.get(frame);
			if(rl==null)
				distance.put(frame, rl=new TreeMap<String,StatDouble>());
			
			StatDouble foo=distance.get(frame).get(nuc);
			if(foo==null)
				distance.get(frame).put(nuc, foo=new StatDouble());
			foo.count(dist);
			}
		
		public EvDecimal getLifeLen()
			{
			return lifeEnd.subtract(lifeStart);
			}
		
		public EvDecimal toGlobalFrame(EvDecimal frame)
			{
			return frame.add(lifeStart);
			}
		public EvDecimal toLocalFrame(EvDecimal frame)
			{
			return frame.subtract(lifeStart);
			}
		
		
		public EvDecimal interpolTime(EvDecimal start, EvDecimal end, EvDecimal frame)
			{
//			System.out.println(" "+getLifeLen()+" "+start+" "+end+" "+(start+frame*(end-start)/(double)getLifeLen()));
			return start.add(frame.multiply(end.subtract(start).divide(getLifeLen())));
			}
		
		}
		
	
	
	
	/**
	 * Decide lifetimes to use
	 */
	public void deriveLifetime()
		{
		deriveLifetime("AB",nuc.get("AB"));
		deriveLifetime("P1'",nuc.get("P1'"));

/*		for(Map.Entry<String, NucStatsOne> e:nuc.entrySet())
			if(e.getValue().parent==null)
				deriveLifetime(e.getKey(), e.getValue());*/
		}
/*	private boolean hasChildren(String nucname)
		{
		for(NucStatsOne one:nuc.values())
			if(one.parent!=null && one.parent.equals(nucname))
				return true;
		return false;
		}*/
	private void deriveLifetime(String nucname, NucStatsOne n)
		{
		if(n.parent==null)
//			n.lifeStart=0;
			n.lifeStart=new EvDecimal(1000);
		else
			n.lifeStart=/*nuc.get(n.parent).lifeStart+*/nuc.get(n.parent).lifeEnd;
		
		//Set life length arbitrarily if unknown
		//Later: Better to set average of all if none exist
		EvDecimal len=EvDecimal.ZERO;
		if(n.lifetime.isEmpty())
			{
//			len=10;
			len=new EvDecimal(30); //or longer? inf=will not move anymore
			System.out.println("Does not know life length for "+nucname);
			}
		else
			{
			for(EvDecimal l:n.lifetime)
				len=len.add(l);
			len=len.divide(n.lifetime.size());
			}

//		if(nucname.equals("AB") || nucname.equals("P1'"))
//			n.lifeStart=1000;
		
		n.lifeEnd=n.lifeStart.add(len);
		
		if(nucname.equals("P1'"))
			{
			EvDecimal diff=EvDecimal.ZERO;
			for(EvDecimal d:ABPdiff)
				diff=diff.add(d);
			diff=diff.divide(ABPdiff.size());
			System.out.println("AB - P1' diff: "+diff);
			n.lifeEnd=n.lifeEnd.subtract(diff);
			n.lifeStart=n.lifeStart.subtract(diff);
			}
		
		System.out.println("lifetime "+nucname+" "+len+ " from "+n.lifeStart+" "+n.lifeEnd);
		
		for(Map.Entry<String, NucStatsOne> e:nuc.entrySet())
			if(e.getValue().parent!=null && e.getValue().parent.equals(nucname))
				deriveLifetime(e.getKey(), e.getValue());
		}
	
	
	
	/**
	 * Get or create a nuc
	 */
	public NucStatsOne get(String nucname)
		{
		NucStatsOne one=nuc.get(nucname);
		if(one==null)
			nuc.put(nucname,one=new NucStatsOne());
		return one;
		}
	
	
	public Map<String, NucStatsOne> getAtFrame(EvDecimal frame)
		{
		Map<String, NucStatsOne> m=new TreeMap<String, NucStatsOne>();
		for(Entry<String, NucStatsOne> e:nuc.entrySet())
			if(e.getValue().existAt(frame) && e.getValue().distance.get(frame)!=null)
				m.put(e.getKey(), e.getValue());
		return m;
		}
	
	public EvDecimal maxFrame()
		{
		EvDecimal f=EvDecimal.ZERO;
		for(Entry<String, NucStatsOne> e:nuc.entrySet())
			if(e.getValue().lifeEnd.greater(f))
				f=e.getValue().lifeEnd;
		return f;
		}
	
	public EvDecimal minFrame()
		{
		EvDecimal f=null;
		for(Entry<String, NucStatsOne> e:nuc.entrySet())
			if(f==null || e.getValue().lifeStart.less(f))
				f=e.getValue().lifeStart;
		return f;
		}
	

	/**
	 * Set up tree in XML
	 */
	public Lineage generateXMLtree()
		{
		Lineage lin=new Lineage();
		for(Map.Entry<String, NucStatsOne> e:nuc.entrySet())
			lin.getCreateParticle(e.getKey());
		for(Map.Entry<String, NucStatsOne> e:nuc.entrySet())
			if(e.getValue().parent!=null && lin.particle.get(e.getKey()).parents.isEmpty())
				{
				lin.createParentChild(e.getValue().parent, e.getKey());
				}
		return lin;
		}
	
	/**
	 * Initalize coordinates before running iteration for one frame
	 */
	public void prepareCoord(Lineage lin, EvDecimal frame)
		{
		//Inititalize coords
		//Children get coords from parents with some perturbation
		for(Map.Entry<String, NucStatsOne> e:nuc.entrySet())
			{
			if(e.getValue().existAt(frame) && e.getValue().curpos==null)
				{
				e.getValue().curpos=new Vector3d();
				if(e.getValue().parent!=null)
					{
					NucStatsOne parentone=nuc.get(e.getValue().parent);
					if(parentone.curpos!=null)
						e.getValue().curpos=new Vector3d(parentone.curpos);
					}
				double s=1;
				e.getValue().curpos.add(new Vector3d(s*Math.random(),s*Math.random(),s*Math.random()));
				}
			}
		}
	
	/**
	 * Put coords into XML data
	 */
	public void writeCoord(Lineage lin, EvDecimal frame)
		{
		
		//TODO: save even when there is no neighbour?
		
		for(Map.Entry<String, NucStatsOne> e:nuc.entrySet())
			if(e.getValue().curpos!=null && e.getValue().existAt(frame) /*&& !e.getValue().neigh.isEmpty()*/)
				{
				Lineage.Particle nuc=lin.particle.get(e.getKey());
				ParticlePos pos=nuc.getCreatePos(frame);
				
				NucStatsOne one=e.getValue();
				pos.x=one.curpos.x;
				pos.y=one.curpos.y;
				pos.z=one.curpos.z;

//				int relframe=frame-e.getValue().lifeStart;
				StatDouble radiusList=e.getValue().radius.get(frame); //was relframe
				if(radiusList==null)
					{
					if(e.getValue().radius.isEmpty())
						{
						radiusList=new StatDouble();
						radiusList.count(1);
						System.out.println("radius gone missing");
						}
					else
						radiusList=e.getValue().radius.get(e.getValue().radius.lastKey()); //hack, should it even exist this long?
					
					}
				pos.r=radiusList.getMean();
				
				if(one.curposAvg[0].getCount()>1)
					{
					LineageExp expVarR=nuc.getCreateExp("posMeanDevR");
//					double var=one.curposAvg[0].getVar()+one.curposAvg[1].getVar()+one.curposAvg[2].getVar();
					//Cannot add up!?
					expVarR.level.put(frame, Math.sqrt(one.raverror));

//					NucExp expVarX=nuc.getExpCreate("posVarX");
//					expVarX.level.put(frame, Math.sqrt(one.curposAvg[0].getVar()));
					
					
					//Life time
					StatDouble divstat=new StatDouble();
					for(EvDecimal i:one.lifetime)
						divstat.count(i.doubleValue());
					if(divstat.count>1)
						{
						LineageExp expVarDiv=nuc.getCreateExp("divDev");
						expVarDiv.level.put(EvDecimal.ZERO, Math.sqrt(divstat.getVar())/Math.sqrt(divstat.count));
						}
					}
				}
		}
	
	}

