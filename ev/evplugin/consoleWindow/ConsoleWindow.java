package evplugin.consoleWindow;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import evplugin.basicWindow.*;
import evplugin.ev.*;
import evplugin.script.*;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Vector;

public class ConsoleWindow extends BasicWindow implements ActionListener, FocusListener, KeyListener
	{
	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e)
		{
		if(e.getKeyCode()==KeyEvent.VK_ESCAPE)
			commandLine.requestFocus();
		}


	static final long serialVersionUID=0;

	public static void initPlugin() {}
	static
		{
		BasicWindow.addBasicWindowExtension(new ConsoleBasic());
		EV.personalConfigLoaders.put("consolewindow",new PersonalConfig()
			{
			public void loadPersonalConfig(Vector<String> arg)
				{
				new ConsoleWindow(
						Integer.parseInt(arg.get(1)),Integer.parseInt(arg.get(2)),
						Integer.parseInt(arg.get(3)),Integer.parseInt(arg.get(4)));
				}
			public String savePersonalConfig(){return "";}
			});

		}
	
	/** Last component with focus remembered so this one can be refocused */
	private Component lastFocusComponent=null;
	private JFrame lastFocusFrame=null;
	
	//GUI components
	private JTextArea history=new JTextArea();
	private JTextFieldHistorized commandLine=new JTextFieldHistorized();
	
	/**
	 * Store down settings for window into personal config file
	 */
	public String windowPersonalSettings()
		{
		Rectangle r=getBounds();
		return "consolewindow "+r.x+" "+r.y+" "+r.width+" "+r.height+";";
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
		
		//Put GUI together
		setLayout(new BorderLayout());
		add(hs,BorderLayout.CENTER);
		add(commandLine,BorderLayout.SOUTH);
		
		//Window overall things
		setTitle(EV.programName+" Console Window");
		pack();
		setBounds(x,y,w,h);
		setVisible(true);
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
					addHistory(""+exp+"\n");
					}
				catch(Exception ex)
					{
					StringWriter sw=new StringWriter();
					PrintWriter pw=new PrintWriter(sw);
					ex.printStackTrace(pw);
					pw.flush();
					addHistory(ex.getMessage()+"\n"+sw.toString());
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
		if(lastFocusComponent!=null)
			{
			lastFocusComponent.requestFocus();
			lastFocusComponent=null;
			lastFocusFrame.toFront();
			}
		}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
	 */
	public void focusGained(FocusEvent e) {}

	/**
	 * Called when focus lost. Forget which object was last focused when this happens.
	 */
	public void focusLost(FocusEvent e)
		{
		lastFocusComponent=null;
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
	public static void focusConsole(JFrame frame, Component me)
		{
		ConsoleWindow c=getConsole();
		c.toFront();
		if(c==null) c=new ConsoleWindow();
		c.lastFocusComponent=me;
		c.lastFocusFrame=frame;
		c.commandLine.requestFocus();
		}


	//TODO: free from basicwindow? 
	/**
	 * Get a handle to the console
	 * @return The console or null if none is open
	 */
	public static ConsoleWindow getConsole()
		{
		for(BasicWindow w:BasicWindow.windowList)
			if(w instanceof ConsoleWindow)
				return (ConsoleWindow)w;
		return null;
		}
	
	
	}
