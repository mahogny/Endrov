/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.lightpathWindow;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import org.jdom.Element;

import endrov.basicWindow.BasicWindow;
import endrov.basicWindow.BasicWindowExtension;
import endrov.basicWindow.BasicWindowHook;
import endrov.data.EvData;

/**
 * 
 * @author Johan Henriksson
 *
 */
public class LightpathWindow extends BasicWindow
	{
	private static final long serialVersionUID = 1L;

	
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
					JMenuItem mi=new JMenuItem("Lightpath",new ImageIcon(getClass().getResource("jhLightpathWindow.png")));
					mi.addActionListener(this);
					BasicWindow.addMenuItemSorted(w.getCreateMenuWindowCategory("Recording"), mi);
					}
	
				public void actionPerformed(ActionEvent e) 
					{
					new LightpathWindow();
					}
	
				public void buildMenu(BasicWindow w){}
				}
			});
		
		}
	
	
	LightpathView view=new LightpathView();
	
	public LightpathWindow()
		{
		this(new Rectangle(400,300));
		}

	public LightpathWindow(Rectangle bounds)
		{

		
		setLayout(new BorderLayout());
		add(view,BorderLayout.CENTER);
		
		
		
		//Window overall things
		setTitleEvWindow("Lightpath");
		packEvWindow();
		setVisibleEvWindow(true);
		setBoundsEvWindow(bounds);
		//setResizable(false);		
		}
	
	@Override
	public void dataChangedEvent()
		{
		}

	@Override
	public void freeResources()
		{
		}

	@Override
	public void loadedFile(EvData data)
		{
		}

	@Override
	public void windowSavePersonalSettings(Element e)
		{
		}
	
	public static void main(String[] args)
		{
		new LightpathWindow();
		}

	}
