package evplugin.filter;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import evplugin.data.*;
import evplugin.ev.*;
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
		
		
		
		EV.personalConfigLoaders.put("filterseqwindow",new PersonalConfig()
			{
			public void loadPersonalConfig(Element e)
				{
				try
					{
					new WindowFilterSeq(BasicWindow.getXMLbounds(e));
					}
				catch (Exception e1){e1.printStackTrace();}
				}
			public void savePersonalConfig(Element e){}
			});
		}
	
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	
	private ObjectCombo objectCombo=new ObjectCombo(this, true);
	private WidgetFilterSeq wFilterSeq=new WidgetFilterSeq();
	private JMenu mAdd=new JMenu("Add");
	
	
	public Alternative[] comboAddAlternative(ObjectCombo combo)
		{
		return new Alternative[]{};
		}
	public Alternative[] comboAddObjectAlternative(ObjectCombo combo, final EvData meta)
		{
		Alternative a=new Alternative(meta,null,"New",new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
				{
				FilterSeq seq=new FilterSeq();
				meta.addMetaObject(seq);
				}
			});
		return new Alternative[]{a};
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
		this(new Rectangle(600,300,500,400));
		}
	
	/**
	 * Make a new window at some specific location
	 */
	public WindowFilterSeq(Rectangle bounds)
		{
		objectCombo.addActionListener(this);
		
		//Put GUI together
		setLayout(new BorderLayout());
		add(objectCombo,BorderLayout.NORTH);
		add(wFilterSeq,BorderLayout.CENTER);
		addMenubar(mAdd);
		wFilterSeq.buildMenu(mAdd);
				
		//Window overall things
		setTitle(EV.programName+" Filter Sequence");
		pack();
		setBounds(bounds);
		setVisible(true);
		}
	
	
	
	/**
	 * Store down settings for window into personal config file
	 */
	public void windowPersonalSettings(Element root)
		{
		Element e=new Element("filterseqwindow");
		setXMLbounds(e);
		root.addContent(e);
		}

	/**
	 * Get the currently selected filter sequence or null
	 */
	public FilterSeq getFilterSequence()
		{
		return (FilterSeq)objectCombo.getObject();
		}
	
	

	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==objectCombo)
			{
			wFilterSeq.setFilterSeq((FilterSeq)objectCombo.getObject());
			}
		}
	
	public void dataChangedEvent()
		{
		objectCombo.updateObjectList();
		wFilterSeq.buildMenu(mAdd);
		}
	
	
	}
