/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageWindow.tools;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import endrov.basicWindow.BasicWindow;
import endrov.imageWindow.ImageWindow;
import endrov.imageWindow.ImageWindowTool;
import endrov.undo.UndoOpNone;
import endrov.util.EvDecimal;

//either send down variables or add accessors to imagewindow

/**
 * Get a screenshot of the current display
 * @author Johan Henriksson
 */
public class ImageWindowToolEditImage implements ImageWindowTool, ActionListener
	{
	private final JMenu miRemove=new JMenu("Remove");

	private final ImageWindow w;

	private final JMenuItem miRemoveChannel=new JMenuItem("Channel");
	private final JMenuItem miRemoveFrame=new JMenuItem("Frame");

	
	public ImageWindowToolEditImage(final ImageWindow w)
		{
		this.w=w;
		}
	
	public JMenuItem getMenuItem()
		{
		//Create menus
		BasicWindow.addMenuItemSorted(miRemove, miRemove);
		BasicWindow.addMenuItemSorted(miRemove, miRemoveChannel);
		BasicWindow.addMenuItemSorted(miRemove, miRemoveFrame);

		//Add listeners
		miRemoveChannel.addActionListener(this);
		miRemoveFrame.addActionListener(this);
		
		return miRemove;
		}
	
	public void actionPerformed(ActionEvent e)
		{
		
		if(e.getSource()==miRemoveChannel)
			{
			final String ch=w.getCurrentChannelName();
			if(JOptionPane.showConfirmDialog(null, "Do you really want to remove (channel "+ch+")? This can not be undone","EV",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
				{
				new UndoOpNone("Remove channel")
					{
					public void redo()
						{
						w.getImageset().removeChannel(ch);
						BasicWindow.updateWindows();
						}
					}.execute();
				}
			}
		else if(e.getSource()==miRemoveFrame)
			{ 
			final String ch=w.getCurrentChannelName();
			final EvDecimal frame=w.frameControl.getFrame();
			
			if(JOptionPane.showConfirmDialog(null, "Do you really want to remove (channel "+ch+", frame "+frame+")? This can not be undone","EV",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
				{
				new UndoOpNone("Remove frame")
					{
					public void redo()
						{
						w.getImageset().getChannel(ch).imageLoader.remove(frame);
						BasicWindow.updateWindows();
						}
					}.execute();
				}
			}
		
		}
	
	public void deselected(){}
	
	public void mouseClicked(MouseEvent e, Component invoker){}
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
	public void mouseDragged(MouseEvent e, int dx, int dy){}
	public void paintComponent(Graphics g){}
	public void mouseMoved(MouseEvent e, int dx, int dy){}
	public void keyPressed(KeyEvent e){}
	public void keyReleased(KeyEvent e){}
	public void mouseExited(MouseEvent e){}
	}
