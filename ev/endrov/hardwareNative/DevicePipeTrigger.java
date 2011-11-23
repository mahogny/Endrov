package endrov.hardwareNative;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;


import endrov.hardware.DevicePropertyType;
import endrov.hardware.EvDeviceObserver.DeviceListener;
import endrov.recording.HWTrigger;

/**
 * Device: Software triggerer based on unix pipes
 * 
 * @author Johan Henriksson
 *
 */
public class DevicePipeTrigger implements HWTrigger
	{

	private List<TriggerListener> triggerListeners=new LinkedList<TriggerListener>();
	private List<DeviceListener> deviceListeners=new LinkedList<DeviceListener>();
	
	
	private String pipepath="";
	
	private PipeThread thread;
	
	public void addTriggerListener(TriggerListener listener)
		{
		triggerListeners.add(listener);
		}

	public void removeTriggerListener(TriggerListener listener)
		{
		triggerListeners.remove(listener);
		}

	
	
	
	public void addDeviceListener(DeviceListener listener)
		{
		deviceListeners.add(listener);
		}
	

	public void removeDeviceListener(DeviceListener listener)
		{
		deviceListeners.remove(listener);
		}
	

	public String getDescName()
		{
		return "Pipe triggerer";
		}

	public SortedMap<String, String> getPropertyMap()
		{
		SortedMap<String,String> properties=new TreeMap<String, String>();
		properties.put("path",pipepath);
		return properties;
		}

	public SortedMap<String, DevicePropertyType> getPropertyTypes()
		{
		SortedMap<String, DevicePropertyType> types=new TreeMap<String, DevicePropertyType>();
		types.put("path", DevicePropertyType.getEditableStringState());
		return types;
		}

	public String getPropertyValue(String prop)
		{
		if(prop.equals("path"))
			return pipepath;
		else
			return null;
		}

	public Boolean getPropertyValueBoolean(String prop)
		{
		return null;
		}

	public boolean hasConfigureDialog()
		{
		return false;
		}

	public void openConfigureDialog()
		{
		}


	
	
	
	public void setPropertyValue(String prop, String value)
		{
		if(prop.equals("path"))
			{
			pipepath=value;
			
			//It would be possible to create the pipe here but maybe let the user take care of this?
			//Runtime.getRuntime().exec("mkfifo "+pipepath); //This may need escaping
			
			//Start up the pipe listener
			if(thread!=null)
				{
				thread.active=false;
				thread=null;
				}
			thread=new PipeThread(new File(pipepath));
			thread.start();
			}
		}

	public void setPropertyValue(String prop, boolean value)
		{
		}

	
	
	private class PipeThread extends Thread
		{
		boolean active=true;
		private final File f;
		
		public PipeThread(File f)
			{
			this.f=f;
			}
		
		@Override
		public void run()
			{
			while(active)
				{
				try
					{
					FileReader fr=new FileReader(f);

					int c;
					do
						{
						try
							{
							//Read all characters waiting, at least one. It is not sufficient to read only one because
							//the writing process may have sent more than one trigger since the buffer was last emptied
							c=fr.read();
							while(fr.ready())
								c=fr.read();
							
							//Tell listeners
							if(active && c!=-1)
								{
								for(TriggerListener l:triggerListeners)
									l.triggered();
								}
							}
						catch (IOException e)
							{
							System.out.println("I/O error reading named pipe: "+e.getMessage());
							c=-1;
							}
						
						//If the character -1 was last read then the writer has closed the pipe. Some writers will be able to continuously write to the same pipe.
						//Others might re-open it. Or the pipe might be broken and will have to be set up again. If this happens then close it and open it again.
						}
					while(active && c!=-1);
					fr.close();
					}
				catch (IOException e)
					{
					e.printStackTrace();
					}
				}
			}
		}
	
	
	public static void main(String[] args)
		{
		
		DevicePipeTrigger tr=new DevicePipeTrigger();
		tr.setPropertyValue("path", "/home/tbudev3/pipetrigger");
		tr.addTriggerListener(new TriggerListener()
			{
				public void triggered()
					{
					System.out.println("trigger!");
					}
			});
		
		try
			{
			Thread.sleep(10000);
			}
		catch (InterruptedException e)
			{
			}
		
		}
	
	}
