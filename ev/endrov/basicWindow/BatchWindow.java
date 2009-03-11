package endrov.basicWindow;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import endrov.data.EvData;
import endrov.ev.*;

import org.jdom.*;

/**
 * @author Johan Henriksson
 */
public class BatchWindow extends BasicWindow implements ActionListener, BatchListener
	{
	static final long serialVersionUID=0;
	
	
	//GUI components
	private JButton bStop=new JButton("Stop");
	private JLabel lCurFrame=new JLabel("");

	private final BatchThread thread;
	
	/**
	 * Make a new window at default location
	 */
	public BatchWindow(BatchThread thread)
		{
		this(thread, 50,50,500,100);
		}
	
	/**
	 * Make a new window at some specific location
	 */
	public BatchWindow(BatchThread thread, int x, int y, int w, int h)
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
	public void windowPersonalSettings(Element e)
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
	
	public void loadedFile(EvData data){}
	public void freeResources(){}
	}
