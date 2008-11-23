package endrov.recording.recmetManual;

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
import endrov.hardware.HardwarePath;
import endrov.hardware.PropertyType;
import endrov.recording.*;
import endrov.recording.recWindow.MicroscopeWindow;
import endrov.util.EvSwingTools;
import endrov.util.JImageToggleButton;

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
			
			
			
			
			for(Map.Entry<HardwarePath, Hardware> entry:HardwareManager.getHardwareMap().entrySet())
				{
				if(entry.getValue() instanceof HWCamera)
					hw.add(new CameraPanel(entry.getKey(),(HWCamera)entry.getValue()));
				else if(entry.getValue() instanceof HWShutter)
					hw.add(new ShutterPanel(entry.getKey(),(HWShutter)entry.getValue()));
				else if(entry.getValue() instanceof HWState)
					hw.add(new StateDevicePanel(entry.getKey(),(HWState)entry.getValue()));
				else if(entry.getValue() instanceof HWStage)
					hw.add(new StagePanel(entry.getKey(),(HWStage)entry.getValue()));
//				else
//					System.out.println("manual extension ignoring "+entry.getValue().getDescName()+" "+entry.getValue().getClass());
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
			JToggleButton b=new JImageToggleButton(iconShutterClosed,"Shutter status");
			public ShutterPanel(HardwarePath devName, HWShutter hw)
				{
				JLabel lTitle=new JLabel(devName.toString());
				lTitle.setToolTipText(hw.getDescName());
				
				setOpen(b.isSelected());
				b.addActionListener(this);
				
				setLayout(new BorderLayout());
				add(lTitle,BorderLayout.CENTER);
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
		 *                               State device                                                         *
		 *****************************************************************************************************/

		public static class StateDevicePanel extends JPanel implements ActionListener
			{
			//Cannot separate out generic state devices from filters
			
			//Since we have cubes, several filters in one. use / to separate
			//Color filters. Somewhere one need a filter database with the entire
			//range for calculations.
			private static final long serialVersionUID=0;
			private JComboBox state;
			private HWState hw;
			public StateDevicePanel(HardwarePath devName,HWState hw)
				{
				this.hw=hw;
				Vector<String> fs=new Vector<String>(hw.getStateNames());
				state=new JComboBox(fs);
				try
					{
					state.setSelectedIndex(hw.getCurrentState());
					}
				catch (Exception e)
					{
					e.printStackTrace();
					}
				
				JLabel lTitle=new JLabel(devName.toString());
				lTitle.setToolTipText(hw.getDescName());
				
				state.addActionListener(this);
				
				setLayout(new BorderLayout());
				add(lTitle,BorderLayout.CENTER);
				add(state,BorderLayout.EAST);
				}
			public void actionPerformed(ActionEvent e)
				{
				hw.setCurrentState(state.getSelectedIndex());
				}
			}
		

		/******************************************************************************************************
		 *                               Camera                                                               *
		 *****************************************************************************************************/

		public static class CameraPanel extends JPanel implements ActionListener
			{
			//TODO: build a function to decompose not only menus but entire swing components?
			static final long serialVersionUID=0;
			public CameraPanel(HardwarePath devName,final HWCamera hw)
				{
				setBorder(BorderFactory.createTitledBorder(devName.toString()));
				setToolTipText(hw.getDescName());
				
				List<JComponent> comps=new LinkedList<JComponent>();
				
				SortedMap<String, PropertyType> props=hw.getPropertyTypes();
				for(Map.Entry<String, PropertyType> entry:props.entrySet())
					{
					final String propName=entry.getKey();
					final PropertyType pt=entry.getValue();
					JComponent comp=null;
					if(pt.readOnly)
						;
					else if(pt.isBoolean)
						{
						JCheckBox b=new JCheckBox();
						b.setSelected(hw.getPropertyValueBoolean(propName));
						b.addActionListener(new ActionListener(){
							public void actionPerformed(ActionEvent e)
								{hw.setPropertyValue(propName, ((JCheckBox)e.getSource()).isSelected());}
						});
						comp=b;
						}
					else if(!pt.categories.isEmpty())
						{
						JComboBox c=new JComboBox(new Vector<String>(pt.categories));
						c.setSelectedItem(hw.getPropertyValue(propName));
						c.addActionListener(new ActionListener(){
							public void actionPerformed(ActionEvent e)
								{hw.setPropertyValue(propName, (String)((JComboBox)e.getSource()).getSelectedItem());}
						});
						comp=c;
						}
					else if(pt.hasRange)
						{
						comp=new JNumericField(0.0);
						//Override: Exposure, Gain
						}
					if(comp!=null)
						comps.add(EvSwingTools.withLabel(entry.getKey(), comp));
					//System.out.println(entry.getKey()+" "+pt.isBoolean+" "+pt.foo);
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
