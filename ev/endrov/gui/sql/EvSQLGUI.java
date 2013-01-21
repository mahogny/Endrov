package endrov.gui.sql;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import endrov.core.EvSQLConnection;
import endrov.core.log.EvLog;
import endrov.data.EvData;
import endrov.data.gui.DataMenuExtension;
import endrov.data.gui.EvDataMenu;
import endrov.gui.EvSwingUtil;
import endrov.gui.window.EvBasicWindow;


/**
 * User interface routines for active SQL connections, and bookmarks
 * 
 * @author Johan Henriksson
 *
 */
public class EvSQLGUI
	{

	public static Vector<EvSQLConnection> openConnections=new Vector<EvSQLConnection>();

	

	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvDataMenu.extensions.add(new DataMenuExtension()
			{
			final JMenu miOMERO=new JMenu("SQL");
			
			public void buildData(JMenu menu)
				{
				EvSwingUtil.tearDownMenu(miOMERO);
				//This is abuse...

				//Login
				JMenuItem miLogin=new JMenuItem("Connect new...");
				miOMERO.add(miLogin);
				miLogin.addActionListener(new ActionListener()
					{
					public void actionPerformed(ActionEvent e)
						{
						SwingUtilities.invokeLater(new Runnable()
							{
							public void run()
								{
								EvSQLConnection conn=EvDialogConnectSQL.openDialog();
								if(conn!=null)
									{
									try
										{
										conn.connect();
										openConnections.add(conn);
										EvBasicWindow.updateWindows();
										}
									catch (SQLException e)
										{
										EvBasicWindow.showErrorDialog("Could not connect: "+e);
										EvLog.printError(e);
										}
									}
								}
							});
						}
					});

				for(final EvSQLConnection conn:EvSQLGUI.openConnections)
					{

					//Log out
					JMenuItem miLogout=new JMenuItem("Disconnect "+conn);
					miOMERO.add(miLogout);
					miLogout.addActionListener(new ActionListener()
						{
						public void actionPerformed(ActionEvent e)
							{
							try
								{
								conn.disconnect();
								EvSQLGUI.openConnections.remove(conn);
								EvBasicWindow.updateWindows();
								}
							catch (SQLException e1)
								{
								EvBasicWindow.showErrorDialog("Could not disconnect: "+e1);
								EvLog.printError(e1);
								}
							}
						});
					
					
					}
				
				
				}
				
				
			public void buildOpen(JMenu menu)
				{
//				miOMERO.setIcon(new ImageIcon(EvSQLGUI.class.getResource("iconOMERO.png")));
				addMetamenu(menu,miOMERO);
				}
			
			public void buildSave(JMenu menu, final EvData meta)
				{
				}
			});
		
		}

	}
