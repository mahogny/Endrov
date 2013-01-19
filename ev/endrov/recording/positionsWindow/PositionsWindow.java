/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.positionsWindow;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jdom.*;

import endrov.data.EvData;
import endrov.gui.EvSwingUtil;
import endrov.gui.icon.BasicIcon;
import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.EvBasicWindowExtension;
import endrov.gui.window.EvBasicWindowHook;
import endrov.recording.RecordingResource;
import endrov.recording.StoredStagePosition;
import endrov.recording.RecordingResource.PositionListListener;
import endrov.util.io.EvFileUtil;

/**
 * Window used to create and remove positions
 * 
 * @author Kim Nordlöf, Erik Vernersson
 */
public class PositionsWindow extends EvBasicWindow implements ActionListener, PositionListListener
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
	private RecWidgetAxisInclude axisList = new RecWidgetAxisInclude();

	private JButton bAdd = new JButton(BasicIcon.iconAdd);
	private JButton bRemove = new JButton(BasicIcon.iconRemove);
	private JButton bMoveUp = new JButton(BasicIcon.iconButtonUp);
	private JButton bMoveDown = new JButton(BasicIcon.iconButtonDown);
	
	private JButton bRename = new JButton("Rename");
	
	private JButton bGoToPos = new JButton("Go to pos");
	private JButton bGoToHome = new JButton("Go to home");
	
	private JButton bSave = new JButton("Save...");
	private JButton bLoad = new JButton("Load...");
	
	
	
	

	public PositionsWindow()
		{
		bAdd.addActionListener(this);
		bRemove.addActionListener(this);
		bGoToPos.addActionListener(this);
		bMoveUp.addActionListener(this);
		bMoveDown.addActionListener(this);
		bRename.addActionListener(this);
		bSave.addActionListener(this);
		bLoad.addActionListener(this);
		bGoToHome.addActionListener(this);

		bAdd.setToolTipText("Store current position");
		bRemove.setToolTipText("Remove selected positions");
		bMoveUp.setToolTipText("Move selected position up");
		bMoveDown.setToolTipText("Move selected position down");
		bRename.setToolTipText("Rename position");
		bSave.setToolTipText("Store all positions to disk");
		bLoad.setToolTipText("Load positions from disk");
		bGoToPos.setToolTipText("Move stage to selected position");
		bGoToHome.setToolTipText("Move stage to home position");
		
		posList = new JList(listModel);
		posList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		posList.setLayoutOrientation(JList.VERTICAL);
		JScrollPane posListScroller = new JScrollPane(posList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		JPanel posPanel = new JPanel(new BorderLayout());
		posPanel.add(posListScroller, BorderLayout.CENTER);
		posPanel.add(axisList, BorderLayout.SOUTH);

		
		JPanel bPanel = new JPanel(new BorderLayout()); 
		bPanel.add(EvSwingUtil.layoutCompactVertical(
				bAdd, bRemove, 
				bMoveUp, bMoveDown,
				bRename,
				bGoToPos, bGoToHome,
				bSave, bLoad), BorderLayout.NORTH);

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

	public void windowEventUserLoadedFile(EvData data)
		{
		}

	public void windowSavePersonalSettings(Element e){}
	public void windowLoadPersonalSettings(Element e){}
	public void windowFreeResources(){}

	
	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource()==bAdd)
			{
			StoredStagePosition newPos=axisList.createCurrentPosition();
			RecordingResource.posList.add(newPos);
			RecordingResource.posListUpdated();
			}
		else if (e.getSource()==bRemove)
			removeSelectedPos();
		else if (e.getSource()==bGoToPos)
			{
			int index = posList.getSelectedIndex();
			if (index>=0)
				RecordingResource.posList.get(index).goTo();
			}
		else if (e.getSource()==bMoveUp)
			moveUp();
		else if (e.getSource()==bMoveDown)
			moveDown();
		else if (e.getSource()==bSave)
			savePosList();
		else if (e.getSource()==bLoad)
			loadPosList();
		else if (e.getSource()==bGoToHome)
			RecordingResource.goToHome();
		else if(e.getSource()==bRename)
			{
			int index = posList.getSelectedIndex();
			if (index>=0)
				renameDialog(RecordingResource.posList.get(index));
			}
		}
	
	
	private void renameDialog(StoredStagePosition pos)
		{
		String input=EvBasicWindow.showInputDialog("Name of position", pos.getName());
		if(input!=null && !input.equals(""))
			{
			pos.setName(input);
			RecordingResource.posListUpdated();
			}
		}

	private void removeSelectedPos()
		{
		int[] indices=posList.getSelectedIndices();
		for(int i=indices.length-1;i>=0;i--)
			RecordingResource.posList.remove(indices[i]);
		RecordingResource.posListUpdated();

		/*
		int index = posList.getSelectedIndex();
		if (index>=0)
			{
			RecordingResource.posList.remove(index);
			RecordingResource.posListUpdated();
			}
		*/
		}
	
	private void moveUp()
		{
		int index = posList.getSelectedIndex();
		if (index>0)
			{
			LinkedList<StoredStagePosition> posList = RecordingResource.posList;
			posList.add(index+1, posList.get(index-1));
			posList.remove(index-1);
			RecordingResource.posListUpdated();
			}
		}

	private void moveDown()
		{
		int index = posList.getSelectedIndex();
		if (index>=0 && index<listModel.getSize()-1)
			{
			LinkedList<StoredStagePosition> posList = RecordingResource.posList;
			posList.add(index, posList.get(index+1));
			posList.remove(index+2);
			RecordingResource.posList = posList;
			RecordingResource.posListUpdated();
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
			try
				{
				File theFile=fc.getSelectedFile();
				RecordingResource.loadPosList(theFile);
				}
			catch (IOException e)
				{
				EvBasicWindow.showErrorDialog(e.getMessage());
				}
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
		EvBasicWindow.addBasicWindowExtension(new EvBasicWindowExtension()
			{
				public void newBasicWindow(EvBasicWindow w)
					{
					w.basicWindowExtensionHook.put(this.getClass(), new Hook());
					}

				class Hook implements EvBasicWindowHook, ActionListener
					{
					public void createMenus(EvBasicWindow w)
						{
						JMenuItem mi = new JMenuItem("Positions", new ImageIcon(getClass()
								.getResource("jhPositionsWindow.png")));
						mi.addActionListener(this);
						EvBasicWindow.addMenuItemSorted(
								w.getCreateMenuWindowCategory("Recording"), mi);
						}

					public void actionPerformed(ActionEvent e)
						{
						new PositionsWindow();
						}

					public void buildMenu(EvBasicWindow w)
						{
						}
					}
			});

		}

	}
