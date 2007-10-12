package evplugin.basicWindow;

import java.awt.event.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;

import evplugin.ev.*;
import evplugin.keyBinding.KeyBinding;
import org.jdom.*;


//System.setOut(out)


/**
 * Any window in the application inherits this class.
 * 
 * @author Johan Henriksson
 */
public abstract class BasicWindow extends JFrame implements WindowListener
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0; //wtf

	
	/** The set of all extensions */
	public static Vector<BasicWindowExtension> basicWindowExtensions=new Vector<BasicWindowExtension>();
	
	/** The set of all windows */
	public static HashSet<BasicWindow> windowList=new HashSet<BasicWindow>();
	
	
	public static void initPlugin() {}
	static
		{
		EV.personalConfigLoaders.put("basicwindow",new PersonalConfig()
			{
			public void loadPersonalConfig(Element e)
				{
				}
			public void savePersonalConfig(Element e)
				{
				//Settings for individual windows
				for(BasicWindow w:BasicWindow.windowList)
					w.windowPersonalSettings(e);
				}
			});
		}

	public static final int KEY_GETCONSOLE=KeyBinding.register(new KeyBinding("Basic Window","Get console",KeyEvent.VK_ESCAPE, 0));
	
	/**
	 * Add an extension of Basic Window
	 */
	public static void addBasicWindowExtension(BasicWindowExtension e)
		{
		basicWindowExtensions.add(e);
		}

	/**
	 * Tell all windows to update except where the signal came from.
	 * This is needed to avoid nasty infinite recursion if signal
	 * is emitted during rendering.
	 */	
	public static void updateWindows(BasicWindow from)
		{
		for(BasicWindow w:windowList)
			if(w!=from)
				{
				for(evplugin.basicWindow.BasicWindowHook h:w.basicWindowExtensionHook.values())
					h.buildMenu(w);
				w.dataChangedEvent();
				}
		//TODO: is a lock needed to avoid infinite loops?
		//should we describe what kind of change?
		}

	/**
	 * Tell all windows to update
	 */	
	public static void updateWindows()
		{
		BasicWindow.updateWindows(null);
		}
	
	public static boolean holdModifier1(KeyEvent e)
		{
		return e.getModifiersEx()==KeyEvent.META_DOWN_MASK || e.getModifiersEx()==KeyEvent.CTRL_DOWN_MASK;
		}
	
	public static Rectangle getXMLbounds(Element e) throws Exception
		{
		int x=e.getAttribute("x").getIntValue();
		int y=e.getAttribute("y").getIntValue();
		int w=e.getAttribute("w").getIntValue();
		int h=e.getAttribute("h").getIntValue();
		return new Rectangle(x,y,w,h);
		}
	
	public void setXMLbounds(Element e)
		{
		Rectangle r=getBounds();
		e.setAttribute("x", ""+r.x);
		e.setAttribute("y", ""+r.y);
		e.setAttribute("w", ""+r.width);
		e.setAttribute("h", ""+r.height);
		}
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	/** Hooks for all extensions */
	public HashMap<Class,BasicWindowHook> basicWindowExtensionHook=new HashMap<Class,BasicWindowHook>();

	
	/**
	 * Just copy in needed data 
	 */
	public BasicWindow()
		{
		BasicWindow.windowList.add(this);
		
		for(BasicWindowExtension e:basicWindowExtensions)
			e.newBasicWindow(this);

		createMenus();
		
		addWindowListener(this);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		}
	

	/**
	 * Handle menus etc
	 */
	private ActionListener listener=new ActionListener()
		{
		public void actionPerformed(ActionEvent e) 
			{
			if(e.getSource()==miQuit)         dialogQuit();
			if(e.getSource()==miGC)
				{
				System.out.println("Running GC");
				System.gc();
				}
			if(e.getSource()==miResetPC)
				{
				if(JOptionPane.showConfirmDialog(null, "Do you really want to reset config? This requires a restart of EV.", "EV", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
					{
					EV.resetPersonalConfig();
					System.exit(0);
					}
				}

			if(e.getSource()==miSavePluginList)  EV.savePluginList();

			if(e.getSource()==miWebHome)      BrowserControl.displayURL(EV.website+"Main_Page");
			if(e.getSource()==miWebUser)      BrowserControl.displayURL(EV.website+"Users_Guide");
			if(e.getSource()==miWebDeveloper) BrowserControl.displayURL(EV.website+"Developers_Guide");
			if(e.getSource()==miWebPlugins)   BrowserControl.displayURL(EV.website+"Plugins");
			if(e.getSource()==miAbout)        dialogAbout();
			if(e.getSource()==miSysInfo)      dialogSysInfo();
			}
		};
		
	private JMenuBar menubar=new JMenuBar();
	private JMenu menuFile=new JMenu("EV");
	private JMenu menuMaintenance=new JMenu("Maintenance");
	private JMenu menuWindows=new JMenu("Windows");
	private JMenu menuBatch=new JMenu("Batch");
	private JMenu menuInfo=new JMenu("Info");
	private JMenuItem miGC=new JMenuItem("Run GC");
	private JMenuItem miResetPC=new JMenuItem("Reset personal config");
	private JMenuItem miSavePluginList=new JMenuItem("Save plugin list");
	private JMenuItem miQuit=new JMenuItem("Exit");

	private JMenuItem miAbout=new JMenuItem("About");
	private JMenuItem miWebHome=new JMenuItem(EV.programName+" Home");
	private JMenuItem miWebUser=new JMenuItem("User Guide");
	private JMenuItem miWebDeveloper=new JMenuItem("Developer Guide");
	private JMenuItem miWebPlugins=new JMenuItem("Plugins");
	private JMenuItem miSysInfo=new JMenuItem("System Info");

	
	/**
	 * Add to the menu Window
	 */
	public void addMenuWindow(JMenuItem ni)
		{
		addMenuItemSorted(menuWindows, ni);
		}

	/**
	 * Add to the menu Batch
	 */
	public void addMenuBatch(JMenuItem ni)
		{
		addMenuItemSorted(menuBatch, ni);
		}

	/**
	 * Add to the menubar
	 */
	public void addMenubar(JMenu ni)
		{
		addMenuSorted(menubar, ni, 1);
		}

	/**
	 * Add menu item to a menu, put it in alphabetical order
	 * @param menu
	 * @param ni
	 */
	public static void addMenuItemSorted(JMenu menu, JMenuItem ni)
		{
		String thisText=ni.getText();
		for(int i=0;i<menu.getItemCount();i++)
			{
			JMenuItem nj=(JMenuItem)menu.getItem(i);
			if(thisText.compareTo(nj.getText())<0)
				{
				menu.add(ni, i);
				return;
				}			
			}
		menu.add(ni);
		}

	/**
	 * Add menu to a menubar, put in alphabetical order
	 */
	public static void addMenuSorted(JMenuBar menu, JMenu ni, int startPos)
		{
		String thisText=ni.getText();
		for(int i=startPos;i<menu.getMenuCount();i++)
			{
			JMenuItem nj=(JMenuItem)menu.getMenu(i);
			if(thisText.compareTo(nj.getText())<0)
				{
				menu.add(ni, i);
				return;
				}			
			}
		menu.add(ni);
		}
	

	/**
	 * Set up basic menus
	 */
	public void createMenus()
		{
		setJMenuBar(menubar);		
		
		//Menu structure	
		addMenubar(menuFile);
		addMenubar(menuWindows);
		addMenubar(menuBatch);	
		menuFile.add(menuInfo);
		menuFile.add(menuMaintenance);
		menuMaintenance.add(miGC);
		menuMaintenance.add(miResetPC);
		menuMaintenance.add(miSavePluginList);
		menuFile.add(miQuit);
		
		menuInfo.add(miAbout);
		menuInfo.add(miWebHome);
		menuInfo.add(miWebUser);
		menuInfo.add(miWebDeveloper);
		menuInfo.add(miWebPlugins);
		menuInfo.add(miSysInfo);

		for(BasicWindowHook hook:basicWindowExtensionHook.values())
			hook.createMenus(this);

		//Listeners
		miQuit.addActionListener(listener);
		miResetPC.addActionListener(listener);
		miGC.addActionListener(listener);
		miSavePluginList.addActionListener(listener);
		
		miAbout.addActionListener(listener);
		miWebHome.addActionListener(listener);
		miWebUser.addActionListener(listener);
		miWebDeveloper.addActionListener(listener);
		miWebPlugins.addActionListener(listener);
		miSysInfo.addActionListener(listener);
		}
	
	
	/**
	 * Show about dialog
	 */
	public static void dialogAbout()
		{
		String text=EV.programName+" "+EV.version+"\n"+
     "Developed by Johan Henriksson at KI, department of Biosciences and Nutrition\n"+
     "http://www.biosci.ki.se/groups/tbu/\n"+
     "This program is under BSD3 license\n" +
     "Individual plugins may be under different licenses";
		JOptionPane.showMessageDialog(null, text);
		}

	/**
	 * Show system info dialog
	 */
	public static void dialogSysInfo()
		{
		Runtime rt=Runtime.getRuntime();
		String text=
			"Available processors: "+rt.availableProcessors()+"\n"+
			"Total memory used: "+(rt.totalMemory()/1024/1024)+" MiB\n"+
			"Free memory (in JVM): "+(rt.freeMemory()/1024/1024)+" MiB\n"+
			"Memory left: "+((rt.maxMemory()-rt.totalMemory())/1024/1024)+" MiB\n"+
			"Max memory available for Java: "+(rt.maxMemory()/1024/1024)+" MiB";
		JOptionPane.showMessageDialog(null, text);
		}
	
	

	/** Handle "preferences" from the Mac menu */
	public void dialogPreferences() 
		{
		}

	/** Show the quit dialog */
	public static void dialogQuit() 
		{
		int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?", "Quit?", JOptionPane.YES_NO_OPTION);
		if (option == JOptionPane.YES_OPTION)
    	EV.quit();
		}

	
	public void windowClosing(WindowEvent e) {}
	public void windowActivated(WindowEvent arg0)	{}
	public void windowDeactivated(WindowEvent arg0)	{}
	public void windowDeiconified(WindowEvent arg0)	{}
	public void windowIconified(WindowEvent arg0) {}
	public void windowOpened(WindowEvent arg0) {}
	public void windowClosed(WindowEvent arg0)
		{
		BasicWindow.windowList.remove(this);
		}
	
	/**
	 * Called whenever EV has changed
	 */
	public abstract void dataChangedEvent();

	/**
	 * Called to obtain personal settings for that window
	 * @return Something to write in the personal settings
	 */
	public abstract void windowPersonalSettings(Element e);
	
	
	
	/**
	 * Totally rip a menu apart, recursively. Action listeners are removed in a safe way which guarantees GC can proceed
	 */
	public static void tearDownMenu(JMenu menu)
		{
		Vector<JMenuItem> componentsToRemove=new Vector<JMenuItem>();
		for(int i=0;i<menu.getItemCount();i++)
			componentsToRemove.add(menu.getItem(i));
		for(JMenuItem c:componentsToRemove)
			if(c==null)
				;//Separator
			else if(c instanceof JMenu)
				tearDownMenu((JMenu)c);
			else
				for(ActionListener l:c.getActionListeners())
					c.removeActionListener(l);
		menu.removeAll();
		}
	}
