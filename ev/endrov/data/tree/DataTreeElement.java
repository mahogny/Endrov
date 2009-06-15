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
		return data.get().getChild(path);
		}

	public EvPath getPath()
		{
		return path;
		}

	public EvData getData()
		{
		return data.get();
		}

	public DataTreeElement getChild(int index)
		{
		if(isRoot)
			return new DataTreeElement(EvData.openedData.get(index),new EvPath());
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
			//return "<data>";
			return "";
		else if(path.path.length==0)
			return data.get().toString();
		else
			return path.getLeafName();
		}
	
	
	}
