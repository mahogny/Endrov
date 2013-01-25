/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.constants;

import java.awt.Color;
import java.util.Map;

import javax.swing.ImageIcon;

import endrov.flow.Flow;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flowBasic.RendererFlowUtil;

/**
 * Common look for all flow constants
 * @author Johan Henriksson
 *
 */
public abstract class FlowUnitConst extends FlowUnitBasic
	{
	protected Color colorBadValue=Color.RED; 

	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		}
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("out", getConstType());
		}
	
	protected abstract FlowType getConstType();
	
	@Override
	public Color getBackground()
		{
		return RendererFlowUtil.colConstant;
		}

	@Override
	public ImageIcon getIcon()
		{
		return CategoryInfo.icon;
		}
	
	}
