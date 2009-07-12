package endrov.basicWindow;

import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.*;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import endrov.basicWindow.icon.BasicIcon;
import endrov.data.EvData;
import endrov.data.GuiEvDataIO;
import endrov.ev.*;
import endrov.keyBinding.JInputManager;
import endrov.keyBinding.JinputListener;
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
	 * Static *
	 *****************************************************************************************************/
	static final long serialVersionUID = 0;

	/** The set of all extensions */
	public static Vector<BasicWindowExtension> basicWindowExtensions = new Vector<BasicWindowExtension>();

	/** Manager for creating windows */
	public static EvWindowManagerMaker windowManager = new EvWindowManagerFree.Manager();

	public interface EvWindowManagerMaker
		{
		public EvWindowManager createWindow(BasicWindow bw);
		public List<BasicWindow> getAllWindows();
		public BasicWindow getFocusWindow();
		}

	public static void initPlugin()
		{
		}

	static
		{
		EV.personalConfigLoaders.put("basicwindow", new PersonalConfig()
			{
				public void loadPersonalConfig(Element e)
					{
					}

				public void savePersonalConfig(Element e)
					{
					// Settings for individual windows
					for (BasicWindow w : windowManager.getAllWindows())
						w.windowSavePersonalSettings(e);
					}
			});
		
		JInputManager.addGamepadMode("Active window", new JInputModeBasicWindow(), true);
		}

	public static final int KEY_GETCONSOLE = KeyBinding.register(new KeyBinding(
			"Basic Window", "Get console", KeyEvent.VK_ESCAPE, 0));

	/** Get the set of all windows, not to be modified */
	public static List<BasicWindow> getWindowList()
		{
		return windowManager.getAllWindows();
		}

	/**
	 * Add an extension of Basic Window
	 */
	public static void addBasicWindowExtension(BasicWindowExtension e)
		{
		basicWindowExtensions.add(e);
		}

	/**
	 * Tell all windows to update except where the signal came from. This is
	 * needed to avoid nasty infinite recursion if signal is emitted during
	 * rendering. DEPRECATED!
	 */
	public static void updateWindows(BasicWindow from)
		{
		for (BasicWindow w : getWindowList())
			if (w!=from)
				{
				for (endrov.basicWindow.BasicWindowHook h : w.basicWindowExtensionHook.values())
					h.buildMenu(w);
				w.dataChangedEvent();
				}
		// TODO: is a lock needed to avoid infinite loops?
		// should we describe what kind of change?
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
		return e.getModifiersEx()==KeyEvent.META_DOWN_MASK
				||e.getModifiersEx()==KeyEvent.CTRL_DOWN_MASK;
		}

	/**
	 * Get bounds of window from XML element
	 */
	public static Rectangle getXMLbounds(Element e) throws Exception
		{
		int x = e.getAttribute("x").getIntValue();
		int y = e.getAttribute("y").getIntValue();
		int w = e.getAttribute("w").getIntValue();
		int h = e.getAttribute("h").getIntValue();
		return new Rectangle(x, y, w, h);
		}

	/**
	 * Store bounds of this window into XML element
	 */
	public void setXMLbounds(Element e)
		{
		Rectangle r = getEvw().getBounds();
		e.setAttribute("x", ""+r.x);
		e.setAttribute("y", ""+r.y);
		e.setAttribute("w", ""+r.width);
		e.setAttribute("h", ""+r.height);
		}

	/**
	 * Add menu item to a menu, put it in alphabetical order
	 */
	public static void addMenuItemSorted(JMenu menu, JMenuItem ni, String itemName)
		{
		// String thisText=ni.getText().toLowerCase();
		String thisText = itemName;
		ni.setName(itemName);
		for (int i = 0; i<menu.getItemCount(); i++)
			{
			JMenuItem nj = (JMenuItem) menu.getItem(i);
			// System.out.println(thisText+" vs "+nj.getName()+" "+thisText.compareTo(nj.getName()));
			if (thisText.compareTo(nj.getName())<0)
			// if(thisText.compareTo(nj.getText().toLowerCase())<0)
				{
				menu.add(ni, i);
				return;
				}
			}
		menu.add(ni);
		}

	/**
	 * Add sorted entry, take label as name
	 */
	public static void addMenuItemSorted(JMenu menu, JMenuItem ni)
		{
		addMenuItemSorted(menu, ni, ni.getText());
		}

	/**
	 * Add menu to a menubar, put in alphabetical order
	 */
	public static void addMenuSorted(JMenuBar menu, JMenu ni, int startPos)
		{
		String thisText = ni.getText().toLowerCase();
		for (int i = startPos; i<menu.getMenuCount(); i++)
			{
			JMenuItem nj = (JMenuItem) menu.getMenu(i);
			if (thisText.compareTo(nj.getText().toLowerCase())<0)
				{
				menu.add(ni, i);
				return;
				}
			}
		menu.add(ni);
		}

	/**
	 * Broadcast that a file has been loaded
	 */
	public static void updateLoadedFile(EvData d)
		{
		for (BasicWindow w : getWindowList())
			w.loadedFile(d);
		}

	/******************************************************************************************************
	 * Static: DnD utils *
	 *****************************************************************************************************/


	public static String convertStreamToString(InputStreamReader r)
		{
		BufferedReader reader=new BufferedReader(r);
		StringBuilder sb = new StringBuilder();

		String line = null;
		try 
			{
			while ((line = reader.readLine()) != null) 
				sb.append(line + "\n");
			}
		catch (IOException e) 
			{
			e.printStackTrace();
			} 
		finally
			{
			try
				{
				reader.close();
				} 
			catch (IOException e) 
				{
				e.printStackTrace();
				}
			}
		return sb.toString();
		}
	public static String convertStreamToString(InputStream is)
		{
		return convertStreamToString(new InputStreamReader(is));
		}


	public static void attachDragAndDrop(JComponent c)
		{
		// c.getClass().getMethod("setDragEnabled", parameterTypes)
		if (c instanceof JList)
			((JList) c).setDragEnabled(false);
		c.setTransferHandler(new FSTransfer());
		}

	@SuppressWarnings("unchecked")
	public static List<File> transferableToFileList(Transferable t)
		{
		System.out.println("Drag and drop");
		try
			{
			List<File> files = new LinkedList<File>();
			if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
				{
				List data = (List) t.getTransferData(DataFlavor.javaFileListFlavor);
				Iterator i = data.iterator();
				while (i.hasNext())
					files.add((File) i.next());
				System.out.println("javalistflavour "+files);
				return files;
				}
			else //if (t.isDataFlavorSupported(DataFlavor.stringFlavor))
				{
				String data;
				if (t.isDataFlavorSupported(DataFlavor.stringFlavor))
					data = (String) t.getTransferData(DataFlavor.stringFlavor);
				else
					{
					Object inp=t.getTransferData(t.getTransferDataFlavors()[0]);
					if(inp.getClass()==String.class)
						data=(String)inp;
					else if(InputStream.class.isInstance(inp)) 
						data=convertStreamToString((InputStream)inp);
					else if(InputStreamReader.class.isInstance(inp))
						data=convertStreamToString((InputStreamReader)inp);
					else
						{
						System.out.println("Unsupported type: "+inp.getClass());
						return null;
						}
					}
		    for (StringTokenizer st = new StringTokenizer(data, "\r\n"); st.hasMoreTokens();) 
		    	{
		      String s = st.nextToken();
		      if (s.startsWith("#")) 
		        // the line is a comment (as per the RFC 2483)
		        continue;
		      
		      try 
		      	{
		        URI uri = new URI(s);
		        files.add(new File(uri));
		      	}
		      catch (URISyntaxException e) 
		      	{
		        e.printStackTrace();
		      	}
		      catch (IllegalArgumentException e) 
		      	{
		        e.printStackTrace();
		      	}
		    	}

				/*
				
				BufferedReader buf = new BufferedReader(new StringReader(data));
				String line;
				while ((line = buf.readLine())!=null)
					{
					if (line.startsWith("file:/"))
						{
						line = line.substring("file:/".length());
						files.add(new File(line));
						}
					else
						System.out.println("Not sure how to read: "+line);
					}*/
				System.out.println(files);
				return files;
				}
/*			else
				{
				Object inp=t.getTransferData(t.getTransferDataFlavors()[0]);
				System.out.println(""+inp.getClass()+"   "+inp);

				
				LinkedList<DataFlavor> flav=new LinkedList<DataFlavor>();
				for(DataFlavor fl:t.getTransferDataFlavors())
					flav.add(fl);
				System.out.println("unsupported drag and drop, flavours: "+flav.toString());
				return null;
				}*/
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
		static final long serialVersionUID = 0;

		public boolean importData(JComponent comp, Transferable t)
			{
			final List<File> files = transferableToFileList(t);
			if (files!=null)
				{
				new Thread()
					{
						public void run()
							{
							EV.waitUntilStartedUp();
							List<String> flist = new LinkedList<String>();
							for (File f : files)
								flist.add(f.getAbsolutePath());
							for (EvData d : GuiEvDataIO.loadFile(flist))
								EvData.registerOpenedData(d);

							/*
							 * LoadProgressDialog loadDialog=new
							 * LoadProgressDialog(files.size()); final List<EvData> dlist=new
							 * LinkedList<EvData>(); int i=0; for(File f:files) {
							 * loadDialog.setCurFile(i); loadDialog.fileIOStatus(0,
							 * "Loading "+f); EvData d=EvData.loadFile(f); if(d==null)
							 * JOptionPane.showMessageDialog(null, "Failed to open "+f); else
							 * { EvData.setLastDataPath(f.getParentFile()); dlist.add(d); }
							 * i++; } SwingUtilities.invokeLater(new Runnable(){ public void
							 * run() { for(EvData d:dlist) EvData.registerOpenedData(d); } });
							 * loadDialog.dispose();
							 */
							}
					}.start();
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
	 * Instance *
	 *****************************************************************************************************/

	private WeakReference<EvWindowManager> evw = new WeakReference<EvWindowManager>(windowManager.createWindow(this));

	public EvWindowManager getEvw()
		{
		return evw.get();
		}


	
	public void packEvWindow()
		{
		getEvw().pack();
		}

	public Rectangle getBoundsEvWindow()
		{
		return getEvw().getBounds();
		}

	public void setBoundsEvWindow(Rectangle r)
		{
		if (r!=null)
			getEvw().setBounds(r);
		}

	public void setBoundsEvWindow(int x, int y, int width, int height)
		{
		getEvw().setBounds(new Rectangle(x, y, width, height));
		}

	public void setLocationEvWindow(int x, int y)
		{
		getEvw().setLocation(x, y);
		}

	public Rectangle getBounds()
		{
		return getEvw().getBounds();
		}

	/**
	 * Set title of this window
	 */
	public void setTitleEvWindow(String title)
		{
		getEvw().setTitle(title);
		}

	/**
	 * Set visibility of this window
	 */
	public void setVisibleEvWindow(boolean b)
		{
		getEvw().setVisible(true);
		}

	public void setResizable(boolean b)
		{
		getEvw().setResizable(b);
		}

	public void disposeEvWindow()
		{
		getEvw().dispose();
		}

	public void toFront()
		{
		getEvw().toFront();
		}

	// setfocusable
	// addkeylistener

	/** Hooks for all extensions */
	public HashMap<Class<?>, BasicWindowHook> basicWindowExtensionHook = new HashMap<Class<?>, BasicWindowHook>();

	private static Object instanceCounterLock = new Object();
//	private static int instanceCounter = 0;

	/**
	 * Instance number unique to this window. Can be presented to the user to keep
	 * track of related dialogs.
	 */
	public int windowInstance;

	/**
	 * Just copy in needed data
	 */
	public BasicWindow()
		{
		synchronized (instanceCounterLock)
			{
			//Get an instance number. try to keep the values low by reusing old ones.
			int instanceCounter = -1;
			boolean same;
			do
				{
				instanceCounter++;
				same=false;
				for(BasicWindow w:getWindowList())
					if(w.windowInstance==instanceCounter)
						same=true;
				} while(same);
			windowInstance = instanceCounter;
			}

		for (BasicWindowExtension e : basicWindowExtensions)
			e.newBasicWindow(this);

		createMenus();
		}

	/**
	 * Handle menus etc
	 */
	private ActionListener listener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
				{
				if (e.getSource()==miQuit)
					dialogQuit();
				else if (e.getSource()==miGC)
					{
					System.out.println("Running GC");
					System.gc();
					}
				else if (e.getSource()==miResetPC)
					{
					if (JOptionPane
							.showConfirmDialog(
									null,
									"Do you really want to reset config? This requires a restart of EV.",
									"EV", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
						{
						EV.resetPersonalConfig();
						System.exit(0);
						}
					}
				else if (e.getSource()==miToggleSplash)
					{
					boolean b = !EvSplashScreen.isSplashEnabled();
					EvSplashScreen.setSplashEnabled(b);
					JOptionPane.showMessageDialog(null, "Show splash screen: "+b);
					}
				else if (e.getSource()==miSavePluginList)
					EV.savePluginList();
				else if (e.getSource()==miWebHome)
					BrowserControl.displayURL(EV.website+"Main_Page");
				else if (e.getSource()==miWebUser)
					BrowserControl.displayURL(EV.website+"Users_Guide");
				else if (e.getSource()==miWebPlugins)
					BrowserControl.displayURL(EV.website+"Plugins");
				else if (e.getSource()==miAbout)
					dialogAbout();
				else if (e.getSource()==miSysInfo)
					dialogSysInfo();
				else if (e.getSource()==miSaveConfig)
					EV.savePersonalConfig();
				else if (e.getSource()==miOpenConfig)
					EV.openExternal(EV.getGlobalConfigEndrovDir());
				}
		};

	private JMenuBar menubar = new JMenuBar();
	public JMenu menuFile = new JMenu("File");
	private JMenu menuMaintenance = new JMenu("Maintenance");
	private JMenu menuWindows = new JMenu("Windows");
	private JMenu menuBatch = new JMenu("Batch");
	// private JMenu menuInfo=new JMenu("Info");
	private JMenuItem miGC = new JMenuItem("Run GC");
	private JMenuItem miResetPC = new JMenuItem("Reset personal config");
	private JMenuItem miSavePluginList = new JMenuItem("Save plugin list");
	private JMenuItem miToggleSplash = new JMenuItem("Toggle splash screen");
	private JMenuItem miOpenConfig = new JMenuItem("Open config directory");

	private JMenuItem miQuit = new JMenuItem("Exit", BasicIcon.iconMenuQuit);

	private JMenuItem miAbout = new JMenuItem("About");
	private JMenuItem miWebHome = new JMenuItem(EV.programName+" Home");
	private JMenuItem miWebUser = new JMenuItem("User Guide");
	private JMenuItem miWebPlugins = new JMenuItem("Plugins");
	private JMenuItem miSysInfo = new JMenuItem("System Info");
	private JMenuItem miSaveConfig = new JMenuItem("Save config now");

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
		getEvw().setJMenuBar(menubar);

		// Menu structure
		addMenubar(menuFile);
		addMenubar(menuWindows);
		addMenubar(menuBatch);
		BasicWindow.addMenuItemSorted(menuFile, menuMaintenance, "sys_maintenance");
		menuMaintenance.add(miGC);
		menuMaintenance.add(miResetPC);
		menuMaintenance.add(miSavePluginList);
		menuMaintenance.add(miToggleSplash);
		menuMaintenance.add(miOpenConfig);
		menuMaintenance.add(miSaveConfig);
		BasicWindow.addMenuItemSorted(menuFile, miQuit, "zquit");

		for (BasicWindowHook hook : basicWindowExtensionHook.values())
			hook.createMenus(this);

		JMenu mHelp = new JMenu("Help");

		mHelp.add(miAbout);
		mHelp.add(miWebHome);
		mHelp.add(miWebUser);
		mHelp.add(miWebPlugins);
		mHelp.add(miSysInfo);

		menubar.add(Box.createHorizontalGlue());
		menubar.add(mHelp);

		// Listeners
		miQuit.addActionListener(listener);
		miResetPC.addActionListener(listener);
		miGC.addActionListener(listener);
		miSavePluginList.addActionListener(listener);
		miToggleSplash.addActionListener(listener);
		miOpenConfig.addActionListener(listener);

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
		String text = EV.programName
				+" "
				+EvBuild.version
				+"\n"
				+"Developed by Johan Henriksson at KI, department of Biosciences and Nutrition\n"
				+"http://www.biosci.ki.se/groups/tbu/\n"
				+"This program is under BSD3 license\n"
				+"Individual plugins may be under different licenses";
		JOptionPane.showMessageDialog(null, text);
		}

	/**
	 * Show system info dialog
	 */
	public static void dialogSysInfo()
		{
		String[] wf = ImageIO.getWriterFormatNames();
		String jaiformats = EvLang.printf("JAI supports extensions:");//"JAI supports extensions:";
		for (String s : wf)
			jaiformats += " "+s;

		Runtime rt = Runtime.getRuntime();
		String text = "Available processors: "+rt.availableProcessors()+"\n"
				+"Total memory used: "+(rt.totalMemory()/1024/1024)+" MiB\n"
				+"Free memory (in JVM): "+(rt.freeMemory()/1024/1024)+" MiB\n"
				+"Memory left: "+((rt.maxMemory()-rt.totalMemory())/1024/1024)+" MiB\n"
				+"Max memory available for Java: "+(rt.maxMemory()/1024/1024)+" MiB\n"
				+jaiformats;
		JOptionPane.showMessageDialog(null, text);
		}

	/** Handle "preferences" from the Mac menu */
	public void dialogPreferences()
		{
		}

	/** Show the quit dialog */
	public static void dialogQuit()
		{
		int option = JOptionPane.showConfirmDialog(null,
				"Are you sure you want to quit?", "Quit?", JOptionPane.YES_NO_OPTION);
		if (option==JOptionPane.YES_OPTION)
			EV.quit();
		}

	protected void finalize() throws Throwable
		{
		System.out.println("Finalize basic window");
		}

	public void freeResourcesBasic()
		{
		//Rip menu apart just to be sure that GC works properly. or might do more harm.
/*
		for(Component c:menubar.getComponents())
			if(c instanceof JMenu)
				tearDownMenu((JMenu)c);
				*/
		freeResources();
		}

	/**
	 * Show error dialog
	 */
	public static void showErrorDialog(String error)
		{
		//Can get current window
		JOptionPane.showMessageDialog(null, error,"Error",JOptionPane.ERROR_MESSAGE);
		}

	/**
	 * Show warning dialog
	 */
	public static void showWarningDialog(String warning)
		{
		//Can get current window
		JOptionPane.showMessageDialog(null, warning,"Warning",JOptionPane.WARNING_MESSAGE);
		}

	/**
	 * Ask for input
	 */
	public static String showInputDialog(String message, String value)
		{
//		JOptionPane.showInputDialog(message, value);
		return JOptionPane.showInputDialog(null, message, value);
//		return JOptionPane.showInputDialog(null, message, EV.programName, JOptionPane.OK_OPTION);
		}
	
	/**
	 * Show informative dialog
	 */
	public static void showInformativeDialog(String message)
		{
		//Can get current window
		JOptionPane.showMessageDialog(null, message);
		}

	/**
	 * Show dialog asking Yes/No to a question
	 * */
	public static boolean showConfirmDialog(String question)
		{
		int option = JOptionPane.showConfirmDialog(null, question, EV.programName, JOptionPane.YES_NO_OPTION);
		return option==JOptionPane.YES_OPTION;
		}

	
	public WeakHashMap<JinputListener,Object> jinputListeners=new WeakHashMap<JinputListener,Object>();
	
	public void attachJinputListener(JinputListener listener)
		{
		jinputListeners.put(listener,null);
		}
	
	//TODO put to use
	public static void setAcceleratorCopy(JMenuItem mi)
		{
		mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,ActionEvent.CTRL_MASK));
		}
	public static void setAcceleratorPaste(JMenuItem mi)
		{
		mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,ActionEvent.CTRL_MASK));
		}
	public static void setAcceleratorCut(JMenuItem mi)
		{
		mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,ActionEvent.CTRL_MASK));
		}
	public static void setAcceleratorDelete(JMenuItem mi)
		{
		mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));
		}
	
	
	/******************************************************************************************************
	 * Abstract Instance *
	 *****************************************************************************************************/

	/**
	 * Called whenever EV has changed
	 */
	public abstract void dataChangedEvent();

	/**
	 * Called to obtain personal settings for that window. Function has to create
	 * new elements and add them to the given element.
	 */
	public abstract void windowSavePersonalSettings(Element e);

	/**
	 * Called when a file has just been loaded and should be displayed in all
	 * windows
	 */
	public abstract void loadedFile(EvData data);

	public abstract void freeResources();

	
		
	}
