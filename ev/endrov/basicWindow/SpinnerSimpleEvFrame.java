/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
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
