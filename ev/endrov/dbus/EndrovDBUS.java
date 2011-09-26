package endrov.dbus;

import java.util.Collection;


import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;


public class EndrovDBUS
	{
	private static String busName="org.endrov.service";
	
	/**
	 * Start the DBUS server
	 */
	public static boolean startServer()
		{
		try
			{
			DBusConnection conn = DBusConnection.getConnection(DBusConnection.SESSION);
			conn.requestBusName(busName);
			conn.exportObject("/", new DBusImpl());
			return true;
			}
		catch (DBusException e)
			{
			return false;
			}
		}
	
	
	
	private static DBusConnection conn;
	private static DBus object;
	
	/**
	 * Connect to dbus server
	 */
	public static boolean connect()
		{
		if(object!=null)
			return true;
		else
			{
			try
				{
				conn = DBusConnection.getConnection(DBusConnection.SESSION);
				object = conn.getRemoteObject(busName, "/", DBus.class);
				}
			catch (DBusException e)
				{
				e.printStackTrace();
				return false;
				}
			return true;
			}
		}
	
	
	public static boolean openFile(Collection<String> filename)
		{
		if(connect())
			{
			for(String f:filename)
				try
					{
					object.openFile(f);
					}
				catch (Exception e)
					{
					e.printStackTrace();
					return false;
					}
			return true;
			}
		return false;
		}
	
	}
