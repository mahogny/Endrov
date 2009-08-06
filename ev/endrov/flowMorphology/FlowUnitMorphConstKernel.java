package endrov.flowMorphology;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdom.Element;

import endrov.basicWindow.icon.BasicIcon;
import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitDeclaration;
import endrov.flow.ui.FlowPanel;
import endrov.flowBasic.constants.FlowUnitConst;
import endrov.util.EvSwingUtil;
import endrov.util.JImageButton;

/**
 * Flow unit: kernel constant
 * @author Johan Henriksson
 *
 */
public class FlowUnitMorphConstKernel extends FlowUnitConst
	{
	private boolean var=true;
	private int[] extent=new int[]{1,1,1,1}; //Extension left, right, up, down
	
	
	
	//private static ImageIcon icon=null;//new ImageIcon(FlowUnitMorphConstKernel.class.getResource("jhBoolean.png"));
	private static final String metaType="constMorphKernel";
	
	public static void initPlugin() {}
	static
		{
		FlowUnitDeclaration decl=new FlowUnitDeclaration(CategoryInfo.name,"Kernel",metaType,FlowUnitMorphConstKernel.class, 
				CategoryInfo.icon,"Constant kernel");
		Flow.addUnitType(decl);
		FlowType.registerSuggestCreateUnitInput(Boolean.class, decl);
		}
	
	public String toXML(Element e)
		{
		e.setAttribute("value", ""+isVar());
		return metaType;
		}

	public void fromXML(Element e)
		{
		setVar(Boolean.parseBoolean(e.getAttributeValue("value")));
		}

	
	
	protected String getLabel()
		{
		return "B";
		}

	protected FlowType getConstType()
		{
		return FlowType.TBOOLEAN;
		}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.put("out", isVar());
		}
	

	/**
	 * Panel with all pixels annotated
	 * @author Johan Henriksson
	 */
	private class KernelPixels extends JPanel
		{
		private static final long serialVersionUID = 1L;

		private final int gsize=10;

		@Override
		public Dimension getSize()
			{
			int startX=-extent[0];
			int startY=-extent[3]; //Follow pixel coordinates?
			int endX=extent[1];
			int endY=extent[2];

			int numX=endX-startX+1;
			int numY=endY-startY+1;
			return new Dimension(numX*gsize,numY*gsize);
			}
		
		protected void paintComponent(Graphics g)
			{
			//draw all in one, no need to re-layout

			int startX=-extent[0];
			int startY=-extent[3]; //Follow pixel coordinates?
			int endX=extent[1];
			int endY=extent[2];

			g.setColor(Color.WHITE);
			g.fillRect(0, 0, getWidth(), getHeight());
			
			for(int ay=startY;ay<=endY;ay++)
				for(int ax=startX;ax<=endX;ax++)
					{
					//TODO
					}
			
			//Draw grid
			
			
			
			
			// TODO Auto-generated method stub
			super.paintComponent(g);
			}
		}

	private void addExtentListener(final int i, JButton bMinus, JButton bPlus)
		{
		bMinus.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
				{
				if(extent[i]>0)
					extent[i]--;
				newExtent();
				}});
		bPlus.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e)
			{
			extent[i]++;
			newExtent();
			}});
		}

	private void newExtent()
		{
		//Remove pixels outside extent
		
		}
	
	
	public Component getGUIcomponent(final FlowPanel p)
		{
		final JPanel comp=new JPanel(new BorderLayout());
		comp.setOpaque(false);
		
		JButton bLeftPlus=new JImageButton(BasicIcon.iconAdd,"Increase size");
		JButton bLeftMinus=new JImageButton(BasicIcon.iconRemove,"Decrease size");
		JButton bRightPlus=new JImageButton(BasicIcon.iconAdd,"Increase size");
		JButton bRightMinus=new JImageButton(BasicIcon.iconRemove,"Decrease size");
		JButton bUpPlus=new JImageButton(BasicIcon.iconAdd,"Increase size");
		JButton bUpMinus=new JImageButton(BasicIcon.iconRemove,"Decrease size");
		JButton bDownPlus=new JImageButton(BasicIcon.iconAdd,"Increase size");
		JButton bDownMinus=new JImageButton(BasicIcon.iconRemove,"Decrease size");

		KernelPixels pPixels=new KernelPixels();

		comp.add(EvSwingUtil.layoutEvenVertical(bLeftMinus,bLeftPlus),BorderLayout.WEST);
		comp.add(EvSwingUtil.layoutEvenVertical(bRightMinus,bRightPlus),BorderLayout.EAST);
		comp.add(EvSwingUtil.layoutEvenHorizontal(bUpMinus,bUpPlus),BorderLayout.NORTH);
		comp.add(EvSwingUtil.layoutEvenHorizontal(bDownMinus,bDownPlus),BorderLayout.SOUTH);
		comp.add(pPixels,BorderLayout.CENTER);
		
		addExtentListener(0, bLeftPlus, bLeftMinus);
		addExtentListener(1, bRightPlus, bRightMinus);
		addExtentListener(2, bUpPlus, bUpMinus);
		addExtentListener(3, bDownPlus, bDownMinus);
		
		return comp;
		}

	public void setVar(boolean var)
		{
			this.var = var;
		}

	public boolean isVar()
		{
			return var;
		}
	}
