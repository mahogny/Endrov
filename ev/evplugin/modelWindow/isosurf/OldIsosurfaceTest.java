package evplugin.modelWindow.isosurf;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.nio.*;
import java.util.TreeMap;

import javax.media.opengl.*;
import javax.vecmath.Vector3f;

import com.sun.opengl.util.*;

import evplugin.data.EvData;
import evplugin.imageset.EvImage;
import evplugin.imageset.Imageset;



//http://www.java-tips.org/other-api-tips/jogl/vertex-buffer-objects-nehe-tutorial-jogl-port-2.html


public class OldIsosurfaceTest 
	{
	
	
	
	
	
	
	
	
	/////fix publics in isosurface. make inner class
	
	Isosurface iso=new Isosurface();
	FloatBuffer vertb;
	FloatBuffer vertn;
	IntBuffer indb;
	
	//can replace float with short, half memory => twice basic speed
	
	public void oeoeuIsotest()
		{
		int w=400,h=400,z=200;

		long time3=System.currentTimeMillis();
		float ptScalarField[]=new float[w*h*z];
		for(int i=0;i<w;i++)
			for(int j=0;j<h;j++)
				for(int k=0;k<z;k++)
					ptScalarField[i*z*h + j*z + k]=new Vector3f(i-w/2,j-h/2,k-z/2).length();
		
		long time4=System.currentTimeMillis();
		System.out.println("alloc time: "+(time4-time3));
		
		
		//with -1
		//400x400x200, new Vector3f(i-w/2,j-h/2,k-z/2).length();
		//load time: 1762
		//#tri: 627186
		
		//WARM UP JIT
		for(int i=0;i<2;i++)
			iso.generateSurface(ptScalarField, (float)(w/4), w-1, h-1, z-1, 2.0f, 2.0f, 2.0f);
		System.out.println("warm-up ok");

		long time=System.currentTimeMillis();
		//3s for 400x400x200
		for(int i=0;i<5;i++)
			iso.generateSurface(ptScalarField, (float)(w/4), w-1, h-1, z-1, 2.0f, 2.0f, 2.0f);

		long time2=System.currentTimeMillis();
		System.out.println("load time: "+(time2-time)/5); 
		//TOTAL MAC 0.3 s for 20x20x20   0.6s for 200x200x200   3.2s for 400x400x200

		System.exit(1);


		//15ms for 400x400x200
		Vector3f[] vertices=iso.getVertices();
		vertb=BufferUtil.newFloatBuffer(vertices.length*3);
		for(int i=0;i<vertices.length;i++)
			{
			vertb.put(vertices[i].x);
			vertb.put(vertices[i].y);
			vertb.put(vertices[i].z);
			}

		Vector3f[] normals=iso.getNormals();
		vertn=BufferUtil.newFloatBuffer(normals.length*3);
		for(int i=0;i<normals.length;i++)
			{
			vertn.put(normals[i].x);
			vertn.put(normals[i].y);
			vertn.put(normals[i].z);
			}


		//27ms for 400x400x200 
		int[] indices=iso.getIndices();
		indb=BufferUtil.newIntBuffer(indices.length); 
		for(int i:indices)
			indb.put(i);
	

		
		}
	
	
	
	public void render(GL gl)
		{
		if(vertb==null)
			{
			
			
			//blur x-y
			float weight = 1.0f/9.0f;
			float[] elements = new float[9]; // create 2D array

			// fill the array with nine equal elements
			for (int i = 0; i < 9; i++) 
		   	   elements[i] = weight;
			
			// use the array of elements as argument to create a Kernel
			Kernel myKernel = new Kernel(3, 3, elements);
			ConvolveOp simpleBlur = new ConvolveOp(myKernel);
			
			
			
			
			//can replace float with short, half memory => twice basic speed
			
			int w=0,h=0,d=0;
			float realw=0,realh=0,reald=0; //TODO: should be able to have different distances

			float ptScalarField[]=null;

			
			String channelName="GFP";
			if(!EvData.metadata.isEmpty() && EvData.metadata.get(0) instanceof Imageset)
				{
				Imageset im=(Imageset)EvData.metadata.get(0);
				
				if(im.channelImages.containsKey(channelName))
					{
					//todo: get nearest. framecontrol?
					int cframe=im.channelImages.get(channelName).closestFrame((int)Math.round(0)); //TODO get frame
					

					System.out.println("loading");
					double resZ=im.meta.resZ;

					long t=System.currentTimeMillis();

					TreeMap<Integer,EvImage> slices=im.channelImages.get(channelName).imageLoader.get(cframe);
					int curslice=0;
					if(slices!=null)
						for(int i:slices.keySet())
							{
							EvImage evim=slices.get(i);
							BufferedImage bim=evim.getJavaImage();
							
						

							 // blur the image
							BufferedImage bufo=new BufferedImage(bim.getWidth(), bim.getHeight(), bim.getType());
							simpleBlur.filter(bim, bufo);
							bim=bufo;
							
							
							if(ptScalarField==null)
								{
								w=bim.getWidth();
								h=bim.getHeight();
								d=slices.size();
								realw=(float)bim.getWidth()/(float)(evim.getResX()/evim.getBinning());
								realh=(float)bim.getHeight()/(float)(evim.getResY()/evim.getBinning());
								reald=(float)d/(float)resZ;
								ptScalarField=new float[w*h*d];
								System.out.println("alloc "+w+" "+h+" "+d);
								}
							
						
							
							float[] pixels=new float[bim.getWidth()];
							for(int y=0;y<bim.getHeight();y++)
								{
								bim.getRaster().getPixels(0, y, bim.getWidth(), 1, pixels);
								for(int x=0;x<bim.getWidth();x++)
									ptScalarField[curslice*w*h+y*w+x]=pixels[x];
								
								}
							curslice++;
							}

					System.out.println("loading ok:"+(System.currentTimeMillis()-t));
					//System.exit(1);
					}
				}
			
			
			
			
			

			//fill in field
			
			if(ptScalarField!=null)
				{
			
				//Smoothen Z?
				
				
				
				//Generate surface
				
				long time=System.currentTimeMillis();
				for(int i=0;i<5;i++)
					iso.generateSurface(ptScalarField, 20f, w-1, h-1, d-1, realw/w, realh/h, reald/d);
				long time2=System.currentTimeMillis();
				System.out.println("iso time: "+(time2-time)/5);
				//System.out.println("#tri "+iso.indices.length/3+" #vert "+iso.vertices.length/3);
				
				Vector3f[] vertices=iso.getVertices();
				vertb=BufferUtil.newFloatBuffer(vertices.length*3);
				for(int i=0;i<vertices.length;i++)
					{
					vertb.put(vertices[i].x);
					vertb.put(vertices[i].y);
					vertb.put(vertices[i].z);
					}

				Vector3f[] normals=iso.getNormals();
				vertn=BufferUtil.newFloatBuffer(normals.length*3);
				for(int i=0;i<normals.length;i++)
					{
					vertn.put(normals[i].x);
					vertn.put(normals[i].y);
					vertn.put(normals[i].z);
					}
				int[] indices=iso.getIndices();
				indb=BufferUtil.newIntBuffer(indices.length); 
				for(int i:indices)
					indb.put(i);
				
				}

			}

		if(vertb!=null)
			{
			vertb.rewind();
			vertn.rewind();
			indb.rewind();
			gl.glEnableClientState( GL.GL_VERTEX_ARRAY );
			gl.glEnableClientState( GL.GL_NORMAL_ARRAY );

			
			gl.glDisable(GL.GL_CULL_FACE);//temp
			
			gl.glEnable(GL.GL_LIGHTING);
			gl.glEnable(GL.GL_LIGHT0);
			gl.glColor3d(1,1,1);


			long time=System.currentTimeMillis();
			gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertb);
			gl.glNormalPointer(GL.GL_FLOAT, 0, vertn);
			//gl.glDrawElements(GL.GL_TRIANGLES, indb.iso.indices.length, GL.GL_UNSIGNED_INT, indb);
			gl.glDrawElements(GL.GL_TRIANGLES, indb.remaining(), GL.GL_UNSIGNED_INT, indb);
			long time2=System.currentTimeMillis();


			//new better time: 1550milli total at home

			System.out.println("rnder time: "+(time2-time)); // 10milli for 200x200x200   //60milli for w=400,h=400,z=200;

			//			phong shading TODO

			gl.glDisable(GL.GL_LIGHT0);
			gl.glDisable(GL.GL_LIGHTING);

			gl.glDisableClientState( GL.GL_VERTEX_ARRAY );
			gl.glDisableClientState( GL.GL_NORMAL_ARRAY );
			
			gl.glEnable(GL.GL_CULL_FACE); //temp
			
			}
		
		


		}


	
	/*
	public void render(GL gl)
		{
		vertb.rewind();
		vertn.rewind();
		indb.rewind();
		gl.glEnableClientState( GL.GL_VERTEX_ARRAY );
		gl.glEnableClientState( GL.GL_NORMAL_ARRAY );

		gl.glEnable(GL.GL_LIGHTING);
		gl.glEnable(GL.GL_LIGHT0);
		gl.glColor3d(1,1,1);


		long time=System.currentTimeMillis();
		gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertb);
		gl.glNormalPointer(GL.GL_FLOAT, 0, vertn);
		gl.glDrawElements(GL.GL_TRIANGLES, iso.m_piTriangleIndices.length, GL.GL_UNSIGNED_INT, indb);
		long time2=System.currentTimeMillis();

		
		//new better time: 1550milli total at home
		
		System.out.println("rnder time: "+(time2-time)); // 10milli for 200x200x200   //60milli for w=400,h=400,z=200;
		
//		phong shading TODO

		gl.glDisable(GL.GL_LIGHTING);

		gl.glDisableClientState( GL.GL_VERTEX_ARRAY );
		gl.glDisableClientState( GL.GL_NORMAL_ARRAY );
		}
	
*/
	}
