package endrov.frameTime;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import endrov.basicWindow.EvDropDownButton;
import endrov.data.EvData;
import endrov.data.EvPath;

/**
 * Drop-down menu to select frametime for use with frametime spinners
 * @author Johan Henriksson
 */
public class FrameTimeDropDown extends EvDropDownButton
	{
	private static final long serialVersionUID = 1L;
	
	private List<EvFrameEditor> listeners=new LinkedList<EvFrameEditor>();
	
	
	
	public FrameTimeDropDown()
		{
		super(FrameTime.icon,"Set map frame<->time");
		}

	public JPopupMenu createPopup()
		{
		JPopupMenu m=new JPopupMenu();
		
		JMenuItem miNull=new JMenuItem("<No map>");
		miNull.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent ee){click(null);}});
		m.add(miNull);
		
		for(EvData data:EvData.metadata)
			{
			for(Map.Entry<EvPath, FrameTime> e:data.getIdObjectsRecursive(FrameTime.class).entrySet())
				{
				JMenuItem mi=new JMenuItem(e.getKey().toString());
				final FrameTime ft=e.getValue();
				mi.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent ee){click(ft);}});
				m.add(mi);
				}
			}
		return m;
		}

	private void click(FrameTime ft)
		{
		for(EvFrameEditor l:listeners)
			l.setFrameTime(ft);
		}
	
	public void addActionListener(EvFrameEditor l)
		{
		listeners.add(l);
		}
	
	}
