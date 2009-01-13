package endrov.makeMovie;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Vector;
//import java.awt.image.*;
import javax.swing.*;

import endrov.basicWindow.*;
import endrov.data.*;
import endrov.ev.*;
import endrov.filter.FilterSeq;
import endrov.filter.WindowFilterSeq;
import endrov.imageset.*;
import endrov.util.EvSwingTools;

import org.jdom.*;

/**
 * Tool for generating expression profiles, I(x,t), where x is the distance to posterior projected to major axis.
 * @author Johan Henriksson
 */
public class MakeMovieWindow extends BasicWindow implements ActionListener
	{
	static final long serialVersionUID=0;
	
	public static void initPlugin()	{}
	static
		{
		BasicWindow.addBasicWindowExtension(new MakeMovieBasic());
		}

	private int numChannelCombo=4;

	//GUI components
	private JButton bStart=new JButton("Start");
	private Vector<EvComboChannel> channelCombo=new Vector<EvComboChannel>();
	private Vector<FilterSeq> filterSeq=new Vector<FilterSeq>();
	private Vector<JTextField> chanDesc=new Vector<JTextField>();
	
	private SpinnerModel startModel =new SpinnerNumberModel(0,0,1000000,1);
	private JSpinner spinnerStart   =new JSpinner(startModel);
	
	private SpinnerModel endModel   =new SpinnerNumberModel(100000,0,1000000,1);
	private JSpinner spinnerEnd     =new JSpinner(endModel);
	
	private SpinnerModel zModel =new SpinnerNumberModel(35,0,1000000,1);
	private JSpinner spinnerZ   =new JSpinner(zModel);

	private SpinnerModel wModel =new SpinnerNumberModel(336,0,1000000,1);
	private JSpinner spinnerW   =new JSpinner(wModel);

	private EvComboObjectOne<Imageset> metaCombo=new EvComboObjectOne<Imageset>(new Imageset(),false,false);

	//TODO maybe put these in codec
//	public static final String[] qualityStrings = {"Low", "Normal", "High", "Maximum"};

	
	private JComboBox codecCombo = new JComboBox(EvMovieMakerFactory.makers);
	private JComboBox qualityCombo = new JComboBox();
	
	/**
	 * Make a new window at default location
	 */
	public MakeMovieWindow()
		{
		this(new Rectangle(20,20,700,300));
		}
	
	public void updateQualityList()
		{
		EvMovieMakerFactory maker=(EvMovieMakerFactory)codecCombo.getSelectedItem();
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
	 * Make a new window at some specific location
	 */
	public MakeMovieWindow(Rectangle bounds)
		{
		updateQualityList();
		//codecCombo.setSelectedItem(QTMovieMaker.codecs[QTMovieMaker.codecs.length-1]);
//		qualityCombo.setSelectedItem(qualityStrings[2]);
		for(int i=0;i<numChannelCombo;i++)
			{
			EvComboChannel c=new EvComboChannel(getCurrentImageset(),true);
			c.addActionListener(this);
			channelCombo.add(c);
			
			filterSeq.add(new FilterSeq());
			if(i==0)
				chanDesc.add(new JTextField("<channel/> (<frame/>)"));
			else
				chanDesc.add(new JTextField("<channel/>"));
			}
		metaCombo.addActionListener(this);
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

		framesPanel.add(EvSwingTools.withLabel("From:", spinnerStart));
		framesPanel.add(EvSwingTools.withLabel("To:", spinnerEnd));
		framesPanel.add(EvSwingTools.withLabel("Z:", spinnerZ));
		framesPanel.setBorder(BorderFactory.createTitledBorder("Range"));
		
		encPanel.add(EvSwingTools.withLabel("Width:", spinnerW));
		encPanel.add(EvSwingTools.withLabel("Codec:", codecCombo));
		encPanel.add(EvSwingTools.withLabel("Quality:", qualityCombo));
		encPanel.setBorder(BorderFactory.createTitledBorder("Encoding"));
		
		JPanel channelPanel=new JPanel(new GridLayout(channelCombo.size()+1,1));
		add(channelPanel,BorderLayout.CENTER);

		channelPanel.add(metaCombo);
		for(int i=0;i<channelCombo.size();i++)
			{
			JPanel cp = new JPanel(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			
			//Channel name
			c.gridy = 0;c.gridx = 0;
			cp.add(new JLabel("Ch "+i+": "),c);
			
			//Filter sequence
			JButton bFS=FilterSeq.createFilterSeqButton();
			final int fi=i;
			bFS.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
					{
					new WindowFilterSeq(filterSeq.get(fi));
					}
			});
			c.gridx++;
			cp.add(bFS,c);
			
			//Channel selector
			c.gridx++;
			cp.add(channelCombo.get(i),c);

			//Channel description
			c.gridx++;
			c.weightx=1;
			c.fill = GridBagConstraints.HORIZONTAL;
			cp.add(chanDesc.get(i),c);
			
			channelPanel.add(cp);
			}
		
		add(bStart,BorderLayout.SOUTH);
		
		
		//Window overall things
		setTitleEvWindow("Make Movie");
		packEvWindow();
		setBoundsEvWindow(bounds);
		setVisibleEvWindow(true);
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
			for(EvComboChannel c:channelCombo)
				c.setRoot(getCurrentImageset());
			packEvWindow();
			}
		else if(e.getSource()==codecCombo)
			{
			updateQualityList();
			}
		else if(e.getSource()==bStart)
			{
			if(/*channelCombo.getChannel().equals("") ||*/ metaCombo.getSelectedObject()==null)
				{
				JOptionPane.showMessageDialog(null, "No imageset selected");
				}
			else
				{
				boolean textBad=false;
				for(JTextField tf:chanDesc)
					if(!new MovieDescString(tf.getText()).isValidXML())
						{
						JOptionPane.showMessageDialog(this, "This is not valid: "+tf.getText());
						textBad=true;
						}
				
				if(!textBad && metaCombo.getSelectedObject()!=null)
					{
					Vector<CalcThread.MovieChannel> channelNames=new Vector<CalcThread.MovieChannel>();
					for(int i=0;i<channelCombo.size();i++)
						if(!channelCombo.get(i).getChannel().equals(""))
							channelNames.add(new CalcThread.MovieChannel(channelCombo.get(i).getChannel(), filterSeq.get(i), chanDesc.get(i).getText()));
						
					EvData data=metaCombo.getData();
					
					//Decide name of movie file
					if(data.io==null || data.io.datadir()==null)
						JOptionPane.showMessageDialog(null,"Error: data does not support a data directory");
					else
						{
						String lastpart=data.getMetadataName()+"-"+channelNames.get(0).name;
						for(int i=1;i<channelNames.size();i++)
							lastpart+="_"+channelNames.get(i).name;
						File moviePath=new File(data.io.datadir(),lastpart);
						
						BatchThread thread=new CalcThread(getCurrentImageset(), 
								(Integer)spinnerStart.getValue(), (Integer)spinnerEnd.getValue(), (Integer)spinnerZ.getValue(), channelNames, (Integer)spinnerW.getValue(),
								(String) (String)qualityCombo.getSelectedItem(),moviePath, (EvMovieMakerFactory)codecCombo.getSelectedItem());
						new BatchWindow(thread);
						}
					}
				}
			}
		}
	
	
	public Imageset getCurrentImageset()
		{
		return metaCombo.getSelectedObjectNotNull();
		}
	
	/*
	 * (non-Javadoc)
	 * @see client.BasicWindow#dataChanged()
	 */
	public void dataChangedEvent()
		{
		metaCombo.updateList();
		Imageset im=getCurrentImageset();	
		for(EvComboChannel c:channelCombo)
			c.setRoot(im);
		}
	
	
	public void loadedFile(EvData data){}
	public void freeResources(){}
	
	}
