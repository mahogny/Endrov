package endrov.basicWindow;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Attach a hidable panel to a center panel
 * @author Johan Henriksson
 */
public class EvHidableSidePane extends JPanel
	{
	static final long serialVersionUID=0; 
	public static int preferWidth=15;
	
	private boolean visible=true;

	
	private Component /*center,*/ right;
	private final JPanel rest=new JPanel(new BorderLayout());


	JComponent toggleButton=new HideButton();
	
	/**
	 * Check if hidable panel is visible
	 */
	public boolean getPanelVisible()
		{
		return visible;
		}
	
	/**
	 * Set if hidable panel is visible
	 */
	public void setPanelVisible(boolean v)
		{
		if(v)
			rest.add(right,BorderLayout.CENTER);
		else
			rest.remove(right);
		visible=v;
		validate();
		toggleButton.repaint();
		}
	
	
	private class HideButton extends JPanel implements MouseListener
		{
		static final long serialVersionUID=0; 
		public HideButton()
			{
			setPreferredSize(new Dimension(preferWidth,1));
			addMouseListener(this);
			}
		protected void paintComponent(Graphics g)
			{
			super.paintComponent(g);
			int h=getHeight();
			g.setColor(Color.BLACK);
			int part=preferWidth/3;
			int part2=2*part;
			int dy=part*4;
			int x1=getPanelVisible() ? part  : part2;
			int x2=getPanelVisible() ? part2 : part;
			int y=h/2-dy*3/2;
			for(int i=0;i<3;i++)
				{
				g.drawLine(x1, y, x2, y+part);
				g.drawLine(x2, y+part, x1, y+part2);
				y+=dy;
				}
			/*
			for(int y=part;y+part2<h;y+=dy)
				{
				}*/
			}
		public void mouseClicked(MouseEvent e)
			{
			setPanelVisible(!getPanelVisible());
			}
		public void mouseEntered(MouseEvent e){}
		public void mouseExited(MouseEvent e){}
		public void mousePressed(MouseEvent e){}
		public void mouseReleased(MouseEvent e){}
		}
	

	
	
	public EvHidableSidePane(final Component center, final Component right, boolean visible)
		{
		this.right=right;
		setLayout(new BorderLayout());
		rest.add(toggleButton,BorderLayout.WEST);
//		rest.add(right,BorderLayout.CENTER);
		add(center,BorderLayout.CENTER);
		add(rest,BorderLayout.EAST);

		setPanelVisible(visible);
		}
	
	
	
	}
