/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.gui.component;

import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.*;

/**
 * A hybrid of toggle buttons and a combo. If there is space,
 * a group of toggle buttons will be used, otherwise a combo box
 * @author Johan Henriksson
 *
 *
 * Unimplemented design: keep a group of components (weakhashmap),
 * let these decide on maximum width. separate class.
 *
 */
public class JSmartToggleCombo extends JPanel
	{
	private static final long serialVersionUID=0;

/*	
	private static class SmallJToggleButton extends JToggleButton
		{
		private static final long serialVersionUID=0;
		public SmallJToggleButton(String s)
			{
			super(s);
			BufferedImage im=new BufferedImage(1,1,BufferedImage.TYPE_3BYTE_BGR);
			FontMetrics fm=im.getGraphics().getFontMetrics();
			}
		public Dimension getMinimumSize()
			{
			Dimension d=super.getMinimumSize();
			d.width=new JLabel(getText()).getWidth()+5;
			return d;
			}
		}
	*/
	final private Vector<String> names;
	
	private List<ActionListener> actionListeners=new LinkedList<ActionListener>();
	private ButtonGroup allButtons=new ButtonGroup();
	
	//could add the ability to suggest icons
	
	boolean useCombo;
	
	JPanel buttonPanel=null;
	JComboBox<String> combo=null;
	private int cachedIndex=0;
	
	public JSmartToggleCombo(final Vector<String> names)
		{
		this.names=names;
		setLayout(new GridLayout(1,1));
		
		int maxWidth=200;
		if(getMinimumSizeOfButtons()<maxWidth)
			{
			//Place buttons
			useCombo=false;
			//TODO allow more compact rep
			buttonPanel=new JPanel(new GridLayout(1,names.size()));
			add(buttonPanel);
			for(int i=0;i<names.size();i++)
				{
				final int fi=i;
				//JToggleButton b=new SmallJToggleButton(names.get(i));
				String label=names.get(i);
				if(label.equals(""))
					label=" ";
				JToggleButton b=new JToggleButton(label);
				fixMargin(b);
				if(i==0)
					b.setSelected(true);
				allButtons.add(b);
				b.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e)
						{
						userSetSelectedIndex(fi);
						}
				});
				buttonPanel.add(b);
				}
			
			
			}
		else
			{
			//Place combo
			combo=new JComboBox<String>(names);
			useCombo=true;
			add(combo);
			
			combo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
				{
				userSetSelectedIndex(combo.getSelectedIndex());
				}
			});
			
			
			}
		
		
		}
	
	private AbstractButton fixMargin(AbstractButton b)
		{
		b.setMargin(new Insets(2,2,2,2));
		return b;
		}
	
	private int getMinimumSizeOfButtons()
		{
		int totSize=0;
		for(String s:names)
			//totSize+=new SmallJToggleButton(s).getMinimumSize().width;
			totSize+=fixMargin(new JToggleButton(s)).getMinimumSize().width;
		return totSize;
		}
	
	private void userSetSelectedIndex(int i)
		{
		cachedIndex=i;
		ActionEvent ae=new ActionEvent(this,0,null);
		for(ActionListener list:actionListeners)
			list.actionPerformed(ae);
		}
	
	/**
	 * Set selected index. this will fire an event like normal Swing widgets
	 */
	public void setSelectedIndex(int i)
		{
		//cachedIndex will be updated with the callbacks
		if(useCombo)
			combo.setSelectedIndex(i);
		else
			{
			((JToggleButton)buttonPanel.getComponent(i)).setSelected(true);
			}
		}

	public void setSelectedItem(String s)
		{
		if(useCombo)
			combo.setSelectedItem(s);
		else
			{
			for(int i=0;i<buttonPanel.getComponentCount();i++)
				{
				JToggleButton tb=(JToggleButton)buttonPanel.getComponent(i);
				if(tb.getText().equals(s))
					tb.setSelected(true);
				}
			}
		}
	
	public int getSelectedIndex()
		{
		return cachedIndex;
		}
	
	public String getSelectedItem()
		{
		return names.get(cachedIndex);
		}
	
	public void addActionListener(ActionListener list)
		{
		actionListeners.add(list);
		}
	
	
	}
