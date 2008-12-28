package endrov.util;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * JSlider that snaps back to middle when the mouse is released
 * @author Johan Henriksson
 *
 */
public class SnapBackSlider extends JSlider implements ChangeListener
	{
	static final long serialVersionUID=0;

	private int lastValue;
	
	public interface SnapChangeListener
		{
		public void slideChange(int change);
		}

	private List<SnapChangeListener> listeners=new LinkedList<SnapChangeListener>();
	
	
	public SnapBackSlider(int orientation, int min, int max) 
		{
		super(orientation,min,max,0);
		snapback();
		super.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent arg0){}
			public void mouseEntered(MouseEvent arg0){}
			public void mouseExited(MouseEvent arg0){}
			public void mousePressed(MouseEvent arg0){}
			public void mouseReleased(MouseEvent arg0)
				{
				snapback();
				}
		});
		}

	
	
	public void stateChanged(ChangeEvent e)
		{
		int c=getValue()-lastValue;
		lastValue=getValue();
		for(SnapChangeListener list:listeners)
			list.slideChange(c);
		}

	private void snapback()
		{
		removeChangeListener(this);
		setValue((getMaximum()+getMinimum())/2);
		addChangeListener(this);
		lastValue=getValue();
		}
	
	

	public void addSnapListener(SnapChangeListener c)
		{
		listeners.add(c);
		}
	
	public static void main(String arg[])
		{
		SnapBackSlider bar=new SnapBackSlider(JScrollBar.VERTICAL,0,1000);
		JFrame f=new JFrame();
		f.setSize(70,300);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(bar);
		}
	
	
	}
