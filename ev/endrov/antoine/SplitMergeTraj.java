package endrov.antoine;

import java.util.HashSet;
import java.util.Set;

import endrov.util.EvDecimal;

/**
 * Trajectories of particles that can both split and merge.
 * Time must strictly increase or decrease within trajectory.
 * 
 * it should never be possible to get to one point going both backwards and forwards
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class SplitMergeTraj
	{

	/**
	 * Gain of this vs nuclineage allowing multiple parents?
	 * 
	 *  how will user work with it? 
	 *  
	 *  complementary data, radius, expression?
	 *  
	 *  nuc starts existing at first point. valid here too.
	 *  nuc stops existing at first child start of existence. valid here too.
	 *  {lie. what takes priority at interval between parent and child? unclear. parent I think. should maybe be none?}
	 *  
	 *  how would a nucview be rendered? must be more compact.
	 *  
	 *  nuclin data would not change. just internal classes.
	 *  
	 *  new 3d render option, render all trajectories, also past?
	 *  
	 *  public String parent=null; becomes a treeset instead
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  
	 *  layout for new nuclin linrenderer:
	 *    * the tree concept would die with multiple parents
	 *    * optimize for trees anyway
	 *    * start allocating leafs
	 *    * BFS sort of, why not sort by end-time and go backwards allocating position?
	 *    * get rid of camera+-, do that during rendering? speeds up
	 *    
	 *  
	 *  
	 *  
	 */
	
	
	
	
	
	
	public static class TPoint
		{
		EvDecimal time,x,y,z;
		
		
		public void remove()
			{
			
			}
		
		public void insertFront(TPoint p)
			{
			
			}
		
		public Set<TPoint> next=new HashSet<TPoint>();
		public Set<TPoint> last=new HashSet<TPoint>();
		}
	
	
	}
