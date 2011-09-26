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
	public static void startServer()
		{
		try
			{
			DBusConnection conn = DBusConnection.getConnection(DBusConnection.SESSION);
			conn.requestBusName(busName);
			conn.exportObject("/", new DBusImpl());
			}
		catch (DBusException e)
			{
			e.printStackTrace();
			}
		}
	
	
	
	private static DBusConnection conn;
	private static DBus info;
	
	/**
	 * Connect to dbus server
	 */
	public static boolean connect()
		{
		if(info!=null)
			return true;
		else
			{
			try
				{
				conn = DBusConnection.getConnection(DBusConnection.SESSION);
				info = conn.getRemoteObject(busName, "/", DBus.class);
				System.out.println("    info "+info);
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
					info.openFile(f);
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
