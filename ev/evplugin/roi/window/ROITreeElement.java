package evplugin.roi.window;

import java.util.*;
import javax.swing.tree.*;

import evplugin.data.*;
import evplugin.roi.ROI;


/**
 * Holder of ROIs in a tree
 * @author Johan Henriksson
 */
public class ROITreeElement
	{
	public Object e; //a ROI or EvData
	final public ROITreeElement parent;
	
	public ROITreeElement(ROITreeModel model, Object e, ROITreeElement parent)
		{
		this.e=e;
		this.parent=parent;
		if(model!=null)
			model.allElements.put(e,this);
		}
	
	/**
	 * Get path to this element
	 */
	public TreePath getPath()
		{
		Vector<Object> path=new Vector<Object>();
		ROITreeElement e=this;
		while(e!=null)
			{
			path.add(0,e);
			e=e.parent;
			}
		return new TreePath(path.toArray());
		}

	/**
	 * Get all children ROIs
	 */
	public Vector<ROI> getROIChildren()
		{
		if(e==null)
			return new Vector<ROI>();
		else if(e instanceof ROI)
			return ((ROI)e).getSubRoi();
		else
			{
			Vector<ROI> v=new Vector<ROI>();
			EvData data=(EvData)e;
			for(EvObject ob:data.metaObject.values())
				if(ob instanceof ROI)
					v.add((ROI)ob);
			return v;
			}
		}
	
	/**
	 * Get ROI if it is a ROI, otherwise null
	 */
	public ROI getROI()
		{
		if(e!=null && e instanceof ROI)
			return (ROI)e;
		else
			return null;
		}

	/**
	 * Are there ROIs in this container? should it always return true if it is a container?
	 */
	public boolean isLeaf()
		{
		return getROIChildren().isEmpty();
		}

	/**
	 * Description in tree
	 */
	public String toString()
		{
		if(e==null)
			return "<empty>";
		else if(e instanceof ROI)
			return ((ROI)e).getROIDesc();
		else
			return ((EvData)e).getMetadataName();
		}
	}
