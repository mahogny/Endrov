package evplugin.filterBasic;

import java.awt.image.*;
import javax.swing.*;
import org.jdom.Element;

import evplugin.filter.*;

/**
 * Filter: Increase image contrast by totally inverting histogram CDF
 * 
 * @author Johan Henriksson
 */
public class EqualizeHistogram extends FilterSlice
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	private static String filterName="Histogram Equalize";
	private static String filterCategory="Enhance";

	public static void initPlugin() {}
	static
		{
		Filter.addFilter(new FilterInfo()
			{
			public String getCategory(){return filterCategory;}
			public String getName(){return filterName;}
			public boolean hasFilterROI(){return true;}
			public FilterROI filterROI(){return new EqualizeHistogram();}
			public Filter readXML(Element e)
				{
				EqualizeHistogram f=new EqualizeHistogram();
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
		}

	
	public JComponent getFilterWidget()
		{
		return null;
		}

	
	public void applyImage(BufferedImage in, BufferedImage out)
		{
		WritableRaster rin=in.getRaster();
		
		//Count each color
		int[] colorcount=new int[256];
		int totalCount=rin.getWidth()*rin.getHeight();
		int width=rin.getWidth();
		int[] pix=new int[width];
		for(int ah=0;ah<rin.getHeight();ah++)
			{
			rin.getSamples(0, ah, width, 1, 0, pix);
			for(int aw=0;aw<width;aw++)
				colorcount[pix[aw]]++;
			}
		
		int lowerLimit=0;
//		int lowerLimit=130;
		
		//Eliminate lower part
		int lowerCount=0;
		for(int i=0;i<lowerLimit;i++)
			{
			lowerCount+=colorcount[i];
			colorcount[i]=0;
			}
		totalCount-=lowerCount;
		
		//Cumulative sum. Do separate from renormalization for precision
		for(int i=1;i<256;i++)
			colorcount[i]+=colorcount[i-1];

		//Normalize
		byte[] b=new byte[256];
		if(totalCount==0)
			for(int i=0;i<256;i++)
				b[i]=0;
		else
			for(int i=0;i<256;i++)
				b[i]=(byte)(255*colorcount[i]/totalCount);
		
		ByteLookupTable table=new ByteLookupTable(0,b);
		LookupOp bcfilter=new LookupOp(table,null);
		bcfilter.filter(in,out);
		}
	
	
	
	}
