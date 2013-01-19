/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowViewer2D.basicExtensions;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import endrov.gui.window.BasicWindow;
import endrov.undo.UndoOpNone;
import endrov.util.EvDecimal;
import endrov.windowViewer2D.Viewer2DWindow;
import endrov.windowViewer2D.Viewer2DTool;

//either send down variables or add accessors to imagewindow

/**
 * Get a screenshot of the current display
 * @author Johan Henriksson
 */
public class ImageWindowToolEditImage implements Viewer2DTool, ActionListener
	{
	private final JMenu miRemove=new JMenu("Remove");

	private final Viewer2DWindow w;

	//private final JMenuItem miRemoveChannel=new JMenuItem("Channel");
	private final JMenuItem miRemoveFrame=new JMenuItem("Frame");

	
	public ImageWindowToolEditImage(final Viewer2DWindow w)
		{
		this.w=w;
		}
	
	public JMenuItem getMenuItem()
		{
		//Create menus
		BasicWindow.addMenuItemSorted(miRemove, miRemove);
//		BasicWindow.addMenuItemSorted(miRemove, miRemoveChannel);
		BasicWindow.addMenuItemSorted(miRemove, miRemoveFrame);

		//Add listeners
	//	miRemoveChannel.addActionListener(this);
		miRemoveFrame.addActionListener(this);
		
		return miRemove;
		}
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==miRemoveFrame)
			{ 
			final String ch=w.getCurrentChannelName();
			final EvDecimal frame=w.getFrame();
			
			if(BasicWindow.showConfirmYesNoDialog("Do you really want to remove (channel "+ch+", frame "+frame+")? This can not be undone"))
				{
				new UndoOpNone("Remove frame")
					{
					public void redo()
						{
						w.getCurrentChannel().removeStack(frame);
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
