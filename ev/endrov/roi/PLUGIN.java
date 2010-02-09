/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.roi;
import endrov.ev.PluginDef;
import endrov.roi.primitive.BoxROI;
import endrov.roi.primitive.DiffROI;
import endrov.roi.primitive.EllipseROI;
import endrov.roi.primitive.IntersectROI;
import endrov.roi.primitive.SubtractROI;
import endrov.roi.primitive.UnionROI;
import endrov.roi.window.WindowROI;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "ROI system";
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
		return new Class[]{ROI.class,WindowROI.class,
				BoxROI.class, EllipseROI.class,UnionROI.class,IntersectROI.class,DiffROI.class,SubtractROI.class};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}
