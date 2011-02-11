/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imageWindow;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;

import endrov.basicWindow.EvDecimalEditor;
import endrov.basicWindow.FrameControl;
import endrov.basicWindow.icon.BasicIcon;
import endrov.frameTime.*;
import endrov.imageset.EvChannel;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.util.EvDecimal;
import endrov.util.EvMathUtil;
import endrov.util.EvSwingUtil;



/**
 * Common control to change which frame is to be displayed
 * 
 * @author Johan Henriksson
 */
public class FrameControlImage extends JPanel implements ActionListener, ChangeListener, FrameControl.Synch
	{
	static final long serialVersionUID=0;

	/** Timer used for playback. Set to null when there is no playback */
	private javax.swing.Timer timer=null;
	
	/** Set to True if playing forward, False if playing backwards */
	private boolean playingForward=true;
	
	/** Component to tell that frame has changed */
	private final ChangeListener listener;
	
	/** Which channel this control refers to */
	private String channel=null;
	private Imageset imageset;
	//New version: will never be null unless imageset is null
	
	
	private JButton buttonStepBack=new JButton(BasicIcon.iconFramePrev);
	private JButton buttonStepForward=new JButton(BasicIcon.iconFrameNext);
	private JButton buttonPlayBack=new JButton(BasicIcon.iconPlayBackward);
	private JButton buttonPlayForward=new JButton(BasicIcon.iconPlayForward);
	private JButton buttonBeginning=new JButton(BasicIcon.iconFrameFirst);
	private JButton buttonEnd=new JButton(BasicIcon.iconFrameLast);

	private FrameTimeDropDown buttonFrameTime=new FrameTimeDropDown();
	
	private SpinnerModel groupModel=new SpinnerNumberModel(0,0,9,1);
	private JSpinner spinnerZ;
	private JSpinner spinnerFrame;
	private JSpinner spinnerGroup=new JSpinner(groupModel);
	private JCheckBox checkGroupSlice=new JCheckBox("");

	/** Frame spinner behaviour */
	private SpinnerModel frameModel=new SpinnerFrameModel()
		{
		public EvDecimal lastFrame(EvDecimal currentFrame)
			{
			return FrameControlImage.this.lastFrame();
			}
		public EvDecimal nextFrame(EvDecimal currentFrame)
			{
			return FrameControlImage.this.nextFrame();
			}
		};
	
	/*
	
	new SpinnerModel()
		{
		private Vector<ChangeListener> listeners=new Vector<ChangeListener>();
		public void addChangeListener(ChangeListener e){listeners.add(e);}
		public void removeChangeListener(ChangeListener e){listeners.remove(e);}
		public EvDecimal frame=new EvDecimal(0);
		public Object getNextValue()
			{
			EvDecimal i=nextFrame();
			if(i==null)	return frame;	else return i;
			}
		public Object getPreviousValue()
			{
			EvDecimal i=lastFrame();
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
		};*/
		
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

	
		
		
		
	public void setChannel(Imageset imageset, String channel)
		{
		this.imageset=imageset;
		this.channel=channel;
		}
	
	private Imageset getImageset()
		{
		return imageset;
		}
	

	/**
	 * @param l Object to receive updates on change
	 */
	public FrameControlImage(ChangeListener l)
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
		spinnerGroup.setValue(FrameControl.getUniqueGroup());
		
		//Build list of play buttons
		/*
		JPanel playPanel=new JPanel(new GridLayout(1,6,0,0));
		playPanel.add(buttonBeginning);
		playPanel.add(buttonEnd);
		playPanel.add(buttonStepBack);
		playPanel.add(buttonStepForward);
		playPanel.add(buttonPlayBack);
		playPanel.add(buttonPlayForward);
		*/
		
		JComponent playPanel=EvSwingUtil.layoutCompactHorizontal(
				buttonBeginning,buttonEnd,buttonStepBack,buttonStepForward,
				buttonPlayBack,buttonPlayForward);
		

		/*
		setLayout(new GridLayout(1,2));
		add(playPanel);
		add(therest);
		*/
		
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
		//zPanel.add(checkGroupSlice, BorderLayout.EAST);
		
		JPanel fPanel=new JPanel(new BorderLayout());
				
		fPanel.add(new JLabel("Frame:"), BorderLayout.WEST);
		fPanel.add(spinnerFrame, BorderLayout.CENTER);
		fPanel.add(buttonFrameTime, BorderLayout.EAST);
		
		JComponent gPanel=EvSwingUtil.layoutCompactHorizontal(checkGroupSlice, EvSwingUtil.withLabel("Group:",spinnerGroup));
		checkGroupSlice.setToolTipText("Synchronize current Z");
		checkGroupSlice.setSelected(true);

		JPanel therest=new JPanel(new GridLayout(1,2,0,0));
		therest.add(fPanel);
		therest.add(zPanel);
		//therest.add(gPanel);

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
		FrameControl.add(this);
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
			//if(true)
				//{
				if(playingForward)
					stepForward();
				else
					stepBack();
			/*	}
			else
				{
				if(playingForward)
					setFrame(getFrame().add(new EvDecimal(0.1)));
				else
					setFrame(getFrame().subtract(new EvDecimal(0.1)));
				}*/
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
		if(channel!=null && getImageset().getChannel(channel)!=null)
			setFrame(getImageset().getChannel(channel).getFirstFrame());
		}

	public void goLastFrame()
		{
		if(channel!=null && getImageset().getChannel(channel)!=null)
			setFrame(getImageset().getChannel(channel).getLastFrame());
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
		if(channel!=null && getImageset().getChannel(channel)!=null)
			return getImageset().getChannel(channel).closestFrameAfter(getFrame());
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
		if(channel!=null && getImageset().getChannel(channel)!=null)
			return getImageset().getChannel(channel).closestFrameBefore(getFrame());
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
		EvChannel ch=getImageset().getChannel(channel);
		if(ch!=null)
			{
			EvStack stack=ch.getStack(getFrame());
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
		EvChannel ch=getImageset().getChannel(channel);
		if(ch!=null)
			{
			EvStack stack=ch.getStack(getFrame());
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
		EvChannel ch=getImageset().getChannel(channel);
		if(ch!=null)
			{
			frame=ch.closestFrame(frame);
			EvStack stack=ch.getStack(frame);
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
		FrameControl.replicateSettings(this);
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
		
		if(channel!=null && getImageset().getChannel(channel)!=null)
			{
			EvChannel ch=getImageset().getChannel(channel);
			frame=ch.closestFrame(frame);
			EvStack stack=ch.getStack(frame);
			
			slicenum=new EvDecimal(stack.transformImageWorldZ(
			EvMathUtil.clamp(Math.round(stack.transformWorldImageZ(z.doubleValue())), 0, stack.getDepth()-1)
					));
//			slicenum=stack.resZ.multiply(stack.closestZint(z.doubleValue()));
			//slicenum=ch.closestZ(frame, slicenum);
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
	
	}
