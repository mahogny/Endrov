package endrov.filterBasic;

import java.awt.GridLayout;
import java.awt.image.*;

import javax.swing.*;

import org.jdom.Element;

import endrov.ev.*;
import endrov.filter.*;

/**
 * Filter: Adjust contrast & brightness
 * 
 * @author Johan Henriksson
 */
public class ContrastBrightnessFilter extends FilterSlice
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	private static String filterName="Contrast & Brightness";
	private static String filterMeta="ContrastBrightness";
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
			public FilterROI filterROI(){return new ContrastBrightnessFilter();}
			});
		}
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	public EvMutableDouble pcontrast=new EvMutableDouble(1.0);
	public EvMutableDouble pbrightness=new EvMutableDouble(0.0);
	public EvMutableBoolean pauto=new EvMutableBoolean();
	
	public String getFilterName()
		{
		return filterName;
		}
	
	
	public void saveMetadata(Element e)
		{
		setFilterXmlHead(e, filterMeta);
		e.setAttribute("pwhite",""+pcontrast);
		e.setAttribute("pblack",""+pbrightness);
		}
	public void loadMetadata(Element e)
		{
		pcontrast.setValue(Double.parseDouble(e.getAttributeValue("pwhite")));
		pbrightness.setValue(Double.parseDouble(e.getAttributeValue("pblack")));
		}
	
	public JComponent getFilterWidget()
		{
		JPanel pane=new JPanel(new GridLayout(3,2));
		
		JNumericFieldMutableDouble npwhite=new JNumericFieldMutableDouble(pcontrast, observer, this);
		JNumericFieldMutableDouble npblack=new JNumericFieldMutableDouble(pbrightness, observer, this);
		JCheckBoxMutableBoolean nauto=new JCheckBoxMutableBoolean("", pauto, observer, this);
		
		pane.add(new JLabel("Contrast:"));
		pane.add(npwhite);
		pane.add(new JLabel("Brightness:"));
		pane.add(npblack);
		pane.add(new JLabel("Auto:"));
		pane.add(nauto);

		return pane;
		}

	
	
	
	public void applyImage(BufferedImage in, BufferedImage out)
		{
		double contrast=pcontrast.getValue();
		double brightness=pbrightness.getValue();
		
		//Automatic parameters?
		if(pauto.getValue())
			{
			WritableRaster rin=in.getRaster();
			int[] colorcount=new int[256];
			int width=rin.getWidth();
			int[] pix=new int[width];
			for(int ah=0;ah<rin.getHeight();ah++)
				{
				rin.getSamples(0, ah, width, 1, 0, pix);
				for(int aw=0;aw<width;aw++)
					colorcount[pix[aw]]++;
				}
			
			//Cumulative sum
			for(int i=1;i<256;i++)
				colorcount[i]+=colorcount[i-1];
			
			int lower, upper;
			for(lower=0;colorcount[lower]==0;lower++);
			for(upper=255;colorcount[lower]==0;upper--);
			
			contrast=255.0/(upper-lower);
			brightness=-lower*contrast;
			
			}
		
		
		ContrastBrightnessOp bcfilter=new ContrastBrightnessOp(contrast,brightness);
		bcfilter.filter(in,out);
		//danger!? source image changes, should out be unaffected? I think this is needed? force write by copying source?
		}
	}
