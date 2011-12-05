/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording;
import endrov.ev.PluginDef;
import endrov.recording.bleachWindow.RecWindowQuickBleach;
import endrov.recording.flipWindow.EvFLIPAcquisition;
import endrov.recording.flipWindow.FlowUnitSumIntensityROI;
import endrov.recording.flipWindow.RecWindowFLIP;
import endrov.recording.frapWindow.EvFRAPAcquisition;
import endrov.recording.frapWindow.FlowUnitCalcFRAP;
import endrov.recording.frapWindow.FlowUnitShowGraph;
import endrov.recording.frapWindow.RecWindowFRAP;
import endrov.recording.liveWindow.LiveWindow;
import endrov.recording.propertyWindow.PropertyWindow;
import endrov.recording.recmetBurst.EvBurstAcquisition;
import endrov.recording.recmetBurst.RecWindowBurst;
import endrov.recording.recmetMultidim.RecWindowMultiDim;
import endrov.recording.resolutionConfigWindow.ResolutionConfigWindow;

public class PLUGIN extends PluginDef
	{
	public String getPluginName()
		{
		return "Recording hardware";
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
				LiveWindow.class, 
				RecordingResource.class,
				RecWindowMultiDim.class,
				ResolutionConfigWindow.class,
				
				PropertyWindow.class,
				
				RecWindowBurst.class,
				RecWindowFRAP.class,
				RecWindowFLIP.class, 
				RecWindowQuickBleach.class, 
				EvBurstAcquisition.class,
				EvFRAPAcquisition.class,FlowUnitCalcFRAP.class,
				EvFLIPAcquisition.class,FlowUnitSumIntensityROI.class,
				FlowUnitShowGraph.class,
				};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}
