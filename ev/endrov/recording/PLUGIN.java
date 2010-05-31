/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording;
import endrov.ev.PluginDef;
import endrov.recording.camWindow.CamWindow;
import endrov.recording.controlWindow.RecControlWindow;
import endrov.recording.frapWindow.EvFRAPAcquisition;
import endrov.recording.frapWindow.FlowUnitCalcFRAP;
import endrov.recording.frapWindow.RecWindowFRAP;
import endrov.recording.lightpathWindow.LightpathWindow;
import endrov.recording.recmetBurst.EvBurstAcquisition;
import endrov.recording.recmetBurst.RecWindowBurst;
import endrov.recording.recmetMultidim.RecWindowMultiDim;

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
				RecControlWindow.class,CamWindow.class, RecordingResource.class,
				RecWindowMultiDim.class,
				
				RecWindowBurst.class,
				RecWindowFRAP.class,
				EvBurstAcquisition.class,
				EvFRAPAcquisition.class,FlowUnitCalcFRAP.class,
				
				LightpathWindow.class
				};
		}
	
	public boolean isDefaultEnabled(){return true;};
	}
