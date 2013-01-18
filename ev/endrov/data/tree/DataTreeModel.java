/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.data.tree;

import java.util.LinkedList;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;


/**
 * Model for tree of all objects
 * 
 * @author mahogny
 *
 */
public class DataTreeModel implements TreeModel
	{
	LinkedList<TreeModelListener> listener=new LinkedList<TreeModelListener>(); 
	
	//needed?
	//public WeakHashMap<Object, ROITreeElement> allElements=new WeakHashMap<Object, ROITreeElement>(); 
	
	private DataTreeElement root;
	
	public DataTreeModel(boolean canCreate)
		{
		root=new DataTreeElement(canCreate);
		}
	
	public void addTreeModelListener(TreeModelListener l)
		{
		listener.add(l);
		}

	public Object getChild(Object parent, int index)
		{
		DataTreeElement eparent=(DataTreeElement)parent;
		return eparent.getChild(index);
		}

	public int getChildCount(Object parent)
		{
		DataTreeElement eparent=(DataTreeElement)parent;
		if(eparent.isRoot)
			return EvData.openedData.size();
		else
			{
			return eparent.getChildCount();
			}
		}

	
	public int getIndexOfChild(Object parent, Object child)
		{
		DataTreeElement eparent=(DataTreeElement)parent;
		
		if(eparent.isRoot)
			{
			return EvData.openedData.indexOf(child);
			}
		else
			{
			EvContainer c=eparent.getLeaf();
			int i=0;
			for(EvObject o:c.metaObject.values())
				{
				if(o==child)
					return i;
				i++;
				}
			return i;
//			return -1;
			}
		}

	public Object getRoot()
		{
		return root;
		}

	public boolean isLeaf(Object node)
		{
		DataTreeElement eparent=(DataTreeElement)node;
		if(eparent.isRoot)
			return false;
		else
			return eparent.getChildCount()==0;
		}

	public void removeTreeModelListener(TreeModelListener l)
		{
		listener.remove(l);
		}

	public void valueForPathChanged(TreePath path, Object newValue)
		{
		}
	
	public void dataUpdated()
		{
		for(TreeModelListener list:listener)
			list.treeStructureChanged(new TreeModelEvent(this,new Object[]{getRoot()}));
		}

	}
