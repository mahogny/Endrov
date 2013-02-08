/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeLineage;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.*;
import java.util.Collection;
import java.util.List;

import javax.swing.*;
import javax.swing.text.JTextComponent;

import endrov.data.EvData;
import endrov.data.gui.GuiEvDataIO;
import endrov.gui.window.EvBasicWindow;


/**
 * Dialog to rename a particle
 * @author Johan Henriksson
 */
public class LineageRenameParticleDialog extends JDialog implements ActionListener
	{
	static final long serialVersionUID=0;

	public static Lineage templateLineage=null;

	private final JComboBox<String> inputName=new JComboBox<String>();
	private final JButton bLoad=new JButton("Load template");
	private final JButton bOk=new JButton("Ok");
	private final JButton bCancel=new JButton("Cancel");
	private final Frame frame;
	
	private final Lineage oldLineage;
	private final String oldName;
	
	
	private LineageRenameParticleDialog(Frame frame, LineageSelParticle pair)
		{
		super(frame,"EV Rename Particle: "+pair.snd(),false);
		this.frame=frame;
		oldLineage=pair.fst();
		oldName=pair.snd();

		//Make ESC quit the window
		JRootPane rootPane = new JRootPane();
		rootPane.registerKeyboardAction(
				new ActionListener() {public void actionPerformed(ActionEvent actionEvent) {dispose();}},
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		setRootPane(rootPane);
		
		inputName.setEditable(true);
		inputName.setSelectedItem(oldName);

		fillCombo();

		//Focus and select current text
		inputName.requestFocusInWindow();
		JTextComponent editor = (JTextComponent)inputName.getEditor().getEditorComponent();
		editor.setSelectionStart(0);
		editor.setSelectionEnd(editor.getText().length());

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
			EvData data=GuiEvDataIO.loadFileDialog();
			if(data!=null)
				{
				List<Lineage> lins=data.getObjects(Lineage.class);
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
		final String newName=(String)inputName.getSelectedItem();
		if(!newName.equals(""))
			{
			if(oldLineage.particle.containsKey(newName))
				JOptionPane.showMessageDialog(frame, "Particle already exists");
			else
				{
				new UndoOpLineageReplaceAllParticle("Rename particle",oldLineage)
					{
					public void redo()
						{
						oldLineage.renameParticles(oldName, newName);
						}
					}.execute();
				
				dispose();
				EvBasicWindow.updateWindows();
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
		Lineage.Particle parent=oldLineage.particle.get(oldName);
		inputName.removeAllItems();
		if(parent!=null && templateLineage!=null)
			{
			if(!parent.parents.isEmpty())
				{
				parent=templateLineage.particle.get(parent.parents.iterator().next());
				if(parent!=null)
					for(String s:parent.child)
						inputName.addItem(s);
				}
			}
		inputName.setSelectedItem(current);
		inputName.addActionListener(this);
		}

	/**
	 * Create rename dialog if possible
	 * @param caller Window that calls or null
	 */
	public static void run(Collection<LineageSelParticle> nucs, Frame caller)
		{
		if(nucs.size()==1) 
			{
			LineageSelParticle nucPair=nucs.iterator().next();
			if(nucPair!=null)
				new LineageRenameParticleDialog(caller,nucPair);
			}
		else
			JOptionPane.showMessageDialog(caller, "Select 1 particle");
		}
	
	
	}
