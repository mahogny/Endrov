package evplugin.consoleWindow;

import java.awt.*;
import java.awt.event.*;
import java.lang.ref.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.io.*;

import evplugin.basicWindow.*;
import evplugin.data.EvData;
import evplugin.ev.*;
import evplugin.keyBinding.KeyBinding;
import evplugin.script.*;
import org.jdom.*;


/**
 * Console Window. Provides a CLI in the GUI
 * 
 * @author Johan Henriksson
 */
public class ConsoleWindow extends BasicWindow implements ActionListener, KeyListener, ChangeListener
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;

	public static void initPlugin() {}
	static
		{
		BasicWindow.addBasicWindowExtension(new ConsoleBasic());
		EV.personalConfigLoaders.put("consolewindow",new PersonalConfig()
			{
			public void loadPersonalConfig(Element e)
				{
				try
					{
					int x=e.getAttribute("x").getIntValue();
					int y=e.getAttribute("y").getIntValue();
					int w=e.getAttribute("w").getIntValue();
					int h=e.getAttribute("h").getIntValue();
					new ConsoleWindow(x,y,w,h);
					}
				catch (DataConversionException e1)
					{
					e1.printStackTrace();
					}
				}
			public void savePersonalConfig(Element e){}
			});
		}
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	
	
	/** Last component with focus remembered so this one can be refocused */
	private WeakReference<Component> lastFocusComponent=null;
	private WeakReference<BasicWindow> lastFocusFrame=null;
	
	//GUI components
	private JTextArea history=new JTextArea();
	private JTextFieldHistorized commandLine=new JTextFieldHistorized();
	private JMenu consoleMenu=new JMenu("Console");
	private JMenuItem miShowTraces=new JCheckBoxMenuItem("Show traces",false);
	
	/**
	 * Take new log events and put them in console history
	 */
	private Log consoleLog=new Log()
		{
		public void listenDebug(String s)
			{
			history.append(s);
			history.append("\n");
			}
	
		public void listenError(String s, Exception e)
			{
			if(s!=null)
				{
				history.append(s);
				history.append("\n");
				}
			if(e!=null)
				{
				StringWriter sw=new StringWriter();
				PrintWriter s2=new PrintWriter(sw);
				e.printStackTrace(s2);
				s2.flush();
	
				history.append("Exception message: ");
				history.append("\n");
				history.append(sw. toString());
				}
			}
	
		public void listenLog(String s)
			{
			history.append(s);
			history.append("\n");
			}
		};

		
	/**
	 * Remove log listener once window is closed. Otherwise the window cannot be eliminated.
	 */
	private WindowListener wlist=new WindowListener()
		{
		public void windowActivated(WindowEvent e){}
		public void windowClosed(WindowEvent e)
			{
			Log.listeners.remove(consoleLog);
			}
		public void windowClosing(WindowEvent e){}
		public void windowDeactivated(WindowEvent e)
			{
			lastFocusComponent=new WeakReference<Component>(null);
			}
		public void windowDeiconified(WindowEvent e){}
		public void windowIconified(WindowEvent e){}
		public void windowOpened(WindowEvent e){}
		};

	
	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e)
		{
		if(KeyBinding.get(KEY_GETCONSOLE).typed(e))
//		if(e.getKeyCode()==KeyEvent.VK_ESCAPE)
			commandLine.requestFocus();
		}
	
	
	
	
	/**
	 * Store down settings for window into personal config file
	 */
	public void windowPersonalSettings(Element root)
		{
		Element e=new Element("consolewindow");
		setXMLbounds(e);
		root.addContent(e);
		}

	
	/**
	 * Make a new window at default location
	 */
	public ConsoleWindow()
		{
		this(0,700,1200,200);
		}
	
	/**
	 * Make a new window at some specific location
	 */
	public ConsoleWindow(int x, int y, int w, int h)
		{
		JScrollPane hs=new JScrollPane(history, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		history.setEditable(false);
		commandLine.addActionListener(this);
		history.addKeyListener(this);
		addKeyListener(this);
		miShowTraces.addChangeListener(this);
	
		//Menu
		addMenubar(consoleMenu);
		consoleMenu.add(miShowTraces);
		
		//Put GUI together
		setLayout(new BorderLayout());
		add(hs,BorderLayout.CENTER);
		add(commandLine,BorderLayout.SOUTH);
		
		//Log handling
		Log.listeners.add(consoleLog);
		evw.addWindowListener(wlist);
		history.append(Log.memoryLog.get());
		
		//Window overall things
		setTitleEvWindow("Console Window");
		packEvWindow();
		setBoundsEvWindow(x,y,w,h);
		setVisibleEvWindow(true);
		}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==commandLine)
			{
			String cmd=commandLine.getText();
			commandLine.setText("");
			addHistory("> "+cmd+"\n");
			if(!cmd.equals(""))
				{
				try
					{
					//TODO: why is error not given back here?
					Exp exp=Script.evalExp(cmd);
					if(exp==null)
						addHistory("-\n");
					else
						addHistory(""+exp+"\n");
					}
				catch(Exception ex)
					{
					if(miShowTraces.isSelected())
						{
						StringWriter sw=new StringWriter();
						PrintWriter pw=new PrintWriter(sw);
						ex.printStackTrace(pw);
						pw.flush();
						addHistory(ex.getMessage()+"\n"+sw.toString());
						}
					else
						addHistory(ex.getMessage()+"\n");
					}
				}
			returnFocus();
			}
		}
	


	
	
	/**
	 * Return focus to window that switched here
	 */
	public void returnFocus()
		{
		Component c=lastFocusComponent.get();
		BasicWindow f=lastFocusFrame.get();
		if(c!=null)
			{
			c.requestFocus();
			lastFocusComponent=new WeakReference<Component>(null);
			if(f!=null)
				{
				f.evw.toFront();
				System.out.println("tofront");
				}
			}
		}

	
	/*
	 * (non-Javadoc)
	 * @see client.BasicWindow#dataChanged()
	 */
	public void dataChangedEvent()
		{
		}
	
	/**
	 * Add text to the history
	 * @param s Text to be added
	 */
	public void addHistory(String s)
		{
		history.append(s);
		}


	/**
	 * Give focus to console
	 * @param me Component with current focus
	 */
	public static void focusConsole(BasicWindow frame, Component me)
		{
		ConsoleWindow c=getConsole();
		if(c==null) c=new ConsoleWindow();
		c.evw.toFront();
		c.lastFocusComponent=new WeakReference<Component>(me);
		c.lastFocusFrame=new WeakReference<BasicWindow>(frame);
		c.commandLine.requestFocus();
		}


	//TODO: free from basicwindow? 
	/**
	 * Get a handle to the console
	 * @return The console or null if none is open
	 */
	public static ConsoleWindow getConsole()
		{
		for(BasicWindow w:BasicWindow.getWindowList())
			if(w instanceof ConsoleWindow)
				return (ConsoleWindow)w;
		return null;
		}
	
	
	public void stateChanged(ChangeEvent arg0)
		{
		}
	
	public void finalize()
		{
		System.out.println("removing console window");
		}
	
	public void loadedFile(EvData data){}

	}
