/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowConsole;

import java.awt.*;
import java.awt.event.*;
import java.lang.ref.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.io.*;

import endrov.core.log.EvLog;
import endrov.data.EvData;
import endrov.gui.EvSwingUtil;
import endrov.gui.keybinding.KeyBinding;
import endrov.gui.window.EvBasicWindow;
import endrov.script.*;
import endrov.util.FuncAB;

import org.jdom.*;


/**
 * Console Window. Provides a CLI in the GUI
 * 
 * @author Johan Henriksson
 */
public class ConsoleWindow extends EvBasicWindow implements ActionListener, KeyListener, ChangeListener
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;

	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	public Script script=new Script();
	
	/** Last component with focus remembered so this one can be refocused */
	private WeakReference<Component> lastFocusComponent=new WeakReference<Component>(null);
	private WeakReference<EvBasicWindow> lastFocusFrame=new WeakReference<EvBasicWindow>(null);
	
	//GUI components
	private JTextArea history=new JTextArea();
	private JTextFieldHistorized commandLine=new JTextFieldHistorized();
	private JTextArea commandArea=new JTextArea("\n\n");
	private JScrollPane hs=new JScrollPane(history, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	
	
	private JMenu consoleMenu=new JMenu("Console");
	private JMenuItem miShowTraces=new JCheckBoxMenuItem("Show traces",false);
	private JMenuItem miMultiLineInput=new JCheckBoxMenuItem("Multi-line input",false);
	private JButton bMultiGo=new JButton("Run");	
	private JPanel pMulti=new JPanel(new BorderLayout());
	

		

	
	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e)
		{
		if(KeyBinding.get(KEY_GETCONSOLE).typed(e))
//		if(e.getKeyCode()==KeyEvent.VK_ESCAPE)
			commandLine.requestFocus();
		}
	
	
	
	
	public void windowSavePersonalSettings(Element root)
		{
		}
	public void windowLoadPersonalSettings(Element e)
		{
		}
	
	
	public OutputStream consoleOS;
	
	/**
	 * Make a new window
	 */
	public ConsoleWindow()
		{
		this(true);
		}
	
	/**
	 * Make a new window
	 */
	public ConsoleWindow(boolean visible)
		{
		commandArea.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e){}
			public void keyTyped(KeyEvent e){}
			public void keyReleased(KeyEvent e){
			if(e.getKeyCode()==KeyEvent.VK_ENTER && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK)!=0)
				{
				String cmd=commandArea.getText();
				execLine(cmd);
				}
			}
		});
		
		
		history.setEditable(false);
		commandLine.addActionListener(this);
		history.addKeyListener(this);
		addKeyListener(this);
		miShowTraces.addChangeListener(this);
		miMultiLineInput.addActionListener(this);

		bMultiGo.addActionListener(this);
		
		pMulti.add(commandArea,BorderLayout.CENTER);
		pMulti.add(bMultiGo,BorderLayout.EAST);

		/*
		consoleOS=new OutputStream(){
			public void write(int b) throws IOException
				{
				
				
				System.out.print("cons "+Character.toString((char)b));
				}
		};
		script.bsh.setOut(new PrintStream(consoleOS));
*/
		
		//Menu
		addMainMenubarWindowSpecific(consoleMenu);
		consoleMenu.add(miShowTraces);
		consoleMenu.add(miMultiLineInput);
		
		//Put GUI together
		setLayout(new BorderLayout());
		add(hs,BorderLayout.CENTER);
		add(commandLine,BorderLayout.SOUTH);

		//Grab previous log from memory
		history.append(EvLog.memoryLog.get());
		
		//Ensure console gets log events, if not previously installed
		ConsoleLogger.install();
		
		
		//Window overall things
		setTitleEvWindow("Console");
		packEvWindow();
		setBoundsEvWindow(0,500,600,200);
		setVisibleEvWindow(visible);
		}
	
	
	private void execLine(String cmd)
		{
		if(!cmd.equals(""))
			{
			try
				{
				//TODO: why is error not given back here?
				Object exp=script.eval(cmd);
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
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==commandLine || e.getSource()==bMultiGo)
			{
			String cmd=commandLine.getText();
			addHistory("> "+cmd+"\n");
			commandLine.setText("");
			execLine(cmd);
			}
		else if(e.getSource()==miMultiLineInput)
			{
			if(miMultiLineInput.isSelected())
				{
				remove(commandLine);
				add(pMulti,BorderLayout.SOUTH);
/*				commandArea.setVisible(true);
				commandLine.setVisible(false);*/
				revalidate();
	//			repaint();
				scrollEnd();
				}
			else
				{
				remove(pMulti);
				add(commandLine,BorderLayout.SOUTH);
		/*		commandArea.setVisible(false);
				commandLine.setVisible(true);*/
				revalidate();
			//	repaint();
				scrollEnd();
				}
			
			}
		}
	


	
	
	/**
	 * Return focus to window that switched here
	 */
	public void returnFocus()
		{
		Component c=lastFocusComponent.get();
		EvBasicWindow f=lastFocusFrame.get();
		if(c!=null)
			{
			c.requestFocus();
			lastFocusComponent=new WeakReference<Component>(null);
			if(f!=null)
				f.getEvw().toFront();
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
	 */
	public void addHistory(final String s)
		{
		SwingUtilities.invokeLater(new Runnable(){
			public void run()
				{
				history.append(s);
				scrollEnd();
				}
		});
//		hs.scrollRectToVisible(		  new Rectangle(0,history.getHeight()-2,1,1));
		}

	public void scrollEnd()
		{
		history.setCaretPosition(history.getText().length() );
		}

	/**
	 * Give focus to console. Open console if needed.
	 * 
	 * @param me Component with current focus
	 */
	public static void focusConsole(final EvBasicWindow frame, final Component me)
		{
		EvSwingUtil.invokeAndWaitIfNeeded(new Runnable()
			{
			public void run()
				{
				ConsoleWindow c=openConsole();
				c.getEvw().toFront();
				c.lastFocusComponent=new WeakReference<Component>(me);
				c.lastFocusFrame=new WeakReference<EvBasicWindow>(frame);
				c.commandLine.requestFocus();
				}
			});
		}

	public static ConsoleWindow openConsole()
		{
		return openConsole(true);
		}
	
	/**
	 * Open console if not already open
	 */
	private static ConsoleWindow openConsole(final boolean defaultVisible)
		{
		return EvSwingUtil.runInSwingThread(new FuncAB<Object, ConsoleWindow>()
			{
			public ConsoleWindow func(Object in)
				{
				System.out.println("Open console "+defaultVisible);
				ConsoleWindow c=getConsole();
				if(c==null)
					c=new ConsoleWindow(defaultVisible);
				if(defaultVisible)
					c.setVisibleEvWindow(true);
				return c;
				}
			}, null);
		}

	
	/**
	 * Get a handle to the console
	 * 
	 * @return The console or null if none is open
	 */
	public static ConsoleWindow getConsole()
		{
		for(EvBasicWindow w:EvBasicWindow.getWindowList())
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
	
	public void windowEventUserLoadedFile(EvData data){}
	public void windowFreeResources(){}

	@Override
	public String windowHelpTopic()
		{
		return "The console window";
		}
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvBasicWindow.addBasicWindowExtension(new ConsoleBasic());
		}


	
	}
