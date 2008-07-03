package endrov.filterOS;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.event.*;

import org.jdom.Element;

import endrov.ev.*;
import endrov.filter.*;



/**
 * Filter: Order statistic kernel filter. Sort from kernel mask and pick entry k
 * 
 * @author Johan Henriksson
 */
public class OSKernelFilter extends FilterSlice
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	private static String filterMeta="OSKernel2D";
	private static String filterName="Order statistics kernel 2D";
	private static String filterCategory="Order statistics";

	public static void initPlugin() {}
	static
		{
		Filter.addFilter(new FilterInfo()
			{
			public String getCategory(){return filterCategory;}
			public String getMetaName(){return filterMeta;}
			public String getReadableName(){return filterName;}
			public boolean hasFilterROI(){return true;}
			public FilterROI filterROI(){return new OSKernelFilter();}
			public Filter readXML(Element e)
				{
				OSKernelFilter f=new OSKernelFilter();
				f.currentKernel.repeats.setValue(Integer.parseInt(e.getAttributeValue("repeats")));
				return f;
				}
			});
		}
	
	
	public static class MorphKernel
		{
		public int width;
		public boolean[] filter;
		
		public MorphKernel(){}
			{
			width=1;
			filter=new boolean[]{true};
			}
		public MorphKernel(int width, boolean[] filter)
			{
			this.width=width;
			this.filter=filter;
			}
		}
	
		
	
	
	
	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/

	private class CurrentKernel
		{
		public int kernelWidth=1;
		public EvMutableBoolean[] kernelm=new EvMutableBoolean[]{new EvMutableBoolean(true)};
		public EvMutableInteger repeats=new EvMutableInteger(1);
		}
	public CurrentKernel currentKernel=new CurrentKernel();
	
	public OSKernelFilter()
		{
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
		//TODO
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
		
		private JPanel spanel=new JPanel(new BorderLayout());
		private JSpinner ws=new JSpinner(new SpinnerNumberModel(1,1,128,1));
		private JSpinner hs=new JSpinner(new SpinnerNumberModel(1,1,128,1));
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
			
			ws.setValue(getKernelWidth());
			hs.setValue(getKernelHeight());
			ws.addChangeListener(this);
			hs.addChangeListener(this);
			

			
			JPanel lefttot=new JPanel(new GridLayout(3,1));
			JPanel panelxy=new JPanel(new GridLayout(1,2));
			panelxy.add(EvSwingTools.withLabel("#X:",ws));
			panelxy.add(EvSwingTools.withLabel("#Y:",hs));
			lefttot.add(panelxy);
			lefttot.add(EvSwingTools.withLabel("Repeats:",nrepeats));
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
			resizeKernel((Integer)ws.getValue(), (Integer)hs.getValue());
			makeLeftPanel();
			observer.emit(thisfilter);
			}

		public void actionPerformed(ActionEvent e)
			{
			System.out.println("D "+leftPanelY+" %"+" "+leftPanelX+" "+currentKernel.kernelWidth);
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
