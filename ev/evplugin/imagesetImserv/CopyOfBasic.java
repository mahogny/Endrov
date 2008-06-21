package evplugin.imagesetImserv;

/*
import evplugin.basicWindow.BasicWindow;
import evplugin.data.*;

import java.awt.event.*;
import javax.swing.*;

*/
public class CopyOfBasic 
	{
	/*
	public static void initPlugin() {}
	static
		{
		EvDataMenu.extensions.add(new DataMenuExtension()
			{

			public void buildOpen(JMenu menu)
				{
				final JMenu miOME=new JMenu("ImServ");
				addMetamenu(menu,miOME);

				//Login
				JMenuItem miLogin=new JMenuItem("Login");
				miOME.add(miLogin);
				miLogin.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						DialogOpenDatabase dia=new DialogOpenDatabase(null);
						EvImserv.EvImservSession ome=dia.run();
						EvImserv.sessions.add(ome);
						BasicWindow.updateWindows();
						}
					});
				
				//The rest is only shown when logged in
				for(final EvImserv.EvImservSession evimsession:EvImserv.sessions)
					{
					
					
					//Log out & browser
					JMenuItem miLogout=new JMenuItem("Logout");
					miOME.add(miLogout);
					miLogout.addActionListener(new ActionListener()
						{
						public void actionPerformed(ActionEvent e)
							{
//							evimsession.disconnect(); //TODO!!!
							EvImserv.sessions.remove(evimsession);
							BasicWindow.updateWindows();
							}
						});
					
					//Browser
					JMenuItem miBrowser=new JMenuItem("Browse");
					miOME.add(miBrowser);
					miBrowser.addActionListener(new ActionListener()
						{
						public void actionPerformed(ActionEvent e)
							{
							new evplugin.imagesetImserv.service.ImservClientPane(evimsession.conn);
							}
						});
					
					
								
					}
				}
			
			public void buildSave(JMenu menu, final EvData meta)
				{
				}
			});
		}
		*/
	
	}