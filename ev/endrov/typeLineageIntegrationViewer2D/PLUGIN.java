/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeLineageIntegrationViewer2D;
import endrov.core.EvPluginDefinition;
import endrov.typeLineageIntegrationViewer2D.LineageImageRenderer;

public class PLUGIN extends EvPluginDefinition
	{
	public String getPluginName()
		{
		return "Lineage (image window extension)";
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
		return new Class[]{LineageImageRenderer.class};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}
