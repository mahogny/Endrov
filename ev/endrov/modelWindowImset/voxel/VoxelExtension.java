/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.modelWindowImset.voxel;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.Vector;

import javax.media.opengl.*;
import javax.swing.*;
import javax.vecmath.Vector3d;

import org.jdom.Element;

import endrov.basicWindow.EvComboColor;
import endrov.basicWindow.icon.BasicIcon;
import endrov.data.*;
import endrov.imageset.*;
import endrov.modelWindow.*;
import endrov.modelWindow.ModelWindow.ProgressMeter;
import endrov.modelWindowImset.voxel.StackRendererInterface.ChanProp;
import endrov.util.EvDecimal;
import endrov.util.EvSwingUtil;
import endrov.util.ProgressHandle;
import endrov.util.SnapBackSlider;

//for now does not update if image updated. would need a data update CB but
//better to wait until data observer system is ready.

//http://www.java-tips.org/other-api-tips/jogl/vertex-buffer-objects-nehe-tutorial-jogl-port-2.html

//Preferably disk data should be cached

/**
 * 3D stack renderers 
 * @author Johan Henriksson
 */
public class VoxelExtension implements ModelWindowExtension
	{

	public void newModelWindow(ModelWindow w)
		{
		//TODO Optimally only add if shaders are supported
		w.modelWindowHooks.add(new Hook(w)); 
		}

	/**
	 * Assuming set is non-empty, get the closest match
	 */
	public static EvDecimal getClosestFrame(SortedSet<EvDecimal> frames, EvDecimal frame)
		{
		if(frames.contains(frame))
			return frame;
		else
			{
			SortedSet<EvDecimal> before=frames.headSet(frame);
			SortedSet<EvDecimal> after=frames.tailSet(frame);
			if(before.isEmpty())
				return frames.first();
			else if(after.isEmpty())
				return frames.last();
			else
				{
				EvDecimal afterkey=after.first();
				EvDecimal beforekey=before.last();

				if(afterkey.subtract(frame).less(frame.subtract(beforekey)))
					return afterkey;
				else
					return beforekey;
				}
			}
		
		
		}

	/**
	 * Instance for one model window
	 * @author Johan Henriksson
	 *
	 */
	private class Hook implements ModelWindowHook, ActionListener
		{
		private ModelWindow w;
		private Vector<ToolIsolayer> isolayers=new Vector<ToolIsolayer>();
		//private JPanel totalPanel=new JPanel(new GridLayout(1,3));
		private StackRendererInterface currentStackRenderer=null;
		private StackRendererInterface loadingStackRenderer=null;
		private Vector<StackRendererInterface> removableStacks=new Vector<StackRendererInterface>();
		private JButton addIsolevel=new JButton("Add volume");

		/*
		private OneImageChannel icR=new OneImageChannel("R", Color.RED);
		private OneImageChannel icG=new OneImageChannel("G", Color.GREEN);
		private OneImageChannel icB=new OneImageChannel("B", Color.BLUE);*/
	
		public JRadioButtonMenuItem miRender3dTexture=new JRadioButtonMenuItem("Use 3D texture",true);
		public JRadioButtonMenuItem miRender2dTexture=new JRadioButtonMenuItem("Use 2D texture",false);
		private ButtonGroup bgTexture=new ButtonGroup();
		public JCheckBoxMenuItem miSolidColor=new JCheckBoxMenuItem("Solid color");
		public JCheckBoxMenuItem miDrawEdge=new JCheckBoxMenuItem("Draw edge");
		public JCheckBoxMenuItem miMixColors=new JCheckBoxMenuItem("Mix colors");

		
		
		
		
		
		public Hook(ModelWindow w)
			{
			this.w=w;
			addIsolevel.addActionListener(this);
			/*
			totalPanel.add(icR);
			totalPanel.add(icG);
			totalPanel.add(icB);
			*/
			
			bgTexture.add(miRender3dTexture);
			bgTexture.add(miRender2dTexture);
			
			JMenu miVoxels=new JMenu("Voxels");
			w.menuModel.add(miVoxels);
		
			miVoxels.add(miRender2dTexture);
			miVoxels.add(miRender3dTexture);
			miVoxels.addSeparator();
			miVoxels.add(miSolidColor);
			miVoxels.add(miDrawEdge);
			miVoxels.add(miMixColors);
			
			
			miRender2dTexture.addActionListener(this);
			miRender3dTexture.addActionListener(this);
			miSolidColor.addActionListener(this);
			miDrawEdge.addActionListener(this);
			miMixColors.addActionListener(this);
			}
		
		
		public void actionPerformed(ActionEvent e)
			{
			if(e.getSource()==miDrawEdge || e.getSource()==miMixColors)
				w.view.repaint();
			else if(e.getSource()==addIsolevel)
				{
				isolayers.add(new ToolIsolayer());
				w.updateToolPanels();
				}
			/*
			else
				{
				//System.out.println("voxel repaint2");
				icR.stackChanged();
				icG.stackChanged();
				icB.stackChanged();
				}*/
			}



		public StackRendererInterface getCurrentStack()
			{
			return currentStackRenderer;
			}
		

		public Collection<Double> adjustScale()
			{
			StackRendererInterface s=getCurrentStack();
			if(s!=null)
				return s.adjustScale(w);
			else
				return Collections.emptySet();
			}	

		public Collection<Vector3d> autoCenterMid()
			{
			StackRendererInterface s=getCurrentStack();
			if(s!=null)
				return s.autoCenterMid();
			else
				return Collections.emptySet();
			}
		public double autoCenterRadius(Vector3d mid)
			{
			StackRendererInterface s=getCurrentStack();
			if(s!=null)
				{
				double r=s.autoCenterRadius(mid);
				return r;
				}
			return 0;
			}
		public boolean canRender(EvObject ob){return false;}
		
		public void initOpenGL(GL gl)
			{
			}
		
		public void displayInit(GL gl){}
		public void displaySelect(GL gl){}
		public void readPersonalConfig(Element e){}
		public void savePersonalConfig(Element e){}
		public void fillModelWindowMenus()
			{
//			w.bottomPanelItems.add(totalPanel);
			w.sidePanelItems.add(addIsolevel);
			for(ToolIsolayer ti:isolayers)
				w.sidePanelItems.add(ti);
			}

	
		
		public EvDecimal getFrame()
			{
			return this.w.getFrame();
			}

		public void datachangedEvent()
			{
			for(ToolIsolayer ti:isolayers)
				ti.datachangedEvent();
			/*
			//System.out.println("voxel datachanged event");
			EvContainer data=w.getSelectedData();
			Imageset im=data instanceof Imageset ? (Imageset)data : new Imageset();

			
			icR.channelCombo.setRoot(im);
			icG.channelCombo.setRoot(im);
			icB.channelCombo.setRoot(im);
			icR.checkStackChanged();
			icG.checkStackChanged();
			icB.checkStackChanged();
			*/
			}
		
		
		
		
		public void displayFinal(GL gl,List<TransparentRender> transparentRenderers)
			{
			try
				{
				//Remove prior data
				for(StackRendererInterface s:removableStacks)
					s.clean(gl);
				removableStacks.clear();

				//Swap to new stack whenever possible
				if(loadingStackRenderer!=null && loadingStackRenderer.newisReady)
					{
					if(currentStackRenderer!=null)
						removableStacks.add(currentStackRenderer);
					currentStackRenderer=loadingStackRenderer;
					loadingStackRenderer=null;
					currentStackRenderer.loadGL(gl);
					}
				
				//Check if a new stack has to be uploaded
				boolean needNewStack=currentStackRenderer==null;
				if(!needNewStack)
					{
					//TODO this can be improved a lot
					if(!currentStackRenderer.newlastFrame.equals(getFrame()))
						needNewStack=true;
					if(currentStackRenderer.outOfDate)
						needNewStack=true;
					}
				
				if(needNewStack)
					{
					//Abort prior loading
					if(loadingStackRenderer!=null)
						{
						loadingStackRenderer.stopCreate();
						loadingStackRenderer=null;
						}

					//Build set of channels in Swing loop. Then there is no need to worry about strange GUI interaction
					final List<StackRendererInterface.ChannelSelection> chsel=new ArrayList<StackRendererInterface.ChannelSelection>(); 
					for(ToolIsolayer oc:isolayers)
						{
						/*Imageset im=oc.channelCombo.getImagesetNotNull();
						String channelName=oc.channelCombo.getChannelName();
						if(channelName!=null)
							{*/
							EvChannel chim=oc.channelCombo.getSelectedObject();//im.getChannel(channelName);
							if(chim!=null)
								{
								StackRendererInterface.ChannelSelection sel=new StackRendererInterface.ChannelSelection();
								chsel.add(sel);
								//sel.im=oc.channelCombo.getImageset();//im;
								sel.ch=chim;
								sel.prop=oc.prop;
								}
							//}
						}

					//Start build thread
					if(!chsel.isEmpty())
						{
						System.out.println(chsel);
						System.out.println(chsel);
						System.out.println(chsel);
						
						System.out.println("New stack to build ----- "+chsel);
						if(miRender3dTexture.isSelected()) 
							loadingStackRenderer=new Stack3D(); 
						else
							loadingStackRenderer=new Stack2D();
						loadingStackRenderer.newlastFrame=getFrame();
						final ProgressMeter pm=w.createProgressMeter();
						new Thread(){
						public void run()
							{
							pm.set(0);
							ProgressHandle progh=new ProgressHandle(); //TODO expose to the user!
							if(loadingStackRenderer.newCreate(progh, pm, getFrame(), chsel, w))
								loadingStackRenderer.newisReady=true;
							pm.done();
							}
						}.run();
						}
					else
						{
						//Remove existing stack if all channels deselected
						loadingStackRenderer=null;
						if(currentStackRenderer!=null)
							removableStacks.add(currentStackRenderer);
						currentStackRenderer=null;
						}
					
					
					
					
					//TODO changing data does not trigger making new stack
						
					
					}
				
				//Render current stack
				if(currentStackRenderer!=null)
					currentStackRenderer.render(gl,transparentRenderers,w.view.camera,miSolidColor.isSelected(),miDrawEdge.isSelected(), miMixColors.isSelected());
				}
			catch (Exception e)
				{
				//This catches exceptions related to shaders
				e.printStackTrace();
				}
			}
		


		
		public EvDecimal getFirstFrame(){return null;}
		public EvDecimal getLastFrame(){return null;}

		
		public void markOutOfDate()
			{
			if(currentStackRenderer!=null)
				currentStackRenderer.outOfDate=true;
			}

		private class ToolIsolayer extends JPanel implements /*ChangeListener, */ActionListener, SnapBackSlider.SnapChangeListener
			{
			public ChanProp prop=new ChanProp();
			
			static final long serialVersionUID=0;
			private EvComboChannel channelCombo=new EvComboChannel(true,true);
			private JButton bDelete=BasicIcon.getButtonDelete();
			private EvComboColor colorCombo=new EvComboColor(false);
//			private WeakReference<Imageset> lastImageset=new WeakReference<Imageset>(null);
			
			public WeakReference<EvChannel> lastChannelImages=new WeakReference<EvChannel>(null);

			private SnapBackSlider sliderContrast=new SnapBackSlider(SnapBackSlider.HORIZONTAL, -1000, 1000);
			private SnapBackSlider sliderBrightness=new SnapBackSlider(SnapBackSlider.HORIZONTAL, -1000, 1000);
			
//			private double contrast=1;
//			private double brightness=0;
			
//			private HashMap<EvDecimal,Vector<IsosurfaceRenderer>> surfaces=new HashMap<EvDecimal,Vector<IsosurfaceRenderer>>(); 
			
			public ToolIsolayer()
				{				
				JPanel q3in=new JPanel(new GridLayout(4,1));
				JPanel q3=new JPanel(new BorderLayout());
				q3in.add(channelCombo);
				q3in.add(EvSwingUtil.withLabel("C: ", sliderContrast));
				q3in.add(EvSwingUtil.withLabel("B: ", sliderBrightness));
				q3in.add(colorCombo);
				q3.add(q3in,BorderLayout.CENTER);
				q3.add(bDelete,BorderLayout.EAST);
				
				setLayout(new GridLayout(1,1));
				setBorder(BorderFactory.createEtchedBorder());
				add(q3);
/*				add(q2);
				add(q1);
				add(q3);*/
				
				channelCombo.addActionListener(this);
				bDelete.addActionListener(this);
				colorCombo.addActionListener(this);
				
				sliderContrast.addSnapListener(this);
				sliderBrightness.addSnapListener(this);
				
				//Initial property values
				prop.color=colorCombo.getColor();
				prop.contrast=1;
				prop.brightness=0;
				}
			
			
			public void datachangedEvent()
				{
				channelCombo.updateList();
				checkStackChanged();
				}

			private void stopLoadingStack()
				{
				if(loadingStackRenderer!=null)
					{
					loadingStackRenderer.stopCreate();
					loadingStackRenderer=null;
					}
				}
			
			public void stackChanged()
				{
				stopLoadingStack();
				markOutOfDate();
				
				System.out.println("new stack (changed)");
	
				EvChannel ch=channelCombo.getSelectedObject();//channelCombo.getImagesetNotNull().getChannel(channelCombo.getChannelName());
				lastChannelImages=new WeakReference<EvChannel>(ch);
				
				System.out.println("voxel repaint");
				w.view.repaint();
				}
			
			public void checkStackChanged()
				{
				String channelName=channelCombo.getChannelName();
				if(channelName!=null)
					{
					EvChannel ch=channelCombo.getSelectedObject();//getImagesetNotNull().getChannel(channelName);
					if(ch!=lastChannelImages.get())
						stackChanged();
					}
				else if(channelName==null)
					{
					if(lastChannelImages.get()!=null)
						stackChanged();
					}
				}


			public void actionPerformed(ActionEvent e)
				{
				if(e.getSource()==colorCombo)
					{
					prop.color=colorCombo.getColor();
					w.view.repaint();
					}
				else if(e.getSource()==bDelete)
					{
					stopLoadingStack();
					markOutOfDate();
					isolayers.remove(this);
					w.updateToolPanels();
					}
				else if(e.getSource()==channelCombo)
					{
					w.view.repaint(); //TODO modw repaint
					}
				}


			public void slideChange(SnapBackSlider source, int change)
				{
				if(source==sliderBrightness)
					{
					prop.brightness+=prop.contrast*change/10000.0;
					}
				else if(source==sliderContrast)
					{
					prop.contrast*=Math.exp(change/1000.0);
					}
				w.view.repaint();
				}

			
			}
		
		}

	
	
	
	
	
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		ModelWindow.modelWindowExtensions.add(new VoxelExtension());
		}

	}
