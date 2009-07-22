package endrov.lineageWindow2;

import java.awt.Graphics;
import java.util.LinkedList;
import java.util.List;

/**
 * Hierarchical 2D rendering system. When drawing really large charts that do not
 * fit within the screen, the most important optimization is culling of objects that
 * are outside the screen. This class does it by storing objects in a tree such that
 * a sub-object is within the bounding box of the parent. This logarithmically reduces
 * the number of objects that has to be considered for rendering.
 * 
 * @author Johan Henriksson
 *
 */
public class HierarchicalPainter
	{

	/**
	 * Location of camera and current zoom
	 * @author Johan Henriksson
	 *
	 */
	public static class Camera
		{
		public double cameraX, cameraY;
		public double zoomX=1, zoomY=1;
		
		public int toScreenX(double x)
			{
			return (int)(x*zoomX-cameraX);
			}
		
		public int toScreenY(double y)
			{
			return (int)(y*zoomX-cameraY);
			}
		
		public double toWorldX(double x)
			{
			return (cameraX+x)/zoomX;
			}

		public double toWorldY(double y)
			{
			return (cameraY+y)/zoomY;
			}

		public double scaleScreenDistX(double x)
			{
			return x;
			}
		public double scaleScreenDistY(double y)
			{
			return y;
			}
		
		@Override
		public String toString()
			{
			return "( xy "+cameraX+","+cameraY+" zoom "+zoomX+","+zoomY+")";
			}
		
		}
	
	
	/**
	 * Bounding box around something to draw
	 * @author Johan Henriksson
	 *
	 */
	public static class BoundingBox
		{
		public final double x1,y1,x2,y2;

		public BoundingBox(double x1, double y1, double x2, double y2)
			{
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
			}
		
		public BoundingBox enlarge(BoundingBox bb)
			{
			return new BoundingBox(Math.min(x1,bb.x1), Math.min(y1,bb.y1),Math.max(x2,bb.x2),Math.max(y2, bb.y2));
			}

		public boolean isDisjunct(BoundingBox bb)
			{
			return bb.x1>x2 || bb.y1>y2 || bb.x2<x1 || bb.y2<y2;
			}
		
		public boolean isOverlapping(BoundingBox bb)
			{
			return !isDisjunct(bb);
			}
		
		public String toString()
			{
			return "("+x1+","+y1+" , "+x2+","+y2+")";
			}
		}
	
	/**
	 * One drawable node in the hierarchical tree
	 * @author Johan Henriksson
	 *
	 */
	public static abstract class DrawNode
		{
		public BoundingBox bb=null;
		
		public List<DrawNode> subNodes=new LinkedList<DrawNode>();
		
		/**
		 * Add a node. Enlarge bounding box if needed. This should
		 * only be done once the node has all components add to it in turn!
		 */
		public void addSubNode(DrawNode node)
			{
			subNodes.add(node);
			if(bb==null)
				bb=node.bb;
			else
				bb=bb.enlarge(node.bb);
			}
		
		
		
		public DrawNode(double bbx1, double bby1, double bbx2, double bby2)
			{
			bb=new BoundingBox(bbx1,bby1,bbx2,bby2);
			}

		private DrawNode(){}

		public abstract void paint(Graphics g, double width, double height, Camera cam);
		}

	/**
	 * Node that doesn't draw anything
	 * @author Johan Henriksson
	 */
	public static class DrawNodeContainer extends DrawNode
		{
		public void paint(Graphics g, double width, double height, Camera cam)
			{
			}
		}
	
	
	public List<DrawNode> topNodes=new LinkedList<DrawNode>();
	
	
	
	/**
	 * Get a bounding box around all objects, or null if there are no objects
	 */
	public BoundingBox getTotalBoundingBox()
		{
		BoundingBox bb=null;
		for(DrawNode dn:topNodes)
			if(dn.bb!=null)
				{
				if(bb==null)
					bb=dn.bb;
				else
					bb=bb.enlarge(dn.bb);
				}
		return bb;
		/*
		Iterator<DrawNode> itbb=topNodes.iterator();
		if(itbb.hasNext())
			{
			BoundingBox bb=itbb.next().bb;
			while(itbb.hasNext())
				bb=bb.enlarge(itbb.next().bb);
			return bb;
			}
		else
			return null;
			*/
		}
	
	
	
	
	/**
	 * Paint everything
	 * @param width  Width of output g 
	 * @param height Height of output g
	 * @param cam    Camera
	 */
	public void paint(Graphics g, double width, double height, Camera cam)
		{
		BoundingBox screen=new BoundingBox(cam.toWorldX(0),cam.toWorldY(0),cam.toWorldX(width),cam.toWorldY(height));
		int totDraw=0;
		for(DrawNode n:topNodes)
			totDraw+=paint_(g, width, height, cam, screen, n);
		System.out.println("Drawn regions: "+totDraw);
		}
	
	public int paint_(Graphics g, double width, double height, Camera cam, 
			BoundingBox screenBB, DrawNode n)
		{
		int totDraw=0;
		if(screenBB.isOverlapping(screenBB))
			{
			//Draw this component
			n.paint(g, width, height, cam);
			totDraw++;
			
			//Recurse children
			for(DrawNode c:n.subNodes)
				totDraw+=paint_(g, width, height, cam, screenBB, c);
			}
		return totDraw;
		}
	
	}
