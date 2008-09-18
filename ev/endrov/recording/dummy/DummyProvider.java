package endrov.recording.dummy;

import java.util.*;

import org.jdom.Element;

import endrov.hardware.*;

public class DummyProvider extends HardwareProvider
	{
	/*
	public static Camera cam=new Camera(){
		public String getDescName()
			{
			return "Dummy camera";
			}
		public SortedMap<String,PropertyType> getPropertyTypes()
			{
			TreeMap<String, PropertyType> map=new TreeMap<String, PropertyType>();
			return map;
			}
		public Map<String,String> getPropertyMap()
			{
			
			}
		public String getPropertyValue(String prop)
			{
			
			}
		
		
	};
	public static Stage stage=new Stage(){
	public String getDescName()
		{
		return "Dummy stage";
		}
	
	};*/

	public Set<Hardware> providing=new HashSet<Hardware>();
	
	public DummyProvider()
		{
		//provider should not be static!!
//		providing.add(cam);
//		providing.add(stage);
		}
	
	
	@Override
	public Set<Hardware> autodetect()
		{
		return providing;
		}

	@Override
	public void getConfig(Element root)
		{
		}

	@Override
	public String getName()
		{
		return "Dummy Provider";
		}

	@Override
	public List<String> provides()
		{
		// TODO Auto-generated method stub
		return null;
		}

	@Override
	public void setConfig(Element root)
		{
		// TODO Auto-generated method stub
		
		}

	}
