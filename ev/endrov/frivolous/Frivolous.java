package endrov.frivolous;

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
			TreeMap<String, String> p = new TreeMap<String, String>();
			p.put("Numerical Aperture", ""+model.getSettings().na);
			return p;
		}

		public SortedMap<String, PropertyType> getPropertyTypes() {
			TreeMap<String, PropertyType> pt = new TreeMap<String, PropertyType>();
			PropertyType p = new PropertyType();
			p.rangeUpper = 0.95;
			p.rangeLower = 0.25;
			p.hasRange = true;
			pt.put("Numerical Aperture", p);
			return pt;
		}

		public String getPropertyValue(String prop) {
			if (prop.equals("Numerical Aperture")) return ""+model.getSettings().na;
			return getPropertyMap().get(prop);
		}

		public Boolean getPropertyValueBoolean(String prop) {
			return null;
		}

		public void setPropertyValue(String prop, boolean value) {
		}

		public void setPropertyValue(String prop, String value) {
			if (prop.equals("Numerical Aperture")) model.getSettings().na = Double.parseDouble(value);
			System.out.println(prop+" "+value);
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

			int r=(int)stagePos[2];

			model.convolve();
			BufferedImage im = model.getImage();
			CameraImage cim=new CameraImage();
			cim.bytesPerPixel=1;
			cim.w=im.getWidth();
			cim.h=im.getHeight();

			cim.pixels=im;
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
			model.getSettings().offsetZ = stagePos[2];
			model.updatePSF();
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
