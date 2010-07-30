/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.widgets;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import endrov.data.EvPath;
import endrov.data.tree.DataTree;
import endrov.util.EvSwingUtil;

/**
 * Widget for recording settings: Position settings
 * @author Johan Henriksson
 *
 */
public class RecWidgetPositions extends JPanel implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	
	JCheckBox cbAutofocus=new JCheckBox("Autofocus");
	//which device here
	//MM: switch of hw autofocus while moving xy
	//MM: switch of hw autofocus while moving z
	
	private JCheckBox cUseGrid=new JCheckBox("Use grid");
	private JButton bSetGrid=new JButton("Configure");
	
	private List<EvPath> roiList=new ArrayList<EvPath>();
	
	private DataTree dataTree=new DataTree();
	
	private JButton bAdd=new JButton("Add>");
	private JButton bRemove=new JButton("<Remove");

	
	/**
	 * GUI Model for the list of added ROIs
	 */
	private class ListModelROI implements ListModel
		{
		public List<ListDataListener> listeners=new LinkedList<ListDataListener>(); 
		
		public void addListDataListener(ListDataListener l)
			{
			listeners.add(l);
			}
	
		public Object getElementAt(int index)
			{
			return roiList.get(index);
			}
	
		public int getSize()
			{
			return roiList.size();
			}
	
		public void removeListDataListener(ListDataListener l)
			{
			listeners.remove(l);
			}
		}
	
	private ListModelROI listModelAdded=new ListModelROI();
	private JList listUseROIs=new JList(listModelAdded);
	
	
	public RecWidgetPositions()
		{
		bAdd.addActionListener(this);
		bRemove.addActionListener(this);
		bSetGrid.addActionListener(this);
		
		cbAutofocus.setToolTipText("Autofocus for each position the first time");
		cUseGrid.setToolTipText("Take positions from a grid (typical for stitching)");

		JPanel roiPanel=new JPanel(new GridBagLayout());
		GridBagConstraints c=new GridBagConstraints();
		c.fill=GridBagConstraints.BOTH;
		c.weightx=1;
		c.gridx=0;
		roiPanel.add(dataTree,c);
		
		c.weightx=0;
		c.gridx=1;
		roiPanel.add(EvSwingUtil.layoutCompactVertical(
				new JLabel("ROIs"),
				bAdd,
				bRemove),c);
		
		c.weightx=1;
		c.gridx=2;
		roiPanel.add(listUseROIs,c);
		
		setLayout(new GridLayout(1,1));
		add(
			EvSwingUtil.withTitledBorder("Positions",
					EvSwingUtil.layoutCompactVertical(
							
							EvSwingUtil.layoutCompactHorizontal(
									cbAutofocus,
									EvSwingUtil.layoutFlow(cUseGrid, bSetGrid)
									),
					
							roiPanel
					)
			));
		
		
		
		}


	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bAdd)
			{
			//Add new path to list of selected ROIs
			Set<EvPath> paths=new TreeSet<EvPath>();
			paths.addAll(roiList);
			for(EvPath p:dataTree.getSelectedPaths())
				paths.add(p);
			roiList.clear();
			roiList.addAll(paths);
			System.out.println(roiList);
			for(ListDataListener l:listModelAdded.listeners)
				l.contentsChanged(new ListDataEvent(listModelAdded, ListDataEvent.CONTENTS_CHANGED, 0, roiList.size()));
			listUseROIs.repaint();
			}
		else if(e.getSource()==bRemove)
			{
			
			}
		else if(e.getSource()==bSetGrid)
			{
			
			}
		
		}
	}
