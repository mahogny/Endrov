package evplugin.filterBasic;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

import org.jdom.Element;

import java.util.*;


import evplugin.ev.*;
import evplugin.filter.*;

/**
 * Filter: add noise according to poisson distribution
 * 
 * @author Johan Henriksson
 */
public class NoisePoisson extends FilterSlice
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	private static String filterName="Poisson";
	private static String filterCategory="Noise";

	public static void initPlugin() {}
	static
		{
		Filter.addFilter(new FilterInfo()
			{
			public String getCategory(){return filterCategory;}
			public String getName(){return filterName;}
			public boolean hasFilterROI(){return true;}
			public FilterROI filterROI(){return new NoisePoisson();}
			public Filter readXML(Element e)
				{
				NoisePoisson f=new NoisePoisson();
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
		setFilterXmlHead(e, filterName);
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

	
	
	private int nextPoisson(Random r, double lambda) 
		{
		double elambda = Math.exp(-1*lambda);
		double product = 1;
		int count =  0;
		int result=0;
		while (product >= elambda)
			{
			product *= r.nextDouble();
			result = count;
			count++; // keep result one behind
			}
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
				double r=nextPoisson(rand, lambda);
				pix[aw]+=r;
				if(pix[aw]>255)
					pix[aw]=255;
				}
			rout.setSamples(0, ah, width, 1, 0, pix);
			}
		}
	}
