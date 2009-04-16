package endrov.imageset;

import java.util.TreeMap;

import endrov.util.EvDecimal;

/**
 * One stack of images
 * @author Johan Henriksson
 *
 */
public class EvStack
	{
	TreeMap<EvDecimal, EvImage> loaders=new TreeMap<EvDecimal, EvImage>();
	
	
	//TODO lazy generation of the stack
	
	public EvStack(){}
	
	
	//Temp, can be removed later
	public EvStack(TreeMap<EvDecimal, EvImage> l)
		{
		loaders.putAll(l);
		}
	
	
	public EvImage get(EvDecimal frame)
		{
		return loaders.get(frame);
		}
	
	public void put(EvDecimal frame, EvImage im)
		{
		loaders.put(frame,im);
		}

	
	
	

	}
