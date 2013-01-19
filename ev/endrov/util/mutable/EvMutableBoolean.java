/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util.mutable;

//TODO: add ev observers

/**
 * Mutable Boolean - replacement for Boolean whenever one wants to be able to change the value 
 * 
 * @author Johan Henriksson
 */
public class EvMutableBoolean 
	{
	static final long serialVersionUID=0;
	private boolean b;

	
	public EvMutableBoolean()
		{
		b=false;
		}
	
	public EvMutableBoolean(boolean b)
		{
		this.b=b;
		}

	
	public void setValue(boolean b)
		{
		this.b=b;
		}
	public boolean getValue()
		{
		return b;
		}
	
	
	}
