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

	private int numChannelCombo=4;

	//GUI components
	private JButton bStart=new JButton("Start");
	private Vector<ChannelCombo> channelCombo=new Vector<ChannelCombo>();
	private Vector<JCheckBox> equalizeToggle=new Vector<JCheckBox>();
	
	private SpinnerModel startModel =new SpinnerNumberModel(0,0,1000000,1);
	private JSpinner spinnerStart   =new JSpinner(startModel);
	
	private SpinnerModel endModel   =new SpinnerNumberModel(100000,0,1000000,1);
	private JSpinner spinnerEnd     =new JSpinner(endModel);
	
	private SpinnerModel zModel =new SpinnerNumberModel(35,0,1000000,1);
	private JSpinner spinnerZ   =new JSpinner(zModel);

	private SpinnerModel wModel =new SpinnerNumberModel(336,0,1000000,1);
	private JSpinner spinnerW   =new JSpinner(wModel);

	private MetaCombo metaCombo=new MetaCombo(this, false);
	public boolean comboFilterMetadataCallback(EvData meta)
		{
		return meta instanceof Imageset;
		}
	
	private JComboBox codecCombo = new JComboBox(QTMovieMaker.codecs);
	private JComboBox qualityCombo = new JComboBox(QTMovieMaker.qualityStrings);
	
	/**
	 * Make a new window at default location
	 */
	public MakeQTWindow()
		{
		this(20,20,600,300);
		}
	
	/**
	 * Make a new window at some specific location
	 */
	public MakeQTWindow(int x, int y, int w, int h)
		{
		codecCombo.setSelectedItem(QTMovieMaker.codecs[QTMovieMaker.codecs.length-1]);
		qualityCombo.setSelectedItem(QTMovieMaker.qualityStrings[2]);
		for(int i=0;i<numChannelCombo;i++)
			{
			ChannelCombo c=new ChannelCombo((Imageset)metaCombo.getMeta(),true);
			c.addActionListener(this);
			channelCombo.add(c);
			
			JCheckBox e=new JCheckBox("Equalize");
			equalizeToggle.add(e);
			}
		metaCombo.addActionListener(this);
		bStart.addActionListener(this);
		
		//Put GUI together
		setLayout(new BorderLayout());
	
		JPanel midp=new JPanel(new GridLayout(2,6));
		JPanel bottom=new JPanel(new GridLayout(channelCombo.size()+1,1));
		add(metaCombo,BorderLayout.NORTH);
		add(midp, BorderLayout.CENTER);
		add(bottom,BorderLayout.SOUTH);
		
		midp.add(new JLabel("Start frame:"));
		midp.add(spinnerStart);
		midp.add(new JLabel("End frame:"));
		midp.add(spinnerEnd);
		midp.add(new JLabel("Z:"));
		midp.add(spinnerZ);		

		midp.add(new JLabel("Width:"));
		midp.add(spinnerW);		
		midp.add(new JLabel("Codec:"));
		midp.add(codecCombo);
		midp.add(new JLabel("Quality:"));
		midp.add(qualityCombo);
		
		for(int i=0;i<channelCombo.size();i++)
			{
			JPanel cp=new JPanel(new GridLayout(1,2));
			cp.add(new JLabel("Channel "+i+": "));
			cp.add(channelCombo.get(i));
			cp.add(equalizeToggle.get(i));
			bottom.add(cp);
			}
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
				Vector<CalcThread.MovieChannel> channelNames=new Vector<CalcThread.MovieChannel>();
				for(int i=0;i<channelCombo.size();i++)
					if(!channelCombo.get(i).getChannel().equals(""))
						channelNames.add(new CalcThread.MovieChannel(channelCombo.get(i).getChannel(), equalizeToggle.get(i).isSelected()));
					
				BatchThread thread=new CalcThread(metaCombo.getImageset(), 
						(Integer)spinnerStart.getValue(), (Integer)spinnerEnd.getValue(), (Integer)spinnerZ.getValue(), channelNames, (Integer)spinnerW.getValue(),
						(String)codecCombo.getSelectedItem(), (String)qualityCombo.getSelectedItem());
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
