package evplugin.pluginWindow;

//Auto-rewrap README? if so, then on WORDS, not on characters

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;

import evplugin.basicWindow.*;
import evplugin.ev.*;
import org.jdom.*;


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
	private JTextArea docArea=new JTextArea();
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
	public void windowPersonalSettings(Element root)
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
		plugins=EV.getPluginList();

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
  	JScrollPane listScroller = new JScrollPane(list);
  	listScroller.setAlignmentX(LEFT_ALIGNMENT);


  	//Put GUI together
  	JPanel left=new JPanel(new GridLayout(2,1));

  	docArea.setLineWrap(true);
  	miscArea.setLineWrap(true);
  	
  	JScrollPane docScrollPane = new JScrollPane(docArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
  	JSplitPane sp=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
  	add(sp);
  	sp.add(left);
  	sp.add(docScrollPane);

  	left.add(list);
  	left.add(new JScrollPane(miscArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

  	//Window overall things
  	setTitle(EV.programName+" Plugins");
  	pack();
  	setVisible(true);
  	setBounds(x,y,w,h);
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
		/*
		String misc="";
		misc+="Name:\n"+plugin.name+"\n\n";
		misc+="Author:\n"+plugin.author+"\n\n";
		if(!plugin.cite.equals(""))
			misc+="Cite:\n"+plugin.cite+"\n\n";
		miscArea.setText(misc);
		plugin.loadDoc();
		docArea.setText(plugin.doc);
		*/
		}

	
	}
