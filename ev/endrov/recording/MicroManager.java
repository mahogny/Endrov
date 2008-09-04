package endrov.recording;

import java.util.LinkedList;
import java.util.List;

import org.jdom.Element;

import endrov.hardware.Hardware;
import endrov.hardware.HardwareProvider;

public class MicroManager extends HardwareProvider
	{
	public String getName()
		{
		return "umanager";
		}
	
	
	
	public List<Hardware> autodetect()
		{
		return null;
		}

	public void getConfig(Element root)
		{
		}

	public void setConfig(Element root)
		{
		}
	
	public List<String> provides()
		{
		List<String> list=new LinkedList<String>();

		list.add("xystage");
		list.add("magnifier");
		
		
		return list;
		}

	}
