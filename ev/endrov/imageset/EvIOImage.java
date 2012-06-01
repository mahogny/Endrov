/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageset;

import java.io.File;

import endrov.util.MemoizeX;


/**
 * I/O functions connected to a particular image
 * 
 * @author Johan Henriksson
 *
 */
public abstract class EvIOImage extends MemoizeX<EvPixels>
	{
	public abstract File getRawJPEGData();
	
	
	
	protected static File defaultGetRawJPEG(File f)
		{
		String n=f.getName().toLowerCase();
		if(n.endsWith(".jpg") || n.endsWith(".jpeg"))
			return f;
		else
			return null;
		}
	}
