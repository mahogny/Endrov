/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;

import org.jdom.Element;

import endrov.gui.component.EvDecimalEditor;
import endrov.gui.component.EvFrameControl;
import endrov.gui.component.EvFrameEditor;
import endrov.gui.component.JSpinnerFrameModel;
import endrov.gui.icon.BasicIcon;
import endrov.typeImageset.EvChannel;
import endrov.typeImageset.EvStack;
import endrov.util.ProgressHandle;
import endrov.util.math.EvDecimal;
import endrov.util.math.EvMathUtil;



/**
 * Common control to change which frame is to be displayed
 *
 * TODO integrate with FrameControlGeneric
 * 
 * @author Johan Henriksson
 */
public class FrameControl2D extends JPanel implements ActionListener, ChangeListener, EvFrameControl.Synch
	{
	static final long serialVersionUID=0;

	/** Timer used for playback. Set to null when there is no playback */
	private javax.swing.Timer timer=null;
	
	/** Set to True if playing forward, False if playing backwards */
	private boolean playingForward=true;
	
	/** Component to tell that frame has changed */
	private final ChangeListener listener;
	
	/** Which channel this control refers to */
	private EvChannel channel;
	
	
	private JButton buttonStepBack=new JButton(BasicIcon.iconFramePrev);
	private JButton buttonStepForward=new JButton(BasicIcon.iconFrameNext);
	private JButton buttonPlayBack=new JButton(BasicIcon.iconPlayBackward);
	private JButton buttonPlayForward=new JButton(BasicIcon.iconPlayForward);
	private JButton buttonBeginning=new JButton(BasicIcon.iconFrameFirst);
	private JButton buttonEnd=new JButton(BasicIcon.iconFrameLast);

	private JFrameTimeMenuButton buttonFrameTime=new JFrameTimeMenuButton();
	
	private SpinnerModel groupModel=new SpinnerNumberModel(0,0,9,1);
	private JSpinner spinnerZ;
	private JSpinner spinnerFrame;
	private JSpinner spinnerGroup=new JSpinner(groupModel);
	private JCheckBox checkGroupSlice=new JCheckBox("");

	/** Frame spinner behaviour */
	private SpinnerModel frameModel=new JSpinnerFrameModel()
		{
		public EvDecimal lastFrame(EvDecimal currentFrame)
			{
			return FrameControl2D.this.lastFrame();
			}
		public EvDecimal nextFrame(EvDecimal currentFrame)
			{
			return FrameControl2D.this.nextFrame();
			}
		};
		
	/** Z spinner behaviour */
	private SpinnerModel zModel=new SpinnerModel()
		{
		private Vector<ChangeListener> listeners=new Vector<ChangeListener>();
		public void addChangeListener(ChangeListener e){listeners.add(e);}
		public void removeChangeListener(ChangeListener e){listeners.remove(e);}
		public EvDecimal z=new EvDecimal(0);
		public Object getNextValue()
			{
			EvDecimal i=nextUp();
			System.out.println("nextup "+i);
			if(i==null)	return z;	else return i;
			}
		public Object getPreviousValue()
			{
			EvDecimal i=nextDown();
			System.out.println("nextdown "+i);
			if(i==null)	return z;	else return i;
			}
		public Object getValue(){return z;}
		public void setValue(Object e)
			{
			if(e instanceof Double)
				z=new EvDecimal((Double)e);
			else if(e instanceof Integer)
				z=new EvDecimal((Integer)e);
			else if(e instanceof EvDecimal)
				z=(EvDecimal)e;
			for(ChangeListener li:listeners)
				li.stateChanged(new ChangeEvent(this));
			}
		};		

	
		
		
		
	public void setChannel(EvChannel channel)
		{
		this.channel=channel;
		}

	/**
	 * @param l Object to receive updates on change
	 */
	public FrameControl2D(ChangeListener l, boolean includePlayButtons, boolean includeZ)
		{	
		listener=l;

		buttonStepBack.setToolTipText("Previous frame");
		buttonStepForward.setToolTipText("Next frame");
		buttonPlayBack.setToolTipText("Play backwards");
		buttonPlayForward.setToolTipText("Play forward");
		buttonBeginning.setToolTipText("Go to first frame");
		buttonEnd.setToolTipText("Go to last frame");
		
		buttonStepBack.setFocusable(false);
		buttonStepForward.setFocusable(false);
		buttonPlayBack.setFocusable(false);
		buttonPlayForward.setFocusable(false);
		buttonBeginning.setFocusable(false);
		buttonEnd.setFocusable(false);

		
		//Find a unique group ID
		spinnerGroup.setValue(EvFrameControl.getUniqueGroup());
		
		//Build list of play buttons
		JComponent playPanel;
		if(includePlayButtons)
			playPanel=EvSwingUtil.layoutCompactHorizontal(
					buttonBeginning,buttonEnd,buttonStepBack,buttonStepForward,
					buttonPlayBack,buttonPlayForward);
		else
			playPanel=EvSwingUtil.layoutCompactHorizontal(
					buttonBeginning,buttonEnd,buttonStepBack,buttonStepForward);
			
		spinnerFrame=new JSpinner(frameModel);
		
		EvFrameEditor frameEditor=new EvFrameEditor(spinnerFrame);
		spinnerFrame.setEditor(frameEditor);
		buttonFrameTime.addEditor(frameEditor);
		
		spinnerZ=new JSpinner(zModel);
		spinnerZ.setEditor(new EvDecimalEditor(spinnerZ));

		
		//Build other controls and merge
		JPanel zPanel=new JPanel(new BorderLayout());
		zPanel.add(new JLabel("Z:"), BorderLayout.WEST);
		zPanel.add(spinnerZ, BorderLayout.CENTER);
		
		JPanel fPanel=new JPanel(new BorderLayout());
				
		fPanel.add(new JLabel("Frame:"), BorderLayout.WEST);
		fPanel.add(spinnerFrame, BorderLayout.CENTER);
		fPanel.add(buttonFrameTime, BorderLayout.EAST);
		
		JComponent gPanel=EvSwingUtil.layoutCompactHorizontal(checkGroupSlice, EvSwingUtil.withLabel("Group:",spinnerGroup));
		checkGroupSlice.setToolTipText("Synchronize current Z");
		checkGroupSlice.setSelected(true);

		JPanel therest=new JPanel(new GridLayout(1,2,0,0));
		therest.add(fPanel);
		if(includeZ)
			therest.add(zPanel);

		setLayout(new BorderLayout());
		add(playPanel,BorderLayout.WEST);
		add(therest,BorderLayout.CENTER);
		add(gPanel,BorderLayout.EAST);

		
		//Make this class listen to everything
		buttonBeginning.addActionListener(this);
		buttonPlayBack.addActionListener(this);
		buttonStepBack.addActionListener(this);
		buttonStepForward.addActionListener(this);
		buttonPlayForward.addActionListener(this);
		buttonEnd.addActionListener(this);
		
		addChangeListener();
		
		//Add this control to the global list of controls
		EvFrameControl.add(this);
		}
	
	private void addChangeListener()
		{
		spinnerFrame.addChangeListener(this);
		spinnerZ.addChangeListener(this);
		spinnerGroup.addChangeListener(this);
		}

	private void removeChangeListener()
		{
		spinnerFrame.removeChangeListener(this);
		spinnerZ.removeChangeListener(this);
		spinnerGroup.removeChangeListener(this);
		}
	
	

	
	/**
	 * Handle buttons and timer
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==buttonBeginning)
			goFirstFrame();
		else if(e.getSource()==buttonEnd)
			goLastFrame();
		else if(e.getSource()==buttonStepForward)
			stepForward();
		else if(e.getSource()==buttonStepBack)
			stepBack();
		else if(e.getSource()==buttonPlayForward)
			stopStart(true);
		else if(e.getSource()==buttonPlayBack)
			stopStart(false);
		else if(e.getSource()==timer)
			{
			if(playingForward)
				stepForward();
			else
				stepBack();
			}
		
		}
	
	/**
	 * Start and stop playback
	 * @param forward Set to true if to play forward
	 */
	public void stopStart(boolean forward)
		{
		int dt=1000/4;
			
		if(timer==null)
			{
			playingForward=forward;
			timer=new javax.swing.Timer(dt, this);
			timer.start();
			buttonPlayForward.setIcon(BasicIcon.iconPlayStop);
			buttonPlayBack.setIcon(BasicIcon.iconPlayStop);
			}
		else
			{
			timer=null;
			buttonPlayForward.setIcon(BasicIcon.iconPlayForward);
			buttonPlayBack.setIcon(BasicIcon.iconPlayBackward);
			}
		}
	
	public void goFirstFrame()
		{
		if(channel!=null)
			setFrame(channel.getFirstFrame());
		}

	public void goLastFrame()
		{
		if(channel!=null)
			setFrame(channel.getLastFrame());
		}

	/**
	 * Move to next existing frame
	 */
	public void stepForward()
		{
		EvDecimal i=nextFrame();
		if(i!=null) setFrame(i);
		}
	private EvDecimal nextFrame()
		{
		if(channel!=null)
			return channel.closestFrameAfter(getFrame());
		else
			return null;
		}
	
	
	
	/**
	 * Move to last existing frame
	 */
	public void stepBack()
		{
		EvDecimal i=lastFrame();
		if(i!=null) setFrame(i);
		}
	private EvDecimal lastFrame()
		{
		if(channel!=null)
			return channel.closestFrameBefore(getFrame());
		else
			return null;
		}
	
	/**
	 * Move to slice above
	 */
	public void stepUp()
		{
		EvDecimal i=nextUp();
		if(i!=null) setZ(i);
		}
	private EvDecimal nextUp()
		{
		EvChannel ch=channel;
		if(ch!=null)
			{
			EvStack stack=ch.getStack(new ProgressHandle(), getFrame());
			if(stack!=null)
				{
				double nextZ=Math.round(stack.transformWorldImageZ(getZ().doubleValue())-1);
				if(nextZ<0)
					nextZ=0;
				return new EvDecimal(stack.transformImageWorldZ(nextZ));
				}
			else
				return null;
			}
		else
			return null;
		}
	/**
	 * Move to slice below
	 */
	public void stepDown()
		{
		EvDecimal i=nextDown();
		if(i!=null) setZ(i);
		}
	private EvDecimal nextDown()
		{
		EvChannel ch=channel;
		if(ch!=null)
			{
			EvStack stack=ch.getStack(new ProgressHandle(), getFrame());
			if(stack!=null)
				{
				double nextZ=Math.round(stack.transformWorldImageZ(getZ().doubleValue())+1);
				if(nextZ>stack.getDepth()-1)
					nextZ=stack.getDepth()-1;
				return new EvDecimal(stack.transformImageWorldZ(nextZ));
//				return stack.closestZAbove(getZ());
				}
			else
				return null;
			}
		else
			return null;
		}
	
	/**
	 * Handle spinners
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
		{
		setAll(getFrame(), getZ());
		}


	
	/**
	 * Update all settings at the same time. This is an optimization as it avoids multiple repaints
	 */
	public void setAll(EvDecimal frame, EvDecimal z)
		{
		//Find the closest z for this controller. Maybe one should just keep the value as-is?
		EvChannel ch=channel;
		if(ch!=null)
			{
			frame=ch.closestFrame(frame);
			EvStack stack=ch.getStack(new ProgressHandle(), frame);
			if(stack!=null)
				{
				double curz=Math.round(stack.transformWorldImageZ(z.doubleValue()));
				z=new EvDecimal(stack.transformImageWorldZ(curz));
//				z=stack.resZ.multiply(stack.closestZint(z.doubleValue())).add(stack.dispZ);
				}
			}
		removeChangeListener();
		spinnerFrame.setValue(frame);
		spinnerZ.setValue(z);
		addChangeListener();
		listener.stateChanged(new ChangeEvent(this));
		EvFrameControl.replicateSettings(this);
		}
	
	
	/** Convert world to screen Z coordinate. REPLICATED CODE, BAD! */
	public EvDecimal w2sz(EvDecimal z) {return z/*.multiply(getImageset().meta.resZ)*/;}
	/** Convert world to screen Z coordinate. REPLICATED CODE, BAD! */
	public EvDecimal s2wz(EvDecimal sz) {return sz/*.divide(getImageset().meta.resZ)*/;} 
	
	/**
	 * Get settings from another synchronized control
	 */
	public void replicate(EvDecimal frame, EvDecimal z)
		{
		if(z==null)
			z=getModelZ();
		EvDecimal slicenum=w2sz(z);
		
		if(channel!=null)
			{
			EvChannel ch=channel;
			frame=ch.closestFrame(frame);
			EvStack stack=ch.getStack(new ProgressHandle(), frame);
			
			slicenum=new EvDecimal(stack.transformImageWorldZ(
			EvMathUtil.clamp(Math.round(stack.transformWorldImageZ(z.doubleValue())), 0, stack.getDepth()-1)
					));
			}
		removeChangeListener();
		spinnerFrame.setValue(frame);
		if(checkGroupSlice.isSelected())
			spinnerZ.setValue(slicenum);
		addChangeListener();
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
		return (EvDecimal)spinnerFrame.getValue();
		}

	/** Set current frame */
	public void setFrame(EvDecimal frame)
		{
		if(frame!=null)
			setAll(frame,getZ());
		}
	
	/** Current slice/Z */
	public EvDecimal getZ()
		{
		return (EvDecimal)spinnerZ.getValue();
		}
	public EvDecimal getModelZ()
		{
		return s2wz((EvDecimal)spinnerZ.getValue());
		}
	
	
	
	/** Set current slice/Z */
	public void setZ(EvDecimal z)
		{
		setAll(getFrame(), z);
		}
	
	/** Set current slice/Z */
	public void setModelZ(EvDecimal z)
		{
		setAll(getFrame(), z);
		}

	public void storeSettings(Element root)
		{
		root.setAttribute("framecontrol_group",""+getGroup());
		}
	public void getSettings(Element root)
		{
		setGroup(Integer.parseInt(root.getAttributeValue("framecontrol_group")));
		}

	}
