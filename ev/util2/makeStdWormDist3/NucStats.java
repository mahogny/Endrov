package util2.makeStdWormDist3;

import java.util.*;
import java.util.Map.Entry;
import javax.vecmath.Vector3d;
import evplugin.nuc.NucLineage;
import evplugin.nuc.NucLineage.NucExp;
import evplugin.nuc.NucLineage.NucPos;



public class NucStats
	{

	public TreeMap<String, NucStatsOne> nuc=new TreeMap<String,NucStatsOne>();
	
	public List<Integer> ABPdiff=new LinkedList<Integer>();

	
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
		public List<Integer> lifetime=new LinkedList<Integer>();
		public SortedMap<Integer, Map<String,StatDouble>> distance=new TreeMap<Integer, Map<String,StatDouble>>(); //frame rel start, nuc, length
		public SortedMap<Integer, StatDouble> radius=new TreeMap<Integer, StatDouble>();
		
		public SortedMap<Integer, List<Vector3d>> collectedPos=new TreeMap<Integer, List<Vector3d>>();
		
		//Derived
		public int lifeStart;
		public int lifeEnd;
		public String parent;
		public Map<Integer, List<NucLineage.NucPos>> derivedPos=new TreeMap<Integer, List<NucLineage.NucPos>>();
		
		//Used in BestFitLength
		public Vector3d curpos=null;
		public Vector3d df=new Vector3d();
		public TreeSet<Neigh> neigh=new TreeSet<Neigh>();
		public StatDouble[] curposAvg=new StatDouble[]{new StatDouble(),new StatDouble(),new StatDouble()};
		
		public boolean existAt(int frame)
			{
			return lifeStart<=frame && frame<lifeEnd;
			}
		
		
		/**
		 * Decide on average distances, weights and which neighbours to consider
		 */
		public void findNeigh(int frame)
			{
			Map<String,StatDouble> thisdistances=distance.get(frame-lifeStart);
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

		
		
		
		
		public void addRadius(int frame, double rs)
			{
			StatDouble rl=radius.get(frame);
			if(rl==null)
				radius.put(frame, rl=new StatDouble());
			rl.count(rs);
			}
		public void addCollPos(int frame, Vector3d pos)
			{
			List<Vector3d> rl=collectedPos.get(frame);
			if(rl==null)
				collectedPos.put(frame, rl=new LinkedList<Vector3d>());
			rl.add(pos);
			}
		
		public void addDistance(int frame, String nuc, double dist)
			{
			Map<String,StatDouble> rl=distance.get(frame);
			if(rl==null)
				distance.put(frame, rl=new TreeMap<String,StatDouble>());
			
			StatDouble foo=distance.get(frame).get(nuc);
			if(foo==null)
				distance.get(frame).put(nuc, foo=new StatDouble());
			foo.count(dist);
			}
		
		public int getLifeLen()
			{
			return lifeEnd-lifeStart;
			}
		
		public int toGlobalFrame(int frame)
			{
			return frame+lifeStart;
			}
		public int toLocalFrame(int frame)
			{
			return frame-lifeStart;
			}
		
		
		public double interpolTime(int start, int end, int frame)
			{
//			System.out.println(" "+getLifeLen()+" "+start+" "+end+" "+(start+frame*(end-start)/(double)getLifeLen()));
			return start+frame*(end-start)/(double)getLifeLen();
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
			n.lifeStart=1000;
		else
			n.lifeStart=/*nuc.get(n.parent).lifeStart+*/nuc.get(n.parent).lifeEnd;
		
		//Set life length arbitrarily if unknown
		//Later: Better to set average of all if none exist
		int len=0;
		if(n.lifetime.isEmpty())
			{
//			len=10;
			len=30; //or longer? inf=will not move anymore
			System.out.println("Does not know life length for "+nucname);
			}
		else
			{
			for(int l:n.lifetime)
				len+=l;
			len/=n.lifetime.size();
			}

//		if(nucname.equals("AB") || nucname.equals("P1'"))
//			n.lifeStart=1000;
		
		n.lifeEnd=n.lifeStart+len;
		
		if(nucname.equals("P1'"))
			{
			double diff=0;
			for(int d:ABPdiff)
				diff+=d;
			diff/=ABPdiff.size();
			System.out.println("AB - P1' diff: "+diff);
			n.lifeEnd-=diff;
			n.lifeStart-=diff;
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
	
	
	public Map<String, NucStatsOne> getAtFrame(int frame)
		{
		Map<String, NucStatsOne> m=new TreeMap<String, NucStatsOne>();
		for(Entry<String, NucStatsOne> e:nuc.entrySet())
			if(e.getValue().existAt(frame) && e.getValue().distance.get(frame)!=null)
				m.put(e.getKey(), e.getValue());
		return m;
		}
	
	public int maxFrame()
		{
		int f=0;
		for(Entry<String, NucStatsOne> e:nuc.entrySet())
			if(e.getValue().lifeEnd>f)
				f=e.getValue().lifeEnd;
		return f;
		}
	
	public int minFrame()
		{
		Integer f=null;
		for(Entry<String, NucStatsOne> e:nuc.entrySet())
			if(f==null || e.getValue().lifeStart<f)
				f=e.getValue().lifeStart;
		return f;
		}
	

	/**
	 * Set up tree in XML
	 */
	public NucLineage generateXMLtree()
		{
		NucLineage lin=new NucLineage();
		for(Map.Entry<String, NucStatsOne> e:nuc.entrySet())
			lin.getNucCreate(e.getKey());
		for(Map.Entry<String, NucStatsOne> e:nuc.entrySet())
			if(e.getValue().parent!=null && lin.nuc.get(e.getKey()).parent==null)
				{
//				System.out.println("PC: "+e.getValue().parent+" -> "+e.getKey());
				lin.createParentChild(e.getValue().parent, e.getKey());
				}
		return lin;
		}
	
	/**
	 * Initalize coordinates before running iteration for one frame
	 */
	public void prepareCoord(NucLineage lin, int frame)
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
	public void writeCoord(NucLineage lin, int frame)
		{
		
		//TODO: save even when there is no neighbour?
		
		for(Map.Entry<String, NucStatsOne> e:nuc.entrySet())
			if(e.getValue().curpos!=null && e.getValue().existAt(frame) /*&& !e.getValue().neigh.isEmpty()*/)
				{
				NucLineage.Nuc nuc=lin.nuc.get(e.getKey());
				NucPos pos=nuc.getPosCreate(frame);
				
				NucStatsOne one=e.getValue();
				pos.x=one.curpos.x;
				pos.y=one.curpos.y;
				pos.z=one.curpos.z;

				int relframe=frame-e.getValue().lifeStart;
				StatDouble radiusList=e.getValue().radius.get(relframe);
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
					NucExp expVar=nuc.getExpCreate("posvar");
					double var=one.curposAvg[0].getVar()+one.curposAvg[1].getVar()+one.curposAvg[2].getVar();
					expVar.level.put(frame, var);
					}
				}
		}
	
	}

