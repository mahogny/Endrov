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

import endrov.data.EvObject;
import endrov.modelWindow.*;
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
		
		/**
		 * Prepare for rendering
		 */
		public void displayInit(GL gl)
			{
			}
		
		/**
		 * Render for selection
		 */
		public void displaySelect(GL gl)
			{
			}
		
		private Map<Mesh3D, MeshVBO> vbos=new HashMap<Mesh3D, MeshVBO>();

		/**
		 * Render graphics
		 */
		public void displayFinal(GL glin,List<TransparentRender> transparentRenderers)
			{
			GL2 gl=glin.getGL2();
			gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
			
//			EvDecimal curFrame=w.getFrame();

			
			Collection<Mesh3D> meshs=getMeshs();
			
			//Delete VBOs no longer in use
			for(Mesh3D oldmesh:new LinkedList<Mesh3D>(vbos.keySet()))
				if(!meshs.contains(oldmesh))
					vbos.get(oldmesh).destroy(gl);

			//TODO what about meshs that have changed?
			
			//Render all meshs
			for(Mesh3D mesh:meshs)
				{
				//Upload to card if needed
				MeshVBO vbo=vbos.get(mesh);
				if(vbo==null)
					vbos.put(mesh, vbo=buildVBO(gl, mesh));
				vbo.render(gl);
				}
			
			gl.glPopAttrib();
			}

		
		
		
		/**
		 * Mesh that has been prepared for efficient rendering
		 * 
		 * @author Johan Henriksson
		 *
		 */
		private static class MeshVBO
			{
			public int vertVBO;
			public int normVBO;
			public int texVBO;
			public int vertexCount;
			
			public FloatBuffer vertices;
			public FloatBuffer norms;
			public FloatBuffer tex;
			
			public void render(GL2 gl)
				{
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
        gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);  

        if(vertices!=null)
        	{
        	//Use vertex arrays
        	vertices.rewind();
        	tex.rewind();
        	norms.rewind();
          gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertices); 
          gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, tex); 
          gl.glNormalPointer(GL.GL_FLOAT, 0, norms);
        	}
        else
        	{
        	//Use VBO
          gl.glBindBuffer(GL.GL_ARRAY_BUFFER, texVBO);
          gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, 0);    
          gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertVBO);
          gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);    
          gl.glBindBuffer(GL.GL_ARRAY_BUFFER, normVBO);
          gl.glNormalPointer(GL.GL_FLOAT, 0, 0);
        	}

	      gl.glEnable(GL2.GL_LIGHTING);
				gl.glEnable(GL2.GL_CULL_FACE);
				gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, new float[]{0.5f,0.5f,0.5f}, 0);

        gl.glColor3f(1.0f, 1.0f, 1.0f);
//        gl.glDrawArrays(GL.GL_LINES, 0, vertexCount);  
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, vertexCount);  

  			gl.glDisable(GL2.GL_CULL_FACE);
  			gl.glDisable(GL2.GL_LIGHTING);

        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);  
        gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
        gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        
				}
			
			public void destroy(GL2 gl)
				{
				gl.glDeleteBuffers(3, new int[]{vertVBO, normVBO, texVBO}, 0);
				}
			
			}
		
		
		private static MeshVBO buildVBO(GL gl, Mesh3D mesh)
			{
			MeshVBO vbo=new MeshVBO();

			int vertexCount=mesh.faces.size()*3;

      FloatBuffer vertices=BufferUtil.newFloatBuffer(vertexCount*3);
      FloatBuffer norms=BufferUtil.newFloatBuffer(vertexCount*3);
      FloatBuffer tex=BufferUtil.newFloatBuffer(vertexCount*2);
      /*float[] avert=vertices.array();
      float[] anorms=norms.array();
      float[] atex=tex.array();*/

      //int arrayIndex=0;
			for(int fi=0;fi<mesh.faces.size();fi++)
				{
				Mesh3D.Face f=mesh.faces.get(fi);
				for(int vi=0;vi<3;vi++)
					{
	      	Vector3d vert=mesh.vertex.get(f.vertex[vi]);
	      	vertices.put((float)vert.x);
	      	vertices.put((float)vert.y);
	      	vertices.put((float)vert.z);
	      	/*
	      	avert[arrayIndex*3+0]=(float)vert.x;
	      	avert[arrayIndex*3+1]=(float)vert.y;
	      	avert[arrayIndex*3+2]=(float)vert.z;*/      	
					
	      	if(f.texcoord!=null)
	      		{
		      	Vector3d t=mesh.texcoord.get(f.texcoord[vi]);
		      	tex.put((float)t.x);
		      	tex.put((float)t.y);
		      	/*
		      	atex[arrayIndex*2+0]=(float)t.x;
		      	atex[arrayIndex*2+1]=(float)t.y;*/
		      	//3d texture support?
	      		}
	      	else
	      		{
		      	tex.put(0);
		      	tex.put(0);
	      		}
	      	
	      	if(f.normal!=null)
	      		{
		      	Vector3d n=mesh.normal.get(f.normal[vi]);
		      	norms.put((float)n.x);
		      	norms.put((float)n.y);
		      	norms.put((float)n.z);
		      	/*
		      	anorms[arrayIndex*3+0]=(float)n.x;
		      	anorms[arrayIndex*3+1]=(float)n.y;
		      	anorms[arrayIndex*3+2]=(float)n.z;
		      	*/
	      		}
	      	else
	      		{
	      		norms.put(0);
	      		norms.put(0);
	      		norms.put(0);
	      		}
	      	
	      	//arrayIndex++;
					}
				}
			
			if(true)
				{
      	vertices.rewind();
      	tex.rewind();
      	norms.rewind();
      	
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
	      vbo.normVBO=VBONormals[0];
				}
			else
				{
	      vbo.vertices=vertices;
	      vbo.norms=norms;
	      vbo.tex=tex;
				}
      
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
			double maxr=Math.sqrt(maxr2);
			return maxr;
			}
		
		
		public EvDecimal getFirstFrame()
			{
			return null;
			}
		public EvDecimal getLastFrame()
			{
			return null;
			}

		public void hover(int id)
			{
			}

		public void hoverInit(int id)
			{
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


