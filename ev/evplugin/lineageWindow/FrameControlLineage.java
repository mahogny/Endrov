package evplugin.lineageWindow;

import java.awt.BorderLayout;

import javax.swing.*;
import javax.swing.event.*;
import evplugin.basicWindow.*;



//todo: spinners are based on integer. low precision?

/**
 * Extremely reduced control. Just listens to current frame.
 * @author Johan Henriksson
 */
public class FrameControlLineage extends JPanel implements FrameControl.Synch
	{
	static final long serialVersionUID=0;
	
	/** Component to tell that frame has changed */
	private final ChangeListener listener;

	private SpinnerModel groupModel=new SpinnerNumberModel(0,0,9,1);
	private JSpinner spinnerGroup=new JSpinner(groupModel);
	
	private double frame;
	

	/**
	 * @param l Object to receive updates on change
	 */
	public FrameControlLineage(ChangeListener l)
		{	
		listener=l;
		
		//Find a unique group ID		
		spinnerGroup.setValue(FrameControl.getUniqueGroup());
		
		//Build other controls and merge
		setLayout(new BorderLayout());
		add(new JLabel("Group:"),BorderLayout.CENTER);
		add(spinnerGroup, BorderLayout.EAST);
		//TODO listen for group, get frame if changed
		
		//Add this control to the global list of controls
		FrameControl.add(this);
		}
	

	
	/**
	 * Get settings from another synchronized control
	 */
	public void replicate(double frame, Integer z)
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
	public double getFrame()
		{
		return frame;
		}

	/** Set current frame */
	public void setFrame(double frame)
		{
		this.frame=frame;
		listener.stateChanged(new ChangeEvent(this));
		FrameControl.replicateSettings(this);
		}
	
	/** Current slice/Z */
	public Integer getZ()
		{
		return null;
		}
	}
