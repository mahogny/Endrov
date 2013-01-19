/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.utilityUnsorted.generics.muting;

public class NumFloat implements Num
	{
	double x;
	
	public NumFloat(double x)
		{
		this.x = x;
		}

	public Num add(Num a, Num b)
		{
		return new NumFloat(((NumFloat)a).x+((NumFloat)b).x);
		}
	}
