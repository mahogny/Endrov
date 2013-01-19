package endrov.core.dbus;

import java.util.Collection;


import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;

/**
 * Endrov DBUS hub
 * 
 * @author Johan Henriksson
 *
 */
public class EndrovDBUS
	{
	private static String busName="org.endrov.service";
	
	/**
	 * To only try to connect once
	 */
	private static boolean use=true;
	
	/**
	 * Start the DBUS server
	 */
	public static boolean startServer()
		{
		if(!use)
			return false;
		
		try
			{
			DBusConnection conn = DBusConnection.getConnection(DBusConnection.SESSION);
			conn.requestBusName(busName);
			conn.exportObject("/", new DBusImpl());
			return true;
			}
		catch (DBusException e)
			{
			use=false;
			return false;
			}
		catch (NoClassDefFoundError e)
			{
			System.out.println("---- DBUS library not found");
			use=false;
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
		if(!use)
			return false;
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
				use=false;
				return false;
				}
			catch (NoClassDefFoundError e)
				{
				System.out.println("---- DBUS library not found");
				use=false;
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
