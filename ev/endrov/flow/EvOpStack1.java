/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flow;

import endrov.imageset.EvChannel;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;

/**
 * Image operation defined by operation on stacks
 * @author Johan Henriksson
 *
 */
public abstract class EvOpStack1 extends EvOpGeneral
	{
	//By necessity, stack operators have to deal with laziness manually.
	//Example: avgZ only computes one slice and then duplicates it. other operands compute entire
	//stack. cannot fit together. possible to make functions beneath this.
	//public abstract EvStack[] exec(EvStack... p);
	
	public EvPixels exec1(EvPixels... p)
		{
		//TODO only one pixel supported
		//TODO where is lazyness? where is events?
		EvImage im=new EvImage();
		im.setPixelsReference(p[0]);
		EvStack stack=new EvStack();
		stack.putInt(0, im);
		stack=exec1(stack);
		return stack.getInt(0).getPixels();
		}
	
	public EvPixels[] exec(EvPixels... p)
		{
		return new EvPixels[]{exec1(p)};
		}
	
	public EvChannel[] exec(EvChannel... ch)
		{
		return EvOpStack.applyStackOp(ch, this);
		}
	


	public EvStack[] exec(EvStack... p)
		{
		return new EvStack[]{exec1(p)};
		}
	
	public abstract EvStack exec1(EvStack... p);
	
	public EvChannel exec1(EvChannel... ch)
		{
		return exec(ch)[0];
		}

	
	public int getNumberChannels()
		{
		return 1;
		}
	}