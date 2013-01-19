/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowViewer3D;

import javax.media.opengl.GL;

/**
 * Renderer of transparent objects. Sorted by variable z before called. The RenderState will be
 * set before the render is called. Can be null.
 * 
 * @author Johan Henriksson
 */
public abstract class TransparentRenderer3D implements Comparable<TransparentRenderer3D>
	{
	public double z;
	
	/** Required render state, can be null */
	public RenderState renderState=null;

	/** Called to render this object */
	public abstract void render(GL gl);

	public int compareTo(TransparentRenderer3D o)
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
		/** Asked to switch with less overhead. Returns true if it succeeds. */
		public boolean optimizedSwitch(GL gl, TransparentRenderer3D.RenderState currentState);
		}
	}
