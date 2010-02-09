/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.basicWindow;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import endrov.util.JImageButton;

/**
 * Drop-down button
 * @author Johan Henriksson
 *
 */
public abstract class EvDropDownButton extends JImageButton implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	JPopupMenu popup=null;

	public abstract JPopupMenu createPopup();

	public EvDropDownButton(Icon icon, String tooltip)
		{
		super(icon,tooltip);
		addActionListener(this);
		}
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==this)
			{
			if(popup==null)
				{
				popup=createPopup();
				popup.pack();
				Point thisPos=getLocationOnScreen();
				int x=(int)thisPos.getX();
				int y=(int)thisPos.getY()-getHeight();
				//might have to move popup if outside
				//might have to be heavy for modelw

				popup.setLightWeightPopupEnabled(false);

				//Additional action listeners: close down popup
				for(Component d:popup.getComponents())
					if(d instanceof JMenuItem)
						addActionListenerToAll((JMenuItem)d, new ActionListener(){
							public void actionPerformed(ActionEvent e){closePopup();}
						});

				
				popup.show(this, x-(int)thisPos.getX(), y-(int)thisPos.getY());
				}
			else
				closePopup();
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
