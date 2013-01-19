/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.objects;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdom.Element;

import endrov.data.EvContainer;
import endrov.data.EvObject;
import endrov.data.EvPath;
import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnit;
import endrov.flow.FlowUnitDeclaration;
import endrov.flowWindow.FlowView;
import endrov.gui.window.BasicWindow;
import endrov.util.EvSwingUtil;
import endrov.util.Maybe;

/**
 * Get object, absolute path
 * 
 * @author Johan Henriksson
 *
 */
public class FlowUnitObjectReference extends FlowUnit
	{
	private static final String metaType="evobjectio";
	private static final String showName="ObjectRef";
	
	
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitObjectReference.class, null,"Load or store an object in the object hierarchy"));		
		}

	

	private String nameOfObject;
	
	
	public String getLabel()
		{
		return showName;
		}
	
	public FlowUnitObjectReference()
		{
		}
	
	public FlowUnitObjectReference(String path)
		{
		nameOfObject=path;
		}
	
	public Dimension getBoundingBox(Component comp, Flow flow)
		{
		int w=fm.stringWidth(getLabel());
		Dimension d=new Dimension(3+w+3+comp.getWidth()+1,comp.getHeight()+8);
		return d;
		}
	
	public void paint(Graphics g, FlowView panel, Component comp)
		{
		Dimension d=getBoundingBox(comp, panel.getFlow());
		
		g.setColor(Color.GREEN);
		g.fillRect(x,y,d.width,d.height);
		g.setColor(getBorderColor(panel));
		g.drawRect(x,y,d.width,d.height);
		g.setColor(getTextColor());
		g.drawString(getLabel(), x+3, y+d.height/2+fonta/2);
		
		panel.drawConnPointLeft(g,this,"parent",x,y+4);
		panel.drawConnPointLeft(g,this,"in",x,y+d.height-4);
		panel.drawConnPointRight(g,this,"out",x+d.width,y+d.height/2);
		}

	public boolean mouseHoverMoveRegion(int x, int y, Component comp, Flow flow)
		{
		Dimension dim=getBoundingBox(comp, flow);
		return x>=this.x && y>=this.y && x<=this.x+dim.width && y<=this.y+dim.height;
		}

	
	/** Get types of flows in */
	public void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("in", new FlowType(EvObject.class));
		types.put("parent", new FlowType(EvContainer.class));
		}
	
	/** Get types of flows out */
	public void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("out", new FlowType(EvContainer.class));
		}

	
	
	public void editDialog()
		{
		}

	
	public Collection<FlowUnit> getSubUnits(Flow flow)
		{
		return Collections.singleton((FlowUnit)this);
		}

	
	
	
	
	public int getGUIcomponentOffsetX()
		{
		int w=fm.stringWidth(getLabel());
		return 3+w+3;
		}
	public int getGUIcomponentOffsetY()
		{
		return 5;
		}

	
	
	public void setRef(String s)
		{
		nameOfObject=s;
		System.out.println("set ref "+s);
		}
	
	public Component getGUIcomponent(final FlowView p)
		{
		final JTextArea field=new JTextArea(nameOfObject);
		field.setMinimumSize(new Dimension(20,field.getPreferredSize().height));
		/*field.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0)
				{
				//Should maybe be change listener
				//should emit an update
				}});*/
		
		EvSwingUtil.textAreaChangeListener(field, new ChangeListener(){
			public void stateChanged(ChangeEvent e)
				{
				setRef(field.getText());
				p.repaint();
				}
		});

		return field;
		}
	
	
	public String toXML(Element e)
		{
		e.setAttribute("ref",nameOfObject);
		return metaType;
		}
	public void fromXML(Element e)
		{
		nameOfObject=e.getAttributeValue("ref");
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		//EvContainer parent=exec.getParent();

		Maybe<EvContainer> con=flow.getInputValueMaybe(this, exec, "in",EvContainer.class);
		Maybe<EvContainer> parent=flow.getInputValueMaybe(this, exec, "parent",EvContainer.class);
		EvContainer obvalue;
		
		if(con.hasValue())
			{
			//////////////////////////////// Write value
			if(con.get()==null)
				throw new RuntimeException("Object to store must not be null");
			else
				{
				EvContainer parentContainer;
				if(parent.hasValue())
					{
					parentContainer=parent.get();
					}
				else
					{
					EvPath currentPath=exec.getPath().getParent();
					EvPath path=currentPath.getRelativePath(nameOfObject);
					parentContainer=path.getParent().getObject();
					}
				
				//Safety check
				EvContainer oldObjectHere=parentContainer.metaObject.get(nameOfObject);
				if(oldObjectHere!=null && !oldObjectHere.isGeneratedData)
					throw new Exception("Trying to overwrite data that has not been autogenerated");
					
				//Get value and store it
				obvalue=con.get();
				obvalue.isGeneratedData=true; 
				//If an object is forwarded without remod, such as a channel, this will become a problem. setting value should be done somewhere else?
				
				parentContainer.metaObject.put(nameOfObject, (EvObject)obvalue);  //TODO bad cast?
	
				//Update windows about new object
				BasicWindow.updateWindows();
				}
			}
		else
			{
			//////////////////////////////// Read value
			if(parent.hasValue())
				{
				EvContainer parentContainer=parent.get();
				obvalue=parentContainer.getChild(nameOfObject);
				}
			else
				{
				EvPath currentPath=exec.getPath().getParent();
				obvalue=currentPath.getRelativePath(nameOfObject).getObject();
				}
			System.out.println("got "+obvalue);
			}
		
		//Set output to the same value
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.clear();
		lastOutput.put("out", obvalue);
		}

	
	
	
	}
