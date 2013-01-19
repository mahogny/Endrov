/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.gui.component;

import javax.swing.JSpinner;

import endrov.typeFrameTime.EvFrameEditor;
import endrov.util.math.EvDecimal;

/**
 * Simple frame selector
 * @author Johan Henriksson
 *
 */
public class JSpinnerSimpleEvFrame extends JSpinner
	{
	private static final long serialVersionUID = 1L;

//	public Color normalColor;
	
	public JSpinnerSimpleEvFrame()
		{
	//	normalColor=getBackground();
		setModel(new EvDecimalSpinnerModel());
		setEditor(new EvFrameEditor(this));
		}
	
	
	public EvDecimal getDecimalValue()
		{
		return (EvDecimal)getValue();
		}
	
	public void setFrame(String f)
		{
		setValue(EvFrameControl.parseTime(f));
		}
	
	}
