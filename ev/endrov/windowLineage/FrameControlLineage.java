/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowLineage;

import java.awt.BorderLayout;

import javax.swing.*;
import javax.swing.event.*;

import endrov.gui.component.EvFrameControl;
import endrov.util.EvDecimal;


/**
 * Extremely reduced control. Just listens to current frame.
 * @author Johan Henriksson
 */
public class FrameControlLineage extends JPanel implements EvFrameControl.Synch
	{
	static final long serialVersionUID=0;
	
	/** Component to tell that frame has changed */
	private final ChangeListener listener;

	private final SpinnerModel groupModel=new SpinnerNumberModel(0,0,9,1);
	private final JSpinner spinnerGroup=new JSpinner(groupModel);
	
	private EvDecimal frame;
	private EvDecimal z=null;
	

	/**
	 * @param l Object to receive updates on change
	 */
	public FrameControlLineage(ChangeListener l)
		{	
		listener=l;
		
		//Find a unique group ID		
		spinnerGroup.setValue(EvFrameControl.getUniqueGroup());
		
		//Build other controls and merge
		setLayout(new BorderLayout());
		add(new JLabel("Group:"),BorderLayout.CENTER);
		add(spinnerGroup, BorderLayout.EAST);
		//TODO listen for group, get frame if changed
		
		//Add this control to the global list of controls
		EvFrameControl.add(this);
		}
	

	
	/**
	 * Get settings from another synchronized control
	 */
	public void replicate(EvDecimal frame, EvDecimal z)
		{
		this.frame=frame;
		listener.stateChanged(new ChangeEvent(this));
		}
	
	
	
	/** Get current group */
	public int getGroup()
		{
		return (Integer)spinnerGroup.getValue();
		}
	
	/** Set group */
	public void setGroup(int g)
		{
		spinnerGroup.setValue(g);
		}
	
	/** Get current frame */
	public EvDecimal getFrame()
		{
		return frame;
		}

	/** Set current frame */
	public void setFrame(EvDecimal frame)
		{
		this.frame=frame;
		listener.stateChanged(new ChangeEvent(this));
		EvFrameControl.replicateSettings(this);
		}
	
	/** Current slice/Z */
	public EvDecimal getModelZ()
		{
		return z;
		}
	
	/** Set current z */
	public void setFrameZ(EvDecimal frame, EvDecimal z)
		{
		this.frame=frame;
		this.z=z;
		listener.stateChanged(new ChangeEvent(this));
		EvFrameControl.replicateSettings(this);
		this.z=null;
		}
	}
