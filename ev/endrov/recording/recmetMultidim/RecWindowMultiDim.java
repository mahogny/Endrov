package endrov.recording.recmetMultidim;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import org.jdom.*;

import endrov.basicWindow.*;
import endrov.data.EvData;
import endrov.util.EvSwingUtil;

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
		RecWidgetSlices wslices=new RecWidgetSlices();
		RecWidgetTimes wtimes=new RecWidgetTimes();
		RecWidgetChannels wchans=new RecWidgetChannels();
		RecWidgetOrder worder=new RecWidgetOrder();
		RecWidgetPositions wpos=new RecWidgetPositions();
		RecWidgetRecDesc wdesc=new RecWidgetRecDesc();
		RecWidgetAcquireMultidim wacq=new RecWidgetAcquireMultidim();
		

		JPanel leftright=new JPanel(new GridLayout(1,2));
		leftright.add(EvSwingUtil.layoutCompactVertical(wslices,worder,wpos));
		leftright.add(EvSwingUtil.layoutCompactVertical(wtimes,wacq));
		
		setLayout(new BorderLayout());
		add(EvSwingUtil.layoutCompactVertical(
				leftright,wchans
				),BorderLayout.NORTH);
		add(
				wdesc
				,BorderLayout.CENTER);

		
		//Window overall things
		setTitleEvWindow("Multidimensional acquisition");
		packEvWindow();
		setVisibleEvWindow(true);
//		setBoundsEvWindow(bounds);
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
	
	public static void main(String[] args)
		{
		new RecWindowMultiDim();
		
		}
	
	}
