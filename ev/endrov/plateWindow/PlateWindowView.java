package endrov.plateWindow;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.SwingUtilities;
import javax.vecmath.Vector3d;

import endrov.basicWindow.EvColor;
import endrov.data.EvPath;
import endrov.flowMeasure.ParticleMeasure;
import endrov.imageset.EvChannel;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;
import endrov.plateWindow.CalcAggregation.AggregationMethod;
import endrov.plateWindow.scene.Scene2DHistogram;
import endrov.plateWindow.scene.Scene2DImage;
import endrov.plateWindow.scene.Scene2DRect;
import endrov.plateWindow.scene.Scene2DScatter;
import endrov.plateWindow.scene.Scene2DText;
import endrov.plateWindow.scene.Scene2DText.Alignment;
import endrov.plateWindow.scene.Scene2DView;
import endrov.util.EvDecimal;
import endrov.util.Tuple;

/**
 * View for plates
 * 
 * @author Johan Henriksson
 *
 */
public class PlateWindowView extends Scene2DView implements MouseListener, MouseMotionListener, KeyListener, MouseWheelListener
	{
	private static final long serialVersionUID = 1L;
	
	
	/** Last coordinate of the mouse pointer. Used to detect dragging distance. */
	private int mouseLastDragX=0, mouseLastDragY=0;
	/** Last coordinate of the mouse pointer. Used to detect moving distance. For event technical reasons,
	 * this requires a separate set of variables than dragging (or so it seems) */
	public int mouseLastX=0, mouseLastY=0;
	/** Current mouse coordinate. Used for repainting. */
	public int mouseCurX=0, mouseCurY=0;
	/** Flag if the mouse cursor currently is in the window */
	public boolean mouseInWindow=false;

	
	
	
	

	

	public Map<EvPath, OneWell> wellMap=new HashMap<EvPath, OneWell>();
	
	
	private LinkedList<Grid> grids=new LinkedList<Grid>();
	
	/**
	 * How to display one well
	 */
	public static class OneWell
		{
//		public EvPath path;
		public int x, y;
		
		//image to show
		public EvStack stack;
		
		public Double aggrValue;
		
		public LinkedList<Double> arrA, arrB;

//		public int[] histogram;
		}
	

	/**
	 * One grid to show labels for
	 * 
	 */
	public static class Grid
		{
		int rows, cols;
		int x,y;
		public int distance;
		}
	

	
	public PlateWindowView()
		{
		//Attach listeners
		addKeyListener(this); 
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		}

	private int imageMargin=10;
	private int imageSize=100;

	
	private static class ValueRange
		{
		public Double min, max;
		
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
		}

	
	/**
	 * Take current settings of sliders and apply it to image
	 */
	public void layoutImagePanel()
		{
		//Update scene graph
		clear();
	
		Font gridFont=new Font("Arial", Font.PLAIN, 60);
		for(Grid g:grids)
			{
			for(int i=1;i<=g.cols;i++)
				{
				char c='A';
				Scene2DText st=new Scene2DText(g.x + (i-1)*(g.distance)+50, g.y + -20, ""+(char)(c+i-1));
				st.font = gridFont;
				st.alignment=Alignment.Center;
				addElem(st);
				}
			for(int i=1;i<=g.rows;i++)
				{
				Scene2DText st=new Scene2DText(g.x + -20, g.y + (i-1)*(g.distance)+50, ""+i);
				st.font=gridFont;
				st.alignment=Alignment.Right;
				addElem(st);
				}
			}
		
		
		//Calculate aggregate value
		ValueRange ragg=new ValueRange();
		ValueRange raggA=new ValueRange();
		ValueRange raggB=new ValueRange();
		if(!aggrMethod.equals(aggrHide) && pm!=null)
			{
			for(Map.Entry<EvPath, OneWell> e:wellMap.entrySet())
				{
				OneWell w=e.getValue();
				EvPath path=e.getKey();

				String pathString=path.toString();
				
				//Should probably add another level here(?)
				Map<Integer,ParticleMeasure.ParticleInfo> mapp=pm.getFrame(EvDecimal.ZERO);

				//Extract relevant particle values
				LinkedList<Double> listA=new LinkedList<Double>();
				LinkedList<Double> listB=new LinkedList<Double>();
				w.arrA=listA;
				w.arrB=listB;
				for(ParticleMeasure.ParticleInfo pi:mapp.values())
					{
					String src=pi.getString("source");
					if(src.equals(pathString))
						{
						listA.add(pi.getDouble(attr1));
						listB.add(pi.getDouble(attr2));
						}
					}

				if(aggrMethod instanceof CalcAggregation.AggregationMethod)
					{
					CalcAggregation.AggregationMethod am=(CalcAggregation.AggregationMethod)aggrMethod;
					w.aggrValue=am.calc(listA, listB);
					}
				else
					w.aggrValue=null;
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
			
			if(aggrMethod.equals(aggrHide))
				{
				//Show image
				Scene2DImage imp=new Scene2DImage();
				imp.setImage(w.stack, 0);
				imp.borderColor=EvColor.green;
				
				w.stack.cs.setMidBases(new Vector3d(w.x,w.y,0), new Vector3d[]{
						new Vector3d(1,0,0),
						new Vector3d(0,1,0),
						new Vector3d(0,0,1)
						});
				
				imp.loadImage(); //Should totally not be available here TODO
				
				addElem(imp);
				drawRect=false;
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
			else if(aggrMethod.equals(aggrHistogram))
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
					}
				}
			else if(aggrMethod.equals(aggrScatter))
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
		if(!newPaths.equals(lastPaths))
			{
			zoomToFit();
			lastPaths=newPaths;
			}


		
/*		
		
		

		
	
		EvChannel ch=cw.getChannel();
		
		
		//Imageset rec2=cw.comboChannel.getImageset();
		//String chname=cw.comboChannel.getChannelName();
		if(ch!=null)
		//if(rec2!=null && chname!=null)
			{
			//EvChannel ch=rec2.getChannel(chname);
			
			
			ImageLayoutView.ImagePanelImage pi=new ImageLayoutView.ImagePanelImage();
			pi.brightness=cw.getBrightness();//cw.sliderBrightness.getValue();
			pi.contrast=cw.getContrast();//Math.pow(2,cw.sliderContrast.getValue()/1000.0);
			pi.color=EvColor.white;
			
			EvDecimal frame=frameControl.getFrame();
			EvDecimal z=frameControl.getZ();
			frame=ch.closestFrame(frame);
			
			EvStack stack=ch.getStack(new ProgressHandle(), frame);
			
			//System.out.println("---- got stack "+stack);
			
			if(stack==null)
				pi.setImage(null,0);
			else
				{
				int closestZ=stack.closestZint(z.doubleValue());
				//System.out.println("----closest z: "+closestZ+"   depth:"+stack.getDepth());
				if(closestZ!=-1)
					{
					EvImage evim=stack.getInt(closestZ);
					//System.out.println("--- got stack 2: "+evim+"   "+evim.getPixels(null));
					
					if(evim!=null)
						pi.setImage(stack,closestZ);
					else
						{
						System.out.println("Image was null. ch:"+cw.getChannelName());
						}
					}
				else
					System.out.println("--z=-1 for ch:"+cw.getChannelName());
				}
			images.add(pi);
			}

		*/
		invalidateImages();
		repaintImagePanel();
		}


	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/** 
	 * Scale screen vector to world vector 
	 */
	/*
	public double scaleS2w(double s)
		{
		return scaleS2w(s);
		}
	*/
	/**
	 * Scale world to screen vector 
	 */
	/*
	public double scaleW2s(double w) 
		{
		return scaleW2s(w);
		}
*/

	
	//New functions, should replace the ones above at some point

	/** Transform world coordinate to screen coordinate */
	/*
	public Vector2d transformPointW2S(Vector2d u)
		{
		return super.transformPointW2S(u);
		}*/
		
	/** 
	 * Transform screen coordinate to world coordinate 
	 * NOTE: This means panning is not included! 
	 */
	/*
	public Vector2d transformPointS2W(Vector2d u)
		{
		return transformPointS2W(u);
		}
		*/

	/**
	 * Transform screen vector to world vector.
	 * NOTE: This means panning is not included! 
	 * 
	 */
	/*
	public Vector2d transformVectorS2W(Vector2d u)
		{
		return transformVectorS2W(u);
		}
*/
	
	/** Convert world to screen Z coordinate */
	/*
	public double w2sz(double z)
		{
		return z;
		}
	*/
	/** Convert world to screen Z coordinate */
	/*
	public double s2wz(double sz) 
		{
		return sz;
		} 
*/
	
	
	

	
	/**
	 * Callback: Key pressed down
	 */
	public void keyPressed(KeyEvent e)
		{
			
		}
	/**
	 * Callback: Key has been released
	 */
	public void keyReleased(KeyEvent e)
		{
		}
	/**
	 * Callback: Keyboard key typed (key down and up again)
	 */
	public void keyTyped(KeyEvent e)
		{
		/*
		if(KeyBinding.get(KEY_STEP_BACK).typed(e))
			frameControl.stepBack();
		else if(KeyBinding.get(KEY_STEP_FORWARD).typed(e))
			frameControl.stepForward();
		else if(KeyBinding.get(KEY_STEP_DOWN).typed(e))
			frameControl.stepDown();
		else if(KeyBinding.get(KEY_STEP_UP).typed(e))
			frameControl.stepUp();
			*/
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
//		int dx=e.getX()-mouseLastX;
//		int dy=e.getY()-mouseLastY;
		mouseLastX=e.getX();
		mouseLastY=e.getY();
		mouseInWindow=true;
		mouseCurX=e.getX();
		mouseCurY=e.getY();
		
		//Handle tool specific feedback
//		if(currentTool!=null)
//			currentTool.mouseMoved(e,dx,dy);
		
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
			{
			pan(dx,dy);
			repaintImagePanel();
			}

//		if(currentTool!=null)
	//		currentTool.mouseDragged(e,dx,dy);
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
	
	/**
	 * Update, but assume images are still ok
	 */
	public void repaintImagePanel()
		{				
		//Check if recenter needed
		boolean zoomToFit=false;
		
		//Show new image
		repaint();

		if(zoomToFit)
			{
			zoomToFit();
			}

//		updateWindowTitle();
		}

	
	public void zoomToFit()
		{
		super.zoomToFit();
		zoom(0.8);
		}


	
	private TreeSet<String> lastPaths=new TreeSet<String>();

	public void layoutWells()
		{
		TreeSet<String> wellNames=new TreeSet<String>();
		for(EvPath p:wellMap.keySet())
			wellNames.add(p.getLeafName());

		
		grids.clear();
		Grid g=isMultiwellFormat(wellNames);
		if(g!=null)
			grids.add(g);
		
		//Multi-well layout
		for(EvPath p:wellMap.keySet())
			{
			OneWell well=wellMap.get(p);
			Tuple<Integer,Integer> pos=parseWellPos(p.getLeafName());
			if(pos!=null)
				{
				well.y=(pos.fst()-1)*(imageSize+imageMargin);
				well.x=(pos.snd()-1)*(imageSize+imageMargin);
				}
			}
		
		}
		


	public void addWell(EvPath p, EvChannel evChannel)
		{
		if(p==null)
			throw new RuntimeException("null path");
			
		OneWell well=new OneWell();
		
		EvStack stack=well.stack=new EvStack();  //Displacement from here
		stack.allocate(1, 1, 1, EvPixelsType.DOUBLE);
		double[] v=stack.getInt(0).getPixels().getArrayDouble();
		v[0]=Math.random()*200;
		
		int maxdim=stack.getWidth();
		if(stack.getHeight()>maxdim)
			maxdim=stack.getHeight();
		stack.resX=stack.resY=stack.resZ=imageSize/(double)maxdim;

		wellMap.put(p, well);
		}

	
	
	public Tuple<Integer, Integer> parseWellPos(String n)
		{
		n=n.toUpperCase();
		int ac=0;
		while(ac<n.length() && Character.isLetter(n.charAt(ac)))
			ac++;
		String letterpart=n.substring(0,ac);
		String numberpart=n.substring(ac);
		while(ac<n.length() && Character.isDigit(n.charAt(ac)))
			ac++;
		if(ac!=n.length() || letterpart.isEmpty() || numberpart.isEmpty() || letterpart.length()!=1)
			return null;
		
		int num=Integer.parseInt(numberpart);
		int letter=letterpart.charAt(0)-'A'+1;
		return Tuple.make(num, letter);
		}
	

	public PlateWindowView.Grid isMultiwellFormat(Collection<String> wellNames)
		{
		int maxletter=0;
		int maxnum=0;
		
		//Does this follow a multi-well format?  LettersNumbers
		for(String n:wellNames)
			{
			Tuple<Integer,Integer> pos=parseWellPos(n);
			if(pos==null)
				return null;
			
			if(pos.fst()>maxnum)
				maxnum=pos.fst();
			if(pos.snd()>maxletter)
				maxletter=pos.snd();
			}
		
		PlateWindowView.Grid g=new PlateWindowView.Grid();
		g.cols=maxletter;
		g.rows=maxnum;
		g.distance=imageSize+imageMargin;
		return g;
		}


	public static final String aggrHide="Hide";
	public static final String aggrHistogram="Histogram";
	public static final String aggrScatter="Scatter";

	
	private Object aggrMethod=aggrHide;
	
	private String attr1, attr2;
	
	public void setAggrMethod(Object o, String attr1, String attr2)
		{
		aggrMethod=o;
		this.attr1=attr1;
		this.attr2=attr2;
//		layoutImagePanel();
//		repaint();
		}

	/**
	 * Get a list of all aggregation modes
	 */
	public static Object[] getAggrModes()
		{
		LinkedList<Object> list=new LinkedList<Object>();
		list.add(aggrHide);
		list.add(aggrHistogram);
		list.add(aggrScatter);
		for(AggregationMethod m:CalcAggregation.getAggregationMethods())
			list.add(m);
		return list.toArray(new Object[0]);
		}



	private ParticleMeasure pm=null;
	
	public void setPM(ParticleMeasure particleMeasure)
		{
		pm=particleMeasure;
		}


	
	}
