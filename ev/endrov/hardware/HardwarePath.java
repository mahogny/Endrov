package endrov.hardware;

import java.util.*;

/**
 * Path to hardware. Internally a list of strings, for the user seen as a dot-separated string
 * @author Johan Henriksson
 */
public class HardwarePath implements Comparable<HardwarePath>
	{
	public String[] path;
	
	/**
	 * Construct from dot-separated path
	 */
	public HardwarePath(String dotPath)
		{
		List<String> n=new LinkedList<String>();
		StringTokenizer stok=new StringTokenizer(dotPath,"/");
		while(stok.hasMoreElements())
			n.add(stok.nextToken());
		path=n.toArray(new String[]{});
		}

	/**
	 * Construct from raw path
	 */
	public HardwarePath(String[] path)
		{
		this.path=path;
		}
	
	/**
	 * Make dot-representation
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
	 * Ordering of paths
	 */
	public int compareTo(HardwarePath o)
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
	 * testing
	 */
	public static void main(String[] arg)
		{
		HardwarePath a=new HardwarePath(new String[]{"a","c"});
		HardwarePath b=new HardwarePath(new String[]{"a"});
		System.out.println(a.compareTo(b));
		System.out.println(a);
		}
	
	}
