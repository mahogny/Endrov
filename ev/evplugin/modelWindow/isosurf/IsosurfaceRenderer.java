package evplugin.modelWindow.isosurf;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.TreeMap;
import javax.media.opengl.GL;
import javax.vecmath.Vector3f;
import com.sun.opengl.util.BufferUtil;
import evplugin.imageset.EvImage;
import evplugin.imageset.Imageset;



//can replace float with short, half memory => twice basic speed
//*should* load images while rendering, skipping the ptscalarstep


/**
 * 
 * @author Johan Henriksson
 */
public class IsosurfaceRenderer
	{
	Isosurface iso=new Isosurface();
	FloatBuffer vertb;
	FloatBuffer vertn;
	IntBuffer indb;

	public IsosurfaceRenderer(Imageset im, String channelName, int cframe, int blursize, float cutoff)
		{
		//Prepare to blur XY
		ConvolveOp simpleBlur=null;
		if(blursize!=0)
			{
			int blurarrsize=(1+2*blursize)*(1+2*blursize);
			float weight = 1.0f/(float)blurarrsize;
			float[] elements = new float[blurarrsize]; 
			for (int i = 0; i < elements.length; i++) 
				elements[i] = weight;
			Kernel myKernel = new Kernel(blursize*2+1, blursize*2+1, elements);
			simpleBlur = new ConvolveOp(myKernel);
			}

		int w=0,h=0,d=0;
		float realw=0,realh=0,reald=0; //TODO: should be able to have different distances

		float ptScalarField[]=null;
		if(im.channelImages.containsKey(channelName) &&
				im.channelImages.get(channelName).imageLoader.containsKey(cframe))
			{
			double resZ=im.meta.resZ;

			//long t=System.currentTimeMillis();

			TreeMap<Integer,EvImage> slices=im.channelImages.get(channelName).imageLoader.get(cframe);
			int curslice=0;
			if(slices!=null)
				for(int i:slices.keySet())
					{
					EvImage evim=slices.get(i);
					BufferedImage bim=evim.getJavaImage();

					//Blur the image
					if(simpleBlur!=null)
						{
						BufferedImage bufo=new BufferedImage(bim.getWidth(), bim.getHeight(), bim.getType());
						simpleBlur.filter(bim, bufo);
						bim=bufo;
						}

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

			//System.out.println("loading ok:"+(System.currentTimeMillis()-t));
			}


		//Generate polygons
		if(ptScalarField!=null)
			{
			//Smoothen Z?
			//Generate surface
			iso.generateSurface(ptScalarField, cutoff, w-1, h-1, d-1, realw/w, realh/h, reald/d);

			Vector3f[] vertices=iso.getVertices();
			Vector3f[] normals=iso.getNormals();
			int[] indices=iso.getIndices();
			if(vertices.length>0 && indices.length>0 && normals.length>0)
				{
				vertb=BufferUtil.newFloatBuffer(vertices.length*3);
				for(int i=0;i<vertices.length;i++)
					{
					vertb.put(vertices[i].x);
					vertb.put(vertices[i].y);
					vertb.put(vertices[i].z);
					}
				vertn=BufferUtil.newFloatBuffer(normals.length*3);
				for(int i=0;i<normals.length;i++)
					{
					vertn.put(normals[i].x);
					vertn.put(normals[i].y);
					vertn.put(normals[i].z);
					}
				indb=BufferUtil.newIntBuffer(indices.length); 
				for(int i:indices)
					indb.put(i);
				}
			}
		}
	
	
	
	public void render(GL gl, float red, float green, float blue, float trans)
		{
		//Render surface
		if(vertb!=null)
			{
			float lightDiffuse[]=new float[]{red, green, blue, trans};
			float lightAmbient[] = { 0.3f, 0.3f, 0.3f, 0.0f };
			gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, lightAmbient, 0);   
	    gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, lightDiffuse, 0);   
			
			
	    if(trans<0.99)
	    	{
	    	//NEHE does additive instead
	    	
		    gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
				gl.glEnable(GL.GL_BLEND);
				gl.glDepthMask(false);
				gl.glDisable(GL.GL_CULL_FACE);
	    	}
				
	    
			vertb.rewind();
			vertn.rewind();
			indb.rewind();
			gl.glEnableClientState( GL.GL_VERTEX_ARRAY );
			gl.glEnableClientState( GL.GL_NORMAL_ARRAY );
			gl.glDisable(GL.GL_CULL_FACE);
			gl.glEnable(GL.GL_LIGHTING);
			gl.glEnable(GL.GL_LIGHT0);
			gl.glColor4f(1,1,1,trans);

			gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertb);
			gl.glNormalPointer(GL.GL_FLOAT, 0, vertn);
			gl.glDrawElements(GL.GL_TRIANGLES, indb.remaining(), GL.GL_UNSIGNED_INT, indb);

			gl.glDisable(GL.GL_LIGHT0);
			gl.glDisable(GL.GL_LIGHTING);
			gl.glEnable(GL.GL_CULL_FACE);
			gl.glDisableClientState( GL.GL_VERTEX_ARRAY );
			gl.glDisableClientState( GL.GL_NORMAL_ARRAY );
			
			
	    if(trans<0.99)
	    	{
	    	gl.glDisable(GL.GL_BLEND);
				gl.glDepthMask(true);
				gl.glEnable(GL.GL_CULL_FACE);
				
				
				//Can draw an additional time, but just write to Z-buffer. dunno what is best
				
	    	}
			}
		}
	
	}
