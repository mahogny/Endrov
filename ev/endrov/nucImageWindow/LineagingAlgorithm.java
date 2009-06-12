package endrov.nucImageWindow;

import endrov.imageset.EvChannel;
import endrov.nuc.NucLineage;
import endrov.util.EvDecimal;

/**
 * General interface to automatic lineaging algorithm. 
 * 
 * @author Johan Henriksson
 *
 */
public interface LineagingAlgorithm
	{
	/**
	 * Called by algorithm for user feedback
	 * @author Johan Henriksson
	 */
	public interface LineageListener
		{
		public void nowAtFrame(EvDecimal f);
		}
	
	/**
	 * Set up the algorithm to work with given channel and lineage object. Only called once.
	 */
	public void init(EvChannel chan, NucLineage lin);
	
	/**
	 * User-enforced stop of lineaging algorithm thread
	 */
	public void stop();
	
	/**
	 * Run algorithm, starting at given frame
	 */
	public void run(EvDecimal startFrame, LineageListener listener);
	
	
	}
