package evplugin.roi;
import evplugin.ev.PluginDef;
import evplugin.roi.primitive.BoxROI;
import evplugin.roi.primitive.EllipseROI;
import evplugin.roi.primitive.IntersectROI;
import evplugin.roi.primitive.UnionROI;
import evplugin.roi.window.WindowROI;

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
				BoxROI.class, EllipseROI.class,IntersectROI.class,UnionROI.class};
		}
	}
