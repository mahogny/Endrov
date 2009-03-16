package endrov.frameTime;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import endrov.basicWindow.EvDecimalEditor;
import endrov.basicWindow.EvDropDownButton;
import endrov.basicWindow.icon.BasicIcon;
import endrov.modelWindow.ModelWindow;
import endrov.util.EvDecimal;

/**
 * Frame control
 * @author Johan Henriksson
 *
 */
public abstract class MyFrameControl extends JPanel implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	private JSpinner spinnerFrame;

	
	/** Timer used for playback. Set to null when there is no playback */
	private javax.swing.Timer timer=null;
	
	/** Set to True if playing forward, False if playing backwards */
	private boolean playingForward=true;
	
	
	private JButton buttonStepBack=new JButton(BasicIcon.iconFramePrev);
	private JButton buttonStepForward=new JButton(BasicIcon.iconFrameNext);
	private JButton buttonPlayBack=new JButton(BasicIcon.iconPlayBackward);
	private JButton buttonPlayForward=new JButton(BasicIcon.iconPlayForward);
	private JButton buttonBeginning=new JButton(BasicIcon.iconFrameFirst);
	private JButton buttonEnd=new JButton(BasicIcon.iconFrameLast);
	
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
	
	/**
	 * Drop-down menu button to select FrameTime mapping
	 */
	public EvDropDownButton buttonFrameTime=new EvDropDownButton("FT")
		{
		private static final long serialVersionUID = 1L;

		@Override
		public JPopupMenu createPopup()
			{
			JPopupMenu popup = new JPopupMenu();
			JMenuItem menuItem = new JMenuItem("A popup menu item");
			menuItem.addActionListener(this);
			popup.add(menuItem);
			menuItem = new JMenuItem("Another popup menu item");
			menuItem.addActionListener(this);
			popup.add(menuItem);
			
			return popup;
			}
		};
	
	
	public void actionPerformed(ActionEvent e)
		{
		
		
		}
	
	public abstract EvDecimal lastFrame();
	public abstract EvDecimal nextFrame();
	
	
	public SpinnerModel frameModel=new SpinnerModel()
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
		};


	public MyFrameControl()
		{
		
		spinnerFrame=new JSpinner(frameModel);
		spinnerFrame.setEditor(new EvDecimalEditor(spinnerFrame));

		JPanel gridLeft=new JPanel(new GridLayout(1,3));
		gridLeft.add(buttonBeginning);
		gridLeft.add(buttonPlayBack);
		gridLeft.add(buttonStepBack);

		JPanel gridRight=new JPanel(new GridLayout(1,4));
		gridRight.add(buttonFrameTime);
		gridRight.add(buttonStepForward);
		gridRight.add(buttonPlayForward);
		gridRight.add(buttonEnd);

		setLayout(new BorderLayout());
		add(gridLeft,BorderLayout.WEST);
		add(spinnerFrame,BorderLayout.CENTER);
		add(gridRight,BorderLayout.EAST);

		buttonFrameTime.addActionListener(this);

		Vector<Speed> speeds=new Vector<Speed>();
		speeds.add(new Speed("0.01"));
		speeds.add(new Speed("0.1"));
		speeds.add(new Speed("1"));
		speeds.add(new Speed("10"));
		speeds.add(new Speed("100"));
		speeds.add(new Speed("1000"));
		speeds.add(new Speed("10000"));
		speedCombo=new JComboBox(speeds);
		speedCombo.setSelectedIndex(2);
		
		//TODO: idea: can use nextFrame to find when next frame occurs. then adjust speed with this.
		//fits with imagewindow
		
		}
		
		
	/** 
	 * Get current frame 
	 */
	public EvDecimal getFrame()
		{
		return (EvDecimal)spinnerFrame.getValue();
		}
	
	/**
	 * Set current frame. will NOT trigger replicateSettings or any listeners!
	 */
	public void setFrame(EvDecimal frame)
		{
		spinnerFrame.setValue(frame);
		}

	
		
	}
