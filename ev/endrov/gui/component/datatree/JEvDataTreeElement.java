/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.gui.component.datatree;

import java.lang.ref.WeakReference;

import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvPath;

/**
 * Node in the DataTree widget
 * 
 * @author Johan Henriksson
 *
 */
public class JEvDataTreeElement
	{
	public final boolean isRoot;
	private EvPath path;
	private boolean canCreate;
	private WeakReference<EvData> data; 
	private boolean isCreate;

	
	public JEvDataTreeElement(boolean canCreate)
		{
		isRoot=true;
		this.canCreate=canCreate;
		}

	public JEvDataTreeElement(boolean isCreate, boolean canCreate, EvData data, EvPath path)
		{
		this.path=path;
		this.data=new WeakReference<EvData>(data);
		isRoot=false;
		this.canCreate=canCreate;
		this.isCreate=isCreate;
		}
	
	public EvContainer getLeaf()
		{
		if(isCreate)
			throw new RuntimeException("Getting object for create-leaf");
		else
			return path.getObject();
		}

	public EvPath getPath()
		{
		if(!isRoot && path==null)
			throw new RuntimeException("inconsistency: path is null");
		return path;
		}

	public EvData getData()
		{
		return data.get();
		}

	public JEvDataTreeElement getChild(int index)
		{
		if(isRoot)
			{
			EvData data=EvData.openedData.get(index);
			return new JEvDataTreeElement(false, canCreate, data,new EvPath(data));
			}
		else
			{
			EvContainer c=getLeaf();
			int i=0;
			for(String s:c.metaObject.keySet())
				{
				if(i==index)
					return new JEvDataTreeElement(false, canCreate, data.get(),new EvPath(path,s));
				i++;
				}
			if(canCreate && i==index)
				return new JEvDataTreeElement(true, false, data.get(),new EvPath(path,"<NEW>"));
			return null;
			}
		}

	
	public String toString()
		{
		if(isRoot)
			return "";
		else
			{
			String leaf=path.getLeafName();
			if(leaf==null)
				return data.get().toString();
			else
				return leaf;
			}
		}

	public boolean isRealObject()
		{
		return !isCreate;
		}

	public int getChildCount()
		{
		if(isRealObject())
			{
			if(canCreate)
				return getLeaf().metaObject.size()+1;
			else
				return getLeaf().metaObject.size();
			}
		else
			return 0;
		}
	
	
	}
