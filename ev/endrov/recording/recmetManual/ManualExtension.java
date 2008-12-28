package endrov.recording.recmetManual;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import endrov.util.JImageToggleButton;
import endrov.util.JSmartToggleCombo;

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
		
		
		GroupLayout lay=null;
		GroupLayout.SequentialGroup hgroup=null;
		GroupLayout.SequentialGroup vgroup=null;
		GroupLayout.ParallelGroup leftcol=null;
		GroupLayout.ParallelGroup rightcol=null;

		JPanel p=new JPanel();
		int row=0;
		
		public void add2(JComponent left,JComponent right)
			{
			GridBagConstraints c=new GridBagConstraints();
			c.gridy=row;
			c.anchor=GridBagConstraints.BASELINE_LEADING;
			c.fill=GridBagConstraints.HORIZONTAL;
			c.weightx=0;
			p.add(left,c);
			c.gridx=1;
			c.weightx=1;
			p.add(right,c);
			row++;
			}
		public void add1(JComponent center)
			{
			GridBagConstraints c=new GridBagConstraints();
			c.gridy=row;
			c.fill=GridBagConstraints.HORIZONTAL;
			c.gridwidth=2;
			p.add(center,c);
			row++;
			}
		
		public Hook()
			{
//			List<JComponent> hw=new Vector<JComponent>();
			//boolean isEven=true;
			
//			JPanel p=new JPanel();
			setLayout(new BorderLayout());
			add(p,BorderLayout.NORTH);

			p.setLayout(new GridBagLayout());
			
			
			for(Map.Entry<HardwarePath, Hardware> entry:HardwareManager.getHardwareMap().entrySet())
				{
				//isEven=!isEven;
		//		JComponent c=null;
			
				
				
				if(entry.getValue() instanceof HWCamera)
					/*c=*/ 
					new CameraPanel(entry.getKey(),(HWCamera)entry.getValue());
	//			else 
					if(entry.getValue() instanceof HWShutter)
					/*c=*/new ShutterPanel(entry.getKey(),(HWShutter)entry.getValue());
				else if(entry.getValue() instanceof HWState)
					/*c=*/new StateDevicePanel(entry.getKey(),(HWState)entry.getValue());
				else if(entry.getValue() instanceof HWStage)
					/*c=*/new StagePanel(entry.getKey(),(HWStage)entry.getValue(),this);
/*				if(c!=null)
					{
					hw.add(c);*/
					/*
					if(isEven)
						{
						Color col=c.getBackground();
						c.setBackground(new Color(col.getRed()*97/100,col.getGreen()*97/100,col.getBlue()*97/100));
						c.setOpaque(true);
						}
						*/
	//				}
				
//				else
//					System.out.println("manual extension ignoring "+entry.getValue().getDescName()+" "+entry.getValue().getClass());
				}
			
			/*
			int counta=0;
			JPanel p=new JPanel(new GridBagLayout());
			for(JComponent c:hw)
				{
				GridBagConstraints cr=new GridBagConstraints();	cr.gridy=counta;	cr.fill=GridBagConstraints.HORIZONTAL;
				cr.weightx=1;
				p.add(c,cr);
				counta++;
				}	
			
			*/
			
			}
		
		
		
		/******************************************************************************************************
		 *                               Shutter                                                              *
		 *****************************************************************************************************/

		public class ShutterPanel implements ActionListener
			{
			static final long serialVersionUID=0;
			JToggleButton b=new JImageToggleButton(iconShutterClosed,"Shutter status");
			public ShutterPanel(HardwarePath devName, HWShutter hw)
				{
				JLabel lTitle=new JLabel(devName.toString());
				lTitle.setToolTipText(hw.getDescName()+" ");
				
				setOpen(b.isSelected());
				b.addActionListener(this);
				
				add2(lTitle,b);
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

		public class StateDevicePanel implements ActionListener
			{
			//Cannot separate out generic state devices from filters
			
			//Since we have cubes, several filters in one. use / to separate
			//Color filters. Somewhere one need a filter database with the entire
			//range for calculations.
			private static final long serialVersionUID=0;
			private JSmartToggleCombo state;
			private HWState hw;
			public StateDevicePanel(HardwarePath devName,HWState hw)
				{
				this.hw=hw;
				Vector<String> fs=new Vector<String>(hw.getStateNames());
				state=new JSmartToggleCombo(fs);
				try
					{
					state.setSelectedIndex(hw.getCurrentState());
					}
				catch (Exception e)
					{
					e.printStackTrace();
					}
				
				JLabel lTitle=new JLabel(devName.toString()+" ");
				lTitle.setToolTipText(hw.getDescName());
				
				state.addActionListener(this);
				add2(lTitle,state);
				}
			public void actionPerformed(ActionEvent e)
				{
				hw.setCurrentState(state.getSelectedIndex());
				}
			}
		

		/******************************************************************************************************
		 *                               Camera                                                               *
		 *****************************************************************************************************/

		public class CameraPanel extends JPanel implements ActionListener
			{
			//TODO: build a function to decompose not only menus but entire swing components?
			static final long serialVersionUID=0;
			public CameraPanel(HardwarePath devName,final HWCamera hw)
				{
				setBorder(BorderFactory.createTitledBorder(devName.toString()));
				setToolTipText(hw.getDescName());
				
				GroupLayout lay=new GroupLayout(this);
				setLayout(lay);
				GroupLayout.SequentialGroup vgroup=lay.createSequentialGroup();
				GroupLayout.SequentialGroup hgroup=lay.createSequentialGroup();
				GroupLayout.ParallelGroup leftcol=lay.createParallelGroup();
				GroupLayout.ParallelGroup rightcol=lay.createParallelGroup();
				
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
//						JPanel p=new JPanel(new BorderLayout());
//						p.add(new DotPanel(),BorderLayout.CENTER);
//						p.add(b,BorderLayout.EAST);
//						comp=p;
						comp=b;
						}
					else if(!pt.categories.isEmpty())
						{
						JSmartToggleCombo c=new JSmartToggleCombo(new Vector<String>(pt.categories));
						c.setSelectedItem(hw.getPropertyValue(propName));
						c.addActionListener(new ActionListener(){
							public void actionPerformed(ActionEvent e)
								{hw.setPropertyValue(propName, (String)((JSmartToggleCombo)e.getSource()).getSelectedItem());}
						});
						/*
						JComboBox c=new JComboBox(new Vector<String>(pt.categories));
						c.setSelectedItem(hw.getPropertyValue(propName));
						c.addActionListener(new ActionListener(){
							public void actionPerformed(ActionEvent e)
								{hw.setPropertyValue(propName, (String)((JComboBox)e.getSource()).getSelectedItem());}
						});
						*/
						comp=c;
						}
					else if(pt.hasRange)
						{
						comp=new JNumericField(0.0);
						//Override: Exposure, Gain
						}
					if(comp!=null)
						{
						JLabel label=new JLabel(entry.getKey());
						vgroup.addGroup(lay.createParallelGroup().
								addComponent(label).addComponent(comp));
						leftcol.addComponent(label);
						rightcol.addComponent(comp);
						}
					}
				lay.setVerticalGroup(vgroup);
				lay.setHorizontalGroup(hgroup);
				hgroup.addGroup(leftcol);
				hgroup.addGroup(rightcol);
				
				add1(this);
				}
			public void actionPerformed(ActionEvent e)
				{
				}
			}

		/******************************************************************************************************
		 *                               Dot panel                                                            *
		 *****************************************************************************************************/
		public static class DotPanel extends JPanel
			{
			static final long serialVersionUID=0;

			protected void paintComponent(Graphics g)
				{
				super.paintComponent(g);
				int w=getWidth();
				int h=getHeight();
				g.setColor(Color.BLACK);
				for(int x=0;x<w;x+=5)
					g.drawLine(x, h/2, x+2, h/2);
				}
			
			
			}
		
		
		
		}
	
	}
