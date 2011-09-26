package endrov.dbus;

import org.freedesktop.dbus.DBusInterface;

//@DBusInterfaceName("endrov.dbus.DBus")
public interface DBus extends DBusInterface
	{
	@org.freedesktop.DBus.Description("Tell running client to open a file whenever possible")
	public boolean openFile(String filename);
	}