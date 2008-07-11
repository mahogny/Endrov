package endrov.flow;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

public class FlowPanel extends JPanel
	{
	static final long serialVersionUID=0;

	public int cameraX, cameraY;
	
	
	public FlowPanel()
		{
		
		}


	protected void paintComponent(Graphics g)
		{
		g.setColor(Color.WHITE);
		g.fillRect(0,0,getWidth(),getHeight());
		
		
		}
	
	
	
	
	
	
	}
