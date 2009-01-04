package endrov.roi.window;

import java.util.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import endrov.data.*;

/**
 * Tree model showing the XML of a custom meta object
 * @author Johan Henriksson
 */
public class ROITreeModel implements TreeModel
	{
	HashSet<TreeModelListener> listener=new HashSet<TreeModelListener>();
	private EvContainer meta=null;

	public WeakHashMap<Object, ROITreeElement> allElements=new WeakHashMap<Object, ROITreeElement>(); 
	
	
	public void setMetaObject(EvContainer o)
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

	

	private ROITreeElement getCreate(Object e, ROITreeElement parent)
		{
		//System.out.println("getcreate e:"+e+" p:"+parent);
		ROITreeElement o=allElements.get(e);
		if(o==null)
			{
			o=new ROITreeElement(this,e, parent);
			allElements.put(e,o);
			}
		return o;
		}
	
	
	public Object getChild(Object parento, int childnum)
		{
		ROITreeElement parent=(ROITreeElement)parento;
		return getCreate(parent.getROIChildren().get(childnum), parent);
		}

	public int getChildCount(Object arg0)
		{
		ROITreeElement e=(ROITreeElement)arg0;
		return e.getROIChildren().size();
		}

	public int getIndexOfChild(Object arg0, Object arg1)
		{
		if(arg0==null || arg1==null)
			return -1;
		ROITreeElement e=(ROITreeElement)arg0;
		ROITreeElement e2=(ROITreeElement)arg1;
		List<?> list=e.getROIChildren();
		for(int i=0;i<list.size();i++)
			if(list.get(i)==e2)
				return i;
		return -1;
		}

	public Object getRoot()
		{
		if(meta==null)
			return new ROITreeElement(null, null,null);
		else
			return getCreate(meta,null);
		}

	public boolean isLeaf(Object o)
		{
		ROITreeElement e=(ROITreeElement)o;
		return e.isLeaf();
		}


	/** When item changed. Not supported. */
	public void valueForPathChanged(TreePath arg0, Object arg1){}
	
	public void emitAllChanged()
		{
		for(TreeModelListener l:listener)
			l.treeStructureChanged(new TreeModelEvent(this, new Object[]{getRoot()}));
		}
	
	public void updateElement(ROITreeElement e)
		{
		for(TreeModelListener l:listener)
			l.treeNodesChanged(new TreeModelEvent(this, e.getPath()));
		emitAllChanged();
		}
	}
