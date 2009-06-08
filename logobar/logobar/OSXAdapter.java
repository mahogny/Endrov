package logobar;

import java.io.File;
import javax.swing.JOptionPane;
//import javax.swing.SwingUtilities;



public class OSXAdapter extends ApplicationAdapter implements MRJOpenDocumentHandler
	{
	
	//pseudo-singleton model; no point in making multiple instances
	//of the EAWT application or our adapter
	private static com.apple.eawt.Application theApplication;
	

	/**
	 * The main entry-point for this functionality.  This is the only method
	 * that needs to be called at runtime, and it can easily be done using
	 * reflection
	 */
	@SuppressWarnings("deprecation") public static void registerMacOSXApplication() 
		{
		if (theApplication == null)
			{
			theApplication = new com.apple.eawt.Application();
			OSXAdapter theAdapter = new OSXAdapter();
			theApplication.addApplicationListener(theAdapter);
			MRJApplicationUtils.registerOpenDocumentHandler(theAdapter);
			}
		}
	
	/**
	 * Another static entry point for EAWT functionality.  Enables the 
	 * "Preferences..." menu item in the application menu. 
	 */ 
	public static void enablePrefs(boolean enabled) 
		{
		if (theApplication == null) 
			theApplication = new com.apple.eawt.Application();
		theApplication.setEnabledPreferencesMenu(enabled);
		}
	
	/**
	 * Invoked on Apple -> About
	 */
	public void handleAbout(ApplicationEvent ae) 
		{
		ae.setHandled(true);
		LogoBar.showAboutWindow();
		}
	
	/**
	 * Invoked on Apple -> Preferences
	 */
	public void handlePreferences(ApplicationEvent ae)
		{
		ae.setHandled(true);
		}
	
	/**
	 * Invoked on Apple -> Quit
	 */
	public void handleQuit(ApplicationEvent ae) 
		{
		ae.setHandled(false);
		System.exit(0);
		}

	//Note that DnD will not work if starter for next app is used.
	//For no reason at all, openfile is not opened on startup
	
	/**
	 * Invoked on file drag to bundle
	 */
	public void handleOpenFile(final File f)
		{
		}

	

	}