package evplugin.customData;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import java.util.*;
import org.jfree.data.xy.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;

import evplugin.basicWindow.*;
import evplugin.ev.*;
import evplugin.metadata.*;
import org.jdom.*;

//TODO: auto-replicate down to metadata

/**
 * Adjust Frame-Time mapping
 * @author Johan Henriksson
 */
public class CustomWindow extends BasicWindow implements ActionListener, ChangeListener, ObjectCombo.comboFilterMetaObject
	{
	static final long serialVersionUID=0;

	public static void initPlugin() {}
	static
		{
		BasicWindow.addBasicWindowExtension(new CustomBasic());
		
		EV.personalConfigLoaders.put("CustomWindow",new PersonalConfig()
			{
			public void loadPersonalConfig(Element e)
				{
				try	{new CustomWindow(BasicWindow.getXMLbounds(e));}
				catch (Exception e1) {e1.printStackTrace();}
				}
			public void savePersonalConfig(Element e){}
			});
		
		}
	
	
	//GUI components
	private JButton bAdd=new JButton("Add");
	private JButton bApply=new JButton("Apply");
	private JButton bRefresh=new JButton("Refresh");
	
	
	private ObjectCombo objectCombo=new ObjectCombo(this, true);

	/**
	 * Store down settings for window into personal config file
	 */
	public void windowPersonalSettings(Element root)
		{
		Element e=new Element("CustomWindow");
		setXMLbounds(e);
		root.addContent(e);
		}

	

	/**
	 * Make a new window at default location
	 */
	public CustomWindow()
		{
		this(new Rectangle(100,100,1000,600));
		}
	
	/**
	 * Make a new window at some specific location
	 */
	public CustomWindow(Rectangle bounds)
		{		
		objectCombo.addActionListener(this);

		JPanel treePanel=new JPanel();
		JPanel tablePanel=new JPanel();
		
		JTabbedPane tabs=new JTabbedPane();
		tabs.addTab("Tree", treePanel);
		tabs.addTab("Table", tablePanel);
		
		setLayout(new BorderLayout());
		add(tabs, BorderLayout.CENTER);
		add(objectCombo, BorderLayout.NORTH);
		
		loadData();
		
		//Window overall things
		setTitle(EV.programName+" Custom Data");
		pack();
		setVisible(true);
		setBounds(bounds);
		}

	/**
	 * For combo box - which meta objects to list
	 */
	public boolean comboFilterMetaObjectCallback(MetaObject ob)
		{
		return ob instanceof MetaObjectUnknown;
		}
	/**
	 * Add special options for the combo box
	 */
	public ObjectCombo.Alternative[] comboAddAlternative(final ObjectCombo combo, final Metadata meta)
		{
		return new ObjectCombo.Alternative[]{};
		}

	
	
	
	/**
	 * Load data 
	 */
	public void loadData()
		{
//		inputVector.clear();
		
		MetaObjectUnknown meta=(MetaObjectUnknown)objectCombo.getObject();
		if(meta!=null)
			;
//			for(Pair p:meta.list)
//				addEntry(p.frame, p.frametime);
		
		fillDatapart();
		}
	
	
	
	/**
	 * Save data 
	 */
	public void saveData()
		{
		//should save even exist?
		MetaObjectUnknown meta=(MetaObjectUnknown)objectCombo.getObject();
		if(meta!=null)
			{
//			meta.list.clear();
			
//			for(int i=0;i<inputVector.size();i++)
	//			meta.add((Integer)inputVector.get(i)[0].getValue(), (Double)inputVector.get(i)[1].getValue());
			meta.metaObjectModified=true;
			}
		}
	

	/**
	 * Regenerate UI
	 */
	public void fillDatapart()
		{
		/*
		datapart.removeAll();
		datapart.setLayout(new GridLayout(1+inputVector.size(),2));
		datapart.add(new JLabel("Frame"));
		datapart.add(new JLabel("Time"));
		for(int i=0;i<inputVector.size();i++)
			{
			datapart.add(inputVector.get(i)[0]);
			datapart.add(inputVector.get(i)[1]);
			}
			*/
		setVisible(true);
		}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==objectCombo)
			{
			loadData();
			}
		if(e.getSource()==bAdd)
			{
			
			fillDatapart();
			}
		else if(e.getSource()==bRefresh)
			{
			loadData();
			}
		else if(e.getSource()==bApply)
			{
			saveData();
			loadData();
			}
		
		}
	
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
		{
		
//		fillGraphpart();
		}
	
	/*
	 * (non-Javadoc)
	 * @see client.BasicWindow#dataChanged()
	 */
	public void dataChangedEvent()
		{
		objectCombo.updateObjectList();
		loadData();
		}
	
	
	
	
	}
