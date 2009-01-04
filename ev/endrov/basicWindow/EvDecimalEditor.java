package endrov.basicWindow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import endrov.util.EvDecimal;


/**
 * Editor for EvDecimal spinners 
 * @author Johan Henriksson
 */	
public class EvDecimalEditor extends JTextField
	{
	static final long serialVersionUID=0;
	public EvDecimalEditor(final JSpinner sp)
		{
		addActionListener(new ActionListener()
			{public void actionPerformed(ActionEvent e){sp.getModel().setValue(Integer.parseInt(getText()));}});
		sp.getModel().addChangeListener(new ChangeListener()
			{public void stateChanged(ChangeEvent e){setText(""+(EvDecimal)sp.getModel().getValue());}});
		setText(""+(EvDecimal)sp.getModel().getValue());
		}
	}