/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.db;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;
import endrov.flow.ui.FlowPanel;
import endrov.util.EvSwingUtil;

/**
 * Flow unit: connect to an SQL database
 * @author Johan Henriksson
 *
 */
public class FlowUnitConnectSQL extends FlowUnitBasic
	{
	private static final String metaType="connectSQL";
	
	public static final ImageIcon icon=new ImageIcon(CategoryInfo.class.getResource("silkDatabaseConnect.png"));
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		FlowUnitDeclaration decl=new FlowUnitDeclaration(CategoryInfo.name,"Connect to SQL",metaType,FlowUnitConnectSQL.class, 
				icon,"Connect to an SQL database");
		Flow.addUnitType(decl);
		FlowType.registerSuggestCreateUnitInput(Connection.class, decl);
		}
	
	//jdbc:postgresql://localhost/booktown
	//jdbc:mysql://localhost/coffeebreak
	
	private String connDriver="org.postgresql.Driver";
	private String connURL="jdbc:postgresql://localhost/mydb";
	private String connUser="";
	private String connPass="";

	public FlowUnitConnectSQL()
		{
		textPosition=TEXTABOVE;
		}
	
	private void setValues(String connDriver, String connURL, String connUser, String connPass)
		{
		this.connDriver=connDriver;
		this.connURL=connURL;
		this.connUser=connUser;
		this.connPass=connPass;
		}
	
	public String toXML(Element e)
		{
		Element eDriver=new Element("driver");
		Element eUrl=new Element("URL");
		Element eUser=new Element("user");
		Element ePass=new Element("pass");
		
		eDriver.setText(connDriver);
		eUrl.setText(connURL);
		eUser.setText(connUser);
		ePass.setText(connPass);
		
		e.addContent(eDriver);
		e.addContent(eUrl);
		e.addContent(eUser);
		e.addContent(ePass);
		
		return metaType;
		}

	public void fromXML(Element e)
		{
		connDriver=e.getChildText("driver");
		connURL=e.getChildText("URL");
		connUser=e.getChildText("user");
		connPass=e.getChildText("pass");
		}

	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);

    Class.forName(connDriver).newInstance();
    Connection conn = DriverManager.getConnection(connURL, connUser, connPass);      
//	      conn.close();
		
		lastOutput.put("connection", conn);
		}
	
	
	/**
	 * The special swing component for this unit
	 * @author Johan Henriksson
	 */
	private class TotalPanel extends JPanel implements ChangeListener, ActionListener
		{
		private static final long serialVersionUID = 1L;
		
		private JComboBox comboDriver;
		private JTextField tfURL=new JTextField(connURL);
		private JTextField tfUser=new JTextField(connUser);
		private JTextField tfPassword=new JTextField(connPass);
		
		public TotalPanel()
			{
			setLayout(new GridLayout(4,1));
			
			Vector<String> classes=new Vector<String>();
			classes.add("org.postgresql.Driver");
			classes.add("com.mysql.jdbc.Driver");
			
			comboDriver=new JComboBox(classes);
			comboDriver.setEditable(true);
			comboDriver.setSelectedItem(connDriver);
			
			add(EvSwingUtil.withLabel("Driver", comboDriver));
			add(EvSwingUtil.withLabel("URL", tfURL));
			add(EvSwingUtil.withLabel("User", tfUser));
			add(EvSwingUtil.withLabel("Password", tfPassword));
			
			comboDriver.addActionListener(this);
			EvSwingUtil.textAreaChangeListener(tfURL, this);
			EvSwingUtil.textAreaChangeListener(tfUser, this);
			EvSwingUtil.textAreaChangeListener(tfPassword, this);
			
			setOpaque(false);
			}
		
		public void copyValues()
			{
			setValues((String)comboDriver.getSelectedItem(), tfURL.getText(), tfUser.getText(), tfPassword.getText());
			}

		public void stateChanged(ChangeEvent arg0)
			{
			copyValues();
			}

		public void actionPerformed(ActionEvent arg0)
			{
			copyValues();
			}
				
		}


	public Component getGUIcomponent(final FlowPanel p)
		{
		return new TotalPanel();
		}

	
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("connection", FlowType.TCONNECTION);
		}
	


	@Override
	public Color getBackground()
		{
		return CategoryInfo.bgColor;
		}

	@Override
	public String getBasicShowName()
		{
		return "Connect to SQL";
		}

	@Override
	public ImageIcon getIcon()
		{
		return CategoryInfo.icon;
		}

	
	
	}
