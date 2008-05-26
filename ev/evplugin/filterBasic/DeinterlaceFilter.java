package evplugin.filterBasic;

import java.awt.image.*;
import javax.swing.*;
import org.jdom.Element;

import evplugin.filter.*;

/**
 * Filter: Deinterlace by replace every second line by the average of below and above
 * 
 * @author Johan Henriksson
 */
public class DeinterlaceFilter extends FilterSlice
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	private static String filterName="Deinterlace";
	private static String filterCategory="Enhance";

	public static void initPlugin() {}
	static
		{
		Filter.addFilter(new FilterInfo()
			{
			public String getCategory(){return filterCategory;}
			public String getMetaName(){return filterName;}
			public String getReadableName(){return filterName;}
			public boolean hasFilterROI(){return true;}
			public FilterROI filterROI(){return new DeinterlaceFilter();}
			public Filter readXML(Element e)
				{
				DeinterlaceFilter f=new DeinterlaceFilter();
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
		WritableRaster rout=out.getRaster();
		
		int width=rin.getWidth();
		int[] pix=new int[width];
		int[] above=new int[width];
		int[] below=new int[width];
		for(int ah=0;ah<rin.getHeight();ah++)
			{
			if(ah%2==1)
				{
				rin.getSamples(0, ah-1, width, 1, 0, above);
				if(ah+1<rin.getHeight())
					rin.getSamples(0, ah+1, width, 1, 0, below);
				else
					below=above;
				for(int aw=0;aw<width;aw++)
					pix[aw]=(above[aw]+below[aw])/2;
				}
			else
				rin.getSamples(0, ah, width, 1, 0, pix);
			rout.setSamples(0, ah, width, 1, 0, pix);
			}
		}
	}
