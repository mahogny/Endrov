package endrov.recording.manualRec;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;

import endrov.ev.JNumericField;
import endrov.hardware.Hardware;
import endrov.hardware.HardwareManager;
import endrov.hardware.PropertyType;
import endrov.recording.HWCamera;
import endrov.recording.HWFilter;
import endrov.recording.HWShutter;
import endrov.recording.recWindow.MicroscopeWindow;
import endrov.util.EvSwingTools;

/**
 * Microscope control: Manual
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
			
			
			
			
			for(Map.Entry<String, Hardware> entry:HardwareManager.getHardwareMap().entrySet())
				{
				if(entry.getValue() instanceof HWCamera)
					hw.add(new CameraPanel(entry.getKey(),(HWCamera)entry.getValue()));
				else if(entry.getValue() instanceof HWShutter)
					hw.add(new ShutterPanel(entry.getKey(),(HWShutter)entry.getValue()));
				else if(entry.getValue() instanceof HWFilter)
					hw.add(new FilterPanel(entry.getKey(),(HWFilter)entry.getValue()));
				System.out.println(entry.getValue().getClass());
				}
			
			
			int counta=0;
			JPanel p=new JPanel(new GridBagLayout());
			for(JComponent c:hw)
				{
				GridBagConstraints cr=new GridBagConstraints();	cr.gridy=counta;	cr.fill=GridBagConstraints.HORIZONTAL;
				cr.weightx=1;
				p.add(c,cr);
				counta++;
				}	
			
			setLayout(new BorderLayout());
			add(p,BorderLayout.NORTH);

			
			
			}
		
		
		
		/******************************************************************************************************
		 *                               Shutter                                                              *
		 *****************************************************************************************************/

		public static class ShutterPanel extends JPanel implements ActionListener
			{
			static final long serialVersionUID=0;
			JToggleButton b=new JToggleButton(iconShutterClosed);
			public ShutterPanel(String devName, HWShutter hw)
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
		
		
		/******************************************************************************************************
		 *                               Filter                                                               *
		 *****************************************************************************************************/

		public static class FilterPanel extends JPanel implements ActionListener
			{
			//Since we have cubes, several filters in one. use / to separate
			//Color filters. Somewhere one need a filter database with the entire
			//range for calculations.
			static final long serialVersionUID=0;
			JComboBox filters;
			public FilterPanel(String devName,HWFilter hw)
				{
				Vector<String> fs=new Vector<String>();
				fs.add("500 / 300");
				fs.add("550 / 200");
				filters=new JComboBox(fs);
				
				String name="Filter set";
				
				filters.setSelectedIndex(0); //TODO
				filters.addActionListener(this);
				
				setLayout(new BorderLayout());
				add(new JLabel(name),BorderLayout.CENTER);
				add(filters,BorderLayout.EAST);
				}
			public void actionPerformed(ActionEvent e)
				{
				}
			}
		

		/******************************************************************************************************
		 *                               Camera                                                               *
		 *****************************************************************************************************/

		public static class CameraPanel extends JPanel implements ActionListener
			{
			static final long serialVersionUID=0;
			public CameraPanel(String devName,HWCamera hw)
				{
				setBorder(BorderFactory.createTitledBorder(devName));
				
				List<JComponent> comps=new LinkedList<JComponent>();
				
				SortedMap<String, PropertyType> props=hw.getPropertyTypes();
				for(Map.Entry<String, PropertyType> entry:props.entrySet())
					{
					PropertyType pt=entry.getValue();
					JComponent comp=null;
					if(pt.readOnly)
						;
					else if(pt.isBoolean)
						{
						comp=new JCheckBox();
						}
					else if(!pt.categories.isEmpty())
						{
						comp=new JComboBox(new Vector<String>(pt.categories));
						}
					else if(pt.hasRange)
						{
						comp=new JNumericField(0.0);
						//Override: Exposure, Gain
						}
					if(comp!=null)
						comps.add(EvSwingTools.withLabel(entry.getKey(), comp));
					System.out.println(entry.getKey()+" "+pt.isBoolean+" "+pt.foo);
					}
				setLayout(new GridLayout(comps.size(),1));
				for(JComponent c:comps)
					add(c);
				}
			public void actionPerformed(ActionEvent e)
				{
				}
			}

		
		
		}
	
	}
