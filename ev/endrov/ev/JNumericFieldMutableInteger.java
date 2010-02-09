/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.ev;

import java.lang.ref.WeakReference;

/**
 * Numeric field linked to a mutable integer
 * @author Johan Henriksson
 *
 */
public class JNumericFieldMutableInteger extends JNumericField
	{
	static final long serialVersionUID=0;

	public JNumericFieldMutableInteger(final EvMutableInteger d, SimpleObserver obs, Object obso)
		{
		super(d.intValue());
		final WeakReference<SimpleObserver> wobs=new WeakReference<SimpleObserver>(obs);
		final WeakReference<Object> wobso=new WeakReference<Object>(obso);
		addNumericListener(new JNumericField.JNumericListener()
			{
			public void numericChanged(JNumericField source)
				{
				d.setValue(getInt(0));
				SimpleObserver so=wobs.get();
				if(so!=null)
					so.emit(wobso.get());
				}
			});
		}
	
	}
