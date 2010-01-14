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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.Vector;

import javax.media.opengl.*;
import javax.swing.*;
import javax.vecmath.Vector3d;

import org.jdom.Element;

import endrov.data.*;
import endrov.imageset.*;
import endrov.modelWindow.*;
import endrov.modelWindow.ModelWindow.ProgressMeter;
import endrov.util.EvDecimal;

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
	public static void initPlugin() {}
	static
		{
		ModelWindow.modelWindowExtensions.add(new VoxelExtension());
		}

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
	 * Settings for one channel in an imageset
	 */
	public static class ChannelSelection
		{
		public Imageset im;
		public EvChannel ch;
		//public FilterSeq filterSeq;
		public Color color=new Color(0,0,0);
		}
	
	private class Hook implements ModelWindowHook, ActionListener
		{
		private ModelWindow w;
		private JPanel totalPanel=new JPanel(new GridLayout(1,3));
		//private TreeMap<EvDecimal, StackInterface> currentStack=new TreeMap<EvDecimal, StackInterface>();
		private StackInterface currentStack=null;
		private StackInterface loadingStack=null;
		private Vector<StackInterface> removableStacks=new Vector<StackInterface>();

		private OneImageChannel icR=new OneImageChannel("R", Color.RED);
		private OneImageChannel icG=new OneImageChannel("G", Color.GREEN);
		private OneImageChannel icB=new OneImageChannel("B", Color.BLUE);
	
		public JRadioButtonMenuItem miRender3dTexture=new JRadioButtonMenuItem("Use 3D texture",true);
		public JRadioButtonMenuItem miRender2dTexture=new JRadioButtonMenuItem("Use 2D texture",false);
		private ButtonGroup bgTexture=new ButtonGroup();
		public JCheckBoxMenuItem miSolidColor=new JCheckBoxMenuItem("Solid color");
		public JCheckBoxMenuItem miDrawEdge=new JCheckBoxMenuItem("Draw edge");
		public JCheckBoxMenuItem miMixColors=new JCheckBoxMenuItem("Mix colors");

		
		
		
		
		
		public Hook(ModelWindow w)
			{
			this.w=w;
			
			totalPanel.add(icR);
			totalPanel.add(icG);
			totalPanel.add(icB);
			
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
			else
				{
				//System.out.println("voxel repaint2");
				icR.stackChanged();
				icG.stackChanged();
				icB.stackChanged();
				}
			}



		public StackInterface getCurrentStack()
			{
			return currentStack;
			}
		
		
		public Collection<Double> adjustScale()
			{
			StackInterface s=getCurrentStack();
			if(s!=null)
				return s.adjustScale(w);
			else
				return Collections.emptySet();
			}
		public Collection<Vector3d> autoCenterMid()
			{
			StackInterface s=getCurrentStack();
			if(s!=null)
				return s.autoCenterMid();
			else
				return Collections.emptySet();
			}
		public Collection<Double> autoCenterRadius(Vector3d mid, double FOV)
			{
			StackInterface s=getCurrentStack();
			if(s!=null)
				{
				Double r=s.autoCenterRadius(mid, FOV);
				if(r!=null)
					return Collections.singleton(r);
				}
			return Collections.emptySet();
			}
		public boolean canRender(EvObject ob){return false;}
		public void displayInit(GL gl){}
		public void displaySelect(GL gl){}
		public void readPersonalConfig(Element e){}
		public void savePersonalConfig(Element e){}
		public void fillModelWindowMenus()
			{
			w.bottomPanelItems.add(totalPanel);
			}

	
		
		public EvDecimal getFrame()
			{
			return this.w.frameControl.getFrame();
			}

		public void datachangedEvent()
			{
			//System.out.println("voxel datachanged event");
			EvContainer data=w.getSelectedData();
			Imageset im=data instanceof Imageset ? (Imageset)data : new Imageset();
			
			icR.channelCombo.setRoot(im);
			icG.channelCombo.setRoot(im);
			icB.channelCombo.setRoot(im);
			icR.checkStackChanged();
			icG.checkStackChanged();
			icB.checkStackChanged();
			}
		
		
		
		
		public void displayFinal(GL gl,List<TransparentRender> transparentRenderers)
			{
			try
				{
				//Remove prior data
				for(StackInterface s:removableStacks)
					s.clean(gl);
				removableStacks.clear();

				//Swap to new stack whenever possible
				if(loadingStack!=null && loadingStack.newisReady)
					{
					if(currentStack!=null)
						removableStacks.add(currentStack);
					currentStack=loadingStack;
					loadingStack=null;
					currentStack.loadGL(gl);
					}
				
				//Check if a new stack has to be uploaded
				boolean needNewStack=currentStack==null;
				if(!needNewStack)
					{
					//TODO this can be improved a lot
					if(!currentStack.newlastFrame.equals(getFrame()))
						needNewStack=true;
					if(currentStack.outOfDate)
						needNewStack=true;
					}
				
				if(needNewStack)
					{
					//Abort prior loading
					if(loadingStack!=null)
						{
						loadingStack.stopCreate();
						loadingStack=null;
						}
					
					//Build set of channels in Swing loop. Then there is no need to worry about strange GUI interaction
					final HashMap<EvChannel, ChannelSelection> chsel=new HashMap<EvChannel, ChannelSelection>(); 
					for(OneImageChannel oc:new OneImageChannel[]{icR, icG, icB})
						{
						Imageset im=oc.channelCombo.getImagesetNotNull();
//					System.out.println("im: "+im);
//					System.out.println("im2: "+oc.channelCombo.getChannel());
						String channelName=oc.channelCombo.getChannel();
						if(channelName!=null)
							{
							EvChannel chim=im.getChannel(channelName);
							if(chim!=null)
								{
								ChannelSelection sel=chsel.get(chim);
								if(sel==null)
									chsel.put(chim, sel=new ChannelSelection());
								sel.im=im;
								sel.ch=chim;
								//sel.filterSeq=oc.filterSeq;
								sel.color=new Color(
										sel.color.getRed()+oc.color.getRed(),
										sel.color.getGreen()+oc.color.getGreen(),
										sel.color.getBlue()+oc.color.getBlue());
								}
							}
						}
					
					//Start build thread
					if(!chsel.isEmpty())
						{
						System.out.println("New stack ----- "+chsel.keySet());
						if(miRender3dTexture.isSelected()) 
							loadingStack=new Stack3D(); 
						else
							loadingStack=new Stack2D();
						loadingStack.newlastFrame=getFrame();
						final ProgressMeter pm=w.createProgressMeter();
						new Thread(){
						public void run()
							{
							pm.set(0);
							if(loadingStack.newCreate(pm, getFrame(), chsel, w))
								loadingStack.newisReady=true;
							pm.done();
							}
						}.run();
						}
					else
						{
						//Remove existing stack if all channels deselected
						loadingStack=null;
						if(currentStack!=null)
							removableStacks.add(currentStack);
						currentStack=null;
						}
					
					
					
					
					//TODO changing data does not trigger making new stack
						
					
					}
				
				//Render current stack
				if(currentStack!=null)
					currentStack.render(gl,transparentRenderers,w.view.camera,miSolidColor.isSelected(),miDrawEdge.isSelected(), miMixColors.isSelected());
				}
			catch (Exception e)
				{
				//This catches exceptions related to shaders
				e.printStackTrace();
				}
			}
		

	

		/**
		 * GUI component to control channel settings
		 */
		private class OneImageChannel extends JPanel implements ActionListener
			{
			static final long serialVersionUID=0;
			//private JButton bFs=FilterSeq.createFilterSeqButton();
			private EvComboChannel channelCombo=new EvComboChannel(null,true);
			private Color color;
			//private FilterSeq filterSeq=new FilterSeq();
			
			public OneImageChannel(String name,Color color)
				{
				this.color=color;
				setLayout(new BorderLayout());
				add(new JLabel(name),BorderLayout.WEST);
				add(channelCombo,BorderLayout.CENTER);
				//add(bFs,BorderLayout.EAST);
				channelCombo.addActionListener(this);
				//bFs.addActionListener(this);
				//filterSeq.observer.addWeakListener(filterSeqObserver);
				}
			
			/** Invoked when filter sequence changed */
			/*private SimpleObserver.Listener filterSeqObserver=new SimpleObserver.Listener()
				{public void observerEvent(Object o){stackChanged();}};*/
			
			public WeakReference<EvChannel> lastChannelImages=new WeakReference<EvChannel>(null);
			
			/** Update stack */ 
			public void stackChanged()
				{
				//TODO: when filter seq updated, a signal should be sent back
				if(loadingStack!=null)
					{
					loadingStack.stopCreate();
					loadingStack=null;
					}
				if(currentStack!=null)
					{
					currentStack.outOfDate=true;
					}
				
//				removableStacks.add(currentStack);
				System.out.println("new stack (changed)");
	
				//TODO good enough?
				
				
/*				if(miRender3dTexture.isSelected()) 
					currentStack=new Stack3D(); 
				else
					currentStack=new Stack2D();*/
//				lastImageset=new WeakReference<Imageset>(channelCombo.getImagesetNull());
//				lastChannel=channelCombo.getChannelNotNull();

				EvChannel images=channelCombo.getImagesetNotNull().getChannel(channelCombo.getChannel());
				lastChannelImages=new WeakReference<EvChannel>(images);
				
				System.out.println("voxel repaint");
				w.view.repaint();
				}
			
			/** Update stack if imageset or channel changed */
			public void checkStackChanged()
				{
/*				Imageset ims=lastImageset.get();
				String ch=lastChannel;
				Imageset newImageset=channelCombo.getImagesetNull();
				String newChannel=channelCombo.getChannelNotNull();*/
				String channelName=channelCombo.getChannel();
				if(channelName!=null)
					{
					EvChannel images=channelCombo.getImagesetNotNull().getChannel(channelName);
					if(images!=lastChannelImages.get())
	//				if(ims!=newImageset || !ch.equals(newChannel))
						stackChanged();
					}
				}
				
			public void actionPerformed(ActionEvent e)
				{
				if(e.getSource()==channelCombo)
					{
					stackChanged();
//					System.out.println("call stack change");
					}
				/*
				else if(e.getSource()==bFs)
					{
					new WindowFilterSeq(filterSeq);
					}*/
				}
			
			
			}
		
		
		public EvDecimal getFirstFrame(){return null;}
		public EvDecimal getLastFrame(){return null;}
		
		}
	
	}
