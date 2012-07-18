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
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import endrov.recording.RecordingResource;
import endrov.recording.RecordingResource.PositionListListener;
import endrov.recording.positionsWindow.Position;
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
	
	private JCheckBox cUseGrid=new JCheckBox("Use grid");
	private JButton bSetGrid=new JButton("Configure");
	
	
	private JButton bAdd=new JButton("Add>");
	private JButton bRemove=new JButton("<Remove");

	
	
	private DefaultListModel listModel=new DefaultListModel();
	private JList posList=new JList(listModel);
	private JScrollPane listScroller = new JScrollPane(posList);
	
	private DefaultListModel listModelAdded=new DefaultListModel();
	private JList posListAdded=new JList(listModelAdded);
	private JScrollPane listScrollerAdded = new JScrollPane(posListAdded);
	
	
	public RecSettingsPositions getSettings()
		{
		RecSettingsPositions settings=new RecSettingsPositions(getPositions());
		return settings;
		}
	
	public RecWidgetPositions()
		{
		bAdd.addActionListener(this);
		bRemove.addActionListener(this);
		bSetGrid.addActionListener(this);
		
		RecordingResource.posListListeners.addWeakListener(this);

		for(Position pos:RecordingResource.posList){
			listModel.addElement(pos);
		}
		
		cbAutofocus.setToolTipText("Autofocus for each position the first time");
		cUseGrid.setToolTipText("Take positions from a grid (typical for stitching)");

		JPanel roiPanel=new JPanel(new GridBagLayout());
		GridBagConstraints c=new GridBagConstraints();
		c.fill=GridBagConstraints.BOTH;
		c.weightx=1;
		c.gridx=0;
		roiPanel.add(listScroller,c);
		
		c.weightx=0;
		c.gridx=1;
		roiPanel.add(EvSwingUtil.layoutCompactVertical(
				//new JLabel("ROIs"),
				bAdd,
				bRemove),c);
		
		c.weightx=1;
		c.gridx=2;
		//roiPanel.add(listUseROIs,c);
		roiPanel.add(listScrollerAdded,c);
		
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
	//			posList.getSelectedIndices()
			
				int index = posList.getSelectedIndex();
				if(index >=0){
					listModelAdded.addElement(listModel.get(index));
					listModel.remove(index);
				}
			}
		else if(e.getSource()==bRemove)
			{
				int index = posListAdded.getSelectedIndex();
				if(index >=0){
					listModel.addElement(listModelAdded.get(index));
					listModelAdded.remove(index);
				}
			}
		else if(e.getSource()==bSetGrid)
			{
			
			}
		
		}
	
	
	public void dataChangedEvent()
		{
		//TODO
		//roiList.
		}

	public void positionsUpdated() {
		
		boolean found = false;
		if(RecordingResource.posList.size() > listModel.getSize()+listModelAdded.getSize()){
			listModel.addElement(RecordingResource.posList.get(RecordingResource.posList.size() -1 ));
		}else if(RecordingResource.posList.size() < listModel.getSize()+listModelAdded.getSize()){
			for(int i = 0; i < listModel.getSize();i++){
				found = false;
				for(Position p:RecordingResource.posList){
					if(listModel.get(i).equals(p)){
						found = true;
					}
				}
				if(!found){
					listModel.remove(i);
				}
			}
			for(int j = 0; j < listModelAdded.getSize(); j++){
				found = false;
				for(Position p:RecordingResource.posList){
					if(listModelAdded.get(j).equals(p)){
						found = true;
					}
				}
				if(!found){
					listModelAdded.remove(j);
				}
			}
		}

		
	}
	
	public LinkedList<Position> getPositions(){
		List<Position> positions = new LinkedList<Position>();
		for(int i = 0; i< listModelAdded.size(); i++){
			positions.add((Position) listModelAdded.get(i));
		}
			
		return (LinkedList<Position>) positions;
	}
	
}
