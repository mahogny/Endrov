package endrov.recording;

import java.util.HashMap;
import java.util.LinkedList;

import endrov.imageset.EvImage;

/**
 * The high-level recording system is split into Recording methods and Recorders.
 * A recording method generates or transforms recording primitives which is what
 * you want to record. A recorder takes primitives and runs the hardware.
 *
 * A recording method generates primitives as a sequence
 * 
 * A recording method can be purely transforming. It will take the output from
 * other methods and combine them somehow, adding additional tags
 * 
 * The recording method will look at the tags and control the hardware correspondingly
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class PlannedRecPrimitive
	{
	//Standardized tag names
	public final static String CHANNEL="CHANNEL"; // String
	public final static String IMAGESET_NAME="IMAGESET_NAME";  // String
	public final static String FRAME="CHANNEL"; //EvDecimal
	public final static String Z="Z"; //EvDecimal
	public final static String EXPOSURETIME="EXPOSURETIME"; //EvDecimal
	public final static String WAITUNTIL="WAITUNTIL"; //EvDecimal, unix time. 

	/**
	 * A listener to be informed upon completion of this primitive
	 */
	public interface PlannedRecPrimitiveListener
		{
		public void done(PlannedRecPrimitive p, EvImage im);
		}
	
	/**
	 * Tags can hold any data but their names and types should be standardized to extend
	 * method interoperability
	 */
	public HashMap<String, Object> tags=new HashMap<String, Object>();
	
	private LinkedList<PlannedRecPrimitiveListener> listeners=new LinkedList<PlannedRecPrimitiveListener>();
	
	public void addListener(PlannedRecPrimitiveListener l)
		{
		listeners.add(l);
		}

	/**
	 * Called when this primitive has been completed
	 */
	public void done(EvImage im)
		{
		for(PlannedRecPrimitiveListener l:listeners)
			l.done(this, im);
		}
	
	
	}
