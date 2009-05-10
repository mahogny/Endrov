package endrov.unsortedImageFilters.unfinished;

import endrov.imageset.EvPixels;

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
