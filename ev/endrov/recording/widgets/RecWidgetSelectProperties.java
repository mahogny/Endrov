package endrov.recording.widgets;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import endrov.gui.EvSwingUtil;
import endrov.hardware.EvDevicePath;
import endrov.hardware.EvDevicePropPath;
import endrov.hardware.EvHardware;


/**
 * Widget for selecting properties
 * 
 * @author Johan Henriksson
 *
 */
public class RecWidgetSelectProperties extends JPanel implements ActionListener
	{
	private static final long serialVersionUID = 1L;


	/**
	 * List model corresponding to a set
	 * @author Johan Henriksson
	 *
	 */
	private class ListModelPropSet implements ListModel//<EvDevicePropPath>
		{
		private Set<EvDevicePropPath> set;
		private List<EvDevicePropPath> list=new ArrayList<EvDevicePropPath>();
		public List<ListDataListener> listeners=new LinkedList<ListDataListener>(); 

		public ListModelPropSet(Set<EvDevicePropPath> set)
			{
			this.set=set;
			list.addAll(set);
			}
		
		public void addListDataListener(ListDataListener l)
			{
			listeners.add(l);
			}
	
		public EvDevicePropPath getElementAt(int index)
			{
			return list.get(index);
			}
	
		public int getSize()
			{
			return list.size();
			}
	
		public void removeListDataListener(ListDataListener l)
			{
			listeners.remove(l);
			}
		
		public void updateList()
			{
			list.clear();
			list.addAll(set);
			for(ListDataListener l:listeners)
				l.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, list.size()));
			repaint();
			}
		}
	

	private TreeSet<EvDevicePropPath> setAvail=new TreeSet<EvDevicePropPath>(); 
	private TreeSet<EvDevicePropPath> setUse=new TreeSet<EvDevicePropPath>();
	private ListModelPropSet listModelAvail=new ListModelPropSet(setAvail);
	private ListModelPropSet listModelUse=new ListModelPropSet(setUse);
	private JList listAvail=new JList/*<EvDevicePropPath>*/(listModelAvail);
	private JList listUse=new JList/*<EvDevicePropPath>*/(listModelUse);

	private JButton bAdd=new JButton("Add>");
	private JButton bRemove=new JButton("<Remove");


	/**
	 * Create window
	 */
	public RecWidgetSelectProperties()
		{
		for(EvDevicePath p:EvHardware.getDeviceList())
			for(String propName:p.getDevice().getPropertyMap().keySet())
				setAvail.add(new EvDevicePropPath(p, propName));
		listModelAvail.updateList();
		
		bAdd.addActionListener(this);
		bRemove.addActionListener(this);


		listAvail.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		listUse.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		JScrollPane scrollAvail=new JScrollPane(listAvail, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollPane scrollUse=new JScrollPane(listUse, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		//JPanel midPanel=new JPanel(new GridBagLayout());
		setLayout(new GridBagLayout());
		GridBagConstraints c=new GridBagConstraints();
		c.fill=GridBagConstraints.BOTH;
		c.weightx=1;
		c.weighty=1;
		c.gridx=0;
		add(EvSwingUtil.layoutACB(new JLabel("Available"),scrollAvail,null),c);
		
		c.fill=0;
		c.weightx=0;
		c.gridx=1;
		add(EvSwingUtil.layoutCompactVertical(
				bAdd,
				bRemove),c);
		
		c.fill=GridBagConstraints.BOTH;
		c.weightx=1;
		c.gridx=2;
		add(EvSwingUtil.layoutACB(new JLabel("To be used"),scrollUse,null),c);
		}


	/**
	 * Handle button presses
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bAdd)
			{
			LinkedList<EvDevicePropPath> obs=new LinkedList<EvDevicePropPath>();
			for(int i:listAvail.getSelectedIndices())
				obs.add((EvDevicePropPath)listAvail.getModel().getElementAt(i));
			for(EvDevicePropPath o:obs)
				{
				listModelAvail.set.remove(o);
				listModelUse.set.add((EvDevicePropPath)o);
				}
			listModelAvail.updateList();
			listModelUse.updateList();
			}
		else if(e.getSource()==bRemove)
			{
			LinkedList<EvDevicePropPath> obs=new LinkedList<EvDevicePropPath>();
			for(int i:listUse.getSelectedIndices())
				obs.add((EvDevicePropPath)listUse.getModel().getElementAt(i));
			for(EvDevicePropPath o:obs)
				{
				listModelAvail.set.add((EvDevicePropPath)o);
				listModelUse.set.remove(o);
				}
			listModelAvail.updateList();
			listModelUse.updateList();
			}
		}

	
	public Set<EvDevicePropPath> getSelectedProperties()
		{
		return new TreeSet<EvDevicePropPath>(setUse);
		}
	}
