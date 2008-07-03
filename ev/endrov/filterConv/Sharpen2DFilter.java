package endrov.filterConv;

import java.awt.GridLayout;
import java.awt.image.*;
import javax.swing.*;

import org.jdom.DataConversionException;
import org.jdom.Element;

import endrov.ev.*;
import endrov.filter.*;

/**
 * Filter: Sharpen in 2D
 * 
 * @author Johan Henriksson
 */
public class Sharpen2DFilter extends FilterSlice
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	private static String filterMeta="Sharpen2D";
	private static String filterName="Sharpen 2D";
	private static String filterCategory="Enhance";

	public static void initPlugin() {}
	static
		{
		Filter.addFilter(new FilterInfo()
			{
			public String getCategory(){return filterCategory;}
			public String getMetaName(){return filterMeta;}
			public String getReadableName(){return filterName;}
			public boolean hasFilterROI(){return true;}
			public FilterROI filterROI(){return new Sharpen2DFilter();}
			public Filter readXML(Element e)
				{
				Sharpen2DFilter f=new Sharpen2DFilter();
				try
					{
					f.flevel.setValue(e.getAttribute("level").getIntValue());
					}
				catch (DataConversionException e1)
					{
					e1.printStackTrace();
					}
				return f;
				}
			});
		}
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	public EvMutableDouble flevel=new EvMutableDouble(1);
	
	
	public String getFilterName(){return filterName;}
	

	
	public void saveMetadata(Element e)
		{
		setFilterXmlHead(e, filterMeta);
		e.setAttribute("level",""+flevel.getValue());
		}
	
	public JComponent getFilterWidget()
		{
		JPanel p=new JPanel(new GridLayout(1,1));
		JNumericFieldMutableDouble nlevel=new JNumericFieldMutableDouble(flevel,observer,this);
		p.add(EvSwingTools.withLabel("Level:",nlevel));
		return p;
		}

	public void applyImage(BufferedImage in, BufferedImage out)
		{
		Convolve2DFilter cf=new Convolve2DFilter();
		cf.setKernel(Convolve2DFilter.makeSharpen((float)flevel.getValue()));
		cf.applyImage(in,out);
		}
	}
