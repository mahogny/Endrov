package endrov.nucLineageWindow;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListDataListener;

import endrov.basicWindow.EvComboColor;
import endrov.basicWindow.icon.BasicIcon;
import endrov.util.EvSwingUtil;
import endrov.util.JImageButton;
import endrov.util.SnapBackSlider;
import endrov.util.SnapBackSlider.SnapChangeListener;

/**
 * Select expressions and how they should be rendered
 * 
 * @author Johan Henriksson
 *
 */
public class LineageExpPanel extends JPanel
	{
	private static final long serialVersionUID = 1L;

	public static final ImageIcon iconAddExpRenderOnTop=new ImageIcon(LineageExpPanel.class.getResource("jhAddGraphOnTop.png"));
	public static final ImageIcon iconAddExpRenderIntensity=new ImageIcon(LineageExpPanel.class.getResource("jhAddIntensity.png"));
	public static final ImageIcon iconAddExpRenderIntensityDiff=new ImageIcon(LineageExpPanel.class.getResource("jhAddIntensityDiff.png"));
	public static final ImageIcon iconAddExpRenderTimeDev=new ImageIcon(LineageExpPanel.class.getResource("jhAddTimeDiff.png"));
	
	private final TreeSet<String> currentAvailableExp=new TreeSet<String>();
	private final LinkedList<RenderEntry> listRenderers=new LinkedList<RenderEntry>();
	
	private JButton addRenderer=new JButton("Add expression"); 
	private JPanel panelAllRenderers=new JPanel(new GridLayout(1,1));
	
	private final LineageView view;
	
	public LineageExpPanel(LineageView view)
		{
		this.view=view;
		
		
		setLayout(new BorderLayout());
		
		add(EvSwingUtil.layoutCompactVertical(addRenderer//,panelAllRenderers		
			),BorderLayout.NORTH);

		add(addRenderer,BorderLayout.NORTH);

		add(new JScrollPane(panelAllRenderers,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),BorderLayout.CENTER);
		
		addRenderer.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
				{
				listRenderers.add(new RenderEntry());
				placeAllRenderers();
				updateLinView();
				}});
		
		placeAllRenderers();
		}
	
	/**
	 * Put all renderers in the GUI, remove old
	 */
	private void placeAllRenderers()
		{
		panelAllRenderers.removeAll();
		JPanel p=new JPanel(new GridLayout(listRenderers.size(),1));
		panelAllRenderers.setLayout(new BorderLayout());
		for(RenderEntry e:listRenderers)
			p.add(e);
		panelAllRenderers.add(p,BorderLayout.NORTH);
		panelAllRenderers.add(new JLabel(""),BorderLayout.CENTER);
		panelAllRenderers.setVisible(true);
		revalidate();
		}
	
	/**
	 * Set list of expression patterns available, only update GUI if needed
	 */
	public void setAvailableExpressions(Collection<String> exps)
		{
		//Check if anything is different. Otherwise don't update
		//Untested
		if(!currentAvailableExp.equals(exps))
			{
			currentAvailableExp.clear();
			currentAvailableExp.addAll(exps);
			for(RenderEntry e:listRenderers)
				e.setAvailableExpressionsUpdate();
			revalidate();
			}

/*		
		if(currentAvailableExp.containsAll(exps))
			{
			HashSet<String> newAvailableExp=new HashSet<String>(exps);
			if(!newAvailableExp.containsAll(currentAvailableExp))
				setAvailableExpressionsUpdate(newAvailableExp);
			}
		else
			setAvailableExpressionsUpdate(exps);
			*/
		}

	/**
	 * Set list of expression patterns, force GUI update
	 */
	/*
	private void setAvailableExpressionsUpdate(Collection<String> newAvailableExp)
		{
		currentAvailableExp.clear();
		currentAvailableExp.addAll(newAvailableExp);
		for(RenderEntry e:listRenderers)
			e.setAvailableExpressionsUpdate();
		revalidate();
		}*/
	
	/**
	 * Send rendering settings to lineage view
	 */
	public void updateLinView()
		{
		LinkedList<LineageView.ExpRenderSetting> list=new LinkedList<LineageView.ExpRenderSetting>();
		for(RenderEntry e:listRenderers)
			list.add(e.exp);
		view.setExpRenderSettings(list);
		}
	
	/**
	 * Settings panel for one renderer
	 * @author Johan Henriksson
	 *
	 */
	private class RenderEntry extends JPanel implements ActionListener, SnapChangeListener
		{
		private static final long serialVersionUID = 1L;
		

		
		public final List<String> avail=new ArrayList<String>();

		
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
		
		/**
		 * Handle list of expressions
		 */
		private ComboBoxModel cm1=new CustomComboModel();
		private ComboBoxModel cm2=new CustomComboModel();
		
		
		public ComboRenderType cRenderType=new ComboRenderType();
		public EvComboColor cColor=new EvComboColor(false);
		public JButton bUp=new JImageButton(BasicIcon.iconButtonUp,"Move renderer up");
		public JButton bDown=new JImageButton(BasicIcon.iconButtonDown,"Move renderer down");
		public JButton bRemoveRenderer=new JImageButton(BasicIcon.iconButtonDelete,"Remove renderer");
		
		public SnapBackSlider snapContrast=new SnapBackSlider(SnapBackSlider.HORIZONTAL,-10000,10000);
		public JComboBox cExp1=new JComboBox(cm1);
		public JComboBox cExp2=new JComboBox(cm2);
		
		
		
		public final LineageView.ExpRenderSetting exp=new LineageView.ExpRenderSetting();
		
		private JPanel firstLine=new JPanel(new GridBagLayout());
		public RenderEntry()
			{
			
			GridBagConstraints c=new GridBagConstraints();
			c.gridy = 0;
			c.gridx = 0;
			c.fill = GridBagConstraints.HORIZONTAL;
			
			firstLine.add(bUp,c);
			c.gridx++;
			firstLine.add(bDown,c);
			c.gridx++;
			firstLine.add(bRemoveRenderer,c);
			c.gridx++;
			firstLine.add(cRenderType,c);
			c.gridx++;
			firstLine.add(cColor,c);
			c.gridx++;
			
			
			snapContrast.setToolTipText("Scale expression level");
			
			setBorder(BorderFactory.createRaisedBevelBorder());
			setAvailableExpressionsUpdate();
			placeExpComponents();
			updateExp();
			}
		
		/**
		 * Place components, remove old components
		 */
		public void placeExpComponents()
			{
			removeAll(); //Also removes all listeners!
			setLayout(new BorderLayout());
			add(firstLine,BorderLayout.NORTH);
			Integer type=cRenderType.getType();
			JComponent secondLine=null;
			if(type==LineageView.ExpRenderSetting.typeGraphOnTop)
				{
				//secondLine=EvSwingUtil.layoutCompactHorizontal(snapContrast,cExp1);
				secondLine=new JPanel(new GridLayout(2,1));
				secondLine.add(cExp1);
				secondLine.add(snapContrast);
				cRenderType.setToolTipText("Draw expression graph on top of lineage");
				}
			else if(type==LineageView.ExpRenderSetting.typeColorIntensity)
				{
				secondLine=new JPanel(new GridLayout(2,1));
				secondLine.add(cExp1);
				secondLine.add(snapContrast);
				cRenderType.setToolTipText("Draw expression as a color intensity on the lineage branches");
				}
			else if(type==LineageView.ExpRenderSetting.typeColorIntensityDiff)
				{
				secondLine=new JPanel(new GridLayout(2,1));
				secondLine.add(cExp1);
				secondLine.add(cExp2);
				cRenderType.setToolTipText("Draw difference of expressions as a color intensity on the lineage branches");
				}
			else if(type==LineageView.ExpRenderSetting.typeTimeDev)
				{
				secondLine=new JPanel(new GridLayout(1,1));
				secondLine.add(cExp1);
				cRenderType.setToolTipText("Show single-valued expression as time deviation");
				}
			else
				System.out.println("type wtf");
			
			
			bUp.addActionListener(this);
			bDown.addActionListener(this);
			bRemoveRenderer.addActionListener(this);
			cRenderType.addActionListener(this);
			snapContrast.addSnapListener(this);
			cColor.addActionListener(this);
			
			if(secondLine!=null)
				add(secondLine,BorderLayout.CENTER);
			revalidate();
			}

		/**
		 * Update list of available expression patterns
		 */
		private void setAvailableExpressionsUpdate()
			{
			avail.clear();
			avail.addAll(currentAvailableExp);
			cExp1.setModel(cm1);
			cExp2.setModel(cm2);
			cExp1.removeActionListener(this);
			cExp1.addActionListener(this);
			cExp2.removeActionListener(this);
			cExp2.addActionListener(this);
			revalidate();
			}
		
		/**
		 * Remove this renderer from the list, update GUI
		 */
		private void removeRenderer()
			{
			listRenderers.remove(this);
			placeAllRenderers();
			updateLinView();
			}

		/**
		 * Move renderer up in list, update GUI
		 */
		private void moveUp()
			{
			int i=listRenderers.indexOf(this);
			listRenderers.remove(i);
			i--;
			if(i<0) i=0;
			listRenderers.add(i,this);
			placeAllRenderers();
			updateExp();
			}
		
		/**
		 * Move renderer down in list, update GUI
		 */
		private void moveDown()
			{
			int i=listRenderers.indexOf(this);
			listRenderers.remove(i);
			i++;
			if(i>listRenderers.size()) listRenderers.size();
			listRenderers.add(i,this);
			placeAllRenderers();
			updateExp();
			}
		
		public void actionPerformed(ActionEvent e)
			{
			if(e.getSource()==bUp)
				moveUp();
			else if(e.getSource()==bDown)
				moveDown();
			else if(e.getSource()==bRemoveRenderer)
				removeRenderer();
			else if(e.getSource()==cExp1 || e.getSource()==cExp2)
				{
				System.out.println("here");
				exp.scale1=null; //Re-scale
				updateExp();
				}
			else if(e.getSource()==cColor)
				updateExp();
			else if(e.getSource()==cRenderType)
				{
				exp.scale1=null; //Re-scale
				placeExpComponents();
				updateExp();
				}
			}

		public void updateExp()
			{
			exp.type=(Integer)cRenderType.getSelectedItem();
			exp.color=cColor.getEvColor();
			exp.expname1=(String)cExp1.getSelectedItem();
			exp.expname2=(String)cExp2.getSelectedItem();
			//System.out.println("here "+exp.expname1);
			updateLinView();
			}
		
		public void slideChange(int change)
			{
			exp.scale1*=Math.exp(change/5000.0);
			//System.out.println("new scale "+exp.scale1);
			updateLinView();
			}
		
		}
	
	
	/**
	 * Selection of graph rendering type
	 * @author Johan Henriksson
	 *
	 */
	private static class ComboRenderType extends JComboBox
		{
		static final long serialVersionUID=0;
		
		/**
		 * Cell renderer
		 * @author Johan Henriksson
		 *
		 */
		private class CustomRenderer extends JLabel implements ListCellRenderer
			{
			private static final long serialVersionUID = 1L;
	
			public CustomRenderer()
				{
				setOpaque(true);
				setHorizontalAlignment(LEFT);
				setVerticalAlignment(CENTER);
				}
	
			public Component getListCellRendererComponent(JList list,	Object value,	int index, boolean isSelected, boolean cellHasFocus) 
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
	
				Integer type=(Integer)value;

				if(type==LineageView.ExpRenderSetting.typeGraphOnTop)
					setIcon(iconAddExpRenderOnTop);
				else if(type==LineageView.ExpRenderSetting.typeColorIntensity)
					setIcon(iconAddExpRenderIntensity);
				else if(type==LineageView.ExpRenderSetting.typeColorIntensityDiff)
					setIcon(iconAddExpRenderIntensityDiff);
				else if(type==LineageView.ExpRenderSetting.typeTimeDev)
					setIcon(iconAddExpRenderTimeDev);
				else
					setIcon(null);
				setText("");
	
				return this;
				}
			}
	
		
		/**
		 * Constructor
		 * @param showText If the combo should also show the name of the color
		 */
		public ComboRenderType()
			{
			super(new Vector<Integer>(Arrays.asList(0,1,2,3)));
			setRenderer(new CustomRenderer());	
			}
		
		/**
		 * Get the currently selected color
		 */
		public Integer getType()
			{
			return (Integer)getSelectedItem();
			}
		}
	
	/*
	private static class ComboExp extends JComboBox
		{
		private static final long serialVersionUID = 1L;

		public ComboExp()
			{
			super(new Vector<String>(Arrays.asList("posMeanDevR","RFP","GFP")));
			}
		}*/
	
	
	}
