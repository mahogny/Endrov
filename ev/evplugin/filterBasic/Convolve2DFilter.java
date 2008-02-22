package evplugin.filterBasic;

//import java.awt.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
				f.repeats.setValue(Integer.parseInt(e.getAttributeValue("repeats")));
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
	
	public static ConvolutionKernel makeSharpen(int k)
		{
		return new ConvolutionKernel("Sharpen"+k+" 2D", 3, new float[]{-k, -k, -k,		-k,8*k+1, -k,		-k, -k, -k});
		}
	

	
	public static ConvolutionKernel[] premadeKernels=new ConvolutionKernel[]{
			new ConvolutionKernel("Laplace4 2D", 3, new float[]{0, 1, 0,		1,-4, 1,		0, 1, 0}),
			new ConvolutionKernel("Laplace8 2D", 3, new float[]{1, 1, 1,		1,-8, 1,		1, 1, 1}),
			new ConvolutionKernel("Laplace X", 3, new float[]{1,-2, 1}),
			new ConvolutionKernel("Laplace Y", 1, new float[]{1,		-2,		1}),
			new ConvolutionKernel("SobelX 2D", 3, new float[]{1, 0,-1,		2, 0,-2,		1, 0,-1}),
			new ConvolutionKernel("SobelY 2D", 3, new float[]{1, 2, 1,		0, 0, 0,		-1,-2,-1}),
			makeSharpen(1),
			makeSharpen(2),
			makeSharpen(3),
	};
	
	
	
	//http://en.wikipedia.org/wiki/Sobel_operator
	//emboss filters not implemented: http://www.gamedev.net/reference/programming/features/imageproc/page2.asp
	//kuwahara TODO http://www.qi.tnw.tudelft.nl/Courses/FIP/noframes/fip-Smoothin.html
	//Gaussian TODO
	//repeat convolution?
	//category
	//IJ filters
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	protected int kernelWidth;
	private EvMutableDouble[] kernelm=new EvMutableDouble[]{};
	private EvMutableInteger repeats=new EvMutableInteger(1);
	private EvMutableBoolean normalize=new EvMutableBoolean(false);
	
	public Convolve2DFilter()
		{
		setKernel(new ConvolutionKernel("Identity", 1, new float[]{1}));
		setKernel(new ConvolutionKernel("SobelY 2D", 3, new float[]{
					 1, 2, 1,
					 0, 0, 0,
					-1,-2,-1}));
		}
	
	public void setKernel(ConvolutionKernel k)
		{
		kernelWidth=k.width;
		kernelm=new EvMutableDouble[k.filter.length];
		for(int i=0;i<k.filter.length;i++)
			kernelm[i]=new EvMutableDouble(k.filter[i]);
		}
	
	
	
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

	
	public void resizeKernel(int nw, int nh)
		{
		EvMutableDouble[] kernel2=new EvMutableDouble[nw*nh];
		int oldh=kernelm.length/kernelWidth;
		int oldw=kernelWidth;
		kernelWidth=nw;
		for(int ay=0;ay<nh;ay++)
			for(int ax=0;ax<nw;ax++)
				{
				if(ay<oldh && ax<oldw)
					kernel2[ay*kernelWidth+ax]=kernelm[ay*oldw+ax];
				else
					kernel2[ay*kernelWidth+ax]=new EvMutableDouble(0);
				}
		kernelm=kernel2;
		}
	
	
	
	private class ConvolvePanel extends JPanel implements ChangeListener
		{
		static final long serialVersionUID=0;
		
		JPanel spanel=new JPanel(new BorderLayout());
		JSpinner xs=new JSpinner(new SpinnerNumberModel(1,1,128,1));
		JSpinner ys=new JSpinner(new SpinnerNumberModel(1,1,128,1));
		
		public JPanel makeLeftPanel()
			{
			int h=kernelm.length/kernelWidth;
			JPanel gpanel=new JPanel(new GridLayout(h,kernelWidth));
			for(int y=0;y<h;y++)
				for(int x=0;x<kernelWidth;x++)
					{
					JNumericFieldMutableDouble nc=new JNumericFieldMutableDouble(kernelm[y*kernelWidth+x],observer,this);
					gpanel.add(nc);
					}
			return gpanel;
			}
		
		Filter thisfilter;
		public ConvolvePanel(Filter thisfilter)
			{
			this.thisfilter=thisfilter;
			int h=kernelm.length/kernelWidth;
			JNumericFieldMutableInteger nrepeats=new JNumericFieldMutableInteger(repeats,observer,this);
			JCheckBoxMutableBoolean nnormalize=new JCheckBoxMutableBoolean("",normalize,observer,this);
			
			xs.setValue(kernelWidth);
			ys.setValue(h);
			xs.addChangeListener(this);
			ys.addChangeListener(this);
			
			JPanel spanell=new JPanel(new GridLayout(4,1));
			JPanel spanelr=new JPanel(new GridLayout(4,1));
			spanell.add(new JLabel("#X:"));
			spanell.add(new JLabel("#Y:"));
			spanell.add(new JLabel("Repeats:"));
			spanell.add(new JLabel("Normalize:"));
			spanelr.add(xs);
			spanelr.add(ys);
			spanelr.add(nrepeats);
			spanelr.add(nnormalize);
			
			spanel.add(spanell,BorderLayout.WEST);
			spanel.add(spanelr,BorderLayout.EAST);

			setLayout(new BorderLayout());
			add(makeLeftPanel(), BorderLayout.WEST);
			add(spanel, BorderLayout.EAST);
			}

		public void stateChanged(ChangeEvent e)
			{
			removeAll();
			resizeKernel((Integer)xs.getValue(), (Integer)ys.getValue());
			add(makeLeftPanel(), BorderLayout.WEST);
			add(spanel, BorderLayout.EAST);
			revalidate();
			observer.emit(thisfilter);
			}
		}
	
	
	public JComponent getFilterWidget()
		{
		return new ConvolvePanel(this);
		}

	
	
	
	public void applyImage(BufferedImage in, BufferedImage out)
		{
		int repeatsv=repeats.getValue();
		
		//Prepare kernel
		float[] kernelf=new float[kernelm.length];
		for(int i=0;i<kernelm.length;i++)
			kernelf[i]=(float)kernelm[i].getValue();
		float kernelsum=0;
		if(normalize.getValue())
			{
			for(float f:kernelf)
				kernelsum+=f;
			if(Math.abs(kernelsum)>0.0001) //Have to deal with FP precision problems
				{
				for(int i=0;i<kernelm.length;i++)
					kernelf[i]/=kernelsum;
				}
			}
		int h=kernelm.length/kernelWidth;
		int w=kernelWidth;
		
		//Ugly hack, uninteresting case
		if(repeatsv==0)
			{
			kernelf=new float[]{1};
			w=h=1;
			}
		
		ConvolveOp filter=new ConvolveOp(new Kernel(w,h,kernelf));


		//Convolve
		for(int i=0;i<repeatsv;i++)
			{
			//Copy source. Responsible for making a copy if entire image is being changed (as in=out)
			WritableRaster raster = in.copyData(null);
			in=new BufferedImage(in.getColorModel(), raster, in.isAlphaPremultiplied(), null);
			filter.filter(in,out);
			in=out;
			}
		
		}
	}
