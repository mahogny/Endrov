/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.basicWindow;

import javax.swing.JSpinner;

import endrov.util.EvDecimal;

/**
 * Simple evdecimal selector
 * @author Johan Henriksson
 *
 */
public class SpinnerSimpleEvDecimal extends JSpinner
	{
	private static final long serialVersionUID = 1L;

	public SpinnerSimpleEvDecimal()
		{
		setModel(new EvDecimalSpinnerModel());
		setEditor(new EvDecimalEditor(this));
		setValue(new EvDecimal(0));
		}
	
	public EvDecimal getDecimalValue()
		{
		return (EvDecimal)super.getValue();
		}

	public void setDecimalValue(EvDecimal v)
		{
		super.setValue(v);
		}

	public void setFrame(String f)
		{
		setDecimalValue(FrameControl.parseTime(f));
		}
	
	}
