package endrov.modelWindow;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.util.*;

import endrov.basicWindow.FrameControl;
import endrov.basicWindow.icon.BasicIcon;
import endrov.util.EvDecimal;




/**
 * Common control to change which frame is to be displayed
 * 
 * @author Johan Henriksson
 */
public class FrameControlModel extends JPanel implements ActionListener, ChangeListener, FrameControl.Synch
	{
	static final long serialVersionUID=0;

	/** Timer used for playback. Set to null when there is no playback */
	private javax.swing.Timer timer=null;
	
	/** Set to True if playing forward, False if playing backwards */
	private boolean playingForward=true;
	
	/** Component to tell that frame has changed */
	private ChangeListener listener;
	
	private ModelWindow w; //TODO hm listener vs w
	
	private JButton buttonStepBack=new JButton(BasicIcon.iconFramePrev);
	private JButton buttonStepForward=new JButton(BasicIcon.iconFrameNext);
	private JButton buttonPlayBack=new JButton(BasicIcon.iconPlayBackward);
	private JButton buttonPlayForward=new JButton(BasicIcon.iconPlayForward);
	private JButton buttonBeginning=new JButton(BasicIcon.iconFrameFirst);
	private JButton buttonEnd=new JButton(BasicIcon.iconFrameLast);

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
	private SpinnerModel frameModel=new SpinnerModel()
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
		};
	
	
	private GridBagConstraints playButtonConstraint(int x)
		{
		GridBagConstraints c=new GridBagConstraints();
		c.gridx=x;
		c.fill=GridBagConstraints.HORIZONTAL;
		c.weightx=1;
		return c;
		}
	
	/**
	 * @param l Object to receive updates on change
	 */
	public FrameControlModel(ModelWindow w, ChangeListener l)
		{	
		listener=l;
		this.w=w;

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
		spinnerGroup.setValue(FrameControl.getUniqueGroup());

		add(buttonBeginning,playButtonConstraint(0));
		add(buttonEnd,playButtonConstraint(1));
		add(buttonStepBack,playButtonConstraint(2));
		add(buttonStepForward,playButtonConstraint(3));
		add(speedCombo,playButtonConstraint(4));
		add(buttonPlayBack,playButtonConstraint(5));
		add(buttonPlayForward,playButtonConstraint(6));

		
		//Build other controls and merge
		//setLayout(new GridLayout());
		//frameModel=new SpinnerNumberModel(new EvDecimal(0),new EvDecimal(0),new EvDecimal((double)1000000.0,(double)0.1);
		spinnerFrame=new JSpinner(frameModel);
		add(new JLabel("Frame:"),playButtonConstraint(7));
		add(spinnerFrame,playButtonConstraint(8));
		add(new JLabel("Group:"),playButtonConstraint(9));
		add(spinnerGroup,playButtonConstraint(10));
		
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
		spinnerGroup.addChangeListener(this);
		}

	private void removeChangeListener()
		{
		spinnerFrame.removeChangeListener(this);
		spinnerGroup.removeChangeListener(this);
		}
	
	private static int FPS=30;
	
	/**
	 * Handle buttons and timer
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==buttonBeginning)
			{
			EvDecimal first=null;
			for(ModelWindowHook h:w.modelWindowHooks)
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
			EvDecimal last=null;
			for(ModelWindowHook h:w.modelWindowHooks)
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
				setFrame(getFrame().add(currentSpeed().divide(FPS)));
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
		listener.stateChanged(new ChangeEvent(this));
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
		spinnerFrame.setValue(frame.doubleValue());
		listener.stateChanged(new ChangeEvent(this));
		FrameControl.replicateSettings(this);
		}
	
	/** Current slice/Z */
	public EvDecimal getModelZ()
		{
		return null;
		}
	
	
	}
