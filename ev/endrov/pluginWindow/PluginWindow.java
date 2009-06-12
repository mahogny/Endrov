package endrov.pluginWindow;


import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import endrov.basicWindow.*;
import endrov.data.EvData;
import endrov.ev.*;

import org.jdom.*;

//TODO when moving with keyboard arrows, text is not updated

/**
 * Browse plugins
 * @author Johan Henriksson
 */
public class PluginWindow extends BasicWindow 
	{
	static final long serialVersionUID=0;
	public static void initPlugin() {}
	static
		{
		BasicWindow.addBasicWindowExtension(new PluginWindowBasic());
		}
	

	
	//GUI components
	private JTextArea miscArea=new JTextArea();
	private Vector<PluginInfo> plugins;

	
	private JList list = new JList()
  	{
  	static final long serialVersionUID=0;
  	//Subclass JList to workaround bug 4832765, which can cause the
  	//scroll pane to not let the user easily scroll up to the beginning
  	//of the list.  An alternative would be to set the unitIncrement
  	//of the JScrollBar to a fixed value. You wouldn't get the nice
  	//aligned scrolling, but it should work.
  	public int getScrollableUnitIncrement(Rectangle visibleRect,int orientation,int direction)
  		{
  		int row;
  		if (orientation == SwingConstants.VERTICAL &&	direction < 0 && (row = getFirstVisibleIndex()) != -1) 
  			{
  			Rectangle r = getCellBounds(row, row);
  			if ((r.y == visibleRect.y) && (row != 0)) 
  				{
  				Point loc = r.getLocation();
  				loc.y--;
  				int prevIndex = locationToIndex(loc);
  				Rectangle prevR = getCellBounds(prevIndex, prevIndex);
  				if (prevR == null || prevR.y >= r.y) 
  					return 0;
  				return prevR.height;
  				}
  			}
  		return super.getScrollableUnitIncrement(visibleRect, orientation, direction);
  		}
  	};
	

	/**
	 * Store down settings for window into personal config file
	 */
	public void windowSavePersonalSettings(Element root)
		{
		}

	

	/**
	 * Make a new window at default location
	 */
	public PluginWindow()
		{
		this(100,100,1000,600);
		}
	
	/**
	 * Make a new window at some specific location
	 */
	public PluginWindow(int x, int y, int w, int h)
		{				
		plugins=new Vector<PluginInfo>();
		plugins.addAll(PluginInfo.getPluginList());
		Collections.sort(plugins, new Comparator<PluginInfo>(){
			public int compare(PluginInfo o1, PluginInfo o2)
				{
				return o1.toString().compareTo(o2.toString());
				}
		});
		
		
		list.setListData(plugins);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
  	list.setLayoutOrientation(JList.VERTICAL);
  	list.setVisibleRowCount(-1);
  	list.addMouseListener(new MouseAdapter() 
  		{
  		public void mouseClicked(MouseEvent e) 
  			{
  			showPlugin((PluginInfo)list.getSelectedValue());
  			}
  		});

  	setLayout(new GridLayout(1,1));

  	//Put GUI together
  	miscArea.setLineWrap(true);
  	
  	//JScrollPane docScrollPane = new JScrollPane(docArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
  	JSplitPane sp=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
  	add(sp);
  	
  	sp.add(new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
  	sp.add(new JScrollPane(miscArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));


  	//Window overall things
  	setTitleEvWindow("Plugins");
  	packEvWindow();
  	setVisibleEvWindow(true);
  	setBoundsEvWindow(x,y,w,h);
		}


	/*
	 * (non-Javadoc)
	 * @see client.BasicWindow#dataChanged()
	 */
	public void dataChangedEvent()
		{
		}

	public void showPlugin(PluginInfo plugin)
		{
		String misc="";
		misc+="Name:\n"+plugin.pdef.getPluginName()+"\n\n";
		misc+="Author:\n"+plugin.pdef.getAuthor()+"\n\n";
		String cite=plugin.pdef.cite();
		if(!cite.equals(""))
			misc+="Cite:\n"+cite+"\n\n";
		miscArea.setText(misc);
		}

	public void loadedFile(EvData data){}
	public void freeResources(){}

	}
