/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.pluginWindow;


import java.awt.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import endrov.basicWindow.*;
import endrov.data.EvData;
import endrov.ev.*;
import endrov.util.MultiLineTableCellRenderer;

import org.jdom.*;

/**
 * Browse plugins
 * 
 * @author Johan Henriksson
 */
public class PluginWindow extends BasicWindow 
	{
	static final long serialVersionUID=0;
	
	private JTable tablePlugins=new JTable();

	/**
	 * Store down settings for window into personal config file
	 */
	public void windowSavePersonalSettings(Element root)
		{
		}

	

	/**
	 * Make a new window at default location
	 */
	public PluginWindow()
		{
		this(100,100,1000,600);
		}
	
	/**
	 * Make a new window at some specific location
	 */
	public PluginWindow(int x, int y, int w, int h)
		{				
		updatePluginsTable();
		setLayout(new GridLayout(1,1));
		
		add(new JScrollPane(tablePlugins, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		
		tablePlugins.setDefaultRenderer(Object.class, new MultiLineTableCellRenderer());
		
  	//Window overall things
  	setTitleEvWindow("Plugins");
  	packEvWindow();
  	setVisibleEvWindow(true);
  	setBoundsEvWindow(x,y,w,h);
		}

	
	public void updatePluginsTable()
		{
		Vector<PluginInfo> plugins;
		plugins=new Vector<PluginInfo>();
		plugins.addAll(PluginInfo.getPluginList());
		Collections.sort(plugins, new Comparator<PluginInfo>(){
			public int compare(PluginInfo o1, PluginInfo o2)
				{
				return o1.toString().compareTo(o2.toString());
				}
		});
		
		
		DefaultTableModel model=new DefaultTableModel(new String[][]{}, new String[]{"Name","Author","Cite","System supported?","Loaded?"}){
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int column)
				{
				return false;
				}
		};
		
		for(PluginInfo info:plugins)
			{
			PluginDef def=info.pdef;
			model.addRow(new String[]{
					def.getPluginName(),
					def.getAuthor(),
					def.cite(),
					def.systemSupported() ? "Yes" : "No",
					PluginInfo.isPluginLoaded(info) ? "Yes" : "No"
			});
			}
		tablePlugins.setModel(model);
		}
	

	/*
	 * (non-Javadoc)
	 * @see client.BasicWindow#dataChanged()
	 */
	public void dataChangedEvent()
		{
		}

	public void loadedFile(EvData data){}
	public void freeResources(){}

	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		BasicWindow.addBasicWindowExtension(new PluginWindowBasic());
		}

	}
