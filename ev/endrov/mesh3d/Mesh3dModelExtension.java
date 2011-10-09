/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.mesh3d;

import java.nio.FloatBuffer;
import java.util.*;

import javax.media.opengl.*;
import javax.vecmath.Vector3d;

import org.jdom.Element;

import com.sun.opengl.util.BufferUtil;

import endrov.basicWindow.BasicWindow;
import endrov.data.EvObject;
import endrov.modelWindow.*;
import endrov.modelWindow.gl.GLMaterial;
import endrov.modelWindow.gl.GLMaterialSelect;
import endrov.modelWindow.gl.GLMaterialSolid;
import endrov.modelWindow.gl.GLMeshVBO;
import endrov.util.*;


/**
 * Extension to Model Window: shows meshs
 * @author Johan Henriksson
 */
public class Mesh3dModelExtension implements ModelWindowExtension
	{
  public void newModelWindow(ModelWindow w)
		{
		w.modelWindowHooks.add(new NucModelWindowHook(w));
		}
	
	static class NucModelWindowHook implements ModelWindowHook, ModelView.GLSelectListener
		{
		final ModelWindow w;
		public void fillModelWindowMenus()
			{
			}
		

		public NucModelWindowHook(ModelWindow w)
			{
			this.w=w;
			
			}
		
		public void readPersonalConfig(Element e){}
		public void savePersonalConfig(Element e){}
		
		public void datachangedEvent()
			{
			}
		
		
		public boolean canRender(EvObject ob)
			{
			return ob instanceof Mesh3D;
			}

		
		public Collection<Mesh3D> getMeshs()
			{
			List<Mesh3D> v=new LinkedList<Mesh3D>();
			for(Mesh3D ob:w.getSelectedData().getObjects(Mesh3D.class))
				if(w.showObject(ob))
					v.add(ob);
			return v;
			}
		
		public void initOpenGL(GL gl)
			{
			}

		
		
		private final HashMap<Integer,SelMesh3D> selectColorMap=new HashMap<Integer,SelMesh3D>();
		
		/**
		 * Prepare for rendering
		 */
		public void displayInit(GL gl)
			{
			selectColorMap.clear();
			}
		
		/**
		 * Render for selection
		 */
		public void displaySelect(GL glin)
			{
			//GL2 gl=glin.getGL2();
//			gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
			
			Collection<Mesh3D> meshs=getMeshs();
			
		//TODO what about meshs that have changed?
			
			//Render all meshs
			for(Mesh3D mesh:meshs)
				{
				int color=w.view.reserveSelectColor(this);
				selectColorMap.put(color, new SelMesh3D(mesh));

				displayMeshSelect(w.view, glin, mesh, color);
				}
				
			//gl.glPopAttrib();
			
			}
		
		public static void displayMeshSelect(ModelView view, GL glin, Mesh3D mesh, int color)
			{
			GL2 gl=glin.getGL2();
			gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
			
			//Upload to card if needed
			GLMeshVBO vbo=view.getMesh(mesh);
			if(vbo==null)
				view.setMesh(mesh, vbo=buildVBO(gl, mesh));
			
			GLMaterial m=new GLMaterialSelect(color);
			
			vbo.render(gl, m);
			
			gl.glPopAttrib();
			}
		
		
		//private Map<Mesh3D, GLMeshVBO> vbos=new HashMap<Mesh3D, GLMeshVBO>();

		/**
		 * Render graphics
		 */
		public void displayFinal(GL glin,List<TransparentRender> transparentRenderers)
			{			
			Collection<Mesh3D> meshs=getMeshs();
			
			/*
			//Delete VBOs no longer in use
			for(Mesh3D oldmesh:new LinkedList<Mesh3D>(vbos.keySet()))
				if(!meshs.contains(oldmesh))
					{
					vbos.get(oldmesh).destroy(gl);
					vbos.remove(oldmesh);
					}
*/
			//TODO what about meshs that have changed?
			
			//Render all meshs
			for(Mesh3D mesh:meshs)
				{
				displayMeshFinal(w.view, glin, mesh);
				
				
				}
			
			
			}
		
		public static void displayMeshFinal(ModelView view, GL glin, Mesh3D mesh)
			{
			GL2 gl=glin.getGL2();
			gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
			
			//Upload to card if needed
			GLMeshVBO vbo=view.getMesh(mesh);
			if(vbo==null)
				view.setMesh(mesh, vbo=buildVBO(gl, mesh));
			
			GLMaterial material=null;
			
			//Get material of face. TODO each face might have a different material!
			if(!mesh.faces.isEmpty())
				material=mesh.faces.iterator().next().material;
			
			if(material==null)
				material=new GLMaterialSolid();
			
			//vbo.drawSolid=false;
			//vbo.drawNormals=true;
			
			vbo.render(gl, material);
			gl.glPopAttrib();
			}
		
		private static GLMeshVBO buildVBO(GL gl, Mesh3D mesh)
			{
			GLMeshVBO vbo=new GLMeshVBO();

			int vertexCount=mesh.faces.size()*3;

      FloatBuffer vertices=BufferUtil.newFloatBuffer(vertexCount*3);
      FloatBuffer normals=BufferUtil.newFloatBuffer(vertexCount*3);
      FloatBuffer tex=BufferUtil.newFloatBuffer(vertexCount*2);

			for(int fi=0;fi<mesh.faces.size();fi++)
				{
				Mesh3D.Face f=mesh.faces.get(fi);
				for(int vi=0;vi<3;vi++)
					{
	      	Vector3d vert=mesh.vertex.get(f.vertex[vi]);
	      	vertices.put((float)vert.x);
	      	vertices.put((float)vert.y);
	      	vertices.put((float)vert.z);
					
	      	if(f.texcoord!=null)
	      		{
		      	Vector3d t=mesh.texcoord.get(f.texcoord[vi]);
		      	tex.put((float)t.x);
		      	tex.put((float)t.y);
		      	//TODO 3d texture support?
	      		}
	      	else
	      		{
		      	tex.put(0);
		      	tex.put(0);
	      		}
	      	
	      	if(f.normal!=null)
	      		{
		      	Vector3d n=mesh.normal.get(f.normal[vi]);
		      	normals.put((float)n.x);
		      	normals.put((float)n.y);
		      	normals.put((float)n.z);
	      		}
	      	else
	      		{
	      		normals.put(0);
	      		normals.put(0);
	      		normals.put(0);
	      		}
					}
				}
			
			
			boolean extensionOK = gl.isExtensionAvailable("GL_ARB_vertex_buffer_object");

			if(extensionOK)
				{
      	vertices.rewind();
      	tex.rewind();
      	normals.rewind();
      	
	      int[] VBOVertices = new int[1];  
	      gl.glGenBuffers(1, VBOVertices, 0);  
	      gl.glBindBuffer(GL.GL_ARRAY_BUFFER, VBOVertices[0]);  
	      gl.glBufferData(GL.GL_ARRAY_BUFFER, vertexCount * 3 * BufferUtil.SIZEOF_FLOAT, vertices, GL.GL_STATIC_DRAW);
	
	      int[] VBONormals = new int[1];  
	      gl.glGenBuffers(1, VBONormals, 0);  
	      gl.glBindBuffer(GL.GL_ARRAY_BUFFER, VBONormals[0]);  
	      gl.glBufferData(GL.GL_ARRAY_BUFFER, vertexCount * 3 * BufferUtil.SIZEOF_FLOAT, vertices, GL.GL_STATIC_DRAW);
	
	      int[] VBOTexCoords = new int[1];
	      gl.glGenBuffers(1, VBOTexCoords, 0);
	      gl.glBindBuffer(GL.GL_ARRAY_BUFFER, VBOTexCoords[0]);
	      gl.glBufferData(GL.GL_ARRAY_BUFFER, vertexCount * 2 * BufferUtil.SIZEOF_FLOAT, tex, GL.GL_STATIC_DRAW);
	      
	      vbo.vertVBO=VBOVertices[0];
	      vbo.texVBO=VBOTexCoords[0];
	      vbo.normalsVBO=VBONormals[0];
	      vbo.useVBO=true;
				}
			else
				{
	      vbo.vertices=vertices;
	      vbo.normals=normals;
	      vbo.tex=tex;

	      vbo.useVBO=false;
				}

			/*
      vbo.vertices=vertices;
      vbo.normals=normals;
      vbo.tex=tex;
      */
			
      vbo.vertices=vertices;
      vbo.normals=normals;
      vbo.tex=tex;

			
      vbo.vertexCount=vertexCount;
      return vbo;
			}

		/**
		 * Adjust the scale
		 * 
		 * TODO: eliminate. use radius function instead?
		 */
		public Collection<Double> adjustScale()
			{
			Collection<Mesh3D> meshs=getMeshs();
			for(Mesh3D mesh:meshs)
				{
				//TODO Potentially slow!!!! cache somehow!
				double maxx=-1000000,maxy=-1000000,maxz=-1000000;
				double minx= 1000000,miny= 1000000,minz= 1000000;

				//Calculate bounds
				for(Vector3d pos:mesh.vertex)
					{
					if(maxx<pos.x) maxx=pos.x;
					if(maxy<pos.y) maxy=pos.y;
					if(maxz<pos.z) maxz=pos.z;
					if(minx>pos.x) minx=pos.x;
					if(miny>pos.y) miny=pos.y;
					if(minz>pos.z) minz=pos.z;
					}
				double dx=maxx-minx;
				double dy=maxy-miny;
				double dz=maxz-minz;
				double dist=dx;
				if(dist<dy)
					dist=dy;
				if(dist<dz)
					dist=dz;
				return Collections.singleton((Double)dist);
				}
			return Collections.emptySet();
			}


		
		/**
		 * Give suitable center of all objects
		 */
		public Collection<Vector3d> autoCenterMid()
			{
			//Calculate center
			double meanx=0, meany=0, meanz=0;
			int num=0;
			for(Mesh3D lin:getMeshs())
				{
				for(Vector3d pos:lin.vertex)
					{
					meanx+=pos.x;
					meany+=pos.y;
					meanz+=pos.z;
					}
				num+=lin.vertex.size();
				}
			if(num==0)
				return Collections.emptySet();
			else
				{
				meanx/=num;
				meany/=num;
				meanz/=num;
				return Collections.singleton(new Vector3d(meanx,meany,meanz));
				}
			}
		
		
		/**
		 * Given a middle position, figure out radius required to fit objects
		 */
		public double autoCenterRadius(Vector3d mid)
			{
			//Calculate maximum radius
			double maxr2=0;
			for(Mesh3D lin:getMeshs())
				for(Vector3d pos:lin.vertex)
					{
					double dx=pos.x-mid.x;
					double dy=pos.y-mid.y;
					double dz=pos.z-mid.z;
					double r2=dx*dx+dy*dy+dz*dz;
					if(maxr2<r2)
						maxr2=r2;
					}
			return Math.sqrt(maxr2);
			}
		
		
		public EvDecimal getFirstFrame()
			{
			return null;
			}
		public EvDecimal getLastFrame()
			{
			return null;
			}

		
		SelMesh3D lastHover;
		SelMesh3D currentHover;
		SelMesh3D emptyHover=new SelMesh3D(null);
		
		public void hoverInit(int pixelid)
			{
			//Update hover
			lastHover=currentHover;
			currentHover=emptyHover;
//			LineageCommonUI.currentHover=LineageCommonUI.emptyHover;
			}

		public void hover(int pixelid)
			{
			System.out.println("tryhover "+pixelid);
			
			currentHover=selectColorMap.get(pixelid);
			if(!currentHover.equals(lastHover))
				{
				System.out.println("mesh rerend");
				BasicWindow.updateWindows(w);
				
				
				}
			System.out.println("Now hovering "+currentHover);
			}
			

		
		};
		
		
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		ModelWindow.modelWindowExtensions.add(new Mesh3dModelExtension());
		}
	

	}


