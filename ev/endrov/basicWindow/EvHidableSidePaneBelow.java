/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.basicWindow;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Attach a hidable panel to a center panel
 * @author Johan Henriksson
 */
public class EvHidableSidePaneBelow extends JPanel
	{
	static final long serialVersionUID=0; 
	public static int preferHeight=15;
	
	private boolean visible=true;

	
	private Component /*center,*/ extraPanel;
	private final JPanel rest=new JPanel(new BorderLayout());


	private JComponent toggleButton=new HideButton();
	
	/**
	 * Check if hidable panel is visible
	 */
	public boolean isPanelVisible()
		{
		return visible;
		}
	
	/**
	 * Set if hidable panel is visible
	 */
	public void setPanelVisible(boolean v)
		{
		if(v)
			rest.add(extraPanel,BorderLayout.CENTER);
		else
			rest.remove(extraPanel);
		visible=v;
		validate();
		toggleButton.repaint();
		
		for(ActionListener l:actionListeners)
			l.actionPerformed(new ActionEvent(this, 0, "toggledPanel"));
		}
	
	
	private class HideButton extends JPanel implements MouseListener
		{
		static final long serialVersionUID=0; 
		public HideButton()
			{
			setPreferredSize(new Dimension(1,preferHeight));
			addMouseListener(this);
			}
		protected void paintComponent(Graphics g)
			{
			super.paintComponent(g);
			int w=getWidth();
			g.setColor(Color.BLACK);
			int part=preferHeight/3;
			int part2=2*part;
			
			int dx=part*4;
			int y1=isPanelVisible() ? part  : part2;
			int y2=isPanelVisible() ? part2 : part;
			int x=w/2-dx*3/2;
			
			for(int i=0;i<3;i++)
				{
				g.drawLine(x, y1, x+part, y2);
				g.drawLine(x+part, y2, x+part2, y1);
				x+=dx;
				}
			/*
			for(int y=part;y+part2<h;y+=dy)
				{
				}*/
			}
		public void mouseClicked(MouseEvent e)
			{
			setPanelVisible(!isPanelVisible());
			}
		public void mouseEntered(MouseEvent e){}
		public void mouseExited(MouseEvent e){}
		public void mousePressed(MouseEvent e){}
		public void mouseReleased(MouseEvent e){}
		}
	

	
	
	public EvHidableSidePaneBelow(final Component center, final Component below, boolean visible)
		{
		this.extraPanel=below;
		setLayout(new BorderLayout());
		rest.add(toggleButton,BorderLayout.NORTH);
//		rest.add(right,BorderLayout.CENTER);
		add(center,BorderLayout.CENTER);
		add(rest,BorderLayout.SOUTH);

		setPanelVisible(visible);
		}
	

	private List<ActionListener> actionListeners=new LinkedList<ActionListener>();
	
	public void addActionListener(ActionListener l)
		{
		actionListeners.add(l);
		}

	
	}
