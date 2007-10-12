package evplugin.filter;

import evplugin.imageset.*;
import evplugin.roi.*;


public class InvertFilter extends FilterROI
	{
	
	
//highest level. should we have a standard implementation here?	
	public void applyImage(EvImage im, ROI roi)
		{
		
		}

//on entire image. could have a standard implementation of this one too.	
	public void applyImage(EvImage im)
		{
		//copy original
		//call above with ROI covering everything
		//mix original and filtered
		//return
		}

	
	public void applyLine(EvImage im, LineIterator it)
		{
		
		}
	
	
	}
