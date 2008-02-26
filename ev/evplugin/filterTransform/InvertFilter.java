package evplugin.filterTransform;

import java.awt.image.*;

import javax.swing.JComponent;

import org.jdom.Element;

import evplugin.filter.*;

/**
 * Filter: invert image, c'=255-c
 * 
 * @author Johan Henriksson
 */
public class InvertFilter extends FilterSlice
	{
	private static String filterName="Invert";
	private static String filterCategory="Transform";
	
	public static void initPlugin() {}
	static
		{
		Filter.addFilter(new FilterInfo()
			{
			public String getCategory(){return filterCategory;}
			public String getName(){return filterName;}
			public boolean hasFilterROI(){return true;}
			public FilterROI filterROI(){return new InvertFilter();}
			public Filter readXML(Element e)
				{
				InvertFilter f=new InvertFilter();
				return f;
				}
			});
		}

	public String getFilterName()
		{
		return filterName;
		}
	
	
	public JComponent getFilterWidget()
		{
		return null;
		}

	public void saveMetadata(Element e)
		{
		setFilterXmlHead(e, filterName);
		}
	
	public void applyImage(BufferedImage in, BufferedImage out)
		{
		WritableRaster rin=in.getRaster();
		WritableRaster rout=out.getRaster();

		int width=rin.getWidth();
		int[] pix=new int[width];
		for(int ah=0;ah<rin.getHeight();ah++)
			{
			rin.getSamples(0, ah, width, 1, 0, pix);
			for(int aw=0;aw<width;aw++)
				pix[aw]=255-pix[aw];
			rout.setSamples(0, ah, width, 1, 0, pix);
			}
		}
	}
