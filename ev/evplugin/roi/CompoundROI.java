package evplugin.roi;

import java.util.*;

import evplugin.basicWindow.BasicWindow;
import evplugin.data.EvData;
import evplugin.data.EvObject;

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

	/**
	 * Take selected ROIs and push them all into a compound ROI
	 */
	public static void makeCompoundROI(EvData data, CompoundROI croi)
		{
		Set<ROI> rois=CompoundROI.collectRecursiveROI(data);
		data.addMetaObject(croi);
		for(ROI roi:rois)
			croi.subRoi.add(roi);
		BasicWindow.updateWindows();
		}
	

	/**
	 * Remove all selected ROIs recursively from their parents and return them
	 */
	private static Set<ROI> collectRecursiveROI(Object parent)
		{
		HashSet<ROI> hs=new HashSet<ROI>();
		if(parent instanceof EvData)
			{
			EvData data=(EvData)parent;
			Set<Integer> toremove=new HashSet<Integer>();
			for(int key:data.metaObject.keySet())
				{
				EvObject ob=data.metaObject.get(key);
				if(ob instanceof ROI)
					{
					if(ROI.isSelected((ROI)ob))
						{
						toremove.add(key);
						hs.add((ROI)ob);
						}
					else
						hs.addAll(collectRecursiveROI(ob));
					}
				}
			for(int key:toremove)
				data.metaObject.remove(key);
			}
		else if(parent instanceof CompoundROI)
			{
			Set<ROI> toremove=new HashSet<ROI>();
			for(ROI roi:((CompoundROI)parent).subRoi)
				{
				if(ROI.isSelected(roi))
					{
					toremove.add(roi);
					hs.add((ROI)roi);
					}
				else
					hs.addAll(collectRecursiveROI(roi));
				}
			((CompoundROI)parent).subRoi.removeAll(toremove);
			}
		return hs;
		}
	
	
//function to write/read all children as xml	
	}
