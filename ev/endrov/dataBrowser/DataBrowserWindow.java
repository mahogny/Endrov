package endrov.dataBrowser;

import org.jdom.Element;

import endrov.basicWindow.BasicWindow;
import endrov.data.EvData;

/**
 * Data browsing - work on objects
 * @author Johan Henriksson
 *
 */
public class DataBrowserWindow extends BasicWindow
	{
	private static final long serialVersionUID = 1L;

	/**
	 * Essentially: only the data tree. allow new operations to be registered.
	 * either on right-click or by clicking on a button. or menu?
	 * 
	 * actually, EvSelection can point to EvContainer. then operations can go into data menu
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	
	
	@Override
	public void dataChangedEvent()
		{
		}

	@Override
	public void freeResources()
		{
		}

	@Override
	public void loadedFile(EvData data)
		{
		}

	@Override
	public void windowSavePersonalSettings(Element e)
		{
		}
	
	
	
	

	}
