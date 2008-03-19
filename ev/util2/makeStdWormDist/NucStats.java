package util2.makeStdWormDist;

import java.util.*;
import java.util.Map.Entry;
import javax.vecmath.Vector3d;
import evplugin.nuc.NucLineage;
import evplugin.nuc.NucLineage.NucPos;



public class NucStats
	{

	public TreeMap<String, NucStatsOne> nuc=new TreeMap<String,NucStatsOne>();
	
	/**
	 * Neighbour
	 */
	public class Neigh implements Comparable
		{
		public String name;
		public double weight;
		public double dist;
		public double distVar;
		
		public int compareTo(Object o)
			{
			Neigh other=(Neigh)o;
			if(dist<other.dist)				return -1;
			else if(dist>other.dist)	return 1;
			else return 0;
			}
		}

	
	
	/**
	 * Stats about one nuc
	 */
	public class NucStatsOne
		{
		//Stats
		public List<Integer> lifetime=new LinkedList<Integer>();
		public SortedMap<Integer, Map<String,List<Double>>> distance=new TreeMap<Integer, Map<String,List<Double>>>(); //frame rel start, nuc, length
		public SortedMap<Integer, List<Double>> radius=new TreeMap<Integer, List<Double>>();
		
		//Derived
		public int lifeStart;
		public int lifeEnd;
		public String parent;
		public Map<Integer, List<NucLineage.NucPos>> derivedPos=new TreeMap<Integer, List<NucLineage.NucPos>>();
		
		//Used in BestFitLength
		public Vector3d curpos=null;
		public Vector3d df=new Vector3d();
		public TreeSet<Neigh> neigh=new TreeSet<Neigh>();
		
		
		public boolean existAt(int frame)
			{
			return lifeStart<=frame && frame<lifeEnd;
			}
		
		
		/**
		 * Decide on average distances, weights and which neighbours to consider
		 */
		public void findNeigh(int frame)
			{
			Map<String,List<Double>> thisdistances=distance.get(frame-lifeStart);
			neigh.clear();
			if(thisdistances==null)
				System.out.println("<<<<no distances!!!>>>>");
			else
				for(String t:thisdistances.keySet())
					if(nuc.containsKey(t) && nuc.get(t).existAt(frame))
						{
						double meanDist=0;
						double y2sum=0;
						int sampleSize=thisdistances.get(t).size();
						for(Double d:thisdistances.get(t))
							{
							meanDist+=d;
							y2sum+=d*d;
							}
						meanDist/=sampleSize;
						
						double s2;
						if(sampleSize>1)
							s2=y2sum/(sampleSize-1.0) - meanDist*meanDist*sampleSize/(sampleSize-1.0);
						else
							s2=10; //what about those very unknown ones? loss of information?
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
			List<Double> rl=radius.get(frame);
			if(rl==null)
				radius.put(frame, rl=new LinkedList<Double>());
			rl.add(rs);
			}
		
		public void addDistance(int frame, String nuc, double dist)
			{
			Map<String,List<Double>> rl=distance.get(frame);
			if(rl==null)
				distance.put(frame, rl=new TreeMap<String,List<Double>>());
			
			List<Double> foo=distance.get(frame).get(nuc);
			if(foo==null)
				distance.get(frame).put(nuc, foo=new LinkedList<Double>());
			foo.add(dist);
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
		for(Map.Entry<String, NucStatsOne> e:nuc.entrySet())
			if(e.getValue().parent==null)
				deriveLifetime(e.getKey(), e.getValue());
		}
	private void deriveLifetime(String nucname, NucStatsOne n)
		{
		if(n.parent==null)
			n.lifeStart=0;
		else
			n.lifeStart=/*nuc.get(n.parent).lifeStart+*/nuc.get(n.parent).lifeEnd;
		
		int len=0;
		for(int l:n.lifetime)
			len+=l;
		len/=n.lifetime.size();
		n.lifeEnd=n.lifeStart+len;
		
		System.out.println("lifetime "+nucname+" "+len+ "   from "+n.lifeStart+" "+n.lifeEnd);
		
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
			if(e.getValue().parent!=null)
				lin.createParentChild(e.getValue().parent, e.getKey());
		return lin;
		}
	
	/**
	 * Initalize coordinates before running iteration for one frame
	 */
	public void prepareCoord(NucLineage lin, int frame)
		{
		//Inititalize coords
		for(Map.Entry<String, NucStatsOne> e:nuc.entrySet())
			{
			if(e.getValue().existAt(frame) && e.getValue().curpos==null /*&& !e.getValue().neigh.isEmpty()*/)
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
			//TODO: get coord from parent
			}
		}
	
	/**
	 * Put coords into XML data
	 */
	public void writeCoord(NucLineage lin, int frame)
		{
		for(Map.Entry<String, NucStatsOne> e:nuc.entrySet())
			if(e.getValue().curpos!=null && e.getValue().existAt(frame) && !e.getValue().neigh.isEmpty())
				{
				NucLineage.Nuc nuc=lin.nuc.get(e.getKey());
				NucPos pos=nuc.getPosCreate(frame);
				
				pos.x=e.getValue().curpos.x;
				pos.y=e.getValue().curpos.y;
				pos.z=e.getValue().curpos.z;

				int relframe=frame-e.getValue().lifeStart;
				double radius=0;
				List<Double> radiusList=e.getValue().radius.get(relframe);
				if(radiusList==null)
					radiusList=e.getValue().radius.get(e.getValue().radius.lastKey()); //hack, should it even exist this long?
//				System.out.println("relframe "+relframe +" "+e.getValue().radius.lastKey());
				for(double d:radiusList)
					radius+=d;
				radius/=radiusList.size();
				pos.r=radius;
				}
		}
	
	}

