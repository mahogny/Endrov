/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.dataBrowser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import org.jdom.Element;

import endrov.basicWindow.BasicWindow;
import endrov.basicWindow.BasicWindowExtension;
import endrov.basicWindow.BasicWindowHook;
import endrov.data.EvData;

/**
 * Data browsing - work on objects
 * @author Johan Henriksson
 *
 */
public class DataBrowserWindow extends BasicWindow
	{
	private static final long serialVersionUID = 1L;

	
	public DataBrowserWindow()
		{
		packEvWindow();
		setTitleEvWindow("Data Browser");
		setVisible(true);
		}
	
	/**
	 * Essentially: only the data tree. allow new operations to be registered.
	 * either on right-click or by clicking on a button. or menu?
	 * 
	 * actually, EvSelection can point to EvContainer. then operations can go into data menu
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	
	
	@Override
	public void dataChangedEvent()
		{
		}

	@Override
	public void freeResources()
		{
		}

	@Override
	public void loadedFile(EvData data)
		{
		}

	@Override
	public void windowSavePersonalSettings(Element e)
		{
		}
	
	
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		BasicWindow.addBasicWindowExtension(new DataBrowserBasic());
		/*
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
			});*/
		}
	
	
	/**
	 * Extension to BasicWindow
	 * @author Johan Henriksson
	 */
	private static class DataBrowserBasic implements BasicWindowExtension
		{
		public void newBasicWindow(BasicWindow w)
			{
			w.basicWindowExtensionHook.put(this.getClass(),new Hook());
			}
		private class Hook implements BasicWindowHook, ActionListener
			{
			public void createMenus(BasicWindow w)
				{
				JMenuItem mi=new JMenuItem("Data Browser",new ImageIcon(getClass().getResource("tangoConsole.png")));
				mi.addActionListener(this);
				w.addMenuWindow(mi);
				}
			
			public void actionPerformed(ActionEvent e) 
				{
				new DataBrowserWindow();
				}
			
			public void buildMenu(BasicWindow w){}
			}
		}

	}
