package evplugin.ev;

import java.awt.event.*;
import javax.swing.*;

/**
 * JCheckBox connected to a mutable boolean
 * 
 * @author Johan Henriksson
 */
public class JCheckBoxMutableBoolean extends JCheckBox
	{
	static final long serialVersionUID=0;
	
	public JCheckBoxMutableBoolean(String text, final EvMutableBoolean b)
		{
		super(text, b.getValue());
		addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
				{
				b.setValue(isSelected());
				}
		});
		}
	}
