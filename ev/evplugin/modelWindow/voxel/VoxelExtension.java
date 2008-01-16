package evplugin.modelWindow.voxel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.media.opengl.*;
import javax.swing.*;

import org.jdom.Element;

import evplugin.basicWindow.ChannelCombo;
import evplugin.data.*;
import evplugin.ev.*;
import evplugin.filter.FilterSeq;
import evplugin.imageset.*;
import evplugin.imageset.Imageset.ChannelImages;
import evplugin.modelWindow.*;


//for now does not update if image updated. would need a data update CB but
//better to wait until data observer system is ready.

//http://www.java-tips.org/other-api-tips/jogl/vertex-buffer-objects-nehe-tutorial-jogl-port-2.html


/**
 * 
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

	
	private class Hook implements ModelWindowHook
		{
		private ModelWindow w;
		private JPanel totalPanel=new JPanel(new GridLayout(1,3));
		private StackSlices slices=new StackSlices();
		private StackSlices lastSlices=null; //to be removed

		private OneImageChannel icR=new OneImageChannel("R", Color.RED);
		private OneImageChannel icG=new OneImageChannel("G", Color.GREEN);
		private OneImageChannel icB=new OneImageChannel("B", Color.BLUE);
		
		
		public Hook(ModelWindow w)
			{
			this.w=w;
			
			totalPanel.add(icR);
			totalPanel.add(icG);
			totalPanel.add(icB);
			}
		public void adjustScale()
			{
			slices.adjustScale(w);
			}
		public Vector3D autoCenterMid()
			{
			return slices.autoCenterMid();
			}
		public Double autoCenterRadius(Vector3D mid, double FOV){return null;}
		public boolean canRender(EvObject ob){return false;}
		public void displayInit(GL gl){}
		public void displaySelect(GL gl){}
		public void readPersonalConfig(Element e){}
		public void savePersonalConfig(Element e){}
		public void select(int id){}
		public void fillModelWindomMenus()
			{
			w.bottompanelItems.add(totalPanel);
			}

	
		
		public double getFrame()
			{
			return this.w.frameControl.getFrame();
			}

		public void datachangedEvent()
			{
			Imageset im=w.metaCombo.getImageset();
			icR.channelCombo.setImageset(im);
			icG.channelCombo.setImageset(im);
			icB.channelCombo.setImageset(im);
			}
		
		public void displayFinal(GL gl)
			{
			//Remove prior data
			if(lastSlices!=null)
				lastSlices.clean(gl);
			lastSlices=null;
			
			//Build list of which channels should be rendered
			double frame=getFrame();
			if(slices.needBuild(frame))
				{
/*				Imageset curim=w.metaCombo.getImageset();
				if(icR.channelCombo.getImageset()!=curim)
					{
					icR.channelCombo.setImageset(curim);
					icG.channelCombo.setImageset(curim);
					icB.channelCombo.setImageset(curim);
					}*/
				
				HashMap<ChannelImages, StackSlices.ChannelSelection> chsel=new HashMap<ChannelImages, StackSlices.ChannelSelection>(); 
				for(OneImageChannel oc:new OneImageChannel[]{icR, icG, icB})
					{
					Imageset im=oc.channelCombo.getImageset();
					ChannelImages chim=im.getChannel(oc.channelCombo.getChannel());
					if(chim!=null)
						{
						StackSlices.ChannelSelection sel=chsel.get(chim);
						if(sel==null)
							{
							sel=new StackSlices.ChannelSelection();
							chsel.put(chim, sel);
							sel.im=im;
							sel.ch=chim;
							}
						sel.color=new Color(
								sel.color.getRed()+oc.color.getRed(),
								sel.color.getGreen()+oc.color.getGreen(),
								sel.color.getBlue()+oc.color.getBlue());
						}
					}
				slices.build(gl, frame, chsel.values());
				}
			
			//Render
			slices.render(gl,w.view.camera);
			}
		

	

		
		private class OneImageChannel extends JPanel implements ActionListener
			{
			static final long serialVersionUID=0;
			JButton bFs=new JButton(FilterSeq.getIconFilterSeq());
			ChannelCombo channelCombo=new ChannelCombo(new EmptyImageset(),true);
			Color color;
			
			public OneImageChannel(String name,Color color)
				{
				this.color=color;
				setLayout(new BorderLayout());
				add(new JLabel(name),BorderLayout.WEST);
				add(channelCombo,BorderLayout.CENTER);
				add(bFs,BorderLayout.EAST);
				channelCombo.addActionListener(this);
				}
			
			
			public void actionPerformed(ActionEvent e)
				{
				//TODO: when filter seq updated, a signal should be sent back
				lastSlices=slices;
				slices=new StackSlices();

				w.repaint();
				}
			
			
			}
		
		
		
		}
	
	}
