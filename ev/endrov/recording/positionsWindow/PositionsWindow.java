/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.positionsWindow;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jdom.*;

import endrov.basicWindow.*;
import endrov.data.EvData;
import endrov.recording.RecordingResource;
import endrov.recording.RecordingResource.PositionListListener;
import endrov.util.EvFileUtil;
import endrov.util.EvSwingUtil;

/**
 * Window used to create and remove positions
 * 
 * @author Kim Nordlöf, Erik Vernersson
 */
public class PositionsWindow extends BasicWindow implements ActionListener, PositionListListener
	{
	/******************************************************************************************************
	 * Static *
	 *****************************************************************************************************/
	static final long serialVersionUID = 0;

	
	/******************************************************************************************************
	 * Instance *
	 *****************************************************************************************************/
	private JList posList;
	private DefaultListModel listModel = new DefaultListModel();
	private CheckBoxList infoList = new CheckBoxList();

	private JButton bAdd = new JButton("Add");
	private JButton bRemove = new JButton("Remove");
	private JButton bGoTo = new JButton("Go To Position");
	private JButton bMoveUp = new JButton("Move Up");
	private JButton bMoveDown = new JButton("Move Down");
	private JButton bSave = new JButton("Save Positions");
	private JButton bLoad = new JButton("Load Positions");
	private JButton bGoToHome = new JButton("Go to home position");
	
	
	
	
	public PositionsWindow()
		{
		this(new Rectangle(500, 300));
		}

	public PositionsWindow(Rectangle bounds)
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

		bPanel.add(EvSwingUtil.layoutCompactVertical(
				bAdd, bRemove, bGoTo, bMoveUp,
				bMoveDown, bSave, bLoad, bGoToHome));


		posList = new JList(listModel);
		posList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		posList.setLayoutOrientation(JList.VERTICAL);

		JScrollPane infoScroller = new JScrollPane(infoList);
		JScrollPane listScroller = new JScrollPane(posList);
		
		posPanel.add(listScroller, BorderLayout.NORTH);
		posPanel.add(infoScroller, BorderLayout.CENTER);
		
		setLayout(new BorderLayout());
		add(EvSwingUtil.withTitledBorder("Positions", posPanel), BorderLayout.CENTER);
		add(bPanel, BorderLayout.WEST);

		RecordingResource.posListListeners.addWeakListener(this);
		RecordingResource.posListUpdated();
		
		// Window overall things
		setTitleEvWindow("Positions");
		setBoundsEvWindow(500, 300);
		setVisibleEvWindow(true);
		}

	public void dataChangedEvent()
		{
		}

	public void loadedFile(EvData data)
		{
		}

	public void windowSavePersonalSettings(Element e)
		{

		}

	public void freeResources()
		{
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

			Position newPos = new Position(newInfo,	RecordingResource.getUnusedPosName());
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
					AxisInfo ai=pos.getAxisInfo()[i];
					gotoPos.put(
							ai.getDevice().getAxisName()[pos.getAxisInfo()[i].getAxis()], 
							ai.getValue());
					}
				RecordingResource.setStagePos(gotoPos);
				}
			}
		else if (e.getSource()==bMoveUp)
			{
			int index = posList.getSelectedIndex();
			if (index>0)
				{
				LinkedList<Position> posList = RecordingResource.posList;
				posList.add(index+1, posList.get(index-1));
				posList.remove(index-1);
				RecordingResource.posListUpdated();
				}

			}
		else if (e.getSource()==bMoveDown)
			{
			int index = posList.getSelectedIndex();
			if (index>=0 && index<listModel.getSize()-1)
				{
				LinkedList<Position> posList = RecordingResource.posList;
				posList.add(index, posList.get(index+1));
				posList.remove(index+2);
				RecordingResource.posList = posList;
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
		
		JFileChooser fc=new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Endrov positions file", "evpos");
    fc.setFileFilter(filter);
		int ret=fc.showSaveDialog(this);

		if(ret == JFileChooser.APPROVE_OPTION)
			{
			File theFile=fc.getSelectedFile();
			theFile=EvFileUtil.makeFileEnding(theFile, ".evpos");
			System.out.println(theFile);
			RecordingResource.savePosList(theFile);
			}
		}

	
	
	private void loadPosList()
		{
		
		JFileChooser fc=new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Endrov positions file", "evpos");
    fc.setFileFilter(filter);
		int ret=fc.showOpenDialog(this);
		
		if(ret == JFileChooser.APPROVE_OPTION)
			{
			File theFile=fc.getSelectedFile();
			RecordingResource.loadPosList(theFile);
			}
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
			listModel.addElement(RecordingResource.posList.get(i));
		repaint();
		}
	

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin()
		{
		}

	static
		{
		BasicWindow.addBasicWindowExtension(new BasicWindowExtension()
			{
				public void newBasicWindow(BasicWindow w)
					{
					w.basicWindowExtensionHook.put(this.getClass(), new Hook());
					}

				class Hook implements BasicWindowHook, ActionListener
					{
					public void createMenus(BasicWindow w)
						{
						JMenuItem mi = new JMenuItem("Positions", new ImageIcon(getClass()
								.getResource("jhPositionsWindow.png")));
						mi.addActionListener(this);
						BasicWindow.addMenuItemSorted(
								w.getCreateMenuWindowCategory("Recording"), mi);
						}

					public void actionPerformed(ActionEvent e)
						{
						new PositionsWindow();
						}

					public void buildMenu(BasicWindow w)
						{
						}
					}
			});

		}

	}
