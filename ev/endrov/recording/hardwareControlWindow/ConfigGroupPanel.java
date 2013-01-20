package endrov.recording.hardwareControlWindow;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import endrov.gui.EvSwingUtil;
import endrov.gui.component.JImageButton;
import endrov.gui.icon.BasicIcon;
import endrov.gui.window.EvBasicWindow;
import endrov.hardware.EvDevice;
import endrov.hardware.EvDevicePropPath;
import endrov.hardware.EvHardwareConfigGroup;

/**
 * Panel to choose state using config groups
 * 
 * @author Johan Henriksson
 *
 */
public class ConfigGroupPanel extends JPanel implements ActionListener
	{
	private static final long serialVersionUID = 1L;

		private JComboBox cState=new JComboBox();
		private JButton bAddState=new JImageButton(BasicIcon.iconAdd, "New state for current config group");
		private JButton bRemoveState=new JImageButton(BasicIcon.iconRemove, "Remove state from group");
		private JButton bRemoveGroup=new JImageButton(BasicIcon.iconRemove, "Remove config group");
		private String groupName;
		
		public ConfigGroupPanel(String groupName)
			{
			this.groupName=groupName;
			
			setLayout(new BorderLayout());
//			add(EvSwingUtil.layoutLCR(bRemoveGroup,new JLabel(groupName+" "),null), BorderLayout.WEST);
			add(bRemoveGroup, BorderLayout.WEST);
			
			EvHardwareConfigGroup group=EvHardwareConfigGroup.getConfigGroup(groupName);

			
			if(group.propsToInclude.size()==1)
				{
				//If it is a single device then allow direct control of it
				EvDevicePropPath devicePropPath=group.propsToInclude.iterator().next();
				EvDevice device=devicePropPath.getDevice();
				if(device!=null)
					{
					String property=devicePropPath.getProperty();
					JComponent propcomp=HardwareControlWindow.createComponentForProperty(device, property);
					if(propcomp!=null)
						add(propcomp, BorderLayout.CENTER);
					else
						System.out.println("Problem with "+devicePropPath);
					}
				else
					System.out.println("Device does not exist "+devicePropPath);
				}
			else
				{
				add(cState, BorderLayout.CENTER);
				add(EvSwingUtil.layoutEvenHorizontal(bAddState,bRemoveState),
						BorderLayout.EAST);
				
				DefaultComboBoxModel modelState=(DefaultComboBoxModel)cState.getModel();
				modelState.removeAllElements();
				modelState.addElement("");
				for(String stateName:group.getStateNames())
					modelState.addElement(stateName);
				}
			
			
			

			/*
			String lastState=lastComboSetting.get(groupName);
			if(lastState!=null)
				cState.setSelectedItem(lastState);*/

			bRemoveGroup.addActionListener(this);
			bAddState.addActionListener(this);
			bRemoveState.addActionListener(this);
			cState.addActionListener(this);
			}
		

		public void actionPerformed(ActionEvent e)
			{
			if(e.getSource()==bRemoveGroup)
				{
				if(EvBasicWindow.showConfirmYesNoDialog("Do you really want to remove the group "+groupName+"?"))
					{
					EvHardwareConfigGroup.removeConfigGroup(groupName);
					EvBasicWindow.updateWindows();
					}
				}
			else if(e.getSource()==bAddState)
				{
				String stateName=JOptionPane.showInputDialog(this, "Current state will be saved in group. Name?");
				if(stateName!=null)
					{
					EvHardwareConfigGroup.getConfigGroup(groupName).captureCurrentStateAsNew(stateName);
					//lastComboSetting.put(groupName, stateName);
					EvBasicWindow.updateWindows();
					}
				}
			else if(e.getSource()==bRemoveState)
				{
				if(cState.getSelectedIndex()!=0)
					if(EvBasicWindow.showConfirmYesNoDialog("Do you really want to remove the state "+cState.getSelectedItem()+"?"))
						{
						EvHardwareConfigGroup.getConfigGroup(groupName).removeState((String)cState.getSelectedItem());
						EvBasicWindow.updateWindows();
						}
				}
			else if(e.getSource()==cState)
				{
				String stateName=(String)cState.getSelectedItem();
				//lastComboSetting.put(groupName, stateName);
				EvHardwareConfigGroup.getConfigGroup(groupName).getState(stateName).activate();
				}
			
			
			}
		
		


	public void dataChangedEvent()
		{
		
		}

	}
