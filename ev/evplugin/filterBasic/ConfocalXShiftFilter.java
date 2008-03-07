package evplugin.filterBasic;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import org.jdom.Element;

import evplugin.ev.*;
import evplugin.filter.*;

/**
 * Filter: Fix confocal images were every second line is shifted a certain amount
 * 
 * @author Johan Henriksson (current implementation)
 * @author Ricardo Figueroa (told about problem and the solution, did first implementation)
 */
public class ConfocalXShiftFilter extends FilterSlice
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	private static String filterName="Confocal X Shift";
	private static String filterMeta="ConfocalXShift";
	private static String filterCategory="Enhance";

	public static void initPlugin() {}
	static
		{
		Filter.addFilter(new FilterInfo()
			{
			public String getCategory(){return filterCategory;}
			public String getName(){return filterName;}
			public boolean hasFilterROI(){return true;}
			public FilterROI filterROI(){return new ConfocalXShiftFilter();}
			public Filter readXML(Element e)
				{
				ConfocalXShiftFilter f=new ConfocalXShiftFilter();
				f.dx.setValue(Double.parseDouble(e.getAttributeValue("dx")));
				return f;
				}
			});
		}
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	public EvMutableDouble dx=new EvMutableDouble(0);
	
	public String getFilterName()
		{
		return filterName;
		}
	
	public void saveMetadata(Element e)
		{
		setFilterXmlHead(e, filterMeta);
		e.setAttribute("dx",""+dx);
		}

	
	public JComponent getFilterWidget()
		{
		JPanel pane=new JPanel(new GridLayout(1,2));
		
		final JNumericFieldMutableDouble nlambda=new JNumericFieldMutableDouble(dx, observer, this);
		
		pane.add(new JLabel("Delta X:"));
		pane.add(nlambda);

		return pane;
		}

	
	
	
	
	public void applyImage(BufferedImage in, BufferedImage out)
		{
		WritableRaster rin=in.getRaster();
		WritableRaster rout=out.getRaster();
		
		
		double dx=this.dx.doubleValue();
		int udx=(int)Math.ceil(dx);
		int ldx=(int)Math.floor(dx);
		double f=dx-ldx;
		double rf=1-f;
		
		int width=rin.getWidth();
		int[] pix=new int[width];
		int[] pix2=new int[width];
		for(int ah=0;ah<rin.getHeight();ah++)
			{
			rin.getSamples(0, ah, width, 1, 0, pix);
			if(ah%2==1)
				{
				int start=0;
				int end=width;
				if(udx>0) end=width-udx;
				if(ldx<0) start=-ldx;					
				for(int aw=start;aw<end;aw++)
					pix2[aw]=(int)(rf*pix[aw+ldx]+f*pix[aw+udx]);
				for(int aw=0;aw<start;aw++)
					pix2[aw]=pix2[start];
				for(int aw=end;aw<width;aw++)
					pix2[aw]=pix2[end-1];
				rout.setSamples(0, ah, width, 1, 0, pix2);
				}
			else
				rout.setSamples(0, ah, width, 1, 0, pix);
			}
		}
	}
