package evplugin.sliceSignal;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
//import java.awt.image.*;
import javax.swing.*;

import evplugin.ev.*;
import evplugin.imageset.*;
import evplugin.metadata.Metadata;
//import evplugin.sql.*;
import evplugin.basicWindow.*;
import org.jdom.*;


/**
 * Tool for generating expression profiles, I(x,t), where x is the distance to posterior projected to major axis.
 * @author Johan Henriksson
 */
public class SliceSignalWindow extends BasicWindow implements ActionListener, MetaCombo.comboFilterMetadata
	{
	static final long serialVersionUID=0;
	
	public static void initPlugin()	{}
	static
		{
		BasicWindow.addBasicWindowExtension(new SliceSignalBasic());
		}
	
	
	
	//GUI components
	private JButton bStart=new JButton("Start");
	private ChannelCombo channelCombo;
	
	private SpinnerModel startModel  =new SpinnerNumberModel(0,0,1000000,1);
	private JSpinner spinnerStart    =new JSpinner(startModel);
	
	private SpinnerModel endModel    =new SpinnerNumberModel(100000,0,1000000,1);
	private JSpinner spinnerEnd      =new JSpinner(endModel);
	
	private SpinnerModel numstripeModel =new SpinnerNumberModel(50,0,1000000,1);
	private JSpinner spinnerNumstripe   =new JSpinner(numstripeModel);
	
	private SpinnerModel stripevarModel =new SpinnerNumberModel(0.001,0.00000001,100000,0.001);
	private JSpinner spinnerStripevar   =new JSpinner(stripevarModel);

	private MetaCombo metaCombo=new MetaCombo(this, false);
	public boolean comboFilterMetadataCallback(Metadata meta)
		{
		return meta instanceof Imageset;
		}

	/**
	 * Make a new window at default location
	 */
	public SliceSignalWindow()
		{
		this(300,300,500,150);
		}
	
	/**
	 * Make a new window at some specific location
	 */
	public SliceSignalWindow(int x, int y, int w, int h)
		{
		channelCombo=new ChannelCombo((Imageset)metaCombo.getMeta(),true);
		bStart.addActionListener(this);
		metaCombo.addActionListener(this);
		
		//Put GUI together
		setLayout(new BorderLayout());
		add(metaCombo,BorderLayout.NORTH);
	
		JPanel bottom=new JPanel(new GridLayout(3,4));
		add(bottom, BorderLayout.CENTER);
		
		bottom.add(new JLabel("Start frame:"));
		bottom.add(spinnerStart);
		bottom.add(new JLabel("End frame:"));
		bottom.add(spinnerEnd);
		
		bottom.add(new JLabel("# stripes:"));
		bottom.add(spinnerNumstripe);		
		bottom.add(new JLabel("Stripe variance:"));
		bottom.add(spinnerStripevar);
		
		bottom.add(new JLabel("Channel: "));
		bottom.add(channelCombo);

		bottom.add(new JLabel(""));		
		bottom.add(bStart);
		
		
		//Window overall things
		setTitle(EV.programName+" Slice/Signal");
		pack();
		setBounds(x,y,w,h);
		setVisible(true);
		}
	
	/**
	 * Store down settings for window into personal config file
	 */
	public void windowPersonalSettings(Element root)
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
			channelCombo.setImageset(metaCombo.getImageset());
			System.out.println("metacombo");
			}
		else if(e.getSource()==bStart)
			{
			if(channelCombo.getChannel().equals("") || metaCombo.getMeta()==null)
				{
				JOptionPane.showMessageDialog(null, "No channel selected");
				}
			else
				{			
				String svar=""+(Double)spinnerStripevar.getValue();
				if(svar.startsWith("0."))
					svar="0_"+svar.substring(2);
				
				String suggestName=metaCombo.getMeta().getMetadataName()+"-"+(Integer)spinnerNumstripe.getValue()+"sl"+svar+"v.txt";
				
				JFileChooser chooser = new JFileChooser();
		    chooser.setCurrentDirectory(metaCombo.getImageset().datadir());
		    chooser.setSelectedFile(new File(suggestName));
		    int returnVal = chooser.showSaveDialog(this);
		    if(returnVal == JFileChooser.APPROVE_OPTION)
		    	{
					BatchThread thread=new CalcThread((Imageset)metaCombo.getMeta(), 
							(Double)spinnerStripevar.getValue(), (Integer)spinnerNumstripe.getValue(), channelCombo.getChannel(),
							(Integer)spinnerStart.getValue(), (Integer)spinnerEnd.getValue(), 
							chooser.getSelectedFile().getAbsolutePath());
					new BatchWindow(thread);
		    	}
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
		channelCombo.setImageset(metaCombo.getImageset());
		}

	
	
	
	
	}
