package endrov.recording.propertyWindow;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import endrov.basicWindow.BasicWindow;
import endrov.basicWindow.icon.BasicIcon;
import endrov.hardware.EvHardwareConfigGroup;
import endrov.util.EvSwingUtil;
import endrov.util.JImageButton;

/**
 * Panel to choose state using config groups
 * 
 * @author Johan Henriksson
 *
 */
public class NewConfigGroupPanel extends JPanel implements ActionListener
	{
	private static final long serialVersionUID = 1L;

		private JComboBox cState=new JComboBox();
		private JButton bAddState=new JImageButton(BasicIcon.iconAdd, "New state for current config group");
		private JButton bRemoveState=new JImageButton(BasicIcon.iconRemove, "Remove state from group");
		private JButton bRemoveGroup=new JImageButton(BasicIcon.iconRemove, "Remove config group");
		private String groupName;
		
		public NewConfigGroupPanel(String groupName)
			{
			this.groupName=groupName;
			
			setLayout(new BorderLayout());
			add(EvSwingUtil.layoutLCR(bRemoveGroup,new JLabel(groupName+" "),null), BorderLayout.WEST);
			add(cState, BorderLayout.CENTER);
			add(EvSwingUtil.layoutEvenHorizontal(bAddState,bRemoveState),
					BorderLayout.EAST);
			
			DefaultComboBoxModel modelState=(DefaultComboBoxModel)cState.getModel();
			modelState.removeAllElements();
			EvHardwareConfigGroup group=EvHardwareConfigGroup.groups.get(groupName);
			modelState.addElement("");
			for(String stateName:group.states.keySet())
				modelState.addElement(stateName);

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
				if(BasicWindow.showConfirmDialog("Do you really want to remove the group "+groupName+"?"))
					{
					EvHardwareConfigGroup.groups.remove(groupName);
					BasicWindow.updateWindows();
					}
				}
			else if(e.getSource()==bAddState)
				{
				String stateName=JOptionPane.showInputDialog(this, "Current state will be saved in group. Name?");
				if(stateName!=null)
					{
					EvHardwareConfigGroup.groups.get(groupName).captureCurrentState(stateName);
					//lastComboSetting.put(groupName, stateName);
					BasicWindow.updateWindows();
					}
				}
			else if(e.getSource()==bRemoveState)
				{
				if(BasicWindow.showConfirmDialog("Do you really want to remove the state "+cState.getSelectedItem()+"?"))
					{
					EvHardwareConfigGroup.groups.get(groupName).states.remove(cState.getSelectedItem());
					BasicWindow.updateWindows();
					}
				}
			else if(e.getSource()==cState)
				{
				String stateName=(String)cState.getSelectedItem();
				//lastComboSetting.put(groupName, stateName);
				EvHardwareConfigGroup.groups.get(groupName).getState(stateName).activate();
				}
			
			
			}
		
		


	public void dataChangedEvent()
		{
		
		}

	}
