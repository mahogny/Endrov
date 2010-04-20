package endrov.imageWindow;

import endrov.basicWindow.WSTransformer;
import endrov.data.EvContainer;
import endrov.util.EvDecimal;

public interface ImageWindowInterface extends WSTransformer
	{
	
	public void addImageWindowTool(ImageWindowTool tool);
	public void addImageWindowRenderer(ImageWindowRenderer renderer);
	
	
	public EvContainer getRootObject();
	public double getRotation();
	
	public EvDecimal getModelZ();
	
	
	public <E> E getRendererClass(Class<E> cl);
	}
