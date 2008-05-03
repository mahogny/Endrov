package evplugin.imageCalc;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import evplugin.data.*;
import evplugin.imageset.*;
import evplugin.basicWindow.*;
import org.jdom.*;

/**
 * Calculate a new chanel by performing aritmetric operations on two chanels.
 * 
 * @author Ricardo Figueroa base on code by Johan Henriksson
 */
public class ImageCalcWindow extends BasicWindow implements ActionListener, MetaCombo.comboFilterMetadata
	{
	static final long serialVersionUID=0;
	

	public static class MaxBasicHook implements BasicWindowHook, ActionListener
		{
		public void createMenus(BasicWindow w)
			{
			JMenuItem mi=new JMenuItem("Image Calculator");
			mi.addActionListener(this);
			w.addMenuBatch(mi);
			}
		public void actionPerformed(ActionEvent e) 
			{
			new ImageCalcWindow();
			}
		public void buildMenu(BasicWindow w){}
		}
	
	public static void initPlugin()	{}
	static
		{
		BasicWindow.addBasicWindowExtension(new BasicWindowExtension()
			{
			public void newBasicWindow(BasicWindow w)
				{
				w.basicWindowExtensionHook.put(this.getClass(),new MaxBasicHook());
				}
			});
		}
	
	
	
	//GUI components
	private String[] operatorString = {"add", "divide", "max", "min", "multiply", "not", "or", "overlay", "subtract", "xor"}; 
	private JButton bStart=new JButton("Calculate");
	
	private ChannelCombo channel1Combo;
	private ChannelCombo channel2Combo;
	
	private SpinnerModel startModel  =new SpinnerNumberModel(0,0,1000000,1);
	private JSpinner spinnerStart    =new JSpinner(startModel);
	
	private SpinnerModel endModel    =new SpinnerNumberModel(100000,0,1000000,1);
	private JSpinner spinnerEnd      =new JSpinner(endModel);
	
	private JComboBox operator = new JComboBox(operatorString);
	
	private MetaCombo metaCombo=new MetaCombo(this, false);
	public boolean comboFilterMetadataCallback(EvData meta)
		{
		return meta instanceof Imageset;
		}
	
	
	/**
	 * Make a new window at default location
	 */
	public ImageCalcWindow()
		{
		this(600,300,500,150);
		}
	
	/**
	 * Make a new window at some specific location
	 */
	public ImageCalcWindow(int x, int y, int w, int h)
		{
		channel1Combo=new ChannelCombo((Imageset)metaCombo.getMeta(),true);
		channel1Combo.addActionListener(this);
		channel2Combo=new ChannelCombo((Imageset)metaCombo.getMeta(),true);
		channel2Combo.addActionListener(this);
		operator.addActionListener(this);
		bStart.addActionListener(this);
		
		//Put GUI together
		setLayout(new BorderLayout());
	
		JPanel bottom=new JPanel(new GridLayout(3,6));
		add(metaCombo,BorderLayout.NORTH);
		add(bottom, BorderLayout.CENTER);
		
		bottom.add(new JLabel("Channel 1:"));
		bottom.add(channel1Combo);
		bottom.add(new JLabel("Operator:"));
		bottom.add(operator);
		bottom.add(new JLabel("Channel 2: "));
		bottom.add(channel2Combo);

		bottom.add(new JLabel("Start frame:"));
		bottom.add(spinnerStart);
		bottom.add(new JLabel(""));
		bottom.add(new JLabel(""));
		bottom.add(new JLabel("End frame:"));
		bottom.add(spinnerEnd);	
		
		bottom.add(new JLabel(""));
		bottom.add(new JLabel(""));
		bottom.add(new JLabel(""));
		bottom.add(new JLabel(""));
		bottom.add(new JLabel(""));
		bottom.add(bStart);
		
		
		//Window overall things
		setTitleEvWindow("Image Calculator");
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
			channel1Combo.setExternalImageset(metaCombo.getImageset());
			channel2Combo.setExternalImageset(metaCombo.getImageset());
			}
		else if(e.getSource()==bStart)
			{
			if(channel1Combo.getChannel().equals("") || metaCombo.getMeta()==null)
				{
				JOptionPane.showMessageDialog(null, "No channel/imageset selected for channel 1");
				}
			else if(channel2Combo.getChannel().equals("") || metaCombo.getMeta()==null)
				{
				JOptionPane.showMessageDialog(null, "No channel/imageset selected for channel 2");
				}
			else
				{
				CalcThread thread=new CalcThread(metaCombo.getImageset(), 
						channel1Combo.getChannel(), (String)operator.getSelectedItem(), channel2Combo.getChannel(), (Integer)spinnerStart.getValue(), (Integer)spinnerEnd.getValue()/*,
						(Double)spinnerQuality.getValue()*/);
				/*CalcThread thread=new CalcThread(metaCombo.getImageset(), 
						(Integer)spinnerStart.getValue(), (Integer)spinnerEnd.getValue(), channelCombo.getChannel(),
						(Double)spinnerQuality.getValue());*/
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
		channel1Combo.setExternalImageset(metaCombo.getImageset());
		channel2Combo.setExternalImageset(metaCombo.getImageset());
		}
	
	public void loadedFile(EvData data){}

	}
