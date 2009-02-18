package endrov.makeMax;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import endrov.basicWindow.*;
import endrov.data.*;
import endrov.imageset.*;

import org.jdom.*;

/**
 * Make a channel by taking the max-value in every slice from anoher channel.
 * 
 * @author Johan Henriksson
 */
public class MakeMaxWindow extends BasicWindow implements ActionListener
	{
	static final long serialVersionUID=0;
	

	public static class MaxBasicHook implements BasicWindowHook, ActionListener
		{
		public void createMenus(BasicWindow w)
			{
			JMenuItem mi=new JMenuItem("Make Max Channel");
			mi.addActionListener(this);
			w.addMenuBatch(mi);
			}
		public void actionPerformed(ActionEvent e) 
			{
			new MakeMaxWindow();
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
	private JButton bStart=new JButton("Start");
	private EvComboChannel channelCombo=new EvComboChannel(null,false);
	
	private SpinnerModel startModel  =new SpinnerNumberModel(0,0,1000000,1);
	private JSpinner spinnerStart    =new JSpinner(startModel);
	
	private SpinnerModel endModel    =new SpinnerNumberModel(100000,0,1000000,1);
	private JSpinner spinnerEnd      =new JSpinner(endModel);

//	private SpinnerModel qualityModel    =new SpinnerNumberModel(0.99,0.0,1.0,0.01);
//	private JSpinner spinnerQuality      =new JSpinner(qualityModel);

	
	/**
	 * Make a new window at default location
	 */
	public MakeMaxWindow()
		{
		this(600,300,500,150);
		}
	
	/**
	 * Make a new window at some specific location
	 */
	public MakeMaxWindow(int x, int y, int w, int h)
		{
		channelCombo.addActionListener(this);
		bStart.addActionListener(this);
		
		//Put GUI together
		setLayout(new BorderLayout());
	
		JPanel bottom=new JPanel(new GridLayout(2,4));
		add(bottom, BorderLayout.CENTER);
		
		bottom.add(new JLabel("Start frame:"));
		bottom.add(spinnerStart);
		bottom.add(new JLabel("End frame:"));
		bottom.add(spinnerEnd);	

		bottom.add(new JLabel("Channel: "));
		bottom.add(channelCombo);
		bottom.add(new JLabel(""));
		bottom.add(bStart);
		
		
		//Window overall things
		setTitleEvWindow("Make Max Channel");
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
		if(e.getSource()==bStart)
			{
			if(channelCombo.getChannel()==null)
				{
				JOptionPane.showMessageDialog(null, "No channel/imageset selected");
				}
			else
				{
				CalcThread thread=new CalcThread(channelCombo.getImageset(), 
						(Integer)spinnerStart.getValue(), (Integer)spinnerEnd.getValue(), channelCombo.getChannel());

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
		channelCombo.updateList();
		}
	
	public void loadedFile(EvData data){}
	public void freeResources(){}

	}
