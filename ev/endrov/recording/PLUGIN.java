/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording;
import endrov.core.EvPluginDefinition;
import endrov.recording.bleachWindow.RecWindowQuickBleach;
import endrov.recording.hardwareControlWindow.HardwareControlWindow;
import endrov.recording.liveWindow.LiveWindow;
import endrov.recording.recmetBurst.EvBurstAcquisition;
import endrov.recording.recmetBurst.RecWindowBurst;
import endrov.recording.recmetFLIP.EvFLIPAcquisition;
import endrov.recording.recmetFLIP.FlowUnitSumIntensityROI;
import endrov.recording.recmetFLIP.RecWindowFLIP;
import endrov.recording.recmetFRAP.EvFRAPAcquisition;
import endrov.recording.recmetFRAP.FlowUnitCalcFRAP;
import endrov.recording.recmetFRAP.FlowUnitShowGraph;
import endrov.recording.recmetFRAP.RecWindowFRAP;
import endrov.recording.recmetMultidim.RecWindowMultiDim;
import endrov.recording.resolutionConfigWindow.ResolutionConfigWindow;
import endrov.recording.windowPlateOverview.PlateOverviewWindow;
import endrov.recording.windowPlatePositions.PlatePositionsWindow;

public class PLUGIN extends EvPluginDefinition
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
				PlateOverviewWindow.class,
				PlatePositionsWindow.class,
				
				HardwareControlWindow.class,
				
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
