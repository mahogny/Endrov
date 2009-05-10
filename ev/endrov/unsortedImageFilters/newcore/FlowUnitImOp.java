package endrov.unsortedImageFilters.newcore;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Collection;
import java.util.Map;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnit;
import endrov.flow.ui.FlowPanel;

public class FlowUnitImOp extends FlowUnit
	{

	@Override
	public void editDialog()
		{
		// TODO Auto-generated method stub
		
		}

	@Override
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		// TODO Auto-generated method stub
		
		}

	@Override
	public void fromXML(Element e)
		{
		// TODO Auto-generated method stub
		
		}

	@Override
	public Dimension getBoundingBox(Component comp, Flow flow)
		{
		// TODO Auto-generated method stub
		return null;
		}

	@Override
	public Component getGUIcomponent(FlowPanel p)
		{
		// TODO Auto-generated method stub
		return null;
		}

	@Override
	public int getGUIcomponentOffsetX()
		{
		// TODO Auto-generated method stub
		return 0;
		}

	@Override
	public int getGUIcomponentOffsetY()
		{
		// TODO Auto-generated method stub
		return 0;
		}

	@Override
	public Collection<FlowUnit> getSubUnits(Flow flow)
		{
		// TODO Auto-generated method stub
		return null;
		}

	@Override
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		// TODO Auto-generated method stub
		
		}

	@Override
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		// TODO Auto-generated method stub
		
		}

	@Override
	public boolean mouseHoverMoveRegion(int x, int y, Component comp, Flow flow)
		{
		// TODO Auto-generated method stub
		return false;
		}

	@Override
	public void paint(Graphics g, FlowPanel panel, Component comp)
		{
		// TODO Auto-generated method stub
		
		}

	@Override
	public String toXML(Element e)
		{
		// TODO Auto-generated method stub
		return null;
		} 
/**
 * just an outline
 */
	
	
	
	}
