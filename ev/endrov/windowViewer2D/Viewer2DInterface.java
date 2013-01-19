package endrov.windowViewer2D;

import endrov.data.EvContainer;
import endrov.gui.WorldScreenTransformer;
import endrov.util.EvDecimal;

public interface Viewer2DInterface extends WorldScreenTransformer
	{
	//public void addImageWindowTool(ImageWindowTool tool);
	public void addImageWindowRenderer(Viewer2DRenderer renderer);
	
	public EvContainer getRootObject();
	public double getRotation();
	
	public EvDecimal getZ();
	public EvDecimal getFrame();
	
	public void updateImagePanel();
	public String getCurrentChannelName(); //Highly questionable function
	
	public <E> E getRendererClass(Class<E> cl);
	
	public void unsetTool();
	}
