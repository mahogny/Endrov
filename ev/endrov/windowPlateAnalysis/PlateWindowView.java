package endrov.windowPlateAnalysis;

import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.SwingUtilities;

import endrov.core.log.EvLog;
import endrov.data.EvPath;
import endrov.gui.EvColor;
import endrov.gui.EvSwingUtil;
import endrov.imglib.evop.EvOpScaleImage;
import endrov.typeImageset.EvChannel;
import endrov.typeImageset.EvImagePlane;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvStack;
import endrov.typeParticleMeasure.ParticleMeasure;
import endrov.util.math.EvDecimal;
import endrov.windowPlateAnalysis.CalcAggregation.AggregationMethod;
import endrov.windowPlateAnalysis.PlateAnalysisQueueWidget.WellRunnable;
import endrov.windowPlateAnalysis.scene.Scene2DElement;
import endrov.windowPlateAnalysis.scene.Scene2DHistogram;
import endrov.windowPlateAnalysis.scene.Scene2DImage;
import endrov.windowPlateAnalysis.scene.Scene2DRect;
import endrov.windowPlateAnalysis.scene.Scene2DScatter;
import endrov.windowPlateAnalysis.scene.Scene2DText;
import endrov.windowPlateAnalysis.scene.Scene2DView;
import endrov.windowPlateAnalysis.scene.Scene2DText.Alignment;

/**
 * View for plates
 * 
 * @author Johan Henriksson
 *
 */
