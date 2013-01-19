/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowViewer3D;

import endrov.core.EvPluginDefinition;
import endrov.windowViewer3D.basicExtensions.ModelWindowClipPlane;
import endrov.windowViewer3D.basicExtensions.ModelWindowGrid;
import endrov.windowViewer3D.basicExtensions.ModelWindowScreenshot;

public class PLUGIN extends EvPluginDefinition
	{
	public String getPluginName()
		{
		return "3D viewer";
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
		return new Class[]{Viewer3DWindow.class,ModelWindowClipPlane.class,ModelWindowGrid.class, ModelWindowScreenshot.class};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}
