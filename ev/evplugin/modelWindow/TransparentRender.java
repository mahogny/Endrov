package evplugin.modelWindow;

import javax.media.opengl.GL;

public abstract class TransparentRender implements Comparable<TransparentRender>
	{
	public double z;
	public abstract void render(GL gl);
	
	public int compareTo(TransparentRender o)
		{
		if(z<o.z) return -1;
		else if(z>o.z) return 1;
		else return 0;
		}
	
	/**
	 * To avoid render state switches in GL, we keep track of state changers
	 */
	public interface RenderState
		{
		public void activate(GL gl);
		public void deactivate(GL gl);
		}
	}
