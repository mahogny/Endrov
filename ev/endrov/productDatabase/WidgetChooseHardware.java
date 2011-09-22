/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.productDatabase;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import endrov.util.EvSwingUtil;

/**
 * Swing widget to choose hardware from database, and enter additional info
 * @author Johan Henriksson
 *
 */
public class WidgetChooseHardware extends JPanel
	{
	private static final long serialVersionUID = 1L;
	private JComboBox cManufacturer;
	private JComboBox cProduct=new JComboBox();
	private JTextField fSerial=new JTextField();
	private JTextField fLot=new JTextField();
	//JTextField fSerial=new JTextField();
	

	public WidgetChooseHardware()
		{
		TreeSet<String> manufacturers=new TreeSet<String>();
		manufacturers.add("");
		for(HardwareMetadata e:HardwareDatabase.entries)
			manufacturers.add(e.getManufacturer());

		cManufacturer=new JComboBox(new Vector<String>(manufacturers));
		cManufacturer.setSelectedItem("");
		addComponents();
		
		
		}
	
	public static class EntryWrapper implements Comparable<EntryWrapper>
		{
		public final HardwareMetadata meta;
		private final String desc;
		
		public EntryWrapper(HardwareMetadata meta)
			{
			this.meta=meta;
			desc=getDescFrom(meta);
			}
		
		public String toString()
			{
			return desc;
			}

		public int compareTo(EntryWrapper o)
			{
			return desc.compareTo(o.desc);
			}
		}

	private static String getDescFrom(HardwareMetadata meta)
		{
		if(meta.getModelNumber()!=null)
			return meta.getModelName()+" || "+meta.getModelNumber();
		else
			return meta.getModelName();
		}
	
	private void addComponents()
		{
		removeAll();
		String selManu=(String)cManufacturer.getSelectedItem();
		if(selManu!=null)
			{
			/*
			LinkedList<HardwareMetadata> list=new LinkedList<HardwareMetadata>();
			for(HardwareMetadata e:HardwareDatabase.entries)
				if(e.getManufacturer().equals(selManu))
					list.add(e);*/

			TreeSet<EntryWrapper> list=new TreeSet<EntryWrapper>();
			for(HardwareMetadata e:HardwareDatabase.entries)
				if(e.getManufacturer().equals(selManu))
					list.add(new EntryWrapper(e));
			
			cProduct=new JComboBox(new Vector<EntryWrapper>(list));
			//cProduct.setRenderer(new Renderer());
			}
		
		
		setLayout(new GridLayout(3,1));
		add(EvSwingUtil.layoutLCR(cManufacturer, cProduct, null));
		add(EvSwingUtil.withLabel("Serial#", fSerial));
		add(EvSwingUtil.withLabel("Lot#", fLot));
		
		
		
		cManufacturer.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){addComponents();}});
		
		revalidate();
		}
	
	
	
	public static void main(String[] args)
		{
		JFrame f=new JFrame();
		f.add(new WidgetChooseHardware());
		f.setSize(300, 100);
		f.setVisible(true);
		
		
		
		}
	
	}
