package evplugin.ev;

import java.lang.ref.WeakReference;

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
