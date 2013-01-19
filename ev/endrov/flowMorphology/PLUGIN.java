/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowMorphology;
import endrov.core.EvPluginDefinition;

public class PLUGIN extends EvPluginDefinition
	{
	public String getPluginName()
		{
		return "Flows: Morphology";
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
		//I think the JRE has issues with so many classes listed here(?). what would happen if I create a list first?
		return new Class[]{
				FlowUnitMorphClose2D.class,
				FlowUnitMorphOpen2D.class,
				FlowUnitMorphDilate2D.class,
				FlowUnitMorphErode2D.class,
				FlowUnitMorphBlackTophat2D.class,
				FlowUnitMorphWhiteTophat2D.class,
				
				FlowUnitMorphComplementBinary.class,
				FlowUnitMorphComplementGray2D.class,
				FlowUnitMorphSkeletonizeBinary3D.class,
				
				FlowUnitMorphHitmissBinary2D.class,
				FlowUnitMorphThickBinary2D.class,
				FlowUnitMorphThinBinary2D.class,
				
				FlowUnitMorphBinarize.class,
				FlowUnitMorphReflect.class,
				FlowUnitMorphConvertToKernel2D.class,
				FlowUnitMorphConstKernel.class,
				
				FlowUnitMorphFillHolesBinary2D.class,
				FlowUnitMorphFillHolesBinary3D.class,
				FlowUnitMorphFillHolesGray2D.class,
				FlowUnitMorphFillHolesGray3D.class,

				FlowUnitMorphDistanceVoronoi2D.class,

				FlowUnitMorphGradientExternal2D.class,
				FlowUnitMorphGradientInternal2D.class,
				FlowUnitMorphGradientWhole2D.class,
				};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}
