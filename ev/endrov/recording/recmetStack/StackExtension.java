package endrov.recording.recmetStack;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;

import javax.swing.*;

import endrov.recording.recWindow.MicroscopeWindow;

/**
 * Microscope control: Capture stack
 * @author Johan Henriksson
 *
 */
public class StackExtension implements MicroscopeWindow.Extension
	{
  	
	
	public static void initPlugin() {}
	static
		{
		MicroscopeWindow.addMicroscopeWindowExtension("Stack", new StackExtension());
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
		
			JPanel p=new JPanel(new GridBagLayout());
			
			setLayout(new BorderLayout());
			add(p,BorderLayout.NORTH);

			
			
			}
		
		
		
		
		
		}
	
	}
