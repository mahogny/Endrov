/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.utilityUnsorted.deconvolution;

import java.util.ArrayList;

import endrov.typeImageset.EvPixels;

/**
 * Just a stack of pixels. No metadata.
 * 
 * @author Johan Henriksson
 *
 */
public class DeconvPixelsStack
	{
	public ArrayList<EvPixels> p=new ArrayList<EvPixels>();
	
	
	public void addSlice(EvPixels slice, int pos)
		{
		p.ensureCapacity(pos+1);
		p.set(pos, slice);
		}
	}
