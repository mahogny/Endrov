package evplugin.filterBasic;

//import java.awt.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
				f.currentKernel.repeats.setValue(Integer.parseInt(e.getAttributeValue("repeats")));
				return f;
				}
			});
		}
	
	
	public static class ConvolutionKernel
		{
		public String name;
		public int width;
		public float[] filter;
		public boolean normalize=true;
		
		public ConvolutionKernel(){}
			{
			name="Custom convolution";
			width=1;
			filter=new float[]{1};
			}
		public ConvolutionKernel(String name, boolean normalize, int width, float[] filter)
			{
			this.name=name;
			this.width=width;
			this.filter=filter;
			this.normalize=normalize;
			}
		public String toString()
			{
			return name;
			}
		}
	
	public static ConvolutionKernel makeSharpen(int k)
		{
		return new ConvolutionKernel("Sharpen"+k+" 2D", true, 3, new float[]{-k, -k, -k,		-k,8*k+1, -k,		-k, -k, -k});
		}
	

	
	public static ConvolutionKernel[] premadeKernels=new ConvolutionKernel[]{
			new ConvolutionKernel("Identity", true, 3, new float[]{0,0,0, 0,1,0, 0,0,0}),
			new ConvolutionKernel("Laplace4 2D", false, 3, new float[]{0, 1, 0,		1,-4, 1,		0, 1, 0}),
			new ConvolutionKernel("Laplace8 2D", false, 3, new float[]{1, 1, 1,		1,-8, 1,		1, 1, 1}),
			new ConvolutionKernel("Laplace X", false, 3, new float[]{1,-2, 1}),
			new ConvolutionKernel("Laplace Y", false, 1, new float[]{1,		-2,		1}),
			new ConvolutionKernel("SobelX 2D", false, 3, new float[]{1, 0,-1,		2, 0,-2,		1, 0,-1}),
			new ConvolutionKernel("SobelY 2D", false, 3, new float[]{1, 2, 1,		0, 0, 0,		-1,-2,-1}),
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

	private class CurrentKernel
		{
		public int kernelWidth=1;
		public EvMutableDouble[] kernelm=new EvMutableDouble[]{};
		public EvMutableInteger repeats=new EvMutableInteger(1);
		public EvMutableBoolean normalize=new EvMutableBoolean(false);
		}
	public CurrentKernel currentKernel=new CurrentKernel();
	
	public Convolve2DFilter()
		{
		setKernel(new ConvolutionKernel("Identity", true, 3, new float[]{0,0,0, 0,1,0, 0,0,0}));
/*		setKernel(new ConvolutionKernel("SobelY 2D", false, 3, new float[]{
					 1, 2, 1,
					 0, 0, 0,
					-1,-2,-1}));*/
		}
	
	/**
	 * Set ker
	 * @param k
	 */
	public void setKernel(ConvolutionKernel k)
		{
		currentKernel.kernelWidth=k.width;
		currentKernel.kernelm=new EvMutableDouble[k.filter.length];
		for(int i=0;i<k.filter.length;i++)
			currentKernel.kernelm[i]=new EvMutableDouble(k.filter[i]);
		currentKernel.normalize.setValue(k.normalize);
		int h=currentKernel.kernelm.length/currentKernel.kernelWidth;
		System.out.println("dim2 "+currentKernel.kernelWidth+" "+h);
		observer.emit(this);
		}
	
	
	
	public String getFilterName()
		{
		return filterName;
		}
	
	public void saveMetadata(Element e)
		{
		setFilterXmlHead(e, filterName);
		e.setAttribute("w",""+currentKernel.kernelWidth);
		//TODO
		}

	/**
	 * Set the new size of the kernel, fill with 0's as needed
	 */
	public void resizeKernel(int nw, int nh)
		{
		EvMutableDouble[] kernel2=new EvMutableDouble[nw*nh];
		int oldh=currentKernel.kernelm.length/currentKernel.kernelWidth;
		int oldw=currentKernel.kernelWidth;
		currentKernel.kernelWidth=nw;
		for(int ay=0;ay<nh;ay++)
			for(int ax=0;ax<nw;ax++)
				{
				if(ay<oldh && ax<oldw)
					kernel2[ay*currentKernel.kernelWidth+ax]=currentKernel.kernelm[ay*oldw+ax];
				else
					kernel2[ay*currentKernel.kernelWidth+ax]=new EvMutableDouble(0);
				}
		currentKernel.kernelm=kernel2;
		}
	
	
	
	private class ConvolvePanel extends JPanel implements ChangeListener, ActionListener
		{
		static final long serialVersionUID=0;
		
		private Filter thisfilter;
		
		private JComboBox kernelCombo=new JComboBox(premadeKernels);
		private JPanel spanel=new JPanel(new BorderLayout());
		private JSpinner xs=new JSpinner(new SpinnerNumberModel(1,1,128,1));
		private JSpinner ys=new JSpinner(new SpinnerNumberModel(1,1,128,1));
		private JPanel leftPanel=new JPanel();
		
		public /*JPanel*/ void makeLeftPanel()
			{
			leftPanel.removeAll();
			int h=currentKernel.kernelm.length/currentKernel.kernelWidth;
			leftPanel.setLayout(new GridLayout(h,currentKernel.kernelWidth));
			System.out.println("dim "+currentKernel.kernelWidth+" "+h);
//			JPanel gpanel=new JPanel(new GridLayout(h,kernelWidth));
			for(int y=0;y<h;y++)
				for(int x=0;x<currentKernel.kernelWidth;x++)
					{
					JNumericFieldMutableDouble nc=new JNumericFieldMutableDouble(currentKernel.kernelm[y*currentKernel.kernelWidth+x],observer,this);
					leftPanel.add(nc);
					}
//			return gpanel;
			leftPanel.revalidate();
			}
		
		public ConvolvePanel(Filter thisfilter)
			{
			this.thisfilter=thisfilter;
			int h=currentKernel.kernelm.length/currentKernel.kernelWidth;
			JNumericFieldMutableInteger nrepeats=new JNumericFieldMutableInteger(currentKernel.repeats,observer,this);
			JCheckBoxMutableBoolean nnormalize=new JCheckBoxMutableBoolean("",currentKernel.normalize,observer,this);
			
			xs.setValue(currentKernel.kernelWidth);
			ys.setValue(h);
			xs.addChangeListener(this);
			ys.addChangeListener(this);
			

			
			kernelCombo.setEditable(true);
			kernelCombo.addActionListener(this);

			
			JPanel lefttot=new JPanel(new GridLayout(4,1));
			JPanel panelxy=new JPanel(new GridLayout(1,2));
			panelxy.add(labelc("#X:",xs));
			panelxy.add(labelc("#Y:",ys));
			lefttot.add(panelxy);
			lefttot.add(labelc("Repeats:",nrepeats));
			lefttot.add(labelc("Normalize:",nnormalize));
			lefttot.add(kernelCombo);
			spanel.add(lefttot,BorderLayout.NORTH);
			
			
			add(spanel, BorderLayout.EAST); //n

			setLayout(new BorderLayout());
			makeLeftPanel();
			add(leftPanel, BorderLayout.WEST);
			//add(makeLeftPanel(), BorderLayout.WEST);
			add(spanel, BorderLayout.EAST);
			}

		public JPanel labelc(String s, JComponent c)
			{
			JPanel p=new JPanel(new BorderLayout());
			p.add(new JLabel(s),BorderLayout.WEST);
			p.add(c,BorderLayout.CENTER);
			return p;
			}
		
		public void stateChanged(ChangeEvent e)
			{
//			removeAll();
	//		if(leftPanel!=null)
//				remove(leftPanel);
			resizeKernel((Integer)xs.getValue(), (Integer)ys.getValue());
//			if(e.getSource()==currentKernel.)
			makeLeftPanel();
//			add(leftPanel, BorderLayout.WEST);
//			revalidate();
			observer.emit(thisfilter);
			}

		public void actionPerformed(ActionEvent e)
			{
			if(e.getSource()==kernelCombo && e.getActionCommand().equals("comboBoxChanged"))
				{
				ConvolutionKernel sel=(ConvolutionKernel)kernelCombo.getSelectedItem();
				setKernel(sel);
				stateChanged(null);
				kernelCombo.removeActionListener(this);
				kernelCombo.setSelectedItem(sel);
				kernelCombo.addActionListener(this);
				}
			}
		
		
		}
	
	
	public JComponent getFilterWidget()
		{
		return new ConvolvePanel(this);
		}

	
	
	
	public void applyImage(BufferedImage in, BufferedImage out)
		{
		int repeatsv=currentKernel.repeats.getValue();
		
		//Prepare kernel
		float[] kernelf=new float[currentKernel.kernelm.length];
		for(int i=0;i<currentKernel.kernelm.length;i++)
			kernelf[i]=(float)currentKernel.kernelm[i].getValue();
		float kernelsum=0;
		if(currentKernel.normalize.getValue())
			{
			for(float f:kernelf)
				kernelsum+=f;
			if(Math.abs(kernelsum)>0.0001) //Have to deal with FP precision problems
				{
				for(int i=0;i<currentKernel.kernelm.length;i++)
					kernelf[i]/=kernelsum;
				}
			}
		int h=currentKernel.kernelm.length/currentKernel.kernelWidth;
		int w=currentKernel.kernelWidth;
		
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
