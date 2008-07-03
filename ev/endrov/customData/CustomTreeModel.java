package endrov.customData;

import java.util.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import org.jdom.*;

import endrov.data.*;

/**
 * Tree model showing the XML of a custom meta object
 * @author Johan Henriksson
 */
public class CustomTreeModel implements TreeModel
	{
	HashSet<TreeModelListener> listener=new HashSet<TreeModelListener>();
	private CustomObject meta=null;

	
	
	
	public void setMetaObject(CustomObject o)
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
		CustomTreeElement e=(CustomTreeElement)arg0;
		return new CustomTreeElement((Element)e.e.getChildren().get(arg1), e);
		}

	public int getChildCount(Object arg0)
		{
		CustomTreeElement e=(CustomTreeElement)arg0;
		return e.e.getChildren().size();
		}

	public int getIndexOfChild(Object arg0, Object arg1)
		{
		if(arg0==null || arg1==null)
			return -1;
		CustomTreeElement e=(CustomTreeElement)arg0;
		CustomTreeElement e2=(CustomTreeElement)arg1;
		List<?> list=e.e.getChildren();
		for(int i=0;i<list.size();i++)
			if(list.get(i)==e2)
				return i;
		return -1;
		}

	public Object getRoot()
		{
		if(meta==null)
			return new CustomTreeElement(null,null);
		else
			return new CustomTreeElement(meta.xml, null);
		}

	public boolean isLeaf(Object arg0)
		{
		CustomTreeElement e=(CustomTreeElement)arg0;
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
	
	public void addChild(CustomTreeElement e, Element ne)
		{
		e.e.addContent(ne);
		emitAllChanged();
		}
	
	public void updateElement(CustomTreeElement e)
		{
		for(TreeModelListener l:listener)
			l.treeNodesChanged(new TreeModelEvent(this, e.getPath()));
		
		emitAllChanged();
		}
	}
