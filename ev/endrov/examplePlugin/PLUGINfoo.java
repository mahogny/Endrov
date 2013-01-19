/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.examplePlugin;
import endrov.core.EvPluginDefinition;

/**
 * Should be named PLUGIN to work
 * @author Johan Henriksson
 *
 */
public class PLUGINfoo extends EvPluginDefinition
	{
	public String getPluginName()
		{
		return "Example plugin";
		}

	public String getAuthor()
		{
		return "Johan Henriksson";
		}
	
	public boolean systemSupported()
		{
		return true;
		}
	
	public String cite()
		{
		return "";
		}
	
	public String[] requires()
		{
		return new String[]{};
		}
	
	public Class<?>[] getInitClasses()
		{
		return new Class[]{ExampleObject.class};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}
