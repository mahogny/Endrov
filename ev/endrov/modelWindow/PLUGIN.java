package endrov.modelWindow;
import endrov.ev.PluginDef;
import endrov.modelWindow.basicExt.ModelWindowClipPlane;
import endrov.modelWindow.basicExt.ModelWindowGrid;
import endrov.modelWindow.isosurf.*;
import endrov.modelWindow.slice3d.Slice3DExtension;
import endrov.modelWindow.voxel.VoxelExtension;

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
