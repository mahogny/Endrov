package evplugin.filterBasic;

import java.awt.GridLayout;
import java.awt.image.*;

import javax.swing.*;

import org.jdom.Element;

import evplugin.ev.*;
import evplugin.filter.*;

/**
 * Filter: Adjust contrast & brightness
 * 
 * @author Johan Henriksson
 */
public class ContrastBrightnessFilter extends FilterSlice
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	private static String filterName="Contrast & Brightness";
	private static String filterCategory="Enhance";

	public static void initPlugin() {}
	static
		{
		Filter.addFilter(new FilterInfo()
			{
			public String getCategory(){return filterCategory;}
			public String getName(){return filterName;}
			public boolean hasFilterROI(){return true;}
			public FilterROI filterROI(){return new ContrastBrightnessFilter();}
			public Filter readXML(Element e)
				{
				ContrastBrightnessFilter f=new ContrastBrightnessFilter();
				f.pcontrast.setValue(Double.parseDouble(e.getAttributeValue("pwhite")));
				f.pbrightness.setValue(Double.parseDouble(e.getAttributeValue("pblack")));
				return f;
				}
			});
		}
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	public EvMutableDouble pcontrast=new EvMutableDouble(1.0);
	public EvMutableDouble pbrightness=new EvMutableDouble(0.0);
	
	public String getFilterName()
		{
		return filterName;
		}
	
	public void saveMetadata(Element e)
		{
		setFilterXmlHead(e, filterName);
		e.setAttribute("pwhite",""+pcontrast);
		e.setAttribute("pblack",""+pbrightness);
		}

	
	public JComponent getFilterWidget()
		{
		JPanel pane=new JPanel(new GridLayout(2,2));
		
		JNumericFieldMutableDouble npwhite=new JNumericFieldMutableDouble(pcontrast);
		JNumericFieldMutableDouble npblack=new JNumericFieldMutableDouble(pbrightness);
		
		pane.add(new JLabel("Contrast:"));
		pane.add(npwhite);
		pane.add(new JLabel("Brightness:"));
		pane.add(npblack);

		return pane;
		}

	
	
	
	public void applyImage(BufferedImage in, BufferedImage out)
		{
		ContrastBrightnessOp bcfilter=new ContrastBrightnessOp(pcontrast.getValue(),pbrightness.getValue());
		bcfilter.filter(in,out);
		//danger!? source image changes, should out be unaffected? I think this is needed? force write by copying source?
		}
	}
