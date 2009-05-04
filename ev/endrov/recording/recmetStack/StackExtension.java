package endrov.recording.recmetStack;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;

import javax.swing.*;

import endrov.recording.recWindow.MicroscopeWindow;
import endrov.recording.recWindow.MicroscopeWindow.ExtensionInstance;

/**
 * Microscope control: Capture stack
 * @author Johan Henriksson
 *
 */
public class StackExtension
	{
  	
	
	public static void initPlugin() {}
	static
		{
		MicroscopeWindow.addMicroscopeWindowExtension(
				new MicroscopeWindow.Extension(){
					public ExtensionInstance getInstance()
						{
						return new Hook();
						}

					public String getName()
						{
						return "Stacl";
						}
				
				}
				);
		
		}
	
	
	
	
	
	///////////////////////////////////////////////////////////////////////
	public static class Hook extends MicroscopeWindow.ExtensionInstance
		{
		static final long serialVersionUID=0;
		
		
		
		
		public Hook()
			{
		
			JPanel p=new JPanel(new GridBagLayout());
			
			setLayout(new BorderLayout());
			add(p,BorderLayout.NORTH);

			
			
			}




		public void dataChangedEvent()
			{
			}
		
		
		
		
		
		}
	
	}
