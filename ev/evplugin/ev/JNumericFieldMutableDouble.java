package evplugin.ev;

public class JNumericFieldMutableDouble extends JNumericField
	{
	static final long serialVersionUID=0;

	public JNumericFieldMutableDouble(final EvMutableDouble d)
		{
		super(d.doubleValue());
		addNumericListener(new JNumericField.JNumericListener()
			{
			public void numericChanged(JNumericField source)
				{
				d.setValue(getDouble(0));
				}
			});
		}
	
	}
