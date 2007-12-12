package evplugin.roi;

import java.util.*;

/**
 * Compound ROI (Region of interest). This is a ROI that depend on other ROI's ie has children
 * 
 * @author Johan Henriksson
 */
public abstract class CompoundROI extends ROI
	{
	public Vector<ROI> subRoi=new Vector<ROI>();
	
	public Vector<ROI> getSubRoi()
		{
		return subRoi;
		}
	
//function to write/read all children as xml	
	}
