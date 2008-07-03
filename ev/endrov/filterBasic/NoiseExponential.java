package endrov.filterBasic;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

import org.jdom.Element;

import java.util.*;


import endrov.ev.*;
import endrov.filter.*;

/**
 * Filter: add noise according to exponential distribution
 * 
 * @author Johan Henriksson
 */
public class NoiseExponential extends FilterSlice
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	private static String filterName="Exponential";
	private static String filterMeta="Exponential";
	private static String filterCategory="Noise";

	public static void initPlugin() {}
	static
		{
		Filter.addFilter(new FilterInfo()
			{
			public String getCategory(){return filterCategory;}
			public String getMetaName(){return filterMeta;}
			public String getReadableName(){return filterName;}
			public boolean hasFilterROI(){return true;}
			public FilterROI filterROI(){return new NoiseExponential();}
			public Filter readXML(Element e)
				{
				NoiseExponential f=new NoiseExponential();
				f.lambda.setValue(Double.parseDouble(e.getAttributeValue("lambda")));
				return f;
				}
			});
		}
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	public EvMutableDouble lambda=new EvMutableDouble(5);
	
	public String getFilterName()
		{
		return filterName;
		}
	
	public void saveMetadata(Element e)
		{
		setFilterXmlHead(e, filterMeta);
		e.setAttribute("lambda",""+lambda);
		}

	
	public JComponent getFilterWidget()
		{
		JPanel pane=new JPanel(new GridLayout(1,2));
		
		final JNumericFieldMutableDouble nlambda=new JNumericFieldMutableDouble(lambda, observer, this);
		
		pane.add(new JLabel("Lambda:"));
		pane.add(nlambda);

		return pane;
		}

	

	private double nextExponential(Random r, double b) 
		{
		double randx;
		double result;
		randx = r.nextDouble();
		result = -1*b*Math.log(randx);
		return result;
		}

	
	
	
	public void applyImage(BufferedImage in, BufferedImage out)
		{
		WritableRaster rin=in.getRaster();
		WritableRaster rout=out.getRaster();
		Random rand=new Random();
		
		
		double lambda=this.lambda.doubleValue();
		
		int width=rin.getWidth();
		int[] pix=new int[width];
		for(int ah=0;ah<rin.getHeight();ah++)
			{
			rin.getSamples(0, ah, width, 1, 0, pix);
			for(int aw=0;aw<width;aw++)
				{
				double r=nextExponential(rand, lambda);
				pix[aw]+=r;
				if(pix[aw]>255)
					pix[aw]=255;
				}
			rout.setSamples(0, ah, width, 1, 0, pix);
			}
		}
	}
