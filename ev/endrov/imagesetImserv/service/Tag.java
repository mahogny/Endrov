package endrov.imagesetImserv.service;

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
	
	
	
	//Two tags are considered equal if name is equal
	
	//used??
	
	public boolean equals(Object obj)
		{
		if(obj!=null && obj instanceof Tag)
			return name.equals(((Tag)obj).name);
		else
			return false;
		}
	
	
	
	}
