package logobar;
public class MacHack
	{

	// THIS IS SPECIAL MAC OPTIONS
	// REMOVE IF COMPLING FOR ALL
	// PLATFORMS
	public static void addMacAbout()
		{
		if(isMac())
			{
			OSXAdapter ad=new OSXAdapter();
			ad.registerMacOSXApplication();
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
