/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.frameTime;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import endrov.basicWindow.FrameControl;
import endrov.util.EvDecimal;


/**
 * Editor for EvDecimal spinners 
 * @author Johan Henriksson
 */	
public class EvFrameEditor extends JTextField
	{
	static final long serialVersionUID=0;
	private FrameTime currentFrameTime=null;
	private final SpinnerModel sm;
	
	
	ActionListener alist=new ActionListener()
		{public void actionPerformed(ActionEvent e)
			{
			removeActionListener(this);
			getModel().removeChangeListener(cl);
			String newText=FrameControl.formatTime(FrameControl.parseTime(getText()));
			setText(newText);
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
			}
		};
	
	public EvFrameEditor(final JSpinner sp)
		{
		sm=sp.getModel();
		addActionListener(alist);
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
			d=currentFrameTime.mapFrame2Time(d);		
		if(d!=null)
			{
			newSetText(d);
			}
		}

	private void newSetText(EvDecimal d)
		{
		String s=FrameControl.formatTime(d);
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
		EvDecimal d=FrameControl.parseTime(getText());
		if(currentFrameTime!=null)
			d=currentFrameTime.mapTime2Frame(d);
		return d;
		}

	/**
	 * Set frame-time map
	 */
	public void setFrameTime(FrameTime ft)
		{
		EvDecimal d=getFrame();
		currentFrameTime=ft;
		setFrame(d);
		sm.setValue(getFrame());
		System.out.println("setframetime "+ft);
		}
	
	}
