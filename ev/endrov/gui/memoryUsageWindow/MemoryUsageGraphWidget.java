package endrov.gui.memoryUsageWindow;

import java.awt.Color;
import java.awt.Graphics;
import java.util.LinkedList;

import javax.swing.JPanel;

/**
 * Simple graph widget displaying memory usage over time
 * 
 * @author Johan Henriksson
 *
 */
public class MemoryUsageGraphWidget extends JPanel
	{
	private static final long serialVersionUID = 1L;
	private LinkedList<Long> usage=new LinkedList<Long>();
	private Runtime rt=Runtime.getRuntime();
	
	@Override
	public void paint(Graphics g)
		{
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.RED);
		
		long maxmem=rt.maxMemory();
		int h=getHeight();

		int x=0;
		boolean first=true;
		int lastY=0;
		for(long m:usage)
			{
			int nextY=h-1-(int)(m*h/maxmem);
			if(!first)
				g.drawLine(x-1, lastY, x, nextY);
			lastY=nextY;
			first=false;
			x++;
			}
		}

	
	public void measureMemory()
		{
		usage.add(rt.totalMemory());
		if(usage.size()>getWidth())
			usage.removeFirst();
		repaint();
		}
	}
