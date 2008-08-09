package endrov.filterOS;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import org.jdom.Element;

import endrov.ev.*;
import endrov.filter.*;
import endrov.util.EvSwingTools;

/**
 * Filter: Order statistics operations in 2D
 * 
 * @author Johan Henriksson
 */
public class OS2DFilter extends FilterSlice
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	private static String filterMeta="OrderStatistics2D";
	private static String filterName="Order Statistics 2D";
	private static String filterCategory="Order Statistics";

	public static void initPlugin() {}
	static
		{
		Filter.addFilter(new FilterInfo()
			{
			public String getCategory(){return filterCategory;}
			public String getMetaName(){return filterMeta;}
			public String getReadableName(){return filterName;}
			public boolean hasFilterROI(){return true;}
			public FilterROI filterROI(){return new OS2DFilter();}
			public Filter readXML(Element e)
				{
				OS2DFilter f=new OS2DFilter();
				f.currentKernel.repeats.setValue(Integer.parseInt(e.getAttributeValue("repeats")));
				return f;
				}
			});
		}
	
	
	/******************************************************************************************************
	 *                               Kernels                                                              *
	 *****************************************************************************************************/
	public static class Kernel
		{
		public String name;
		public int width;
		public boolean[] filter;
		public int midx, midy;
		
		public Kernel(){}
			{
			name="[] 1x1";
			width=1;
			filter=new boolean[]{true};
			midx=midy=0;
			}
		public Kernel(String name, int width, boolean[] filter)
			{
			this.name=name;
			this.width=width;
			this.filter=filter;
			midx=width/2;
			midy=(filter.length/width)/2;
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
	
	public static Kernel kernel3x3=new Kernel("[] 3x3", 3, new boolean[]{true,true,true, true,true,true, true,true,true});
	private static Kernel kernelNull=new Kernel("", 1, new boolean[]{true});
	public static Kernel[] premadeKernels=new Kernel[]{
			kernelNull,
			new Kernel("[] 1x1", 1, new boolean[]{true}),
			new Kernel("[] 2x2", 2, new boolean[]{true,true, true,true}),
			kernel3x3,
			new Kernel("+  3x3", 3, new boolean[]{false,true,false, true,true,true, false,true,false})
	};
	
	
	/******************************************************************************************************
	 *                               Operations                                                           *
	 *****************************************************************************************************/


	
	
	/******************************************************************************************************
	 *                               Filter widget                                                        *
	 *****************************************************************************************************/
	
	private class ConvolvePanel extends JPanel implements ChangeListener, ActionListener
		{
		static final long serialVersionUID=0;
		
		private Filter thisfilter;
		private int leftPanelX=-1;
		private int leftPanelY=-1;		
		private JComboBox kernelCombo=new JComboBox(premadeKernels);
		private JPanel spanel=new JPanel(new BorderLayout());
		private JSpinner xs=new JSpinner(new SpinnerNumberModel(1,1,128,1));
		private JSpinner ys=new JSpinner(new SpinnerNumberModel(1,1,128,1));
		private JPanel leftPanel=new JPanel();
		
		public void makeLeftPanel()
			{
			int h=getKernelHeight();
			if(leftPanelY!=h || leftPanelX!=currentKernel.kernelWidth)
				{
				leftPanelX=currentKernel.kernelWidth;
				leftPanelY=h;
				leftPanel.removeAll();
				leftPanel.setLayout(new GridLayout(h,currentKernel.kernelWidth));
				ButtonGroup bg=new ButtonGroup();
				for(int y=0;y<h;y++)
					for(int x=0;x<currentKernel.kernelWidth;x++)
						{
						JCheckBoxMutableBoolean nc=new JCheckBoxMutableBoolean("",currentKernel.kernelm[y*currentKernel.kernelWidth+x],observer,this);
						JRadioButton radio=new JRadioButton("",x==currentKernel.midx && y==currentKernel.midy);
						bg.add(radio);
						JPanel gp=new JPanel(new GridLayout(1,2));
						gp.setBorder(BorderFactory.createLineBorder(Color.BLACK));
						//radio.setForeground(Color.RED);
						gp.add(nc);
						gp.add(radio);
						leftPanel.add(gp);
						final int finalx=x;
						final int finaly=y;
						radio.addActionListener(new ActionListener(){
							public void actionPerformed(ActionEvent e)
								{
								currentKernel.midx=finalx;
								currentKernel.midy=finaly;
								observer.emit(this);
								}
						});
						}
				leftPanel.revalidate();
				}
			}
		
		public void updateSizeControl()
			{
			xs.removeChangeListener(this);
			ys.removeChangeListener(this);
			xs.setValue(getKernelWidth());
			ys.setValue(getKernelHeight());
			xs.addChangeListener(this);
			ys.addChangeListener(this);
			}
		
		public ConvolvePanel(Filter thisfilter)
			{
			this.thisfilter=thisfilter;
			JNumericFieldMutableInteger nrepeats=new JNumericFieldMutableInteger(currentKernel.repeats,observer,this);
			
			updateSizeControl();
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
			
			add(spanel, BorderLayout.EAST);
			setLayout(new BorderLayout());
			makeLeftPanel();
			add(leftPanel, BorderLayout.WEST);
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
			if(e.getSource()==kernelCombo && e.getActionCommand().equals("comboBoxChanged") && kernelCombo.getSelectedItem()!=kernelNull)
				{
				Kernel sel=(Kernel)kernelCombo.getSelectedItem();
				setKernel(sel);
				leftPanelX=-1;
				makeLeftPanel();
				updateSizeControl();
				observer.emit(thisfilter);
				}
			}
		}
	
	
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	private class CurrentKernel
		{
		public int kernelWidth=1;
		public int midx, midy;
		public EvMutableBoolean[] kernelm=new EvMutableBoolean[]{};
		public EvMutableInteger repeats=new EvMutableInteger(1);
		public Kernel sourceKernel;
		}
	public CurrentKernel currentKernel=new CurrentKernel();
	
	/** Set current kernel */
	public void setKernel(Kernel k)
		{
		currentKernel.kernelWidth=k.width;
		currentKernel.kernelm=new EvMutableBoolean[k.filter.length];
		currentKernel.midx=k.midx;
		currentKernel.midy=k.midy;
		currentKernel.sourceKernel=k;
		for(int i=0;i<k.filter.length;i++)
			currentKernel.kernelm[i]=new EvMutableBoolean(k.filter[i]);
		observer.emit(this);
		}
	/** Resize current kernel, fill with 0's as needed	 */
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
			if(currentKernel.midx>=nw)currentKernel.midx=nw-1;
			if(currentKernel.midy>=nh)currentKernel.midy=nh-1;
			observer.emit(this);
			}
		}
	/** Get height of current kernel */
	public int getKernelHeight()
		{
		return currentKernel.kernelWidth==0 ? 0 : currentKernel.kernelm.length/currentKernel.kernelWidth;
		}
	/** Get width of current kernel */
	public int getKernelWidth()
		{
		return currentKernel.kernelWidth;
		}

	

	
	public OS2DFilter()
		{
		setKernel(kernel3x3);
		}
	
	public String getFilterName(){return filterName;}
	
	public void saveMetadata(Element e)
		{
		setFilterXmlHead(e, filterMeta);
		e.setAttribute("w",""+currentKernel.kernelWidth);
		//TODO
		}

	public JComponent getFilterWidget()
		{
		return new ConvolvePanel(this);
		}
	
	
	
	public static class RelPos
		{
		int x,y;
		}
	
	public void applyImage(BufferedImage in, BufferedImage out)
		{
		int repeatsv=currentKernel.repeats.getValue();
		int w=out.getWidth();
		int h=out.getHeight();

		//Where to get relative pixels from
		java.util.List<RelPos> pos=new LinkedList<RelPos>();
		int minrx=0,maxrx=0,minry=0,maxry=0;
		/*			int possize=0;
			for(int y=0;y<getKernelHeight();y++)
				for(int x=0;x<getKernelHeight();x++)
					if(currentKernel.kernelm[y*getKernelWidth()+x].getValue())
						possize++;
			int posid=0;
			int[] posxy=new int[possize*2];*/
		for(int y=0;y<getKernelHeight();y++)
			for(int x=0;x<getKernelHeight();x++)
				if(currentKernel.kernelm[y*getKernelWidth()+x].getValue())
					{
					RelPos rp=new RelPos();
					rp.x=x-currentKernel.midx;
					rp.y=y-currentKernel.midy;
					if(rp.x<minrx)minrx=rp.x;
					if(rp.x>maxrx)maxrx=rp.x;
					if(rp.y<minry)minry=rp.y;
					if(rp.y>maxry)maxry=rp.y;
					pos.add(rp);
					/*						posxy[posid  ]=rp.x;
						posxy[posid+1]=rp.y;
						posid+=2;*/
					}
		int[] pixels=new int[pos.size()];
		int take=pixels.length/2;
		if(take>=pixels.length)
			take=pixels.length-1;

		for(int currepeat=0;currepeat<repeatsv;currepeat++)
			{


			//Apply filter
			if(pos.size()>0)
				{
				WritableRaster inr=in.getRaster();
				WritableRaster outr=out.getRaster();

				int[][] inarr=new int[h][w];
				for(int y=0;y<h;y++)
					inr.getSamples(0, y, w, 1, 0, inarr[y]);
				int[] outarr=new int[w];

				for(int y=-minry;y<h-maxry;y++)
					{
					for(int x=-minrx;x<w-maxrx;x++)
						{
						//Collect pixels
						int i=0;
						for(RelPos rp:pos)
							//for(int i=0;i<possize;i++)
							{
							int rx=x+rp.x;
							int ry=y+rp.y;
							//int rx=x+posxy[2*i];
							//int ry=y+posxy[2*i+1];
							pixels[i]=inarr[ry][rx];
							i++;
							}
						//Stats, apply
						Arrays.sort(pixels);
						int newval=pixels[take];
						outarr[x]=newval;
						}
					outr.setSamples(0, y, w, 1, 0, outarr);
					}
				}
			in=out;
			}
		}
	}
