/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.gui.component;

import java.lang.ref.WeakReference;

import endrov.core.observer.SimpleObserver;
import endrov.util.EvMutableDouble;

/**
 * Numeric field linked to a mutable double
 * @author Johan Henriksson
 *
 */
public class JNumericFieldMutableDouble extends JNumericField
	{
	static final long serialVersionUID=0;

	public JNumericFieldMutableDouble(final EvMutableDouble d, SimpleObserver obs, Object obso)
		{
		super(d.doubleValue());
		final WeakReference<SimpleObserver> wobs=new WeakReference<SimpleObserver>(obs);
		final WeakReference<Object> wobso=new WeakReference<Object>(obso);
		addNumericListener(new JNumericField.JNumericListener()
			{
			public void numericChanged(JNumericField source)
				{
				d.setValue(getDouble(0));
				SimpleObserver so=wobs.get();
				if(so!=null)
					so.emit(wobso.get());
				}
			});
		}
	
	}
