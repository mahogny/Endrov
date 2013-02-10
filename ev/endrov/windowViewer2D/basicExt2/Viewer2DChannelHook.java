package endrov.windowViewer2D.basicExt2;

import java.awt.BorderLayout;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import endrov.data.EvContainer;
import endrov.data.EvObject;
import endrov.gui.EvSwingUtil;
import endrov.gui.component.EvComboColor;
import endrov.gui.component.EvComboObject;
import endrov.recording.liveWindow.LiveHistogramView;
import endrov.windowViewer2D.Viewer2DWindow;
import endrov.windowViewer2D.Viewer2DWindowHook;

public class Viewer2DChannelHook implements Viewer2DWindowHook
	{

	private static class OneChannel extends JPanel
		{
		private static final long serialVersionUID = 1L;
		
		//This should be relative to the base. Thus it consumes less space
		//also makes it possible to remember the last channel
		private	JComboBox comboChan=new JComboBox();
		
		LiveHistogramView histo=new LiveHistogramView();
		
		EvComboColor comboColor=new EvComboColor(false);
				
		JCheckBox cbColorID=new JCheckBox("ID-color");
		
		public OneChannel()
			{
			setLayout(new BorderLayout());
			add(EvSwingUtil.layoutCompactVertical(
					comboChan, 
					histo,
					EvSwingUtil.layoutLCR(
							null, 
							comboColor, 
							cbColorID)),
					BorderLayout.CENTER);
			}
		
		}
	
	
	private JButton addChannel=new JButton("Add channel");

	private OneChannel one=new OneChannel();
	
	private final EvComboObject metaCombo=new EvComboObject(new LinkedList<EvObject>(),true,false)
		{
		static final long serialVersionUID=0;
		public boolean includeObject(EvContainer cont)
			{
			return true;
			}
		};
			
	
	public void fillMenus(Viewer2DWindow w)
		{

		
		w.sidePanelItems.add(metaCombo);
		
		
		w.sidePanelItems.add(addChannel);
		w.sidePanelItems.add(one);
		}

	public void datachangedEvent()
		{
		}
	
	
	}
