package endrov.flow;

import endrov.imageset.AnyEvImage;
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
public abstract class EvOpGeneral
	{
	public abstract EvPixels[] exec(EvPixels... p);
	public abstract EvPixels exec1(EvPixels... p);
	
	public abstract EvStack[] exec(EvStack... p);
	public abstract EvStack exec1(EvStack... p);
	
	public abstract EvChannel[] exec(EvChannel... ch);
	public abstract EvChannel exec1(EvChannel... ch);
	
	/*
	public Object exec1Untyped(Object ch)
		{
		if(ch instanceof EvPixels)
			return exec((EvPixels)ch);
		else if(ch instanceof EvStack)
			return exec((EvStack)ch);
		else if(ch instanceof EvChannel)
			return exec((EvChannel)ch);
		else
			throw new RuntimeException("Wrong type for unchecked exec: "+ch.getClass());
		}*/
	
	public AnyEvImage exec1Untyped(Object... ch)
		{
		Object fst=ch[0];
		if(fst instanceof EvPixels)
			return exec1((EvPixels[])ch);
		else if(fst instanceof EvStack)
			return exec1((EvStack[])ch);
		else if(fst instanceof EvChannel)
			return exec1((EvChannel[])ch);
		else
			throw new RuntimeException("Wrong type for unchecked exec: "+ch.getClass());
		}

	public AnyEvImage[] execUntyped(Object... ch)
		{
		Object fst=ch[0];
		if(fst instanceof EvPixels)
			return exec((EvPixels[])ch);
		else if(fst instanceof EvStack)
			return exec((EvStack[])ch);
		else if(fst instanceof EvChannel)
			return exec((EvChannel[])ch);
		else
			throw new RuntimeException("Wrong type for unchecked exec: "+ch.getClass());
		}

	
	public abstract int getNumberChannels();
	
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

