/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.generics.muting;

public class MutFloat implements Mut
	{
	double x;
	
	public void add(Num b)
		{
		x+=((MutFloat)b).x;
		}
	}
