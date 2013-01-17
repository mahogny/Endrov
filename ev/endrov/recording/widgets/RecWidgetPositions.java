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
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import endrov.recording.RecordingResource;
import endrov.recording.StoredStagePosition;
import endrov.recording.RecordingResource.PositionListListener;
import endrov.util.EvSwingUtil;

/**
 * Widget for recording settings: Position settings
 * @author Johan Henriksson
 *
 */
public class RecWidgetPositions extends JPanel implements ActionListener, PositionListListener
	{
	private static final long serialVersionUID = 1L;
	
	JCheckBox cbAutofocus=new JCheckBox("Use Autofocus");
	//which device here
	//MM: switch of hw autofocus while moving xy
	//MM: switch of hw autofocus while moving z
	
	
	
	private JButton bAdd=new JButton("Add>");
	private JButton bRemove=new JButton("<Remove");

	
	
	private DefaultListModel listModelAvailable=new DefaultListModel();
	private JList posListAvailable=new JList(listModelAvailable);
	private JScrollPane listScroller = new JScrollPane(posListAvailable);
	
	private DefaultListModel listModelAdded=new DefaultListModel();
	private JList posListAdded=new JList(listModelAdded);
	private JScrollPane listScrollerAdded = new JScrollPane(posListAdded);
	
	
	public RecSettingsPositions getSettings()
		{
		RecSettingsPositions settings=new RecSettingsPositions(getPositions(),cbAutofocus.isSelected());
		return settings;
		}
	
	public RecWidgetPositions()
		{
		bAdd.addActionListener(this);
		bRemove.addActionListener(this);
		
		RecordingResource.posListListeners.addWeakListener(this);

		positionsUpdated();
		
		cbAutofocus.setToolTipText("Autofocus for each position the first time");

		JPanel roiPanel=new JPanel(new GridBagLayout());
		GridBagConstraints c=new GridBagConstraints();
		c.fill=GridBagConstraints.BOTH;
		c.weighty=1;
		c.weightx=1;
		c.gridx=0;
		roiPanel.add(listScroller,c);
		
		c.weightx=0;
		c.gridx=1;
		roiPanel.add(EvSwingUtil.layoutMidVertical(
				EvSwingUtil.layoutEvenVertical(bAdd,bRemove)
				),c);
		
		c.weightx=1;
		c.gridx=2;
		roiPanel.add(listScrollerAdded,c);

		
		setLayout(new GridLayout(1,1));
		add(
			EvSwingUtil.withTitledBorder("Positions",
					EvSwingUtil.layoutACB(
							cbAutofocus,
							roiPanel,
							null)
			));
		
		
	
		
		}


	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bAdd)
			{
			Object[] got=posListAvailable.getSelectedValues();
			for(Object o:got)
				{
				listModelAvailable.removeElement(o);
				listModelAdded.addElement(o);
				positionsUpdated(); //TODO Ugly way
				}
			}
		else if(e.getSource()==bRemove)
			{
			Object[] got=posListAdded.getSelectedValues();
			for(Object o:got)
				{
				listModelAdded.removeElement(o);
				listModelAvailable.addElement(o);
				positionsUpdated(); //TODO Ugly way
				}
			}
		}
	
	
	public void dataChangedEvent()
		{
		}

	public void positionsUpdated()
		{
		HashSet<Object> lastAddedObjects=new HashSet<Object>();
		for(int i=0;i<listModelAdded.getSize();i++)
			lastAddedObjects.add(listModelAdded.get(i));

		listModelAdded.clear();
		listModelAvailable.clear();
		
		for(StoredStagePosition pos:RecordingResource.posList)
			if(lastAddedObjects.contains(pos))
				listModelAdded.addElement(pos);
			else
				listModelAvailable.addElement(pos);
		}
	
	public LinkedList<StoredStagePosition> getPositions()
		{
		LinkedList<StoredStagePosition> positions = new LinkedList<StoredStagePosition>();
		for (int i = 0; i<listModelAdded.size(); i++)
			positions.add((StoredStagePosition) listModelAdded.get(i));
		return positions;
		}
	
}
