package endrov.basicWindow;

import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import endrov.basicWindow.icon.BasicIcon;
import endrov.data.EvData;
import endrov.data.LoadProgressDialog;
import endrov.ev.*;
import endrov.keyBinding.KeyBinding;

import org.jdom.*;



/**
 * Any window in the application inherits this class.
 * 
 * @author Johan Henriksson
 */
public abstract class BasicWindow extends JPanel 
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0; 

	/** The set of all extensions */
	public static Vector<BasicWindowExtension> basicWindowExtensions=new Vector<BasicWindowExtension>();
	
	/** The set of all windows. Cannot be weak, GC time not guaranteed but this is critical to figure out when the program is to close */
	public static HashSet<BasicWindow> windowList=new HashSet<BasicWindow>();
	
	/** Manager for creating windows */
	public static EvWindowManagerMaker windowManager=new EvWindowManagerFree.Manager();
	public interface EvWindowManagerMaker
		{
		public EvWindowManager createWindow(BasicWindow bw);
		}
	
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
	
	/** Get the set of all windows, not to be modified */
	public static Set<BasicWindow> getWindowList()
		{
		return windowList;
		}

	
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
	 * 
	 * DEPRECATED!
	 */	
	public static void updateWindows(BasicWindow from)
		{
		for(BasicWindow w:windowList)
			if(w!=from)
				{
				for(endrov.basicWindow.BasicWindowHook h:w.basicWindowExtensionHook.values())
					h.buildMenu(w);
				w.dataChangedEvent();
				}
		//TODO: is a lock needed to avoid infinite loops?
		//should we describe what kind of change?
		}

	/**
	 * Tell all windows to update. DO NOT USE! DEPRECATED!
	 */	
	public static void updateWindows()
		{
		BasicWindow.updateWindows(null);
		}
	
	public static boolean holdModifier1(KeyEvent e)
		{
		return e.getModifiersEx()==KeyEvent.META_DOWN_MASK || e.getModifiersEx()==KeyEvent.CTRL_DOWN_MASK;
		}
	
	/**
	 * Get bounds of window from XML element
	 */
	public static Rectangle getXMLbounds(Element e) throws Exception
		{
		int x=e.getAttribute("x").getIntValue();
		int y=e.getAttribute("y").getIntValue();
		int w=e.getAttribute("w").getIntValue();
		int h=e.getAttribute("h").getIntValue();
		return new Rectangle(x,y,w,h);
		}
	
	/**
	 * Store bounds of this window into XML element
	 */
	public void setXMLbounds(Element e)
		{
		Rectangle r=evw.getBounds();
		e.setAttribute("x", ""+r.x);
		e.setAttribute("y", ""+r.y);
		e.setAttribute("w", ""+r.width);
		e.setAttribute("h", ""+r.height);
		}
	
	

	/**
	 * Add menu item to a menu, put it in alphabetical order
	 * @param menu
	 * @param ni
	 */
	public static void addMenuItemSorted(JMenu menu, JMenuItem ni)
		{
		String thisText=ni.getText().toLowerCase();
		for(int i=0;i<menu.getItemCount();i++)
			{
			JMenuItem nj=(JMenuItem)menu.getItem(i);
			if(thisText.compareTo(nj.getText().toLowerCase())<0)
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
		String thisText=ni.getText().toLowerCase();
		for(int i=startPos;i<menu.getMenuCount();i++)
			{
			JMenuItem nj=(JMenuItem)menu.getMenu(i);
			if(thisText.compareTo(nj.getText().toLowerCase())<0)
				{
				menu.add(ni, i);
				return;
				}			
			}
		menu.add(ni);
		}
	

	
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


	/**
	 * Broadcast that a file has been loaded
	 */
	public static void updateLoadedFile(EvData d)
		{
		for(BasicWindow w:windowList)
			w.loadedFile(d);
		}

	/******************************************************************************************************
	 *                               Static: DnD utils                                                    *
	 *****************************************************************************************************/

	public static void attachDragAndDrop(JComponent c)
		{
//		c.getClass().getMethod("setDragEnabled", parameterTypes)
		if(c instanceof JList)
			((JList)c).setDragEnabled(false);
    c.setTransferHandler(new FSTransfer());
		}
	
	@SuppressWarnings("unchecked") public static List<File> transferableToFileList(Transferable t)
		{
		try
			{
			List<File> files=new LinkedList<File>();
			if(t.isDataFlavorSupported(DataFlavor.stringFlavor))
				{
				String data = (String)t.getTransferData(DataFlavor.stringFlavor);
				BufferedReader buf=new BufferedReader(new StringReader(data));
				String line;
				while((line=buf.readLine())!=null)
					{
					if(line.startsWith("file:/"))
						{
						line=line.substring("file:/".length());
						files.add(new File(line));
						}
					else
						System.out.println("Not sure how to read: "+line);
					}
				return files;
				}
			else if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
				{
				List data = (List)t.getTransferData(DataFlavor.javaFileListFlavor);
				Iterator i = data.iterator();
				while (i.hasNext()) 
					files.add((File)i.next());
				return files;
				}
			return null;
			}
		catch (UnsupportedFlavorException e)
			{
			e.printStackTrace();
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		return null;
		}
		
	
	/**
	 * Handle drag and drop of files to JList
	 */
	private static class FSTransfer extends TransferHandler 
		{
		static final long serialVersionUID=0;
		public boolean importData(JComponent comp, Transferable t) 
			{
			final List<File> files=transferableToFileList(t);
			if(files!=null)
				{
				new Thread() { 
				public void run()
					{ 
					EV.waitUntilStartedUp();
					LoadProgressDialog loadDialog=new LoadProgressDialog(files.size());
					final List<EvData> dlist=new LinkedList<EvData>();
					int i=0;
					for(File f:files)
						{
						loadDialog.setCurFile(i);
						loadDialog.loadFileStatus(0, "Loading "+f);
						EvData d=EvData.loadFile(f);
						if(d==null)
							JOptionPane.showMessageDialog(null, "Failed to open "+f);
						else
							dlist.add(d);
						i++;
						}
					SwingUtilities.invokeLater(new Runnable(){
					public void run()
						{
						for(EvData d:dlist)
							EvData.registerOpenedData(d);
						}
					});
					loadDialog.dispose();
					}}.start(); 
				return true;
				}
			else
				return false;
			}
		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) 
			{
			return true;
			}
		}

	
	
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	public EvWindowManager evw=windowManager.createWindow(this);
	
	public void packEvWindow()
		{
		evw.pack();
		}
	public Rectangle getBoundsEvWindow()
		{
		return evw.getBounds();
		}
	public void setBoundsEvWindow(Rectangle r)
		{
		if(r!=null)
			evw.setBounds(r);
		}
	public void setBoundsEvWindow(int x, int y, int width, int height)
		{
		evw.setBounds(new Rectangle(x, y, width, height));
		}
	public void setLocationEvWindow(int x, int y)
		{
		evw.setLocation(x, y);
		}
	public Rectangle getBounds()
		{
		return evw.getBounds();
		}
	public void setTitleEvWindow(String title)
		{
		evw.setTitle(title);
		}
	public void setVisibleEvWindow(boolean b)
		{
		evw.setVisible(true);
		}
	public void disposeEvWindow()
		{
		evw.dispose();
		}
	//setfocusable
	//addkeylistener
	
	
	/** Hooks for all extensions */
	public HashMap<Class<?>,BasicWindowHook> basicWindowExtensionHook=new HashMap<Class<?>,BasicWindowHook>();

	
	/**
	 * Just copy in needed data 
	 */
	public BasicWindow()
		{
		BasicWindow.windowList.add(this);
		
		for(BasicWindowExtension e:basicWindowExtensions)
			e.newBasicWindow(this);

		createMenus();
		}
	

	/**
	 * Handle menus etc
	 */
	private ActionListener listener=new ActionListener()
		{
		public void actionPerformed(ActionEvent e) 
			{
			if(e.getSource()==miQuit)         
				dialogQuit();
			else if(e.getSource()==miGC)
				{
				System.out.println("Running GC");
				System.gc();
				}
			else if(e.getSource()==miResetPC)
				{
				if(JOptionPane.showConfirmDialog(null, "Do you really want to reset config? This requires a restart of EV.", "EV", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
					{
					EV.resetPersonalConfig();
					System.exit(0);
					}
				}
			else if(e.getSource()==miToggleSplash)
				{
				boolean b=!EvSplashScreen.isSplashEnabled();
				EvSplashScreen.setSplashEnabled(b);
				JOptionPane.showMessageDialog(null, "Show splash screen: "+b);
				}

			if(e.getSource()==miSavePluginList)  EV.savePluginList();

			if(e.getSource()==miWebHome)      BrowserControl.displayURL(EV.website+"Main_Page");
			if(e.getSource()==miWebUser)      BrowserControl.displayURL(EV.website+"Users_Guide");
			if(e.getSource()==miWebPlugins)   BrowserControl.displayURL(EV.website+"Plugins");
			if(e.getSource()==miAbout)        dialogAbout();
			if(e.getSource()==miSysInfo)      dialogSysInfo();
			if(e.getSource()==miSaveConfig)   EV.savePersonalConfig();
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
	private JMenuItem miToggleSplash=new JMenuItem("Toggle splash screen");
	
	private JMenuItem miQuit=new JMenuItem("Exit",BasicIcon.iconMenuQuit);

	private JMenuItem miAbout=new JMenuItem("About");
	private JMenuItem miWebHome=new JMenuItem(EV.programName+" Home");
	private JMenuItem miWebUser=new JMenuItem("User Guide");
	private JMenuItem miWebPlugins=new JMenuItem("Plugins");
	private JMenuItem miSysInfo=new JMenuItem("System Info");
	private JMenuItem miSaveConfig=new JMenuItem("Save config now");

	
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
	 * Set up basic menus
	 */
	public void createMenus()
		{
		evw.setJMenuBar(menubar);		
		
		//Menu structure	
		addMenubar(menuFile);
		addMenubar(menuWindows);
		addMenubar(menuBatch);	
		menuFile.add(menuInfo);
		menuFile.add(menuMaintenance);
		menuMaintenance.add(miGC);
		menuMaintenance.add(miResetPC);
		menuMaintenance.add(miSavePluginList);
		menuMaintenance.add(miToggleSplash);
		menuMaintenance.add(miSaveConfig);
		menuFile.add(miQuit);
		
		menuInfo.add(miAbout);
		menuInfo.add(miWebHome);
		menuInfo.add(miWebUser);
		menuInfo.add(miWebPlugins);
		menuInfo.add(miSysInfo);

		for(BasicWindowHook hook:basicWindowExtensionHook.values())
			hook.createMenus(this);

		//Listeners
		miQuit.addActionListener(listener);
		miResetPC.addActionListener(listener);
		miGC.addActionListener(listener);
		miSavePluginList.addActionListener(listener);
		miToggleSplash.addActionListener(listener);
		
		miAbout.addActionListener(listener);
		miWebHome.addActionListener(listener);
		miWebUser.addActionListener(listener);
		miWebPlugins.addActionListener(listener);
		miSysInfo.addActionListener(listener);
		miSaveConfig.addActionListener(listener);
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
		String[] wf=ImageIO.getWriterFormatNames();
		String jaiformats="JAI supports extensions:";
		for(String s:wf)
			jaiformats+=" "+s;
		
		Runtime rt=Runtime.getRuntime();
		String text=
			"Available processors: "+rt.availableProcessors()+"\n"+
			"Total memory used: "+(rt.totalMemory()/1024/1024)+" MiB\n"+
			"Free memory (in JVM): "+(rt.freeMemory()/1024/1024)+" MiB\n"+
			"Memory left: "+((rt.maxMemory()-rt.totalMemory())/1024/1024)+" MiB\n"+
			"Max memory available for Java: "+(rt.maxMemory()/1024/1024)+" MiB\n"+
			jaiformats;
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

	
	
	
	
	
	/******************************************************************************************************
	 *                               Abstract Instance                                                    *
	 *****************************************************************************************************/

	
	
	
	/**
	 * Called whenever EV has changed
	 */
	public abstract void dataChangedEvent();

	/**
	 * Called to obtain personal settings for that window. Function has to create new elements and add them
	 * to the given element.
	 */
	public abstract void windowPersonalSettings(Element e);
	

	/**
	 * Called when a file has just been loaded and should be displayed in all windows
	 */
	public abstract void loadedFile(EvData data);
	
	
	}
