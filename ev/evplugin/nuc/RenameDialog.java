package evplugin.nuc;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.*;

import evplugin.basicWindow.BasicWindow;
import evplugin.data.EvDataXML;
import evplugin.data.EvObject;

/**
 * Dialog to rename a nucleus
 * @author Johan Henriksson
 */
public class RenameDialog extends JDialog implements ActionListener
	{
	static final long serialVersionUID=0;

	public static NucLineage templateLineage=null;

	private final JComboBox inputName;
	private JButton bLoad=new JButton("Load template");
	private JButton bOk=new JButton("Ok");
	private JButton bCancel=new JButton("Cancel");
	private Frame frame;
	
	private final NucLineage oldLineage;
	private final String oldName;
	
	
	private RenameDialog(Frame frame, NucPair pair)
		{
		super(frame,"EV Rename Nucleus: "+pair.getRight(),false);
		this.frame=frame;
		oldLineage=pair.getLeft();
		oldName=pair.getRight();

		//Make ESC quit the window
		JRootPane rootPane = new JRootPane();
		rootPane.registerKeyboardAction(
				new ActionListener() {public void actionPerformed(ActionEvent actionEvent) {dispose();}},
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		setRootPane(rootPane);
		
		//String oldName=pair.getRight();
		inputName=new JComboBox();
		inputName.setEditable(true);
		inputName.setSelectedItem(oldName);

		fillCombo();
		
		JPanel up=new JPanel(new BorderLayout());
		up.add(new JLabel("New name:"),BorderLayout.WEST);
		up.add(inputName);
		
		JPanel bp=new JPanel(new GridLayout(1,3));
		bp.add(bOk);
		bp.add(bCancel);
		bp.add(bLoad);
		
		setLayout(new GridLayout(2,1));
		add(up);
		add(bp);

		bLoad.addActionListener(this);
		bOk.addActionListener(this);
		bCancel.addActionListener(this);
		
		pack();
		setLocationRelativeTo(frame);
		setVisible(true);
		}
	
	
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bLoad)
			{
			EvDataXML data=EvDataXML.loadReturnMeta();
			if(data!=null)
				{
				for(EvObject ob:data.metaObject.values())
					if(ob instanceof NucLineage)
						{
						templateLineage=(NucLineage)ob;
						fillCombo();
						return;
						}
				}
			}
		else if(e.getSource()==bOk)
			{
			String newName=(String)inputName.getSelectedItem();
			if(!newName.equals(""))
				{
				if(oldLineage.nuc.containsKey(newName))
					JOptionPane.showMessageDialog(frame, "Nucleus already exists");
				else
					{
					oldLineage.renameNucleus(oldName, newName);
					BasicWindow.updateWindows();
					}
				dispose();
				}
			}
		else if(e.getSource()==bCancel)
			dispose();
		}



	/**
	 * Fill in list of alternatives
	 */
	private void fillCombo()
		{
		String current=(String)inputName.getSelectedItem();
		NucLineage.Nuc parent=oldLineage.nuc.get(oldName);
		inputName.removeAllItems();
		if(parent!=null && templateLineage!=null)
			{
			parent=templateLineage.nuc.get(parent.parent);
			if(parent!=null)
				for(String s:parent.child)
					inputName.addItem(s);
			}
		inputName.setSelectedItem(current);
		}

	/**
	 * Create rename dialog if possible
	 * @param caller Window that calls or null
	 */
	public static void run(Frame caller)
		{
		if(NucLineage.selectedNuclei.size()==1) 
			{
			NucPair nucPair=NucLineage.selectedNuclei.iterator().next();
			if(nucPair!=null)
				new RenameDialog(caller,nucPair);
			}
		else
			JOptionPane.showMessageDialog(caller, "Select 1 nucleus");
		}
	
	
	}
