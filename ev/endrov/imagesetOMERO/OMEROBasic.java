/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imagesetOMERO;

import endrov.data.*;
import endrov.gui.window.BasicWindow;
import endrov.util.EvSwingUtil;

import java.awt.event.*;

import javax.swing.*;

import omero.ServerError;


public class OMEROBasic 
	{
	public static OMEROConnection omesession=null;
	
	public static void disconnectCurrent()
		{
		if(omesession!=null)
			{
			omesession.disconnect();
			omesession=null;
			}
		}
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvDataMenu.extensions.add(new DataMenuExtension()
			{
			final JMenu miOMERO=new JMenu("OMERO");
			public void buildData(JMenu menu)
				{
				EvSwingUtil.tearDownMenu(miOMERO);
				//This is abuse...

				//Login
				if(omesession==null)
					{
					JMenuItem miLogin=new JMenuItem("Login");
					miOMERO.add(miLogin);
					miLogin.addActionListener(new ActionListener()
						{
						public void actionPerformed(ActionEvent e)
							{
							SwingUtilities.invokeLater(new Runnable()
								{
									public void run()
										{
										DialogOpenOMERODatabase dia=new DialogOpenOMERODatabase(null);
										if(dia.run())
											dia.connect();
										}
								});
							
							
							}
						});
					}
				
				//The rest is only shown when logged in
				if(omesession!=null)
					{
					System.out.println("add logout");
					
					//Log out
					JMenuItem miLogout=new JMenuItem("Logout");
					miOMERO.add(miLogout);
					miLogout.addActionListener(new ActionListener()
						{
						public void actionPerformed(ActionEvent e)
							{
							disconnectCurrent();
							BasicWindow.updateWindows();
							}
						});
					
					JMenuItem miImport=new JMenuItem("Import dataset");
					miOMERO.add(miImport);
					miImport.addActionListener(new ActionListener()
						{
						public void actionPerformed(ActionEvent e)
							{
							try
								{
								new DialogImportDataset();
								}
							catch (ServerError e1)
								{
								e1.printStackTrace();
								}
							}
						});
					
					/*
					
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
					
					*/
					
					}
				
				
				}
			public void buildOpen(JMenu menu)
				{
				miOMERO.setIcon(new ImageIcon(OMEROBasic.class.getResource("iconOMERO.png")));
				addMetamenu(menu,miOMERO);
				}
			
			public void buildSave(JMenu menu, final EvData meta)
				{
				/*
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
