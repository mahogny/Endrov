package evplugin.modelWindow.isosurf;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.lang.ref.WeakReference;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.List;

import javax.media.opengl.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import org.jdom.Element;

import com.sun.opengl.util.BufferUtil;

import evplugin.basicWindow.BasicWindow;
import evplugin.basicWindow.ChannelCombo;
import evplugin.basicWindow.ColorCombo;
import evplugin.data.*;
import evplugin.imageset.*;
import evplugin.modelWindow.*;


//optimization: load images only once for multiple layers

//http://www.java-tips.org/other-api-tips/jogl/vertex-buffer-objects-nehe-tutorial-jogl-port-2.html
//possible to generate isosurf on GPU, if not transparent

/**
 * Model window extension: isosurfaces
 * 
 * @author Johan Henriksson
 */
public class IsosurfaceExtension implements ModelWindowExtension
	{
	public static void initPlugin() {}
	static
		{
		ModelWindow.modelWindowExtensions.add(new IsosurfaceExtension());
		}
	
	public void newModelWindow(ModelWindow w)
		{
		w.modelWindowHooks.add(new Hook(w));
		}

	
	private class Hook implements ModelWindowHook, ActionListener
		{
		private final ModelWindow w;
		private Vector<ToolIsolayer> isolayers=new Vector<ToolIsolayer>();
		private Vector<IsosurfaceRenderer> removableRenderers=new Vector<IsosurfaceRenderer>();
		private JButton addIsolevel=new JButton("Add isolevel");

		public Hook(ModelWindow w)
			{
			this.w=w;
			addIsolevel.addActionListener(this);
			}
		
		private Collection<IsosurfaceRenderer> getSurfaces()
			{
			//Currently grabs for all frames. Later caching need to locate the current frame
			List<IsosurfaceRenderer> iso=new LinkedList<IsosurfaceRenderer>();
			for(ToolIsolayer f:isolayers)
				for(Vector<IsosurfaceRenderer> renderers:f.surfaces.values())
					for(IsosurfaceRenderer renderer:renderers)
						iso.add(renderer);
			return iso;
			}
		
		public Collection<Double> adjustScale()
			{
			List<Double> scale=new LinkedList<Double>();
			for(IsosurfaceRenderer s:getSurfaces())
				scale.addAll(s.adjustScale(w));
			return scale;
			}
		public Collection<Vector3d> autoCenterMid()
			{
			List<Vector3d> scale=new LinkedList<Vector3d>();
			for(IsosurfaceRenderer s:getSurfaces())
				scale.add(s.autoCenterMid());
			return scale;
			}
		public Collection<Double> autoCenterRadius(Vector3d mid, double FOV)
			{
			List<Double> scale=new LinkedList<Double>();
			for(IsosurfaceRenderer s:getSurfaces())
				scale.add(s.autoCenterRadius(mid, FOV));
			return scale;
			}
		public boolean canRender(EvObject ob){return false;}
		public void displayInit(GL gl){}
		public void displaySelect(GL gl){}
		public void readPersonalConfig(Element e){}
		public void savePersonalConfig(Element e){}
		public void select(int id){}
		public void datachangedEvent(){}
		public void fillModelWindomMenus()
			{
			w.sidePanelItems.add(addIsolevel);
			for(ToolIsolayer ti:isolayers)
				w.sidePanelItems.add(ti);
			}

		
		
		public void actionPerformed(ActionEvent e)
			{
			isolayers.add(new ToolIsolayer());
			w.updateToolPanels();
			}
		
		
		private double getFrame()
			{
			return this.w.frameControl.getFrame();
			}

		
		
		public void displayFinal(GL gl,List<TransparentRender> transparentRenderers)
			{
			for(ToolIsolayer ti:isolayers)
				ti.render(gl, transparentRenderers);
			}
		
		
		
		private class ToolIsolayer extends JPanel implements ChangeListener, ActionListener
			{
			static final long serialVersionUID=0;
			private JSpinner transSpinner=new JSpinner(new SpinnerNumberModel((double)100.0,(double)0.0,(double)100.0,(double)5.0));
			private JSpinner cutoffSpinner=new JSpinner(new SpinnerNumberModel((double)50.0,(double)0.0,(double)100.0,(double)5.0));
			private JSpinner cutoff2Spinner=new JSpinner(new SpinnerNumberModel((double)50.0,(double)0.0,(double)100.0,(double)5.0));
			private JSpinner numplaneSpinner=new JSpinner(new SpinnerNumberModel((int)1,(int)0,(int)99,(int)1));
			private JSpinner blurxySpinner=new JSpinner(new SpinnerNumberModel((int)1.0,(int)0.0,(int)10.0,(int)1));
			private ChannelCombo chanCombo=new ChannelCombo(null,true);
			private JButton bDelete=new JButton(BasicWindow.getIconDelete());
			private ColorCombo colorCombo=new ColorCombo();
			private WeakReference<Imageset> lastImageset=new WeakReference<Imageset>(null);
			private HashMap<Integer,Vector<IsosurfaceRenderer>> surfaces=new HashMap<Integer,Vector<IsosurfaceRenderer>>(); 
			
			public ToolIsolayer()
				{
				JPanel q2=new JPanel(new GridLayout(1,2));
				q2.add(withLabel("#Pl:",numplaneSpinner));
				q2.add(withLabel("Cut-off2:",cutoff2Spinner));
				JPanel q1=new JPanel(new GridLayout(1,2));
				q1.add(withLabel("Trans:",transSpinner));
				q1.add(withLabel("Cut-off:",cutoffSpinner));
				JPanel q3in=new JPanel(new GridLayout(1,2));
				JPanel q3=new JPanel(new BorderLayout());
				q3in.add(withLabel("BlurX:",blurxySpinner));
				q3in.add(colorCombo);
				q3.add(q3in,BorderLayout.CENTER);
				q3.add(bDelete,BorderLayout.EAST);
				
				setLayout(new GridLayout(4,1));
				setBorder(BorderFactory.createEtchedBorder());
				add(chanCombo);
				add(q2);
				add(q1);
				add(q3);
				
				transSpinner.addChangeListener(this);
				cutoffSpinner.addChangeListener(this);
				cutoff2Spinner.addChangeListener(this);
				numplaneSpinner.addChangeListener(this);
				blurxySpinner.addChangeListener(this);
				chanCombo.addActionListener(this);
				bDelete.addActionListener(this);
				colorCombo.addActionListener(this);
				}
			
			
			public void stateChanged(ChangeEvent e)
				{
				if(e.getSource()!=transSpinner)
					surfaces.clear(); //can be made more clever if performance is wanted
				w.view.repaint(); //TODO modw repaint
				}


			public void actionPerformed(ActionEvent e)
				{
				if(e.getSource()==colorCombo)
					w.view.repaint();
				else
					{
					if(e.getSource()==bDelete)
						{
						for(Vector<IsosurfaceRenderer> renderers:surfaces.values())
							for(IsosurfaceRenderer renderer:renderers)
								removableRenderers.add(renderer);
						isolayers.remove(this);
						w.updateToolPanels();
						}
					surfaces.clear(); //can be made more clever if performance is wanted
					w.view.repaint(); //TODO modw repaint
					}
				}




			/**
			 * Embed control with a label
			 */
			private JComponent withLabel(String text, JComponent right)
				{
				JPanel p=new JPanel(new BorderLayout());
				p.add(new JLabel(text),BorderLayout.WEST);
				p.add(right,BorderLayout.CENTER);
				return p;
				}
			
			/**
			 * Render according to these controls. Create surfaces as needed.
			 */
			public void render(GL gl,List<TransparentRender> transparentRenderers)
				{
				chanCombo.updateChannelList();
				
				synchronized(surfaces)
				{
				
				//Clean up resources
				for(IsosurfaceRenderer renderer:removableRenderers)
					renderer.clean(gl);
				removableRenderers.clear();
				
				//Make sure surfaces are for the right imageset
				Imageset im=chanCombo.getImageset();
				if(lastImageset.get()!=im)
					surfaces.clear();
				lastImageset=new WeakReference<Imageset>(im);
				
				//Get channel
				String channelName=chanCombo.getChannel();
				Imageset.ChannelImages ch=im.channelImages.get(channelName);
				if(ch!=null)
					{
					int cframe=ch.closestFrame((int)getFrame());

					//Create surface if it wasn't there before
					Vector<IsosurfaceRenderer> r=surfaces.get(cframe);
					if(r==null)
						{
						int blursize=(Integer)blurxySpinner.getModel().getValue();
						double cutoff=((Double)cutoffSpinner.getModel().getValue())*255.0/100.0;
						double cutoff2=((Double)cutoff2Spinner.getModel().getValue())*255.0/100.0;
						r=new Vector<IsosurfaceRenderer>();
						surfaces.put(cframe, r);
						int numpl=(Integer)numplaneSpinner.getModel().getValue();
						if(numpl==1)
							{
							GenerateIsosurface gi=new GenerateIsosurface(im,channelName,cframe,blursize,(float)cutoff,w);
							generators.add(gi);
							gi.start();
							}
						else if(numpl>1)
							{
							double cutoffdiff=Math.abs(cutoff-cutoff2)/numpl;
							double cutoffmin=Math.min(cutoff, cutoff2);
							for(int pl=0;pl<numpl;pl++)
								{
								GenerateIsosurface gi=new GenerateIsosurface(im,channelName,cframe,blursize,(float)(cutoffmin+cutoffdiff*pl),w);
								generators.add(gi);
								gi.start();
								}
							}
						
						}
					
					//Render available surfaces
					Color col=colorCombo.getColor();
					double trans=(Double)transSpinner.getModel().getValue();
					for(IsosurfaceRenderer rr:r)
						rr.render(gl,transparentRenderers,w.view.camera,col.getRed()/255.0f, col.getGreen()/255.0f, col.getBlue()/255.0f, (float)trans/100.0f);
					}
				}
				}
			
			
			
			
			public Vector<GenerateIsosurface> generators=new Vector<GenerateIsosurface>();
			
			public class GenerateIsosurface extends Thread
				{
				private boolean stopFlag=false;
				
				private final Imageset im;
				private final String channelName;
				private final int cframe;
				private final int blursize;
				private final float cutoff;
				ModelWindow.ProgressMeter pm;
				
				private Isosurface iso=new Isosurface();
				private FloatBuffer vertb;
				private FloatBuffer vertn;
				private IntBuffer indb;

				final int totalPartLoading=500;
				final int totalPartConvertLists=totalPartLoading+100;

				
				public GenerateIsosurface(Imageset im, String channelName, int cframe, int blursize, float cutoff, ModelWindow mw)
					{
					this.im=im;
					this.channelName=channelName;
					this.cframe=cframe;
					this.blursize=blursize;
					this.cutoff=cutoff;
					pm=mw.createProgressMeter();
					}
				
				/**
				 * Running thread
				 */
				public void run()
					{
					//Prepare to blur XY
					ConvolveOp simpleBlur=null;
					if(blursize!=0)
						{
						int blurarrsize=(1+2*blursize)*(1+2*blursize);
						float weight = 1.0f/(float)blurarrsize;
						float[] elements = new float[blurarrsize]; 
						for (int i = 0; i < elements.length; i++) 
							elements[i] = weight;
						Kernel myKernel = new Kernel(blursize*2+1, blursize*2+1, elements);
						simpleBlur = new ConvolveOp(myKernel);
						}

					int pixelsW=0,pixelsH=0,pixelsD=0;
					float realw=0,realh=0,reald=0; //TODO: should be able to have different distances

					float ptScalarField[]=null;
					if(im.channelImages.containsKey(channelName) &&
							im.channelImages.get(channelName).imageLoader.containsKey(cframe))
						{
						double resZ=im.meta.resZ;

						
						TreeMap<Integer,EvImage> slices=im.channelImages.get(channelName).imageLoader.get(cframe);
						final int numSlices=slices.size();
						int curslice=0;
						if(slices!=null)
							for(final int i:slices.keySet())
								{
								if(shouldStop()) return;
								pm.set(totalPartLoading*i/numSlices);

								EvImage evim=slices.get(i);
								BufferedImage bim=evim.getJavaImage();

								//Blur the image
								if(simpleBlur!=null)
									{
									BufferedImage bufo=new BufferedImage(bim.getWidth(), bim.getHeight(), bim.getType());
									simpleBlur.filter(bim, bufo);
									bim=bufo;
									}

								if(ptScalarField==null)
									{
									pixelsW=bim.getWidth();
									pixelsH=bim.getHeight();
									pixelsD=slices.size();
									realw=(float)bim.getWidth()/(float)(evim.getResX()/evim.getBinning());
									realh=(float)bim.getHeight()/(float)(evim.getResY()/evim.getBinning());
									reald=(float)pixelsD/(float)resZ;
									ptScalarField=new float[pixelsW*pixelsH*pixelsD];
									System.out.println("alloc "+pixelsW+" "+pixelsH+" "+pixelsD);
									}

								float[] pixels=new float[bim.getWidth()];
								for(int y=0;y<bim.getHeight();y++)
									{
									bim.getRaster().getPixels(0, y, bim.getWidth(), 1, pixels);
									for(int x=0;x<bim.getWidth();x++)
										ptScalarField[curslice*pixelsW*pixelsH+y*pixelsW+x]=pixels[x];

									}
								curslice++;
								}
						}
					
					if(shouldStop()) return;

					//Generate polygons
					if(ptScalarField!=null)
						{
						//Smoothen Z?
						//Generate surface
						//TODO threads for surf generation
						//TODO progress
						iso.generateSurface(ptScalarField, cutoff, pixelsW-1, pixelsH-1, pixelsD-1, realw/pixelsW, realh/pixelsH, reald/pixelsD);
						if(shouldStop()) return;

						Vector3f[] vertices=iso.getVertices();
						Vector3f[] normals=iso.getNormals();
						int[] indices=iso.getIndices();
						if(vertices.length>0 && indices.length>0 && normals.length>0)
							{
							vertb=BufferUtil.newFloatBuffer(vertices.length*3);
							for(int i=0;i<vertices.length;i++)
								{
								vertb.put(vertices[i].x);
								vertb.put(vertices[i].y);
								vertb.put(vertices[i].z);
								}
							if(shouldStop()) return;
							vertn=BufferUtil.newFloatBuffer(normals.length*3);
							for(int i=0;i<normals.length;i++)
								{
								vertn.put(normals[i].x);
								vertn.put(normals[i].y);
								vertn.put(normals[i].z);
								}
							if(shouldStop()) return;
							indb=BufferUtil.newIntBuffer(indices.length); 
							for(int i:indices)
								indb.put(i);
							}
						}
					
					if(shouldStop()) return;
					pm.set(totalPartConvertLists);
					
					//If everything went ok, create renderer
					if(iso.isSurfaceValid())
						{
						IsosurfaceRenderer renderer=new IsosurfaceRenderer();
						renderer.uploadData(w.view, vertb, vertn, indb);
				
						
						
						iso.updateScale();
						renderer.maxX=iso.maxX;
						renderer.maxY=iso.maxY;
						renderer.maxZ=iso.maxZ;
						renderer.minX=iso.minX;
						renderer.minY=iso.minY;
						renderer.minZ=iso.minZ;
						
						//Add to list of renderers
						synchronized(surfaces)
							{
							Vector<IsosurfaceRenderer> r=surfaces.get(cframe);
							r.add(renderer);
							}
						}
					
					
					System.out.println("done");
					eliminate();
					}

				public boolean shouldStop()
					{
					if(stopFlag)
						{
						eliminate();
						return true;
						}
					else
						return false;
					}
				public void eliminate()
					{
					generators.remove(this);
					pm.done();
					}
				public void stopGenerate()
					{
					stopFlag=true;
					}
				}
		
			
			}
		
		
		
		}
	
	}
