/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.ev;

/**
 * Definition of a plugin
 * @author Johan Henriksson
 */
public abstract class PluginDef
	{
	public abstract String getPluginName();
	public abstract String getAuthor();
	public abstract String[] requires();
	public abstract Class<?>[] getInitClasses();
	public abstract String cite();
	public abstract boolean systemSupported();
	public abstract boolean isDefaultEnabled();
	}
