package logobar;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MacHack
	{

	// THIS IS SPECIAL MAC OPTIONS
	// REMOVE IF COMPLING FOR ALL
	// PLATFORMS
	public static void addMacAbout()
		{
		if (isMac())
			{
			try
				{
				Class<?> c = Class.forName("logobar.OSXAdapter");
				Object ob = c.newInstance();
				Method m = c.getMethod("registerMacOSXApplication");
				m.invoke(ob);
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			}

		}

	/**
	 * Check if the system is running a mac
	 */
	public static boolean isMac()
		{
		return System.getProperty("os.name").toLowerCase().startsWith("mac os x");
		}

	}
