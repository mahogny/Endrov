package endrov.filterMorphological;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;
import org.jdom.Element;

import endrov.ev.*;
import endrov.filter.*;
import endrov.util.EvSwingUtil;



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
			public String getMetaName(){return filterMeta;}
			public String getReadableName(){return filterName;}
			public boolean hasFilterROI(){return true;}
			public FilterROI filterROI(){return new BinMorph2DFilter();}
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
	
	
	
	
	//gray-scale transformation. draw function to lookup with (linear).
	
	//Order Statistics filtering: median, minimum, maximum special cases
	
	//Canny edge detector p.92
	
	//Dilation (+)  11.9
	//Erosion (-)
	//open: X ( ) B = (X-B)+B
	//closed: X (#) B = (X+B)-B
	
	//hit-or-miss: X (x) B := {b_1 <= X and b_2 <= X^c}, have to implement another filter? 
	//thin:  X (/) B = X\(X (x) B)
	//thick: X (.) B = X U (X (x) B) 
	
	//http://www.google.com/url?sa=t&ct=res&cd=1&url=http%3A%2F%2Fetrij.etri.re.kr%2FCyber%2Fservlet%2FGetFile%3Ffileid%3DSPF-1134112698124&ei=jb3BR5TMFZSiwgHTs638DA&usg=AFQjCNEOEkWiHPJscIUktu6arZNt-R8YuA&sig2=PtzlUyTYVcYb3757PYWBhw
	
	//only make a quick implementation for now?

	
	
	//http://en.wikipedia.org/wiki/Mathematical_morphology
	//Binary:
	//Erosion
	//Dilation
	//Opening
	//Closing
	
	//unary:
	//Skeletonize?
	

	
	
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
				System.out.println("A "+leftPanelY+" "+h+" "+leftPanelX+" "+currentKernel.kernelWidth+" "+spanel);
				leftPanelX=currentKernel.kernelWidth;
				leftPanelY=h;
				leftPanel.removeAll();
				leftPanel.setLayout(new GridLayout(h,currentKernel.kernelWidth));
				System.out.println("dim "+currentKernel.kernelWidth+" "+h);
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
			panelxy.add(EvSwingUtil.withLabel("#X:",xs));
			panelxy.add(EvSwingUtil.withLabel("#Y:",ys));
			lefttot.add(panelxy);
			lefttot.add(EvSwingUtil.withLabel("Repeats:",nrepeats));
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
			
			System.out.println("dim3 "+nw+" "+nh);
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

	

	
	public BinMorph2DFilter()
		{
		setKernel(kernel3x3);
		}
	
	public String getFilterName(){return filterName;}
	
	public void loadMetadata(Element e)
		{
		currentKernel.repeats.setValue(Integer.parseInt(e.getAttributeValue("repeats")));
		}
	public void saveMetadata(Element e)
		{
		setFilterXmlHead(e, filterMeta);
		e.setAttribute("w",""+currentKernel.kernelWidth);
		//TODO
		//TODO
		//TODO
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
