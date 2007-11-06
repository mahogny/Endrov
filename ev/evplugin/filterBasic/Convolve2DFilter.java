package evplugin.filterBasic;

//import java.awt.*;
import java.awt.image.*;

import javax.swing.*;

import org.jdom.Element;

import evplugin.ev.*;
import evplugin.filter.*;


//better put derivates in here directly

/**
 * Filter: Convolve in 2D
 * 
 * @author Johan Henriksson
 */
public class Convolve2DFilter extends FilterSlice
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	private static String filterName="Convolve";
	private static String filterCategory="Mathematical";

	public static void initPlugin() {}
	static
		{
		Filter.addFilter(new FilterInfo()
			{
			public String getCategory(){return filterCategory;}
			public String getName(){return filterName;}
			public boolean hasFilterROI(){return true;}
			public FilterROI filterROI(){return new Convolve2DFilter();}
			public Filter readXML(Element e)
				{
				Convolve2DFilter f=new Convolve2DFilter();
			//	f.pwhite.setValue(Double.parseDouble(e.getAttributeValue("pwhite")));
		//		f.pblack.setValue(Double.parseDouble(e.getAttributeValue("pblack")));
				return f;
				}
			});
		}
	
	
	public static class ConvolutionKernel
		{
		public String name;
		public int width;
		public float[] filter;
		
		public ConvolutionKernel(){}
			{
			name="Custom convolution";
			width=1;
			filter=new float[]{1};
			}
		public ConvolutionKernel(String name, int width, float[] filter)
			{
			this.name=name;
			this.width=width;
			this.filter=filter;
			}
		}
	
	public ConvolutionKernel[] premadeKernels=new ConvolutionKernel[]{
			new ConvolutionKernel("Laplace4 2D", 3, new float[]{
					0, 1, 0,
					1,-4, 1,
					0, 1, 0}),
			new ConvolutionKernel("Laplace8 2D", 3, new float[]{
					1, 1, 1,
					1,-8, 1,
					1, 1, 1}),
			new ConvolutionKernel("Laplace X", 3, new float[]{
					1,-2, 1}),
			new ConvolutionKernel("Laplace Y", 1, new float[]{
					1,
					-2, 
					1})
	};
	
	
	//Sobel?
	//Gaussian
	//repeat convolution?
	//category
	//IJ filters
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	public EvMutableDouble pwhite=new EvMutableDouble(0.04); //change
	//public EvMutableDouble pblack=new EvMutableDouble(0.04);
	
	public int kernelWidth=1;
	float[] kernel=new float[]{1};

	
	public String getFilterName()
		{
		return filterName;
		}
	
	public void saveMetadata(Element e)
		{
		setFilterXmlHead(e, filterName);
		e.setAttribute("w",""+kernelWidth);
		//TODO
		}

	
	public JComponent getFilterWidget()
		{
		return null; //TODO
		/*	
		JPanel pane=new JPanel(new GridLayout(2,2));
		JNumericFieldMutableDouble npwhite=new JNumericFieldMutableDouble(pwhite);
		JNumericFieldMutableDouble npblack=new JNumericFieldMutableDouble(pblack);
		
		pane.add(new JLabel("P[white]:"));
		pane.add(npwhite);
		pane.add(new JLabel("P[black]:"));
		pane.add(npblack);
		return pane;
*/
	
	 
	 
		}

	
	
	
	public void applyImage(BufferedImage in, BufferedImage out)
		{
		//Copy source
		
		WritableRaster raster = in.copyData( null );
		BufferedImage temp = new BufferedImage( in.getColorModel(), raster, in.isAlphaPremultiplied(), null );
		
		//
		
		ConvolveOp filter=new ConvolveOp(new Kernel(kernelWidth,kernel.length/kernelWidth,kernel));
		filter.filter(temp,out);
		}
	}
