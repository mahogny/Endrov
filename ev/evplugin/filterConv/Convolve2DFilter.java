package evplugin.filterConv;

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
	
	private static String filterMeta="Convolve2D";
	private static String filterName="Convolve 2D";
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
		public void debugPrint()
			{
			for(int y=0;y<filter.length/width;y++)
				{
				for(int x=0;x<width;x++)
					System.out.print(filter[y*width+x]+" ");
				System.out.println();
				}
			}
		}
	
	public static ConvolutionKernel makeSharpen(float k)
		{
		return new ConvolutionKernel("Sharpen"+k+" 2D", true, 3, new float[]{-k, -k, -k,		-k,8*k+1, -k,		-k, -k, -k});
		}
	


	public static ConvolutionKernel kernelLaplace8 = new ConvolutionKernel("Laplace8 2D", false, 3, new float[]{1, 1, 1,		1,-8, 1,		1, 1, 1});
	
	public static ConvolutionKernel[] premadeKernels=new ConvolutionKernel[]{
			new ConvolutionKernel("Identity", true, 3, new float[]{0,0,0, 0,1,0, 0,0,0}),
			new ConvolutionKernel("Mean 3x3", true, 3, new float[]{1,1,1, 1,1,1, 1,1,1}),
			new ConvolutionKernel("Gaussian 3x3 2D", true, 3, new float[]{1, 2, 1,		2, 4, 2,		1, 2, 1}),
			new ConvolutionKernel("Laplace4 2D", false, 3, new float[]{0, 1, 0,		1,-4, 1,		0, 1, 0}),
			kernelLaplace8,
			new ConvolutionKernel("Laplace X", false, 3, new float[]{1,-2, 1}),
			new ConvolutionKernel("Laplace Y", false, 1, new float[]{1,		-2,		1}),
			new ConvolutionKernel("PrewittX 2D",  false, 3, new float[]{1, 0,-1,		1, 0,-1,		1, 0,-1}),
			new ConvolutionKernel("PrewittY 2D",  false, 3, new float[]{1, 1, 1,		0, 0, 0,		-1,-1,-1}),
			new ConvolutionKernel("PrewittXY 2D", false, 3, new float[]{0, 1, 1,		-1, 0, 1,		-1,-1,0}),//(4.47)
			new ConvolutionKernel("SobelX 2D", false, 3, new float[]{1, 0,-1,		2, 0,-2,		1, 0,-1}),
			new ConvolutionKernel("SobelY 2D", false, 3, new float[]{1, 2, 1,		0, 0, 0,		-1,-2,-1}),
			new ConvolutionKernel("RobinsonX 2D", false, 3, new float[]{1, -1,-1,		1, 2,-1,		1, -1,-1}),
			new ConvolutionKernel("RobinsonY 2D", false, 3, new float[]{-1, -1,-1,		-1, 2,-1,		1, 1,1}),//(4.50)
			//Kirsch operator 4.51 TODO
			//Laplacian of gaussian 4.54 TODO
			makeSharpen(1),
			makeSharpen(2),
			makeSharpen(3),
	};
	
	
	
	//http://en.wikipedia.org/wiki/Sobel_operator
	//emboss filters not implemented: http://www.gamedev.net/reference/programming/features/imageproc/page2.asp
	//kuwahara TODO http://www.qi.tnw.tudelft.nl/Courses/FIP/noframes/fip-Smoothin.html
	//Gaussian TODO
	//category
	//IJ filters
	//http://de.wikipedia.org/wiki/Laplace-Operator
	
	
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
		int h=getKernelHeight();
		System.out.println("dim2 "+currentKernel.kernelWidth+" "+h);
		observer.emit(this);
		}
	
	//TODO: use this one more
	public int getKernelHeight()
		{
		if(currentKernel.kernelWidth==0)
			return 0;
		else 
			return currentKernel.kernelm.length/currentKernel.kernelWidth;
		}
	
	
	public String getFilterName(){return filterName;}
	
	public void saveMetadata(Element e)
		{
		setFilterXmlHead(e, filterMeta);
		e.setAttribute("w",""+currentKernel.kernelWidth);
		//TODO
		}

	/**
	 * Set the new size of the kernel, fill with 0's as needed
	 */
	public void resizeKernel(int nw, int nh)
		{
		int oldh=currentKernel.kernelm.length/currentKernel.kernelWidth;
		int oldw=currentKernel.kernelWidth;
		if(oldw!=nw || oldh!=nh)
			{
			EvMutableDouble[] kernel2=new EvMutableDouble[nw*nh];
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
			System.out.println("dim3 "+nw+" "+nh);
			observer.emit(this);
			}
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
		
		private int leftPanelX=-1;
		private int leftPanelY=-1;

		
		//this way is not really good. should make it remember name of combo and remove updateforsure
		public void makeLeftPanel(boolean updateforsure)
			{
			int h=currentKernel.kernelm.length/currentKernel.kernelWidth;

			boolean changedSize=leftPanelY!=h || leftPanelX!=currentKernel.kernelWidth;
			if(changedSize || updateforsure)
				{
				System.out.println("A "+leftPanelY+" "+h+" "+leftPanelX+" "+currentKernel.kernelWidth+" "+spanel);
				leftPanelX=currentKernel.kernelWidth;
				leftPanelY=h;
				leftPanel.removeAll();
				leftPanel.setLayout(new GridLayout(h,currentKernel.kernelWidth));
				System.out.println("dim "+currentKernel.kernelWidth+" "+h);
				for(int y=0;y<h;y++)
					for(int x=0;x<currentKernel.kernelWidth;x++)
						{
						JNumericFieldMutableDouble nc=new JNumericFieldMutableDouble(currentKernel.kernelm[y*currentKernel.kernelWidth+x],observer,this);
						leftPanel.add(nc);
						}
				leftPanel.revalidate();
				}
			
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
			panelxy.add(EvSwingTools.withLabel("#X:",xs));
			panelxy.add(EvSwingTools.withLabel("#Y:",ys));
			lefttot.add(panelxy);
			lefttot.add(EvSwingTools.withLabel("Repeats:",nrepeats));
			lefttot.add(EvSwingTools.withLabel("Normalize:",nnormalize));
			lefttot.add(kernelCombo);
			spanel.add(lefttot,BorderLayout.NORTH);
			
			
			add(spanel, BorderLayout.EAST); //n

			setLayout(new BorderLayout());
			makeLeftPanel(false);
			System.out.println("first "+leftPanelY+" "+leftPanelX+" ");
			add(leftPanel, BorderLayout.WEST);
			//add(makeLeftPanel(), BorderLayout.WEST);
			add(spanel, BorderLayout.EAST);
			}

		
		
		public void stateChanged(ChangeEvent e)
			{
			resizeKernel((Integer)xs.getValue(), (Integer)ys.getValue());
			makeLeftPanel(false);
			observer.emit(thisfilter);
			observerGUI.emit(this);
			}

		public void actionPerformed(ActionEvent e)
			{
			System.out.println("D "+leftPanelY+" %"+" "+leftPanelX+" "+currentKernel.kernelWidth);
			if(e.getSource()==kernelCombo && e.getActionCommand().equals("comboBoxChanged"))
				{
				ConvolutionKernel sel=(ConvolutionKernel)kernelCombo.getSelectedItem();
				setKernel(sel);
				stateChanged(null);
				makeLeftPanel(true);
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
