package evplugin.imageWindow;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import evplugin.basicWindow.FrameControl;
import evplugin.imageset.Imageset;



//todo: spinners are based on integer. low precision?

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
	
	private JButton buttonStepBack=new JButton("<");
	private JButton buttonStepForward=new JButton(">");
	private JButton buttonPlayBack=new JButton("<P");
	private JButton buttonPlayForward=new JButton("P>");
	private JButton buttonBeginning=new JButton("|<");
	private JButton buttonEnd=new JButton(">|");

	private SpinnerModel zModel    =new SpinnerNumberModel(0,0,999,1);
	private SpinnerModel frameModel;
	private SpinnerModel groupModel=new SpinnerNumberModel(0,0,9,1);
	private JSpinner spinnerZ    =new JSpinner(zModel);
	private JSpinner spinnerFrame;
	private JSpinner spinnerGroup=new JSpinner(groupModel);

	private JCheckBox checkGroupSlice=new JCheckBox("");
	
	
	
	/** Which channel this control refers to */
	private String channel=null;
	private Imageset imageset;
	//New version: will never be null unless imageset is null
	
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
		
		//Find a unique group ID
		spinnerGroup.setValue(FrameControl.getUniqueGroup());
		
		//Build list of play buttons
		setLayout(new GridLayout(1,2));
		JPanel playPanel=new JPanel(new GridLayout(1,6,0,0));
		JPanel therest=new JPanel(new GridLayout(1,3,0,0));
		add(playPanel);
		add(therest);
		playPanel.add(buttonBeginning);
		playPanel.add(buttonEnd);
		playPanel.add(buttonStepBack);
		playPanel.add(buttonStepForward);
		playPanel.add(buttonPlayBack);
		playPanel.add(buttonPlayForward);
		
		//Build other controls and merge
		JPanel zPanel=new JPanel(new BorderLayout());
		zPanel.add(new JLabel("Z:"), BorderLayout.WEST);
		zPanel.add(spinnerZ, BorderLayout.CENTER);
		zPanel.add(checkGroupSlice, BorderLayout.EAST);
		
		JPanel fPanel=new JPanel(new BorderLayout());
		frameModel=new SpinnerNumberModel((double)0.0,(double)0.0,(double)10000.0,(double)1);
		spinnerFrame=new JSpinner(frameModel);
		fPanel.add(new JLabel("Frame:"), BorderLayout.WEST);
		fPanel.add(spinnerFrame, BorderLayout.CENTER);
		
		JPanel gPanel=new JPanel(new BorderLayout());
		gPanel.add(new JLabel("Group:"), BorderLayout.WEST);
		gPanel.add(spinnerGroup, BorderLayout.CENTER);
		checkGroupSlice.setSelected(true);

		therest.add(fPanel);
		therest.add(zPanel);
		therest.add(gPanel);

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
	 * Called when frame control is destroyed
	 * TODO: call manually on window close
	 */
	public void finalize()
		{
		//Remove this control from global list of controls
		FrameControl.remove(this);
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
			setFrame(getFrame()-1);
		else if(e.getSource()==buttonPlayForward)
			stopStart(true);
		else if(e.getSource()==buttonPlayBack)
			stopStart(false);
		else if(e.getSource()==timer)
			{
			if(true)
				{
				if(playingForward)
					stepForward();
				else
					stepBack();
				}
			else
				{
				if(playingForward)
					setFrame(getFrame()+0.1);
				else
					setFrame(getFrame()-0.1);
				}
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
	
	public void goFirstFrame()
		{
		if(channel!=null && getImageset().getChannel(channel)!=null)
			setFrame(getImageset().getChannel(channel).closestFrameAfter((int)-1000000));
		}

	public void goLastFrame()
		{
		if(channel!=null && getImageset().getChannel(channel)!=null)
			setFrame(getImageset().getChannel(channel).closestFrameAfter((int)1000000));
		}

	/**
	 * Move to next existing frame
	 */
	public void stepForward()
		{
		if(channel!=null && getImageset().getChannel(channel)!=null)
			setFrame(getImageset().getChannel(channel).closestFrameAfter((int)getFrame()));
		}
	/**
	 * Move to last existing frame
	 */
	public void stepBack()
		{
		if(channel!=null && getImageset().getChannel(channel)!=null)
			setFrame(getImageset().getChannel(channel).closestFrameBefore((int)getFrame()));
		}
	/**
	 * Move to slice above
	 */
	public void stepUp()
		{
		if(channel!=null && getImageset().getChannel(channel)!=null)
			setZ(getImageset().getChannel(channel).closestZBelow((int)getFrame(),getZ()));
		}
	/**
	 * Move to slice below
	 */
	public void stepDown()
		{
		if(channel!=null && getImageset().getChannel(channel)!=null)
			setZ(getImageset().getChannel(channel).closestZAbove((int)getFrame(),getZ()));
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
	public void setAll(double frame, int z)
		{
		if(channel!=null && getImageset().getChannel(channel)!=null)
			{
			frame=getImageset().getChannel(channel).closestFrame((int)frame);
			z=getImageset().getChannel(channel).closestZ((int)frame, z);
			}
		removeChangeListener();
		spinnerFrame.setValue((double)frame);
		spinnerZ.setValue(z);
		addChangeListener();
		listener.stateChanged(new ChangeEvent(this));
		FrameControl.replicateSettings(this);
		}
	
	/**
	 * Get settings from another synchronized control
	 */
	public void replicate(double frame, Integer z)
		{
		if(z==null)
			z=getZ();
		if(channel!=null && getImageset().getChannel(channel)!=null)
			{
			frame=getImageset().getChannel(channel).closestFrame((int)frame);
			z=getImageset().getChannel(channel).closestZ((int)frame, z);
			}
		removeChangeListener();
		spinnerFrame.setValue((double)frame);
		if(checkGroupSlice.isSelected())
			spinnerZ.setValue(z);
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
	public double getFrame()
		{
		return (Double)spinnerFrame.getValue();
		}

	/** Set current frame */
	public void setFrame(double frame)
		{
		setAll(frame,getZ());
		}
	
	/** Current slice/Z */
	public Integer getZ()
		{
		return (Integer)spinnerZ.getValue();
		}
	
	/** Set current slice/Z */
	public void setZ(int z)
		{
		setAll(getFrame(), z);
		}
	
	}
