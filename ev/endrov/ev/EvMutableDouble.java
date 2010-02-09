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
public class EvMutableDouble extends Number
	{
	static final long serialVersionUID=0;
	private double d;

	
	public EvMutableDouble()
		{
		d=0;
		}
	
	public EvMutableDouble(double d)
		{
		this.d=d;
		}

	public EvMutableDouble(Number n)
		{
		d=n.doubleValue();
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
		return (int)d;
		}

	public long longValue()
		{
		return (long)d;
		}
	

	public void setValue(double d)
		{
		this.d=d;
		}
	public double getValue()
		{
		return d;
		}
	
	
	}
