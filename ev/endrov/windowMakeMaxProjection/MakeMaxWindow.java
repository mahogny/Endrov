/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowMakeMaxProjection;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import endrov.data.*;
import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.EvBasicWindowExtension;
import endrov.gui.window.EvBasicWindowHook;
import endrov.gui.window.EvBatchWindow;
import endrov.typeImageset.gui.EvComboChannel;

import org.jdom.*;

/**
 * Make a channel by taking the max-value in every slice from anoher channel.
 * 
 * @author Johan Henriksson
 */
public class MakeMaxWindow extends EvBasicWindow implements ActionListener
	{
	static final long serialVersionUID=0;
	

	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin()	{}
	static
		{
		EvBasicWindow.addBasicWindowExtension(new EvBasicWindowExtension()
			{
			public void newBasicWindow(EvBasicWindow w)
				{
				w.addHook(this.getClass(), new EvBasicWindowHook()
						{
						public void createMenus(EvBasicWindow w)
							{
							JMenuItem mi=new JMenuItem("Make Max Projection Channel");
							mi.addActionListener(new ActionListener(){
							public void actionPerformed(ActionEvent e) 
								{
								new MakeMaxWindow();
								}
							});
							w.addMenuOperation(mi,null);
							}
						public void buildMenu(EvBasicWindow w){}
						});
				}
			});
		}
	
	
	
	//GUI components
	private JButton bStart=new JButton("Start");
	private EvComboChannel channelCombo=new EvComboChannel(false,false);
	
	private SpinnerModel startModel  =new SpinnerNumberModel(0,0,1000000,1);
	private JSpinner spinnerStart    =new JSpinner(startModel);
	
	private SpinnerModel endModel    =new SpinnerNumberModel(1000000,0,1000000,1);
	private JSpinner spinnerEnd      =new JSpinner(endModel);
	
	
	/**
	 * Make a new window at some specific location
	 */
	public MakeMaxWindow()
		{
		channelCombo.addActionListener(this);
		bStart.addActionListener(this);
		
		//Put GUI together
		setLayout(new BorderLayout());
	
		JPanel bottom=new JPanel(new GridLayout(4,2));
		add(bottom, BorderLayout.CENTER);
		
		bottom.add(new JLabel("Start frame:"));
		bottom.add(spinnerStart);
		bottom.add(new JLabel("End frame:"));
		bottom.add(spinnerEnd);	

		bottom.add(new JLabel("Channel: "));
		bottom.add(channelCombo);
		bottom.add(new JLabel(""));
		bottom.add(bStart);
		
		
		//Window overall things
		setTitleEvWindow("Make Max Projection Channel");
		packEvWindow();
		setBoundsEvWindow(600,300,500,300);
		setVisibleEvWindow(true);
		}
	
	/**
	 * Store down settings for window into personal config file
	 */
	public void windowSavePersonalSettings(Element e){}
	public void windowLoadPersonalSettings(Element e){}

	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bStart)
			{
			if(channelCombo.getChannelName()==null)
				{
				JOptionPane.showMessageDialog(null, "No channel/imageset selected");
				}
			else
				{
				CalcThread thread=new CalcThread(channelCombo.getSelectObjectParent(), 
						(Integer)spinnerStart.getValue(), (Integer)spinnerEnd.getValue(), channelCombo.getChannelName());

				new EvBatchWindow(thread);
				}
			}
		}
	
	
	/*
	 * (non-Javadoc)
	 * @see client.BasicWindow#dataChanged()
	 */
	public void dataChangedEvent()
		{
		channelCombo.updateList();
		}
	
	public void windowEventUserLoadedFile(EvData data){}
	public void windowFreeResources(){}

	@Override
	public String windowHelpTopic()
		{
		return null;
		}
	}
