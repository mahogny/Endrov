package evplugin.modelWindow;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

import evplugin.basicWindow.FrameControl;



//todo: spinners are based on integer. low precision?

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
	
	private JButton buttonStepBack=new JButton("<");
	private JButton buttonStepForward=new JButton(">");
	private JButton buttonBeginning=new JButton("|<");
	private JButton buttonEnd=new JButton(">|");
	private JButton buttonPlayBack=new JButton("<P");
	private JButton buttonPlayForward=new JButton("P>");

	private SpinnerModel frameModel;
	private SpinnerModel groupModel=new SpinnerNumberModel(0,0,9,1);
	private JSpinner spinnerFrame;
	private JSpinner spinnerGroup=new JSpinner(groupModel);

	/**
	 * Playback speed
	 */
	private static class Speed
		{
		double speed;
		public Speed(double speed)
			{
			this.speed=speed;
			}
		public String toString()
			{
			return ""+speed+" FPS";
			}
		}
		
	private JComboBox speedCombo;
	
	
	
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
	public FrameControlModel(ChangeListener l)
		{	
		listener=l;

		Vector<Speed> speeds=new Vector<Speed>();
		speeds.add(new Speed(0.1));
		speeds.add(new Speed(0.5));
		speeds.add(new Speed(1));
		speeds.add(new Speed(5));
		speeds.add(new Speed(10));
		speeds.add(new Speed(50));
		speeds.add(new Speed(100));
		speedCombo=new JComboBox(speeds);

		setLayout(new GridBagLayout());
		
		//Find a unique group ID		
		spinnerGroup.setValue(FrameControl.getUniqueGroup());
		
		//Build list of play buttons
		/*
		JPanel playPanel=new JPanel(new GridLayout(1,7,0,0));
		playPanel.add(buttonBeginning);
		playPanel.add(buttonEnd);
		playPanel.add(buttonStepBack);
		playPanel.add(buttonStepForward);
		playPanel.add(speedCombo);
		playPanel.add(buttonPlayBack);
		playPanel.add(buttonPlayForward);
		add(playPanel);
*/		

		add(buttonBeginning,playButtonConstraint(0));
		add(buttonEnd,playButtonConstraint(1));
		add(buttonStepBack,playButtonConstraint(2));
		add(buttonStepForward,playButtonConstraint(3));
		add(speedCombo,playButtonConstraint(4));
		add(buttonPlayBack,playButtonConstraint(5));
		add(buttonPlayForward,playButtonConstraint(6));

		
		//Build other controls and merge
		//setLayout(new GridLayout());
		frameModel=new SpinnerNumberModel((double)0.0,(double)0.0,(double)10000.0,(double)0.1);
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
	

	/**
	 * Called when frame control is destroyed
	 * TODO: call manually on window close. GC probably has problem with it
	 */
	public void finalize()
		{
		//Remove this control from global list of controls
		FrameControl.remove(this);
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
	 * Handle buttons and timer
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==buttonBeginning)
			setFrame(0);
		else if(e.getSource()==buttonEnd)
			setFrame(3000);
		else if(e.getSource()==buttonStepForward)
			stepForward(1);
		else if(e.getSource()==buttonStepBack)
			stepBack(1);
		else if(e.getSource()==buttonPlayForward)
			stopStart(true);
		else if(e.getSource()==buttonPlayBack)
			stopStart(false);
		else if(e.getSource()==timer)
			{
			if(playingForward)
				setFrame(getFrame()+currentSpeed());
			else
				setFrame(getFrame()-currentSpeed());
			}
		
		}
	
	private double currentSpeed()
		{
		return ((Speed)speedCombo.getSelectedItem()).speed;
		}
	
	/**
	 * Start and stop playback
	 * @param forward Set to true if to play forward
	 */
	public void stopStart(boolean forward)
		{
		int dt=100/4;
			
		if(timer==null)
			{
			playingForward=forward;
			timer=new javax.swing.Timer(dt, this);
			timer.start();
			buttonPlayForward.setText("[]");
			buttonPlayBack.setText("[]");
			}
		else
			{
			timer=null;
			buttonPlayForward.setText("P>");
			buttonPlayBack.setText("<P");
			}
		}
	
	/**
	 * Move to next existing frame
	 */
	public void stepForward(double s)
		{
		setFrame(getFrame()+s);
		}
	/**
	 * Move to last existing frame
	 */
	public void stepBack(double s)
		{
		setFrame(getFrame()-s);
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
	public void replicate(double frame, Integer z)
		{
		removeChangeListener();
		spinnerFrame.setValue((double)frame);
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
	public double getFrame()
		{
		return (Double)spinnerFrame.getValue();
		}

	/** Set current frame */
	public void setFrame(double frame)
		{
		spinnerFrame.setValue((double)frame);
		listener.stateChanged(new ChangeEvent(this));
		FrameControl.replicateSettings(this);
		}
	
	/** Current slice/Z */
	public Integer getZ()
		{
		return null;
		}
	
	
	}
