/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.controlWindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdom.Element;

import endrov.basicWindow.BasicWindow;
import endrov.basicWindow.BasicWindowExtension;
import endrov.basicWindow.BasicWindowHook;
import endrov.basicWindow.EvComboData;
import endrov.basicWindow.EvComboObject;
import endrov.basicWindow.icon.BasicIcon;
import endrov.data.EvData;
import endrov.ev.EV;
import endrov.ev.JNumericField;
import endrov.ev.PersonalConfig;
import endrov.hardware.EvDevice;
import endrov.hardware.EvDeviceObserver;
import endrov.hardware.EvHardware;
import endrov.hardware.EvDevicePath;
import endrov.hardware.DevicePropertyType;
import endrov.imageset.EvChannel;
import endrov.imageset.EvImage;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.recording.*;
import endrov.util.EvDecimal;
import endrov.util.JImageButton;
import endrov.util.JImageToggleButton;
import endrov.util.JSmartToggleCombo;
import endrov.util.Strings;

/**
 * Microscope control: Manual
 * 
 * @author Johan Henriksson
 */
public class RecControlWindow extends BasicWindow
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;

	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	
	public WeakHashMap<EvComboObject,Object> listComboObject=new WeakHashMap<EvComboObject, Object>();
	
	private ConfigGroupPanel cp=new ConfigGroupPanel();
	
	public RecControlWindow()
		{
		this(null);
		}


	public RecControlWindow(Point position)
		{
		setLayout(new BorderLayout());
		add(p, BorderLayout.NORTH);

		p.setLayout(new GridBagLayout());

		add1(cp);
		
		for (Map.Entry<EvDevicePath, EvDevice> entry : EvHardware.getDeviceMap().entrySet())
			{
			if (entry.getValue() instanceof HWCamera)
				new CameraPanel(entry.getKey(), (HWCamera) entry.getValue());
			if (entry.getValue() instanceof HWShutter)
				new ShutterPanel(entry.getKey(), (HWShutter) entry.getValue());
			else if (entry.getValue() instanceof HWState)
				new StateDevicePanel(entry.getKey(), (HWState) entry.getValue());
			else if (entry.getValue() instanceof HWStage)
				new StagePanel(entry.getKey(), (HWStage) entry.getValue(),	this);
			}
		
		//Window overall things
		setTitleEvWindow("Microscope Control");
		packEvWindow();
		setVisibleEvWindow(true);
		setLocationEvWindow(position);
		}
	
	
	
	
	public void dataChangedEvent()
		{
		for(EvComboObject ob:listComboObject.keySet())
			ob.updateList();
		cp.dataChangedEvent();
		}

	public void loadedFile(EvData data){}

	public void windowSavePersonalSettings(Element root)
		{
		Element e=new Element("reccontrolwindow");
		setXMLbounds(e);
		root.addContent(e);
		} 
	public void freeResources(){}
	
	

	
	
	public static final ImageIcon iconShutterOpen = new ImageIcon(
			RecControlWindow.class.getResource("iconShutterOpen.png"));
	public static final ImageIcon iconShutterClosed = new ImageIcon(
			RecControlWindow.class.getResource("iconShutterClosed.png"));

	public static final ImageIcon iconGoAllUp = new ImageIcon(
			RecControlWindow.class.getResource("iconGoAllUp.png"));
	public static final ImageIcon iconGoAllDown = new ImageIcon(
			RecControlWindow.class.getResource("iconGoAllDown.png"));
	public static final ImageIcon iconGoAllMid = new ImageIcon(
			RecControlWindow.class.getResource("iconGoMid.png"));

	
	

	private JPanel p = new JPanel();
	private int row = 0;

	public void add2(JComponent left, JComponent right)
		{
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = row;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		p.add(left, c);
		c.gridx = 1;
		c.weightx = 1;
		p.add(right, c);
		row++;
		}

	public void add1(JComponent center)
		{
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = row;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 2;
		p.add(center, c);
		row++;
		}


	/******************************************************************************************************
	 * Shutter *
	 *****************************************************************************************************/

	public class ShutterPanel implements ActionListener, EvDeviceObserver.Listener
		{
		static final long serialVersionUID = 0;
		private JToggleButton b = new JImageToggleButton(iconShutterClosed,
				"Shutter status");
		private HWShutter hw;
		
		public ShutterPanel(EvDevicePath devName, HWShutter hw)
			{
			this.hw=hw;
			JLabel lTitle = new JLabel(devName.toString());
			lTitle.setToolTipText(hw.getDescName()+" ");

			b.setSelected(!hw.isOpen());
			b.setToolTipText("Open/Close shutter");
			setRightIcon();
			b.addActionListener(this);

			add2(lTitle, b);
			}

		public void actionPerformed(ActionEvent e)
			{
			setRightIcon();
			hw.setOpen(!b.isSelected());
			}

		private void setRightIcon()
			{
			if (b.isSelected())
				b.setIcon(iconShutterClosed);
			else
				b.setIcon(iconShutterOpen);
			b.repaint();
			}

		public void devicePropertyChange(Object source, EvDevice dev)
			{
			System.out.println("call shutter");
			b.removeActionListener(this);
			b.setSelected(!hw.isOpen());
			hw.setOpen(!b.isSelected());
			setRightIcon();
			b.addActionListener(this);
			}
		}

	/******************************************************************************************************
	 * State device *
	 *****************************************************************************************************/

	public class StateDevicePanel implements ActionListener, EvDeviceObserver.Listener
		{
		// Cannot separate out generic state devices from filters

		// Since we have cubes, several filters in one. use / to separate
		// Color filters. Somewhere one need a filter database with the entire
		// range for calculations.
		private static final long serialVersionUID = 0;
		private JSmartToggleCombo state;
		private HWState hw;

		public StateDevicePanel(EvDevicePath devName, HWState hw)
			{
			this.hw = hw;
			Vector<String> fs = new Vector<String>(hw.getStateNames());
			state = new JSmartToggleCombo(fs);
			try
				{
				state.setSelectedIndex(hw.getCurrentState());
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}

			JLabel lTitle = new JLabel(devName.toString()+" ");
			lTitle.setToolTipText(hw.getDescName());

			state.addActionListener(this);
			add2(lTitle, state);
			}

		public void actionPerformed(ActionEvent e)
			{
			hw.setCurrentState(state.getSelectedIndex());
			}

		public void devicePropertyChange(Object source, EvDevice dev)
			{
			//TODO no need to remove listener?
			//TODO no need to remove listener?
			//TODO no need to remove listener?
			state.setSelectedIndex(hw.getCurrentState());
			}
		}

	/******************************************************************************************************
	 * Camera *
	 *****************************************************************************************************/

	public class CameraPanel extends JPanel implements ActionListener, EvDeviceObserver.Listener
		{
		// TODO: build a function to decompose not only menus but entire swing
		// components?
		static final long serialVersionUID = 0;

		private int camrow=0;
		public CameraPanel(EvDevicePath devName, final HWCamera hw)
			{
			setBorder(BorderFactory.createTitledBorder(devName.toString()));
			setToolTipText(hw.getDescName());

			setLayout(new GridBagLayout());
			SortedMap<String, DevicePropertyType> props = hw.getPropertyTypes();
			for (Map.Entry<String, DevicePropertyType> entry : props.entrySet())
				{
				final String propName = entry.getKey();
				final DevicePropertyType pt = entry.getValue();
				JComponent comp = null;
				if (pt.readOnly)
					;
				else if (pt.isBoolean)
					{
					JCheckBox b = new JCheckBox();
					b.setSelected(hw.getPropertyValueBoolean(propName));
					b.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent e)
								{
								hw.setPropertyValue(propName, ((JCheckBox) e.getSource()).isSelected());
								}
						});
					comp = b;
					}
				else if (!pt.categories.isEmpty())
					{
					Vector<String> cats=new Vector<String>(pt.categories);
					Collections.sort(cats, Strings.getNaturalComparatorAscii());
					JSmartToggleCombo c = new JSmartToggleCombo(cats);
					c.setSelectedItem(hw.getPropertyValue(propName));
					c.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent e)
								{
								hw.setPropertyValue(propName, (String) ((JSmartToggleCombo) e
										.getSource()).getSelectedItem());
								}
						});
					comp = c;
					}
				else //if (pt.hasRange)
					{
					final JNumericField b=new JNumericField(Double.parseDouble(hw.getPropertyValue(propName)));
					final Color defaultBG=b.getBackground();
					comp = b;
					// Override: Exposure, Gain

					b.getDocument().addDocumentListener(new DocumentListener()
						{
						public void changedUpdate(DocumentEvent e){sendAction();}
						public void insertUpdate(DocumentEvent e){sendAction();}
						public void removeUpdate(DocumentEvent e){sendAction();}
						private void sendAction()
							{
							b.setBackground(Color.YELLOW);
							}
						});	
					
					b.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
							{
							b.setBackground(defaultBG);
							String value = ((JNumericField) e.getSource()).getText();
							double d;
							try
								{
								d = Double.parseDouble(value);
								} 
							catch(NumberFormatException ex) 
								{
								((JNumericField) e.getSource()).setText(hw.getPropertyValue(propName));
								return;
								}
							
							if(pt.hasRange)
								{
								if(pt.rangeLower<=d && pt.rangeUpper >=d)
									hw.setPropertyValue(propName, value);
								else 
									{ 
									((JNumericField) e.getSource()).setText(hw.getPropertyValue(propName));
									return;
									}
								}
							else
								{
								hw.setPropertyValue(propName, value);
								}
							}
					});
					
					
					}
				if (comp!=null)
					addComp(entry.getKey(), comp);
				}

			
			//Snapping
			final JTextField tChannel=new JTextField("ch0");
			addComp("To channel",tChannel);
			final EvComboData comboData=new EvComboData(false);
			listComboObject.put(comboData,null);
			addComp("To data",comboData);
			JButton bSnap=new JImageButton(BasicIcon.iconButtonRecord,"Acquire single image");
			addComp("Manual", bSnap);

			bSnap.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0)
					{
					String channelName=tChannel.getText();
					EvData data=comboData.getData();
					if(data==null || channelName.equals(""))
						BasicWindow.showErrorDialog("Select where to store image and choose a proper channel name");
					else
						{
						String imsetName;
						for(int i=0;data.metaObject.containsKey(imsetName="im"+i);i++);
						Imageset imset=new Imageset();
						data.metaObject.put(imsetName, imset);
						RecordingResource.soundCameraSnap.start();
						
						EvChannel ch=imset.getCreateChannel(tChannel.getText());
						EvStack stack=new EvStack();//.getCreateFrame(new EvDecimal(0));
						ch.putStack(new EvDecimal(0), stack);
						CameraImage cim=hw.snap();
						
						//TODO
						stack.resX=1;
						stack.resY=1;
						stack.resZ=1;
						
						//TODO RGB support
						
						EvImage evim=new EvImage();
						evim.setPixelsReference(cim.getPixels()[0]);
						stack.putInt(0,evim);
						
						BasicWindow.updateWindows(); //TODO trigger on data
						
						}
					}
			});
			
			
			add1(this);
			}

		private void addComp(String title, JComponent comp)
			{
			JLabel label = new JLabel(title);

			GridBagConstraints c = new GridBagConstraints();
			c.gridy = camrow;
			c.anchor = GridBagConstraints.LINE_START;
			// c.anchor=GridBagConstraints.BASELINE_LEADING;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 0;
			add(label, c);
			c.gridx = 1;
			c.weightx = 1;
			add(comp, c);
			camrow++;
			}

		public void actionPerformed(ActionEvent e)
			{
			}

		public void devicePropertyChange(Object source, EvDevice dev)
			{
			// TODO Auto-generated method stub
			
			}
		}

	/**
	 * Panel horizontally separating with dots: [. . . . . . .]
	 * @author Johan Henriksson
	 *
	 */
	public static class DotPanel extends JPanel
		{
		static final long serialVersionUID = 0;

		protected void paintComponent(Graphics g)
			{
			super.paintComponent(g);
			int w = getWidth();
			int h = getHeight();
			g.setColor(Color.BLACK);
			for (int x = 0; x<w; x += 5)
				g.drawLine(x, h/2, x+2, h/2);
			}

		}

	


	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		BasicWindow.addBasicWindowExtension(new BasicWindowExtension()
			{
			public void newBasicWindow(BasicWindow w)
				{
				w.basicWindowExtensionHook.put(this.getClass(),new Hook());
				}
			class Hook implements BasicWindowHook, ActionListener
			{
			public void createMenus(BasicWindow w)
				{
				JMenuItem mi=new JMenuItem("Control",new ImageIcon(getClass().getResource("iconWindow.png")));
				mi.addActionListener(this);
				BasicWindow.addMenuItemSorted(w.getCreateMenuWindowCategory("Recording"), mi);
				}

			public void actionPerformed(ActionEvent e) 
				{
				new RecControlWindow();
				}

			public void buildMenu(BasicWindow w){}
			}
			});
		
		
		EV.personalConfigLoaders.put("reccontrolwindow",new PersonalConfig()
			{
			public void loadPersonalConfig(Element e)
				{
				new RecControlWindow(BasicWindow.getXMLposition(e));
				}
			public void savePersonalConfig(Element e)
				{
				}
			});
		}
	
	}
