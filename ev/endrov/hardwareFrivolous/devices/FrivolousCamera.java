package endrov.hardwareFrivolous.devices;

import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;

import endrov.hardware.DevicePropertyType;
import endrov.hardware.EvDeviceObserver;
import endrov.hardwareFrivolous.FrivolousDiffusion;
import endrov.imageset.EvPixels;
import endrov.recording.CameraImage;
import endrov.recording.device.HWImageScanner;


/**
 * Frivolous camera provider
 * 
 * @author Johan Henriksson, David Johansson, Arvid Johansson
 *
 */
public class FrivolousCamera implements HWImageScanner
	{
	private FrivolousDeviceProvider frivolous;
	
	
	//Properties
	private double expTime=0.001;
	private boolean simulatePSF=true;
	private boolean simulateNoise=true;
	
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
		
		
	public FrivolousCamera(FrivolousDeviceProvider frivolous)
		{
		this.frivolous=frivolous;
		}
		
	
	
	public String getDescName()
		{
		return "Frivolous camera";
		}
	
	public SortedMap<String, String> getPropertyMap()
		{
		TreeMap<String, String> p = new TreeMap<String, String>();
		p.put("Numerical Aperture", ""+frivolous.model.getSettings().na);
		p.put("Wavelength", ""+frivolous.model.getSettings().lambda);
		p.put("Exposure", ""+expTime);
		p.put("SimulatePSF", simulatePSF?"1":"0");
		p.put("SimulateNoise", simulatePSF?"1":"0");
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
		
		// Simulate PSF
		p = new DevicePropertyType();
		p.isBoolean=true;
		pt.put("SimulatePSF", p);
	
		// Simulate noise
		p = new DevicePropertyType();
		p.isBoolean=true;
		pt.put("SimulateNoise", p);
	
		return pt;
		}
	
	public String getPropertyValue(String prop)
		{
		if (prop.equals("Numerical Aperture"))
			return ""+frivolous.model.getSettings().na;
		else if (prop.equals("Wavelength"))
			return ""+frivolous.model.getSettings().lambda;
		else if (prop.equals("Exposure"))
			return ""+expTime;
		else if (prop.equals("SimulatePSF"))
			return simulatePSF?"1":"0";
		else if (prop.equals("SimulateNoise"))
			return simulateNoise?"1":"0";
		return getPropertyMap().get(prop);
		}
	
	public Boolean getPropertyValueBoolean(String prop)
		{
		if (prop.equals("SimulatePSF"))
			return simulatePSF;
		else if (prop.equals("SimulateNoise"))
			return simulateNoise;
		return null;
		}
	
	public void setPropertyValue(String prop, boolean value)
		{
		setPropertyValue(prop, value?"1":"0");
		}
	
	public void setPropertyValue(String prop, String value)
		{
	//	System.out.println("set friv prop "+prop+" = "+value);
		if (prop.equals("Numerical Aperture"))
			frivolous.model.getSettings().na = Double.parseDouble(value);
		else if (prop.equals("Wavelength"))
			frivolous.model.getSettings().lambda = Double.parseDouble(value);
		else if (prop.equals("Exposure"))
			expTime=Double.parseDouble(value);
		else if (prop.equals("SimulatePSF"))
			simulatePSF=value.equals("1");
		else if (prop.equals("SimulateNoise"))
			simulateNoise=value.equals("1");
		}
	
	//[um/px]
	private double getRes()
		{
		return frivolous.resolution;
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
		int stagePosPixelsX=-getOffsetX();
		int stagePosPixelsY=-getOffsetY();
		
		int[] im = frivolous.model.convolve(stagePosPixelsX, stagePosPixelsY, simulatePSF, simulateNoise);
		
		EvPixels p=EvPixels.createFromInt(frivolous.model.imageWidth, frivolous.model.imageHeight, im); 
	
		//TODO support other bit depths
	
		CameraImage cim = new CameraImage(p);
	
		return cim;
		}
	
	public CameraImage snap()
		{
		long startTime=System.currentTimeMillis();
		CameraImage cim=snapInternal();
	
		int offsetX=getOffsetX();
		int offsetY=getOffsetY();
	
		
		//Bleach everything visible at the moment
		for(FrivolousDiffusion d:frivolous.model.cell.diffusers)
			d.bleach(frivolous.width, frivolous.height, 0, 0, (float)getBleachFactor());
		frivolous.model.cell.bleachImmobile(frivolous.width, frivolous.height, offsetX, offsetY, (float)getBleachFactor());
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
	public void addDeviceListener(EvDeviceObserver.DeviceListener listener)
		{
		event.addWeakListener(listener);
		}
	public void removeDeviceListener(EvDeviceObserver.DeviceListener listener)
		{
		event.remove(listener);
		}
	
	
	
	public int getHeight()
		{
		return frivolous.height;
		}
	
	public int getWidth()
		{
		return frivolous.width;
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
		double c=2;
	
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
	
	
	/**
	 * Scan the entire image
	 */
	public void scan(int[] buffer, ScanStatusListener status)
		{
		scan(buffer, status, null);
		}
	
	/**
	 * Scan only a ROI
	 */
	public void scan(int[] buffer, ScanStatusListener status, int[] roi)
		{
		long startTime=System.currentTimeMillis();
		
		//Scan
		if(buffer!=null)
			{
			CameraImage im=snapInternal();
			int[] snapped=(int[])im.pixels;
			if(roi!=null)
				{
				for(int i=0;i<snapped.length;i++)
					if(roi[i]!=0)
						buffer[i]=snapped[i];
				}
			else
				{
				for(int i=0;i<snapped.length;i++)
					buffer[i]=snapped[i];
				}
			}
		
		int offsetX=getOffsetX();
		int offsetY=getOffsetY();
		
		//Bleach only the ROI
		for(FrivolousDiffusion d:frivolous.model.cell.diffusers)
			d.bleach(roi, frivolous.width, frivolous.height, offsetX, offsetY, (float)getBleachFactor()); 
		frivolous.model.cell.bleachImmobile(roi, frivolous.width, frivolous.height, offsetX, offsetY, (float)getBleachFactor()); 
		waitInTotal(startTime, expTime);
		}
	
	
	private int getOffsetX()
		{
		return -(int)(frivolous.stagePos[0]/getRes());
		}
	
	private int getOffsetY()
		{
		return -(int)(frivolous.stagePos[1]/getRes());
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
	
	public int getCamWidth() 
		{
		return frivolous.width;
		}
	
	public int getCamHeight()
		{
		return frivolous.height;
		}
	
	}
