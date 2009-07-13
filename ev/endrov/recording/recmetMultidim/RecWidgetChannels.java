package endrov.recording.recmetMultidim;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import endrov.basicWindow.icon.BasicIcon;
import endrov.util.EvSwingUtil;
import endrov.util.JImageButton;

/**
 * Widget for recording settings: Channel settings
 * @author Johan Henriksson
 *
 */
public class RecWidgetChannels extends JPanel
	{
	private static final long serialVersionUID = 1L;

	
	/**
	 * Current settings or
	 * Channels===
	 * 
	 * select config groups
	 * 
	 * one Channel:
	 * * which group
	 * * exposure, or auto
	 * * compensate exposure over Z
	 * * z-skip
	 * * z-start (offset)
	 * * t-skip
	 * * z-scan while recording? a la emilie
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	
	public static class OrderEntry
		{
		public final String id, desc;

		private OrderEntry(String id, String desc)
			{
			this.id = id;
			this.desc = desc;
			}
		}

	//Standard entries
	public static final OrderEntry entryChannel=new OrderEntry("channel","Channel");
	public static final OrderEntry entryPosition=new OrderEntry("position","Position");
	public static final OrderEntry entrySlice=new OrderEntry("slice","Slice");
	

	public ArrayList<JLabel> wlist=new ArrayList<JLabel>();
	public ArrayList<OrderEntry> entrylist=new ArrayList<OrderEntry>();
	
	public JComponent p=new JPanel();
	
	public RecWidgetChannels()
		{
		this(entryPosition,entryChannel,entrySlice);
		}
	
	public RecWidgetChannels(OrderEntry... entry)
		{
		setBorder(BorderFactory.createTitledBorder("Order"));
		setLayout(new BorderLayout());
		add(p,BorderLayout.CENTER);
		entrylist.addAll(Arrays.asList(entry));
		layoutOrder();
		}
	
	private void layoutOrder()
		{
		wlist.clear();
		p.removeAll();
		p.setLayout(new GridLayout(entrylist.size(),1));
		int row=0;
		for(OrderEntry e:entrylist)
			{
			JLabel label=new JLabel(e.desc);
			wlist.add(label);
			
			final int thisRow=row;
			JButton bUp=new JImageButton(BasicIcon.iconButtonUp,"Move this entry up. Bottom-most entry is changed most times (think of it as nested for-loops).");
			JButton bDown=new JImageButton(BasicIcon.iconButtonDown,"Move this entry down. Bottom-most entry is changed most times (think of it as nested for-loops).");
			bUp.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
					{
					OrderEntry ee=entrylist.get(thisRow-1);
					entrylist.set(thisRow-1,entrylist.get(thisRow));
					entrylist.set(thisRow,ee);
					wlist.get(thisRow-1).setText(entrylist.get(thisRow-1).desc);
					wlist.get(thisRow).setText(entrylist.get(thisRow).desc);
					//RecWidgetOrder.this.repaint();
					}
			});
			bDown.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
					{
					OrderEntry ee=entrylist.get(thisRow+1);
					entrylist.set(thisRow+1,entrylist.get(thisRow));
					entrylist.set(thisRow,ee);
					wlist.get(thisRow+1).setText(entrylist.get(thisRow+1).desc);
					wlist.get(thisRow).setText(entrylist.get(thisRow).desc);
					//RecWidgetOrder.this.repaint();
					}
			});

			
			
			JComponent c;
			if(row==0)
				c=EvSwingUtil.layoutLCR(null, label, bDown);
			else if(row==entrylist.size()-1)
				c=EvSwingUtil.layoutLCR(null, label, bUp);
			else
				c=EvSwingUtil.layoutLCR(null, label, EvSwingUtil.layoutCompactHorizontal(bUp, bDown));
			p.add(c);
			row++;
			}
		
		
		}
	

	}
