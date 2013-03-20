package endrov.core.dbus;

import java.io.File;

import endrov.starter.MW;

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
		
		MW.openFileOnLoad(new File(filename));
		
			
		return true;
		}
	}