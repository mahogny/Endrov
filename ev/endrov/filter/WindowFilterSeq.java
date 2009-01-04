package endrov.filter;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import endrov.basicWindow.*;
import endrov.data.*;
import endrov.ev.*;

import org.jdom.*;

/**
 * ROI Window - Display all ROIs
 * 
 * @author Johan Henriksson
 */
public class WindowFilterSeq extends BasicWindow implements ActionListener
	{
	static final long serialVersionUID=0;
	

	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	public static class ThisBasicHook implements BasicWindowHook, ActionListener
		{
		public void createMenus(BasicWindow w)
			{
			JMenuItem mi=new JMenuItem("Filter Sequence",new ImageIcon(getClass().getResource("labelFS.png")));
			mi.addActionListener(this);
			w.addMenuWindow(mi);
			}
		public void actionPerformed(ActionEvent e) 
			{
			new WindowFilterSeq(null);
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
					new WindowFilterSeq(BasicWindow.getXMLbounds(e),null);
					}
				catch (Exception e1){e1.printStackTrace();}
				}
			public void savePersonalConfig(Element e){}
			});
		}
	
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	private boolean isTemporaryWindow;
	

	private EvComboObjectOne<FilterSeq> objectCombo=new EvComboObjectOne<FilterSeq>(new FilterSeq(),false,true);
	private WidgetFilterSeq wFilterSeq=new WidgetFilterSeq();
	private JMenu mAdd=new JMenu("Add");
		
	
	
	/**
	 * Make a new window at default location
	 */
	public WindowFilterSeq(FilterSeq seq)
		{
		this(new Rectangle(600,300,500,400), seq);
		}
	
	/**
	 * Make a new window at some specific location
	 */
	public WindowFilterSeq(Rectangle bounds, FilterSeq seq)
		{
		isTemporaryWindow=seq!=null; //Changed
		
		//Put GUI together
		setLayout(new BorderLayout());
		if(seq==null)
			{
			objectCombo.addActionListener(this);
			add(objectCombo,BorderLayout.NORTH);
			}
		else
			{
			wFilterSeq.setFilterSeq(seq);
		//	objectCombo=null;
			}
		add(wFilterSeq,BorderLayout.CENTER);
		addMenubar(mAdd);
		wFilterSeq.buildMenu(mAdd);
				
		//Window overall things
		setTitleEvWindow("Filter Sequence");
		packEvWindow();
		setBoundsEvWindow(bounds);
		setVisibleEvWindow(true);
		}
	
	
	
	/**
	 * Store down settings for window into personal config file
	 */
	public void windowPersonalSettings(Element root)
		{
		if(!isTemporaryWindow)
			{
			Element e=new Element("filterseqwindow");
			setXMLbounds(e);
			root.addContent(e);
			}
		}

	/**
	 * Get the currently selected filter sequence or null
	 */
	public FilterSeq getFilterSequence()
		{
		return objectCombo.getSelectedObject();
		}
	
	

	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==objectCombo)
			{
			wFilterSeq.setFilterSeq(objectCombo.getSelectedObject());
			}
		}
	
	public void dataChangedEvent()
		{
		objectCombo.updateList();
		wFilterSeq.buildMenu(mAdd);
		}
	
	public void loadedFile(EvData data){}
	public void freeResources(){}

	}
