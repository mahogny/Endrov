package endrov.recording.propertyWindow;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

import endrov.gui.window.BasicWindow;
import endrov.hardware.EvHardwareConfigGroup;
import endrov.recording.widgets.RecWidgetSelectProperties;
import endrov.util.EvSwingUtil;

/**
 * Dialog for creating a new config group - have to specify properties to include
 * 
 * @author Johan Henriksson
 *
 */
public class NewConfigGroupWindow extends JFrame implements ActionListener
	{
	private static final long serialVersionUID = 1L;

	
	RecWidgetSelectProperties wProperties=new RecWidgetSelectProperties();

	private JButton bOk=new JButton("OK");
	private JButton bCancel=new JButton("Cancel");

	private JTextField tName=new JTextField("");

	/**
	 * Create window
	 */
	public NewConfigGroupWindow()
		{
		
		bOk.addActionListener(this);
		bCancel.addActionListener(this);
		
		setLayout(new BorderLayout());
		add(EvSwingUtil.withLabel("Name: ", tName), BorderLayout.NORTH);
		add(wProperties, BorderLayout.CENTER);
		add(EvSwingUtil.layoutEvenHorizontal(bOk, bCancel),
				BorderLayout.SOUTH);
		pack();
		setVisible(true);
		}


	/**
	 * Handle button presses
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bOk)
			{
			String name=tName.getText();
			if(!name.equals(""))
				{
				EvHardwareConfigGroup group=new EvHardwareConfigGroup();
				group.propsToInclude.addAll(wProperties.getSelectedProperties());
				
				EvHardwareConfigGroup.putConfigGroup(name, group);
				
				dispose();
				}
			else
				BasicWindow.showErrorDialog("No name specified for the group");			
			
			}
		else if(e.getSource()==bCancel)
			{
			dispose();
			}
		
		}
	

	}
