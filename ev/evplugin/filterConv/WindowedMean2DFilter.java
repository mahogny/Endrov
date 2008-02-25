package evplugin.filterConv;

import java.awt.GridLayout;
import java.awt.image.*;
import javax.swing.*;
import org.jdom.*;

import evplugin.ev.*;
import evplugin.filter.*;

/**
 * Filter: Laplace in 2D
 * 
 * @author Johan Henriksson
 */
public class WindowedMean2DFilter extends FilterSlice
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	private static String filterMeta="WindowedMean2D";
	private static String filterName="Windowed Mean 2D";
	private static String filterCategory="Mathematical";

	public static void initPlugin() {}
	static
		{
		Filter.addFilter(new FilterInfo()
			{
			public String getCategory(){return filterCategory;}
			public String getName(){return filterName;}
			public boolean hasFilterROI(){return true;}
			public FilterROI filterROI(){return new WindowedMean2DFilter();}
			public Filter readXML(Element e)
				{
				WindowedMean2DFilter f=new WindowedMean2DFilter();
				try
					{
					f.fwidth.setValue(e.getAttribute("w").getIntValue());
					f.fheight.setValue(e.getAttribute("h").getIntValue());
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

	public EvMutableInteger fwidth=new EvMutableInteger(1);
	public EvMutableInteger fheight=new EvMutableInteger(1);
	
	public String getFilterName(){return filterName;}
	

	
	public void saveMetadata(Element e)
		{
		setFilterXmlHead(e, filterMeta);
		e.setAttribute("w",""+fwidth.getValue());
		e.setAttribute("h",""+fheight.getValue());
		}
	
	public JComponent getFilterWidget()
		{
		JPanel p=new JPanel(new GridLayout(1,2));
		JNumericFieldMutableInteger nwidth=new JNumericFieldMutableInteger(fwidth,observer,this);
		JNumericFieldMutableInteger nheight=new JNumericFieldMutableInteger(fheight,observer,this);
		p.add(EvSwingTools.withLabel("Width:",nwidth));
		p.add(EvSwingTools.withLabel("Height:",nheight));
		return p;
		}

	//or make a function to return the kernel?
	
	public void applyImage(BufferedImage in, BufferedImage out)
		{
		//http://de.wikipedia.org/wiki/Laplace-Operator
		//Variants exist, support them as well
		
		int w=fwidth.getValue(), h=fheight.getValue();
		float[] f=new float[w*h];
		for(int i=0;i<f.length;i++)
			f[i]=1;
		
		Convolve2DFilter cf=new Convolve2DFilter();
		cf.setKernel(new Convolve2DFilter.ConvolutionKernel(filterName,true, w,f));
		cf.applyImage(in,out);
		}
	}
