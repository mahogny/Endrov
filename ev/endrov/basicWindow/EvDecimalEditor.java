/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.basicWindow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import endrov.util.EvDecimal;

/**
 * Editor for EvDecimal spinners
 * 
 * @author Johan Henriksson
 */
public class EvDecimalEditor extends JTextField
	{
	static final long serialVersionUID = 0;

	public EvDecimalEditor(final JSpinner sp)
		{
		setText(""+(EvDecimal) sp.getModel().getValue());
		addActionListener(new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
				{
				setFreeze(1);
				try
					{
					sp.getModel().setValue(new EvDecimal(getText()));
					}
				catch (Exception e1)
					{
					}
				setFreeze(-1);
				}
			});
		getDocument().addDocumentListener(new DocumentListener()
			{
			public void removeUpdate(DocumentEvent arg0)
				{
				setFreeze(1);
				try
					{
					sp.getModel().setValue(new EvDecimal(getText()));
					}
				catch (Exception e1)
					{
					}
				setFreeze(-1);
				}
			public void insertUpdate(DocumentEvent arg0)
				{
				setFreeze(1);
				try
					{
					sp.getModel().setValue(new EvDecimal(getText()));
					}
				catch (Exception e1)
					{
					}
				setFreeze(-1);
				}
			public void changedUpdate(DocumentEvent arg0)
				{
				setFreeze(1);
				try
					{
					sp.getModel().setValue(new EvDecimal(getText()));
					}
				catch (Exception e1)
					{
					}
				setFreeze(-1);
				}
			});
		sp.getModel().addChangeListener(new ChangeListener()
			{
			public void stateChanged(ChangeEvent e)
				{
				if(!getLock())
					{
					setFreeze(1);
					setText(""+(EvDecimal) sp.getModel().getValue());
					setFreeze(-1);
					}
				}
			});
		}
	
	int lock=0;
	private void setFreeze(int v)
		{
		lock+=v;
		}
	private boolean getLock()
		{
		return lock!=0;
		}
	}
