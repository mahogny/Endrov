/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.data;

import endrov.basicWindow.EvColor;
import endrov.data.EvSelection.EvSelectable;

/**
 * Selection of an object - common baseline class
 * 
 * @author Johan Henriksson
 *
 */
public class EvSelectObject<E> implements EvSelectable
	{
	static final long serialVersionUID=0;
	
	private E mesh;
	
	public EvSelectObject(E mesh)
		{
		this.mesh=mesh;
		}
	
	/*
	public int hashCode()
		{
		//needed? don't think so
		return super.hashCode();
		}*/
	
	
	public void setColor(EvColor c)
		{
		//TODO
		
		
		//getParticle().color=c.getAWTColor();
		}
	
	protected EvSelectObject<E> clone()
		{
		return new EvSelectObject<E>(mesh);
		}

	
	
	}
