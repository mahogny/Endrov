package endrov.data;

import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import ucar.nc2.dataset.conv.MADISStation;

public class DataObjectSelectMenu
	{
	public interface Callback<E>
		{
		public void select(EvData data, EvPath path, E con);
		}
	
	public static void create(JMenu menu, Class<EvObject> type)
		{
		for(EvData data:EvData.metadata)
			{
			JMenu dataMenu=new JMenu(data.getMetadataName());
			menu.add(dataMenu);
			
			
			
			}
		}
	
	private static <E> JMenuItem createDataMenu(JMenu menu, EvData data, Class<E> type)
		{
		for(Map.Entry<String, EvObject> e:data.metaObject.entrySet())
			{
			createObjectMenu(menu, e.getKey(), e.getValue(), type);
			}
		
		
		}
	
	private static JMenuItem createObjectMenu(JMenu menu, String obname, EvObject ob, Class<EvObject> type)
		{
		if(type.isInstance(ob))
			{
			//menu.addActionListener(arg0)
			
			JMenuItem mi=new JMenuItem(obname);
			mi.addActionListener(l)
			
			}
		JMenu obmenu=new JMenu();
		
		
		}

	
	}
