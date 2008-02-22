package evplugin.filterBasic;

import java.awt.image.*;
import javax.swing.*;

import org.jdom.Element;

import evplugin.filter.*;

/**
 * Filter: Laplace in 2D
 * 
 * @author Johan Henriksson
 */
public class Laplace2DFilter extends Convolve2DFilter
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	private static String filterName="Laplace 2D";
	private static String filterCategory="Mathematical";

	public static void initPlugin() {}
	static
		{
		Filter.addFilter(new FilterInfo()
			{
			public String getCategory(){return filterCategory;}
			public String getName(){return filterName;}
			public boolean hasFilterROI(){return true;}
			public FilterROI filterROI(){return new Laplace2DFilter();}
			public Filter readXML(Element e)
				{
				Laplace2DFilter f=new Laplace2DFilter();
				return f;
				}
			});
		}
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	public String getFilterName()
		{
		return filterName;
		}
	
	public void saveMetadata(Element e)
		{
		setFilterXmlHead(e, filterName);
		e.setAttribute("w",""+currentKernel.kernelWidth);
		}
	
	public JComponent getFilterWidget()
		{
		return null;
		}

	//or make a function to return the kernel?
	
	public void applyImage(BufferedImage in, BufferedImage out)
		{
		//http://de.wikipedia.org/wiki/Laplace-Operator
		//Variants exist, support them as well
		setKernel(new ConvolutionKernel("Laplace2D",false, 3,new float[]{
				0, 1, 0,
				1,-4, 1,
				0, 1, 0
				}));
		super.applyImage(in,out);
		}
	}
