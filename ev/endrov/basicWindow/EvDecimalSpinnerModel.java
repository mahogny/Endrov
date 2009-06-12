package endrov.basicWindow;

import java.util.Vector;

import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import endrov.util.EvDecimal;

/**
 * Model of EvDecimal for JSpinner
 * @author Johan Henriksson
 *
 */
public class EvDecimalSpinnerModel implements SpinnerModel
	{
	private Vector<ChangeListener> listeners=new Vector<ChangeListener>();

	public void addChangeListener(ChangeListener e)
		{
		listeners.add(e);
		}
	
	public void removeChangeListener(ChangeListener e)
		{
		listeners.remove(e);
		}
	
	public EvDecimal frame=new EvDecimal(0);
	
	public Object getNextValue()
		{
		EvDecimal i=frame.add(1);
		if(i==null)	return frame;	else return i;	
		}

	public Object getPreviousValue()
		{
		EvDecimal i=frame.subtract(1);
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
		//frame=(EvDecimal)e;
		for(ChangeListener li:listeners)
			li.stateChanged(new ChangeEvent(this));
		}
	}
