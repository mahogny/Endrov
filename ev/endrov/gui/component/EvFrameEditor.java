/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.gui.component;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import endrov.typeTimeRemap.TimeRemap;
import endrov.util.math.EvDecimal;


/**
 * Editor for EvDecimal spinners 
 * @author Johan Henriksson
 */	
public class EvFrameEditor extends JTextField implements DocumentListener
	{
	static final long serialVersionUID=0;
	private TimeRemap currentFrameTime=null;
	private final SpinnerModel sm;
	private final Color normalColor;
	
	
	ActionListener alist=new ActionListener()
		{public void actionPerformed(ActionEvent e)
			{
			removeActionListener(this);
			getModel().removeChangeListener(cl);
			String newText=EvFrameControl.formatTime(EvFrameControl.parseTime(getText()));
			setText(newText);
			setHighLight(false);
			getModel().setValue(getFrame());
			addActionListener(this);
			getModel().addChangeListener(cl);
			}
		};
		
		
	ChangeListener cl=new ChangeListener()
		{
		public void stateChanged(ChangeEvent e)
			{
			//System.out.println("state changed");
			setFrame((EvDecimal)getModel().getValue());
			setHighLight(false);
			}
		};
	
		
	public EvFrameEditor(final JSpinner sp)
		{
		normalColor=getBackground();
		sm=sp.getModel();
		addActionListener(alist);
		getDocument().addDocumentListener(this);
		getModel().addChangeListener(cl);
		setFrame((EvDecimal)sp.getModel().getValue());
		}
	
	
	
	private SpinnerModel getModel()
		{
		return sm;
		}
	
	
	
	/**
	 * Set text as frame
	 */
	private void setFrame(EvDecimal d)
		{
		if(currentFrameTime!=null)
			d=currentFrameTime.mapOrigTime2MappedTime(d);		
		if(d!=null)
			{
			newSetText(d);
			}
		setBackground(normalColor);
		}

	private void newSetText(EvDecimal d)
		{
		String s=EvFrameControl.formatTime(d);
		int doti=s.indexOf('.');
		if(doti==-1)
			setText(s);
		else
			{
			int len=10;
			if(doti+len<s.length()) //Limit number of decimals when changing number
				s=s.substring(0,doti+len);
			setText(s);
			}
		}
	
	//TODO time increment in model window: next frame should be based on model time
	
	
	/**
	 * Get text as frame
	 */
	private EvDecimal getFrame()
		{
		EvDecimal d=EvFrameControl.parseTime(getText());
		if(currentFrameTime!=null)
			d=currentFrameTime.mapMappedTime2OrigTime(d);
		return d;
		}

	/**
	 * Set frame-time map
	 */
	public void setFrameTime(TimeRemap ft)
		{
		EvDecimal d=getFrame();
		currentFrameTime=ft;
		setFrame(d);
		sm.setValue(getFrame());
		System.out.println("setframetime "+ft);
		}
	
	
	private void setHighLight(boolean state)
		{
		Color c=state ? new Color(219,219,112) : normalColor;
		setBackground(c);
		setBackground(c);
		}
	
	public void changedUpdate(DocumentEvent e){setHighLight(true);}
	public void insertUpdate(DocumentEvent e){setHighLight(true);}
	public void removeUpdate(DocumentEvent e){setHighLight(true);}
	}
