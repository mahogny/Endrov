package util2.makeStdWormDist;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.vecmath.Vector3d;

import evplugin.nuc.NucLineage;

public class NucStats
	{

	public TreeMap<String, NucStatsOne> nuc=new TreeMap<String,NucStatsOne>();
	
	
	
	public class NucStatsOne
		{
		//Stats
		public List<Integer> lifetime=new LinkedList<Integer>();
		public Map<Integer, Map<String,List<Double>>> distance=new TreeMap<Integer, Map<String,List<Double>>>(); //frame rel start, nuc, length
		public Map<Integer, List<Double>> radius=new TreeMap<Integer, List<Double>>();
		
		//Derived
		public int lifeStart;
		public int lifeEnd;
		public String parent;
		public Map<Integer, List<NucLineage.NucPos>> derivedPos=new TreeMap<Integer, List<NucLineage.NucPos>>();
		
		Map<String, Double> avlenthisframe=new HashMap<String, Double>();
		Vector3d curpos=new Vector3d(Math.random(),Math.random(),Math.random());
		
		
		public void calcAvDist(int frame)
			{
			avlenthisframe.clear();
			
			Map<String,List<Double>> thisdistances=distance.get(frame-lifeStart);
			for(String t:thisdistances.keySet())
				{
				double dist=0;
				for(Double d:thisdistances.get(t))
					dist+=d;
				dist/=thisdistances.get(t).size();
				avlenthisframe.put(t, dist);
				}
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
			n.lifeStart=nuc.get(n.parent).lifeStart+nuc.get(n.parent).lifeEnd;
		
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
			if(e.getValue().lifeStart<=frame && e.getValue().lifeEnd>=frame)
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
	
	}
