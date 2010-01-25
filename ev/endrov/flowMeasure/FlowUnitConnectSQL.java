/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMeasure;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnit;
import endrov.flow.FlowUnitDeclaration;
import endrov.flow.ui.FlowPanel;
import endrov.util.EvSwingUtil;

/**
 * Flow unit: connect to an SQL database
 * @author Johan Henriksson
 *
 */
public class FlowUnitConnectSQL extends FlowUnit
	{
	private static final String metaType="connectSQL";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		FlowUnitDeclaration decl=new FlowUnitDeclaration(CategoryInfo.name,"Connect to SQL",metaType,FlowUnitConnectSQL.class, 
				CategoryInfo.icon,"Connect to an SQL database");
		Flow.addUnitType(decl);
		FlowType.registerSuggestCreateUnitInput(Boolean.class, decl);
		}
	
	//jdbc:postgresql://localhost/booktown
	//jdbc:mysql://localhost/coffeebreak
	
	private String connDriver="org.postgresql.Driver";
	private String connURL="jdbc:postgresql://localhost/mydb";
	private String connUser="";
	private String connPass="";

	private void setValues(String connDriver, String connURL, String connUser, String connPass)
		{
		
		}
	
	public String toXML(Element e)
		{
		return metaType;
		}

	public void fromXML(Element e)
		{
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
		types.put("connection", FlowType.flowTypeConnection);
		}
	
	
	public Dimension getBoundingBox(Component comp, Flow flow)
		{
		int w=fm.stringWidth(getLabel());
		Dimension d=new Dimension(3+w+3+comp.getWidth()+4,comp.getHeight()+2);
		return d;
		}
	
	public void paint(Graphics g, FlowPanel panel, Component comp)
		{
		Dimension d=getBoundingBox(comp, panel.getFlow());
		
		g.setColor(Color.GREEN);
		g.fillRect(x,y,d.width,d.height);
		g.setColor(getBorderColor(panel));
		g.drawRect(x,y,d.width,d.height);
		g.setColor(getTextColor());
		g.drawString(getLabel(), x+3, y+d.height/2+fonta/2);
		
		helperDrawConnectors(g, panel, comp, getBoundingBox(comp, panel.getFlow()));
		}

	public boolean mouseHoverMoveRegion(int x, int y, Component comp, Flow flow)
		{
		Dimension dim=getBoundingBox(comp, flow);
		return x>=this.x && y>=this.y && x<=this.x+dim.width && y<=this.y+dim.height;
		}

	

	
	public void editDialog()
		{
		}

	
	public Collection<FlowUnit> getSubUnits(Flow flow)
		{
		return Collections.singleton((FlowUnit)this);
		}

	
	private String getLabel()
		{
		return "Connect to SQL";
		}
	
	
	public int getGUIcomponentOffsetX()
		{
		int w=fm.stringWidth(getLabel());
		return 3+w+3;
		}
	public int getGUIcomponentOffsetY(){return 1;}

	
	
	}
