package evplugin.imagesetOME;

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
	static final long serialVersionUID=0; //wtf
	
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
	public String dbUrl="localhost", dbUser="root", dbPassword="";
	public int dbPort=1099;
	
	/**
	 * Create the dialog. Let a parameter be null if it should not be in, otherwise default value
	 */
	public DialogOpenDatabase(Frame f)
		{
		super(f, "OME Login", true);
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
	public EVOME run()
		{
		try
			{
			setVisible(true);
			sem.acquire();
			}
		catch(InterruptedException e){}
		if(pressedOk)
			{
			EVOME ome=new EVOME();
			if(ome.login(this))
				return ome;
			else
				return null;
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
