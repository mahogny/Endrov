/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flow;

import javax.swing.ImageIcon;

/**
 * One flow unit type
 * @author Johan Henriksson
 *
 */
public class FlowUnitDeclaration //implements Comparable<FlowUnitDeclaration>
	{
	public final String name, category, metadata, description;
	public final ImageIcon icon;
	private final Class<? extends FlowUnit> c;
	public FlowUnitDeclaration(String category, String name, String metadata,Class<? extends FlowUnit> c, ImageIcon icon,
			String description)
		{
		this.name=name;
		this.category=category;
		this.metadata=metadata;
		this.c=c;
		this.icon=icon;
		this.description=description;
		}
	

	
	public FlowUnit createInstance()
		{
		try
			{
			return c.newInstance();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		System.out.println("this should not happen");
		return null; //wtf
		}
	
	public String toString()
		{
		return name;
		}

/*
	//Can be discussed if this method should be here at all
	public int compareTo(FlowUnitDeclaration o)
		{
		int cmp=name.compareTo(o.name);
		if(cmp==0)
			return Double.compare(hashCode(), o.hashCode());
		else
			return cmp;
		}
	*/
	}
