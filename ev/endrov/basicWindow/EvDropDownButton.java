package endrov.basicWindow;

import java.awt.*;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * Drop-down button
 * @author Johan Henriksson
 *
 */
public abstract class EvDropDownButton extends JButton implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	JPopupMenu popup=null;
	
	public EvDropDownButton(String title)
		{
		super(title);
		addActionListener(this);
		}
	
	public abstract JPopupMenu createPopup();
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==this)
			if(popup==null)
				{
				popup=createPopup();
				popup.pack();
				Point thisPos=getLocationOnScreen();
				popup.setLocation((int)thisPos.getX(), (int)thisPos.getY()-getHeight());
				//might have to move popup if outside
				popup.setVisible(true);
				//might have to be heavy for modelw

				popup.setLightWeightPopupEnabled(false);

				//Additional action listeners: close down popup
				for(Component d:popup.getComponents())
					if(d instanceof JMenuItem)
						addActionListenerToAll((JMenuItem)d, new ActionListener(){
							public void actionPerformed(ActionEvent arg0)
								{
								closePopup();
								}
						});
				

				}
			else
				closePopup();
		else
			{
			System.out.println("foo");
			}
		
		}
	
	private void closePopup()
		{
		popup.setVisible(false);
		popup=null;
		}
	
	private static void addActionListenerToAll(JMenuItem c, ActionListener list)
		{
		c.addActionListener(list);
		for(Component d:c.getComponents())
			if(d instanceof JMenuItem)
				addActionListenerToAll((JMenuItem)d, list);
		}
	
	}
