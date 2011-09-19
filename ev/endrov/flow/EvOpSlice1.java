/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flow;

import endrov.imageset.EvChannel;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.util.ProgressHandle;

/**
 * Slice-by-slice operation, returning a single slice
 * 
 * @author Johan Henriksson
 *
 */
public abstract class EvOpSlice1 extends EvOpGeneral //extends StackOp
	{
	public EvPixels[] exec(ProgressHandle ph, EvPixels... p)
		{
		return new EvPixels[]{exec1(ph,p)};
		}
	
	public EvStack[] exec(ProgressHandle ph, EvStack... p)
		{
		return EvOpSlice.makeStackOpFromSliceOp(this).exec(ph,p);
		}
	
	public EvStack exec1(ProgressHandle ph, EvStack... p)
		{
		return exec(ph,p)[0];
		}
	
	public EvChannel[] exec(ProgressHandle ph, EvChannel... ch)
		{
		
		return EvOpSlice.makeStackOpFromSliceOp(this).exec(ph,ch);
		}
	
	public EvChannel exec1(ProgressHandle ph, EvChannel... ch)
		{
		return exec(ph,ch)[0];
		}
	
	public int getNumberChannels()
		{
		return 1;
		}


	}
