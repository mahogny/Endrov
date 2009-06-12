package endrov.recording.recWindow;


import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.*;

import org.jdom.*;

import endrov.basicWindow.BasicWindow;
import endrov.basicWindow.BasicWindowExtension;
import endrov.basicWindow.BasicWindowHook;
import endrov.data.EvData;

/**
 * Microscope Control Window
 * @author Johan Henriksson 
 */
public class MicroscopeWindow extends BasicWindow implements ActionListener
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;

	
	public static void initPlugin() {}
	static
		{
		BasicWindow.addBasicWindowExtension(new BasicWindowExtension()
			{
			public void newBasicWindow(BasicWindow w)
				{
				w.basicWindowExtensionHook.put(this.getClass(),new Hook());
				}
			class Hook implements BasicWindowHook, ActionListener
			{
			public void createMenus(BasicWindow w)
				{
				JMenuItem mi=new JMenuItem("Microscope Control",new ImageIcon(getClass().getResource("iconWindow.png")));
				mi.addActionListener(this);
				w.addMenuWindow(mi);
				}

			public void actionPerformed(ActionEvent e) 
				{
				new MicroscopeWindow();
				}

			public void buildMenu(BasicWindow w){}
			}
			});
		
		
/*		EV.personalConfigLoaders.put("consolewindow",new PersonalConfig()
			{
			public void loadPersonalConfig(Element e)
				{
				try
					{
					int x=e.getAttribute("x").getIntValue();
					int y=e.getAttribute("y").getIntValue();
					int w=e.getAttribute("w").getIntValue();
					int h=e.getAttribute("h").getIntValue();
					new ConsoleWindow(x,y,w,h);
					}
				catch (DataConversionException e1)
					{
					e1.printStackTrace();
					}
				}
			public void savePersonalConfig(Element e){}
			});
			*/
		}
	
	public static List<Extension> extensions=new Vector<Extension>();
	public static void addMicroscopeWindowExtension(Extension e)
		{
		extensions.add(e);
		}

	/******************************************************************************************************
	 *                               Extension                                                            *
	 *****************************************************************************************************/

	public static interface Extension
		{
		public ExtensionInstance getInstance();
		public abstract String getName();
		}
	

	public static abstract class ExtensionInstance extends JComponent
		{
		private static final long serialVersionUID = 1L;
		public abstract void dataChangedEvent();
		}
	
	
	public TreeMap<String,ExtensionInstance> extensionInstance=new TreeMap<String,ExtensionInstance>();
	public void addMicroscopeWindowExtensionInstance(String name, ExtensionInstance e)
		{
		extensionInstance.put(name,e);
		}
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	
	private JComboBox mcombo;
	public JComponent centerp=null;
	
	
	
	
	public MicroscopeWindow()
		{
		this(null);
		}
	
	public MicroscopeWindow(Rectangle bounds)
		{
		for(Extension e:extensions)
			{
			ExtensionInstance ei=e.getInstance();
			addMicroscopeWindowExtensionInstance(e.getName(), ei);
			}
		
		mcombo=new JComboBox(new Vector<String>(extensionInstance.keySet()));
		
		
		setLayout(new BorderLayout());
		add(mcombo,BorderLayout.NORTH);
		setCenterP();
		//String curMode=(String)mcombo.getSelectedItem();
		//add(extensions.get(curMode).addControls(),BorderLayout.CENTER);

		mcombo.addActionListener(this);

		
		//Window overall things
		setTitleEvWindow("Microscope Control");
		packEvWindow();
		setVisibleEvWindow(true);
		setBoundsEvWindow(bounds);
		}
	
	
	public void setCenterP()
		{
		if(centerp!=null)
			remove(centerp);
		
		String curMode=(String)mcombo.getSelectedItem();
		centerp=extensionInstance.get(curMode);
		add(centerp,BorderLayout.CENTER);

		revalidate();
		packEvWindow();

		System.out.println("here");
		}
	
	public void actionPerformed(ActionEvent e)
		{
		setCenterP();
		}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void dataChangedEvent()
		{
		if(mcombo!=null)
			{
			String curMode=(String)mcombo.getSelectedItem();
			extensionInstance.get(curMode).dataChangedEvent();
			}
		}

	public void loadedFile(EvData data){}

	public void windowSavePersonalSettings(Element e)
		{
		} 
	public void freeResources(){}
	
	
	}
