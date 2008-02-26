package evplugin.imagesetOME;

import evplugin.basicWindow.BasicWindow;
import evplugin.data.*;

import java.awt.event.*;
import javax.swing.*;


public class Basic 
	{
	public static EVOME omesession=null;
	
	public static void initPlugin() {}
	static
		{
		EvDataBasic.extensions.add(new DataMenuExtension()
			{

			public void buildOpen(JMenu menu)
				{
				final JMenu miOME=new JMenu("OME");
				addMetamenu(menu,miOME);

				//Login
				JMenuItem miLogin=new JMenuItem("Login");
				miOME.add(miLogin);
				miLogin.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						DialogOpenDatabase dia=new DialogOpenDatabase(null);
						EVOME ome=dia.run();
						if(ome!=null)
							{
							omesession=ome;
							BasicWindow.updateWindows();
							}
						}
					});
				
				//Logout: only shown when logged in
				if(omesession!=null)
					{
					JMenuItem miLogout=new JMenuItem("Logout");
					miOME.add(miLogout);
					miLogout.addActionListener(new ActionListener()
						{
						public void actionPerformed(ActionEvent e)
							{
							omesession=null;
							BasicWindow.updateWindows();
							}
						});
					}
				
				
				
				
				
				
				}
			
			public void buildSave(JMenu menu, final EvData meta)
				{/*
				if(meta instanceof NamebasedImageset)
					{
					JMenuItem miSetup=new JMenuItem("Setup");
					menu.add(miSetup);
					miSetup.addActionListener(new ActionListener()
						{
						public void actionPerformed(ActionEvent e)
							{((NamebasedImageset)meta).setup();}
						});	
					}*/
				}
			});
		
		}
	
	}