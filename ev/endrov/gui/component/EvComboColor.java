/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.gui.component;

import java.awt.Color;
import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import endrov.gui.EvColor;

/**
 * Combo select for colors
 * @author Johan Henriksson
 */
public class EvComboColor extends JComboBox//<EvColor>
	{
	static final long serialVersionUID=0;
	
	/**
	 * Cell renderer
	 * @author Johan Henriksson
	 *
	 */
	private static class Renderer extends JLabel implements ListCellRenderer//<EvColor>
		{
		private static final long serialVersionUID = 1L;

		private boolean showText;
		
		public Renderer(boolean showText)
			{
			setOpaque(true);
			setHorizontalAlignment(LEFT);
			setVerticalAlignment(CENTER);
			this.showText=showText;
			}

		public Component getListCellRendererComponent(JList/*<? extends EvColor>*/ list,	Object value,	int index, boolean isSelected, boolean cellHasFocus) 
			{
			if (isSelected)
				{
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
				}
			else 
				{
				setBackground(list.getBackground());
				setForeground(list.getForeground());
				}

			EvColor color=(EvColor)value;
			
			if (color != null)
				{
				setIcon(new ImageIcon(color.getSampleIcon()));
				if(showText)
					setText(color.name);
				else
					setText("");
				setFont(list.getFont());
				}
			else
				{
				setIcon(null);
				setText("");
				}

			return this;
			}
		}

	

	/**
	 * Constructor
	 * @param showText If the combo should also show the name of the color
	 */
	public EvComboColor(boolean showText, EvColor[] colorList, EvColor select)
		{
		super(colorList);
		setRenderer(new Renderer(showText));	
		setSelectedItem(select);
		}
	
	/**
	 * Constructor
	 * @param showText If the combo should also show the name of the color
	 */
	public EvComboColor(boolean showText)
		{
		this(showText,EvColor.colorList,EvColor.grayMedium);
		}
	
	/**
	 * Get the currently selected color
	 */
	public Color getColor()
		{
		return ((EvColor)getSelectedItem()).c;
		}
	
	/**
	 * Get the currently selected color
	 */
	public EvColor getEvColor()
		{
		return (EvColor)getSelectedItem();
		}
	
	/**
	 * Set the currently selected color
	 */
	public void setColor(EvColor c)
		{
		setSelectedItem(c);
		}
	
	
	/**
	 * For testing
	 */
	public static void main(String[] args)
		{
		JFrame frame=new JFrame();
		frame.add(new EvComboColor(false));
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		}
	}
