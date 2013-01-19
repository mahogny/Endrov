/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.ioOMERO;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import omero.ServerError;

import pojos.DatasetData;

import endrov.data.EvData;
import endrov.gui.EvSwingUtil;
import endrov.gui.window.EvBasicWindow;


import java.util.Set;
import java.util.Vector;

/**
 * A dialog for connecting to a OMERO database
 * @author Johan Henriksson
 */
public class DialogImportDataset extends JFrame implements ActionListener
	{
	static final long serialVersionUID=0; 
	
	private JComboBox comboUser=new JComboBox();
	private JComboBox comboDataset=new JComboBox();
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
		
	
	private static class ComboItemUser
		{
		long id;
		OMEROConnection connection;
		
		
		public ComboItemUser(long id, OMEROConnection connection)
			{
			this.id = id;
			this.connection = connection;
			}


		@Override
		public String toString()
			{
			try
				{
				return connection.getEntry().getAdminService().getExperimenter(id).getFirstName().getValue();
				}
			catch (ServerError e)
				{
				return "<err>";
				}
			}
		}
	
	
	public DialogImportDataset() throws ServerError
		{
		setTitle("Import OMERO dataset");
		setLayout(new BorderLayout());
		
		OMEROConnection connection=OMEROBasic.omesession;
		/*
		Set<DatasetData> datasets=connection.getDatasetsForUser(connection.getMyUserId());
		Vector<ComboDataset> itemsDataset=new Vector<ComboDataset>();
		for(DatasetData d:datasets)
			itemsDataset.add(new ComboDataset(d));
		*/
		
		Vector<ComboItemUser> itemsUsers=new Vector<ComboItemUser>();
		for(long uid:connection.getUserIDs())
			itemsUsers.add(new ComboItemUser(uid, connection));
		
		comboUser=new JComboBox(itemsUsers);
		//comboDataset=new JComboBox(itemsDataset);
		
		
		setLayout(new GridLayout(1,1));
		add(EvSwingUtil.layoutEvenVertical(
				comboUser,
				comboDataset,
				EvSwingUtil.layoutEvenHorizontal(bOk, bCancel)
			));
	
		bOk.addActionListener(this);
		bCancel.addActionListener(this);
		comboUser.addActionListener(this);
		comboDataset.addActionListener(this);

		fillDatasetCombo();

		pack();
		setLocationRelativeTo(null);
	
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setVisible(true);
		}
	
	
	private void fillDatasetCombo()
		{
		comboDataset.removeActionListener(this);
		
		DefaultComboBoxModel model=(DefaultComboBoxModel)comboDataset.getModel();
		model.removeAllElements();
		ComboItemUser seluser=(ComboItemUser)comboUser.getSelectedItem();
		if(seluser!=null)
			{
			try
				{
				OMEROConnection connection=OMEROBasic.omesession;
				Set<DatasetData> datasets=connection.getDatasetsForUser(seluser.id);
				//Vector<ComboDataset> itemsDataset=new Vector<ComboDataset>();
				for(DatasetData d:datasets)
					model.addElement(new ComboDataset(d));
				}
			catch (ServerError e)
				{
				e.printStackTrace();
				}
			}
		comboDataset.addActionListener(this);
		}
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==comboUser)
			{
			fillDatasetCombo();
			}
		else if(e.getSource()==bOk || e.getSource()==comboDataset)
			{
			dispose();
			ComboDataset item=(ComboDataset)comboDataset.getSelectedItem();
			if(item!=null)
				{
				System.out.println("Get "+item.d.getName());
				
				EvData data=new EvData();
				new EvIODataOMERO(OMEROBasic.omesession,data);
				
				EvData.registerOpenedData(data);
				EvBasicWindow.updateWindows();
				

				}
			}
		else if(e.getSource()==bCancel) //Cancel
			{
			dispose();
			}
		}

	
	
	
	}
