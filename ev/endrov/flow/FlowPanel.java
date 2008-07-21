package endrov.flow;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.*;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Panel with flow
 * @author Johan Henriksson
 *
 */
public class FlowPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener//, KeyListener
	{
	static final long serialVersionUID=0;

	public int cameraX, cameraY;
	
	
	public FlowPanel()
		{
		addMouseMotionListener(this);
		addMouseListener(this);
		addMouseWheelListener(this);
/*		setEnabled(true);
		setFocusable(true);
		addKeyListener(this);*/
		}


	protected void paintComponent(Graphics g)
		{
		g.setColor(Color.WHITE);
		g.fillRect(0,0,getWidth(),getHeight());
		
		Vector<FlowUnit> units=new Vector<FlowUnit>();
//		units.add(new FlowUnitInput());
//		units.add(new FlowUnitDiv());
		
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

		units.add(fa);
		units.add(fb);
		units.add(fc);
		units.add(fd);
		units.add(fe);
		
		Graphics2D g2=(Graphics2D)g;
		g2.translate(-cameraX, -cameraY);
		
		
//		g.drawRect(x-panel.cameraX,y-panel.cameraY,30,30);
		
		for(FlowUnit u:units)
			u.paint(g2, this);
		
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
