package endrov.data.gui;

import java.util.Vector;

import org.jdom.Element;

import endrov.core.EndrovCore;
import endrov.core.PersonalConfig;
import endrov.data.EvData;
import endrov.data.RecentReference;
import endrov.gui.window.EvBasicWindow;

/**
 * GUI integration for data objects
 * 
 * @author Johan Henriksson
 *
 */
public class EvDataGUI
	{

	/**
	 * List of recently loaded files
	 */
	public static Vector<RecentReference> recentlyLoadedFiles=new Vector<RecentReference>();
	
	
	/**
	 * Data opened by the user and hence visible as a working data set
	 */
	public static Vector<EvData> openedData=new Vector<EvData>();

	
	
	/** 
	 * Register loaded data in GUI 
	 */
	public static void registerOpenedData(EvData data)
		{
		if(data!=null)
			{
			EvDataGUI.openedData.add(data);
			//EvData.registerLoadedDataGUI(data);
			RecentReference rref=data.getRecentEntry();
			if(rref!=null)
				{
				boolean isAdded=false;
				for(RecentReference rref2:EvDataGUI.recentlyLoadedFiles)
					if(rref2.url.equals(rref.url))
						isAdded=true;
				if(!isAdded)
					{
					EvDataGUI.recentlyLoadedFiles.add(0,rref);
					while(EvDataGUI.recentlyLoadedFiles.size()>10)
						EvDataGUI.recentlyLoadedFiles.remove(EvDataGUI.recentlyLoadedFiles.size()-1);
					}
				}
			EvBasicWindow.updateWindows(); 
			}
		}

	/**
	 * Unregister loaded data from the GUI 
	 */
	public static void unregisterOpenedData(EvData data)
		{
		EvDataGUI.openedData.remove(data);
		EvBasicWindow.updateWindows();  
		}

	


	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{		
	
		//Store recent entries in personal config
		EndrovCore.addPersonalConfigLoader("recentlyLoaded",new PersonalConfig()
			{
			public void loadPersonalConfig(Element e)
				{
				RecentReference rref=new RecentReference(e.getAttributeValue("desc"),e.getAttributeValue("url"));
				EvDataGUI.recentlyLoadedFiles.add(rref);
				EvBasicWindow.updateWindows(); //Semi-ugly. Done many times.
				}
			public void savePersonalConfig(Element root)
				{
				try
					{
					for(RecentReference rref:EvDataGUI.recentlyLoadedFiles)
						{
						Element e=new Element("recentlyLoaded");
						e.setAttribute("desc",rref.descName);
						e.setAttribute("url",rref.url);
						root.addContent(e);
						}
					}
				catch (Exception e)
					{
					e.printStackTrace();
					}
				}
			});
		
		EvBasicWindow.addBasicWindowExtension(new EvDataMenu());
//		BasicWindow.updateWindows();
		//maybe update on new extension?
		//priorities on update? windows should really go last. then the updateWindows call here is solved.
		}
	
	}
