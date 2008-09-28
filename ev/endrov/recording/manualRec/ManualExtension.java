package endrov.recording.manualRec;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;

import endrov.recording.recWindow.MicroscopeWindow;

/**
 * GUI: Manual control of the microscope
 * @author Johan Henriksson
 *
 */
public class ManualExtension implements MicroscopeWindow.Extension
	{
	public static final ImageIcon iconShutterOpen=new ImageIcon(ManualExtension.class.getResource("iconShutterOpen.png"));
	public static final ImageIcon iconShutterClosed=new ImageIcon(ManualExtension.class.getResource("iconShutterClosed.png"));
	
	public static final ImageIcon iconGoAllUp=new ImageIcon(ManualExtension.class.getResource("iconGoAllUp.png"));
	public static final ImageIcon iconGoAllDown=new ImageIcon(ManualExtension.class.getResource("iconGoAllDown.png"));
	public static final ImageIcon iconGoAllMid=new ImageIcon(ManualExtension.class.getResource("iconGoMid.png"));
  	
	
	public static void initPlugin() {}
	static
		{
		MicroscopeWindow.addMicroscopeWindowExtension("Manual Mode", new ManualExtension());
		}
	
	public JComponent addControls()
		{
		return new Hook();
		}
	
	
	
	
	///////////////////////////////////////////////////////////////////////
	public static class Hook extends JPanel
		{
		static final long serialVersionUID=0;
		
		
		
		
		public Hook()
			{
			List<JComponent> hw=new Vector<JComponent>();
			
			///////// Shutter
			hw.add(new ShutterPanel());
			
			JPanel p=new JPanel(new GridLayout(hw.size(),1));
			setLayout(new BorderLayout());
			add(p,BorderLayout.NORTH);
			for(JComponent c:hw)
				p.add(c);
			
			
			
			}
		
		
		
		
		public static class ShutterPanel extends JPanel implements ActionListener
			{
			static final long serialVersionUID=0;
			JToggleButton b=new JToggleButton(iconShutterClosed);
			public ShutterPanel()
				{
				String name="Uniblitz shutter";
				
				setOpen(b.isSelected());
				b.addActionListener(this);
				
				setLayout(new BorderLayout());
				add(new JLabel(name),BorderLayout.CENTER);
				add(b,BorderLayout.EAST);
				}
			public void actionPerformed(ActionEvent e)
				{
				setOpen(!b.isSelected());
				System.out.println(b.isSelected());
				}
			public void setOpen(boolean isOpen)
				{
				if(b.isSelected())
					b.setIcon(iconShutterClosed);
				else
					b.setIcon(iconShutterOpen);
				b.repaint();
				}
			}
		
		
		
		}
	
	}
