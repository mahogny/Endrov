package endrov.dbus;

import java.io.File;

import javax.swing.JOptionPane;

import endrov.basicWindow.BasicWindow;
import endrov.data.EvData;
import endrov.ev.EV;

public class DBusImpl implements DBus
	{
	public boolean isRemote() { return false; }
	
	public boolean openFile(final String filename)
		{
		System.out.println("Asking to open "+filename);
		
		

		try
			{
			throw new Exception("here")	;
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		new Thread(new Runnable() { 
		public void run()
			{ 
			File f=new File(filename);
	
			EV.waitUntilStartedUp();
	
			EvData d=EvData.loadFile(f);
			if(d==null)
				JOptionPane.showMessageDialog(null, "Failed to open "+f);
			else
				{
				EvData.registerOpenedData(d);
				BasicWindow.updateLoadedFile(d);
				}
			}}).start(); 
		
		System.out.println("at end..................");
			
		return true;
		}
	}