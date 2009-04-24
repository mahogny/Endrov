package endrov.filter;

import endrov.imageset.*;
import endrov.roi.*;
import endrov.util.EvDecimal;

/**
 * Filter that works on ROI level
 * 
 * @author Johan Henriksson
 */
public abstract class FilterROI extends Filter
	{
	public abstract void applyImage(EvStack stack, EvImage im, ROI roi, String channel, EvDecimal frame, EvDecimal z);
	public abstract void applyImage(EvStack stack, EvImage im);
	public abstract void applyImage(EvStack stack, EvImage im, LineIterator it);
	}
