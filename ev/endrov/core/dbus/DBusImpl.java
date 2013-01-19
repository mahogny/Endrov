package endrov.core.dbus;

import java.io.File;

import javax.swing.JOptionPane;

import endrov.core.EndrovCore;
import endrov.data.EvData;
import endrov.gui.window.EvBasicWindow;

/**
 * Implementation of the Endrov DBUS connection
 * 
 * @author Johan Henriksson
 *
 */
public class DBusImpl implements DBus
	{
	public boolean isRemote() { return false; }
	
	public boolean openFile(final String filename)
		{
		System.out.println("Via DBUS, asking to open "+filename);
		
		new Thread(new Runnable() { 
		public void run()
			{ 
			File f=new File(filename);
	
			EndrovCore.waitUntilStartedUp();
	
			EvData d=EvData.loadFile(f);
			if(d==null)
				JOptionPane.showMessageDialog(null, "Failed to open "+f);
			else
				{
				EvData.registerOpenedData(d);
				EvBasicWindow.updateLoadedFile(d);
				}
			}}).start(); 
			
		return true;
		}
	}