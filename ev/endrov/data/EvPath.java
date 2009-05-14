package endrov.data;

import java.util.List;


/**
 * Path to an object
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
	
	}
