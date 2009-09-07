package endrov.nuc.modw;

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
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListDataListener;

import endrov.basicWindow.EvComboColor;
import endrov.basicWindow.icon.BasicIcon;
import endrov.util.EvSwingUtil;
import endrov.util.SnapBackSlider;

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

	private final NucModelExtension.NucModelWindowHook hook;
	
	/**
	 * Available expression patterns
	 */
	private final List<String> avail=new ArrayList<String>();


	private final ComboBoxModel cm1=new CustomComboModel();
	
	private final JComboBox cExp1=new JComboBox(cm1);

	private final SnapBackSlider snapContrast=new SnapBackSlider(SnapBackSlider.HORIZONTAL,-10000,10000);

	/**
	 * Scaling of expression. If null then calculate
	 */
	public Double scale1;

	
	
	
	public ModwPanelExpPattern(NucModelExtension.NucModelWindowHook hook)
		{
		this.hook=hook;
		colorCombo.addActionListener(this);
		bDelete.addActionListener(this);
		snapContrast.addSnapListener(this);
		setLayout(new GridLayout(2,1));
		add(EvSwingUtil.layoutLCR(null, cExp1, colorCombo));
		add(EvSwingUtil.layoutLCR(null, snapContrast, bDelete));
		updateColor();
		}
	
	public void slideChange(int change)
		{
		scale1*=Math.exp(change/5000.0);
		hook.w.view.repaint();
		}
	
	
	/**
	 * Set list of expression patterns available, only update GUI if needed
	 */
	public void setAvailableExpressions(Collection<String> exps)
		{
		//Check if anything is different. Otherwise don't update
		//Untested
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
			hook.w.view.repaint();
			updateColor();
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
	
	public double colR, colG, colB;
	
	}
