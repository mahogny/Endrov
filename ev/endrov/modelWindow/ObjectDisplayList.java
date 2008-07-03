package endrov.modelWindow;

import java.awt.*;
import java.awt.event.*;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import endrov.data.*;


/**
 * 
 * @author Johan Henriksson
 */
public class ObjectDisplayList extends JPanel
	{
	public static final long serialVersionUID=0;
	
	public ObjectDisplayList()
		{
		setBorder(BorderFactory.createTitledBorder("Display"));
		setLayout(new GridBagLayout());
		updateList();
		}

	private WeakHashMap<EvObject, Object> toNotDisplay=new WeakHashMap<EvObject, Object>();
	private WeakReference<EvData> evdata=new WeakReference<EvData>(null);
	//can ask to be notified
	
	private Vector<ChangeListener> listeners=new Vector<ChangeListener>();
	
	public void addChangeListener(ChangeListener l)
		{
		listeners.add(l);
		}
	public void removeChangeListener(ChangeListener l)
		{
		listeners.remove(l);
		}
	
	
	public void setData(EvData evdata)
		{
		this.evdata=new WeakReference<EvData>(evdata);
		updateList();
		}
	
	public boolean toDisplay(EvObject ob)
		{
		return !toNotDisplay.containsKey(ob);
		}
	
	
	public void updateList()
		{
		removeAll();
		EvData d=evdata.get();
		if(d!=null && !d.metaObject.isEmpty())
			{
			int countb=0;
			for(Map.Entry<String, EvObject> entry:d.metaObject.entrySet())
				{
				final EvObject thisObject=entry.getValue();
				JCheckBox cb=new JCheckBox(""+entry.getKey()+" "+thisObject.getMetaTypeDesc(),toDisplay(thisObject));
				GridBagConstraints cr=new GridBagConstraints();	cr.weightx=1;	cr.gridy=countb;	cr.fill=GridBagConstraints.HORIZONTAL;
				add(cb,cr);
				countb++;
				
				final ObjectDisplayList tthis=this;
				
				cb.addItemListener(new ItemListener()
					{
						public void itemStateChanged(ItemEvent e)
							{
							if(((JCheckBox)e.getSource()).isSelected())
								toNotDisplay.remove(thisObject);
							else
								toNotDisplay.put(thisObject,null);
							for(ChangeListener l:listeners)
								l.stateChanged(new ChangeEvent(tthis));
							}
					});
				}
			}
		else
			{
			add(new JLabel("<empty>"));
			}
		revalidate();
		}
	
	
	}
