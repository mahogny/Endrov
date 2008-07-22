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
	
	Flow flow=new Flow();
	
	public FlowPanel()
		{
		addMouseMotionListener(this);
		addMouseListener(this);
		addMouseWheelListener(this);
/*		setEnabled(true);
		setFocusable(true);
		addKeyListener(this);*/
		
		
		////////////
		
		FlowUnit fa=new FlowUnitIf();
		fa.x=200;
		fa.y=100;

		FlowUnit fb=new FlowUnitDiv();
		fb.x=200;
		fb.y=20;

		FlowUnit fc=new FlowUnitInput();
		fc.x=100;
		fc.y=100;

		FlowUnit fd=new FlowUnitMap();
		fd.x=0;
		fd.y=0;

		FlowUnit fe=new FlowUnitOutput();
		fe.x=100;
		fe.y=140;

		flow.units.add(fa);
		flow.units.add(fb);
		flow.units.add(fc);
		flow.units.add(fd);
		flow.units.add(fe);
		flow.conns.add(new FlowConn(fa,"out",fb,"A"));

		
		}

	
	
	private Map<Tuple<FlowUnit, String>, Vector2d> connPoint=new HashMap<Tuple<FlowUnit,String>, Vector2d>();
	
	public void drawConnPointLeft(Graphics g,FlowUnit unit, String arg, int x, int y)
		{
		g.setColor(Color.BLACK);
		g.fillRect(x-5, y-2, 5, 5);
		connPoint.put(new Tuple<FlowUnit, String>(unit,arg), new Vector2d(x-2,y));
		}
	
	public void drawConnPointRight(Graphics g,FlowUnit unit, String arg, int x, int y)
		{
		g.setColor(Color.BLACK);
		g.fillRect(x, y-2, 5, 5);
		connPoint.put(new Tuple<FlowUnit, String>(unit,arg), new Vector2d(x+2,y));
		}

	/**
	 * Draw connecting line between two points
	 */
	public void drawConnLine(Graphics g,Vector2d vFrom, Vector2d vTo)
		{
		int spacing=20;
		
		int midy=(int)(vFrom.y+vTo.y)/2;
		int x1=(int)(vFrom.x+vTo.x)/2;
		int x2=x1;
		if(x1<vFrom.x+spacing) x1=(int)vFrom.x+spacing;
		if(x2>vTo.x-spacing) x2=(int)vTo.x-spacing;
			
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
			Vector2d vFrom=connPoint.get(new Tuple<FlowUnit, String>(conn.fromUnit, conn.fromArg));
			Vector2d vTo=connPoint.get(new Tuple<FlowUnit, String>(conn.toUnit, conn.toArg));
			drawConnLine(g,vFrom,vTo);
			}
		
		g2.translate(cameraX, cameraY);
		
		}
	
	
	
	private int mouseLastDragX=0, mouseLastDragY=0;
//	public int mouseLastMoveX, mouseLastMoveY;


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


	public void mousePressed(MouseEvent e)
		{
		mouseLastDragX=e.getX();
		mouseLastDragY=e.getY();
		}


	public void mouseReleased(MouseEvent e){}
	
	
	
	
	
	
	}
