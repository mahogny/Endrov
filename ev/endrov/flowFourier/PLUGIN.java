/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowFourier;
import endrov.core.EvPluginDefinition;

public class PLUGIN extends EvPluginDefinition
	{
	public String getPluginName()
		{
		return "Flows: Fourier transform";
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
				FlowUnitFourier2D.class,
				FlowUnitInverseFourier2D.class,
				FlowUnitFourier3D.class,
				FlowUnitInverseFourier3D.class,

				FlowUnitConvGaussian2D.class,
				FlowUnitConvGaussian3D.class,

				FlowUnitWrapImage2D.class,
				FlowUnitWrapImage3D.class,
				
				FlowUnitCircConv2D.class,
				FlowUnitCircConv3D.class,
				
				FlowUnitDoG2D.class,
				};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}
