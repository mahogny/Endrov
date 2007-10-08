package evplugin.filter;

import javax.swing.*;
import evplugin.basicWindow.*;
import evplugin.imageWindow.*;

public class FilterImageExtension implements ImageWindowExtension
	{

	public void newImageWindow(final ImageWindow w)
		{
		JMenu miOnImageset=new JMenu("On imageset");
		JMenu miOnChannel=new JMenu("On channel");
		JMenu miOnFrame=new JMenu("On frame");
		JMenu miOnSlice=new JMenu("On slice");
		JMenu miOnROI=new JMenu("On ROI");
		//final JMenuItem miRemoveChannel=new JMenuItem("Channel");
		
		
		//Create menus
		BasicWindow.addMenuItemSorted(w.menuImage, miOnImageset);
		BasicWindow.addMenuItemSorted(w.menuImage, miOnChannel);
		BasicWindow.addMenuItemSorted(w.menuImage, miOnFrame);
		BasicWindow.addMenuItemSorted(w.menuImage, miOnSlice);
		BasicWindow.addMenuItemSorted(w.menuImage, miOnROI);
//		BasicWindow.addMenuItemSorted(miRemove, miRemoveSlice);
		
		//The listener
		/*
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
					int frame=(int)w.frameControl.getFrame();
					
					if(JOptionPane.showConfirmDialog(null, "Do you really want to remove channel "+ch+", frame "+frame+"?","EV",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
						{
						w.getImageset().getChannel(ch).imageLoader.remove(frame);
						BasicWindow.updateWindows();
						}
					}
				else if(e.getSource()==miRemoveSlice)
					{
					String ch=w.getCurrentChannelName();
					int frame=(int)w.frameControl.getFrame();
					int z=w.frameControl.getZ();
					
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
				*/
		}

	
	
	
	
	}
