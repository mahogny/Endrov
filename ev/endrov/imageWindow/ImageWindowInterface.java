package endrov.imageWindow;

import endrov.basicWindow.WSTransformer;

public interface ImageWindowInterface extends WSTransformer
	{
	
	public void addImageWindowTool(ImageWindowTool tool);
	public void addImageWindowRenderer(ImageWindowRenderer renderer);
	
	
	
	public void getRendererClass(Class<ImageWindowRenderer> cl);

	}
