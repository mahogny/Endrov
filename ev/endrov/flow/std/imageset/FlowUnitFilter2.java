package endrov.flow.std.imageset;

import java.awt.Color;
import java.util.Map;

import endrov.flow.Flow;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.imageset.Imageset;
import endrov.roi.ROI;

/**
 * Flow unit that works on an imageset
 * @author Johan Henriksson
 *
 */
public abstract class FlowUnitFilter2 extends FlowUnitBasic
	{

//	public String getBasicShowName(){return "Combine channels";}
//	public ImageIcon getIcon(){return null;}
	public Color getBackground(){return CategoryInfo.bgColor;}
	
	
	//Probably all 
	/*
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
*/



	@Override
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		//ROI is optional
		types.put("roi", new FlowType(ROI.class));
		
		//Some take several imagesets. provide one by default?
		types.put("imagesetIn", new FlowType(Imageset.class));
		
		
		}

	@Override
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		//Will imagesets be split? very seldom
		types.put("imagesetOut", new FlowType(Imageset.class));
		}

	//May want to divide into Slice, Stack, Channel, Imageset depending on the level of the output.
	//with laziness in output, arbitrary crossings can be produced but the extending unit has to do
	//more of the work
	
	
	
	
	}
