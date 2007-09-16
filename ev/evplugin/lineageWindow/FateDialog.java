package evplugin.lineageWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import evplugin.basicWindow.BasicWindow;
import evplugin.ev.Pair;
import evplugin.nuc.*;

/**
 * Dialog to edit fate for a nucleus
 * @author Johan Henriksson
 */
public class FateDialog extends JDialog implements ActionListener
	{
	public static final long serialVersionUID=1;

	private final NucLineage lin;
	private final String nucName;
	
	private Vector<String> fateList=new Vector<String>();
	private JComboBox cFate=new JComboBox(fateList);
	private JButton bOk=new JButton("Ok");
	private JButton bCancel=new JButton("Cancel");
	
	public FateDialog(Frame owner, Pair<NucLineage,String> sel)
		{
		super(owner, "Set fate for "+sel.getRight(), true);

		this.lin=sel.getLeft();
		this.nucName=sel.getRight();

		JPanel bp=new JPanel(new GridLayout(1,2));
		bp.add(bOk);
		bp.add(bCancel);
		
		setLayout(new BorderLayout());
		add(cFate,BorderLayout.CENTER);
		add(bp, BorderLayout.EAST);
		
		fateList.add("");
		fateList.add("Abberant");
		fateList.add("Cell Lost");
		fateList.add("Hypodermis");
		fateList.add("Muscle");
		fateList.add("Neuron");
		fateList.add("Pharynx");
		fateList.add("Intestine");
		fateList.add("Death");
		
		cFate.setEditable(true);
		NucLineage.Nuc n=lin.nuc.get(nucName);
		if(n!=null)
			cFate.setSelectedItem(n.fate);
		
		bOk.addActionListener(this);
		bCancel.addActionListener(this);
		
		pack();
		setBounds(100, 300, 500, getHeight());
		setVisible(true);
		}
	
	
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bOk)
			{
			NucLineage.Nuc n=lin.nuc.get(nucName);
			if(n!=null)
				n.fate=(String)cFate.getSelectedItem();
			BasicWindow.updateWindows();
			}
		dispose();
		}
	
	
	}
