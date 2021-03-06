/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.gui.component;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * Simple integer selector
 * @author Johan Henriksson
 *
 */
public class JSpinnerSimpleInteger extends JSpinner
	{
	private static final long serialVersionUID = 1L;

	public JSpinnerSimpleInteger()
		{
		this(0,0,10000,1);
		}
	public JSpinnerSimpleInteger(int value, int min, int max, int step)
		{
		SpinnerNumberModel m=new SpinnerNumberModel(value, min, max, step);
		setModel(m);
		}
	
	public int getIntValue()
		{
		return (Integer)getValue();
		}
	
	public void setIntValue(int value)
		{
		setValue(value);
		}
	
	}
