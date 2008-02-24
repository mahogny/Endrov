package evplugin.filterBasic;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;

import org.jdom.Element;

import evplugin.ev.*;
import evplugin.filter.*;



/**
 * Filter: Binary morphology operations in 2D
 * 
 * @author Johan Henriksson
 */
public class BinMorph2DFilter extends FilterSlice
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	private static String filterMeta="BinaryMorphology2D";
	private static String filterName="Binary Morphology 2D";
	private static String filterCategory="Morphology";

	public static void initPlugin() {}
	static
		{
		Filter.addFilter(new FilterInfo()
			{
			public String getCategory(){return filterCategory;}
			public String getName(){return filterName;}
			public boolean hasFilterROI(){return true;}
			public FilterROI filterROI(){return new BinMorph2DFilter();}
			public Filter readXML(Element e)
				{
				BinMorph2DFilter f=new BinMorph2DFilter();
				f.currentKernel.repeats.setValue(Integer.parseInt(e.getAttributeValue("repeats")));
				return f;
				}
			});
		}
	
	
	public static class MorphKernel
		{
		public String name;
		public int width;
		public boolean[] filter;
		
		public MorphKernel(){}
			{
			name="[] 1x1";
			width=1;
			filter=new boolean[]{true};
			}
		public MorphKernel(String name, int width, boolean[] filter)
			{
			this.name=name;
			this.width=width;
			this.filter=filter;
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
	
	

	
	public static MorphKernel[] premadeKernels=new MorphKernel[]{
			new MorphKernel("[] 1x1", 1, new boolean[]{true}),
			new MorphKernel("[] 2x2", 1, new boolean[]{true,true, true,true}),
			new MorphKernel("[] 3x3", 3, new boolean[]{true,true,true, true,true,true, true,true,true}),
			new MorphKernel("+  3x3", 3, new boolean[]{false,true,false, true,true,true, false,true,false})
	};
	
	
	//http://en.wikipedia.org/wiki/Mathematical_morphology
	//Binary:
	//Erosion
	//Dilation
	//Opening
	//Closing
	
	//unary:
	//Skeletonize?
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	private class CurrentKernel
		{
		public int kernelWidth=1;
		public EvMutableBoolean[] kernelm=new EvMutableBoolean[]{};
		public EvMutableInteger repeats=new EvMutableInteger(1);
		}
	public CurrentKernel currentKernel=new CurrentKernel();
	
	public BinMorph2DFilter()
		{
		setKernel(new MorphKernel());
		}
	
	/**
	 * Set kernel
	 * @param k
	 */
	public void setKernel(MorphKernel k)
		{
		currentKernel.kernelWidth=k.width;
		currentKernel.kernelm=new EvMutableBoolean[k.filter.length];
		for(int i=0;i<k.filter.length;i++)
			currentKernel.kernelm[i]=new EvMutableBoolean(k.filter[i]);
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
	public int getKernelWidth()
		{
		return currentKernel.kernelWidth;
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
			EvMutableBoolean[] kernel2=new EvMutableBoolean[nw*nh];
			currentKernel.kernelWidth=nw;
			for(int ay=0;ay<nh;ay++)
				for(int ax=0;ax<nw;ax++)
					{
					if(ay<oldh && ax<oldw)
						kernel2[ay*currentKernel.kernelWidth+ax]=currentKernel.kernelm[ay*oldw+ax];
					else
						kernel2[ay*currentKernel.kernelWidth+ax]=new EvMutableBoolean(false);
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
		
		public void makeLeftPanel()
			{
			int h=getKernelHeight();//currentKernel.kernelm.length/currentKernel.kernelWidth;

			if(leftPanelY!=h || leftPanelX!=currentKernel.kernelWidth)
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
						JCheckBoxMutableBoolean nc=new JCheckBoxMutableBoolean("",currentKernel.kernelm[y*currentKernel.kernelWidth+x],observer,this);
						leftPanel.add(nc);
						}
				leftPanel.revalidate();
				}
			
			}
		
		public ConvolvePanel(Filter thisfilter)
			{
			this.thisfilter=thisfilter;
			//int h=getHeight();//currentKernel.kernelm.length/currentKernel.kernelWidth;
			JNumericFieldMutableInteger nrepeats=new JNumericFieldMutableInteger(currentKernel.repeats,observer,this);
			
			xs.setValue(getKernelWidth());
			ys.setValue(getKernelHeight());
			xs.addChangeListener(this);
			ys.addChangeListener(this);
			

			
			kernelCombo.setEditable(true);
			kernelCombo.addActionListener(this);

			
			JPanel lefttot=new JPanel(new GridLayout(3,1));
			JPanel panelxy=new JPanel(new GridLayout(1,2));
			panelxy.add(EvSwingTools.withLabel("#X:",xs));
			panelxy.add(EvSwingTools.withLabel("#Y:",ys));
			lefttot.add(panelxy);
			lefttot.add(EvSwingTools.withLabel("Repeats:",nrepeats));
			lefttot.add(kernelCombo);
			spanel.add(lefttot,BorderLayout.NORTH);
			
			
			add(spanel, BorderLayout.EAST); //n

			setLayout(new BorderLayout());
			makeLeftPanel();
			System.out.println("first "+leftPanelY+" "+leftPanelX+" ");
			add(leftPanel, BorderLayout.WEST);
			//add(makeLeftPanel(), BorderLayout.WEST);
			add(spanel, BorderLayout.EAST);
			}

		
		
		public void stateChanged(ChangeEvent e)
			{
			resizeKernel((Integer)xs.getValue(), (Integer)ys.getValue());
			makeLeftPanel();
			observer.emit(thisfilter);
			}

		public void actionPerformed(ActionEvent e)
			{
			System.out.println("D "+leftPanelY+" %"+" "+leftPanelX+" "+currentKernel.kernelWidth);
			if(e.getSource()==kernelCombo && e.getActionCommand().equals("comboBoxChanged"))
				{
				MorphKernel sel=(MorphKernel)kernelCombo.getSelectedItem();
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
		//int repeatsv=currentKernel.repeats.getValue();
		
		
		}
	}
