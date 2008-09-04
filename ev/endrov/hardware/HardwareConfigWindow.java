package endrov.hardware;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import org.jdom.*;

import endrov.basicWindow.BasicWindow;
import endrov.basicWindow.BasicWindowExtension;
import endrov.basicWindow.BasicWindowHook;
import endrov.data.EvData;

public class HardwareConfigWindow extends BasicWindow
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
				JMenuItem mi=new JMenuItem("Hardware Manager",new ImageIcon(getClass().getResource("iconWindow.png")));
				mi.addActionListener(this);
				w.addMenuWindow(mi);
				}

			public void actionPerformed(ActionEvent e) 
				{
				new HardwareConfigWindow();
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
	
	
	JButton bAdd=new JButton("Add");
	JButton bRemove=new JButton("Remove");
	JButton bLoad=new JButton("Load");
	JButton bSave=new JButton("Save");
	JButton bAutodetect=new JButton("Autodetect");
	
	
	public HardwareConfigWindow()
		{
		this(new Rectangle(300,400));
		}
	
	public HardwareConfigWindow(Rectangle bounds)
		{
		JList hwList=new JList();
		
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
		
		setLayout(new BorderLayout());
		add(hwList,BorderLayout.CENTER);
		add(bp,BorderLayout.SOUTH);
		
		//Window overall things
		setTitleEvWindow("Hardware Configuration");
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
