/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.gui.window;

import java.awt.Component;
import java.awt.Point;
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

import endrov.core.*;
import endrov.data.EvData;
import endrov.data.gui.GuiEvDataIO;
import endrov.gui.icon.BasicIcon;
import endrov.gui.keybinding.JInputManager;
import endrov.gui.keybinding.JinputListener;
import endrov.gui.keybinding.KeyBinding;
import endrov.starter.EvSystemUtil;
import endrov.typeImageset.EvImageSwap;
import endrov.typeImageset.EvPixels;
import endrov.util.EvBrowserUtil;

import org.jdom.*;


/**
 * Any window in the application inherits this class.
 * 
 * @author Johan Henriksson
 */
public abstract class EvBasicWindow extends JPanel
	{
	/******************************************************************************************************
	 * Static *
	 *****************************************************************************************************/
	static final long serialVersionUID = 0;

	static
	{
	//This option is not needed on mac, but run it there anyways
	JPopupMenu.setDefaultLightWeightPopupEnabled(false);
	}

	/** The set of all extensions */
	public static Vector<EvBasicWindowExtension> basicWindowExtensions = new Vector<EvBasicWindowExtension>();

	/** Manager for creating windows */
	public static EvWindowManagerMaker windowManager = new EvWindowManagerFree.Manager();

	public interface EvWindowManagerMaker
		{
		public EvWindowManager createWindow(EvBasicWindow bw);
		public List<EvBasicWindow> getAllWindows();
		public EvBasicWindow getFocusWindow();
		}


	public static final int KEY_GETCONSOLE = KeyBinding.register(new KeyBinding(
			"Basic Window", "Get console", KeyEvent.VK_ESCAPE, 0));

	/** Get the set of all windows, not to be modified */
	public static List<EvBasicWindow> getWindowList()
		{
		return windowManager.getAllWindows();
		}

	/**
	 * Add an extension of Basic Window
	 */
	public static void addBasicWindowExtension(EvBasicWindowExtension e)
		{
		basicWindowExtensions.add(e);
		}

	/**
	 * Tell all windows to update except where the signal came from. This is
	 * needed to avoid nasty infinite recursion if signal is emitted during
	 * rendering. DEPRECATED!
	 */
	public static void updateWindows(final EvBasicWindow from)
		{
			SwingUtilities.invokeLater(new Runnable(){
			public void run()
				{
				updateWindowsPrivate(from);
				}
			});
		}


	private static void updateWindowsPrivate(EvBasicWindow from)
		{
		for (EvBasicWindow w : getWindowList())
			if (w!=from)
				{
				for (endrov.gui.window.EvBasicWindowHook h : w.basicWindowExtensionHook.values())
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
		EvBasicWindow.updateWindows(null);
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
	 * Get position of window from XML element. Returns upper-left corner if it fails
	 */
	public static Point getXMLposition(Element e) //throws Exception
		{
		try
			{
			int x = e.getAttribute("x").getIntValue();
			int y = e.getAttribute("y").getIntValue();
			return new Point(x, y);
			}
		catch (DataConversionException e1)
			{
			return new Point(0,0);
			}
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
		for (EvBasicWindow w : getWindowList())
			w.windowEventUserLoadedFile(d);
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
				List<File> data = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
				Iterator<File> i = data.iterator();
				while (i.hasNext())
					files.add(i.next());
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
							EndrovCore.waitUntilStartedUp();
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

	public void setBoundsEvWindow(Integer w, Integer h)
		{
		getEvw().pack();
		Rectangle r=getEvw().getBounds();
		if(w==null)
			w=r.width;
		if(h==null)
			h=r.height;
		getEvw().setBounds(new Rectangle(w,h));
		}
	
	public void setBoundsEvWindow(int x, int y, int width, int height)
		{
		getEvw().setBounds(new Rectangle(x, y, width, height));
		}

	public void setLocationEvWindow(Point p)
		{
		if(p!=null)
			getEvw().setLocation(p.x, p.y);
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
	public HashMap<Class<?>, EvBasicWindowHook> basicWindowExtensionHook = new HashMap<Class<?>, EvBasicWindowHook>();

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
	public EvBasicWindow()
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
				for(EvBasicWindow w:getWindowList())
					if(w.windowInstance==instanceCounter)
						same=true;
				} while(same);
			windowInstance = instanceCounter;
			}

		for (EvBasicWindowExtension e : basicWindowExtensions)
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
					if(showConfirmYesNoDialog("Endrov will quit. Settings will be reset next time you start the program. Do you wish to proceed?"))
						{
						EndrovCore.resetPersonalConfig();
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
					EndrovCore.savePluginList();
				else if (e.getSource()==miWebHome)
					EvBrowserUtil.displayURL(EndrovCore.websiteWikiPrefix+"Main_Page");
				else if (e.getSource()==miWebUser)
					EvBrowserUtil.displayURL(EndrovCore.websiteWikiPrefix+"Users_Guide");
				else if (e.getSource()==miWebPlugins)
					EvBrowserUtil.displayURL(EndrovCore.websiteWikiPrefix+"Plugins");
				else if (e.getSource()==miAbout)
					dialogAbout();
				else if (e.getSource()==miSysInfo)
					dialogSysInfo();
				else if (e.getSource()==miSaveConfig)
					EndrovCore.savePersonalConfig();
				else if (e.getSource()==miRegInfo)
					EvRegistrationDialog.runDialogNoLock();
				else if (e.getSource()==miOpenConfig)
					EndrovUtil.openExternalProgram(EvSystemUtil.getGlobalConfigEndrovDir());
				else if (e.getSource()==miReportBug)
					EvBrowserUtil.displayURL("http://sourceforge.net/tracker/?group_id=199554&atid=969958");
				else if(e.getSource()==miSetSwap)
					dialogSetSwap();
				}

		};

	private JMenuBar menubar = new JMenuBar();
	public JMenu menuFile = new JMenu("File");
	private JMenu menuMaintenance = new JMenu("Maintenance");
	private JMenu menuWindows = new JMenu("Windows");
	private JMenu menuOperation = new JMenu("Operation");
	// private JMenu menuInfo=new JMenu("Info");
	private JMenuItem miGC = new JMenuItem("Run garbage collection");
	private JMenuItem miResetPC = new JMenuItem("Reset personal config");
	private JMenuItem miSavePluginList = new JMenuItem("Save plugin list");
	private JMenuItem miToggleSplash = new JMenuItem("Toggle splash screen");
	private JMenuItem miOpenConfig = new JMenuItem("Open config directory");
	private JMenuItem miReportBug = new JMenuItem("Report bug");

	private JMenuItem miQuit = new JMenuItem("Exit", BasicIcon.iconMenuQuit);

	private JMenuItem miAbout = new JMenuItem("About");
	private JMenuItem miWebHome = new JMenuItem(EndrovCore.programName+" website");
	private JMenuItem miWebUser = new JMenuItem("User's guide");
	private JMenuItem miWebPlugins = new JMenuItem("Plugins");
	private JMenuItem miSysInfo = new JMenuItem("System information");
	private JMenuItem miSaveConfig = new JMenuItem("Save config now");
	private JMenuItem miRegInfo = new JMenuItem("Change registration information");
	private JMenuItem miSetSwap = new JMenuItem("Set swap directory");

	/**
	 * Add to the menu Window
	 */
	public void addMenuWindow(JMenuItem ni)
		{
		synchronized (ni) //TODO why on ni? not on menuWindows?
			{
			addMenuItemSorted(menuWindows, ni);
			}
		}
	
	
	public JMenu getCreateMenuWindowCategory(String category)
		{
		for(Component c:menuWindows.getMenuComponents())
			if(c instanceof JMenu)
				{
				JMenu m=(JMenu)c;
				if(m.getText().equals(category))
					return m;
				}
		JMenu m=new JMenu(category);
		addMenuWindow(m);
		return m;
		}
	

	/**
	 * Add to the menu Operation
	 */
	public void addMenuOperation(JMenuItem ni, String sortText)
		{
		if(sortText==null)
			addMenuItemSorted(menuOperation, ni);
		else
			addMenuItemSorted(menuOperation, ni, sortText);
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
		addMenubar(menuOperation);
		EvBasicWindow.addMenuItemSorted(menuFile, menuMaintenance, "sys_maintenance");
		menuMaintenance.add(miGC);
		menuMaintenance.add(miResetPC);
		menuMaintenance.add(miSavePluginList);
		menuMaintenance.add(miToggleSplash);
		menuMaintenance.add(miOpenConfig);
		menuMaintenance.add(miSaveConfig);
		menuMaintenance.add(miRegInfo);
		menuMaintenance.add(miSetSwap);
		 
		EvBasicWindow.addMenuItemSorted(menuFile, miQuit, "zquit");

		for (EvBasicWindowHook hook : basicWindowExtensionHook.values())
			hook.createMenus(this);

		JMenu mHelp = new JMenu("Help");

		mHelp.add(miAbout);
		mHelp.add(miWebHome);
		mHelp.add(miWebUser);
		mHelp.add(miWebPlugins);
		mHelp.add(miReportBug);
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
		miReportBug.addActionListener(listener);
		miSysInfo.addActionListener(listener);
		miSaveConfig.addActionListener(listener);
		miRegInfo.addActionListener(listener);
		miSetSwap.addActionListener(listener);
		}

	/**
	 * Show about dialog
	 */
	public static void dialogAbout()
		{
		String text = 
				EndrovCore.programName + " " + EvBuild.version + "\n"
				+ "Git hash: "+EvBuild.githash + "\n"
				+EndrovCore.website+"\n"
				+"\n"
				+"The core software is under the newer BSD license\n"
				+"Individual plugins and libraries may be under different licenses";
		showInformativeDialog(text);
		}
	
	private void dialogSetSwap()
		{
		File f=EndrovCore.getSwapDirectory();
		
		//FileChooser fc=new FileChooser(null, dialogType, title, message)
		JFileChooser fc=new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	
		if(f!=null)
			fc.setSelectedFile(f);

		int returnVal = fc.showOpenDialog(this);

		if(returnVal==JFileChooser.APPROVE_OPTION)
			{
			EndrovCore.setSwapDirectory(fc.getSelectedFile());
			}
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
				+"Image planes in memory: "+EvPixels.getNumLiveImages()+"\n"
				+"Image planes swapped to disk: "+EvImageSwap.getNumSwappedImage()+"\n"
				+jaiformats;
		showInformativeDialog(text);
		}

	/** Handle "preferences" from the Mac menu */
	public void dialogPreferences()
		{
		}

	/** Show the quit dialog */
	public static void dialogQuit()
		{
		if(showConfirmYesNoDialog("Are you sure you want to quit?"))
			EndrovCore.quit();
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
		windowFreeResources();
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
		return JOptionPane.showInputDialog(null, message, value);
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
	 */
	public static boolean showConfirmYesNoDialog(String question)
		{
		int option = JOptionPane.showConfirmDialog(null, question, EndrovCore.programName, JOptionPane.YES_NO_OPTION);
		return option==JOptionPane.YES_OPTION;
		}

	
	public enum DialogReturnStatus
	{
	YES, NO, CANCEL
	}
	
	/**
	 * Show dialog asking Yes/No/Cancel to a question
	 */
	public static DialogReturnStatus showConfirmYesNoCancelDialog(String question)
		{
		int option = JOptionPane.showConfirmDialog(null, question, EndrovCore.programName, JOptionPane.YES_NO_CANCEL_OPTION);
		if(option==JOptionPane.YES_OPTION)
			return DialogReturnStatus.YES;
		else if(option==JOptionPane.NO_OPTION)
			return DialogReturnStatus.NO;
		else if(option==JOptionPane.CANCEL_OPTION)
			return DialogReturnStatus.CANCEL;
		else
			throw new RuntimeException("Unexpected joptionpane error");
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
	public abstract void windowSavePersonalSettings(Element root);

	public abstract void windowLoadPersonalSettings(Element e);

	/**
	 * Called when a file has just been loaded and should be displayed in all
	 * windows
	 */
	public abstract void windowEventUserLoadedFile(EvData data);

	/**
	 * Called when the window is closed. Should take care of deallocating and stopping any threads
	 */
	public abstract void windowFreeResources();

	

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin(){}
	static
		{
		EndrovCore.addPersonalConfigLoader("storedwindow", new PersonalConfig()
			{
				public void loadPersonalConfig(Element eWindow)
					{
					String windowClassName=eWindow.getAttributeValue("class");
					try
						{
						EvBasicWindow window=(EvBasicWindow)Class.forName(windowClassName).newInstance();

						int x=Integer.parseInt(eWindow.getAttributeValue("boundsX"));
						int y=Integer.parseInt(eWindow.getAttributeValue("boundsY"));
						int w=Integer.parseInt(eWindow.getAttributeValue("boundsW"));
						int h=Integer.parseInt(eWindow.getAttributeValue("boundsH"));
						
						window.setBoundsEvWindow(x,y,w,h);
						window.windowLoadPersonalSettings(eWindow);
						}
					catch (Exception e1)
						{
						System.out.println("Could not create class "+windowClassName);
						e1.printStackTrace();
						}
					
					}

				public void savePersonalConfig(Element e)
					{
					// Settings for individual windows
					for (EvBasicWindow w : windowManager.getAllWindows())
						{
						
						Element eWindow=new Element("storedwindow");
						eWindow.setAttribute("class", w.getClass().getCanonicalName());

						Rectangle bounds=w.getBoundsEvWindow();
						eWindow.setAttribute("boundsX", ""+bounds.x);
						eWindow.setAttribute("boundsY", ""+bounds.y);
						eWindow.setAttribute("boundsW", ""+bounds.width);
						eWindow.setAttribute("boundsH", ""+bounds.height);
						
						w.windowSavePersonalSettings(eWindow);
						
						e.addContent(eWindow);
						
						}
					}
			});
		
		JInputManager.addGamepadMode("Active window", new JInputModeBasicWindow(), true);
		}
	}
