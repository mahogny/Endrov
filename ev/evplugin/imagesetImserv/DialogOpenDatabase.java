package evplugin.imagesetImserv;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


import java.util.concurrent.Semaphore;

/**
 * A dialog for connecting to a database
 * @author Johan Henriksson
 */
public class DialogOpenDatabase extends JDialog implements ActionListener, WindowListener
	{
	static final long serialVersionUID=0; 
	
	private JTextField iPort=new JTextField();
	private JTextField iUrl=new JTextField();
	private JTextField iUser=new JTextField();
	private JTextField iPassword=new JPasswordField();
	private JButton bOk=new JButton("Ok");
	private JButton bCancel=new JButton("Cancel");
	
	private Semaphore sem=new Semaphore(0);
	private boolean pressedOk=false;

	/**
	 * Final values. Never null
	 */
	public String dbUrl="localhost", dbUser="", dbPassword="";
	public int dbPort=evplugin.imagesetImserv.service.Daemon.PORT;
	
	/**
	 * Create the dialog. Let a parameter be null if it should not be in, otherwise default value
	 */
	public DialogOpenDatabase(Frame f)
		{
		super(f, "ImServ Login", true);
		setLayout(new BorderLayout());
	
		int numline=4;
	
		setLayout(new GridLayout(numline+1,2));
	
		iUrl.setText(dbUrl);
		iPort.setText(""+dbPort);
		iUser.setText(dbUser);
		iPassword.setText(dbPassword);
		
		add(new JLabel("URL:"));      add(iUrl);
		add(new JLabel("Port:"));     add(iPort);
		add(new JLabel("User:"));     add(iUser);
		add(new JLabel("Password:")); add(iPassword);
	
		add(bOk);
		add(bCancel);
		bOk.addActionListener(this);
		bCancel.addActionListener(this);
	
		pack();
		Dimension d=getSize();
		d.width=500;
		setSize(d);
		setLocationRelativeTo(f);
	
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
		{
		if(e!=null && e.getSource()==bOk)
			pressedOk=true;
		dispose();
		dbPort=Integer.parseInt(iPort.getText());
		dbUrl=iUrl.getText();
		dbUser=iUser.getText();
		dbPassword=iPassword.getText();
		sem.release();
		}
	
	
	/**
	 * Run the dialog
	 */
	public EvImserv.EvImservSession run() throws Exception
		{
		try
			{
			setVisible(true);
			sem.acquire();
			}
		catch(InterruptedException e){}
		if(pressedOk)
			{
			EvImserv.EvImservSession ome=new EvImserv.EvImservSession(
					iUrl.getText(),iUser.getText(),iPassword.getText(),Integer.parseInt(iPort.getText()));
			if(ome==null)
				throw new Exception("Wrong user/pass");
			return ome;
			}
		else
			return null;
		}
	
	
	public void windowClosing(WindowEvent e) {actionPerformed(null);}
	public void windowActivated(WindowEvent arg0)	{}
	public void windowClosed(WindowEvent arg0) {}
	public void windowDeactivated(WindowEvent arg0)	{}
	public void windowDeiconified(WindowEvent arg0)	{}
	public void windowIconified(WindowEvent arg0) {}
	public void windowOpened(WindowEvent arg0) {}
	}
