/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowMakeMovie;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Vector;
//import java.awt.image.*;
import javax.swing.*;

import endrov.core.batch.BatchThread;
import endrov.data.*;
import endrov.gui.EvSwingUtil;
import endrov.gui.component.JSpinnerSimpleEvFrame;
import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.EvBatchWindow;
import endrov.movieEncoder.EvMovieEncoderFactory;
import endrov.movieEncoder.EncodeMovieDescriptionFormat;
import endrov.movieEncoder.EncodeMovieThread;
import endrov.typeImageset.gui.EvComboChannel;
import endrov.util.math.EvDecimal;

import org.jdom.*;

/**
 * Operation for window: generate movie file
 * 
 * @author Johan Henriksson
 */
public class MakeMovieWindow extends EvBasicWindow implements ActionListener
	{
	static final long serialVersionUID=0;
	
	private int numChannelCombo=4;

	//GUI components
	private JButton bStart=new JButton("Start");
	private Vector<EvComboChannel> channelCombo=new Vector<EvComboChannel>();
	//private Vector<FilterSeq> filterSeq=new Vector<FilterSeq>();
	private Vector<JTextField> chanDesc=new Vector<JTextField>();
	
	private JSpinnerSimpleEvFrame spinnerStart   =new JSpinnerSimpleEvFrame();
	private JSpinnerSimpleEvFrame spinnerEnd     =new JSpinnerSimpleEvFrame();
	
	private SpinnerModel zModel =new SpinnerNumberModel(35,0,1000000,1);
	private JSpinner spinnerZ   =new JSpinner(zModel);

	private SpinnerModel wModel =new SpinnerNumberModel(336,0,1000000,1);
	private JSpinner spinnerW   =new JSpinner(wModel);

	//private EvComboObjectOne<Imageset> metaCombo=new EvComboObjectOne<Imageset>(new Imageset(),false,false);

	private JComboBox codecCombo = new JComboBox(EvMovieEncoderFactory.makers);
	private JComboBox qualityCombo = new JComboBox();
	
	private void updateQualityList()
		{
		EvMovieEncoderFactory maker=(EvMovieEncoderFactory)codecCombo.getSelectedItem();
		qualityCombo.removeAllItems();		
		if(maker!=null)
			{
			for(String s:maker.getQualities())
				{
				qualityCombo.addItem(s);
				if(s.equals(maker.getDefaultQuality()))
					qualityCombo.setSelectedItem(s);
				}
			}
		}
	
	/**
	 * Make a new window at default location
	 */
	public MakeMovieWindow()
		{
		this(new Rectangle(20,20,700,300));
		}
	
	/**
	 * Make a new window at some specific location
	 */
	public MakeMovieWindow(Rectangle bounds)
		{
		spinnerEnd.setFrame("1000h");
		
		updateQualityList();
		for(int i=0;i<numChannelCombo;i++)
			{
			EvComboChannel c=new EvComboChannel(true, false);
			c.addActionListener(this);
			channelCombo.add(c);
			
			//filterSeq.add(new FilterSeq());
			if(i==0)
				chanDesc.add(new JTextField("<channel/> (<frame/>)"));
			else
				chanDesc.add(new JTextField("<channel/>"));
			}
		//metaCombo.addActionListener(this);
		bStart.addActionListener(this);
		codecCombo.addActionListener(this);
		
		//Put GUI together
		setLayout(new BorderLayout());
		JPanel totalLeft=new JPanel(new BorderLayout());
		add(totalLeft,BorderLayout.WEST);
		
		
		JPanel framesPanel=new JPanel(new GridLayout(3,1));
		JPanel encPanel=new JPanel(new GridLayout(3,1));
		totalLeft.add(framesPanel,BorderLayout.NORTH);
		totalLeft.add(encPanel,BorderLayout.SOUTH);

		framesPanel.add(EvSwingUtil.withLabel("From:", spinnerStart));
		framesPanel.add(EvSwingUtil.withLabel("To:", spinnerEnd));
		framesPanel.add(EvSwingUtil.withLabel("Z:", spinnerZ));
		framesPanel.setBorder(BorderFactory.createTitledBorder("Range"));
		
		encPanel.add(EvSwingUtil.withLabel("Width:", spinnerW));
		encPanel.add(EvSwingUtil.withLabel("Codec:", codecCombo));
		encPanel.add(EvSwingUtil.withLabel("Quality:", qualityCombo));
		encPanel.setBorder(BorderFactory.createTitledBorder("Encoding"));
		
		JPanel cpChan = new JPanel(new GridBagLayout());
		JPanel someRight=new JPanel(new BorderLayout());
		//someRight.add(metaCombo, BorderLayout.NORTH);
		someRight.add(cpChan, BorderLayout.CENTER);
		
		add(someRight,BorderLayout.CENTER);
		
		GridBagConstraints cChan = new GridBagConstraints();
		cChan.gridy=0;
		cChan.gridx=0;
		cChan.fill = 0;
		
		cpChan.add(new JLabel(""),cChan);
		cChan.gridx++;
		cpChan.add(new JLabel("F"));
		cChan.gridx++;
		cpChan.add(new JLabel("From channel"));
		cChan.gridx++;
		cChan.fill = GridBagConstraints.HORIZONTAL;
		cpChan.add(new JLabel("Description"));
			for(int i=0;i<channelCombo.size();i++)
				{
				
				//Channel name
				//cChan.gridy = 0;
				cChan.gridy++;
				cChan.gridx = 0;
				cpChan.add(new JLabel("Ch "+i+": "),cChan);
				
				//Filter sequence
				/*
				JButton bFS=FilterSeq.createFilterSeqButton();
				final int fi=i;
				bFS.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e)
						{
						new WindowFilterSeq(filterSeq.get(fi));
						}
				});
				*/
				cChan.gridx++;
				//cpChan.add(bFS,cChan);
				
				//Channel selector
				cChan.gridx++;
				cpChan.add(channelCombo.get(i),cChan);
	
				//Channel description
				cChan.gridx++;
				cChan.weightx=1;
				cChan.fill = GridBagConstraints.HORIZONTAL;
				cpChan.add(chanDesc.get(i),cChan);
				
				}
//		channelPanel.add(cpChan);
		
		add(bStart,BorderLayout.SOUTH);
		
		
		//Window overall things
		setTitleEvWindow("Make Movie");
		packEvWindow();
		setBoundsEvWindow(bounds);
		setVisibleEvWindow(true);
		}
	
	public void windowSavePersonalSettings(Element e){}
	public void windowLoadPersonalSettings(Element e){}

	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
		{
		/*
		if(e.getSource()==metaCombo)
			{
			for(EvComboChannel c:channelCombo)
				c.setRoot(getCurrentImageset());
			packEvWindow();
			}
		else*/
		if(e.getSource()==codecCombo)
			{
			updateQualityList();
			}
		else if(e.getSource()==bStart)
			{
			/*
			if( metaCombo.getSelectedObject()==null)
				{
				JOptionPane.showMessageDialog(null, "No imageset selected");
				}
			else*/
				//{
				String textBad=null;
				for(JTextField tf:chanDesc)
					if(!new EncodeMovieDescriptionFormat(tf.getText()).isValidXML())
						textBad="This is not valid: "+tf.getText();
				
				if(textBad!=null)
					EvBasicWindow.showErrorDialog(textBad);
				//else if(metaCombo.getSelectedObject()==null)
				//	BasicWindow.showErrorDialog("No data selected");
				else
					{
					EvDecimal z=new EvDecimal((Integer)spinnerZ.getValue());
					
					Vector<EncodeMovieThread.MovieChannel> channelNames=new Vector<EncodeMovieThread.MovieChannel>();
					for(int i=0;i<channelCombo.size();i++)
						if(channelCombo.get(i).getSelectedObject()!=null)
							{
							channelNames.add(new EncodeMovieThread.MovieChannel(channelCombo.get(i).getChannelName(), channelCombo.get(i).getSelectedObject(),/*filterSeq.get(i),*/ chanDesc.get(i).getText(),z));
							}
					if(channelNames.isEmpty())
						{
						showErrorDialog("No channel selected");
						return;
						}
					
					EvData data=channelCombo.get(0).getData();//metaCombo.getData();
					
					//Decide name of movie file
					File outdir;
					if(data.io==null || data.io.datadir()==null)
						outdir=null;
					else
						outdir=data.io.datadir();
					
					if(outdir==null)
						EvBasicWindow.showErrorDialog("This fileformat plugin does not support datadir");
					else
						{
						String lastpart=data.getMetadataName()+"-"+channelNames.get(0).name;
						for(int i=1;i<channelNames.size();i++)
							lastpart+="_"+channelNames.get(i).name;
						File moviePath=new File(outdir,lastpart);
						
						BatchThread thread=new EncodeMovieThread( 
								spinnerStart.getDecimalValue(), spinnerEnd.getDecimalValue(), channelNames, (Integer)spinnerW.getValue(),
								(EvMovieEncoderFactory)codecCombo.getSelectedItem(),(String)qualityCombo.getSelectedItem(), moviePath);
						new EvBatchWindow(thread);
						}
					}
				//}
			}
		}
	
	/*
	public Imageset getCurrentImageset()
		{
		channelCombo.get(0).
		return metaCombo.getSelectedObjectNotNull();
		}*/
	
	/*
	 * (non-Javadoc)
	 * @see client.BasicWindow#dataChanged()
	 */
	public void dataChangedEvent()
		{
		/*
		metaCombo.updateList();
		Imageset im=getCurrentImageset();	
		for(EvComboChannel c:channelCombo)
			c.setRoot(im);*/
		
		for(EvComboChannel c:channelCombo)
			c.updateList();
		}
	
	
	public void windowEventUserLoadedFile(EvData data){}
	public void windowFreeResources(){}

	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin()	{}
	static
		{
		EvBasicWindow.addBasicWindowExtension(new MakeMovieBasic());
		}

	}
