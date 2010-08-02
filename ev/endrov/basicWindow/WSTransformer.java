/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.basicWindow;

import javax.vecmath.Vector2d;

public interface WSTransformer
	{
	public Vector2d transformPointW2S(Vector2d v);
	public Vector2d transformPointS2W(Vector2d v);
	
	/** 
	 * Scale screen vector to world vector 
	 */
	public double scaleS2w(double s);
	
	/**
	 * Scale world to screen vector 
	 */
	public double scaleW2s(double w);
	
	/** Convert world to screen Z coordinate */
	public double w2sz(double z); 
	/** Convert world to screen Z coordinate */
	public double s2wz(double sz); 
	}
