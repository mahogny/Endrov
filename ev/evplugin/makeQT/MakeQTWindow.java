package evplugin.makeQT;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
//import java.awt.image.*;
import javax.swing.*;

import evplugin.ev.*;
import evplugin.imageset.*;
import evplugin.data.*;
import evplugin.basicWindow.*;
import org.jdom.*;

/**
 * Tool for generating expression profiles, I(x,t), where x is the distance to posterior projected to major axis.
 * @author Johan Henriksson
 */
public class MakeQTWindow extends BasicWindow implements ActionListener, MetaCombo.comboFilterMetadata
	{
	static final long serialVersionUID=0;
	
	public static void initPlugin()	{}
	static
		{
		BasicWindow.addBasicWindowExtension(new MakeQTBasic());
		}
	
	
	//GUI components
	private JButton bStart=new JButton("Start");
	private Vector<ChannelCombo> channelCombo=new Vector<ChannelCombo>();
	private int numChannelCombo=4;
	
	private SpinnerModel startModel  =new SpinnerNumberModel(0,0,1000000,1);
	private JSpinner spinnerStart    =new JSpinner(startModel);
	
	private SpinnerModel endModel    =new SpinnerNumberModel(100000,0,1000000,1);
	private JSpinner spinnerEnd      =new JSpinner(endModel);
	
	private SpinnerModel zModel =new SpinnerNumberModel(35,0,1000000,1);
	private JSpinner spinnerZ   =new JSpinner(zModel);

	private SpinnerModel wModel =new SpinnerNumberModel(336,0,1000000,1);
	private JSpinner spinnerW   =new JSpinner(wModel);

	private MetaCombo metaCombo=new MetaCombo(this, false);
	public boolean comboFilterMetadataCallback(EvData meta)
		{
		return meta instanceof Imageset;
		}
	
	
	/**
	 * Make a new window at default location
	 */
	public MakeQTWindow()
		{
		this(300,300,500,150);
		}
	
	/**
	 * Make a new window at some specific location
	 */
	public MakeQTWindow(int x, int y, int w, int h)
		{
		for(int i=0;i<numChannelCombo;i++)
			{
			ChannelCombo c=new ChannelCombo((Imageset)metaCombo.getMeta(),true);
			c.addActionListener(this);
			channelCombo.add(c);
			}
		metaCombo.addActionListener(this);
		bStart.addActionListener(this);
		
		//Put GUI together
		setLayout(new BorderLayout());
	
		JPanel bottom=new JPanel(new GridLayout(3,4));
		add(metaCombo,BorderLayout.NORTH);
		add(bottom, BorderLayout.CENTER);
		
		bottom.add(new JLabel("Start frame:"));
		bottom.add(spinnerStart);
		bottom.add(new JLabel("End frame:"));
		bottom.add(spinnerEnd);
		
		bottom.add(new JLabel("Z:"));
		bottom.add(spinnerZ);		

		bottom.add(new JLabel("Width:"));
		bottom.add(spinnerW);		

		for(int i=0;i<channelCombo.size();i++)
			{
			bottom.add(new JLabel("Channel "+i+": "));
			bottom.add(channelCombo.get(i));
			}
//		bottom.add(new JLabel(""));		
//		bottom.add(new JLabel(""));		
		bottom.add(new JLabel(""));		
		bottom.add(bStart);
		
		
		//Window overall things
		setTitle(EV.programName+" Make QT Movie ");
		pack();
		setBounds(x,y,w,h);
		setVisible(true);
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
		if(e.getSource()==metaCombo)
			{
			for(ChannelCombo c:channelCombo)
				c.setImageset(metaCombo.getImageset());
			}
		else if(e.getSource()==bStart)
			{
			if(/*channelCombo.getChannel().equals("") ||*/ metaCombo.getMeta()==null)
				{
				JOptionPane.showMessageDialog(null, "No imageset selected");
				}
			else
				{		
				Vector<String> channelNames=new Vector<String>();
				for(ChannelCombo c:channelCombo)
					if(!c.getChannel().equals(""))
						channelNames.add(c.getChannel());
					
				BatchThread thread=new CalcThread(metaCombo.getImageset(), 
						(Integer)spinnerStart.getValue(), (Integer)spinnerEnd.getValue(), (Integer)spinnerZ.getValue(), channelNames, (Integer)spinnerW.getValue());
				new BatchWindow(thread);
				}
			}
		}
	
	
	/*
	 * (non-Javadoc)
	 * @see client.BasicWindow#dataChanged()
	 */
	public void dataChangedEvent()
		{
		metaCombo.updateList();
		for(ChannelCombo c:channelCombo)
			c.setImageset(metaCombo.getImageset());
		}
	
	
	
	
	
	

	
	
	}
