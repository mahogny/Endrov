package endrov.basicWindow;

import javax.swing.JSpinner;

import endrov.frameTime.EvFrameEditor;
import endrov.util.EvDecimal;

/**
 * Simple frame selector
 * @author Johan Henriksson
 *
 */
public class SpinnerSimpleEvFrame extends JSpinner
	{
	private static final long serialVersionUID = 1L;

	public SpinnerSimpleEvFrame()
		{
		setModel(new EvDecimalSpinnerModel());
		setEditor(new EvFrameEditor(this));
		
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
