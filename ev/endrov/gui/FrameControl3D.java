/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import org.jdom.Element;

import java.util.*;

import endrov.gui.component.EvFrameControl;
import endrov.gui.component.EvFrameEditor;
import endrov.gui.component.JSpinnerFrameModel;
import endrov.gui.icon.BasicIcon;
import endrov.util.math.EvDecimal;
import endrov.windowViewer3D.Viewer3DWindow;
import endrov.windowViewer3D.Viewer3DHook;




/**
 * Common control to change which frame is to be displayed
 * 
 * @author Johan Henriksson
 */
public class FrameControl3D extends JPanel implements ActionListener, ChangeListener, EvFrameControl.Synch
	{
	static final long serialVersionUID=0;

	/** Timer used for playback. Set to null when there is no playback */
	private javax.swing.Timer timer=null;
	
	/** Set to True if playing forward, False if playing backwards */
	private boolean playingForward=true;
	
	private Viewer3DWindow w; 
	
	
	private JButton buttonStepBack=new JButton(BasicIcon.iconFramePrev);
	private JButton buttonStepForward=new JButton(BasicIcon.iconFrameNext);
	private JButton buttonPlayBack=new JButton(BasicIcon.iconPlayBackward);
	private JButton buttonPlayForward=new JButton(BasicIcon.iconPlayForward);
	private JButton buttonBeginning=new JButton(BasicIcon.iconFrameFirst);
	private JButton buttonEnd=new JButton(BasicIcon.iconFrameLast);
			
	private JFrameTimeMenuButton buttonFrameTime=new JFrameTimeMenuButton();

	//private SpinnerModel frameModel;
	private SpinnerModel groupModel=new SpinnerNumberModel(0,0,9,1);
	private JSpinner spinnerFrame;
	private JSpinner spinnerGroup=new JSpinner(groupModel);

	/**
	 * Playback speed
	 */
	private static class Speed
		{
		EvDecimal speed;
		public Speed(String speed)
			{
			this.speed=new EvDecimal(speed);
			}
		public String toString()
			{
			return ""+speed+"x";
			}
		}
		
	private JComboBox speedCombo;
	
	
	/** Frame spinner behaviour */
	private SpinnerModel frameModel=new JSpinnerFrameModel()
		{
		public EvDecimal lastFrame(EvDecimal currentFrame)
			{
			return currentFrame.subtract(currentSpeed());
			}
		public EvDecimal nextFrame(EvDecimal currentFrame)
			{
			return currentFrame.add(currentSpeed());
			}
		};
		
		
		
	/*new SpinnerModel()
		{
		private Vector<ChangeListener> listeners=new Vector<ChangeListener>();
		public void addChangeListener(ChangeListener e){listeners.add(e);}
		public void removeChangeListener(ChangeListener e){listeners.remove(e);}
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
		};*/
	
		
	private GridBagConstraints editorConstraint(int x)
		{
		GridBagConstraints c=new GridBagConstraints();
		c.gridx=x;
		c.fill=GridBagConstraints.HORIZONTAL;
//			c.fill=0;
		//c.weightx=0;
			c.weightx=1;
		return c;
		}
	
	private GridBagConstraints playButtonConstraint(int x)
		{
		GridBagConstraints c=new GridBagConstraints();
		c.gridx=x;
		c.fill=GridBagConstraints.HORIZONTAL;
//		c.fill=0;
		c.weightx=0;
//		c.weightx=1;
		return c;
		}
	private GridBagConstraints smallButtonConstraint(int x)
		{
		GridBagConstraints c=new GridBagConstraints();
		c.gridx=x;
		c.fill=GridBagConstraints.HORIZONTAL;
		c.weightx=0;
		return c;
		}
	
	/**
	 * Create a frame control linked to model window
	 */
	public FrameControl3D(Viewer3DWindow w)
		{	
		this.w=w;

		buttonStepBack.setToolTipText("Step back");
		buttonStepForward.setToolTipText("Step forward");
		buttonPlayBack.setToolTipText("Play backwards");
		buttonPlayForward.setToolTipText("Play forward");
		buttonBeginning.setToolTipText("Go to first interesting time point");
		buttonEnd.setToolTipText("Go to last interesting time point");

		buttonStepBack.setFocusable(false);
		buttonStepForward.setFocusable(false);
		buttonPlayBack.setFocusable(false);
		buttonPlayForward.setFocusable(false);
		buttonBeginning.setFocusable(false);
		buttonEnd.setFocusable(false);

		Vector<Speed> speeds=new Vector<Speed>();
		speeds.add(new Speed("0.01"));
		speeds.add(new Speed("0.1"));
		speeds.add(new Speed("1"));
		speeds.add(new Speed("10"));
		speeds.add(new Speed("100"));
		speeds.add(new Speed("1000"));
		speeds.add(new Speed("10000"));
		speedCombo=new JComboBox(speeds);
		speedCombo.setSelectedIndex(4);

		setLayout(new GridBagLayout());
		
		//Find a unique group ID		
		spinnerGroup.setValue(EvFrameControl.getUniqueGroup());

		add(buttonBeginning,playButtonConstraint(0));
		add(buttonEnd,playButtonConstraint(1));
		add(buttonStepBack,playButtonConstraint(2));
		add(buttonStepForward,playButtonConstraint(3));
		add(speedCombo,smallButtonConstraint(4));
		add(buttonPlayBack,playButtonConstraint(5));
		add(buttonPlayForward,playButtonConstraint(6));
		

		
		//Build other controls and merge
		//setLayout(new GridLayout());
		//frameModel=new SpinnerNumberModel(new EvDecimal(0),new EvDecimal(0),new EvDecimal((double)1000000.0,(double)0.1);
		spinnerFrame=new JSpinner(frameModel);
//		spinnerFrame.setEditor(new EvDecimalEditor(spinnerFrame));
		EvFrameEditor frameEditor=new EvFrameEditor(spinnerFrame);
		spinnerFrame.setEditor(frameEditor);
		buttonFrameTime.addEditor(frameEditor);
		
		add(EvSwingUtil.withLabel("Frame:",spinnerFrame),editorConstraint(7));
		add(buttonFrameTime,playButtonConstraint(8));
		add(EvSwingUtil.withLabel("Group",spinnerGroup),playButtonConstraint(9));
		
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
		spinnerGroup.addChangeListener(this);
		}

	private void removeChangeListener()
		{
		spinnerFrame.removeChangeListener(this);
		spinnerGroup.removeChangeListener(this);
		}
	
	/**
	 * Number of frames per second movie will be attempted to be played at.
	 * It need to be a nice number to divide with! no rounding problems
	 */
	private static int FPS=50;
	
	/**
	 * Handle buttons and timer
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==buttonBeginning)
			{
			//TODO need to be recursive
			EvDecimal first=null;
			for(Viewer3DHook h:w.modelWindowHooks)
				{
				EvDecimal f=h.getFirstFrame();
				if(f!=null && (first==null || f.less(first)))
					first=f;
				}
			System.out.println(first);
			if(first==null)
				setFrame(new EvDecimal(0));
			else
				setFrame(first);
			}
		else if(e.getSource()==buttonEnd)
			{
			//TODO need to be recursive
			EvDecimal last=null;
			for(Viewer3DHook h:w.modelWindowHooks)
				{
				EvDecimal f=h.getLastFrame();
				if(f!=null && (last==null || f.greater(last)))
					last=f;
				}
			System.out.println(last);
			if(last==null)
				setFrame(new EvDecimal(0));
			else
				setFrame(last);
			}
		else if(e.getSource()==buttonStepForward)
			stepForward(currentSpeed());
		else if(e.getSource()==buttonStepBack)
			stepBack(currentSpeed());
		else if(e.getSource()==buttonPlayForward)
			stopStart(true);
		else if(e.getSource()==buttonPlayBack)
			stopStart(false);
		else if(e.getSource()==timer)
			{
			if(playingForward)
				setFrame(getFrame().add(currentSpeed().divide(FPS)));
			else
				setFrame(getFrame().subtract(currentSpeed().divide(FPS)));
			}
		
		}
	
	
	
	private EvDecimal currentSpeed()
		{
		return ((Speed)speedCombo.getSelectedItem()).speed;
		}
	
	
	/**
	 * Start and stop playback
	 * @param forward Set to true if to play forward
	 */
	public void stopStart(boolean forward)
		{
		int dt=1000/FPS;
		playingForward=forward;	
		
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
	
	/**
	 * Move to next existing frame
	 */
	public void stepForward(EvDecimal s)
		{
		setFrame(getFrame().add(s));
		}
	/**
	 * Move to last existing frame
	 */
	public void stepBack(EvDecimal s)
		{
		setFrame(getFrame().subtract(s));
		}
	
	
	
	/**
	 * Handle spinners
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
		{
		removeChangeListener();
		setFrame(getFrame());
		addChangeListener();
		}

	
	/**
	 * Get settings from another synchronized control
	 */
	public void replicate(EvDecimal frame, EvDecimal z)
		{
		removeChangeListener();
		spinnerFrame.setValue(frame.doubleValue());
		w.stateChanged(new ChangeEvent(this));
		addChangeListener();
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
		spinnerFrame.setValue(frame);
		w.stateChanged(new ChangeEvent(this));
		EvFrameControl.replicateSettings(this);
		}
	
	/** Current slice/Z */
	public EvDecimal getModelZ()
		{
		return null;
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
