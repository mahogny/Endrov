package endrov.starter;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.exceptions.DBusException;

public class EndrovDBUS //implements DBusSigHandler<EndrovDBUS.Sig>
	{
	/*
	public static class Sig extends DBusSignal implements DBusSerializable
		{
		public String filename;
		
		public Sig(String path, String name, String filename) throws DBusException
			{
			super(path, name);
			// TODO Auto-generated constructor stub
			}

		public Object[] serialize() throws DBusException
			{
			return new Object[]{filename};
			}
		
		
		public void deserialize(Object fname)
			{
			filename=(String)fname;
			}
		
		}
		*/
	
	public static interface Interface extends DBusInterface
		{
		public void openFile(String filename);
		}
	
	
	public static class Impl implements Interface
		{
		public boolean isRemote() { return false; }
		
		public void openFile(String filename)
			{
			System.out.println("Asking to open "+filename);
			}
		}
	
	public static void startServer()
		{
		try
			{
			DBusConnection conn = DBusConnection.getConnection(DBusConnection.SESSION);
			
			conn.exportObject("endrov.starter.EndrovDBUS.Interface", new Impl());
			
			//DirectConnection dc = new DirectConnection("unix:path=/tmp/dbus-ABCXYZ,listen=true");
			
			//conn.addSigHandler(EndrovDBUS.Sig.class, new EndrovDBUS());

			
			

			
			//conn.disconnect();
			}
		catch (DBusException e)
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		}
	
	
	public boolean send(String filename)
		{
		try
			{
			DBusConnection conn = DBusConnection.getConnection(DBusConnection.SESSION);
			Interface info = conn.getRemoteObject("endrov.starter.EndrovDBUS.Interface", "/", Interface.class);
			
			info.openFile(filename);
			
			return true;
			}
		catch (DBusException e)
			{
			e.printStackTrace();
			return false;
			}
		
		}
	
	/*
	void send()
		{
		
		try
			{
			DBusConnection conn = DBusConnection.getConnection(DBusConnection.SESSION);
			conn.sendSignal(new EndrovDBUS.Sig());
			}
		catch (DBusException e)
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			}

		
		}
	*/

/*
	public void handle(Sig a)
		{
		// TODO Auto-generated method stub
		
		}
		*/
	}
