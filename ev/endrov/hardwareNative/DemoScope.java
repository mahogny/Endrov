/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.hardwareNative;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jdom.Element;

import endrov.hardware.DevicePropertyType;
import endrov.hardware.EvDevice;
import endrov.hardware.EvDeviceObserver;
import endrov.hardware.EvDeviceProvider;
import endrov.recording.CameraImage;
import endrov.recording.device.HWCamera;
import endrov.recording.device.HWStage;
import endrov.util.EvMathUtil;

/**
 * Demo microscope
 * 
 * To add: condenser distance, cond loc, light intensity, choice of light, DIC, PhC, dark field, bright field, 
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class DemoScope extends EvDeviceProvider implements EvDevice
	{

	
	public DemoScope()
		{
		hw.put("cam", new DevCamera());
		hw.put("stage", new DevStage());
		}
	
	public double[] stagePos=new double[]{0,0,0};
	
	
	private Random rand=new Random();
	
	/**
	 * Demo camera
	 */
	private class DevCamera implements HWCamera
		{
		public CameraImage snap()
			{
			int w=320;
			int h=200;
			BufferedImage im=new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
			Graphics g=im.getGraphics();
			
			g.setColor(Color.WHITE);
//			g.drawString(""+stagePos[0], 30, 30);

			int r=(int)stagePos[2];

			g.translate((int)stagePos[0], (int)stagePos[1]);
			
			 
			int[] arr=new int[w*h];
			for(int i=0;i<arr.length;i++)
				arr[i]=EvMathUtil.nextPoisson(rand, 10)*3;//(int)(Math.random()*30);
			im.getRaster().setSamples(0, 0, w, h, 0, arr);
			
			g.fillOval(100-r, 100-r, 2*r, 2*r);
			
			//TODO support other byte types

			CameraImage cim=new CameraImage(im.getWidth(), im.getHeight(), 1, im, 1, "None");
			
			/*
			int[] allpi=new int[im.getWidth()];
			byte[] allpb=new byte[im.getWidth()*im.getHeight()];
			for(int y=0;y<cim.h;y++)
				{
				im.getRaster().getPixels(0,y,im.getWidth(),1,allpi);
				int off=y*cim.w;
				for(int i=0;i<allpi.length;i++)
					allpb[off+i]=(byte)allpi[i];
				}
			cim.pixels=allpb;*/
			
			return cim;
			}
		public String getDescName()
			{
			return "Demo camera";
			}
		public SortedMap<String, String> getPropertyMap(){return new TreeMap<String, String>();}
		public SortedMap<String, DevicePropertyType> getPropertyTypes(){return new TreeMap<String, DevicePropertyType>();}
		public String getPropertyValue(String prop){return null;}
		public Boolean getPropertyValueBoolean(String prop){return null;}
		public void setPropertyValue(String prop, boolean value){}
		public void setPropertyValue(String prop, String value){}
	
		public boolean hasConfigureDialog(){return false;}
		public void openConfigureDialog(){}
		
		
		
		
		//TODO all below
		
		public double getSequenceCapacityFree()
			{
			return 1;
			}
		public boolean isDoingSequenceAcq()
			{
			// TODO Auto-generated method stub
			return false;
			}
		public CameraImage snapSequence() throws Exception
			{
			// TODO Auto-generated method stub
			return null;
			}
		public void startSequenceAcq(double interval)
				throws Exception
			{
			// TODO Auto-generated method stub
			
			}
		public void stopSequenceAcq()
			{
			// TODO Auto-generated method stub
			
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
		public long getCamWidth() {
			
			return 320;
		}
		public long getCamHeight() {
			
			return 200;
		}

		}

	/**
	 * Demo stage
	 */
	private class DevStage implements HWStage
		{
		//Simulate moving stage?
		
		public String[] getAxisName(){return new String[]{"x","y","z"};}
		public int getNumAxis(){return 3;}
		public double[] getStagePos()
			{
			return new double[]{stagePos[0],stagePos[1],stagePos[2]};
			}
		public void setRelStagePos(double[] axis)
			{
			for(int i=0;i<3;i++)
				stagePos[i]+=axis[i];
			//System.out.println("curpos "+stagePos[0]+"  "+stagePos[1]+"   "+stagePos[2]);
			}
		public void setStagePos(double[] axis)
			{
			for(int i=0;i<3;i++)
				stagePos[i]=axis[i];
			}
		public void goHome()
			{
			for(int i=0;i<3;i++)
				stagePos[i]=0;
			}
		public String getDescName()
			{
			return "Demo stage";
			}
		public SortedMap<String, String> getPropertyMap(){return new TreeMap<String, String>();}
		public SortedMap<String, DevicePropertyType> getPropertyTypes(){return new TreeMap<String, DevicePropertyType>();}
		public String getPropertyValue(String prop){return null;}
		public Boolean getPropertyValueBoolean(String prop){return null;}
		public void setPropertyValue(String prop, boolean value){}
		public void setPropertyValue(String prop, String value){}
		
		public boolean hasConfigureDialog(){return false;}
		public void openConfigureDialog(){}

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
		public boolean getSampleLoadPosition(){return false;}
		
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
		return null;
		}
	public EvDevice newProvided(String s)
		{
		return null; //TODO
		}

	public void setConfig(Element root)
		{
		}

	public String getDescName()
		{
		return "DemoScope";
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
	
	public void openConfigureDialog(){}


	public EvDeviceObserver event=new EvDeviceObserver();
	public void addDeviceListener(EvDeviceObserver.DeviceListener listener)
		{
		event.addWeakListener(listener);
		}
	public void removeDeviceListener(EvDeviceObserver.DeviceListener listener)
		{
		event.remove(listener);
		}
	}
