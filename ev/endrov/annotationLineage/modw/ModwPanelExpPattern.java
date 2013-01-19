/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.annotationLineage.modw;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListDataListener;

import endrov.annotationLineage.Lineage;
import endrov.gui.component.EvComboColor;
import endrov.gui.icon.BasicIcon;
import endrov.util.EvSwingUtil;
import endrov.util.SnapBackSlider;
import endrov.util.Tuple;
import endrov.windowViewer3D.Viewer3DWindow;

/**
 * Expression pattern settings
 * @author Johan Henriksson
 *
 */
public class ModwPanelExpPattern extends JPanel implements ActionListener, SnapBackSlider.SnapChangeListener
	{
	static final long serialVersionUID=0;
	private final JButton bDelete=BasicIcon.getButtonDelete();
	private final EvComboColor colorCombo=new EvComboColor(false);

	private final LineageModelExtension.NucModelWindowHook hook;
	
	/**
	 * Available expression patterns
	 */
	private final List<String> avail=new ArrayList<String>();


	private final ComboBoxModel cm1=new CustomComboModel();
	
	private final JComboBox cExp1=new JComboBox(cm1);

	private final SnapBackSlider snapContrast=new SnapBackSlider(SnapBackSlider.HORIZONTAL,-10000,10000);
	private final SnapBackSlider snapBrightness=new SnapBackSlider(SnapBackSlider.HORIZONTAL,-10000,10000);

	public double colR, colG, colB;

	/**
	 * Scaling of expression. If null then calculate
	 */
	public Double scale1;
	public double add1;
	// final color = signal*scale1 + add1
	
	public ModwPanelExpPattern(LineageModelExtension.NucModelWindowHook hook)
		{
		JLabel labelC=new JLabel("C: ");
		JLabel labelB=new JLabel("B: ");
		labelC.setToolTipText("Contrast");
		labelB.setToolTipText("Brightness");
		
		this.hook=hook;
		colorCombo.addActionListener(this);
		bDelete.addActionListener(this);
		snapContrast.addSnapListener(this);
		snapBrightness.addSnapListener(this);
		cExp1.addActionListener(this);
		
		setLayout(new GridLayout(3,1));
		add(EvSwingUtil.layoutLCR(null, cExp1, colorCombo));
		add(EvSwingUtil.layoutLCR(labelC, snapContrast, bDelete));
		add(EvSwingUtil.layoutLCR(labelB, snapBrightness, null));
		
		updateColor();
		}
	
	public void slideChange(SnapBackSlider source, int change)
		{
		//Shouldn't happen but better than nothing
		if(scale1==null)
			adjustExpPatternScale(hook.w, this);
		if(scale1!=null)
			{
			if(source==snapContrast)
				scale1*=Math.exp(change/5000.0);
			else
				add1+=change*scale1/5000.0;
			hook.w.view.repaint();
			}
		}
	
	
	/**
	 * Set list of expression patterns available, only update GUI if needed
	 */
	public void setAvailableExpressions(Collection<String> exps)
		{
		//Check if anything is different. Otherwise don't update
		if(!avail.equals(exps))
			{
			avail.clear();
			avail.addAll(exps);
			revalidate();
			}
		}
	
	/**
	 * 
	 * @author Johan Henriksson
	 *
	 */
	private class CustomComboModel implements ComboBoxModel
		{
		public String selectedExp="";
		public Object getSelectedItem()
			{
			return selectedExp;
			}

		public void setSelectedItem(Object anItem)
			{
			selectedExp=(String)anItem;
			}

		private LinkedList<ListDataListener> listener=new LinkedList<ListDataListener>();

		public void addListDataListener(ListDataListener arg)
			{
			listener.add(arg);
			}

		public Object getElementAt(int i)
			{
			return avail.get(i);
			}

		public int getSize()
			{
			return avail.size();
			}

		public void removeListDataListener(ListDataListener arg)
			{
			listener.remove(arg);
			}
		}
	
	public void stateChanged(ChangeEvent e)
		{
		hook.w.view.repaint(); //TODO modw repaint
		}


	private void updateColor()
		{
		Color c=colorCombo.getColor();
		colR=c.getRed()/255.0;
		colG=c.getGreen()/255.0;
		colB=c.getBlue()/255.0;
		}
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==colorCombo)
			{
			updateColor();
			hook.w.view.repaint();
			}
		else
			{
			if(e.getSource()==bDelete)
				{
				hook.expsettings.remove(this);
				hook.w.updateToolPanels();
				}
			hook.w.view.repaint(); //TODO modw repaint
			}
		}
	
	public String getSelectedExp()
		{
		return (String)cExp1.getSelectedItem();
		}
	
	public static void adjustExpPatternScale(Viewer3DWindow w, ModwPanelExpPattern panel)
		{
		//Find lineage with this expression pattern
		String expName=panel.getSelectedExp();
		for(Lineage lin:Lineage.getParticles(w.getSelectedData()))
			if(lin.getAllExpNames().contains(expName))
				{
				Tuple<Double,Double> maxMin1=lin.getMaxMinExpLevel(expName);
				if(maxMin1!=null)
					{
					double absmax=Math.max(Math.abs(maxMin1.fst()), Math.abs(maxMin1.snd()));
					panel.scale1=1.0/absmax;
					panel.add1=0;
					}
				break;
				}
		}

	
	}
