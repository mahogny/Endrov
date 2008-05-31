package evplugin.modelWindow.basicExt;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

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
public class ModelWindowClipPlane implements ModelWindowExtension
	{
	//OpenGL follows Ax+By+Cz+D=0 with ABC normalized
	
	public static void initPlugin() {}
	static
		{
		ModelWindow.modelWindowExtensions.add(new ModelWindowClipPlane());
		}
	
	public void newModelWindow(ModelWindow w)
		{
		Hook h=new Hook(w);
		w.modelWindowHooks.add(h); 
		}

	/** Get vector3d axis by index */
	public static double vertexGetCoord(Vector3d v, int i)
		{
		if(i==0) return v.x; else if(i==1) return v.y; else return v.z; //i==2
		}
	/** Set vector3d axis by index */
	public static void vertexSetCoord(Vector3d v, int i, double d)
		{
		if(i==0) v.x=d; else if(i==1) v.y=d; else v.z=d; //i==2
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
		public void readPersonalConfig(Element e){}
		public void savePersonalConfig(Element e){}
		public void select(int id){}
		public void datachangedEvent(){}
		public void fillModelWindomMenus()
			{
			w.sidePanelItems.add(addIsolevel);
			for(ToolSlab ti:isolayers)
				w.sidePanelItems.add(ti);
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
		
		
		
		
		public void displayInit(GL gl)
			{
			int i=0;
			for(ToolSlab ti:isolayers)
				{
				ti.displayInit(gl, i);
				i++;
				}
			for(;i<w.view.numClipPlanesSupported;i++)
				gl.glDisable(GL.GL_CLIP_PLANE0+i);			
			}
		public void displaySelect(GL gl)
			{
			}
		public void displayFinal(GL gl,List<TransparentRender> transparentRenderers)
			{
			int i=0;
			for(ToolSlab ti:isolayers)
				{
				ti.renderFinal(gl,i);
				i++;
				}
			}
		

	
		
		
		
		private class ToolSlab extends JPanel implements ChangeListener, ActionListener
			{
			static final long serialVersionUID=0;
			private JButton bDelete=new JButton(BasicWindow.getIconDelete());
			private JButton bInvert=new JButton("Invert");
			private JCheckBox cEnabled=new JCheckBox("Enabled",true);
			private JCheckBox cVisible=new JCheckBox("Visible",true);
			
			public JNumericField[][] fPoints=new JNumericField[][]{
						{new JNumericField(0.0),new JNumericField(0.0),new JNumericField(0.0)},
						{new JNumericField(0.0),new JNumericField(0.0),new JNumericField(0.0)},
						{new JNumericField(0.0),new JNumericField(0.0),new JNumericField(0.0)}};
			//Should try and set a sensible default size-dependent
			private final Vector3d[] points=new Vector3d[]{new Vector3d(0,0,0),new Vector3d(0,0,0),new Vector3d(0,0,0)};

			
			public ToolSlab()
				{
				//Set coordinates
				double size=ModelWindowGrid.getGridSize(w);
				points[0].x=size;
				points[1].y=size;
				points[2].z=size;
				updateVector2field();

				//Build GUI
				JPanel q3=new JPanel(new BorderLayout());
				q3.add(cEnabled,BorderLayout.CENTER);
				q3.add(bDelete,BorderLayout.EAST);
				JPanel q5=new JPanel(new BorderLayout());
				q5.add(cVisible,BorderLayout.CENTER);
				q5.add(bInvert,BorderLayout.EAST);
				setLayout(new GridLayout(5,1));
				setBorder(BorderFactory.createEtchedBorder());
				
				for(JNumericField[] pfs:fPoints)
					{
					JPanel q4=new JPanel(new GridLayout(1,3));
					for(JNumericField f:pfs)
						{
						f.addActionListener(this);
						q4.add(f);
						}
					add(q4);
					}
				
				add(q5);
				add(q3);
				
				bInvert.addActionListener(this);
				bDelete.addActionListener(this);
				cEnabled.addActionListener(this);
				cVisible.addActionListener(this);
				}
			
			public void stateChanged(ChangeEvent arg0)
				{
				w.view.repaint();
				}


			public void actionPerformed(ActionEvent e)
				{
				System.out.println("called");
				if(e.getSource()==bInvert)
					{
					Vector3d v=points[2];
					points[2]=points[1];
					points[1]=v;
					updateVector2field();
					}
				else if(e.getSource()==bDelete)
					{
					isolayers.remove(this);
					w.updateToolPanels();
					}
				
				System.out.println("here");
				for(int i=0;i<3;i++)
					for(int j=0;j<3;j++)
						if(fPoints[i][j]==e.getSource())
							vertexSetCoord(points[i],j,fPoints[i][j].getDouble(0));
				
				stateChanged(null);
				}


			private void updateVector2field()
				{
				for(int i=0;i<3;i++)
					for(int j=0;j<3;j++)
						fPoints[i][j].set(vertexGetCoord(points[i], j)); //Will this trigger events?
				}
			
			/**
			 * Listener for changes in plane coordinates
			 */
			private class CL implements CrossHandler.CrossListener
				{
				int id;
				public CL(int id){this.id=id;}
				public void crossmove(Vector3d diff)
					{
					if(id==4)
						for(int i=0;i<3;i++)
							points[i].add(diff);
					else
						points[id].add(diff);
					updateVector2field();
					}
				}
			
		
				
			public void displayInit(GL gl, int slabid)
				{
				if(cVisible.isSelected())
					{
					Vector3d mid=new Vector3d();
					for(int i=0;i<3;i++)
						{
						w.crossHandler.addCross(points[i], new CL(i));
						mid.add(points[i]);
						}
					mid.scale(1/3.0);
					w.crossHandler.addCross(mid, new CL(4));
					}
				
				if(cEnabled.isSelected())
					{
					//Calculate plane
					Vector3d va=new Vector3d(points[0]);
					Vector3d vb=new Vector3d(points[0]);
					va.sub(points[1]);
					vb.sub(points[2]);
					Vector3d normal=new Vector3d();
					normal.cross(va,vb);
					normal.normalize();
					double A=normal.x;
					double B=normal.y;
					double C=normal.z;
					double D=-normal.dot(points[0]);
					
					//Draw plane
					gl.glEnable(GL.GL_CLIP_PLANE0+slabid);
					double[] eq=new double[]{A,B,C,D};
					gl.glClipPlane(GL.GL_CLIP_PLANE0+slabid, eq, 0);
					}
				else
					gl.glDisable(GL.GL_CLIP_PLANE0+slabid);
				}
			
		
			/**
			 * Render final
			 */
			public void renderFinal(GL gl, int slabid)
				{
				if(cVisible.isSelected())
					{
					gl.glDisable(GL.GL_CLIP_PLANE0+slabid);
					gl.glBegin(GL.GL_LINE_LOOP);
					gl.glColor3f(1, 0, 0);
					for(int i=0;i<3;i++)
						gl.glVertex3f((float)points[i].x,(float)points[i].y,(float)points[i].z);
					gl.glEnd();
					if(cEnabled.isSelected())
						gl.glEnable(GL.GL_CLIP_PLANE0+slabid);
					}
				}
			
			}

		
		}
	
	}
