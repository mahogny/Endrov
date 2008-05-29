package evplugin.modelWindow;
import evplugin.ev.PluginDef;
import evplugin.modelWindow.basicExt.ModelWindowClipPlane;
import evplugin.modelWindow.basicExt.ModelWindowGrid;
import evplugin.modelWindow.isosurf.*;
import evplugin.modelWindow.slice3d.Slice3DExtension;
import evplugin.modelWindow.voxel.VoxelExtension;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Model Window";
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
		return new Class[]{ModelWindow.class,ModelWindowClipPlane.class,ModelWindowGrid.class,IsosurfaceExtension.class,Slice3DExtension.class,VoxelExtension.class};
		}
	}
