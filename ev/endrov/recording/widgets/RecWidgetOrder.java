/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.widgets;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import endrov.gui.icon.BasicIcon;
import endrov.util.EvSwingUtil;
import endrov.util.JImageButton;

/**
 * Widget for recording settings: Order of dimensions settings
 * @author Johan Henriksson
 *
 */
public class RecWidgetOrder extends JPanel
	{
	private static final long serialVersionUID = 1L;
	
	/**
	 * One ordered entry
	 * @author Johan Henriksson
	 *
	 */
	public static class OrderEntry
		{
		public final String id, desc;

		public OrderEntry(String id, String desc)
			{
			this.id = id;
			this.desc = desc;
			}
		}

	public ArrayList<JLabel> wlist=new ArrayList<JLabel>();
	public ArrayList<OrderEntry> entrylist=new ArrayList<OrderEntry>();
	
	public JComponent p=new JPanel();
	
	public RecWidgetOrder()
		{
		this(RecSettingsDimensionsOrder.createStandard());
		}
	
	public RecWidgetOrder(RecSettingsDimensionsOrder data)
		{
		setBorder(BorderFactory.createTitledBorder("Order"));
		setLayout(new BorderLayout());
		add(p,BorderLayout.CENTER);
		entrylist.addAll(data.entrylist);
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
	
	public RecSettingsDimensionsOrder getSettings()
		{
		RecSettingsDimensionsOrder o=new RecSettingsDimensionsOrder();
		o.entrylist.addAll(entrylist);
		return o;
		}
	
	

	}
