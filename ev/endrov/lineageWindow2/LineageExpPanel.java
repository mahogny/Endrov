package endrov.lineageWindow2;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import endrov.basicWindow.icon.BasicIcon;
import endrov.util.EvSwingUtil;
import endrov.util.JImageButton;

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
	public static final ImageIcon iconAddExpRenderTimeDiff=new ImageIcon(LineageExpPanel.class.getResource("jhAddTimeDiff.png"));
	
	
	private JButton bAddExpRendererGraphOnTop=new JImageButton(iconAddExpRenderOnTop,"Add expression: graph on top");
	private JButton bAddExpRendererIntensity=new JImageButton(iconAddExpRenderIntensity,"Add expression: color intensity");
	private JButton bAddExpRendererIntensityDiff=new JImageButton(iconAddExpRenderIntensityDiff,"Add expression difference: color intensity");
	private JButton bAddExpRendererTimeDiff=new JImageButton(iconAddExpRenderTimeDiff,"Add expression: Time variance");

	private JPanel pUpper=new JPanel(new GridLayout(1,1));
	
	private HashSet<String> currentAvailableExp=new HashSet<String>();
	
	private JList expList=new JList();

	private JPanel availableListPanel=new JPanel(new BorderLayout());
	private JPanel renderListPanel=new JPanel(new BorderLayout());

	private JPanel renderedExpPanel=new JPanel();

	
	public LineageExpPanel()
		{
		
		setLayout(new GridLayout(2,1));
		add(availableListPanel);
		add(renderListPanel);

		
		
		availableListPanel.add(pUpper,BorderLayout.CENTER);
		availableListPanel.add(EvSwingUtil.layoutEvenHorizontal(bAddExpRendererGraphOnTop,bAddExpRendererIntensity,bAddExpRendererIntensityDiff,bAddExpRendererTimeDiff),
				BorderLayout.SOUTH);


		JButton bExpRendererUp=new JImageButton(BasicIcon.iconButtonUp,"Move renderer up");
		JButton bExpRendererDown=new JImageButton(BasicIcon.iconButtonDown,"Move renderer down");

		renderListPanel.add(new JScrollPane(renderedExpPanel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),BorderLayout.CENTER);
		renderListPanel.add(EvSwingUtil.layoutEvenHorizontal(bExpRendererUp,bExpRendererDown),
				BorderLayout.SOUTH);
		
		setAvailableExpressionsUpdate(Arrays.asList("GFP","RFP"));
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
		currentAvailableExp.clear();
		currentAvailableExp.addAll(newAvailableExp);
		Vector<String> exps=new Vector<String>(currentAvailableExp);
		Collections.sort(exps);
		
		
		expList=new JList(exps);
		pUpper.removeAll();
		pUpper.add(new JScrollPane(expList,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		
		invalidate();
		}
	
	}
