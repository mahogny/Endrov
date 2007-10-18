package evplugin.roi;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import evplugin.data.*;
import evplugin.ev.*;
//import evplugin.imageset.*;
import evplugin.basicWindow.*;
import org.jdom.*;

/**
 * ROI Window - Display all ROIs
 * 
 * @author Johan Henriksson
 */
public class WindowROI extends BasicWindow implements ActionListener, MetaCombo.comboFilterMetadata
	{
	static final long serialVersionUID=0;
	

	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	public static class ThisBasicHook implements BasicWindowHook, ActionListener
		{
		public void createMenus(BasicWindow w)
			{
			JMenuItem mi=new JMenuItem("ROI");
			mi.addActionListener(this);
			w.addMenuWindow(mi);
			}
		public void actionPerformed(ActionEvent e) 
			{
			new WindowROI();
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
				w.basicWindowExtensionHook.put(this.getClass(),new ThisBasicHook());
				}
			});
		}
	
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	
	private MetaCombo metaCombo=new MetaCombo(this, false);
	public boolean comboFilterMetadataCallback(EvData meta)
		{
		return true;
		}

	
	
	
	
	/**
	 * Make a new window at default location
	 */
	public WindowROI()
		{
		this(600,300,500,150);
		}
	
	/**
	 * Make a new window at some specific location
	 */
	public WindowROI(int x, int y, int w, int h)
		{
		metaCombo.addActionListener(this);
		
		//Put GUI together
		setLayout(new BorderLayout());
	
		JPanel bottom=new JPanel(new GridLayout(2,6));
		add(metaCombo,BorderLayout.NORTH);
		add(bottom, BorderLayout.CENTER);
		
		//bottom.add(new JLabel("Start frame:"));
		
		//A tree is probably suitable here
		
		
		//Window overall things
		setTitle(EV.programName+" ROI");
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
			///channelCombo.setImageset(metaCombo.getImageset());
			}
		}
	
	
	/*
	 * (non-Javadoc)
	 * @see client.BasicWindow#dataChanged()
	 */
	public void dataChangedEvent()
		{
		metaCombo.updateList();
		//channelCombo.setImageset(metaCombo.getImageset());
		}
	
	
	}
