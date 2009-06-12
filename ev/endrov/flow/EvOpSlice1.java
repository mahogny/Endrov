package endrov.flow;

import endrov.imageset.EvChannel;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;

/**
 * Slice-by-slice operation, returning a single slice
 * 
 * @author Johan Henriksson
 *
 */
public abstract class EvOpSlice1 extends EvOpGeneral //extends StackOp
	{
	//Could have multiple output
	//EvPixels or EvImage?
//	public abstract EvPixels[] exec(EvPixels... p);
	

//	public abstract EvPixels exec1(EvPixels... p); //Override can be used on this, but not the interface


	public EvPixels[] exec(EvPixels... p)
		{
		return new EvPixels[]{exec1(p)};
		}
	
	public EvStack[] exec(EvStack... p)
		{
		return EvOpSlice.makeStackOp(this).exec(p);
		}
	
	public EvStack exec1(EvStack... p)
		{
		return exec(p)[0];
		}
	
	public EvChannel[] exec(EvChannel... ch)
		{
		return EvOpSlice.makeStackOp(this).exec(ch);
		}
	
	public EvChannel exec1(EvChannel... ch)
		{
		return exec(ch)[0];
		}
	
	public int getNumberChannels()
		{
		return 1;
		}


	}
