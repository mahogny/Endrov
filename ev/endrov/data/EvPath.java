/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.data;

import java.lang.ref.WeakReference;
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
	private String path[];
	private WeakReference<EvContainer> root; //This better be data or there will be trouble... I think

	/**
	 * Construct a path by a list
	 */
	public EvPath(EvContainer root, List<String> path)
		{
		this.root=new WeakReference<EvContainer>(root);
		this.path=path.toArray(new String[0]);
		if(this.root.get()==null)
			throw new RuntimeException("root is null");
		}
	
	/**
	 * Construct a path piece by piece. Does not do a deep copy of the array
	 */
	public EvPath(EvContainer root, String... path)
		{
		this.root=new WeakReference<EvContainer>(root);
		this.path=path;
		if(this.root.get()==null)
			throw new RuntimeException("root is null");
		}
	
	/**
	 * The child of a parent
	 */
	public EvPath(EvPath parent, String child)
		{
		this.root=parent.root;
		path=new String[parent.path.length+1];
		for(int i=0;i<parent.path.length;i++)
			path[i]=parent.path[i];
		path[parent.path.length]=child;
		if(this.root.get()==null)
			throw new RuntimeException("root is null");
		}
	
	
	/**
	 * Get standard representation
	 */
	public String toString()
		{
		return toString(false);
		}
	public String toString(boolean showData)
		{
		StringBuffer sb=new StringBuffer();
		EvContainer root=this.root.get();
		if(root==null)
			throw new RuntimeException("root is null");
		if(root instanceof EvData)
			{
			sb.append("#");
			sb.append(((EvData)root).getMetadataName());
			if(path.length!=0)
				sb.append("/");
			}
		
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
	 */
	public EvPath getParent()
		{
		LinkedList<String> list=new LinkedList<String>();
		for(String s:path)
			list.add(s);
		if(!list.isEmpty())
			{
			list.removeLast();
			return new EvPath(root.get(), list.toArray(new String[]{}));
			}
		else
			throw new RuntimeException("path: Unhandled case of getparent, already at the root");
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
	private EvContainer getContainerRecurse(EvContainer c, int pos)
		{
		if(pos<path.length)
			{
			//System.out.println("torecurse "+path[pos]+"   --- "+pos);
			EvContainer sub=c.metaObject.get(path[pos]);
			if(sub==null)
				throw new RuntimeException("Cannot find container "+path[pos]+" , total path: "+toString());
			return getContainerRecurse(sub,  pos+1);
			}
		else
			return c;
		}

	/**
	 * TODO Needs testing
	 * @param currentData
	 */
	/*
	public EvContainer getObject(EvData currentData)
		{
		return getContainer(currentData,new EvPath(currentData));
		}*/
	public EvContainer getObject()
		{
		return getContainerRecurse(root.get(), 0);
		//return getContainer(root.get(),new EvPath(currentData));
		}
	
	/**
	 * Get the object the path points to
	 * 
	 * TODO should it throw an exception?
	 *
	 * TODO this function is strange... rethink it
	 *  
	 */
	/*
	private EvContainer getContainer(EvData currentData, EvPath currentPath)
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
	*/
	public EvPath getRelativePath(String relpath)
		{
		LinkedList<String> newpath=new LinkedList<String>();
		EvContainer newroot=root.get();
		
		//Use old path as base
		for(String s:path)
			newpath.add(s);
		
		//Go through relative path
		boolean first=true;
		StringTokenizer st=new StringTokenizer(relpath,"/");
		while(st.hasMoreElements())
			{
			String s=st.nextToken();
			
			if(s.equals(""))
				{
				if(first)
					{
					//This means it starts with a /
					newpath.clear();
					}
				}
			else if(s.equals(".."))
				{
				//Parent
				if(newpath.isEmpty())
					throw new RuntimeException("Trying to .. into parent but already at top level");
				else
					newpath.removeLast();
				}
			else if(s.equals("."))
				; //Same object, do nothing
			else if(s.startsWith("#"))
				{
				boolean found=false;
				s=s.substring(1);
				//Absolute path in another data
				for(EvData d:EvData.openedData)
					if(d.getMetadataName().equals(s))
						{
						newroot=d;
						newpath.clear();
						found=true;
						}
				if(!found)
					throw new RuntimeException("Absolute reference to object with # but no such object exists: "+s);
				}
			else
				{
				//Go to sub-object
				newpath.add(s);
				}
			first=false;
			}
		
		return new EvPath(newroot, newpath);
		}
	
	/**
	 * Return a relative path, if possible
	 * @param base
	 * @return
	 */
	public String getStringPathRelativeTo(EvPath base)
		{
		//Check if they share root
		if(base.root.get()!=root.get())
			return toString(true);
		else
			{
			//If they do, see how much is in common
			int i=0;
			while(i<base.path.length && i<path.length && base.path[i].equals(path[i]))
				{
				System.out.println("common "+path[i]+" "+base.path[i]);
				i++;
				}
			
			
			
			if(base.path.length>i)
				{
				//Has to go to a parent of base object using .. . For now, not nicely supported
				return toString(true);
				}
			else if(path.length>i)
				{
				//It is a direct sub-object
				String rel="";
				for(;i<path.length-1;i++)
					rel+=path[i]+"/";
				rel+=path[i];
				return rel;
				}
			else
				{
				//It is the same object
				return ".";
				}
			
			
			}
		
		
		
		}
	
	
	/**
	 * Parse a path string, with a default root container if not an absolute path
	 */
	public static EvPath parse(EvContainer root, String s)
		{
		if(root==null)
			throw new RuntimeException("root is null");
		StringTokenizer st=new StringTokenizer(s,"/");
		LinkedList<String> toks=new LinkedList<String>();
		while(st.hasMoreElements())
			toks.add(st.nextToken());
		return new EvPath(root, toks.toArray(new String[]{}));
		}

	public EvContainer getRoot()
		{
		return root.get();
		}
	
	}
