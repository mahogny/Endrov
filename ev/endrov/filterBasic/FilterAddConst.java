package endrov.filterBasic;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

import org.jdom.Element;

import endrov.ev.*;
import endrov.filter.*;

/**
 * Filter: add a constant
 * 
 * @author Johan Henriksson
 */
public class FilterAddConst extends FilterSlice
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	private static String filterName="Add Constant";
	private static String filterMeta="AddConstant";
	private static String filterCategory="Mathematical";

	public static void initPlugin() {}
	static
		{
		Filter.addFilter(new FilterInfo()
			{
			public String getCategory(){return filterCategory;}
			public String getMetaName(){return filterMeta;}
			public String getReadableName(){return filterName;}
			public boolean hasFilterROI(){return true;}
			public FilterROI filterROI(){return new FilterAddConst();}
			});
		}
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	public EvMutableDouble value=new EvMutableDouble(0);
	
	public String getFilterName()
		{
		return filterName;
		}
	
	public void loadMetadata(Element e)
		{
		value.setValue(Double.parseDouble(e.getAttributeValue("value")));
		}
	public void saveMetadata(Element e)
		{
		setFilterXmlHead(e, filterMeta);
		e.setAttribute("value",""+value);
		}

	
	public JComponent getFilterWidget()
		{
		JPanel pane=new JPanel(new GridLayout(1,2));
		
		final JNumericFieldMutableDouble nlambda=new JNumericFieldMutableDouble(value, observer, this);
		
		pane.add(new JLabel("Value:"));
		pane.add(nlambda);

		return pane;
		}

	
	
	
	
	public void applyImage(BufferedImage in, BufferedImage out)
		{
		WritableRaster rin=in.getRaster();
		WritableRaster rout=out.getRaster();
		
		double nvalue=this.value.doubleValue();
		
		int width=rin.getWidth();
		int[] pix=new int[width];
		for(int ah=0;ah<rin.getHeight();ah++)
			{
			rin.getSamples(0, ah, width, 1, 0, pix);
			for(int aw=0;aw<width;aw++)
				{
				pix[aw]+=nvalue;
				if(pix[aw]>255)
					pix[aw]=255;
				if(pix[aw]<0)
					pix[aw]=0;
				//What about double value images?
				}
			rout.setSamples(0, ah, width, 1, 0, pix);
			}
		}
	}
