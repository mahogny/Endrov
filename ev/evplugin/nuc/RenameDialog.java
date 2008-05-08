package evplugin.nuc;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.*;
import java.util.List;

import javax.swing.*;

import evplugin.basicWindow.BasicWindow;
import evplugin.data.EvData;


/**
 * Dialog to rename a nucleus
 * @author Johan Henriksson
 */
public class RenameDialog extends JDialog implements ActionListener
	{
	static final long serialVersionUID=0;

	public static NucLineage templateLineage=null;

	private final JComboBox inputName=new JComboBox();
	private final JButton bLoad=new JButton("Load template");
	private final JButton bOk=new JButton("Ok");
	private final JButton bCancel=new JButton("Cancel");
	private final Frame frame;
	
	private final NucLineage oldLineage;
	private final String oldName;
	
	
	private RenameDialog(Frame frame, NucPair pair)
		{
		super(frame,"EV Rename Nucleus: "+pair.snd(),false);
		this.frame=frame;
		oldLineage=pair.fst();
		oldName=pair.snd();

		//Make ESC quit the window
		JRootPane rootPane = new JRootPane();
		rootPane.registerKeyboardAction(
				new ActionListener() {public void actionPerformed(ActionEvent actionEvent) {dispose();}},
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		setRootPane(rootPane);
		
		//String oldName=pair.getRight();
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
	
	private Timer timer=null; //Hate.
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bLoad)
			{
			timer=null;
			EvData data=EvData.loadFileDialog(null);
			if(data!=null)
				{
				List<NucLineage> lins=data.getObjects(NucLineage.class);
				if(!lins.isEmpty())
					{
					templateLineage=lins.get(0);
					fillCombo();
					return;
					}
				}
			}
		else if(e.getSource()==inputName && e.getActionCommand().equals("comboBoxChanged"))
			{
			timer=new Timer(100,this);
			timer.setRepeats(false);
			timer.start();
			}
		else if(e.getSource()==bOk || e.getSource()==timer)
			clickOk();
		else if(e.getSource()==bCancel)
			{
			timer=null;
			dispose();
			}
		}


	private void clickOk()
		{
		timer=null;
		String newName=(String)inputName.getSelectedItem();
		if(!newName.equals(""))
			{
			if(oldLineage.nuc.containsKey(newName))
				JOptionPane.showMessageDialog(frame, "Nucleus already exists");
			else
				{
				oldLineage.renameNucleus(oldName, newName);
				dispose();
				BasicWindow.updateWindows();
				}
			}
		}			
	

	/**
	 * Fill in list of alternatives
	 */
	private void fillCombo()
		{
		inputName.removeActionListener(this);
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
		inputName.addActionListener(this);
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
