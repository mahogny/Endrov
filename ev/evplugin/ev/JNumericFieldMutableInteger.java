package evplugin.ev;

import java.lang.ref.WeakReference;

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
