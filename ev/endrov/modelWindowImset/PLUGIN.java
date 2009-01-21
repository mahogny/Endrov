package endrov.modelWindowImset;
import endrov.ev.PluginDef;
import endrov.modelWindowImset.isosurf.IsosurfaceExtension;
import endrov.modelWindowImset.slice3d.Slice3DExtension;
import endrov.modelWindowImset.voxel.VoxelExtension;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Model Window Imageset Viewing";
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
//		return new Class[]{};
		return new Class[]{IsosurfaceExtension.class,Slice3DExtension.class,VoxelExtension.class};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}
