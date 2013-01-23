/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowMakeMovie;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
//import java.awt.image.*;
import javax.swing.*;

import endrov.core.batch.BatchThread;
import endrov.data.*;
import endrov.flow.EvOpSlice1;
import endrov.gui.EvColor;
import endrov.gui.EvSwingUtil;
import endrov.gui.component.JSpinnerSimpleEvFrame;
import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.EvBatchWindow;
import endrov.movieEncoder.EvMovieEncoderFactory;
import endrov.movieEncoder.EncodeMovieDescriptionFormat;
import endrov.movieEncoder.EncodeMovieThread;
import endrov.typeImageset.EvChannel;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.gui.EvComboChannel;
import endrov.util.ProgressHandle;
import endrov.util.math.EvDecimal;
import endrov.windowViewer2D.Viewer2DWindow;

import org.jdom.*;


/**
 * Operation for window: generate movie file
 * 
 * @author Johan Henriksson
 */
public class MakeMovieWindow extends EvBasicWindow implements ActionListener
	{
	static final long serialVersionUID=0;
	

	
	private class ChannelChoice
		{
		EncodeMovieThread.MovieChannel channelFromView;
		
		EvComboChannel chCombo=new EvComboChannel(false, false);
		
		JTextField chanDesc=new JTextField();
		
		public JComponent getChanComponent()
			{
			if(channelFromView!=null)
				return new JLabel(channelFromView.name+": ");
			else
				return chCombo;
			}

		public EncodeMovieThread.MovieChannel getChannel()
			{
			if(channelFromView!=null)
				return channelFromView;
			else
				{
				EvDecimal z=new EvDecimal((Integer)spinnerZ.getValue());
				return new EncodeMovieThread.MovieChannel(chCombo.getChannelName(), chCombo.getSelectedObject(),chanDesc.getText(),z);
				}
			}
		}
	
	
	
	//GUI components
	private JButton bStart=new JButton("Start");
	private Vector<ChannelChoice> channelCombo=new Vector<ChannelChoice>();
	
	private JSpinnerSimpleEvFrame spinnerStart   =new JSpinnerSimpleEvFrame();
	private JSpinnerSimpleEvFrame spinnerEnd     =new JSpinnerSimpleEvFrame();
	
	private SpinnerModel zModel =new SpinnerNumberModel(35,0,1000000,1);
	private JSpinner spinnerZ   =new JSpinner(zModel);

	private SpinnerModel wModel =new SpinnerNumberModel(336,0,1000000,1);
	private JSpinner spinnerW   =new JSpinner(wModel);

	private JComboBox codecCombo = new JComboBox(EvMovieEncoderFactory.makers);
	private JComboBox qualityCombo = new JComboBox();
	
	private JTextField tfFileName=new JTextField();
	
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
	

	public MakeMovieWindow()
		{
		this(new LinkedList<EncodeMovieThread.MovieChannel>(), null);
		}

	/**
	 * Make a new window at some specific location
	 */
	public MakeMovieWindow(List<EncodeMovieThread.MovieChannel> channelNames, File suggestFileName)
		{
		if(suggestFileName!=null)
			tfFileName.setText(suggestFileName.getAbsolutePath());

		
		spinnerEnd.setFrame("1000h");
		
		updateQualityList();
		
		if(channelNames.isEmpty())
			{
			//Create channel selection controls
			for(int i=0;i<4;i++)
				{
				ChannelChoice c=new ChannelChoice();
				channelCombo.add(c);
				
				if(i==0)
					c.chanDesc.setText("<channel/> (<frame/>)");
				else
					c.chanDesc.setText("<channel/>");
				}
			}
		else
			{
			//Fill in suggested channels
			for(EncodeMovieThread.MovieChannel in:channelNames)
				{
				ChannelChoice c=new ChannelChoice();
				c.channelFromView=in;
				channelCombo.add(c);
				}
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
		someRight.add(EvSwingUtil.withTitledBorder("Channels", cpChan), BorderLayout.CENTER);
		
		add(someRight,BorderLayout.CENTER);
		
		GridBagConstraints cChan = new GridBagConstraints();
		cChan.fill = GridBagConstraints.HORIZONTAL;
		cChan.gridy=0;
		cChan.gridx=0;
		
		cpChan.add(new JLabel("Source"));
		cChan.gridx++;
		
		cpChan.add(new JLabel("Description"));
		
		
		for(int i=0;i<channelCombo.size();i++)
			{
			ChannelChoice c=channelCombo.get(i);
			cChan.gridy++;
			
			//Channel selector
			cChan.gridx = 0;
			cChan.weightx=0;
			cpChan.add(c.getChanComponent(),cChan);

			//Channel description
			cChan.gridx++;
			cChan.weightx=1;
			cChan.fill = GridBagConstraints.HORIZONTAL;
			cpChan.add(c.chanDesc,cChan);
			}

		JPanel pSouth=new JPanel(new GridLayout(2,1));
		pSouth.add(EvSwingUtil.withLabel("Filename: ", tfFileName));
		pSouth.add(bStart);
		add(pSouth, BorderLayout.SOUTH);
		
		//Window overall things
		setTitleEvWindow("Make Movie");
		setMinimumSize(new Dimension(400,50));
		packEvWindow();
		setVisibleEvWindow(true);
		}
	
	public void windowSavePersonalSettings(Element e){}
	public void windowLoadPersonalSettings(Element e){}

	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==codecCombo)
			{
			updateQualityList();
			}
		else if(e.getSource()==bStart)
			{
			LinkedList<EncodeMovieThread.MovieChannel> channelNames=new LinkedList<EncodeMovieThread.MovieChannel>();


			for(ChannelChoice c:channelCombo)
				{
				if(!new EncodeMovieDescriptionFormat(c.chanDesc.getText()).isValidXML())
					{
					showErrorDialog("This is not a valid description: "+c.chanDesc.getText());
					return;
					}

				EncodeMovieThread.MovieChannel channel=c.getChannel();
				channel.desc=new EncodeMovieDescriptionFormat(c.chanDesc.getText());

				channelNames.add(c.getChannel());
				}

			File moviePath=new File(tfFileName.getText());

			BatchThread thread=new EncodeMovieThread( 
					spinnerStart.getDecimalValue(), spinnerEnd.getDecimalValue(), channelNames, (Integer)spinnerW.getValue(),
					(EvMovieEncoderFactory)codecCombo.getSelectedItem(),(String)qualityCombo.getSelectedItem(), moviePath);
			new EvBatchWindow(thread);
			}
		}
	
	public void dataChangedEvent()
		{
		for(ChannelChoice c:channelCombo)
			c.chCombo.updateList();
		}
	
	
	public void windowEventUserLoadedFile(EvData data){}
	public void windowFreeResources(){}

	@Override
	public String windowHelpTopic()
		{
		return "Making movies";
		}
	
	
	
	
	

	private static File suggestName(EvData data, List<EncodeMovieThread.MovieChannel> channelNames)
		{
		File outdir;
		if(data.io==null || data.io.datadir()==null)
			outdir=null;
		else
			outdir=data.io.datadir();
		
		if(outdir==null)
			return null;
		else
			{
			String lastpart=data.getMetadataName()+"-"+channelNames.get(0).name;
			for(int i=1;i<channelNames.size();i++)
				lastpart+="_"+channelNames.get(i).name;
			File moviePath=new File(outdir,lastpart);
			return moviePath;
			}
		}
	

	/**
	 * This is not a proper evop - it returns a colored image
	 */
	private static class SpecialOpContrastBrightness extends EvOpSlice1
		{
		private final double contrast;
		private final double brightness;
		private final EvColor color;
		
		public SpecialOpContrastBrightness(double contrast, double brightness, EvColor color)
			{
			this.contrast=contrast;
			this.brightness=brightness;
			this.color=color;
			}
		
		private static final byte clampByte(int i)
			{
			if(i > 255)
				return -1; //really correct? why -1????
			if(i < 0)
				return 0;
			else
				return (byte)i;
			}


		@Override
		public EvPixels exec1(ProgressHandle ph, EvPixels... parr)
			{
			EvPixels p=parr[0];
			
			if(contrast==1 && brightness==0 && color.equals(EvColor.white))
				return p;
			else
				{
				double contrastR=contrast*color.getRedDouble();
				double contrastG=contrast*color.getGreenDouble();
				double contrastB=contrast*color.getBlueDouble();

				int w=p.getWidth();
				int h=p.getHeight();
				double[] aPixels=p.convertToDouble(true).getArrayDouble();
				BufferedImage buf=new BufferedImage(w,h,BufferedImage.TYPE_3BYTE_BGR);

				byte[] outarr=new byte[w*h*3];

				for(int i=0;i<aPixels.length;i++)
					{
					byte b=clampByte((int)(aPixels[i]*contrastB+brightness));
					byte g=clampByte((int)(aPixels[i]*contrastG+brightness));
					byte r=clampByte((int)(aPixels[i]*contrastR+brightness));
					outarr[i*3+0]=r;
					outarr[i*3+1]=g;
					outarr[i*3+2]=b;
					}

				buf.getRaster().setDataElements(0, 0, w, h, outarr);
				
				return new EvPixels(buf);
				}
			}
		}
	
	/**
	 * Create a dialog using settings from all open image windows
	 */
	public static void createDialogFromImageWindows()
		{
		ProgressHandle ph=new ProgressHandle();
		
		Map<Integer, Viewer2DWindow> imwindows=new TreeMap<Integer, Viewer2DWindow>();
		List<EncodeMovieThread.MovieChannel> channelNames=new ArrayList<EncodeMovieThread.MovieChannel>();
		EvData data=null;
		for(EvBasicWindow w:EvBasicWindow.windowManager.getAllWindows())
			if(w instanceof Viewer2DWindow)
				imwindows.put(w.windowInstance, (Viewer2DWindow)w);

		for(Viewer2DWindow w:imwindows.values())
			{
			data=w.getSelectedData();
			for(Viewer2DWindow.ChannelWidget chw:w.getChannels())
				{
				//Transform channel using C/B
				EvChannel ch=chw.getChannel();
				if(ch==null)
					continue;
				ch=new SpecialOpContrastBrightness(chw.getContrast(),chw.getBrightness(),chw.getColor()).exec1(ph, ch);
				
				//Add channel to list
				channelNames.add(new EncodeMovieThread.MovieChannel(chw.getChannelName(), ch, "", w.getZ()));

				//TODO gotcha - uses settings when window was opened - cannot change later!!!
				}
			}
		
		if(channelNames.isEmpty())
			EvBasicWindow.showErrorDialog("You need to have some channels in the image windows open");
		else
			{
			File suggestOutputDir=suggestName(data, channelNames);
			new MakeMovieWindow(channelNames, suggestOutputDir);	
			}
		}
	
	
	
	
	
	
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin()	{}
	static
		{
		EvBasicWindow.addBasicWindowExtension(new MakeMovieBasic());
		}

	}
