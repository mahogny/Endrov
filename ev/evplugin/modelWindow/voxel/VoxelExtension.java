package evplugin.modelWindow.voxel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import javax.media.opengl.*;
import javax.swing.*;

import org.jdom.Element;

import evplugin.basicWindow.ChannelCombo;
import evplugin.data.*;
import evplugin.ev.*;
import evplugin.filter.FilterSeq;
import evplugin.filter.WindowFilterSeq;
import evplugin.imageset.*;
import evplugin.imageset.Imageset.ChannelImages;
import evplugin.modelWindow.*;

//for now does not update if image updated. would need a data update CB but
//better to wait until data observer system is ready.

//http://www.java-tips.org/other-api-tips/jogl/vertex-buffer-objects-nehe-tutorial-jogl-port-2.html


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
		w.modelWindowHooks.add(new Hook(w)); //TODO. need be drawn last if voxels also will be in
		}


	
	





	/**
	 * Settings for one channel in an imageset
	 */
	public static class ChannelSelection
		{
		public Imageset im;
		public ChannelImages ch;
		public FilterSeq filterSeq;
		public Color color=new Color(0,0,0);
		}
	
	private class Hook implements ModelWindowHook, ActionListener
		{
		private ModelWindow w;
		private JPanel totalPanel=new JPanel(new GridLayout(1,3));
		private StackInterface slices=new Stack3D();
		private Vector<StackInterface> lastSlices=new Vector<StackInterface>();

		private OneImageChannel icR=new OneImageChannel("R", Color.RED);
		private OneImageChannel icG=new OneImageChannel("G", Color.GREEN);
		private OneImageChannel icB=new OneImageChannel("B", Color.BLUE);
		
		

		
		public JCheckBoxMenuItem miShowSelectedNucNames=new JCheckBoxMenuItem("Names: Show for selected");
	
		public JRadioButtonMenuItem miRender3dTexture=new JRadioButtonMenuItem("Use 3D texture",true);
		public JRadioButtonMenuItem miRender2dTexture=new JRadioButtonMenuItem("Use 2D texture",false);
		private ButtonGroup bgTexture=new ButtonGroup();
		
		public JMenuItem miShowSelectedNuc=new JMenuItem("Nuclei: Unhide selected"); 
	
		
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
			
			miRender2dTexture.addActionListener(this);
			miRender3dTexture.addActionListener(this);
			}
		
		
		public void actionPerformed(ActionEvent e)
			{
			icR.stackChanged();
			icG.stackChanged();
			icB.stackChanged();
			}



		
		
		public Collection<Double> adjustScale()
			{
			return slices.adjustScale(w);
			}
		public Collection<Vector3D> autoCenterMid()
			{
			return slices.autoCenterMid();
			}
		public Collection<Double> autoCenterRadius(Vector3D mid, double FOV){return Collections.emptySet();}
		public boolean canRender(EvObject ob){return false;}
		public void displayInit(GL gl){}
		public void displaySelect(GL gl){}
		public void readPersonalConfig(Element e){}
		public void savePersonalConfig(Element e){}
		public void select(int id){}
		public void fillModelWindomMenus()
			{
			w.bottomPanelItems.add(totalPanel);
			}

	
		
		public double getFrame()
			{
			return this.w.frameControl.getFrame();
			}

		public void datachangedEvent()
			{
			Imageset im=w.metaCombo.getImageset();
			icR.channelCombo.setExternalImageset(im);
			icG.channelCombo.setExternalImageset(im);
			icB.channelCombo.setExternalImageset(im);
			}
		
		
		
		
		
		public void displayFinal(GL gl)
			{
			//Remove prior data
			for(StackInterface s:lastSlices)
				s.clean(gl);
			lastSlices.clear();

			double frame=getFrame();
			
			//Build list of which channels should be rendered
			if(slices.needSettings(frame))
				{
//				System.out.println("needsettings");
				slices.setLastFrame(frame);
				
				//Build set of channels in Swing loop. Then there is no need to worry about strange GUI interaction
				HashMap<ChannelImages, ChannelSelection> chsel=new HashMap<ChannelImages, ChannelSelection>(); 
				for(OneImageChannel oc:new OneImageChannel[]{icR, icG, icB})
					{
					Imageset im=oc.channelCombo.getImageset();
					ChannelImages chim=im.getChannel(oc.channelCombo.getChannel());
					if(chim!=null)
						{
						ChannelSelection sel=chsel.get(chim);
						if(sel==null)
							{
							sel=new ChannelSelection();
							chsel.put(chim, sel);
							}
						sel.im=im;
						sel.ch=chim;
						sel.filterSeq=oc.filterSeq;
						sel.color=new Color(
								sel.color.getRed()+oc.color.getRed(),
								sel.color.getGreen()+oc.color.getGreen(),
								sel.color.getBlue()+oc.color.getBlue());
						}
					}
				
				//Start build thread
				slices.startBuildThread(frame, chsel, w);
				}
			else
				{
//				System.out.println("render");
				//Render
				slices.loadGL(gl);
				slices.render(gl,w.view.camera);
				}
			}
		

	

		/**
		 * GUI component to control channel settings
		 */
		private class OneImageChannel extends JPanel implements ActionListener
			{
			static final long serialVersionUID=0;
			JButton bFs=FilterSeq.createFilterSeqButton();
			ChannelCombo channelCombo=new ChannelCombo(new EmptyImageset(),true);
			Color color;
			FilterSeq filterSeq=new FilterSeq();
			
			public OneImageChannel(String name,Color color)
				{
				this.color=color;
				setLayout(new BorderLayout());
				add(new JLabel(name),BorderLayout.WEST);
				add(channelCombo,BorderLayout.CENTER);
				add(bFs,BorderLayout.EAST);
				channelCombo.addActionListener(this);
				bFs.addActionListener(this);
				filterSeq.observer.addWeakListener(filterSeqObserver);
				}
			
			private SimpleObserver.Listener filterSeqObserver=new SimpleObserver.Listener()
				{public void observerEvent(Object o){stackChanged();}};
			
			public void stackChanged()
				{
				//TODO: when filter seq updated, a signal should be sent back
				if(slices!=null)
					slices.stopBuildThread();
				lastSlices.add(slices);
				if(miRender3dTexture.isSelected()) //Preferably disk data should be cached
					slices=new Stack3D(); 
				else
					slices=new Stack2D();
//				System.out.println("stack changed");
				w.repaint();
				}
				
			public void actionPerformed(ActionEvent e)
				{
				if(e.getSource()==channelCombo)
					{
					stackChanged();
					}
				else if(e.getSource()==bFs)
					{
					new WindowFilterSeq(filterSeq);
					
					
					}
				}
			
			
			}
		
		
		
		}
	
	}
