/***
 * Copyright (C) 2010 David Johansson & Arvid Johansson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.driverFrivolous;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.jdom.Element;

import endrov.basicWindow.BasicWindow;
import endrov.hardware.EvDevice;
import endrov.hardware.EvDeviceObserver;
import endrov.hardware.EvDeviceProvider;
import endrov.hardware.EvHardware;
import endrov.hardware.DevicePropertyType;
import endrov.recording.CameraImage;
import endrov.recording.HWAutoFocus;
import endrov.recording.HWImageScanner;
import endrov.recording.HWStage;
import endrov.util.EvSwingUtil;

/**
 * Device provider for Frivolous virtual microscope
 * 
 * @author David Johansson, Arvid Johansson, Johan Henriksson
 */
public class FrivolousDeviceProvider extends EvDeviceProvider implements EvDevice
	{
	private double resolution=1;
	private int height=512;
	private int width=512;
	private static Map<String, Class<? extends EvDevice>> hardwareProvided = new TreeMap<String, Class<? extends EvDevice>>();
	private FrivolousModel model;

	static
		{
		EvHardware.root.hw.put("fr", new FrivolousDeviceProvider());
		}
		
	public static void initPlugin()
		{
		}


	public FrivolousDeviceProvider()
		{
		}

	public double[] stagePos = new double[]{ 0, 0, 0 };

	
	
	///////////////////
	
	private class FrivolousCamera implements HWImageScanner
		{
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
				return ""+model.getSettings().na;
			else if (prop.equals("Wavelength"))
				return ""+model.getSettings().lambda;
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
//			System.out.println("set friv prop "+prop+" = "+value);
			if (prop.equals("Numerical Aperture"))
				model.getSettings().na = Double.parseDouble(value);
			else if (prop.equals("Wavelength"))
				model.getSettings().lambda = Double.parseDouble(value);
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
			return resolution;
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
			int stagePosPixelsX=(int)(stagePos[0]/getRes());
			int stagePosPixelsY=(int)(stagePos[1]/getRes());
			
			int[] im = model.convolve(stagePosPixelsX, stagePosPixelsY, simulatePSF, simulateNoise);
			//int[]/*BufferedImage*/ im = model.getImage();
			CameraImage cim = new CameraImage(model.imageWidth, model.imageHeight, 4, im, 1);
			return cim;
			}

		public CameraImage snap()
			{
			long startTime=System.currentTimeMillis();
			CameraImage cim=snapInternal();

			int offsetX=-(int)(stagePos[0]/getRes());
			int offsetY=-(int)(stagePos[1]/getRes());
			
			//Bleach everything visible at the moment
			for(FrivolousDiffusion d:model.cell.diffusers)
				d.bleach(width, height, 0, 0, (float)getBleachFactor());
			model.cell.bleachImmobile(width, height, offsetX, offsetY, (float)getBleachFactor());
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
			long startTime=System.currentTimeMillis();
			
			//Scan
			if(buffer!=null)
				{
				CameraImage im=snapInternal();
				int[] snapped=(int[])im.pixels;
				for(int i=0;i<snapped.length;i++)
						buffer[i]=snapped[i];
				}
			
			int offsetX=-(int)(stagePos[0]/getRes());
			int offsetY=-(int)(stagePos[1]/getRes());
			
			//Bleach everything visible at the moment
			for(FrivolousDiffusion d:model.cell.diffusers)
				d.bleach(width, height, offsetX, offsetY, (float)getBleachFactor());
			model.cell.bleachImmobile(width, height, offsetX, offsetY, (float)getBleachFactor());
			waitInTotal(startTime, expTime);
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
				for(int i=0;i<roi.length;i++)
					if(roi[i]!=0)
						buffer[i]=snapped[i];
				}
			
			int offsetX=-(int)(stagePos[0]/getRes());
			int offsetY=-(int)(stagePos[1]/getRes());
			
			//Bleach only the ROI
			for(FrivolousDiffusion d:model.cell.diffusers)
				d.bleach(roi, width, height, offsetX, offsetY, (float)getBleachFactor()); 
			model.cell.bleachImmobile(roi, width, height, offsetX, offsetY, (float)getBleachFactor()); 
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

	
	//////////////////////////////////
	
	
	
	private class FrivolousStage implements HWStage
		{
		// TODO Simulate moving stage? takes time to move? 

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
			}

		public void setStagePos(double[] axis)
			{
			double oldZ = stagePos[2];
			if (axis[2]<-10000)
				axis[2]=-10000;
			else if(axis[2]>10000)
				axis[2]=10000;
			
			//TODO connect to magnification
			/*
			for (int i = 0; i<2; i++)
				if (axis[i]<-512*.1)
					axis[i]=-512*.1;
				else if (axis[i]>512*.1)
					axis[i]=512*.1;
					*/
			
			for (int i = 0; i<2; i++)
				if (axis[i]<-512*resolution)
					axis[i]=-512*resolution;
				else if (axis[i]>512*resolution)
					axis[i]=512*resolution;	

			
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
		public void addDeviceListener(EvDeviceObserver.DeviceListener listener)
			{
			event.addWeakListener(listener);
			}
		public void removeDeviceListener(EvDeviceObserver.DeviceListener listener)
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

	
	
	
	////////////
	
	
	
	private class FrivolousAutofocus implements HWAutoFocus
		{
	
		public FrivolousStage stage;
		
		public FrivolousAutofocus(FrivolousStage stage)
			{
			this.stage=stage;
			}
		
		public String getDescName()
			{
			return "Frivolous autofocus";
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
		public void addDeviceListener(EvDeviceObserver.DeviceListener listener)
			{
			event.addWeakListener(listener);
			}
		public void removeDeviceListener(EvDeviceObserver.DeviceListener listener)
			{
			event.remove(listener);
			}
	
		public boolean contAutoFocus=false;
		public boolean contFocusLock=false;
		public double offset=0;
		
		public void enableContinuousFocus(boolean enable)
			{
			contAutoFocus=enable;
			}
	
		public void fullFocus() throws IOException
			{
			double[] pos=stage.getStagePos();
			pos[2]=offset;
			stage.setStagePos(pos);
			}
	
		public double getAutoFocusOffset()
			{
			return offset;
			}
	
		public double getCurrentFocusScore()
			{
			return 0;
			}
	
		public double getLastFocusScore()
			{
			return 0;
			}
	
		public void incrementalFocus() throws IOException
			{
			double[] pos=stage.getStagePos();
			pos[2]=0;
			stage.setStagePos(pos);
			}
	
		public boolean isContinuousFocusEnabled()
			{
			return contAutoFocus;
			}
	
		public boolean isContinuousFocusLocked()
			{
			return contFocusLock;
			}
	
		public void setAutoFocusOffset(double offset)
			{
			this.offset=offset;
			}
	
		}
	
		
	
	//////////
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
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
		new FrivolousConfig();
		}

	
	
	
	
	/**
	 * Configuration window
	 */
	private class FrivolousConfig extends JFrame implements ActionListener 
		{
		private static final long serialVersionUID = 1L;
		private JButton bStartStop;
		private JTextField tfFileName=new JTextField();
		private JButton bBrowse=new JButton("Browse");
		
		public FrivolousConfig()
			{
			super("Frivolous configuration");

			tfFileName.setText(FrivolousModel.getStandardExperiment().getAbsolutePath());

			bStartStop = new JButton((model==null ? "Start" : "Stop"));
			bStartStop.addActionListener(this);

			bBrowse.addActionListener(this);
			
			setLayout(new GridLayout(1,1));
			add(EvSwingUtil.layoutEvenVertical(
					EvSwingUtil.layoutLCR(new JLabel("Experiment"), tfFileName, bBrowse),
					bStartStop
					));
			
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			pack();
			setVisible(true);
			} 

		public void actionPerformed(ActionEvent e)
			{
			if(e.getSource()==bStartStop)
				{
				if (model==null)
					{
					File f=new File(tfFileName.getText());
					model = new FrivolousModel(f);

					FrivolousCamera cam = new FrivolousCamera();
					FrivolousStage stage=new FrivolousStage();
					cam.seqAcqThread.start();
					hw.put("cam", cam);
					hw.put("stage", stage);
					hw.put("autofocus", new FrivolousAutofocus(stage));

					BasicWindow.updateWindows();
					
					bStartStop.setText("Stop");
					}
				else
					{
					model.stop();
					model = null;
					bStartStop.setText("Start");
					}
				}
			else if(e.getSource()==bBrowse)
				{
				JFileChooser fc=new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int ret=fc.showOpenDialog(this);
				if(ret==JFileChooser.APPROVE_OPTION)
					{
					File f=fc.getSelectedFile();
					tfFileName.setText(f.getAbsolutePath());
					}
				}
			}

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

	public boolean hasSampleLoadPosition(){return false;}
	public void setSampleLoadPosition(boolean b){}

	}
