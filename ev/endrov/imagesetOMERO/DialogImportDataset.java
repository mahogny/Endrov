/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imagesetOMERO;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import omero.ServerError;

import pojos.DatasetData;

import endrov.basicWindow.BasicWindow;
import endrov.data.EvData;
import endrov.util.EvSwingUtil;


import java.util.Set;
import java.util.Vector;

/**
 * A dialog for connecting to a OMERO database
 * @author Johan Henriksson
 */
public class DialogImportDataset extends JFrame implements ActionListener
	{
	static final long serialVersionUID=0; 
	
	private JComboBox combo=new JComboBox();
	private JButton bOk=new JButton("OK"); 
	private JButton bCancel=new JButton("Cancel");
	
	private static class ComboDataset
		{
		DatasetData d;
		
		@Override
		public String toString()
			{
			return d.getName();
			}

		public ComboDataset(DatasetData d)
			{
			this.d = d;
			}
		
		
		}
		
	public DialogImportDataset() throws ServerError
		{
		setTitle("Import OMERO dataset");
		setLayout(new BorderLayout());
		
		
		OMEROConnection connection=OMEROBasic.omesession;
		Set<DatasetData> datasets=connection.getDatasetsForUser(connection.getMyUserId());
		Vector<ComboDataset> items=new Vector<ComboDataset>();
		for(DatasetData d:datasets)
			items.add(new ComboDataset(d));
		
		combo=new JComboBox(items);
		
		setLayout(new GridLayout(1,1));
		add(EvSwingUtil.layoutEvenVertical(
				combo,
				EvSwingUtil.layoutEvenHorizontal(bOk, bCancel)
			));
	
		bOk.addActionListener(this);
		bCancel.addActionListener(this);
	
		pack();
		setLocationRelativeTo(null);
	
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setVisible(true);
		}
	
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bOk)
			{
			ComboDataset item=(ComboDataset)combo.getSelectedItem();
			if(item!=null)
				{
				System.out.println("Get "+item.d.getName());
				
				EvData data=new EvData();
				new EvIODataOMERO(OMEROBasic.omesession,data);
				
				EvData.registerOpenedData(data);
				BasicWindow.updateWindows();
				
				
				}
			}
		else if(e.getSource()==bCancel) //Cancel
			{
			dispose();
			}
		}

	
	
	
	}
