/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowWindow;
import endrov.ev.PluginDef;
import endrov.flowWindow.FlowWindow;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Flow window";
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
		return new Class[]{FlowWindow.class};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}
