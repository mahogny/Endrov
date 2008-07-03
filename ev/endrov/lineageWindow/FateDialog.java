package endrov.lineageWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import endrov.basicWindow.BasicWindow;
import endrov.ev.Tuple;
import endrov.nuc.*;

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
	
	public FateDialog(Frame owner, Tuple<NucLineage,String> sel)
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
			NucLineage.Nuc n=lin.nuc.get(nucName);
			if(n!=null)
				n.fate=(String)cFate.getSelectedItem();
			BasicWindow.updateWindows();
			}
		dispose();
		}
	
	
	}
