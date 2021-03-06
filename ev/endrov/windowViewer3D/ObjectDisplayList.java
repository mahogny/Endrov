/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowViewer3D;

import java.awt.*;
import java.awt.event.*;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import endrov.data.*;


/**
 * 
 * @author Johan Henriksson
 */
public class ObjectDisplayList extends JPanel
	{
	public static final long serialVersionUID=0;
	
	public ObjectDisplayList()
		{
		setBorder(BorderFactory.createTitledBorder("Display"));
		setLayout(new GridBagLayout());
		updateList();
		}

	private WeakHashMap<EvObject, Object> toNotDisplay=new WeakHashMap<EvObject, Object>();
	private WeakReference<EvContainer> evdata=new WeakReference<EvContainer>(null);
	//can ask to be notified
	
	private Vector<ChangeListener> listeners=new Vector<ChangeListener>();
	
	public void addChangeListener(ChangeListener l)
		{
		listeners.add(l);
		}
	public void removeChangeListener(ChangeListener l)
		{
		listeners.remove(l);
		}
	
	
	public void setData(EvContainer evdata)
		{
		this.evdata=new WeakReference<EvContainer>(evdata);
		updateList();
		}
	
	public boolean toDisplay(EvObject ob)
		{
		return !toNotDisplay.containsKey(ob);
		}
	
	
	public void updateList()
		{
		removeAll();
		EvContainer d=evdata.get();
		if(d!=null && !d.metaObject.isEmpty())
			{
			int countb=0;
			for(Map.Entry<String, EvObject> entry:d.metaObject.entrySet())
				{
				final EvObject thisObject=entry.getValue();
				JCheckBox cb=new JCheckBox(""+entry.getKey()+" "+thisObject.getMetaTypeDesc(),toDisplay(thisObject));
				GridBagConstraints cr=new GridBagConstraints();	cr.weightx=1;	cr.gridy=countb;	cr.fill=GridBagConstraints.HORIZONTAL;
				add(cb,cr);
				countb++;
				
				cb.addItemListener(new ItemListener()
					{
						public void itemStateChanged(ItemEvent e)
							{
							if(((JCheckBox)e.getSource()).isSelected())
								toNotDisplay.remove(thisObject);
							else
								toNotDisplay.put(thisObject,null);
							for(ChangeListener l:listeners)
								l.stateChanged(new ChangeEvent(ObjectDisplayList.this));
							}
					});
				}
			}
		else
			{
			add(new JLabel("<empty>"));
			}
		revalidate();
		}
	
	
	
	
	
	public void hideObject(EvObject o)
		{
		toNotDisplay.put(o,null);
		for(ChangeListener l:listeners)
			l.stateChanged(new ChangeEvent(this));
		
		}
	
	
	}
