/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.gui.component;

import java.awt.event.*;
import java.lang.ref.WeakReference;

import javax.swing.*;

import endrov.core.observer.SimpleObserver;
import endrov.util.EvMutableBoolean;

/**
 * JCheckBox connected to a mutable boolean
 * 
 * @author Johan Henriksson
 */
public class JCheckBoxMutableBoolean extends JCheckBox
	{
	static final long serialVersionUID=0;
	
	public JCheckBoxMutableBoolean(String text, final EvMutableBoolean b, SimpleObserver obs, Object obso)
		{
		super(text, b.getValue());
		final WeakReference<SimpleObserver> wobs=new WeakReference<SimpleObserver>(obs);
		final WeakReference<Object> wobso=new WeakReference<Object>(obso);
		addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
				{
				b.setValue(isSelected());
				SimpleObserver so=wobs.get();
				if(so!=null)
					so.emit(wobso.get());
				}
		});
		}
	}
