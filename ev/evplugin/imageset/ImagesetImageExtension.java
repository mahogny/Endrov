package evplugin.imageset;

import java.awt.event.*;
import javax.swing.*;



import evplugin.basicWindow.BasicWindow;
import evplugin.imageWindow.*;

public class ImagesetImageExtension implements ImageWindowExtension
	{
	JMenuItem miRemoveChannel=new JMenuItem("Remove channel");
	
	public void newImageWindow(final ImageWindow w)
		{
		//Create menus
		w.menuImage.add(miRemoveChannel);

		
		//The listener
		ActionListener listener=new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
				{
				if(e.getSource()==miRemoveChannel)
					{
					String ch=w.getCurrentChannelName();
					if(JOptionPane.showConfirmDialog(null, "Do you really want to remove channel "+ch+"?")==JOptionPane.YES_OPTION)
						{
						w.getImageset().removeChannel(ch);
						BasicWindow.updateWindows();
						}
					}
				
				}	
			};
		

		//Add listeners
		miRemoveChannel.addActionListener(listener);
		}

	
	
	
	
	}
