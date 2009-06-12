package endrov.basicWindow;

import javax.swing.JSpinner;

import endrov.util.EvDecimal;

/**
 * Simple frame selector
 * @author Johan Henriksson
 *
 */
public class EvComboSimpleFrame extends JSpinner
	{
	private static final long serialVersionUID = 1L;

	public EvComboSimpleFrame()
		{
		setModel(new EvDecimalSpinnerModel());
		setEditor(new EvDecimalEditor(this));
		}
	
	public EvDecimal getDecimalValue()
		{
		return (EvDecimal)getValue();
		}
	
	public void setFrame(String f)
		{
		setValue(FrameControl.parseTime(f));
		}
	
	}
