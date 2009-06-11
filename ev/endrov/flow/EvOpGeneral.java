package endrov.flow;

import endrov.imageset.EvChannel;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;

/**
 * General image processing operation. Programmers are normally not meant to implement this
 * class directly. Operations have a natural domain, single images or up to entire channels.
 * There are more convenient subclasses that work with each of these.
 * 
 * @author Johan Henriksson
 *
 */
public interface EvOpGeneral
	{
	public EvPixels[] exec(EvPixels... p);
	public EvPixels exec1(EvPixels... p);
	
	public EvStack[] exec(EvStack... p);
	public EvStack exec1(EvStack... p);
	
	public EvChannel[] exec(EvChannel... ch);
	public EvChannel exec1(EvChannel... ch);
	
	
	public int getNumberChannels();
	
	/**
	 * Takes stacks and pixels. Matches these together, produces a stack
	 */
	/*
	public EvStack execStack(EvObject... p)
		{
		
		
		return makeStackOp(this).exec(p);
		}
		*/
	}

