package endrov.frivolous;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jdom.Element;

import endrov.frivolous.model.Model;
import endrov.hardware.Device;
import endrov.hardware.DeviceProvider;
import endrov.hardware.EvHardware;
import endrov.hardware.PropertyType;
import endrov.recording.CameraImage;
import endrov.recording.HWCamera;
import endrov.recording.HWStage;

public class Frivolous extends DeviceProvider implements Device {

	private static Map<String, Class<? extends Device>> hardwareProvided=new TreeMap<String, Class<? extends Device>>();
	private Model model;
	
	public static void initPlugin() {}
	static
		{
		EvHardware.root.hw.put("fr",new Frivolous());
		}
	
	public Frivolous()
		{
		model = new Model();
		hw.put("cam", new FrivolousCamera());
		hw.put("stage", new FrivolousStage());
		}
	
	public double[] stagePos=new double[]{0,0,0}; 
		
	private class FrivolousCamera implements HWCamera {

		public String getDescName() {
			return "Frivolous camera";
		}

		public SortedMap<String, String> getPropertyMap() {
			return new TreeMap<String, String>();
		}

		public SortedMap<String, PropertyType> getPropertyTypes() {
			return new TreeMap<String, PropertyType>();
		}

		public String getPropertyValue(String prop) {
			return null;
		}

		public Boolean getPropertyValueBoolean(String prop) {
			return null;
		}

		public void setPropertyValue(String prop, boolean value) {
		}

		public void setPropertyValue(String prop, String value) {
		}

		public double getResMagX() {
			return 1;
		}

		public double getResMagY() {
			return 1;
		}

		public boolean hasConfigureDialog() {
			return false;
		}

		public void openConfigureDialog() {
		}

		public CameraImage snap() {
			
			
			
			/*BufferedImage im=new BufferedImage(320,200,BufferedImage.TYPE_BYTE_GRAY);
			Graphics g=im.getGraphics();
			
			g.setColor(Color.WHITE);
//			g.drawString(""+stagePos[0], 30, 30);
*/
			int r=(int)stagePos[2];
			/*
			
			int r=(int)stagePos[2];

			g.translate((int)stagePos[0], (int)stagePos[1]);
			*/
			
			//g.fillOval(100-r, 100-r, 2*r, 2*r);
			model.convolve();
			BufferedImage im = model.getImage();
			CameraImage cim=new CameraImage();
			cim.bytesPerPixel=1;
			cim.w=im.getWidth();
			cim.h=im.getHeight();

			cim.pixels=im;
			System.out.println("Oh, snap...");
			return cim;
		}
	}

	private class FrivolousStage implements HWStage {
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
		public SortedMap<String, PropertyType> getPropertyTypes(){return new TreeMap<String, PropertyType>();}
		public String getPropertyValue(String prop){return null;}
		public Boolean getPropertyValueBoolean(String prop){return null;}
		public void setPropertyValue(String prop, boolean value){}
		public void setPropertyValue(String prop, String value){}
		
		public boolean hasConfigureDialog(){return false;}
		public void openConfigureDialog(){}

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
		return Arrays.asList("frivolous");
		}
	public Device newProvided(String s)
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
		return false;
		}


	public void setPropertyValue(String prop, boolean value)
		{
		}


	public void setPropertyValue(String prop, String value)
		{
		}
	
	
	public boolean hasConfigureDialog(){return false;}
	public void openConfigureDialog(){}

}
