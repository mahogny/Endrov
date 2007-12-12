package evplugin.roi.window;

import javax.swing.tree.*;


import evplugin.data.*;
import evplugin.roi.ROI;

import java.util.*;

/**
 * Holder of an XML-node in the tree
 * @author Johan Henriksson
 */
public class ROITreeElement
	{
	public Object e; //ROI or EvData
	final public ROITreeElement parent;
	
	public ROITreeElement(Object e, ROITreeElement parent)
		{
		this.e=e;
		this.parent=parent;
		}
	
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

	
	public Vector<ROI> getChildren()
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
	
	public boolean isLeaf()
		{
		return getChildren().isEmpty();
		}
	
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
