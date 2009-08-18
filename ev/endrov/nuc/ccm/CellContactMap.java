/**
 * 
 */
package endrov.nuc.ccm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import endrov.nuc.NucLineage;
import endrov.nuc.NucSel;
import endrov.nuc.NucVoronoi;
import endrov.util.EvDecimal;
import endrov.util.Tuple;


/**
 * 
 * @author Johan Henriksson
 *
 */
public class CellContactMap
	{
	public Map<EvDecimal,NucVoronoi> fcontacts=new HashMap<EvDecimal, NucVoronoi>();
	public NucLineage lin;
	public String name;
	public int numid;
	//nuc -> nuc -> frames
	public Map<String,Map<String,SortedSet<EvDecimal>>> contactsf=new TreeMap<String, Map<String,SortedSet<EvDecimal>>>();
	//nuc -> lifetime
	public Map<String,Integer> lifelen=new HashMap<String,Integer>();
	
	public EvDecimal frameInc=new EvDecimal(30);
	//20 steps=150s!
	
	public TreeSet<EvDecimal> framesTested=new TreeSet<EvDecimal>();
	
	
	public CellContactMap(NucLineage lin, String name, TreeSet<String> nucNames)
		{
		this.lin=lin; //Should not be needed
		this.name=name;
		calcneigh(nucNames);
		}
	
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
	
	
	/**
	 * Main function to do calculations
	 */
	private void calcneigh(TreeSet<String> nucNames)
		{
		//Prepare different indexing
		for(String n:nucNames)
			{
			Map<String,SortedSet<EvDecimal>> u=new HashMap<String, SortedSet<EvDecimal>>();
			for(String m:nucNames)
				u.put(m,new TreeSet<EvDecimal>());
			contactsf.put(n, u);
			}
	
		//Prepare life length
		for(String n:nucNames)
			lifelen.put(n, 0);
		
		PrintWriter pw=null;
		try
			{
			if(name.equals("celegans2008.2"))
				pw=new PrintWriter(new FileWriter(new File("/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost/data/numneigh.txt")));
			}
		catch (IOException e1)
			{
			System.out.println("failed to open neigh count output");
			System.exit(1);
			}
		
		
		//Go through all frames
		int numframes=0;
		for(EvDecimal curframe=lin.firstFrameOfLineage().fst();curframe.less(lin.lastFrameOfLineage().fst());curframe=curframe.add(frameInc))
			{
			framesTested.add(curframe);
			numframes++;
	                              				
			//interpolate
			Map<NucSel, NucLineage.NucInterp> inter=lin.getInterpNuc(curframe);
			if(curframe.intValue()%100==0)
				System.out.println(curframe);
			try
				{
				//Eliminate cells not in official list or invisible
				Map<NucSel, NucLineage.NucInterp> interclean=new HashMap<NucSel, NucLineage.NucInterp>();
				int numRealNuc=interclean.size();
				for(Map.Entry<NucSel, NucLineage.NucInterp> e:inter.entrySet())
					if(e.getValue().isVisible() && nucNames.contains(e.getKey().snd()))
						interclean.put(e.getKey(), e.getValue());
				int numcleancell=interclean.size();
				inter=interclean;
				
				//Add false nuclei at distance to make voronoi calc possible
				if(!inter.isEmpty())
					{
					double r=3000; //300 is about the embryo. embryo is not centered in reality.
					
					NucLineage.NucInterp i1=new NucLineage.NucInterp();
					i1.pos=new NucLineage.NucPos();
					i1.frameBefore=EvDecimal.ZERO;
					i1.pos.x=r;
	
					NucLineage.NucInterp i2=new NucLineage.NucInterp();
					i2.pos=new NucLineage.NucPos();
					i2.frameBefore=EvDecimal.ZERO;
					i2.pos.x=-r;
	
					NucLineage.NucInterp i3=new NucLineage.NucInterp();
					i3.pos=new NucLineage.NucPos();
					i3.frameBefore=EvDecimal.ZERO;
					i3.pos.y=-r;
	
					NucLineage.NucInterp i4=new NucLineage.NucInterp();
					i4.pos=new NucLineage.NucPos();
					i4.frameBefore=EvDecimal.ZERO;
					i4.pos.y=-r;
	
					inter.put(new NucSel(null,":::1"), i1);
					inter.put(new NucSel(null,":::2"), i2);
					inter.put(new NucSel(null,":::3"), i3);
					inter.put(new NucSel(null,":::4"), i4);
					}
			
				//Get neighbours
				NucVoronoi nvor=new NucVoronoi(inter,true);
				fcontacts.put(curframe, nvor);
				//TODO if parent neigh at this frame, remove child?
				
				//Turn into more suitable index ordering for later use
				for(Tuple<String, String> e:nvor.getNeighPairSet())
					addFrame(e.fst(),e.snd(),curframe);
				//Calculate lifelen
				for(Map.Entry<NucSel, NucLineage.NucInterp> e:inter.entrySet())
					addLifelen(e.getKey().snd());
				
				//Count neigh
				if(pw!=null)
					{
					int numContact=nvor.getNeighPairSetIndex().size()-numRealNuc;
					pw.println(""+curframe+"\t"+numContact+"\t"+numcleancell);
					}
				
				}
			catch (Exception e)
				{
				}
			}
		if(pw!=null)
			pw.close();
		}
	
	
	
	
	}