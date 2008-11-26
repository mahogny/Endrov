package endrov.imageset;

import java.awt.event.*;
import javax.swing.*;



import endrov.basicWindow.BasicWindow;
import endrov.imageWindow.*;
import endrov.util.EvDecimal;

public class ImagesetImageExtension implements ImageWindowExtension
	{
	
	
	public void newImageWindow(final ImageWindow w)
		{
		final JMenu miRemove=new JMenu("Remove");
		final JMenuItem miRemoveChannel=new JMenuItem("Channel");
		final JMenuItem miRemoveFrame=new JMenuItem("Frame");
		final JMenuItem miRemoveSlice=new JMenuItem("Slice");
		
		//Create menus
		BasicWindow.addMenuItemSorted(w.menuImage, miRemove);
		BasicWindow.addMenuItemSorted(miRemove, miRemoveChannel);
		BasicWindow.addMenuItemSorted(miRemove, miRemoveFrame);
		BasicWindow.addMenuItemSorted(miRemove, miRemoveSlice);
		
		//The listener
		ActionListener listener=new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
				{
				if(e.getSource()==miRemoveChannel)
					{
					String ch=w.getCurrentChannelName();
					if(JOptionPane.showConfirmDialog(null, "Do you really want to remove channel "+ch+"?","EV",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
						{
						w.getImageset().removeChannel(ch);
						BasicWindow.updateWindows();
						}
					}
				else if(e.getSource()==miRemoveFrame)
					{
					String ch=w.getCurrentChannelName();
					EvDecimal frame=w.frameControl.getFrame();
					
					if(JOptionPane.showConfirmDialog(null, "Do you really want to remove channel "+ch+", frame "+frame+"?","EV",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
						{
						w.getImageset().getChannel(ch).imageLoader.remove(frame);
						BasicWindow.updateWindows();
						}
					}
				else if(e.getSource()==miRemoveSlice)
					{
					String ch=w.getCurrentChannelName();
					EvDecimal frame=w.frameControl.getFrame();
					EvDecimal z=w.frameControl.getZ();
					
					if(JOptionPane.showConfirmDialog(null, "Do you really want to remove channel "+ch+", frame "+frame+", slice "+z+"?","EV",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
						{
						w.getImageset().getChannel(ch).imageLoader.get(frame).remove(z);
						BasicWindow.updateWindows();
						}
					}
				
				
				
				}	
			};
		

		//Add listeners
		miRemoveChannel.addActionListener(listener);
		miRemoveFrame.addActionListener(listener);
		miRemoveSlice.addActionListener(listener);
		}

	
	
	
	
	}
