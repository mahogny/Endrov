package endrov.recording.recmetMultidim;


import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import org.jdom.*;

import endrov.basicWindow.*;
import endrov.data.EvData;

/**
 * Multi-dimension acquisition
 * @author Johan Henriksson 
 */
public class RecWindowMultiDim extends BasicWindow
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
					JMenuItem mi=new JMenuItem("Multi-dim acq Window",new ImageIcon(getClass().getResource("tangoCamera.png")));
					mi.addActionListener(this);
					w.addMenuWindow(mi);
					}
	
				public void actionPerformed(ActionEvent e) 
					{
					new RecWindowMultiDim();
					}
	
				public void buildMenu(BasicWindow w){}
				}
			});
		
		}
	
	

	public RecWindowMultiDim()
		{
		this(new Rectangle(400,300));
		}
	
	public RecWindowMultiDim(Rectangle bounds)
		{

		
		

		
		
		setLayout(new BorderLayout());
//		add(mcombo,BorderLayout.SOUTH);
	//	add(drawArea,BorderLayout.CENTER);
		
		
		
		//Window overall things
		setTitleEvWindow("Camera Control");
		packEvWindow();
		setVisibleEvWindow(true);
		setBoundsEvWindow(bounds);
		setResizable(false);
		}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void dataChangedEvent()
		{
		
		}

	public void loadedFile(EvData data){}

	public void windowSavePersonalSettings(Element e)
		{
		
		} 
	public void freeResources()
		{
		}
	
	
	}
