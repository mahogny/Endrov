/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.generics;

import endrov.imageset.EvPixels;

public abstract class EvGenerics
	{
	public abstract boolean greaterThan(EvGenerics b);
	public static EvGenericsA[] getPixelsA(EvPixels p){return null;}
	
	/**
	 * which type to return?
	 * how about EvGenericsCommon which is the best match of the two?
	 * 
	 * intermediates: EvGenericsAx   int or double, depending on context
	 * 
	 */
	public static EvGenerics add(EvGenerics a, EvGenerics b){return null;};
	
	/**
	 * an import static makes these comfortable to use. the advantage is
	 * that it easily works with scalar arguments so the syntax need not change.
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean greaterThan(EvGenerics a, EvGenerics b){return false;};
	}
