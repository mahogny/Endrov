package endrov.recording.recWindow;


import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
public class MicroscopeWindow extends BasicWindow
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
		
		//addMicroscopeWindowExtension("Manual", new ManualExtension());
		
		
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
	
	public static TreeMap<String,Extension> extensions=new TreeMap<String,Extension>();
	public static void addMicroscopeWindowExtension(String name, Extension e)
		{
		extensions.put(name,e);
		}
	
	public static interface Extension
		{
		public JComponent addControls();
		
		}
	
	
	
	JButton bAdd=new JButton("Add");
	JButton bRemove=new JButton("Remove");
	JButton bLoad=new JButton("Load");
	JButton bSave=new JButton("Save");
	JButton bAutodetect=new JButton("Autodetect");
	
	
	
	public MicroscopeWindow()
		{
		this(new Rectangle(400,300));
		}
	
	public MicroscopeWindow(Rectangle bounds)
		{
		
		
		/*
		Vector<HWListItem> hwNames=new Vector<HWListItem>();
	
		
		
		JList hwList=new JList(hwNames);
		
		JPanel bpu=new JPanel(new GridLayout(1,2));
		bpu.add(bAdd);
		bpu.add(bRemove);
		JPanel bpl=new JPanel(new GridLayout(1,3));
		bpl.add(bLoad);
		bpl.add(bSave);
		bpl.add(bAutodetect);

		JPanel bp=new JPanel(new GridLayout(2,1));
		bp.add(bpu);
		bp.add(bpl);
		
		add(hwList,BorderLayout.CENTER);
		add(bp,BorderLayout.SOUTH);
		*/

		
		JComboBox mcombo=new JComboBox(new Vector<String>(extensions.keySet()));
		String curMode=(String)mcombo.getSelectedItem();
		
		
		
		setLayout(new BorderLayout());
		add(mcombo,BorderLayout.NORTH);
		add(extensions.get(curMode).addControls(),BorderLayout.CENTER);
		
		//Window overall things
		setTitleEvWindow("Microscope Control");
		packEvWindow();
		setVisibleEvWindow(true);
		setBoundsEvWindow(bounds);
		}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void dataChangedEvent()
		{
		// TODO Auto-generated method stub
		
		}

	public void loadedFile(EvData data){}

	public void windowPersonalSettings(Element e)
		{
		// TODO Auto-generated method stub
		
		} 
	
	/*
	public static void main(String[] arg)
		{
		new HardwareConfigWindow();
		}
*/	
	
	}
