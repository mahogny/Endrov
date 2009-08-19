package endrov.data;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * Path to an object.<br/>
 * 
 * Relative path: ob1/ob2/ob3 <br/> 
 * Absolute path: /ob1/ob2 <br/>
 * Path into another EvData: #dataname/ob1/ob2 <br/>
 * 
 * @author Johan Henriksson
 *
 */
public class EvPath implements Comparable<EvPath>
	{
	//another path interface in HW
	public String path[];
	
	
	public EvPath(List<String> path)
		{
		this.path=path.toArray(new String[0]);
		}
	public EvPath(String... path)
		{
		//Maybe make a copy?
		this.path=path;
		}
	public EvPath(EvPath root, String child)
		{
		path=new String[root.path.length+1];
		for(int i=0;i<root.path.length;i++)
			path[i]=root.path[i];
		path[root.path.length]=child;
		}
	
	
	/**
	 * Get standard representation
	 */
	public String toString()
		{
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<path.length;i++)
			{
			sb.append(path[i]);
			if(i!=path.length-1)
				sb.append("/");
			}
		return sb.toString();
		}
	
	/**
	 * Get name of last object in hierarchy
	 */
	public String getLeafName()
		{
		if(path.length!=0)
			return path[path.length-1];
		else
			return null;
		}
	
	/**
	 * Get path to parent
	 * TODO handle special cases
	 */
	public EvPath getParent()
		{
		LinkedList<String> list=new LinkedList<String>();
		for(String s:path)
			list.add(s);
		if(!list.isEmpty())
			list.removeLast();
		else
			System.out.println("Unhandled case of getparent");
		return new EvPath(list.toArray(new String[]{}));
		}
	
	/**
	 * Ordering of paths
	 */
	public int compareTo(EvPath o)
		{
		int checkLength=path.length;
		int defret=-1;
		if(o.path.length<checkLength)
			{
			checkLength=o.path.length;
			defret=1;
			}
		else if(path.length==o.path.length)
			defret=0;
		for(int i=0;i<checkLength;i++)
			{
			int cmp=path[i].compareTo(o.path[i]);
			if(cmp!=0)
				return cmp;
			}
		return defret;
		}
	
	
	/**
	 * Recurse a container given path
	 */
	private EvContainer getContainerRecurse(EvContainer c, String[] path, int pos)
		{
		if(pos<path.length)
			{
			System.out.println("torec "+path[pos]+"   --- "+pos);
			EvContainer sub=c.metaObject.get(path[pos]);
			if(sub==null)
				throw new RuntimeException("Cannot find container "+path[pos]);
			return getContainerRecurse(sub, path, pos+1);
			}
		else
			return c;
		}

	/**
	 * Needs testing
	 * @param currentData
	 * @return
	 */
	public EvContainer getContainer(EvData currentData)
		{
		return getContainer(currentData,new EvPath());
		}
	
	/**
	 * Get the object the path points to
	 * 
	 * TODO should it throw an exception? 
	 */
	public EvContainer getContainer(EvData currentData, EvPath currentPath)
		{
		if(path.length==0)
			{
			//Relative path special case: currentPath
			return getContainerRecurse(currentData, currentPath.path, 0);
			}
		else
			{
			String s=path[0];
			if(s.startsWith("#"))
				{
				s=s.substring(1);
				//Absolute path in another data
				for(EvData d:EvData.openedData)
					if(d.getMetadataName().equals(s))
						return getContainerRecurse(d, path, 1);
				return null;
				}
			else if(s.equals(""))
				{
				//Absolute path within data
				return getContainerRecurse(currentData, path, 1);
				}
			else
				{
				//Relative path
				EvContainer c=getContainerRecurse(currentData, currentPath.path, 0);
				return getContainerRecurse(c, path, 0);
				}
			}
		
		}
	
	
	
	public static EvPath parse(String s)
		{
		StringTokenizer st=new StringTokenizer(s,"/");
		LinkedList<String> toks=new LinkedList<String>();
		while(st.hasMoreElements())
			toks.add(st.nextToken());
		return new EvPath(toks.toArray(new String[]{}));
		}
	
	}
