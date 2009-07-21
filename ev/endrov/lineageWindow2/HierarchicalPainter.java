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
		public double zoomX, zoomY;
		
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

		}
	
	
	/**
	 * Bounding box around something to draw
	 * @author Johan Henriksson
	 *
	 */
	/*
	public static class BoundingBox
		{
		public double x1,y1,x2,y2;

		public BoundingBox(double x1, double y1, double x2, double y2)
			{
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
			}
		}
	*/
	
	/**
	 * One drawable node in the hierarchical tree
	 * @author Johan Henriksson
	 *
	 */
	public static abstract class DrawNode
		{
		public double bbx1,bby1,bbx2,bby2;
		public boolean hasBB=false;
		
		public List<DrawNode> subNodes=new LinkedList<DrawNode>();
		
		/**
		 * Add a node. Enlarge bounding box if needed
		 */
		public void addSubNode(DrawNode node)
			{
			subNodes.add(node);
			if(hasBB)
				{
				if(bbx1>node.bbx1)
					bbx1=node.bbx1;
				if(bby1>node.bby1)
					bby1=node.bby1;
				if(bbx2<node.bbx2)
					bbx2=node.bbx2;
				if(bby2<node.bby2)
					bby2=node.bby2;
				}
			else
				{
				bbx1=node.bbx1;
				bby1=node.bby1;
				bbx2=node.bbx2;
				bby2=node.bby2;
				hasBB=true;
				}
			}
		
		public abstract void paint(Graphics g, double width, double height, Camera cam);
		}
	
	
	public List<DrawNode> topNodes=new LinkedList<DrawNode>();
	
	/**
	 * Paint everything
	 * @param width  Width of output g 
	 * @param height Height of output g
	 * @param cam    Camera
	 */
	public void paint(Graphics g, double width, double height, Camera cam)
		{
		double x1=cam.toWorldX(0);
		double y1=cam.toWorldY(0);
		double x2=cam.toWorldX(width);
		double y2=cam.toWorldY(height);
		for(DrawNode n:topNodes)
			paint_(g, width, height, cam, x1, y1, x2, y2, n);
		}
	
	public void paint_(Graphics g, double width, double height, Camera cam, 
			double x1, double y1, double x2, double y2, DrawNode n)
		{
		
		if(n.bbx1>x2 || n.bby1>y2 || n.bbx2<x1 || n.bby2<y2)
			; //Outside
		else
			{
			//Draw this component
			n.paint(g, width, height, cam);
			
			//Recurse children
			for(DrawNode c:n.subNodes)
				paint_(g, width, height, cam, x1, y1, x2, y2, c);
			}
		}
	
	}
