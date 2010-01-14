/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.ev;

//TODO: add ev observers

/**
 * Mutable Double - replacement for Double whenever one wants to be able to change the value 
 * 
 * @author Johan Henriksson
 */
public class EvMutableInteger extends Number
	{
	static final long serialVersionUID=0;
	private int d;

	
	public EvMutableInteger()
		{
		d=0;
		}
	
	public EvMutableInteger(int d)
		{
		this.d=d;
		}

	public EvMutableInteger(Number n)
		{
		d=n.intValue();
		}

	public double doubleValue()
		{
		return d;
		}

	public float floatValue()
		{
		return (float)d;
		}

	public int intValue()
		{
		return d;
		}

	public long longValue()
		{
		return (long)d;
		}
	

	public void setValue(int d)
		{
		this.d=d;
		}
	public int getValue()
		{
		return d;
		}
	
	
	}
