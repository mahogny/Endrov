/***
 * Copyright (C) 2010 David Johansson & Arvid Johansson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.driverFrivolous;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jdom.Element;

import endrov.hardware.EvDevice;
import endrov.hardware.EvDeviceObserver;
import endrov.hardware.EvDeviceProvider;
import endrov.hardware.EvHardware;
import endrov.hardware.DevicePropertyType;
import endrov.recording.CameraImage;
import endrov.recording.HWImageScanner;
import endrov.recording.HWStage;

/**
 * Device provider for Frivolous virtual microscope
 * 
 * @author David Johansson, Arvid Johansson, Johan Henriksson
 */
public class FrivolousDeviceProvider extends EvDeviceProvider implements EvDevice
	{

	private static Map<String, Class<? extends EvDevice>> hardwareProvided = new TreeMap<String, Class<? extends EvDevice>>();
	private FrivolousModel model;

	public static void initPlugin()
		{
		}

	static
		{
		EvHardware.root.hw.put("fr", new FrivolousDeviceProvider());
		}

	public FrivolousDeviceProvider()
		{
		}

	public double[] stagePos = new double[]{ 0, 0, 0 };

	private class FrivolousCamera implements HWImageScanner
		{
		//Properties
		private double expTime=0.01;
		
		//
		private LinkedList<CameraImage> seqBuffer=new LinkedList<CameraImage>();
		private boolean runSequenceAcq=false;
		private double duration;
		
		//Thread to capture images sequentially while there is a demand (burst acquisition)
		Thread seqAcqThread=new Thread()
			{
			public void run()
				{
				while(FrivolousCamera.this.runSequenceAcq)
					{
					try
						{
						if(duration<1)
							Thread.sleep(100);
						else
							Thread.sleep((int)duration);
						
						synchronized (seqBuffer)
							{
							seqBuffer.addLast(snap());
							}
						}
					catch (InterruptedException e)
						{
						e.printStackTrace();
						}
					}
				}
			};
			
		

		public String getDescName()
			{
			return "Frivolous camera";
			}

		public SortedMap<String, String> getPropertyMap()
			{
			TreeMap<String, String> p = new TreeMap<String, String>();
			p.put("Numerical Aperture", ""+model.getSettings().na);
			p.put("Wavelength", ""+model.getSettings().lambda);
			p.put("Exposure", ""+expTime);
			return p;
			}

		public SortedMap<String, DevicePropertyType> getPropertyTypes()
			{
			TreeMap<String, DevicePropertyType> pt = new TreeMap<String, DevicePropertyType>();
			DevicePropertyType p;

			// NA
			p = new DevicePropertyType();
			p.rangeUpper = 0.95;
			p.rangeLower = 0.25;
			p.hasRange = true;
			pt.put("Numerical Aperture", p);

			// wavelength
			p = new DevicePropertyType();
			p.rangeUpper = 800;
			p.rangeLower = 300;
			p.hasRange = true;
			pt.put("Wavelength", p);
			
			// Exposure
			p = new DevicePropertyType();
			pt.put("Exposure", p);
			
			return pt;
			}

		public String getPropertyValue(String prop)
			{
			if (prop.equals("Numerical Aperture"))
				return ""+model.getSettings().na;
			else if (prop.equals("Wavelength"))
				return ""+model.getSettings().lambda;
			else if (prop.equals("Exposure"))
				return ""+expTime;
			return getPropertyMap().get(prop);
			}

		public Boolean getPropertyValueBoolean(String prop)
			{
			return null;
			}

		public void setPropertyValue(String prop, boolean value)
			{
			}

		public void setPropertyValue(String prop, String value)
			{
			if (prop.equals("Numerical Aperture"))
				model.getSettings().na = Double.parseDouble(value);
			else if (prop.equals("Wavelength"))
				model.getSettings().lambda = Double.parseDouble(value);
			else if (prop.equals("Exposure"))
				expTime=Double.parseDouble(value);
			System.out.println(prop+" "+value);
			}
		
		//[um/px]
		private double getRes()
			{
			//return 0.1;
			//return 1; //temp
			return 10;
			}
		
		public double getResMagX()
			{
			return getRes();
			}

		public double getResMagY()
			{
			return getRes();
			}

		public boolean hasConfigureDialog()
			{
			return false;
			}

		public void openConfigureDialog()
			{
			}

		public CameraImage snapInternal()
			{
//		int r = (int) stagePos[2];
			model.convolve((int)stagePos[0],(int)stagePos[1]);
			int[]/*BufferedImage*/ im = model.getImage();
			CameraImage cim = new CameraImage(model.imageWidth, model.imageHeight, 4, im, 1);
			return cim;
			}

		public CameraImage snap()
			{
			long startTime=System.currentTimeMillis();
			CameraImage cim=snapInternal();
			
			//Bleach everything visible at the moment
			for(FrivolousDiffusion d:model.cell.diffusers)
				d.bleach(width, height, 0, 0, (float)getBleachFactor());

			waitInTotal(startTime, expTime);
			return cim;
			}

		
		
		////////////// TODO
		
		
		public double getSequenceCapacityFree()
			{
			return 1;
			}

		public boolean isDoingSequenceAcq()
			{
			return runSequenceAcq;
			}

		public CameraImage snapSequence() throws Exception
			{
			synchronized (seqBuffer)
				{
				if(seqBuffer.isEmpty())
					return null;
				else
					return seqBuffer.poll();
				}
			}

		public void startSequenceAcq(double interval) throws Exception
			{
			seqBuffer.clear();
			runSequenceAcq=true;
			}

		public void stopSequenceAcq()
			{
			runSequenceAcq=false;
			}
		
		
		public EvDeviceObserver event=new EvDeviceObserver();
		public void addListener(EvDeviceObserver.Listener listener)
			{
			event.addWeakListener(listener);
			}
		public void removeListener(EvDeviceObserver.Listener listener)
			{
			event.remove(listener);
			}

		
		int height=512;
		int width=512;
		
		public int getHeight()
			{
			return height;
			}

		public int getWidth()
			{
			return width;
			}

		/**
		 * How much to bleach from one single laser scan
		 * 
		 * Not yet here: wavelength dependence. cross-talk.
		 * 
		 */
		public double getBleachFactor()
			{
			double laserPower=1;
			double c=1;

			return Math.exp(-expTime*laserPower*c);
			}

		/**
		 * Wait at least a certain time - for simulating slow response
		 */
		public void waitInTotal(long startTime, double totalDuration)
			{
			long currentTime=System.currentTimeMillis();
			long dt=startTime+(long)(totalDuration*1000)-currentTime;
			if(dt>0)
				{
				try
					{
					Thread.sleep(dt);
					}
				catch (InterruptedException e)
					{
					}
				}
			}
		
		
		public void scan(int[] buffer, ScanStatusListener status)
			{
			long startTime=System.currentTimeMillis();
			
			//Scan
			if(buffer!=null)
				{
				CameraImage im=snapInternal();
				int[] snapped=(int[])im.pixels;
				for(int i=0;i<snapped.length;i++)
						buffer[i]=snapped[i];
				}
			
			//Bleach everything visible at the moment
			for(FrivolousDiffusion d:model.cell.diffusers)
				d.bleach(width, height, 0, 0, (float)getBleachFactor());
			
			waitInTotal(startTime, expTime);
			}

		public void scan(int[] buffer, ScanStatusListener status, int[] roi)
			{
			long startTime=System.currentTimeMillis();
			
			//Scan
			if(buffer!=null)
				{
				CameraImage im=snapInternal();
				int[] snapped=(int[])im.pixels;
				for(int i=0;i<roi.length;i++)
					if(roi[i]!=0)
						buffer[i]=snapped[i];
				}
			
			//Bleach only the ROI
			for(FrivolousDiffusion d:model.cell.diffusers)
				d.bleach(roi, width, height, 0, 0, (float)getBleachFactor()); 
			
			waitInTotal(startTime, expTime);
			}

		/**
		 * Set how many pixels should be scanned
		 */
		public void setNumberPixels(int width, int height) throws Exception
			{
			throw new Exception("Not supported yet");
			//this.width=width;
			//this.height=height;
			}

		}

	private class FrivolousStage implements HWStage
		{
		// Simulate moving stage?

		public String[] getAxisName()
			{
			return new String[]{ "x", "y", "z" };
			}

		public int getNumAxis()
			{
			return 3;
			}

		public double[] getStagePos()
			{
			return new double[]{ stagePos[0], stagePos[1], stagePos[2] };
			}

		public void setRelStagePos(double[] axis)
			{
			double[] tmp = stagePos.clone();
			for (int i = 0; i<3; i++)
				tmp[i] += axis[i];
			setStagePos(tmp);
			System.out.println("curpos "+stagePos[0]+"  "+stagePos[1]+"   "+stagePos[2]);
			}

		public void setStagePos(double[] axis)
			{
			double oldZ = stagePos[2];
			if (axis[2]<-10000)
				axis[2]=-10000;
			else if(axis[2]>10000)
				axis[2]=10000;
			
			for (int i = 0; i<2; i++)
				if (axis[i]<-512*.1)
					axis[i]=-512*.1;
				else if (axis[i]>512*.1)
					axis[i]=512*.1;	
			
			for (int i = 0; i<3; i++)
				stagePos[i] = axis[i];

			model.getSettings().offsetZ = stagePos[2];
			if(stagePos[2]!=oldZ)
				model.updatePSF();
			}

		public void goHome()
			{
			for (int i = 0; i<3; i++)
				stagePos[i] = 0;
			}

		public String getDescName()
			{
			return "Frivolous stage";
			}

		public SortedMap<String, String> getPropertyMap()
			{
			return new TreeMap<String, String>();
			}

		public SortedMap<String, DevicePropertyType> getPropertyTypes()
			{
			return new TreeMap<String, DevicePropertyType>();
			}

		public String getPropertyValue(String prop)
			{
			return null;
			}

		public Boolean getPropertyValueBoolean(String prop)
			{
			return null;
			}

		public void setPropertyValue(String prop, boolean value)
			{
			}

		public void setPropertyValue(String prop, String value)
			{
			}

		public boolean hasConfigureDialog()
			{
			return false;
			}

		public void openConfigureDialog()
			{
			}

		
		public EvDeviceObserver event=new EvDeviceObserver();
		public void addListener(EvDeviceObserver.Listener listener)
			{
			event.addWeakListener(listener);
			}
		public void removeListener(EvDeviceObserver.Listener listener)
			{
			event.remove(listener);
			}

		public boolean hasSampleLoadPosition(){return false;}
		public void setSampleLoadPosition(boolean b){}
		public boolean getSampleLoadPosition(){return false;};
		
		public void stop()
			{
			}

		}

	public Set<EvDevice> autodetect()
		{
		return null;
		}

	public void getConfig(Element root)
		{
		}

	public List<String> provides()
		{
		return Arrays.asList("frivolous");
		}

	public EvDevice newProvided(String s)
		{
		try
			{
			return hardwareProvided.get(s).newInstance();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			return null;
			}
		}

	public void setConfig(Element root)
		{

		}

	public String getDescName()
		{
		return "Frivolous";
		}

	public SortedMap<String, String> getPropertyMap()
		{
		return new TreeMap<String, String>();
		}

	public SortedMap<String, DevicePropertyType> getPropertyTypes()
		{
		return new TreeMap<String, DevicePropertyType>();
		}

	public String getPropertyValue(String prop)
		{
		return null;
		}

	public Boolean getPropertyValueBoolean(String prop)
		{
		return false;
		}

	public void setPropertyValue(String prop, boolean value)
		{
		}

	public void setPropertyValue(String prop, String value)
		{
		}

	public boolean hasConfigureDialog()
		{
		return true;
		}

	public void openConfigureDialog()
		{
		model = new FrivolousModel();
		FrivolousCamera cam=new FrivolousCamera();
		cam.seqAcqThread.start();
		hw.put("cam", cam);
		hw.put("stage", new FrivolousStage());
		}

	
	public EvDeviceObserver event=new EvDeviceObserver();
	public void addListener(EvDeviceObserver.Listener listener)
		{
		event.addWeakListener(listener);
		}
	public void removeListener(EvDeviceObserver.Listener listener)
		{
		event.remove(listener);
		}

	public boolean hasSampleLoadPosition(){return false;}
	public void setSampleLoadPosition(boolean b){}

	}
