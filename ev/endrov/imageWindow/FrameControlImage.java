package endrov.imageWindow;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;

import endrov.basicWindow.FrameControl;
import endrov.imageset.Imageset;



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
	
	
	private static ImageIcon iconFramePrev=new ImageIcon(FrameControlImage.class.getResource("buttonFramePrev.png"));
	private static ImageIcon iconFrameNext=new ImageIcon(FrameControlImage.class.getResource("buttonFrameNext.png"));
	private static ImageIcon iconFrameFirst=new ImageIcon(FrameControlImage.class.getResource("buttonFrameFirst.png"));
	private static ImageIcon iconFrameLast=new ImageIcon(FrameControlImage.class.getResource("buttonFrameLast.png"));
	private static ImageIcon iconPlayBackward=new ImageIcon(FrameControlImage.class.getResource("buttonPlayBackward.png"));
	private static ImageIcon iconPlayForward=new ImageIcon(FrameControlImage.class.getResource("buttonPlayForward.png"));
	private static ImageIcon iconPlayStop=new ImageIcon(FrameControlImage.class.getResource("buttonPlayStop.png"));
	
	private JButton buttonStepBack=new JButton(iconFramePrev);
	private JButton buttonStepForward=new JButton(iconFrameNext);
	private JButton buttonPlayBack=new JButton(iconPlayBackward);
	private JButton buttonPlayForward=new JButton(iconPlayForward);
	private JButton buttonBeginning=new JButton(iconFrameFirst);
	private JButton buttonEnd=new JButton(iconFrameLast);

	private SpinnerModel groupModel=new SpinnerNumberModel(0,0,9,1);
	private JSpinner spinnerZ;
	private JSpinner spinnerFrame;
	private JSpinner spinnerGroup=new JSpinner(groupModel);
	private JCheckBox checkGroupSlice=new JCheckBox("");

	/** Frame spinner behaviour */
	private SpinnerModel frameModel=new SpinnerModel()
		{
		private Vector<ChangeListener> listeners=new Vector<ChangeListener>();
		public void addChangeListener(ChangeListener e){listeners.add(e);}
		public void removeChangeListener(ChangeListener e){listeners.remove(e);}
		public int frame;
		public Object getNextValue()
			{
			Integer i=nextFrame();
			if(i==null)	return frame;	else return i;
			}
		public Object getPreviousValue()
			{
			Integer i=lastFrame();
			if(i==null)	return frame;	else return i;
			}
		public Object getValue(){return frame;}
		public void setValue(Object e)
			{
			frame=(Integer)e;
			for(ChangeListener li:listeners)
				li.stateChanged(new ChangeEvent(this));
			}
		};
		
	/** Z spinner behaviour */
	private SpinnerModel zModel=new SpinnerModel()
		{
		private Vector<ChangeListener> listeners=new Vector<ChangeListener>();
		public void addChangeListener(ChangeListener e){listeners.add(e);}
		public void removeChangeListener(ChangeListener e){listeners.remove(e);}
		public int z;
		public Object getNextValue()
			{
			Integer i=nextUp();
			if(i==null)	return z;	else return i;
			}
		public Object getPreviousValue()
			{
			Integer i=nextDown();
			if(i==null)	return z;	else return i;
			}
		public Object getValue(){return z;}
		public void setValue(Object e)
			{
			z=(Integer)e;
			for(ChangeListener li:listeners)
				li.stateChanged(new ChangeEvent(this));
			}
		};		

	/** Editor for integer spinners */	
	private static class IntegerEditor extends JTextField
		{
		static final long serialVersionUID=0;
		public IntegerEditor(final JSpinner sp)
			{
			addActionListener(new ActionListener()
				{public void actionPerformed(ActionEvent e){sp.getModel().setValue(Integer.parseInt(getText()));}});
			sp.getModel().addChangeListener(new ChangeListener()
				{public void stateChanged(ChangeEvent e){setText(""+(Integer)sp.getModel().getValue());}});
			setText(""+(int)(Integer)sp.getModel().getValue());
			}
		}
		
		
		
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
		
		spinnerFrame=new JSpinner(frameModel);
		spinnerFrame.setEditor(new IntegerEditor(spinnerFrame));
		spinnerZ=new JSpinner(zModel);
		spinnerZ.setEditor(new IntegerEditor(spinnerZ));

		
		//Build other controls and merge
		JPanel zPanel=new JPanel(new BorderLayout());
		zPanel.add(new JLabel("Z:"), BorderLayout.WEST);
		zPanel.add(spinnerZ, BorderLayout.CENTER);
		zPanel.add(checkGroupSlice, BorderLayout.EAST);
		
		JPanel fPanel=new JPanel(new BorderLayout());
				
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
			buttonPlayForward.setIcon(iconPlayStop);
			buttonPlayBack.setIcon(iconPlayStop);
			}
		else
			{
			timer=null;
			buttonPlayForward.setIcon(iconPlayForward);
			buttonPlayBack.setIcon(iconPlayBackward);
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
		Integer i=nextFrame();
		if(i!=null) setFrame((int)i);
		}
	private Integer nextFrame()
		{
		if(channel!=null && getImageset().getChannel(channel)!=null)
			return getImageset().getChannel(channel).closestFrameAfter((int)getFrame());
		else
			return null;
		}
	
	
	
	/**
	 * Move to last existing frame
	 */
	public void stepBack()
		{
		Integer i=lastFrame();
		if(i!=null) setFrame((int)i);
		}
	private Integer lastFrame()
		{
		if(channel!=null && getImageset().getChannel(channel)!=null)
			return getImageset().getChannel(channel).closestFrameBefore((int)getFrame());
		else
			return null;
		}
	
	/**
	 * Move to slice above
	 */
	public void stepUp()
		{
		Integer i=nextUp();
		if(i!=null) setZ((int)i);
		}
	private Integer nextUp()
		{
		if(channel!=null && getImageset().getChannel(channel)!=null)
			return getImageset().getChannel(channel).closestZBelow((int)getFrame(),getZ());
		else
			return null;
		}
	/**
	 * Move to slice below
	 */
	public void stepDown()
		{
		Integer i=nextDown();
		if(i!=null) setZ((int)i);
		}
	private Integer nextDown()
		{
		if(channel!=null && getImageset().getChannel(channel)!=null)
			return getImageset().getChannel(channel).closestZAbove((int)getFrame(),getZ());
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
	public void setAll(double frame, int z)
		{
		if(channel!=null && getImageset().getChannel(channel)!=null)
			{
			frame=getImageset().getChannel(channel).closestFrame((int)frame);
			z=getImageset().getChannel(channel).closestZ((int)frame, z);
			}
		removeChangeListener();
		spinnerFrame.setValue((int)frame);
		spinnerZ.setValue(z);
		addChangeListener();
		listener.stateChanged(new ChangeEvent(this));
		FrameControl.replicateSettings(this);
		}
	
	
	/** Convert world to screen Z coordinate. REPLICATED CODE, BAD! */
	public double w2sz(double z) {return z*getImageset().meta.resZ;}
	/** Convert world to screen Z coordinate. REPLICATED CODE, BAD! */
	public double s2wz(double sz) {return sz/(double)getImageset().meta.resZ;} 
	
	/**
	 * Get settings from another synchronized control
	 */
	public void replicate(double frame, Double z)
		{
		if(z==null)
			z=getModelZ();
		int slicenum=(int)Math.round(w2sz(z));
		
		if(channel!=null && getImageset().getChannel(channel)!=null)
			{
			frame=getImageset().getChannel(channel).closestFrame((int)frame);
			slicenum=getImageset().getChannel(channel).closestZ((int)frame, slicenum);
			}
		removeChangeListener();
		spinnerFrame.setValue((int)frame);
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
	public double getFrame()
		{
		return (Integer)spinnerFrame.getValue();
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
	public Double getModelZ()
		{
		return s2wz((Integer)spinnerZ.getValue());
		}
	
	
	
	/** Set current slice/Z */
	public void setZ(int z)
		{
		setAll(getFrame(), z);
		}
	
	/** Set current slice/Z */
	public void setModelZ(int z)
		{
		setAll(getFrame(), z);
		}
	
	}
