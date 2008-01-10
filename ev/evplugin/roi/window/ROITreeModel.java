package evplugin.roi.window;

import java.util.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import evplugin.data.*;
//import evplugin.roi.ROI;

/**
 * Tree model showing the XML of a custom meta object
 * @author Johan Henriksson
 */
public class ROITreeModel implements TreeModel
	{
	HashSet<TreeModelListener> listener=new HashSet<TreeModelListener>();
	private EvData meta=null;

	public void setMetaObject(EvData o)
		{
		meta=o;
		emitAllChanged();
		}
	
	public void addTreeModelListener(TreeModelListener arg0)
		{
		listener.add(arg0);
		}
	public void removeTreeModelListener(TreeModelListener arg0)
		{
		listener.remove(arg0);
		}

	
	
	public Object getChild(Object arg0, int arg1)
		{
		ROITreeElement e=(ROITreeElement)arg0;
		return new ROITreeElement(e.getChildren().get(arg1), e);
		}

	public int getChildCount(Object arg0)
		{
		ROITreeElement e=(ROITreeElement)arg0;
		return e.getChildren().size();
		}

	public int getIndexOfChild(Object arg0, Object arg1)
		{
		if(arg0==null || arg1==null)
			return -1;
		ROITreeElement e=(ROITreeElement)arg0;
		ROITreeElement e2=(ROITreeElement)arg1;
		List<?> list=e.getChildren();
		for(int i=0;i<list.size();i++)
			if(list.get(i)==e2)
				return i;
		return -1;
		}

	public Object getRoot()
		{
		if(meta==null)
			return new ROITreeElement(null,null);
		else
			return new ROITreeElement(meta, null);
		}

	public boolean isLeaf(Object arg0)
		{
		ROITreeElement e=(ROITreeElement)arg0;
		return e.isLeaf();
		}


	public void valueForPathChanged(TreePath arg0, Object arg1)
		{
		//When item changed. Not supported.
		}

	
	
	public void emitAllChanged()
		{
		for(TreeModelListener l:listener)
			l.treeStructureChanged(new TreeModelEvent(this, new Object[]{getRoot()}));
		}
	
	/*
	public void addChild(ROITreeElement e, ROI ne)
		{
//TODO
		//		e.e.addContent(ne);
		emitAllChanged();
		}*/
	
	public void updateElement(ROITreeElement e)
		{
		for(TreeModelListener l:listener)
			l.treeNodesChanged(new TreeModelEvent(this, e.getPath()));
		
		emitAllChanged();
		}
	}
