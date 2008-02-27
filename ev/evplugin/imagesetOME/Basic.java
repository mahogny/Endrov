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