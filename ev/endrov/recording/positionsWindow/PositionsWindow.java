/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.positionsWindow;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import org.jdom.*;

import endrov.basicWindow.*;
import endrov.data.EvData;
import endrov.util.EvSwingUtil;

/**
 * Window used to create and remove positions
 * 
 * @author Kim Nordlöf, Erik Vernersson
 */
public class PositionsWindow extends BasicWindow
	{
	/******************************************************************************************************
	 * Static *
	 *****************************************************************************************************/
	static final long serialVersionUID = 0;

	private WidgetPositions wpos = new WidgetPositions();

	public PositionsWindow()
		{
		this(new Rectangle(300, 300));
		}

	public PositionsWindow(Rectangle bounds)
		{
		setLayout(new BorderLayout());
		add(EvSwingUtil.layoutCompactHorizontal(wpos));

		// Window overall things
		setTitleEvWindow("Positions");
		setBoundsEvWindow(300, 300);
		setVisibleEvWindow(true);
		}

	public void dataChangedEvent()
		{
		wpos.dataChangedEvent();
		}

	public void loadedFile(EvData data)
		{
		}

	public void windowSavePersonalSettings(Element e)
		{

		}

	public void freeResources()
		{
		}

	public static void main(String[] args)
		{
		new PositionsWindow();

		}

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin()
		{
		}

	static
		{
		BasicWindow.addBasicWindowExtension(new BasicWindowExtension()
			{
				public void newBasicWindow(BasicWindow w)
					{
					w.basicWindowExtensionHook.put(this.getClass(), new Hook());
					}

				class Hook implements BasicWindowHook, ActionListener
					{
					public void createMenus(BasicWindow w)
						{
						JMenuItem mi = new JMenuItem("Positions", new ImageIcon(getClass()
								.getResource("jhMultidimWindow.png")));
						mi.addActionListener(this);
						BasicWindow.addMenuItemSorted(
								w.getCreateMenuWindowCategory("Recording"), mi);
						}

					public void actionPerformed(ActionEvent e)
						{
						new PositionsWindow();
						}

					public void buildMenu(BasicWindow w)
						{
						}
					}
			});

		}

	}
