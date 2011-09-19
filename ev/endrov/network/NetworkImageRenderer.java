/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.network;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;
import java.util.*;

import javax.vecmath.*;


import endrov.imageWindow.*;
import endrov.imageset.EvChannel;
import endrov.imageset.EvStack;
import endrov.util.EvDecimal;
import endrov.util.ProgressHandle;
import endrov.util.Vector3i;

/**
 * Image window renderer of nuclei
 * @author Johan Henriksson
 *
 */
public class NetworkImageRenderer implements ImageWindowRenderer
	{
	public ImageWindow w;
	

	private Vector3d forcedStartingPoint=null;
	private SemiautoNetworkTracer lastAuto=null;
	private WeakReference<EvStack> lastStack=new WeakReference<EvStack>(null);
	private Vector3d[] previewPoints;

	
	public NetworkImageRenderer(ImageWindow w)
		{
		this.w=w;
		}


	
	public Collection<Network> getVisibleObjects()
		{
		return w.getRootObject().getObjects(Network.class);
		}
	

	
	/**
	 * Render nuclei
	 */
	public void draw(Graphics g)
		{
		
	
		EvDecimal currentFrame=w.getFrame();
		
		for(Network network:getVisibleObjects())
			{
			Network.NetworkFrame nf=network.frame.get(currentFrame);
			
			if(nf!=null)
				{
				//double screenZ=w.getZ().doubleValue();
				
				
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
						
						int sr=(int)w.scaleW2s(pr);
						
						g.drawOval(x-sr, y-sr, sr*2+1, sr*2+1);
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
	 * Project sphere onto plane
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

	/**
	 * Get stack to trace at the moment
	 */
	EvStack getCurrentStack()
		{
		EvDecimal frame=w.getFrame();
		EvChannel ch=w.getSelectedChannel();
		if(ch==null)
			return null;
		else
			{
			EvDecimal closestFrame=ch.closestFrame(frame);
			EvStack stack=ch.getStack(closestFrame);
			return stack;
			}
		}
	
	
	public boolean hasTracer()
		{
		return lastAuto!=null;
		}
	
	public void setForcedStartingPoint(Vector3d v)
		{
		if(!equalsHandlesNull(forcedStartingPoint,v))
			{
			forcedStartingPoint=v;
			lastAuto=null;
			lastStack=new WeakReference<EvStack>(null);
			}
		}

	/**
	 * Check equality, handles null objects
	 */
	private static <E> boolean equalsHandlesNull(E a, E b)
		{
		if(a==null)
			return b==null;
		else if(b==null)
			return false;
		else
			return a.equals(b);
		}
	
	/**
	 * Get the automatic tracer and ensure it is up to date
	 */
	public SemiautoNetworkTracer getAutoTracer(EvStack stack, Network.NetworkFrame nf)
		{
		//Reuse or start anew?
		SemiautoNetworkTracer auto;
		if(lastStack.get()==stack && lastAuto!=null)
			auto=lastAuto;
		else
			{
			lastStack=new WeakReference<EvStack>(stack);
			lastAuto=auto=new SemiautoNetworkTracer();
			}
		
		//Update costs. If there is a forced starting point then use it, otherwise use the network 
		System.out.println("Calculating");
		if(forcedStartingPoint==null)
			auto.preprocess(new ProgressHandle(), stack, SemiautoNetworkTracer.startingPointsFromFrame(stack, nf));
		else
			{
			System.out.println("--------------- calc from forced point ----");
			Vector3d v=stack.transformWorldImage(forcedStartingPoint);
			auto.preprocess(new ProgressHandle(), stack, Collections.singleton(new Vector3i((int)v.x,(int)v.y,(int)v.z)));
			}
		System.out.println("Calc done");
		return auto;
		}
	
	/**
	 * Remove tracer and associated memory
	 */
	public void removeTracer()
		{
		lastStack=new WeakReference<EvStack>(null);
		lastAuto=null;
		previewPoints=null;
		forcedStartingPoint=null;
		}

	
	
	/**
	 * Calculate preview points. This should only be done when the mouse moves as the window might be redrawn for other reasons
	 * @param e
	 */
	public void recalcTrace(MouseEvent e)
		{
		SemiautoNetworkTracer auto=lastAuto;
		
		EvStack stack=getCurrentStack();
		if(stack!=null)
			{
			Vector3i toPosImage=getMousePosImage(stack, e);
			List<Vector3i> points=auto.findPathTo(toPosImage.x, toPosImage.y, toPosImage.z);
			if(points!=null)
				{
				previewPoints=new Vector3d[points.size()];
				for(int i=0;i<points.size();i++)
					{
					Vector3i v=points.get(i);
					previewPoints[i]=stack.transformImageWorld(new Vector3d(v.x,v.y,v.z));
					}
				}
			}
		else
			previewPoints=null;
		}
	
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		ImageWindow.addImageWindowExtension(new ImageWindowExtension()
			{
			public void newImageWindow(ImageWindow w)
				{
				NetworkImageRenderer r=new NetworkImageRenderer(w);
				w.addImageWindowTool(new NetworkImageTool(w,r));
				w.addImageWindowRenderer(r);
				}
			});
		}
	
	
	

	}
