package endrov.frameTime;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import endrov.basicWindow.FrameControl;
import endrov.util.EvDecimal;


/**
 * Editor for EvDecimal spinners 
 * @author Johan Henriksson
 */	
public class EvFrameEditor extends JTextField
	{
	static final long serialVersionUID=0;
	private FrameTime currentFrameTime=null;
	private final SpinnerModel sm;
	
	
	ActionListener alist=new ActionListener()
		{public void actionPerformed(ActionEvent e)
			{
			removeActionListener(this);
			String newText=FrameControl.formatTime(FrameControl.parseTime(getText()));
			setText(newText);
			addActionListener(this);
			getModel().setValue(getFrame());
			}
		};
		
	
	public EvFrameEditor(final JSpinner sp)
		{
		sm=sp.getModel();
		addActionListener(alist);
		sp.getModel().addChangeListener(new ChangeListener()
			{public void stateChanged(ChangeEvent e)
				{
				setFrame((EvDecimal)sp.getModel().getValue());
				}
			});
		setFrame((EvDecimal)sp.getModel().getValue());
		}
	
	
	
	private SpinnerModel getModel()
		{
		return sm;
		}
	
	
	
	/**
	 * Set text as frame
	 */
	private void setFrame(EvDecimal d)
		{
		if(currentFrameTime!=null)
			d=currentFrameTime.interpolateTime(d);		
		setText(FrameControl.formatTime(d));
		}

	/**
	 * Get text as frame
	 */
	private EvDecimal getFrame()
		{
		EvDecimal d=FrameControl.parseTime(getText());
		if(currentFrameTime!=null)
			d=currentFrameTime.interpolateFrame(d);
		return d;
		}

	/**
	 * Set frame-time map
	 */
	public void setFrameTime(FrameTime ft)
		{
		EvDecimal d=getFrame();
		currentFrameTime=ft;
		setFrame(d);
		sm.setValue(getFrame());
		System.out.println("setframetime "+ft);
		}
	
	}