/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.roi;

//or should one at least supply scanlines? much less overhead. channel,frame,z,y -> [x]
public class EmptyLineIterator extends LineIterator
	{
	public boolean next()
		{
		return false;
		}
	}
