package endrov.filterBasic;

import java.awt.GridLayout;
import java.awt.image.*;
import javax.swing.*;

import org.jdom.Element;

import endrov.ev.*;
import endrov.filter.*;

/**
 * Filter: invert image, c'=255-c
 * 
 * @author Johan Henriksson
 */
public class NoisePepperAndSalt extends FilterSlice
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	private static String filterName="Salt and Pepper";
	private static String filterMeta="SaltandPepper";
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
			public FilterROI filterROI(){return new NoisePepperAndSalt();}
			public Filter readXML(Element e)
				{
				NoisePepperAndSalt f=new NoisePepperAndSalt();
				f.pwhite.setValue(Double.parseDouble(e.getAttributeValue("pwhite")));
				f.pblack.setValue(Double.parseDouble(e.getAttributeValue("pblack")));
				return f;
				}
			});
		}
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	public EvMutableDouble pwhite=new EvMutableDouble(0.04);
	public EvMutableDouble pblack=new EvMutableDouble(0.04);
	
	public String getFilterName()
		{
		return filterName;
		}
	
	public void saveMetadata(Element e)
		{
		setFilterXmlHead(e, filterMeta);
		e.setAttribute("pwhite",""+pwhite);
		e.setAttribute("pblack",""+pblack);
		}

	
	public JComponent getFilterWidget()
		{
		JPanel pane=new JPanel(new GridLayout(2,2));
		
		JNumericFieldMutableDouble npwhite=new JNumericFieldMutableDouble(pwhite, observer, this);
		JNumericFieldMutableDouble npblack=new JNumericFieldMutableDouble(pblack, observer, this);
		
		pane.add(new JLabel("P[white]:"));
		pane.add(npwhite);
		pane.add(new JLabel("P[black]:"));
		pane.add(npblack);

		return pane;
		}

	
	
	
	public void applyImage(BufferedImage in, BufferedImage out)
		{
		WritableRaster rin=in.getRaster();
		WritableRaster rout=out.getRaster();

		double pwhite=this.pwhite.doubleValue();
		double pwhiteblack=pwhite+pblack.doubleValue();
		
		int width=rin.getWidth();
		int[] pix=new int[width];
		for(int ah=0;ah<rin.getHeight();ah++)
			{
			rin.getSamples(0, ah, width, 1, 0, pix);
			for(int aw=0;aw<width;aw++)
				{
				double r=Math.random();
				if(r<pwhite)
					pix[aw]=255;
				else if(r<pwhiteblack)
					pix[aw]=0;
				
				}
			rout.setSamples(0, ah, width, 1, 0, pix);
			}
		}
	}
