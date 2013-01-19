/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowCustomData;

import java.util.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import org.jdom.*;

import endrov.data.basicTypes.EvCustomObject;

/**
 * Tree model showing the XML of a custom meta object
 * @author Johan Henriksson
 */
public class CustomDataTreeModel implements TreeModel
	{
	HashSet<TreeModelListener> listener=new HashSet<TreeModelListener>();
	private EvCustomObject meta=null;

	/**
	 * To keep paths stable, old tree elements corresponding to xml elements should be used. To avoid
	 * memory management, use a weak hashmap to cache entries
	 */
	private WeakHashMap<Element, CustomDataTreeElement> cachedNodes=new WeakHashMap<Element, CustomDataTreeElement>();
	
	/**
	 * Get cached node or create a new node
	 */
	private CustomDataTreeElement getCachedNode(Element e, CustomDataTreeElement parent)
		{
		CustomDataTreeElement el=cachedNodes.get(e);
		if(el==null)
			cachedNodes.put(e,el=new CustomDataTreeElement(e, parent));
		return el;
		}
	
	public void setMetaObject(EvCustomObject o)
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

	
	
	public Object getChild(Object arg0, int childIndex)
		{
		CustomDataTreeElement node=(CustomDataTreeElement)arg0;
		return getCachedNode((Element)node.e.getChildren().get(childIndex), node);
		}

	public int getChildCount(Object arg0)
		{
		CustomDataTreeElement e=(CustomDataTreeElement)arg0;
		return e.e.getChildren().size();
		}

	public int getIndexOfChild(Object arg0, Object arg1)
		{
		if(arg0==null || arg1==null)
			return -1;
		CustomDataTreeElement e=(CustomDataTreeElement)arg0;
		CustomDataTreeElement e2=(CustomDataTreeElement)arg1;
		List<?> list=e.e.getChildren();
		for(int i=0;i<list.size();i++)
			if(list.get(i)==e2)
				return i;
		return -1;
		}

	public Object getRoot()
		{
		if(meta==null)
			return getCachedNode(null,null);
//			return new CustomTreeElement(null,null);
		else
			return getCachedNode(meta.xml,null);
		}

	public boolean isLeaf(Object arg0)
		{
		CustomDataTreeElement e=(CustomDataTreeElement)arg0;
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
	
	public void addChild(CustomDataTreeElement e, Element ne)
		{
		e.e.addContent(ne);
		emitAllChanged();
		}
	
	public void updateElement(CustomDataTreeElement e)
		{
		for(TreeModelListener l:listener)
			l.treeNodesChanged(new TreeModelEvent(this, e.getPath()));
		//emitAllChanged();
		}
	}
