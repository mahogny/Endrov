package endrov.recording.controlWindow;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import endrov.basicWindow.BasicWindow;
import endrov.basicWindow.icon.BasicIcon;
import endrov.hardware.EvHardwareConfigGroup;
import endrov.recording.propertyWindow.NewConfigGroupWindow;
import endrov.util.EvSwingUtil;
import endrov.util.JImageButton;

/**
 * Panel to choose state using config groups
 * 
 * @author Johan Henriksson
 *
 */
public class OldConfigGroupPanel extends JPanel implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	private JButton bAddGroup=new JImageButton(BasicIcon.iconAdd, "New config group");
	
	private JPanel pGroups=new JPanel();
	private HashMap<String,String> lastComboSetting=new HashMap<String, String>();
	
	/**
	 * Configuration for one group
	 */
	private class StatesPanel extends JPanel implements ActionListener
		{
		private static final long serialVersionUID = 1L;
		private JComboBox cState=new JComboBox();
		private JButton bAddState=new JImageButton(BasicIcon.iconAdd, "New state for current config group");
		private JButton bRemoveState=new JImageButton(BasicIcon.iconRemove, "Remove state from group");
		private JButton bRemoveGroup=new JImageButton(BasicIcon.iconRemove, "Remove config group");
		private String groupName;
		
		public StatesPanel(String groupName)
			{
			this.groupName=groupName;
			
			setLayout(new BorderLayout());
			add(EvSwingUtil.layoutLCR(bRemoveGroup,new JLabel(groupName+" "),null), BorderLayout.WEST);
			add(cState, BorderLayout.CENTER);
			add(EvSwingUtil.layoutEvenHorizontal(bAddState,bRemoveState),
					BorderLayout.EAST);
			
			DefaultComboBoxModel modelState=(DefaultComboBoxModel)cState.getModel();
			modelState.removeAllElements();
			EvHardwareConfigGroup group=EvHardwareConfigGroup.getConfigGroup(groupName);
			for(String stateName:group.getStateNames())
				modelState.addElement(stateName);

			String lastState=lastComboSetting.get(groupName);
			if(lastState!=null)
				cState.setSelectedItem(lastState);

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
					//EvHardwareConfigGroup.groups.remove(groupName);  //TODO this class to be deleted
					BasicWindow.updateWindows();
					}
				}
			else if(e.getSource()==bAddState)
				{
				String stateName=JOptionPane.showInputDialog(this, "Current state will be saved in group. Name?");
				if(stateName!=null)
					{
					EvHardwareConfigGroup.getConfigGroup(groupName).captureCurrentStateAsNew(stateName);
					lastComboSetting.put(groupName, stateName);
					BasicWindow.updateWindows();
					}
				}
			else if(e.getSource()==bRemoveState)
				{
				if(BasicWindow.showConfirmDialog("Do you really want to remove the state "+cState.getSelectedItem()+"?"))
					{
					EvHardwareConfigGroup.getConfigGroup(groupName).removeState((String)cState.getSelectedItem());
					BasicWindow.updateWindows();
					}
				}
			else if(e.getSource()==cState)
				{
				String stateName=(String)cState.getSelectedItem();
				lastComboSetting.put(groupName, stateName);
				EvHardwareConfigGroup.getConfigGroup(groupName).getState(stateName).activate();
				}
			
			
			}
		
		}

	

	
	
	public OldConfigGroupPanel()
		{
		makeLayout();

		bAddGroup.addActionListener(this);
		
		setBorder(BorderFactory.createTitledBorder("Meta states"));
		setLayout(new BorderLayout());
		add(pGroups,BorderLayout.CENTER);
		add(bAddGroup,BorderLayout.WEST);
		}

	
	private void makeLayout()
		{
		int numGroups=EvHardwareConfigGroup.getConfigGroups().size();
		pGroups.removeAll();
		pGroups.setLayout(new GridLayout(numGroups,1));
		for(String groupName:EvHardwareConfigGroup.getConfigGroups().keySet())
			{
			StatesPanel p=new StatesPanel(groupName);
			pGroups.add(p);
			}
		revalidate();
		}



	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bAddGroup)
			{
			new NewConfigGroupWindow();
			}
		
		
		}




	public void dataChangedEvent()
		{
		makeLayout();
		
		}

	}
