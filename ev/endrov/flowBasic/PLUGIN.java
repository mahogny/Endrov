/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic;

import endrov.core.EvPluginDefinition;
import endrov.flowBasic.collection.FlowUnitConcat;
import endrov.flowBasic.collection.FlowUnitHeadTail;
import endrov.flowBasic.collection.FlowUnitSize;
import endrov.flowBasic.colorspace.FlowUnitColorRGBtoHSV;
import endrov.flowBasic.colorspace.FlowUnitQuantizeColors2D;
import endrov.flowBasic.constants.*;
import endrov.flowBasic.control.FlowUnitComments;
import endrov.flowBasic.control.FlowUnitIf;
import endrov.flowBasic.control.FlowUnitInput;
import endrov.flowBasic.control.FlowUnitMap;
import endrov.flowBasic.control.FlowUnitOutput;
import endrov.flowBasic.control.FlowUnitShow;
import endrov.flowBasic.convert.FlowUnitConvertFromVector2i;
import endrov.flowBasic.convert.FlowUnitConvertFromVector3i;
import endrov.flowBasic.convert.FlowUnitConvertToVector2i;
import endrov.flowBasic.convert.FlowUnitConvertToVector3i;
import endrov.flowBasic.convert.FlowUnitWrapInChannel;
import endrov.flowBasic.db.FlowUnitConnectSQL;
import endrov.flowBasic.images.FlowUnitCastPixelType;
import endrov.flowBasic.images.FlowUnitChannelDim2D;
import endrov.flowBasic.images.FlowUnitChannelDim3D;
import endrov.flowBasic.images.FlowUnitCropImage;
import endrov.flowBasic.logic.FlowUnitAnd;
import endrov.flowBasic.logic.FlowUnitNot;
import endrov.flowBasic.logic.FlowUnitOr;
import endrov.flowBasic.logic.FlowUnitXor;
import endrov.flowBasic.math.*;
import endrov.flowBasic.objects.FlowUnitFlowParent;
import endrov.flowBasic.objects.FlowUnitGetObjectsOfType;
import endrov.flowBasic.objects.FlowUnitObjectReference;
import endrov.flowBasic.objects.FlowUnitThisData;
import endrov.flowBasic.objects.FlowUnitThisFlow;

public class PLUGIN extends EvPluginDefinition
	{
	public String getPluginName()
		{
		return "Flows: Basic units";
		}

	public String getAuthor()
		{
		return "Johan Henriksson";
		}
	
	public boolean systemSupported()
		{
		return true;
		}
	
	public String cite()
		{
		return "";
		}
	
	public String[] requires()
		{
		return new String[]{};
		}
	
	public Class<?>[] getInitClasses()
		{
		
		
		return new Class[]{
				//Collection
				FlowUnitConcat.class,
				FlowUnitHeadTail.class,
				FlowUnitMap.class,
				FlowUnitSize.class,
				
				//Constants
				FlowUnitConstBoolean.class,
				FlowUnitConstDouble.class,
				FlowUnitConstInteger.class,
				FlowUnitConstString.class,
				FlowUnitConstClass.class,
				FlowUnitConstEvDecimal.class,
				FlowUnitConstFile.class,
				FlowUnitConstExpression.class,

				//Objects
				FlowUnitGetObjectsOfType.class,
				FlowUnitFlowParent.class,
				FlowUnitObjectReference.class,
				FlowUnitThisData.class,
				FlowUnitThisFlow.class,

				//Control
				FlowUnitIf.class,
				FlowUnitInput.class,
				FlowUnitOutput.class,
				//FlowUnitScript.class,
				FlowUnitShow.class,
				FlowUnitComments.class,

				//Convert
				FlowUnitConvertFromVector2i.class,
				FlowUnitConvertFromVector3i.class,
				FlowUnitConvertToVector2i.class,
				FlowUnitConvertToVector3i.class,
				FlowUnitWrapInChannel.class,

				//Colorspace
				FlowUnitQuantizeColors2D.class,
				FlowUnitColorRGBtoHSV.class,
				
				//db
				FlowUnitConnectSQL.class,
				/*
				//Imserv
				FlowUnitImserv.class,FlowUnitImservLoad.class,FlowUnitImservQuery.class,
				*/

				//Images
				FlowUnitChannelDim2D.class,
				FlowUnitChannelDim3D.class,
				FlowUnitCropImage.class,
				FlowUnitCastPixelType.class,

				//Logic
				FlowUnitAnd.class,
				FlowUnitOr.class,
				FlowUnitXor.class,
				FlowUnitNot.class,
				FlowUnitGreaterThan.class,
				FlowUnitEquals.class,

				//Math
				FlowUnitAdd.class,FlowUnitDiv.class,FlowUnitSub.class,FlowUnitMul.class,
				FlowUnitSin.class, FlowUnitCos.class, FlowUnitExp.class, FlowUnitLog.class, FlowUnitSqrt.class,
				FlowUnitMin.class,FlowUnitMax.class, FlowUnitPow.class, 
				FlowUnitAbs.class,
				FlowUnitAbsGradXY2.class,
				FlowUnitComplexMul.class,
				FlowUnitMinusSign.class,
				
				
		};
		
		
		}
	
	public boolean isDefaultEnabled(){return true;};
	}
