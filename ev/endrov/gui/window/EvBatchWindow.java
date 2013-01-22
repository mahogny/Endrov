/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.gui.window;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import endrov.core.batch.BatchListener;
import endrov.core.batch.BatchThread;
import endrov.data.EvData;

import org.jdom.*;

/**
 * @author Johan Henriksson
 */
public class EvBatchWindow extends EvBasicWindow implements ActionListener, BatchListener
	{
	static final long serialVersionUID=0;
	
	
	//GUI components
	private JButton bStop=new JButton("Stop");
	private JLabel lCurFrame=new JLabel("");

	private final BatchThread thread;
	
	/**
	 * Make a new window at default location
	 */
	public EvBatchWindow(BatchThread thread)
		{
		this(thread, 50,50,500,100);
		}
	
	/**
	 * Make a new window at some specific location
	 */
	public EvBatchWindow(BatchThread thread, int x, int y, int w, int h)
		{		
		bStop.addActionListener(this);
		
		//Put GUI together
		setLayout(new BorderLayout());
		add(lCurFrame,BorderLayout.CENTER);
	
		JPanel bottom=new JPanel();
		add(bottom, BorderLayout.SOUTH);
		
		bottom.add(bStop);
		
		//Window overall things
		setTitleEvWindow("Batch "+ thread.getBatchName());
		packEvWindow();
		setBoundsEvWindow(x,y,w,h);
		setVisibleEvWindow(true);

		//Start job
		this.thread=thread;
		thread.addBatchListener(this);
		thread.start();
		}
	
	/**
	 * Store down settings for window into personal config file
	 */
	public void windowSavePersonalSettings(Element e)
		{
		}
	public void windowLoadPersonalSettings(Element e)
		{
		}

	

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(bStop.getText().equals("Done"))
			disposeEvWindow();
		else
			thread.die=true;
		}
	
	
	/*
	 * (non-Javadoc)
	 * @see client.BasicWindow#dataChanged()
	 */
	public void dataChangedEvent()
		{
		}

	
	
	public void batchDone()
		{
		bStop.setText("Done");
		}
	public void batchError(String s)
		{
		lCurFrame.setText(s);
		System.out.println(s);
		}
	public void batchLog(String s)
		{
		lCurFrame.setText(s);
		}
	
	public void windowEventUserLoadedFile(EvData data){}
	public void windowFreeResources(){}


	@Override
	public String windowHelpTopic()
		{
		return null;
		}	
	}
