package evplugin.imagesetImserv.service;

import java.io.Serializable;

/**
 * Tag/Attribute
 * @author Johan Henriksson
 *
 */
public class Tag implements Serializable // Comparable<Tag>, Serializable//, Externalizable
	{
	public static final long serialVersionUID=0;
	
	public String name;
	public String value;
	public boolean virtual;

	public Tag(boolean virtual, String name, String value)
		{
		this.virtual=virtual;
		this.name=name;
		this.value=value;
		}
	public Tag(boolean virtual, String name)
		{
		this.virtual=virtual;
		this.name=name;
		}

	public String toString()
		{
		if(value==null)
			return TagExpr.escapeStringIfNeeded(name);
		else
			return TagExpr.escapeStringIfNeeded(name)+"="+TagExpr.escapeStringIfNeeded(value);
		}
	
	/*
	public Tag(boolean virtual, String name, String value)
		{
		this.virtual=virtual;
		this.name=name;
		this.value=value;
		}

	public Tag(boolean virtual, String name)
		{
		this.virtual=virtual;
		this.name=name;
		}
*/
	

	
	//Two tags are considered equal if name is equal
	
	public boolean equals(Object obj)
		{
		if(obj!=null && obj instanceof Tag)
			return name.equals(((Tag)obj).name);
		else
			return false;
		}
	/*
	public int compareTo(Tag o)
		{
		return name.compareTo(o.name);
		}
*/
	/*
	public int compareTo(Tag o)
		{
		int la=name.length;
		int lb=o.name.length;
		for(int i=0;i<la && i<lb;i++)
			{
			int c=name[i].compareTo(o.name[i]);
			if(c!=0)
				return c;
			}
		if(la<lb)
			return -1;
		else if(la>lb)
			return 1;
		else
			return 0;
		}
*/
	
	
	/*
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
		{
		}

	public void writeExternal(ObjectOutput out) throws IOException
		{
		}
*/	
	
	
	}
