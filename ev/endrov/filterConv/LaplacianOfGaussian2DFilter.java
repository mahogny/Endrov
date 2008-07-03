package endrov.filterConv;

import java.awt.GridLayout;
import java.awt.image.*;
import javax.swing.*;

import org.jdom.DataConversionException;
import org.jdom.Element;

import endrov.ev.*;
import endrov.filter.*;

//http://homepages.inf.ed.ac.uk/rbf/HIPR2/log.htm

/**
 * Filter: Laplacian of Gaussian in 2D
 * 
 * @author Johan Henriksson
 */
public class LaplacianOfGaussian2DFilter extends FilterSlice
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	private static String filterMeta="LoG2D";
	private static String filterName="LoG 2D";
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
			public FilterROI filterROI(){return new LaplacianOfGaussian2DFilter();}
			public Filter readXML(Element e)
				{
				LaplacianOfGaussian2DFilter f=new LaplacianOfGaussian2DFilter();
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
		p.add(EvSwingTools.withLabel("sigma (pixels):",nlevel));
		return p;
		}

	//or make a function to return the kernel?
	
	public void applyImage(BufferedImage in, BufferedImage out)
		{
		//http://de.wikipedia.org/wiki/Laplace-Operator
		//Variants exist, support them as well
		
		double sigma=flevel.getValue();
		if(sigma!=0) //in=out right?
			{
			double sigma2=sigma*sigma;
			double sigma22=sigma*sigma*2.0;
			int w=(int)Math.ceil(sigma);
			int aw=2*w+1;
			
			double front=1.0/(sigma2*sigma2*Math.PI);
			
			float[] f=new float[aw*aw];
			for(int x=-w;x<=w;x++)
				for(int y=-w;y<=w;y++)
					{
					double r2=x*x+y*y;
					f[aw*(y+w)+w+x]=(float)(front*(1.0-r2/sigma22)*Math.exp(-r2/sigma22));
					}
			
			//TODO: replace with two separable filters
			
			Convolve2DFilter cf=new Convolve2DFilter();
			Convolve2DFilter.ConvolutionKernel kernel=new Convolve2DFilter.ConvolutionKernel(filterName,false, aw,f);
			cf.setKernel(kernel);
			cf.applyImage(in,out);
			}
		}
	}
