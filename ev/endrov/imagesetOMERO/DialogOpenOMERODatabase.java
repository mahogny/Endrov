/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.imagesetOMERO;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import endrov.gui.window.BasicWindow;
import endrov.util.EvSwingUtil;

import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;

import java.util.concurrent.Semaphore;

/**
 * A dialog for connecting to a OMERO database
 * @author Johan Henriksson
 */
public class DialogOpenOMERODatabase extends JDialog implements ActionListener, WindowListener
	{
	static final long serialVersionUID=0; 
	

	/**
	 * Final values. Never null
	 */
	public String dbUrl="localhost", dbUser="root", dbPassword="";
	public int dbPort=4064;
	public boolean dbEncrypted=true;
	
	
	/**
	 * Input fields
	 */
	private JTextField iPort=new JTextField(""+dbPort);
	private JTextField iUrl=new JTextField(dbUrl);
	private JTextField iUser=new JTextField(dbUser);
	private JTextField iPassword=new JPasswordField(dbPassword);
	private JButton bOk=new JButton("Ok");
	private JButton bCancel=new JButton("Cancel");
	private JCheckBox chEncrypted=new JCheckBox("", dbEncrypted);
	
	private Semaphore sem=new Semaphore(0);
	private boolean pressedOk=false;

	/**
	 * Create the dialog. Let a parameter be null if it should not be in, otherwise default value
	 */
	public DialogOpenOMERODatabase(Frame f)
		{
		super(f, "OMERO Login", true);
	
		setLayout(new GridLayout(1, 1));
		add(EvSwingUtil.layoutCompactVertical(
			EvSwingUtil.layoutTableCompactWide(
					new JLabel("URL:"),      iUrl,
					new JLabel("Port:"),     iPort,
					new JLabel("Encrypted:"),  chEncrypted,
					new JLabel("User:"),     iUser,
					new JLabel("Password:"), iPassword
			),
			EvSwingUtil.layoutEvenHorizontal(bOk, bCancel)
			));
		
		bOk.addActionListener(this);
		bCancel.addActionListener(this);
	
		iUrl.addActionListener(this);
		iPort.addActionListener(this);
		iUser.addActionListener(this);
		iPassword.addActionListener(this);
		
		pack();
		Dimension d=getSize();
		d.width=500;
		setSize(d);
		setLocationRelativeTo(f);
	
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		}
	
	public void actionPerformed(ActionEvent e)
		{
		if(e!=null)
			{
			if(e.getSource()==bCancel)
				{
				pressedOk=false;
				dispose();
				}
			else
				{
				dbPort=Integer.parseInt(iPort.getText());
				dbUrl=iUrl.getText();
				dbUser=iUser.getText();
				dbPassword=iPassword.getText();
				dbEncrypted=chEncrypted.isSelected();
				pressedOk=true;
				dispose();
				sem.release();
				}
			
			}
			
		}
	
	/**
	 * Run the dialog
	 */
	public boolean run()
		{
		try
			{
			setVisible(true);
			//iUser.requestFocus();
			sem.acquire();
			}
		catch(InterruptedException e){}
		return pressedOk;
		}
	

	public void connect()
		{
		final JFrame fConn=new JFrame();
		fConn.setLayout(new GridLayout(1,1));
		fConn.add(new JLabel("Connecting..."));
		fConn.pack();
		fConn.setLocationRelativeTo(this);
		fConn.setVisible(true);

		SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
					{
					try
						{
						OMEROConnection connection;
						connection = new OMEROConnection();
						connection.connect(dbUrl, dbPort, dbUser, dbPassword, dbEncrypted);

						System.out.println("got session!!!");
						
						OMEROBasic.disconnectCurrent();
						OMEROBasic.omesession=connection;
						BasicWindow.updateWindows();
						}
					catch (CannotCreateSessionException e)
						{
						BasicWindow.showErrorDialog("Cannot connect: "+e.getMessage());
						e.printStackTrace();
						}
					catch (PermissionDeniedException e)
						{
						BasicWindow.showErrorDialog("Cannot connect, permission denied");
						}
					catch (Exception e)
						{
						BasicWindow.showErrorDialog("General error: "+e.getMessage());
						e.printStackTrace();
						}
					fConn.dispose();
					}
			});
		}
	
	
	public void windowClosing(WindowEvent e) {actionPerformed(null);}
	public void windowActivated(WindowEvent arg0)	{}
	public void windowClosed(WindowEvent arg0) {}
	public void windowDeactivated(WindowEvent arg0)	{}
	public void windowDeiconified(WindowEvent arg0)	{}
	public void windowIconified(WindowEvent arg0) {}
	public void windowOpened(WindowEvent arg0) {}
	}
