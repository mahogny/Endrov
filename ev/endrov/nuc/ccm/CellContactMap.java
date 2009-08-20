/**
 * 
 */
package endrov.nuc.ccm;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import endrov.nuc.NucVoronoi;
import endrov.util.EvDecimal;


/**
 * 
 * @author Johan Henriksson
 *
 */
public class CellContactMap
	{
	//TODO must replace
	public Map<EvDecimal,NucVoronoi> fcontacts=new HashMap<EvDecimal, NucVoronoi>();
	//public NucLineage lin;
	//public String name;
	
	//nuc -> nuc -> frames
	public Map<String,Map<String,SortedSet<EvDecimal>>> contactsf=new TreeMap<String, Map<String,SortedSet<EvDecimal>>>();
	//nuc -> lifetime
	public Map<String,Integer> lifelen=new HashMap<String,Integer>();
	
	public TreeSet<EvDecimal> framesTested=new TreeSet<EvDecimal>();
	
	public TreeSet<String> allNuc=new TreeSet<String>();
	
	//TODO fill
	public Map<String, EvDecimal> firstFrame=new HashMap<String, EvDecimal>();
	public Map<String, EvDecimal> lastFrame=new HashMap<String, EvDecimal>();
	
//	EvDecimal firstFrame=thisNuc.getFirstFrame();
//	EvDecimal lastFrame=thisNuc.getLastFrame();

	/**
	 * Add to life length
	 */
	public void addLifelen(String a)
		{
		Integer len=lifelen.get(a);
		if(len==null)
			len=0;
		len++;
		lifelen.put(a,len);
		}
	
	
	/**
	 * Add frame with contact a <-> b
	 * @param a
	 * @param b
	 * @param f
	 */
	public void addFrame(String a, String b, EvDecimal f)
		{
		addFrame1(a, b, f);
		addFrame1(b, a, f);
		}
	
	/**
	 * Add frame with contact a -> b(?)
	 */
	private void addFrame1(String a, String b, EvDecimal f)
		{
		Map<String,SortedSet<EvDecimal>> na=contactsf.get(a);
		if(na==null)
			contactsf.put(a,na=new TreeMap<String,SortedSet<EvDecimal>>());
		SortedSet<EvDecimal> sa=na.get(b);
		if(sa==null)
			na.put(b, sa=new TreeSet<EvDecimal>());
		sa.add(f);
		}
	

	
	
	
	
	
	}