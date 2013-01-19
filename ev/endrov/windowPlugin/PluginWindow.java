/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowPlugin;


import java.awt.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import endrov.core.*;
import endrov.data.EvData;
import endrov.gui.component.JMultiLineTableCellRenderer;
import endrov.gui.window.EvBasicWindow;

import org.jdom.*;

/**
 * Browse plugins
 * 
 * @author Johan Henriksson
 */
public class PluginWindow extends EvBasicWindow 
	{
	static final long serialVersionUID=0;
	
	private JTable tablePlugins=new JTable();


	

	
	/**
	 * Make a new window 
	 */
	public PluginWindow()
		{				
		updatePluginsTable();
		setLayout(new GridLayout(1,1));
		
		add(new JScrollPane(tablePlugins, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		
		tablePlugins.setDefaultRenderer(Object.class, new JMultiLineTableCellRenderer());
		
  	//Window overall things
  	setTitleEvWindow("Plugins");
  	packEvWindow();
  	setBoundsEvWindow(100,100,1000,600);
  	setVisibleEvWindow(true);
		}

	
	public void updatePluginsTable()
		{
		Vector<EvPluginManager> plugins;
		plugins=new Vector<EvPluginManager>();
		plugins.addAll(EvPluginManager.getPluginList());
		Collections.sort(plugins, new Comparator<EvPluginManager>(){
			public int compare(EvPluginManager o1, EvPluginManager o2)
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
		
		for(EvPluginManager info:plugins)
			{
			EvPluginDefinition def=info.pdef;
			model.addRow(new String[]{
					def.getPluginName(),
					def.getAuthor(),
					def.cite(),
					def.systemSupported() ? "Yes" : "No",
					EvPluginManager.isPluginLoaded(info) ? "Yes" : "No"
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

	public void windowSavePersonalSettings(Element e){}
	public void windowLoadPersonalSettings(Element e){}
	public void windowEventUserLoadedFile(EvData data){}
	public void windowFreeResources(){}

	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvBasicWindow.addBasicWindowExtension(new PluginWindowBasic());
		}

	}
