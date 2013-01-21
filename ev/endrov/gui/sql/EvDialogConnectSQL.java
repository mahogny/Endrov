package endrov.gui.sql;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JTextField;
import endrov.core.EvSQLConnection;
import endrov.gui.EvSwingUtil;

/**
 * Dialog for choosing one of the available SQL connections
 * 
 * @author Johan Henriksson
 */
public class EvDialogConnectSQL extends JDialog implements ActionListener
	{
	private static final long serialVersionUID = 1L;

	private JComboBox comboDriver;
	private JTextField tfURL=new JTextField();
	private JTextField tfUser=new JTextField();
	private JTextField tfPassword=new JTextField();

	private JButton bOk=new JButton("OK");
	private JButton bCancel=new JButton("Cancel");

	public EvDialogConnectSQL()
		{
		
		Vector<String> classes=new Vector<String>();
		for(String s:EvSQLConnection.getCommonSQLdrivers())
			classes.add(s);
		
		comboDriver=new JComboBox(classes);
		comboDriver.setEditable(true);
		
		setLayout(new GridLayout(5,1));
		add(EvSwingUtil.withLabel("Driver", comboDriver));
		add(EvSwingUtil.withLabel("URL", tfURL));
		add(EvSwingUtil.withLabel("User", tfUser));
		add(EvSwingUtil.withLabel("Password", tfPassword));
		add(EvSwingUtil.layoutEvenHorizontal(bOk, bCancel));
		
		setTitle("Connect to SQL database");
		setModal(true);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		}
	
	

	private boolean responseOK=false;
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==bOk)
			{
			responseOK=true;
			dispose();
			}
		else if(e.getSource()==bCancel)
			{
			dispose();
			}
		}

	
	public static EvSQLConnection openDialog()
		{
		EvDialogConnectSQL dia=new EvDialogConnectSQL();
		if(dia.responseOK)
			{
			EvSQLConnection conn=new EvSQLConnection();
			conn.connDriver=(String)dia.comboDriver.getSelectedItem();
			conn.connURL=dia.tfURL.getText();
			conn.setUserPass(dia.tfUser.getText(), dia.tfPassword.getText());
			return conn;
			}
		else
			return null;
		}
	
	}
