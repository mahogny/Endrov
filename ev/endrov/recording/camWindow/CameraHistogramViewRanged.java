package endrov.recording.camWindow;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.LinkedList;
import java.util.List;

import endrov.imageset.EvPixels;


/**
 * Histogram extended with range 
 * @author Johan Henriksson
 *
 */
public class CameraHistogramViewRanged extends CameraHistogramView implements MouseListener, MouseMotionListener
	{
	private static final long serialVersionUID = 1L;

	public int lower=0;
	public int upper=255;
	
	private Color rangeBarColor=Color.RED;
	
	public CameraHistogramViewRanged()
		{
		addMouseListener(this);
		addMouseMotionListener(this);
		}
	
	@Override
	protected void paintComponent(Graphics g)
		{
		super.paintComponent(g);
		int xLower=toScreenX(lower);
		int xUpper=toScreenX(upper);
		g.setColor(rangeBarColor);
		vertStitch(g, xLower);
		vertStitch(g, xUpper);
		}
	
	/**
	 * Transform to screen coordinates
	 */
	private int toScreenX(int x)
		{
		int screenWidth=getWidth();
		return x*screenWidth/rangeMax;
		}

	/**
	 * Transform to world coordinates
	 */
	private int toWorldX(int x)
		{
		int screenWidth=getWidth();
		return x*rangeMax/screenWidth;
		}

	/**
	 * Vertical stitched line
	 */
	private void vertStitch(Graphics g, int x)
		{
		int h=getHeight();
		for(int y=0;y<h;y+=4)
			g.drawLine(x, y, x, y);
		}
	
	/**
	 * Calculate range automatically from image
	 */
	public void calcAutoRange(EvPixels p)
		{
		calcAutoRange(p.convertToInt(true).getArrayInt());
		}
	
	/**
	 * Calculate range automatically from image
	 */
	public void calcAutoRange(int[] p)
		{
		int min=p[0], max=p[0];
		for(int i=1;i<p.length;i++)
			{
			int v=p[i];
			if(v<min)
				min=v;
			else if(v>max)
				max=v;
			}
		lower=min;
		upper=max;
		}

	public void mouseClicked(MouseEvent e)
		{
		}

	public void mouseEntered(MouseEvent e)
		{
		}

	public void mouseExited(MouseEvent e)
		{
		}

	public void mousePressed(MouseEvent e)
		{
		moveLimit(e);
		}

	public void mouseReleased(MouseEvent e)
		{
		}

	public void mouseDragged(MouseEvent e)
		{
		moveLimit(e);
		}

	public void mouseMoved(MouseEvent arg0)
		{
		}
	
	/**
	 * Move one limit using the mouse 
	 */
	public void moveLimit(MouseEvent e)
		{
		int mx=toScreenX(e.getX());
		int xLower=toScreenX(lower);
		int xUpper=toScreenX(upper);
		
		if(Math.abs(mx-xLower) < Math.abs(mx-xUpper))
			lower=toWorldX(mx);
		else
			upper=toWorldX(mx);
		
		if(lower<0)
			lower=0;
		if(upper>rangeMax)
			upper=rangeMax;
		
		repaint();
		
		for(ActionListener l:actionListeners)
			l.actionPerformed(new ActionEvent(this, 0, "rangeChanged"));
		}
	
	private List<ActionListener> actionListeners=new LinkedList<ActionListener>();
	
	public void addActionListener(ActionListener l)
		{
		actionListeners.add(l);
		}
	
	
	
	}
