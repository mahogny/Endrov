/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.nuc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import endrov.basicWindow.BasicWindow;

/**
 * Dialog to edit fate for a nucleus
 * @author Johan Henriksson
 */
public class FateDialog extends JDialog implements ActionListener
	{
	public static final long serialVersionUID=1;

	private final NucLineage lin;
	private final String nucName;
	
	private final Vector<String> fateList=new Vector<String>();
	private final JComboBox cFate=new JComboBox(fateList);
	private final JButton bOk=new JButton("Ok");
	private final JButton bCancel=new JButton("Cancel");
	
	public FateDialog(Frame owner, NucSel sel)
		{
		super(owner, "Set fate for "+sel.snd(), true);

		this.lin=sel.fst();
		this.nucName=sel.snd();

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
			new UndoOpReplaceSomeNuclei("Set fate")
				{
				public void redo()
					{
					NucLineage.Nuc n=lin.nuc.get(nucName);
					if(n!=null)
						n.fate=(String)cFate.getSelectedItem();
					BasicWindow.updateWindows();
					}
				};
			}
		dispose();
		}
	
	
	}
