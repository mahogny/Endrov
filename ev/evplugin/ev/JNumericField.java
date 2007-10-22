package evplugin.ev;

import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

/**
 * Custom controller to input numbers. One single function will be called upon update
 * 
 * @author Johan Henriksson
 */
public class JNumericField extends JTextField
	{
	static final long serialVersionUID=0;
	
	public interface JNumericListener
		{
		public void numericChanged(JNumericField source);
		}
	

	private LinkedList<JNumericListener> listeners=new LinkedList<JNumericListener>(); 

	
	public JNumericField(int v)
		{
		setText(""+v);
		makeReg();
		}	
	public JNumericField(double v)
		{
		setText(""+v);
		makeReg();
		}
	

	private void makeReg()
		{
		final JNumericField thethis=this;
		getDocument().addDocumentListener(new DocumentListener()
			{
			public void changedUpdate(DocumentEvent e){sendAction();}
			public void insertUpdate(DocumentEvent e){sendAction();}
			public void removeUpdate(DocumentEvent e){sendAction();}
			private void sendAction()
				{
				for(JNumericListener l:listeners)
					l.numericChanged(thethis);
				}
			});	
		}
	
	
	public void addNumericListener(JNumericListener l)
		{
		listeners.add(l);
		}
	public void removeNumericListener(JNumericListener l)
		{
		listeners.remove(l);
		}
	
	

	
	
	public int getInt(int def)
		{
		try
			{
			return Integer.parseInt(getText());
			}
		catch (NumberFormatException e)
			{
			return def;
			}
		}
	
	public double getDouble(double def)
		{
		try
			{
			return Double.parseDouble(getText());
			}
		catch (NumberFormatException e)
			{
			return def;
			}
		}
	
	
	
	
	}
