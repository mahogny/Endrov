/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.positionsWindow;

import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import java.util.HashMap;
import java.util.LinkedList;

import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JButton;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import javax.swing.ListSelectionModel;

import endrov.recording.RecordingResource;
import endrov.recording.RecordingResource.PositionListListener;
import endrov.util.EvSwingUtil;

/**
 * Widget used by PositionsWindow to handle positions
 * 
 * @author Kim Nordlöf, Erik Vernersson
 */
public class WidgetPositions extends JPanel implements ActionListener,
		PositionListListener
	{
	private static final long serialVersionUID = 1L;

	// which device here
	// MM: switch of hw autofocus while moving xy
	// MM: switch of hw autofocus while moving z

	private JList posList;
	private DefaultListModel listModel;
	private JScrollPane listScroller;

	private CheckBoxList infoList;
	private JScrollPane infoScroller;

	private JButton bAdd = new JButton("Add");
	private JButton bRemove = new JButton("Remove");
	private JButton bGoTo = new JButton("Go To Position");
	private JButton bMoveUp = new JButton("Move Up");
	private JButton bMoveDown = new JButton("Move Down");
	private JButton bSave = new JButton("Save Positions");
	private JButton bLoad = new JButton("Load Positions");

	private JButton bGoToHome = new JButton("Go to home position");

	public WidgetPositions()
		{

		bAdd.addActionListener(this);
		bRemove.addActionListener(this);
		bGoTo.addActionListener(this);
		bMoveUp.addActionListener(this);
		bMoveDown.addActionListener(this);
		bSave.addActionListener(this);
		bLoad.addActionListener(this);

		bGoToHome.addActionListener(this);

		JPanel bPanel = new JPanel();
		JPanel posPanel = new JPanel(new BorderLayout());
		this.setLayout(new BorderLayout());

		bPanel.add(EvSwingUtil.layoutCompactVertical(bAdd, bRemove, bGoTo, bMoveUp,
				bMoveDown, bSave, bLoad, bGoToHome));

		listModel = new DefaultListModel();
		infoList = new CheckBoxList();

		posList = new JList(listModel);
		posList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		posList.setLayoutOrientation(JList.VERTICAL);

		listScroller = new JScrollPane(posList);
		posPanel.add(listScroller, BorderLayout.NORTH);

		infoScroller = new JScrollPane(infoList);

		posPanel.add(infoScroller, BorderLayout.CENTER);

		add(EvSwingUtil.withTitledBorder("Positions", posPanel),
				BorderLayout.CENTER);
		add(EvSwingUtil.withTitledBorder("", bPanel), BorderLayout.WEST);

		RecordingResource.posListListeners.addWeakListener(this);

		RecordingResource.posListUpdated();

		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource()==bAdd)
			{
			AxisInfo[] newInfo = new AxisInfo[infoList.getInfo().length];
			for (int i = 0; i<infoList.getInfo().length; i++)
				{
				newInfo[i] = new AxisInfo(infoList.getInfo()[i].getDevice(),
						infoList.getInfo()[i].getAxis(), infoList.getInfo()[i].getDevice()
								.getStagePos()[infoList.getInfo()[i].getAxis()]);
				}

			Position newPos = new Position(newInfo,
					RecordingResource.getUnusedPosName());
			RecordingResource.posList.add(newPos);

			RecordingResource.posListUpdated();

			}
		else if (e.getSource()==bRemove)
			{
			int index = posList.getSelectedIndex();
			if (index>=0)
				{
				RecordingResource.posList.remove(index);
				RecordingResource.posListUpdated();
				}
			}
		else if (e.getSource()==bGoTo)
			{
			int index = posList.getSelectedIndex();
			if (index>=0)
				{
				Position pos = RecordingResource.posList.get(index);

				Map<String, Double> gotoPos = new HashMap<String, Double>();
				for (int i = 0; i<pos.getAxisInfo().length; i++)
					{
					gotoPos
							.put(pos.getAxisInfo()[i].getDevice().getAxisName()[pos
									.getAxisInfo()[i].getAxis()], pos.getAxisInfo()[i].getValue());
					}
				RecordingResource.setStagePos(gotoPos);
				}
			}
		else if (e.getSource()==bMoveUp)
			{
			int index = posList.getSelectedIndex();
			if (index>0)
				{
				LinkedList<Position> newList = RecordingResource.posList;
				newList.add(index+1, newList.get(index-1));
				newList.remove(index-1);
				RecordingResource.posListUpdated();

				}

			}
		else if (e.getSource()==bMoveDown)
			{
			int index = posList.getSelectedIndex();
			if (index>=0&&index<listModel.getSize()-1)
				{
				LinkedList<Position> newList = RecordingResource.posList;
				newList.add(index, newList.get(index+1));
				newList.remove(index+2);
				RecordingResource.posList = newList;
				RecordingResource.posListUpdated();

				}

			}
		else if (e.getSource()==bSave)
			{
			savePosList();

			}
		else if (e.getSource()==bLoad)
			{
			loadPosList();

			}
		else if (e.getSource()==bGoToHome)
			{
			RecordingResource.goToHome();
			}
		}

	private void savePosList()
		{
		Position[] anArray = new Position[listModel.getSize()];
		listModel.copyInto(anArray);
		try
			{
			// use buffering
			OutputStream file = new FileOutputStream("list.ser");
			OutputStream buffer = new BufferedOutputStream(file);
			ObjectOutput output = new ObjectOutputStream(buffer);
			try
				{
				output.writeObject(anArray);
				}
			finally
				{
				output.close();
				}
			}
		catch (IOException ex)
			{

			}

		}

	private void loadPosList()
		{
		Position[] anArray = null;

		try
			{
			// use buffering
			InputStream file = new FileInputStream("list.ser");
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream(buffer);

			anArray = (Position[]) input.readObject();
			input.close();
			}
		catch (Exception e2)
			{
			// TODO Auto-generated catch block
			}

		for (int i = 0; i<anArray.length; i++)
			{
			RecordingResource.posList.add(anArray[i]);
			}
		RecordingResource.posListUpdated();
		}

	public void dataChangedEvent()
		{
		// TODO
		}

	public double getStageX() // um
		{
		return RecordingResource.getCurrentStageX();
		}

	public double getStageY() // um
		{
		return RecordingResource.getCurrentStageY();
		}

	public void positionsUpdated()
		{
		listModel.removeAllElements();
		for (int i = 0; i<RecordingResource.posList.size(); i++)
			{
			listModel.addElement(RecordingResource.posList.get(i));

			}

		}

	}
