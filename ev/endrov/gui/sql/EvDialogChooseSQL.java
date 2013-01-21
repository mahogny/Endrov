package endrov.gui.sql;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;

import endrov.core.EvSQLConnection;
import endrov.gui.EvSwingUtil;
import endrov.gui.window.EvBasicWindow;

/**
 * Dialog for choosing one of the available SQL connections
 * 
 * @author Johan Henriksson
 */
public class EvDialogChooseSQL extends JDialog implements ActionListener
	{
	private static final long serialVersionUID = 1L;

	private JButton bOk=new JButton("OK");
	private JButton bCancel=new JButton("Cancel");
	private JComboBox cbConnections=new JComboBox();
	
	public EvDialogChooseSQL()
		{
		bOk.addActionListener(this);
		bCancel.addActionListener(this);
		
		for(EvSQLConnection c:EvSQLGUI.openConnections)
			cbConnections.addItem(c);
		
		if(cbConnections.getItemCount()==0)
			{
			EvBasicWindow.showErrorDialog("There are no open SQL connections");
			dispose();
			return;
			}
		
		setLayout(new BorderLayout());
		add(EvSwingUtil.layoutEvenVertical(
				cbConnections,
				EvSwingUtil.layoutEvenHorizontal(
						bOk, bCancel
						)
				));
		
		setTitle("Choose SQL database");
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
		EvDialogChooseSQL dia=new EvDialogChooseSQL();
		if(dia.responseOK)
			return (EvSQLConnection)dia.cbConnections.getSelectedItem();
		else
			return null;
		}
	
	}
