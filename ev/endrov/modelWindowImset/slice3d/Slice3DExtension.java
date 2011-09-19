/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.modelWindowImset.slice3d;

import java.awt.*;
import java.awt.event.*;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.List;

import javax.media.opengl.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.vecmath.Vector3d;

import org.jdom.Element;

import endrov.basicWindow.EvComboColor;
import endrov.basicWindow.icon.BasicIcon;
import endrov.data.*;
import endrov.imageset.*;
import endrov.modelWindow.*;
import endrov.util.EvDecimal;
import endrov.util.ProgressHandle;


/**
 * 
 * @author Johan Henriksson
 */
public class Slice3DExtension implements ModelWindowExtension
	{
	
	public void newModelWindow(ModelWindow w)
		{
		w.modelWindowHooks.add(new Hook(w)); 
		}

	
	private class Hook implements ModelWindowHook, ActionListener
		{
		private ModelWindow w;
		private Vector<ToolIsolayer> isolayers=new Vector<ToolIsolayer>();
		private JButton addIsolevel=new JButton("Add slice");

		public Hook(ModelWindow w)
			{
			this.w=w;
			addIsolevel.addActionListener(this);
			}
		
		
 		
		public Collection<Double> adjustScale()
			{
			List<Double> col=new LinkedList<Double>();
			for(ToolIsolayer ti:isolayers)
				col.addAll(ti.slice.adjustScale());
			return col;
			}
		public Collection<Vector3d> autoCenterMid()
			{
			return Collections.emptySet();
			}
		public double autoCenterRadius(Vector3d mid)
			{
			double r=0;
			for(ToolIsolayer ti:isolayers)
				{
				double nr=ti.slice.autoCenterRadius(mid);
				if(nr>r)
					r=nr;
				}
			return r;
			}
		public boolean canRender(EvObject ob){return false;}
		
		public void initOpenGL(GL gl)
			{
			}
		
		public void displayInit(GL gl){}
		public void displaySelect(GL gl){}
		public void readPersonalConfig(Element e){}
		public void savePersonalConfig(Element e){}
		public void datachangedEvent(){}
		public void fillModelWindowMenus()
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
		
		
		private EvDecimal getFrame()
			{
			return this.w.getFrame();
			}

		
		
		public void displayFinal(GL gl,List<TransparentRender> transparentRenderers)
			{
			for(ToolIsolayer ti:isolayers)
				ti.render(new ProgressHandle(), gl);   //TODO           this is the wrong place for this!!!!!! must able to abort
			}
		

		
		
		
		private class ToolIsolayer extends JPanel implements ChangeListener, ActionListener
			{
			static final long serialVersionUID=0;
			private JSpinner zplaneSpinner=new JSpinner(new SpinnerNumberModel((int)0.0,(int)-99.0,(int)999.0,(int)1));
			private EvComboChannel chanCombo=new EvComboChannel(null,true);
			private JButton bDelete=BasicIcon.getButtonDelete();
			private JCheckBox zProject=new JCheckBox("@Z=0");
			private EvComboColor colorCombo=new EvComboColor(false);
			private WeakReference<Imageset> lastImageset=new WeakReference<Imageset>(null);
			public Slice3D slice=new Slice3D();
			
			
			public ToolIsolayer()
				{
				JPanel q3=new JPanel(new BorderLayout());
				q3.add(zProject,BorderLayout.CENTER);
				q3.add(bDelete,BorderLayout.EAST);
				JPanel q4=new JPanel(new GridLayout(1,2));
				q4.add(withLabel("Slice:",zplaneSpinner));
				q4.add(colorCombo);
				setLayout(new GridLayout(3,1));
				setBorder(BorderFactory.createEtchedBorder());
				add(chanCombo);
				add(q4);
				add(q3);
				
				zplaneSpinner.addChangeListener(this);
				chanCombo.addActionListener(this);
				colorCombo.addActionListener(this);
				bDelete.addActionListener(this);
				zProject.addActionListener(this);
				}
			
			
			public void stateChanged(ChangeEvent arg0)
				{
				slice.rebuild();
				w.view.repaint(); //TODO modw repaint
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
			public void render(ProgressHandle progh, GL gl)
				{
				chanCombo.updateList();
				
				//Make sure surfaces are for the right imageset
				Imageset im=chanCombo.getImageset();
				if(lastImageset.get()!=im)
					slice.rebuild();
				lastImageset=new WeakReference<Imageset>(im);
				if(im==null)
					im=new Imageset();
				
				String channelName=chanCombo.getChannelName();
				if(channelName!=null)
					{
					EvChannel ch=im.getChannel(channelName);
					EvDecimal cframe=ch.closestFrame(getFrame());
					int zplane=(Integer)zplaneSpinner.getModel().getValue();

					//Create surface if it wasn't there before
					if(slice.needBuild(cframe))
						slice.build(progh, gl, cframe, im, ch, zplane);
					
					//Finally render
					slice.render(gl,colorCombo.getColor(), zProject.isSelected());
					}
				}
			
			}
		
		public EvDecimal getFirstFrame(){return null;}
		public EvDecimal getLastFrame(){return null;}		
		}

	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		ModelWindow.modelWindowExtensions.add(new Slice3DExtension());
		}

	}
