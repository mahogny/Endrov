/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.unsortedImageFilters.unfinished;

import endrov.typeImageset.EvPixels;

public interface oldLUT
	{
	
	/**
	 * What is the purpose of defining a LUT? flows works as well. what the user might want is a list of LUT-like operations in
	 * one place, for example a premade list of colorings. 
	 */
	
	/*
	public class LUTInput
		{
		EvPixels p;
		int start, length;
		
		public LUTInput()
		
		}
	*/
	
	public EvPixels[] applyLUT(EvPixels... p);
	}
