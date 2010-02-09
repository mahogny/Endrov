/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.rec;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import endrov.flow.Flow;
import endrov.flow.FlowConn;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.recording.RecmetPrimitive;

/**
 * Flow unit for a recording method. The design is special in that additional conformity is wanted.
 * Components can be shown in the recmet window or in the flow. all additional inputs should be optional
 * or it will not work for the window.
 * 
 * @see RecmetPrimitive
 * 
 * @author Johan Henriksson
 *
 */
public abstract class FlowUnitRecmet extends FlowUnitBasic
	{

	/**
	 * information that could be nice at every stage:
	 * * exposure time
	 * * camera choice
	 * * filter choices, objectives etc. state devices.
	 * * binning
	 * 
	 * 3 categories: 
	 * * set a /device state/ by name, 
	 * * set a /device /property/, 
	 * * set a /virtual property/ to be used programatically (exposure)
	 * 
	 * setting device state is tricky!!! most of this machinery need to go into recording unit
	 * still, allow it to be done:
	 * 
	 * tag: sethwprop/ix/cam.binning	=	4 
	 *  
	 *  
	 *  
	 *  
	 * 
	 * aliases for devices?
	 * tag: <zstage>	= ix.stage.z		-- not splitting axises seem to give hell	
	 * then: zstage		= 5							-- will set z, using the z device <zstage>
	 * 
	 * 
	 * 
	 */

	
	
	
	public interface RecmetGenerator
		{
		/**
		 * Lazy generation of primitives. The idea is that enough is generated every call that the following
		 * transformer can do work. If it is not ready to output more it should give the empty list
		 */
		public List<RecmetPrimitive> getMore();
		}
	
	//May want to divide into Slice, Stack, Channel, Imageset depending on the level of the output.
	//with laziness in output, arbitrary crossings can be produced but the extending unit has to do
	//more of the work
	
	
	
	//need component for recmet window. Will use the one that already exists, getGUIcomponent

	//need to know how to render connected components
	//can assume N of these are connected. maybe one single way of rendering?

	
	
	/**
	 * How many components can be connected? null if infinite
	 */
	public abstract Integer shouldConnectNum();
	
	
	/** 
	 * Get types of flows in. If this is overridden to add optional inputs then 
	 * this super method has to be invoked as well.
	 */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		Integer shouldNum=shouldConnectNum();
		String prefix="primin";
		if(shouldNum==null)
			{
			//Add those in use
			HashSet<Integer> nums=new HashSet<Integer>();
			for(FlowConn c:flow.getFlowsToUnit(this))
				if(c.toArg.startsWith(prefix))
					{
					nums.add(Integer.parseInt(c.toArg.substring(prefix.length())));
					types.put(c.toArg, null);
					}
			//Add one free in addition
			int i=0;
			while(nums.contains(i))
				i++;
			types.put(prefix+i, null);
			}
		else
			{
			for(int i=0;i<shouldNum;i++)
				types.put(prefix+i, null);
			}
		
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("primout", null); //Type
		}
	
	public void editDialog(){}

	
	}
