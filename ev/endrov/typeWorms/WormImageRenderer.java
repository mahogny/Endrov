/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeWorms;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.*;

import javax.vecmath.*;


import endrov.typeImageset.EvChannel;
import endrov.typeImageset.EvStack;
import endrov.util.math.EvDecimal;
import endrov.util.math.Vector3i;
import endrov.windowViewer2D.*;

/**
 * Image window renderer of network
 * @author Johan Henriksson
 *
 */
public class WormImageRenderer implements Viewer2DRenderer
	{
	public Viewer2DWindow w;
	


	
	public WormImageRenderer(Viewer2DWindow w)
		{
		this.w=w;
		}


	
	public Collection<WormFit> getVisibleObjects()
		{
		return w.getRootObject().getObjects(WormFit.class);
		}
	

	
	/**
	 * Render
	 */
	public void draw(Graphics g)
		{
		EvDecimal currentFrame=w.getFrame();
		
		for(WormFit wfit:getVisibleObjects())
			{
			
			WormFit.WormFrame wf=wfit.frames.get(currentFrame);
			if(wf!=null)
				{
				//Draw center line
				g.setColor(Color.red);
				for(int i=0;i<wf.centerPoints.size()-1;i++)
					{
					Vector2d posS=w.transformPointW2S(wf.centerPoints.get(i));
					Vector2d posS2=w.transformPointW2S(wf.centerPoints.get(i+1));
					g.drawLine((int)posS.x, (int)posS.y, (int)posS2.x, (int)posS2.y);
					}
				
				//TODO draw outline
				
				}
			
			
			}
		
		}
	
	
	public void dataChangedEvent()
		{
		}

	
	
	
	
	

	/**
	 * Get mouse position within image
	 */
	Vector3i getMousePosImage(EvStack stack, MouseEvent e)
		{
		Vector2d pressPosWorldXY=w.transformPointS2W(new Vector2d(e.getX(),e.getY()));
		double pressPosWorldZ=w.getZ().doubleValue();
		
		Vector3d toPosImage=stack.transformWorldImage(new Vector3d(pressPosWorldXY.x, pressPosWorldXY.y, pressPosWorldZ));
		return new Vector3i((int)toPosImage.x, (int)toPosImage.y, (int)toPosImage.z);
		}

	/**
	 * Get stack to trace at the moment
	 */
	EvStack getCurrentStack()
		{
		EvDecimal frame=w.getFrame();
		EvChannel ch=w.getCurrentChannel();
		if(ch==null)
			return null;
		else
			{
			EvDecimal closestFrame=ch.closestFrame(frame);
			EvStack stack=ch.getStack(closestFrame);
			return stack;
			}
		}
	
	

	
	
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Viewer2DWindow.addImageWindowExtension(new Viewer2DWindowExtension()
			{
			public void newImageWindow(Viewer2DWindow w)
				{
				WormImageRenderer r=new WormImageRenderer(w);
				w.addImageWindowTool(new WormImageTool(w,r));
				w.addImageWindowRenderer(r);
				}
			});
		}
	
	
	

	}
