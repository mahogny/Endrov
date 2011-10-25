/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.data.tree;

import java.lang.ref.WeakReference;

import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvPath;

/**
 * Node in the DataTree widget
 * @author Johan Henriksson
 *
 */
public class DataTreeElement
	{
	public final boolean isRoot;
	private EvPath path;
	
	private WeakReference<EvData> data; 

	public DataTreeElement()
		{
		isRoot=true;
		}

	public DataTreeElement(EvData data, EvPath path)
		{
		this.path=path;
		this.data=new WeakReference<EvData>(data);
		isRoot=false;
		}
	
	public EvContainer getLeaf()
		{
		return path.getObject();
//		return data.get().getChild(path);
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

	public DataTreeElement getChild(int index)
		{
		if(isRoot)
			{
			EvData data=EvData.openedData.get(index);
			return new DataTreeElement(data,new EvPath(data));
			}
		else
			{
			EvContainer c=getLeaf();
			int i=0;
			for(String s:c.metaObject.keySet())
				{
				if(i==index)
					return new DataTreeElement(data.get(),new EvPath(path,s));
				i++;
				}
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
	
	
	}
