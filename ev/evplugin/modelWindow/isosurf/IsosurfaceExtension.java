package evplugin.modelWindow.isosurf;

import java.awt.*;
import java.awt.event.*;
import java.lang.ref.WeakReference;
import java.util.*;

import javax.media.opengl.*;
import javax.swing.*;
import javax.swing.event.*;

import org.jdom.Element;

import evplugin.basicWindow.BasicWindow;
import evplugin.basicWindow.ChannelCombo;
import evplugin.basicWindow.ColorCombo;
import evplugin.data.*;
import evplugin.ev.*;
import evplugin.imageset.*;
import evplugin.modelWindow.*;


//optimization: load images only once for multiple layers

//http://www.java-tips.org/other-api-tips/jogl/vertex-buffer-objects-nehe-tutorial-jogl-port-2.html

/**
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
		w.modelWindowHooks.add(new Hook(w)); //TODO. need be drawn last if voxels also will be in
		}

	
	private class Hook implements ModelWindowHook, ActionListener
		{
		private ModelWindow w;
		private Vector<ToolIsolayer> isolayers=new Vector<ToolIsolayer>();
		private JButton addIsolevel=new JButton("Add isolevel");

		public Hook(ModelWindow w)
			{
			this.w=w;
			addIsolevel.addActionListener(this);
			}
		
		private Collection<Isosurface> getOnedamnsurface()
			{
			Vector<Isosurface> iso=new Vector<Isosurface>();
			for(ToolIsolayer f:isolayers)
				if(!f.surfaces.isEmpty())
					{
					/*
					for(IsosurfaceRenderer r:f.surfaces.values().iterator().next())
						if(r.iso.isSurfaceValid())
							return r.iso;
					*/
					Vector<IsosurfaceRenderer> vr=f.surfaces.values().iterator().next();
					if(vr!=null)
						{
						IsosurfaceRenderer r=f.surfaces.values().iterator().next().iterator().next();
						if(r!=null && r.iso.isSurfaceValid())
							iso.add(r.iso);
						//				return r.iso;
						}
					}
			return iso;
//			return null;
			}
		
		public Collection<Double> adjustScale()
			{
			Collection<Isosurface> iso=getOnedamnsurface();
			Vector<Double> scale=new Vector<Double>();
			for(Isosurface s:iso)
				scale.addAll(s.adjustScale(w));
			return scale;
//			if(iso!=null)
	//			iso.adjustScale(w);
			}
		public Collection<Vector3D> autoCenterMid()
			{
			Collection<Isosurface> iso=getOnedamnsurface();
			Vector<Vector3D> scale=new Vector<Vector3D>();
			for(Isosurface s:iso)
				scale.add(s.autoCenterMid());
			return scale;
			/*
			Isosurface iso=getOnedamnsurface();
			if(iso!=null)
				return iso.autoCenterMid();
			else
				return null;*/
			}
		public Collection<Double> autoCenterRadius(Vector3D mid, double FOV)
			{
			Collection<Isosurface> iso=getOnedamnsurface();
			Vector<Double> scale=new Vector<Double>();
			for(Isosurface s:iso)
				scale.add(s.autoCenterRadius(mid, FOV));
			return scale;
			/*
			Isosurface iso=getOnedamnsurface();
			if(iso!=null)
				return iso.autoCenterRadius(mid, FOV);
			else
				return null;*/
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

		
		
		public void displayFinal(GL gl)
			{
			for(ToolIsolayer ti:isolayers)
				ti.render(gl);
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
				//JPanel q4=new JPanel(new GridLayout(1,3));
				
				setLayout(new GridLayout(4,1));
				setBorder(BorderFactory.createEtchedBorder());
				add(chanCombo);
				add(q2);
				add(q1);
				add(q3);
				//add(q4);
				
				transSpinner.addChangeListener(this);
				cutoffSpinner.addChangeListener(this);
				cutoff2Spinner.addChangeListener(this);
				numplaneSpinner.addChangeListener(this);
				blurxySpinner.addChangeListener(this);
				chanCombo.addActionListener(this);
				bDelete.addActionListener(this);
				}
			
			
			public void stateChanged(ChangeEvent e)
				{
				surfaces.clear(); //can be made more clever if performance is wanted
				w.repaint(); //TODO modw repaint
				}


			public void actionPerformed(ActionEvent e)
				{
				if(e.getSource()==bDelete)
					{
					isolayers.remove(this);
					w.updateToolPanels();
					}
				stateChanged(null);
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
			public void render(GL gl)
				{
				chanCombo.updateChannelList();
				
				//Make sure surfaces are for the right imageset
				Imageset im=chanCombo.getImageset();
				if(lastImageset.get()!=im)
					surfaces.clear();
				lastImageset=new WeakReference<Imageset>(im);
				
				
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
							r.add(new IsosurfaceRenderer(im,channelName,cframe,blursize,(float)cutoff));
						else if(numpl>1)
							{
							double cutoffdiff=Math.abs(cutoff-cutoff2)/numpl;
							double cutoffmin=Math.min(cutoff, cutoff2);
							for(int pl=0;pl<numpl;pl++)
								r.add(new IsosurfaceRenderer(im,channelName,cframe,blursize,(float)(cutoffmin+cutoffdiff*pl)));
							}
						
						}
					
					//Finally render
					Color col=colorCombo.getColor();
					double trans=(Double)transSpinner.getModel().getValue();
					for(IsosurfaceRenderer rr:r)
						rr.render(gl,col.getRed()/255.0f, col.getGreen()/255.0f, col.getBlue()/255.0f, (float)trans/100.0f);
					}
				}
			
			}
		
		
		}
	
	}
