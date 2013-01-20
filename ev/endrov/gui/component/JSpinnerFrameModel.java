/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.gui.component;

import java.util.Vector;

import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import endrov.util.math.EvDecimal;

/**
 * Model for frames with JSpinner
 * @author Johan Henriksson
 *
 */
public abstract class JSpinnerFrameModel implements SpinnerModel
	{
	private Vector<ChangeListener> listeners=new Vector<ChangeListener>();
	public void addChangeListener(ChangeListener e){listeners.add(e);}
	public void removeChangeListener(ChangeListener e){listeners.remove(e);}
	public EvDecimal frame=new EvDecimal(0);
	public Object getNextValue()
		{
		EvDecimal i=nextFrame(frame);
		if(i==null)	return frame;	else return i;
		}
	public Object getPreviousValue()
		{
		EvDecimal i=lastFrame(frame);
		if(i==null)	return frame;	else return i;
		}
	public Object getValue(){return frame;}
	public void setValue(Object e)
		{
		if(e instanceof Double)
			frame=new EvDecimal((Double)e);
		else if(e instanceof Integer)
			frame=new EvDecimal((Integer)e);
		else if(e instanceof EvDecimal)
			frame=(EvDecimal)e;
		for(ChangeListener li:listeners)
			li.stateChanged(new ChangeEvent(this));
		}
		
	public abstract EvDecimal lastFrame(EvDecimal currentFrame);
	public abstract EvDecimal nextFrame(EvDecimal currentFrame);
	
	}
