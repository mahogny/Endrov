package endrov.flow.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.*;
import java.util.*;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.vecmath.Vector2d;

import endrov.ev.Tuple;
import endrov.flow.*;
import endrov.flow.basic.*;

/**
 * Panel showing flow
 * @author Johan Henriksson
 *
 */
public class FlowPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener//, KeyListener
	{
	static final long serialVersionUID=0;

	public int cameraX, cameraY;
	public Flow flow=new Flow();

	private Map<Tuple<FlowUnit, String>, ConnPoint> connPoint=new HashMap<Tuple<FlowUnit,String>, ConnPoint>();
	private int mouseLastDragX=0, mouseLastDragY=0;
	private FlowUnit holdingUnit=null;
	private DrawingConn drawingConn=null;

	/**
	 * Constructor
	 */
	public FlowPanel()
		{
		addMouseMotionListener(this);
		addMouseListener(this);
		addMouseWheelListener(this);
/*		setEnabled(true);
		setFocusable(true);
		addKeyListener(this);*/
		
		
		////////////
////////////////////////////////
////////////////////////////////
	
		/*
		FlowUnit fa=new FlowUnitIf();
		fa.x=200;
		fa.y=100;

		FlowUnit fb=new FlowUnitDiv();
		fb.x=200;
		fb.y=20;

		FlowUnit fc=new FlowUnitInput("chA");
		fc.x=100;
		fc.y=100;

		FlowUnit ff=new FlowUnitInput("chB");
		ff.x=100;
		ff.y=100;

		FlowUnit fd=new FlowUnitMap();
		fd.x=0;
		fd.y=0;
*/
		FlowUnit fe=new FlowUnitOutput("chDiv");
		fe.x=100;
		fe.y=140;
/*
		FlowUnit fg=new FlowUnitScript();
		fe.x=100;
		fe.y=140;
*/
		FlowUnit fh=new FlowUnitConstString("timelapse and homeobox");
		fh.x=250;
		fh.y=140;
/*
		flow.units.add(fa);
		flow.units.add(fb);
		flow.units.add(fc);
		flow.units.add(fd);
		flow.units.add(ff);
		flow.units.add(fg);
*/
		flow.units.add(fe);
		
		flow.units.add(fh);
		
////////////////////////////////
////////////////////////////////
////////////////////////////////
		
		}

	
	
	private static class ConnPoint
		{
		Vector2d pos;
		boolean isFrom;
		}
	
	public void drawConnPointLeft(Graphics g,FlowUnit unit, String arg, int x, int y)
		{
		g.setColor(Color.BLACK);
		g.fillRect(x-5, y-2, 5, 5);
		ConnPoint p=new ConnPoint();
		p.pos=new Vector2d(x-2,y);
		p.isFrom=false;
		connPoint.put(new Tuple<FlowUnit, String>(unit,arg), p);
		}
	
	public void drawConnPointRight(Graphics g,FlowUnit unit, String arg, int x, int y)
		{
		g.setColor(Color.BLACK);
		g.fillRect(x, y-2, 5, 5);
		ConnPoint p=new ConnPoint();
		p.pos=new Vector2d(x+2,y);
		p.isFrom=true;
		connPoint.put(new Tuple<FlowUnit, String>(unit,arg), p);
		}

	/**
	 * Draw connecting line between two points
	 */
	public void drawConnLine(Graphics g,Vector2d vFrom, Vector2d vTo)
		{
		int spacing=15;
		
		int midy=(int)(vFrom.y+vTo.y)/2;
		int x1=(int)(vFrom.x+vTo.x)/2;
		int x2=x1;
		if(vFrom.x>vTo.x)
			{
			if(x1<vFrom.x+spacing) x1=(int)vFrom.x+spacing;
			if(x2>vTo.x-spacing) x2=(int)vTo.x-spacing;
			}
		g.drawLine((int)vFrom.x, (int)vFrom.y, x1, (int)vFrom.y);
		g.drawLine(x1, (int)vFrom.y,x1, midy);
		g.drawLine(x1, midy, x2,midy);
		g.drawLine(x2, (int)vTo.y,x2, midy);
		g.drawLine((int)vTo.x, (int)vTo.y, x2, (int)vTo.y);
		}
	
	
	protected void paintComponent(Graphics g)
		{
		g.setColor(Color.WHITE);
		g.fillRect(0,0,getWidth(),getHeight());
		
		
		
		Graphics2D g2=(Graphics2D)g;
		g2.translate(-cameraX, -cameraY);
		
		
//		g.drawRect(x-panel.cameraX,y-panel.cameraY,30,30);
		
		//Draw all units
		for(FlowUnit u:flow.units)
			u.paint(g2, this);
		
		//All connection points should now be in the list
		//Draw connection arrows
		for(FlowConn conn:flow.conns)
			{
			Vector2d vFrom=connPoint.get(new Tuple<FlowUnit, String>(conn.fromUnit, conn.fromArg)).pos;
			Vector2d vTo=connPoint.get(new Tuple<FlowUnit, String>(conn.toUnit, conn.toArg)).pos;
			drawConnLine(g,vFrom,vTo);
			}
		
		if(drawingConn!=null)
			{
			ConnPoint p=connPoint.get(new Tuple<FlowUnit, String>(drawingConn.t.fst(), drawingConn.t.snd()));
			Vector2d vFrom=p.pos;
			Vector2d vTo=drawingConn.toPoint;
			if(!p.isFrom)
				{
				Vector2d v=vFrom;
				vFrom=vTo;
				vTo=v;
				}
			drawConnLine(g,vFrom,vTo);
			}
		
		
		g2.translate(cameraX, cameraY);
		
		}
	
	
	


	public void mouseWheelMoved(MouseWheelEvent e){}

	public void mouseDragged(MouseEvent e)
		{
		int dx=(e.getX()-mouseLastDragX);
		int dy=(e.getY()-mouseLastDragY);
		if(SwingUtilities.isRightMouseButton(e))
			{
			cameraX-=dx;
			cameraY-=dy;
			repaint();
			}
		else
			{
			if(holdingUnit!=null)
				{
				holdingUnit.x+=dx;
				holdingUnit.y+=dy;
				//TODO: containers
				repaint();
				}
			}
		
		if(drawingConn!=null)
			{
			int mx=e.getX()+cameraX;
			int my=e.getY()+cameraY;
			drawingConn.toPoint=new Vector2d(mx,my);
			repaint();
			}
		
		mouseLastDragX=e.getX();
		mouseLastDragY=e.getY();
		}


	public void mouseMoved(MouseEvent e)
		{
//		int sdx=(e.getX()-mouseLastMoveX);
//		int sdy=(e.getY()-mouseLastMoveY);
	//	mouseLastMoveX=e.getX();
	//	mouseLastMoveY=e.getY();
		}

/*
	public void keyPressed(KeyEvent e){}


	public void keyReleased(KeyEvent e){}


	public void keyTyped(KeyEvent e){}
*/

	public void mouseClicked(MouseEvent e){}


	public void mouseEntered(MouseEvent e){}


	public void mouseExited(MouseEvent e){}

	

	private static class DrawingConn
		{
		Tuple<FlowUnit,String> t;
		Vector2d toPoint;
		}
	
	public void mousePressed(MouseEvent e)
		{
		mouseLastDragX=e.getX();
		mouseLastDragY=e.getY();
		int mx=e.getX()+cameraX;
		int my=e.getY()+cameraY;

		boolean found=false;
		
		//Find connection point
		if(!found && SwingUtilities.isLeftMouseButton(e))
			{
			Tuple<FlowUnit,String> t=findHoverConnPoint(mx, my);
			if(t!=null)
				{
				drawingConn=new DrawingConn();
				drawingConn.t=t;
				drawingConn.toPoint=new Vector2d(mx,my);
				found=true;
				}
			}
		
		//Find component. TODO: containers?
		if(!found && SwingUtilities.isLeftMouseButton(e))
			for(FlowUnit u:flow.units)
				if(u.mouseHoverMoveRegion(mx,my))
					{
					holdingUnit=u;
					found=true;
					break;
					}
		
		
		
		}

	/**
	 * Find connection point near given coordinate
	 */
	private Tuple<FlowUnit,String> findHoverConnPoint(int mx, int my)
		{
		for(Map.Entry<Tuple<FlowUnit, String>, ConnPoint> entry:connPoint.entrySet())
			{
			Vector2d diff=new Vector2d(mx,my);
			diff.sub(entry.getValue().pos);
			if(diff.lengthSquared()<4*4)
				return entry.getKey();
			}
		return null;
		}
	

	public void mouseReleased(MouseEvent e)
		{
		holdingUnit=null;
		if(drawingConn!=null)
			{
			int mx=e.getX()+cameraX;
			int my=e.getY()+cameraY;
			Tuple<FlowUnit,String> t=findHoverConnPoint(mx, my);
			
			if(t!=null)
				{
				if(connPoint.get(drawingConn.t).isFrom != connPoint.get(t).isFrom)
					{
				
					if(connPoint.get(drawingConn.t).isFrom)
						flow.conns.add(new FlowConn(drawingConn.t.fst(),drawingConn.t.snd(), t.fst(), t.snd()));
					else 
						flow.conns.add(new FlowConn(t.fst(), t.snd(), drawingConn.t.fst(),t.snd()));

					//If a connection already exists at "to", remove it
					}
				}
			drawingConn=null;
			repaint();
			}
		
		}
	
	
	
	
	
	
	}
