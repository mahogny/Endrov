package endrov.sliceSignal;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import endrov.basicWindow.*;
import endrov.data.*;
import endrov.ev.*;
import endrov.imageset.*;
import endrov.shell.*;

import org.jdom.*;


/**
 * Tool for generating expression profiles, I(x,t), where x is the distance to posterior projected to major axis.
 * @author Johan Henriksson
 */
public class SliceSignalWindow extends BasicWindow implements ActionListener
	{
	static final long serialVersionUID=0;
	
	public static void initPlugin()	{}
	static
		{
		BasicWindow.addBasicWindowExtension(new SliceSignalBasic());
		}
	
	
	
	//GUI components
	private JButton bStart=new JButton("Start");
	private EvComboChannel channelCombo=new EvComboChannel(null,false);
	
	private SpinnerModel startModel  =new SpinnerNumberModel(0,0,1000000,1);
	private JSpinner spinnerStart    =new JSpinner(startModel);
	
	private SpinnerModel endModel    =new SpinnerNumberModel(100000,0,1000000,1);
	private JSpinner spinnerEnd      =new JSpinner(endModel);
	
	private SpinnerModel numstripeModel =new SpinnerNumberModel(50,0,1000000,1);
	private JSpinner spinnerNumstripe   =new JSpinner(numstripeModel);
	
	private SpinnerModel stripevarModel =new SpinnerNumberModel(0.001,0.00000001,100000,0.001);
	private JSpinner spinnerStripevar   =new JSpinner(stripevarModel);

	private EvComboObjectOne<Shell> metaCombo=new EvComboObjectOne<Shell>(new Shell(),false,false);


	
	
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
		setTitleEvWindow("Slice/Signal");
		packEvWindow();
		setBoundsEvWindow(x,y,w,h);
		setVisibleEvWindow(true);
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
			channelCombo.setRoot(metaCombo.getSelectedObject());
			System.out.println("metacombo");
			}
		else if(e.getSource()==bStart)
			{
			//maybe null channel
			if(channelCombo.getChannel().equals("") || metaCombo.getSelectedObject()==null)
				{
				JOptionPane.showMessageDialog(null, "No object selected");
				}
			else
				{			
				String svar=""+(Double)spinnerStripevar.getValue();
				if(svar.startsWith("0."))
					svar="0_"+svar.substring(2);
				
				
				
				String suggestName=channelCombo.getData().getMetadataName()+"-"+(Integer)spinnerNumstripe.getValue()+"sl"+svar+"v.txt";
				
				JFileChooser chooser = new JFileChooser();
		    chooser.setCurrentDirectory(channelCombo.getData().io.datadir());
		    chooser.setSelectedFile(new File(suggestName));
		    int returnVal = chooser.showSaveDialog(this);
		    if(returnVal == JFileChooser.APPROVE_OPTION)
		    	{
					BatchThread thread=new CalcThread(channelCombo.getImageset(), metaCombo.getSelectedObject(), 
							(Double)spinnerStripevar.getValue(), (Integer)spinnerNumstripe.getValue(), channelCombo.getChannel(),
							(Integer)spinnerStart.getValue(), (Integer)spinnerEnd.getValue(), 
							chooser.getSelectedFile());
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
		channelCombo.updateList();
		}

	
	public void loadedFile(EvData data){}
	public void freeResources(){}

	
	
	}
