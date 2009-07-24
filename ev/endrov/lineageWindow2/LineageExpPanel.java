package endrov.lineageWindow2;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import endrov.basicWindow.EvComboColor;
import endrov.basicWindow.icon.BasicIcon;
import endrov.util.EvSwingUtil;
import endrov.util.JImageButton;
import endrov.util.SnapBackSlider;

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
	
	/*
	private JButton bAddExpRendererGraphOnTop=new JImageButton(iconAddExpRenderOnTop,"Add expression: graph on top");
	private JButton bAddExpRendererIntensity=new JImageButton(iconAddExpRenderIntensity,"Add expression: color intensity");
	private JButton bAddExpRendererIntensityDiff=new JImageButton(iconAddExpRenderIntensityDiff,"Add expression difference: color intensity");
	private JButton bAddExpRendererTimeDiff=new JImageButton(iconAddExpRenderTimeDiff,"Add expression: Time variance");
*/
//	private JPanel pUpper=new JPanel(new GridLayout(1,1));
	
	private HashSet<String> currentAvailableExp=new HashSet<String>();
	
//	private JList expList=new JList();
/*
	private JPanel availableListPanel=new JPanel(new BorderLayout());
	private JPanel renderListPanel=new JPanel(new BorderLayout());

	private JPanel renderedExpPanel=new JPanel();
*/
	private JButton addRenderer=new JButton("Add expression"); 
	
	
	private JPanel panelAllRenderers=new JPanel(new GridLayout(1,1));
	
	private LinkedList<RenderEntry> listRenderers=new LinkedList<RenderEntry>();
	
	public LineageExpPanel()
		{
		setLayout(new BorderLayout());
		
		add(EvSwingUtil.layoutCompactVertical(addRenderer//,panelAllRenderers		
			),BorderLayout.NORTH);

		add(addRenderer,BorderLayout.NORTH);

//		add(addRenderer,BorderLayout.NORTH);
	//	add(allRenderers,BorderLayout.CENTER);
		
		add(new JScrollPane(panelAllRenderers,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),BorderLayout.CENTER);
//		add(panelAllRenderers);
		
		
		addRenderer.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
				{
				listRenderers.add(new RenderEntry());
				placeAllRenderers();
				}});
		
		
		//panelAllRenderers.add(EvSwingUtil.layoutCompactVertical(new RenderEntry()));
		
		/*		
		setLayout(new GridLayout(2,1));
		add(availableListPanel);
		add(renderListPanel);

		
		availableListPanel.add(pUpper,BorderLayout.CENTER);
		availableListPanel.add(EvSwingUtil.layoutEvenHorizontal(bAddExpRendererGraphOnTop,bAddExpRendererIntensity,bAddExpRendererIntensityDiff,bAddExpRendererTimeDiff),
				BorderLayout.SOUTH);



		renderListPanel.add(new JScrollPane(renderedExpPanel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),BorderLayout.CENTER);
		//renderListPanel.add(EvSwingUtil.layoutEvenHorizontal(bExpRendererUp,bExpRendererDown),
				//BorderLayout.SOUTH);
*/		
		//setAvailableExpressionsUpdate(Arrays.asList("GFP","RFP"));
		
		placeAllRenderers();
		}
	
	
	private void placeAllRenderers()
		{
		panelAllRenderers.removeAll();
		JPanel p=new JPanel(new GridLayout(listRenderers.size(),1));
		//panelAllRenderers.setLayout(new GridLayout(listRenderers.size(),1));
		panelAllRenderers.setLayout(new BorderLayout());
		System.out.println("siz "+listRenderers.size());
		for(RenderEntry e:listRenderers)
			{
			p.add(e);
			//e.setVisible(true);
			}
		//p.setVisible(true);
		panelAllRenderers.add(p,BorderLayout.NORTH);
		panelAllRenderers.add(new JLabel(""),BorderLayout.CENTER);
		panelAllRenderers.setVisible(true);
		revalidate();
		//setVisible(false);
		//setVisible(true);
		}
	
	public void setAvailableExpressions(Collection<String> exps)
		{
		//Check if anything is different. Otherwise don't update
		if(currentAvailableExp.containsAll(exps))
			{
			HashSet<String> newAvailableExp=new HashSet<String>(exps);
			if(!newAvailableExp.containsAll(currentAvailableExp))
				setAvailableExpressionsUpdate(newAvailableExp);
			}
		else
			setAvailableExpressionsUpdate(exps);
		}

	private void setAvailableExpressionsUpdate(Collection<String> newAvailableExp)
		{
		//TODO
		currentAvailableExp.clear();
		currentAvailableExp.addAll(newAvailableExp);
		/*
		Vector<String> exps=new Vector<String>(currentAvailableExp);
		Collections.sort(exps);
		
		invalidate();
		*/
		}
	
	
	
	/**
	 * Settings panel for one renderer
	 * @author Johan Henriksson
	 *
	 */
	private class RenderEntry extends JPanel implements ActionListener
		{
		private static final long serialVersionUID = 1L;
		public ComboRenderType cRenderType=new ComboRenderType();
		public EvComboColor cColor=new EvComboColor(false);
		public JButton bUp=new JImageButton(BasicIcon.iconButtonUp,"Move renderer up");
		public JButton bDown=new JImageButton(BasicIcon.iconButtonDown,"Move renderer down");
		public JButton bRemoveRenderer=new JImageButton(BasicIcon.iconButtonDelete,"Remove renderer");
		
		public SnapBackSlider snapContrast=new SnapBackSlider(SnapBackSlider.HORIZONTAL,-10000,10000);
		
		public ComboExp cExp1=new ComboExp();
		public ComboExp cExp2=new ComboExp();
		
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
			
			bUp.addActionListener(this);
			bDown.addActionListener(this);
			bRemoveRenderer.addActionListener(this);
			
			placeExpComponents();
			setBorder(BorderFactory.createRaisedBevelBorder());
			}
		
		
		public void placeExpComponents()
			{
	//		removeAll();
			setLayout(new BorderLayout());
			add(firstLine,BorderLayout.NORTH);
			Integer type=cRenderType.getType();
			JComponent secondLine=null;
			if(type==LineageView.ExpRenderSetting.typeGraphOnTop)
				{
				//secondLine=EvSwingUtil.layoutCompactHorizontal(snapContrast,cExp1);
				secondLine=new JPanel(new GridLayout(1,2));
				secondLine.add(snapContrast);
				secondLine.add(cExp1);
				}
			else if(type==LineageView.ExpRenderSetting.typeColorIntensity)
				{
				secondLine=new JPanel(new GridLayout(1,2));
				secondLine.add(snapContrast);
				secondLine.add(cExp1);
				}
			else if(type==LineageView.ExpRenderSetting.typeColorIntensityDiff)
				{
				secondLine=new JPanel(new GridLayout(1,2));
				secondLine.add(cExp1);
				secondLine.add(cExp2);
				}
			else if(type==LineageView.ExpRenderSetting.typeTimeDev)
				{
				secondLine=new JPanel(new GridLayout(1,1));
				secondLine.add(cExp1);
				}
			else
				System.out.println("type wtf");
			
			if(secondLine!=null)
				add(secondLine,BorderLayout.CENTER);
			
			}
		
		
		private void setAvailableExpressionsUpdate(Collection<String> newAvailableExp)
			{
			//TODO
			}
		
		
		private void removeRenderer()
			{
			listRenderers.remove(this);
			placeAllRenderers();
			//todo
			}

		private void moveUp()
			{
			int i=listRenderers.indexOf(this);
			listRenderers.remove(i);
			i--;
			if(i<0) i=0;
			listRenderers.add(i,this);
			placeAllRenderers();
			//todo
			}
		
		private void moveDown()
			{
			int i=listRenderers.indexOf(this);
			listRenderers.remove(i);
			i++;
			if(i>listRenderers.size()) listRenderers.size();
			listRenderers.add(i,this);
			placeAllRenderers();
			//todo
			}
		
		public void actionPerformed(ActionEvent e)
			{
			if(e.getSource()==bUp)
				moveUp();
			else if(e.getSource()==bDown)
				moveDown();
			else if(e.getSource()==bRemoveRenderer)
				removeRenderer();
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
	
	private static class ComboExp extends JComboBox
		{
		private static final long serialVersionUID = 1L;

		public ComboExp()
			{
			super(new Vector<String>(Arrays.asList("RFP","GFP")));
			}
		}
	
	
	}
