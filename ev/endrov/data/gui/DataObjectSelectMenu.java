/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.data.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.data.EvPath;

/**
 * Add menu entries for selecting a type of object
 * 
 * @author Johan Henriksson
 */
public class DataObjectSelectMenu
	{
	public interface Callback<E>
		{
		public void select(EvData data, EvPath path, E con);
		}
	
	public static <E> void create(JMenu menu, Class<E> type, Callback<E> cb)
		{
		for(EvData data:EvData.openedData)
			{
			LinkedList<String> path=new LinkedList<String>();
			path.add(data.getMetadataName());
			JMenuItem dataMenu=createObjectMenu(data, path, data, type, cb);
			if(dataMenu!=null)
				menu.add(dataMenu);
			}
		}
	
	
	@SuppressWarnings("unchecked")
	private static <E> JMenuItem createObjectMenu(final EvData data, final LinkedList<String> path, EvContainer con, Class<E> type, final Callback<E> cb)
		{
		JMenu submenu=new JMenu(path.getLast()+".");
		for(Map.Entry<String, EvObject> e:con.metaObject.entrySet())
			{
			path.addLast(e.getKey());
			if(type.isInstance(e.getValue()))
				{
				final E obe=(E)e.getValue();
				JMenuItem mi=new JMenuItem(e.getKey());
				submenu.add(mi);
				
				mi.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e)
						{
						cb.select(data, new EvPath(data, path), obe);
						}
				});
				
				}
			
			JMenuItem subitem=createObjectMenu(data, path, e.getValue(), type, cb);
			if(subitem!=null)
				submenu.add(subitem);
			
			path.removeLast();
			}

		if(submenu.getMenuComponentCount()>0)
			return submenu;
		else
			return null;
		}

	
	}
