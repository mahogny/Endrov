/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeNetwork;

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
public class NetworkImageRenderer implements Viewer2DRenderer
	{
	public Viewer2DWindow w;
	Vector3d[] previewPoints;

	
	public NetworkImageRenderer(Viewer2DWindow w)
		{
		this.w=w;
		}


	
	public Collection<Network> getVisibleObjects()
		{
		return w.getRootObject().getObjects(Network.class);
		}
	

	
	/**
	 * Render
	 */
	public void draw(Graphics g)
		{
		EvDecimal currentFrame=w.getFrame();
		
		for(Network network:getVisibleObjects())
			{
			Network.NetworkFrame nf=network.frame.get(currentFrame);
			
			if(nf!=null)
				{

				///////// Render all points
				g.setColor(Color.red);
				for(Network.Point p:nf.points.values())
					{
					double useRadius;
					if(p.r==null)
						useRadius=w.scaleS2w(1);
					else
						useRadius=p.r;
					
					double pr=projectSphere(useRadius, p.z);
					if(pr>0)
						{
						Vector2d pos=w.transformPointW2S(new Vector2d(p.x,p.y));
						int x=(int)pos.x;
						int y=(int)pos.y;
						int sr=(int)pr;
						g.drawOval(x-sr, y-sr, sr*2+1, sr*2+1);
						if(p.z==w.getZ().doubleValue())
							{
							sr++;
							g.drawOval(x-sr, y-sr, sr*2+1, sr*2+1);
							}
						}

					}
				
				///////// Render segment
				g.setColor(Color.CYAN);
				for(Network.Segment s:nf.segments)
					{
					Integer lastX=null;
					int lastY=0;
					for(int pi:s.points)
						{
						Network.Point p=nf.points.get(pi);
						Vector2d pos=w.transformPointW2S(new Vector2d(p.x,p.y));
						int x=(int)pos.x;
						int y=(int)pos.y;
						if(lastX!=null)
							g.drawLine(lastX, lastY, x, y);
						lastX=x;
						lastY=y;
						}
					}
				
				///////// Render preview points
				g.setColor(Color.YELLOW);
				if(previewPoints!=null)
					{
					Integer lastX=null;
					int lastY=0;
					for(Vector3d p:previewPoints)
						{
						Vector2d pos=w.transformPointW2S(new Vector2d(p.x,p.y));
						int x=(int)pos.x;
						int y=(int)pos.y;
						if(lastX!=null)
							g.drawLine(lastX, lastY, x, y);
						lastX=x;
						lastY=y;
						}
					}
				
				
				}
			
			}
		
		}
	
	
	public void dataChangedEvent()
		{
		}

	
	
	/**
	 * Project sphere onto plane, returns size in pixels
	 * @param r Radius
	 * @param z Relative z
	 * @return Projected radius in pixels
	 */
	private double projectSphere(double r, double z)
		{
		//Currently assumes resx=resy. Maybe this should be specified harder?
		//double wz=w.frameControl.getZ().doubleValue();//w.s2wz(w.frameControl.getZ().doubleValue());
		double wz=w.getZ().doubleValue(); 
		double tf=r*r-(z-wz)*(z-wz);
		if(tf>0)
			{
			double wpr=Math.sqrt(tf);
			return w.scaleW2s(wpr);	
			}
		else
			return -1;
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
	
	Vector3d getMousePosWorld(MouseEvent e)
		{
		Vector2d u=w.transformPointS2W(new Vector2d(e.getX(),e.getY()));
		return new Vector3d(u.x, u.y, w.getZ().doubleValue());
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
				NetworkImageRenderer r=new NetworkImageRenderer(w);
				w.addImageWindowTool(new NetworkImageTool(w,r));
				w.addImageWindowRenderer(r);
				}
			});
		}
	
	
	
	}
