package evplugin.modelWindow.clipPlane;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.media.opengl.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.vecmath.Vector3d;

import org.jdom.Element;

import evplugin.basicWindow.BasicWindow;
import evplugin.data.*;
import evplugin.ev.*;
import evplugin.modelWindow.*;


/**
 * Clipping planes
 * @author Johan Henriksson
 */
public class ClipPlaneExtension implements ModelWindowExtension
	{
	public static void initPlugin() {}
	static
		{
		ModelWindow.modelWindowExtensions.add(new ClipPlaneExtension());
		}
	
	public void newModelWindow(ModelWindow w)
		{
		w.modelWindowHooks.add(new Hook(w)); 
		}

	

	
	private class Hook implements ModelWindowHook, ActionListener
		{
		private ModelWindow w;
		private Vector<ToolSlab> isolayers=new Vector<ToolSlab>();
		private JButton addIsolevel=new JButton("Add clip plane");

		public Hook(ModelWindow w)
			{
			this.w=w;
			addIsolevel.addActionListener(this);
			}
		
		
		public Collection<Double> adjustScale()
			{
			return Collections.emptySet();
			}
		public Collection<Vector3D> autoCenterMid()
			{
			return Collections.emptySet();
			}
		public Collection<Double> autoCenterRadius(Vector3D mid, double FOV)
			{
			return Collections.emptySet();
			}
		public boolean canRender(EvObject ob){return false;}
		public void displayInit(GL gl){}
		public void readPersonalConfig(Element e){}
		public void savePersonalConfig(Element e){}
		public void select(int id){}
		public void datachangedEvent(){}
		public void fillModelWindomMenus()
			{
			w.sidePanelItems.add(addIsolevel);
			if(!isolayers.isEmpty())
				{
				JLabel label=new JLabel("Ax+By+Cz+D=0");
				w.sidePanelItems.add(label);
				for(ToolSlab ti:isolayers)
					w.sidePanelItems.add(ti);
				}
			}

		
		
		public void actionPerformed(ActionEvent e)
			{
			if(isolayers.size()<w.view.numClipPlanesSupported)
				{
				isolayers.add(new ToolSlab());
				w.updateToolPanels();
				}
			else
				JOptionPane.showMessageDialog(null, "Your hardware does not support more planes");
			}
		
		
		
		
		public void displaySelect(GL gl)
			{
			for(ToolSlab ti:isolayers)
				ti.renderSelect(gl);
			}
		public void displayFinal(GL gl)
			{
			int i=0;
			for(ToolSlab ti:isolayers)
				{
				ti.render(gl,i);
				i++;
				}
			for(;i<w.view.numClipPlanesSupported;i++)
				gl.glDisable(GL.GL_CLIP_PLANE0+i);
			}
		

	
		
		
		
		private class ToolSlab extends JPanel implements ChangeListener, ActionListener
			{
			static final long serialVersionUID=0;
			private JButton bDelete=new JButton(BasicWindow.getIconDelete());
			private JButton bInvert=new JButton("Invert");
			private JCheckBox cEnabled=new JCheckBox("Enabled",true);
			private JCheckBox cVisible=new JCheckBox("Visible",true);
		
			private JNumericField sA=new JNumericField(1.0);
			private JNumericField sB=new JNumericField(0.0);
			private JNumericField sC=new JNumericField(0.0);
			private JNumericField sD=new JNumericField(0.0);
			
			//Should try and set a sensible default size-dependent
			private Vector3d[] points=new Vector3d[]{new Vector3d(1,0,0),new Vector3d(0,1,0),new Vector3d(0,0,1)};
			
			public ToolSlab()
				{
				JPanel q3=new JPanel(new BorderLayout());
				q3.add(cEnabled,BorderLayout.CENTER);
				q3.add(bDelete,BorderLayout.EAST);
				JPanel q4=new JPanel(new GridLayout(1,4));
				q4.add(sA);q4.add(sB);q4.add(sC);q4.add(sD);
				JPanel q5=new JPanel(new BorderLayout());
				q5.add(cVisible,BorderLayout.CENTER);
				q5.add(bInvert,BorderLayout.EAST);
				setLayout(new GridLayout(3,1));
				setBorder(BorderFactory.createEtchedBorder());
				add(q4);
				add(q5);
				add(q3);
				
				bInvert.addActionListener(this);
				bDelete.addActionListener(this);
				cEnabled.addActionListener(this);
				cVisible.addActionListener(this);
				sA.addActionListener(this);
				sB.addActionListener(this);
				sC.addActionListener(this);
				sD.addActionListener(this);
				}
			
			public void stateChanged(ChangeEvent arg0)
				{
				w.view.repaint();
//				w.repaint();
				}


			public void actionPerformed(ActionEvent e)
				{
				System.out.println("called");
				if(e!=null && e.getSource()==bInvert)
					{
					sA.set(-sA.getDouble(0));
					sB.set(-sB.getDouble(0));
					sC.set(-sC.getDouble(0));
					sD.set(-sD.getDouble(0));
					}
				if(e!=null && e.getSource()==bDelete)
					{
					isolayers.remove(this);
					w.updateToolPanels();
					}
				stateChanged(null);
				}

			
			/**
			 * Listener for changes in plane coordinates
			 */
			private class CL implements ModelWindow.CrossListener
				{
				int id;
				public CL(int id){this.id=id;}
				public void crossmove(Vector3d diff)
					{
					points[id].add(diff);
					System.out.println("here "+id);
					}
				}
			
			public void renderSelect(GL gl)
				{
				if(cEnabled.isSelected() && cVisible.isSelected())
					for(int i=0;i<3;i++)
						w.addCross(points[i], new CL(i));
				}
				
			/**
			 * Render according to these controls. Create surfaces as needed.
			 */
			public void render(GL gl, int slabid)
				{
				//OpenGL follows Ax+By+Cz+D=0
				if(cEnabled.isSelected())
					{
					
					//Calculate plane
					Vector3d va=new Vector3d(points[0]);
					Vector3d vb=new Vector3d(points[0]);
					va.sub(points[1]);
					vb.sub(points[2]);
					Vector3d normal=new Vector3d();
					normal.cross(va,vb); //Note: not normalized
					double D=-normal.dot(normal);
					double A=normal.x;
					double B=normal.y;
					double C=normal.z;
					
					if(cVisible.isSelected())
						{
						gl.glDisable(GL.GL_CLIP_PLANE0+slabid);
						gl.glBegin(GL.GL_LINE_LOOP);
						gl.glColor3f(1, 0, 0);
						for(int i=0;i<3;i++)
							gl.glVertex3f((float)points[i].x,(float)points[i].y,(float)points[i].z);
						gl.glEnd();
						}
					
					gl.glEnable(GL.GL_CLIP_PLANE0+slabid);
					double[] eq=new double[]{A,B,C,D};
					gl.glClipPlane(GL.GL_CLIP_PLANE0+slabid, eq, 0);
					}
				else
					gl.glDisable(GL.GL_CLIP_PLANE0+slabid);
				}
			
			}
		
		
		}
	
	}
