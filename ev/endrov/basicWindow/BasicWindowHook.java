/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.basicWindow;


/**
 * Template for hook for extension to BasicWindow (one hook per extension and window)
 * @author Johan Henriksson
 */
public interface BasicWindowHook
	{
	public abstract void createMenus(BasicWindow w);
	public abstract void buildMenu(BasicWindow w);
	}
