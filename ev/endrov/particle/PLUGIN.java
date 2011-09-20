/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.particle;
import endrov.ev.PluginDef;
import endrov.particle.modw.LineageModelExtension;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Lineage (definition)";
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
		return new Class[]{
				Lineage.class,
				LineageModelExtension.class
				};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}
