package endrov.driverNative;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jdom.Element;

import endrov.hardware.Device;
import endrov.hardware.DeviceProvider;
import endrov.hardware.PropertyType;
import endrov.recording.CameraImage;
import endrov.recording.HWCamera;
import endrov.recording.HWStage;

/**
 * Demo microscope
 * 
 * To add: condenser distance, cond loc, light intensity, choice of light, DIC, PhC, dark field, bright field, 
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class DemoScope extends DeviceProvider implements Device
	{

	
	public DemoScope()
		{
		hw.put("cam", new DevCamera());
		hw.put("stage", new DevStage());
		}
	
	public double[] stagePos=new double[]{0,0,0};
	
	
	/**
	 * Demo camera
	 */
	private class DevCamera implements HWCamera
		{
		public CameraImage snap()
			{
			BufferedImage im=new BufferedImage(320,200,BufferedImage.TYPE_BYTE_GRAY);
			Graphics g=im.getGraphics();
			
			g.setColor(Color.WHITE);
//			g.drawString(""+stagePos[0], 30, 30);

			int r=(int)stagePos[2];

			g.translate((int)stagePos[0], (int)stagePos[1]);
			
			
			g.fillOval(100-r, 100-r, 2*r, 2*r);
			
			CameraImage cim=new CameraImage();
			cim.bytesPerPixel=1;
			cim.w=im.getWidth();
			cim.h=im.getHeight();

			cim.pixels=im;
			
			
			
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
		public SortedMap<String, PropertyType> getPropertyTypes(){return new TreeMap<String, PropertyType>();}
		public String getPropertyValue(String prop){return null;}
		public Boolean getPropertyValueBoolean(String prop){return null;}
		public void setPropertyValue(String prop, boolean value){}
		public void setPropertyValue(String prop, String value){}
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
			stagePos[0]+=axis[0];
			stagePos[1]+=axis[1];
			stagePos[2]+=axis[2];
			}
		public void setStagePos(double[] axis)
			{
			stagePos[0]=axis[0];
			stagePos[1]=axis[1];
			stagePos[2]=axis[2];
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
		public SortedMap<String, PropertyType> getPropertyTypes(){return new TreeMap<String, PropertyType>();}
		public String getPropertyValue(String prop){return null;}
		public Boolean getPropertyValueBoolean(String prop){return null;}
		public void setPropertyValue(String prop, boolean value){}
		public void setPropertyValue(String prop, String value){}
		}




	public Set<Device> autodetect()
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
	public Device newProvided(String s)
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

	public SortedMap<String, PropertyType> getPropertyTypes()
		{
		return new TreeMap<String, PropertyType>();
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
	
	
	
	}
