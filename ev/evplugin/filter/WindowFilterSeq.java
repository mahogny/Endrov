package evplugin.filter;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import evplugin.data.*;
import evplugin.ev.*;
//import evplugin.imageset.*;
import evplugin.basicWindow.*;
import evplugin.basicWindow.ObjectCombo.Alternative;

import org.jdom.*;

/**
 * ROI Window - Display all ROIs
 * 
 * @author Johan Henriksson
 */
public class WindowFilterSeq extends BasicWindow implements ActionListener, ObjectCombo.comboFilterMetaObject
	{
	static final long serialVersionUID=0;
	

	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	public static class ThisBasicHook implements BasicWindowHook, ActionListener
		{
		public void createMenus(BasicWindow w)
			{
			JMenuItem mi=new JMenuItem("Filter Sequence");
			mi.addActionListener(this);
			w.addMenuWindow(mi);
			}
		public void actionPerformed(ActionEvent e) 
			{
			new WindowFilterSeq();
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

	
	private ObjectCombo objectCombo=new ObjectCombo(this, false);
	private WidgetFilterSeq filterseq=new WidgetFilterSeq();
	
	
	
	public Alternative[] comboAddAlternative(ObjectCombo combo)
		{
		return new Alternative[]{};
		}
	public Alternative[] comboAddObjectAlternative(ObjectCombo combo, EvData meta)
		{
		return new Alternative[]{};
		}
	public boolean comboFilterMetaObjectCallback(EvObject ob)
		{
		return ob instanceof FilterSeq;
		}

	/**
	 * Make a new window at default location
	 */
	public WindowFilterSeq()
		{
		this(600,300,500,400);
		}
	
	/**
	 * Make a new window at some specific location
	 */
	public WindowFilterSeq(int x, int y, int w, int h)
		{
		objectCombo.addActionListener(this);
		
		//Put GUI together
		setLayout(new BorderLayout());
	
		add(objectCombo,BorderLayout.NORTH);
		add(filterseq,BorderLayout.CENTER);
		
		//Window overall things
		setTitle(EV.programName+" Filter Sequence");
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
		if(e.getSource()==objectCombo)
			{
			filterseq.setFilterSeq((FilterSeq)objectCombo.getObject());
			}
		}
	
	
	/*
	 * (non-Javadoc)
	 * @see client.BasicWindow#dataChanged()
	 */
	public void dataChangedEvent()
		{
		objectCombo.updateObjectList();
		}
	
	
	}
