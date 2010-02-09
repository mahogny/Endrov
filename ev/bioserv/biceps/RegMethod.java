/**
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package bioserv.biceps;

import java.lang.reflect.Method;

/**
 * Registered method
 * @author Johan Henriksson
 */
public class RegMethod
	{
	public final Method m;
//	public final Class<?>[] c;
//	public final Class<?> r;
	public final Object mthis;
	
	public RegMethod(Object mthis, Method m)
		{
		this.m=m;
//		c=m.getParameterTypes();
//		r=m.getReturnType();
		this.mthis=mthis;
		}
	}
