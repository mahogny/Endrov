/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowScript;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;

import java.util.HashSet;
import java.util.Set;

import endrov.core.log.EvLog;
import endrov.data.EvData;
import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.EvBasicWindowExtension;
import endrov.gui.window.EvBasicWindowHook;
import endrov.script.*;
import endrov.util.io.EvFileUtil;
import endrov.windowConsole.ConsoleWindow;

import org.jdom.*;




/**
 * Console Window. Provides a CLI in the GUI
 * 
 * @author Johan Henriksson
 */
public class ScriptWindow extends EvBasicWindow implements ActionListener
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;

	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	public Script script=new Script();
	
	//GUI components
	private JTextArea scriptArea=new JTextArea("\n\n");
	
	
	private JMenu scriptMenu=new JMenu("Script");
	private JMenuItem miRun=new JMenuItem("Run");
	private JMenuItem miNew=new JMenuItem("New");
	private JMenuItem miOpen=new JMenuItem("Open...");
	private JMenuItem miSave=new JMenuItem("Save");
	private JMenuItem miSaveAs=new JMenuItem("Save As...");

	
	private File currentFile=null;
	
	/**
	 * Store down settings for window into personal config file
	 */
	public void windowSavePersonalSettings(Element root)
		{
		}
	public void windowLoadPersonalSettings(Element e)
		{
		}

	
	
	public ScriptEngineFactory currentLanguage=null;

	
	/**
	 * Make a new window 
	 */
	public ScriptWindow()
		{
		//Menu
		addMainMenubarWindowSpecific(scriptMenu);
		scriptMenu.add(miNew);
		scriptMenu.add(miOpen);
		scriptMenu.add(miSave);
		scriptMenu.add(miSaveAs);
		scriptMenu.add(new JSeparator());
		scriptMenu.add(miRun);
		
		buildLanguageOptions(scriptMenu);
		
		miRun.addActionListener(this);
		miNew.addActionListener(this);
		miOpen.addActionListener(this);
		miSave.addActionListener(this);
		miSaveAs.addActionListener(this);
		
		miRun.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Event.CTRL_MASK));
		
		//Put GUI together
		setLayout(new BorderLayout());
		add(scriptArea,BorderLayout.CENTER);
		
		//Window overall things
		setCurrentFile(null);
		packEvWindow();
		setBoundsEvWindow(50,50,500,500);
		setVisibleEvWindow(true);
		}
	
	/**
	 * Build options for selecting language
	 * @param menu
	 */
	private void buildLanguageOptions(JMenu menu)
		{

		ScriptEngineManager scriptEngineManager=new ScriptEngineManager(ScriptWindow.class.getClassLoader());

		Set<ScriptEngineFactory> languages=new HashSet<ScriptEngineFactory>(scriptEngineManager.getEngineFactories());
		
		
		
		//languages.add(new BeanShellScriptEngineFactory());
		
		//scriptEngineManager.registerEngineName("BeanShell", new BeanShellScriptEngineFactory());

		//scriptEngineManager.put("test", );
		
		System.out.println("Scripting languages: "+scriptEngineManager.getEngineFactories());
		//http://download.oracle.com/javase/6/docs/technotes/guides/jar/jar.html#Service%20Provider
		//TODO let jars register themselves!
		//Is this because I have my own class loader?
		//http://www.dirisala.net/scripting/
		//http://java.sun.com/developer/technicalArticles/J2SE/Desktop/scripting/

		
		menu.add(new JSeparator());
		ButtonGroup grp=new ButtonGroup();
		
		for(final ScriptEngineFactory factory:languages)
			{
			JRadioButtonMenuItem miOptionLang=new JRadioButtonMenuItem("Use "+factory.getLanguageName());
			grp.add(miOptionLang);
			menu.add(miOptionLang);
			
			if(factory.getExtensions().contains("bsh"))
				{
				currentLanguage=factory;
				miOptionLang.setSelected(true);
				}
			
			miOptionLang.addActionListener(new ActionListener()
				{
				public void actionPerformed(ActionEvent e)
					{
					currentLanguage=factory;
					}
				});
			}

		}
	
	
	private Thread scriptThread=null;
	
	
	//TODO status indicator, if it is running
	
	@SuppressWarnings({ "deprecation" })
	private void stopThread()
		{
		if(scriptThread!=null)
			{
			scriptThread.stop();
			scriptThread=null;
			}
		}
	
	
	private void exec()
		{
		if(scriptThread!=null)
			stopThread();
		
		ConsoleWindow.openConsole(); //TODO maybe only do this if there is output


		scriptThread=new Thread()
			{
			@Override
			public void run()
				{
				//ScriptEngineManager factory = new ScriptEngineManager();
				
				//Class<?> cl=Class.forName(currentLanguage.getClass().getCanonicalName());
				
				
				try
					{
					Class<?> cl=ScriptWindow.class.getClassLoader().loadClass(currentLanguage.getClass().getCanonicalName());
					currentLanguage=(ScriptEngineFactory)cl.newInstance();
					}
				catch (Exception e1)
					{
					e1.printStackTrace();
					}
				
				ScriptEngine eng=currentLanguage.getScriptEngine();

				
				Writer w=new Writer()
					{
					StringBuffer sb=new StringBuffer();
					
					@Override
					public void write(char[] cbuf, int off, int len) throws IOException
						{
						for(int i=off;i<off+len;i++)
							{
							char c=cbuf[i];
							if(c=='\n')
								{
								flush();
								}
							else
								sb.append(c);
							}
						}
					
					@Override
					public void flush() throws IOException
						{
						EvLog.printLog(sb.toString());
						sb=new StringBuffer();
						}
					
					@Override
					public void close() throws IOException
						{
						flush();
						EvLog.printLog("-ok-");
						}
				};
				
				eng.getContext().setWriter(w);
				
				try
					{
					eng.eval(scriptArea.getText());
					}
				catch (ScriptException e)
					{
					EvLog.printError(e.getMessage(), null);
					//EvLog.printError(e);
					e.printStackTrace();
					}
				try
					{
					w.close();
					}
				catch (IOException e)
					{
					e.printStackTrace();
					}
				
				
				/*
				
				PipedInputStream pin=new PipedInputStream();
				final BufferedReader bin=new BufferedReader(new InputStreamReader(pin));
				new Thread()
					{
					public void run() {
					
					try
						{
						for(;;)
							{
							String line=bin.readLine();
							if(line==null)
								{
								EvLog.printLog("-ok-");
								return;
								}
							EvLog.printLog(line);
							}
						}
					catch (IOException e)
						{
						e.printStackTrace();
						}
					};
					}.start();
				
				try
					{
					Interpreter bsh=new Interpreter();
					PrintStream po=new PrintStream(new PipedOutputStream(pin));
					bsh.setOut(po);
					bsh.eval(scriptArea.getText());
					po.close();
					}
				catch (IOException e)
					{
					e.printStackTrace();
					}
				catch (EvalError e)
					{
					EvLog.printError(e.getMessage(), null);
					}
				*/
				
				stopThread();

				}
			};
			
		scriptThread.start();


		}
	
	private void setCurrentFile(File f)
		{
		currentFile=f;
		if(f==null)
			setTitleEvWindow("Script editor - <new>");
		else
			setTitleEvWindow("Script editor - "+f.getName());
		}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==miRun)
			exec();
		else if(e.getSource()==miNew)
			{
			setCurrentFile(null);
			scriptArea.setText("");
			}
		else if(e.getSource()==miOpen)
			{
			actionOpen();
			}
		else if(e.getSource()==miSaveAs)
			{
			actionSaveAs();
			}
		else if(e.getSource()==miSave)
			{
			if(currentFile==null)
				actionSaveAs();
			else
				writeOutFile(currentFile);
			}
		}
	
	
	private void actionOpen()
		{
		File f=EvBasicWindow.openDialogOpenFile();
		if(f!=null)
    	{
			try
				{
				scriptArea.setText(EvFileUtil.readFile(f));
				setCurrentFile(f);
				}
			catch (IOException e1)
				{
				showErrorDialog("Failed to open file: "+e1.getMessage());
				}
			}
		}
	
	
	private void actionSaveAs()
		{
		File f=EvBasicWindow.openDialogSaveFile(null);
		if(f!=null)
    	{
			writeOutFile(f);
			setCurrentFile(f);
			}
		}
	
	private void writeOutFile(File f)
		{
		try
			{
			EvFileUtil.writeFile(f, scriptArea.getText());
			}
		catch (IOException e1)
			{
			showErrorDialog("Failed to save file: "+e1.getMessage());
			}
		}


	
	/*
	 * (non-Javadoc)
	 * @see client.BasicWindow#dataChanged()
	 */
	public void dataChangedEvent()
		{
		}
	
	
	
	
	public void windowEventUserLoadedFile(EvData data){}
	public void windowFreeResources(){}

	@Override
	public String windowHelpTopic()
		{
		return "The script editor";
		}
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	private static ImageIcon iconWindow=new ImageIcon(ScriptWindow.class.getResource("tangoScript.png"));
	public static void initPlugin() {}
	static
		{
		EvBasicWindow.addBasicWindowExtension(new EvBasicWindowExtension()
					{
					public void newBasicWindow(EvBasicWindow w)
						{
						w.addHook(ScriptWindow.class,new Hook());
						}
					class Hook implements EvBasicWindowHook, ActionListener
						{
						public void createMenus(EvBasicWindow w)
							{
							JMenuItem mi=new JMenuItem("Script editor",iconWindow);
							mi.addActionListener(this);
							w.addMenuWindow(mi);
							}
						
						public void actionPerformed(ActionEvent e) 
							{
							new ScriptWindow();
							}
						
						public void buildMenu(EvBasicWindow w){}
						}
					});
		}

	
	}