public class PlateWindowView extends Scene2DView implements MouseListener, MouseMotionListener, MouseWheelListener
	{
	private static final long serialVersionUID = 1L;

	
	public static final String layoutByWellID="Well ID";
	
	public interface Listener
		{
		public void attributesMayHaveUpdated();

		public WellRunnable getNextBackgroundTask();
		}
	
	/******************************************************************************************************
	 *                               Internal classes                                                     *
	 *****************************************************************************************************/

	/**
	 * How to display one well
	 */
	private static class OneWell
		{
		public int x, y;
		public Double aggrValue;
		public LinkedList<Double> arrA, arrB;
		public EvPixels pixels;
		public EvChannel evChannel;
		public Scene2DImage imp;
		}


	
	/**
	 * Thread that does calculations in the background
	 */
	public class WorkerThread extends Thread
		{
		public void run()
			{
			for(;;)
				{
				Runnable r=getNextBackgroundTask();
				if(r==null)
					return;
				try
					{
					r.run();
					}
				catch (Exception e)
					{
					e.printStackTrace();
					}
				}			
			}
		}
	

	

	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/


	public static final String aggrHide="Layout only";
	public static final String aggrImage="Image";
	public static final String aggrHistogram="Histogram";
	public static final String aggrScatter="Scatter";

	
	private Object aggrMethod=aggrHide;
	
	private String attr1="", attr2="";
//	private EvPath pathToFlow;
	
	/** Last coordinate of the mouse pointer. Used to detect dragging distance. */
	private int mouseLastDragX=0, mouseLastDragY=0;
	/** Last coordinate of the mouse pointer. Used to detect moving distance. For event technical reasons,
	 * this requires a separate set of variables than dragging (or so it seems) */
	public int mouseLastX=0, mouseLastY=0;
	/** Current mouse coordinate. Used for repainting. */
	public int mouseCurX=0, mouseCurY=0;
	/** Flag if the mouse cursor currently is in the window */
	public boolean mouseInWindow=false;

	private ValueRange imIntensityRange=new ValueRange();

	
	
	/**
	 * Date source: Particle measure
	 */
	private ParticleMeasure pm=null;
	
	/**
	 * Data source: wells
	 */
	public Map<EvPath, OneWell> wellMap=new TreeMap<EvPath, OneWell>();
	
	/** Grids that has been laid out */
	private LinkedList<Scene2DElement> grids=new LinkedList<Scene2DElement>();

	/** Image loading thread */
	private WorkerThread imageLoaderThread=new WorkerThread();	
	/** Flag if to shut down image loading thread */
	private boolean imageThreadClose=false;
	/** Lock used for image loading thread */
	private Object imageThreadLock=new Object();

	/** The last wells that existed - to see if anything has changed in layout */
	private TreeSet<String> lastWellPaths=new TreeSet<String>();

	
	private Integer thumbnailSize=200;
	private double contrast=1, brightness=0;
	static final int imageMargin=1000;
	static final int imageSize=10000;
	private int scaleText=100;
	private EvDecimal currentFrame=EvDecimal.ZERO;
	private EvDecimal currentZ=EvDecimal.ZERO;

	private Scene2DText sceneItemText=new Scene2DText(0,0,"");

	private Listener listener;
	
	/**
	 * Construct panel
	 */
	public PlateWindowView(Listener listener)
		{
		this.listener=listener;
		
		//Attach listeners
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		
		imageLoaderThread.start();
		
		sceneItemText.alignment=Alignment.Center;
		sceneItemText.font=new Font("Arial", Font.PLAIN, 15*scaleText);
		sceneItemText.color=EvColor.white;
		}


	
	public static class ValueRange
		{
		public Double min, max;
		
		public boolean hasValue()
			{
			return min!=null;
			}
		
		public void add(double v)
			{
			if(max==null || v>max)
				max=v;
			if(min==null || v<min)
				min=v;
			}
		
		public double rescale(double v)
			{
			if(max==min)
				return 1;
			else
				return (v-min)/(max-min);
			}

		public void clear()
			{
			min=max=null;
			}
		}

	
	/**
	 * Take current settings of sliders and apply it to image
	 */
	public void redrawPanel()
		{
		//Update scene graph
		clear();
	
		for(Scene2DElement e:grids)
			addElem(e);
		
		//Calculate aggregate value
		ValueRange ragg=new ValueRange();
		ValueRange raggA=new ValueRange();
		ValueRange raggB=new ValueRange();
		if(!aggrMethod.equals(aggrHide) && !aggrMethod.equals(aggrImage) && pm!=null && attr1!=null && attr2!=null && 
				pm.getParticleColumns().contains(attr1) && pm.getParticleColumns().contains(attr2))
			{
			for(Map.Entry<EvPath, OneWell> e:wellMap.entrySet())
				{
				OneWell w=e.getValue();
				EvPath path=e.getKey();

				String pathString=path.toString();

				//Extract relevant particle values
				LinkedList<Double> listA=new LinkedList<Double>();
				LinkedList<Double> listB=new LinkedList<Double>();
				w.arrA=listA;
				w.arrB=listB;
				w.aggrValue=null;
				
				ParticleMeasure.Well pmw=pm.getWell(pathString);
				if(pmw!=null)
					{
					//TODO closest frame
					ParticleMeasure.Frame mapp=pmw.getFrame(currentFrame);
					if(mapp!=null)
						{
						for(ParticleMeasure.ColumnSet pi:mapp.getParticles())
							{
							listA.add(pi.getDouble(attr1));
							listB.add(pi.getDouble(attr2));
							}

						if(aggrMethod instanceof CalcAggregation.AggregationMethod)
							{
							CalcAggregation.AggregationMethod am=(CalcAggregation.AggregationMethod)aggrMethod;
							w.aggrValue=am.calc(listA, listB);
							}
						}
					
					}
				}
			
			
			//Normalize values
			for(OneWell w:wellMap.values())
				{
				if(w.aggrValue!=null)
					ragg.add(w.aggrValue);
				if(w.arrA!=null)
					for(double d:w.arrA)
						raggA.add(d);
				if(w.arrB!=null)
					for(double d:w.arrB)
						raggB.add(d);
				}
			for(OneWell w:wellMap.values())
				if(w.aggrValue!=null)
					w.aggrValue=ragg.rescale(w.aggrValue);
			}
		
		
		//Add wells to scene
		for(OneWell w:wellMap.values())
			{
			boolean drawRect=true;
			
			if(aggrMethod.equals(aggrImage))
				{
				//Show image
				if(w.imp!=null)
					{
					Scene2DImage imp=w.imp;
					imp.x=w.x;
					imp.y=w.y;
					addElem(imp);
					drawRect=false;				
					}
				}
			else if(aggrMethod instanceof CalcAggregation.AggregationMethod)
				{
				if(w.aggrValue!=null)
					{
					Scene2DRect imp=new Scene2DRect(w.x, w.y, imageSize, imageSize);
					imp.fillColor=new EvColor("", w.aggrValue, w.aggrValue, w.aggrValue, 1);
					addElem(imp);
					}
				else
					{
					Scene2DRect impRect=new Scene2DRect(w.x, w.y, imageSize, imageSize);
					impRect.fillColor=new EvColor("", 1,0,0, 1);
					addElem(impRect);
					}
				}
			else if(aggrMethod.equals(aggrHistogram) && w.arrA!=null)
				{
				//Calculate histogram
				int[] barh=null;
				if(w.arrA!=null)
					{
					int numbin=w.arrA.size()/10;
					if(numbin>50)
						numbin=50;
					if(numbin<5)
						numbin=5;
					barh=new int[numbin];
					for(double d:w.arrA)
						{
						int i=(int)(barh.length*raggA.rescale(d));
						if(i==barh.length)
							i=barh.length-1;
						barh[i]++;
						}
					for(int i=0;i<barh.length;i++)
						barh[i]*=(double)imageSize/w.arrA.size();
					}

				//Draw histogram
				if(barh!=null)
					{
					int barw=imageSize/barh.length;
					Scene2DHistogram imp=new Scene2DHistogram(w.x, w.y+imageSize, barw, barh);
					addElem(imp);

					addScaleBarX(w, raggA);
					}
				}
			else if(aggrMethod.equals(aggrScatter) && w.arrA!=null && w.arrB!=null)
				{
				int n=w.arrA.size();
				int[] listx=new int[n];
				int[] listy=new int[n];
				for(int i=0;i<n;i++)
					{
					listx[i]=w.x + (int)(imageSize*raggA.rescale(w.arrA.get(i)));
					listy[i]=w.y + imageSize - (int)(imageSize*raggB.rescale(w.arrB.get(i)));
					}
				
				Scene2DScatter imp=new Scene2DScatter(listx, listy);
				addElem(imp);
				
				addScaleBarX(w, raggA);
				addScaleBarY(w, raggB);
				}
			
			if(drawRect)
				{
				Scene2DRect impRect=new Scene2DRect(w.x-1, w.y-1, imageSize+2, imageSize+2);
				impRect.borderColor=new EvColor("", 1,1,1, 0.3);
				addElem(impRect);
				}
			}

		
		//Figure out if camera should be updated
		TreeSet<String> newPaths=new TreeSet<String>();
		for(EvPath p:wellMap.keySet())
			newPaths.add(p.toString());
		if(!newPaths.equals(lastWellPaths))
			{
			zoomToFit();
			lastWellPaths=newPaths;
			}


		addElem(sceneItemText);
		repaint();
		}
	
	
	private void addScaleBarX(OneWell w, ValueRange raggX)
		{
		if(!raggX.hasValue())
			return;
		
		NumberFormat nf=NumberFormat.getInstance();
		nf.setMaximumFractionDigits(5);
		nf.setGroupingUsed(false);
		
		Scene2DText textLower=new Scene2DText(w.x, w.y+imageSize+imageMargin/3, nf.format(raggX.min));
		Scene2DText textUpper=new Scene2DText(w.x+imageSize, w.y+imageSize+imageMargin/3, ""+nf.format(raggX.max));
		
		textLower.alignment=Alignment.Left;
		textUpper.alignment=Alignment.Right;
		textUpper.font=textLower.font=new Font("Arial", Font.PLAIN, 4*scaleText);
		
		addElem(textLower);
		addElem(textUpper);
		}

	private void addScaleBarY(OneWell w, ValueRange raggY)
		{
		if(!raggY.hasValue())
			return;
		
		NumberFormat nf=NumberFormat.getInstance();
		nf.setMaximumFractionDigits(5);
		nf.setGroupingUsed(false);
		
		Scene2DText textLower=new Scene2DText(w.x+imageSize+imageMargin/10, w.y, nf.format(raggY.min));
		Scene2DText textUpper=new Scene2DText(w.x+imageSize+imageMargin/10, w.y+imageSize, ""+nf.format(raggY.max));
		
		textLower.alignment=Alignment.Left;
		textUpper.alignment=Alignment.Left;
		textUpper.font=textLower.font=new Font("Arial", Font.PLAIN, 1*scaleText);
		
		addElem(textLower);
		addElem(textUpper);
		}

	
	private void setCalculatingText(final OneWell well)
		{
		EvSwingUtil.invokeAndWaitIfNeeded(new Runnable()
			{
			public void run()
				{
				if(well!=null)
					{
					sceneItemText.text="Calculating...";
					sceneItemText.x=well.x+imageSize/2;
					sceneItemText.y=well.y+imageSize/2;
					}
				else
					sceneItemText.text="";
				repaint();
				}
			});
		}
	
	/**
	 * Callback: Mouse button clicked
	 */
	public void mouseClicked(MouseEvent e)
		{
		requestFocus();
		}
	/**
	 * Callback: Mouse button pressed
	 */
	public void mousePressed(MouseEvent e)
		{
		requestFocus();
		mouseLastDragX=e.getX();
		mouseLastDragY=e.getY();
		}
	/**
	 * Callback: Mouse button released
	 */
	public void mouseReleased(MouseEvent e)
		{
		}
	/**
	 * Callback: Mouse pointer has entered window
	 */
	public void mouseEntered(MouseEvent e)
		{
		mouseInWindow=true;
		}
	/**
	 * Callback: Mouse pointer has left window
	 */
	public void mouseExited(MouseEvent e)
		{
		mouseInWindow=false;
		}
	/**
	 * Callback: Mouse moved
	 */
	public void mouseMoved(MouseEvent e)
		{
		mouseLastX=e.getX();
		mouseLastY=e.getY();
		mouseInWindow=true;
		mouseCurX=e.getX();
		mouseCurY=e.getY();
				
		//Need to update currentHover so always repaint.
		repaint();
		}
	/**
	 * Callback: mouse dragged
	 */
	public void mouseDragged(MouseEvent e)
		{
		mouseInWindow=true;
		int dx=e.getX()-mouseLastDragX;
		int dy=e.getY()-mouseLastDragY;
		mouseLastDragX=e.getX();
		mouseLastDragY=e.getY();
		if(SwingUtilities.isRightMouseButton(e))
			pan(dx,dy);
		}
	/**
	 * Callback: Mouse scrolls
	 */
	public void mouseWheelMoved(MouseWheelEvent e)
		{
		//TODO use e.getWheelRotation() only
		//Self-note: linux machine at home (mahogny) uses UNIT_SCROLL
		if(e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL)
			scrollZoom(e.getUnitsToScroll()/5.0);
		else if(e.getScrollType() == MouseWheelEvent.WHEEL_BLOCK_SCROLL)
			scrollZoom(e.getUnitsToScroll()*2);
		}

	private void scrollZoom(double val)
		{
		zoom(Math.pow(10,val/10));
		}
	

	
	private String layoutMethod=layoutByWellID;
	
	public void setLayoutMethod(String method)
		{
		layoutMethod=method;
		layoutWells();
		}

	

	public void layoutWells()
		{
		TreeSet<String> wellNames=new TreeSet<String>();
		for(EvPath p:wellMap.keySet())
			wellNames.add(p.getLeafName());

		//Check which layout method to use
		String orderByColumn=null;
		if(!layoutMethod.equals(layoutByWellID) && pm!=null)
			{
			if(pm.getWellColumns().contains(layoutByWellID))
				orderByColumn=layoutByWellID;
			}

		if(orderByColumn!=null)
			{
			/////////// Multiple rows. One for each type
			
			//Collect available attributes
			TreeMap<String,Integer> valueRowCount=new TreeMap<String, Integer>();
			for(EvPath p:wellMap.keySet())
				{
				ParticleMeasure.Well w=pm.getWell(p.toString());
				Object attrOb=w.getWellColumns().getObject(orderByColumn);
				String attr;
				if(attrOb!=null)
					attr=attrOb.toString();
				else
					attr="NULL";
				valueRowCount.put(attr, 0);
				}
				
			//Make a header for each column
			Font headerFont=new Font("Arial", Font.PLAIN, 20*scaleText);
			ArrayList<String> headers=new ArrayList<String>(valueRowCount.keySet());
			for(int i=0;i<headers.size();i++)
				{
				Scene2DText st=new Scene2DText(i*(imageSize*2+imageMargin), imageSize/2, headers.get(i));
				st.font=headerFont;
				st.alignment=Alignment.Center;
				grids.add(st);
				}
			

			//Place wells under headers
			Font wellNameFont=new Font("Arial", Font.PLAIN, 20*scaleText);
			for(EvPath p:wellMap.keySet())
				{
				ParticleMeasure.Well w=pm.getWell(p.toString());
				Object attrOb=w.getWellColumns().getObject(orderByColumn);
				String attr;
				if(attrOb!=null)
					attr=attrOb.toString();
				else
					attr="NULL";

				int indexHeader=headers.indexOf(attr);
				int rowIndex=valueRowCount.get(attr);
				valueRowCount.put(attr, rowIndex+1);

				//Layout well
				OneWell well=wellMap.get(p);
				well.x=indexHeader*(imageSize*2+imageMargin);   //Text beneath would make more sense(?)
				well.y=rowIndex*(imageSize+imageMargin);
				
				//Name of well
				Scene2DText st=new Scene2DText(imageSize+imageMargin, rowIndex*(imageSize+imageMargin) + imageSize/2, p.toString());
				st.font=wellNameFont;
				st.alignment=Alignment.Left;
				grids.add(st);
				}
			
			}
		else
			{
			//Grid-structured layout
			grids.clear();
			MultiWellGridLayout g=MultiWellGridLayout.isMultiwellFormat(wellNames);
			if(g!=null)
				{
				/////////Multi-well layout
				
				//Set image positions
				for(EvPath p:wellMap.keySet())
					{
					OneWell well=wellMap.get(p);
					MultiWellPlateIndex pos=MultiWellPlateIndex.parse(p.getLeafName());
					if(pos!=null)
						{
						well.x=(pos.indexNumber-1)*(imageSize+imageMargin);
						well.y=(pos.indexLetter-1)*(imageSize+imageMargin);
						}
					}

				//Place text
				Font gridFont=new Font("Arial", Font.PLAIN, 60*scaleText);
				for(int i=1;i<=g.numLetter;i++)
					{
					char c='A';
					Scene2DText st=new Scene2DText(g.x - 20*scaleText, g.y + (i-1)*(g.distance)+50*scaleText, ""+(char)(c+i-1));
					st.font = gridFont;
					st.alignment=Alignment.Right;
					st.color=EvColor.red;
					grids.add(st);
					}
				for(int i=1;i<=g.numNumber;i++)
					{
					Scene2DText st=new Scene2DText(g.x + (i-1)*(g.distance)+50*scaleText, g.y - 40*scaleText, ""+i);
					st.font=gridFont;
					st.alignment=Alignment.Center;
					st.color=EvColor.red;
					grids.add(st);
					}
				}
			else
				{
				/////////General layout. Just a linear list
				
				Font gridFont=new Font("Arial", Font.PLAIN, 20*scaleText);
				int posIndex=0;
				for(EvPath p:wellMap.keySet())
					{
					OneWell well=wellMap.get(p);
					well.x=0;
					well.y=posIndex*(imageSize+imageMargin);
					
					Scene2DText st=new Scene2DText(imageSize+imageMargin, posIndex*(imageSize+imageMargin) + imageSize/2, p.toString());
					st.font=gridFont;
					st.alignment=Alignment.Left;
					grids.add(st);
					
					posIndex++;
					}
				}

			}
		}
		

		
	
	
	/**
	 * Get the next image to be loaded. Returns null when the thread should quit
	 */
	private Runnable getNextBackgroundTask()
		{
		synchronized (imageThreadLock)
			{
			for(;;)
				{
				//If the thread is to close, then return nothing
				if(imageThreadClose)
					return null;

				//Try to find images to be loaded
				if(aggrMethod.equals(aggrImage))
					{
					//Load images
					for(final OneWell w:wellMap.values())
						if(w.pixels==null || w.imp==null || w.imp.pixels==null)
							return new Runnable(){public void run(){loadImageForWell(w);}};
					}
/*				
				//If a histogram/scatter plot is to be shown, see if there is anything that must be calculated
				if(!aggrMethod.equals(aggrHide))
					{
					//Run flow on currently selected images
					for(final EvPath pathToWell:wellMap.keySet())
						{
						if(pm!=null && pathToFlow!=null)
							{
							ParticleMeasure.Well pmw=pm.getWell(pathToWell.toString());
							if(pmw==null)
								{
								return new Runnable()
									{
									public void run()
										{
										OneWell well=wellMap.get(pathToWell);
										if(well!=null && well.evChannel!=null)
											{
											setCalculatingText(well);
											
											try
												{
												ParticleMeasure wellPM=ParticleMeasureWellFlowExec.execFlowOnWell(pathToWell, pathToFlow);
												ParticleMeasureWellFlowExec.mergeWellPM(pm, wellPM, pathToWell);
												
												SwingUtilities.invokeAndWait(new Runnable(){public void run(){
													redrawPanel();
													listener.attributesMayHaveUpdated();
													setCalculatingText(null);
													}}); 
												 //This does way more work than needed(?)
												}
											catch (Exception e)
												{
												EvLog.printError(e);
												}
											}
										}
									};
								}
							}
						}
					}
*/
				//If there is no job for the data displayed, then instead check the batch queue
				final WellRunnable runnable=listener.getNextBackgroundTask();
				if(runnable!=null)
					{
					
					
					return new Runnable()
						{
							public void run()
								{
								try
									{
									SwingUtilities.invokeAndWait(new Runnable(){public void run(){
										//See if this well is displayed. If so, show the calculating text
										OneWell well=wellMap.get(runnable.pathToWell);
										if(well!=null)
											setCalculatingText(well);
									}}); 

									//Do the work
									runnable.run();

									SwingUtilities.invokeAndWait(new Runnable(){public void run(){
										redrawPanel();
										listener.attributesMayHaveUpdated();
										setCalculatingText(null);
									}});
									}
								catch (Throwable e)
									{
									EvLog.printError(e);
									} 
								}
						};
					}
				
				//No work to return. Sleep for now
				try
					{
					imageThreadLock.wait();
					}
				catch (InterruptedException e){}
				}
			}
		}
	
	
	
	

	/**
	 * Get the pixel data from the data source
	 */
	private void loadImageForWell(OneWell w)
		{
		if(w.pixels==null && w.evChannel!=null)
			{
			EvDecimal closestFrame=w.evChannel.closestFrame(currentFrame);
			EvStack stack=w.evChannel.getStack(closestFrame);
			if(stack!=null)
				{
				if(w!=null)
					setCalculatingText(w);
				
				int z=stack.getClosestPlaneIndex(currentZ.doubleValue());
				if(z>=stack.getDepth())
					z=stack.getDepth()-1;
					
				//Fetch image from channel
				if(thumbnailSize!=null)
					{
					double scaleFactor=thumbnailSize/(double)stack.getWidth();
					stack=new EvOpScaleImage(scaleFactor, scaleFactor).exec1(null, stack);  //TODO no reason to scale all stack
					}
				EvImagePlane evim=stack.getPlane(z);
				EvPixels pixels=evim.getPixels();
				w.pixels=pixels;

				//Set up scene element
				Scene2DImage imp=new Scene2DImage();
				imp.pixels=w.pixels;
				applyContrastBrightness(imp);
				imp.resX=imageSize/(double)w.pixels.getWidth();
				imp.resY=imageSize/(double)w.pixels.getHeight();
				imp.prepareImage();
				w.imp=imp; //This should be done last
				
				//Keep track of the intensity range
				for(float f:pixels.convertToFloat(true).getArrayFloat())
					imIntensityRange.add(f);
				
				try
					{
					SwingUtilities.invokeAndWait(new Runnable(){public void run(){
						setCalculatingText(null);
						redrawPanel();
					}}); 
					 //This does way more work than needed(?)
					}
				catch (Exception e)
					{
					e.printStackTrace();
					}
				}
			else
				setCalculatingText(null);
			}
		}

	
	/**
	 * Remove all wells (and thus all data)
	 */
	public void clearWells()
		{
		imIntensityRange.clear();
		wellMap.clear();
		}
	
	/**
	 * Add one well to the panel
	 */
	public void addWell(EvPath p, EvChannel channel)
		{
		if(p==null)
			throw new RuntimeException("null path");
			
		OneWell well=new OneWell();
		well.evChannel=channel; //TODO baaaad! this locks the memory. what trouble might it cause?
		
		synchronized (imageThreadLock)
			{
			wellMap.put(p, well);
			imageThreadLock.notifyAll();
			}
		}

	
	
	



	/**
	 * Set the aggregation methods to use
	 */
	public void setAggrMethod(Object o, String attr1, String attr2)
		{
		aggrMethod=o;
		this.attr1=attr1;
		this.attr2=attr2;
		redrawPanel();
		}

	/**
	 * Get a list of all aggregation modes
	 */
	public static Object[] getAggrModes()
		{
		LinkedList<Object> list=new LinkedList<Object>();
		list.add(aggrHide);
		list.add(aggrImage);
		list.add(aggrHistogram);
		list.add(aggrScatter);
		for(AggregationMethod m:CalcAggregation.getAggregationMethods())
			list.add(m);
		return list.toArray(new Object[0]);
		}



	
	public void setParticleMeasure(ParticleMeasure particleMeasure)
		{
		pm=particleMeasure;
		}

	
	
	public void setFlow(EvPath pathToFlow)
		{
//		this.pathToFlow=pathToFlow;
		}
	
	
	
	
	
	
		
	/**
	 * Set the contrast and brightness for all images
	 */
	public void setContrastBrightness(double contrast, double brightness)
		{
		this.contrast=contrast;
		this.brightness=brightness;
		
		//Update for each well
		for(OneWell w:wellMap.values())
			if(w.imp!=null)
				applyContrastBrightness(w.imp);
		
		repaint();
		}


	private void applyContrastBrightness(Scene2DImage imp)
		{
		imp.setContrastBrightness(contrast, brightness);
		}


	/**
	 * Set the current frame and z
	 */
	public void setFrameZ(EvDecimal frame, EvDecimal z)
		{
		currentFrame=frame;
		currentZ=z;
		invalidateAllImages();
		}

	/**
	 * Remove all images, prepare to reload them
	 */
	private void invalidateAllImages()
		{
		for(OneWell w:wellMap.values())
			{
			w.imp=null;
			w.pixels=null;
			}
		synchronized (imageThreadLock)
			{
			imageThreadLock.notifyAll();
			}
		imIntensityRange.clear();
		}
	

	/**
	 * Called when window is closed
	 */
	public void freeResources()
		{
		synchronized (imageThreadLock)
			{
			imageThreadClose=true;
			imageThreadLock.notifyAll();
			}
		}


	public ValueRange getIntensityRange()
		{
		return imIntensityRange;
		}

	




	/**
	 * Set the size of the thumbnail images, if to be rescaled
	 * 
	 * @param size  Null if no rescale
	 */
	public void setThumbnailImageSize(Integer size)
		{
		thumbnailSize=size;
		synchronized (imageThreadLock)
			{
			for(OneWell w:wellMap.values())
				w.pixels=null;
			imageThreadLock.notifyAll();
			}
		}


	/**
	 * Clear the output PM
	 */
	public void clearPM()
		{
		synchronized (imageThreadLock)
			{
			if(pm!=null)
				{
				pm.clearData();
				layoutWells();
				}
			imageThreadLock.notifyAll();
			}
		}


	public void startWorkerThread()
		{
		synchronized (imageThreadLock)
			{
			imageThreadLock.notifyAll();
			}
		}


	
	
	
	
	
	}
