/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imagesetOME;

import endrov.basicWindow.BasicWindow;
import endrov.data.*;

import java.awt.event.*;
import javax.swing.*;


public class Basic 
	{
	public static EVOME omesession=null;
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvDataMenu.extensions.add(new DataMenuExtension()
			{
			public void buildData(JMenu menu)
				{
				
				}
			public void buildOpen(JMenu menu)
				{
				final JMenu miOME=new JMenu("ome");
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
				
				//The rest is only shown when logged in
				if(omesession!=null)
					{
					//Log out
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
					
					//List projects
					for(ome.model.containers.Project p:omesession.getProjectList())
						{
						final JMenu miProject=new JMenu(""+p.getName());
						miOME.add(miProject);
						//List datasets
						for(ome.model.containers.Dataset ds:omesession.getDatasets(p))
							{
							final JMenu miDS=new JMenu(""+ds.getName());
							miProject.add(miDS);
							//List images
							for(final ome.model.core.Image im:omesession.getImages(ds))
								{
								final JMenuItem miLoadImageset=new JMenuItem(""+im.getName());
								miDS.add(miLoadImageset);
								miLoadImageset.addActionListener(new ActionListener()
									{
									public void actionPerformed(ActionEvent e)
										{
										EvData data=new EvData();
										data.io=new EvIODataOME(data, omesession, im);
										EvData.registerOpenedData(data);
										}
									});
								
								
								}
							}
						}
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
