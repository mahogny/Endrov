package endrov.filter;

import endrov.imageset.*;
import endrov.roi.*;

/**
 * Filter that works on ROI level
 * 
 * @author Johan Henriksson
 */
public abstract class FilterROI extends Filter
	{
	public abstract void applyImage(EvImage im, ROI roi, String channel, int frame, int z);
	public abstract void applyImage(EvImage im);
	public abstract void applyImage(EvImage im, LineIterator it);
	}
