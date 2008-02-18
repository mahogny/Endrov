package evplugin.ev;

import java.awt.event.*;
import java.lang.ref.WeakReference;

import javax.swing.*;

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
