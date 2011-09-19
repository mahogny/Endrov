/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowGenerateImage;

import endrov.flowBasic.math.EvOpImageMulImage;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;
import endrov.util.ProgressHandle;

/**
 * Generate special images useful for calculations
 * 
 * @author Johan Henriksson
 *
 */
public class GenerateSpecialImage
	{
	
	
	
	

	/**
	 * Return an image of given size: im(x,y)=x^p*y^q.
	 * Requires p,q>=0
	 */
	public static EvPixels genXpYp(ProgressHandle ph, int w, int h, int p, int q)
		{
		EvPixels out=GenerateSpecialImage.genConstant(w, h, 1);
		
		if(p>0)
			{
			EvPixels mul=GenerateSpecialImage.genIncX(w, h);
			for(int i=0;i<p;i++)
				out=new EvOpImageMulImage().exec1(ph, out, mul);
			}
		if(q>0)
			{
			EvPixels mul=GenerateSpecialImage.genIncY(w, h);
			for(int i=0;i<q;i++)
				out=new EvOpImageMulImage().exec1(ph, out, mul);
			}
		return out;
		}

	/**
	 * Return an image of given size: im(x,y)=c
	 * c c c
	 * c c c
	 * c c c
	 */
	public static EvPixels genConstant(int w, int h, int c)
		{
		EvPixels p=new EvPixels(EvPixelsType.INT,w,h);
		int[] aPixels=p.getArrayInt();
		for(int y=0;y<h;y++)
			{
			int base=y*w;
			for(int x=0;x<w;x++)
				aPixels[base+x]=c;
			}
		return p;
		}
	
	/**
	 * Return an image of given size: im(x,y)=c
	 * c c c
	 * c c c
	 * c c c
	 */
	public static EvPixels genConstant(int w, int h, double c)
		{
		EvPixels p=new EvPixels(EvPixelsType.DOUBLE,w,h);
		double[] aPixels=p.getArrayDouble();
		for(int y=0;y<h;y++)
			{
			int base=y*w;
			for(int x=0;x<w;x++)
				aPixels[base+x]=c;
			}
		return p;
		}

	/**
	 * Return an image of given size: im(x,y)=y
	 * 0 0 0
	 * 1 1 1
	 * 2 2 2
	 */
	public static EvPixels genIncY(int w, int h)
		{
		EvPixels p=new EvPixels(EvPixelsType.INT,w,h);
		int[] aPixels=p.getArrayInt();
		for(int y=0;y<h;y++)
			{
			int base=y*w;
			for(int x=0;x<w;x++)
				aPixels[base+x]=y;
			}
		return p;
		}

	/**
	 * Return an image of given size: im(x,y)=x
	 * 0 1 2
	 * 0 1 2
	 * 0 1 2
	 */
	public static EvPixels genIncX(int w, int h)
		{
		EvPixels p=new EvPixels(EvPixelsType.INT,w,h);
		int[] aPixels=p.getArrayInt();
		for(int y=0;y<h;y++)
			{
			int base=y*w;
			for(int x=0;x<w;x++)
				aPixels[base+x]=x;
			}
		return p;
		}
	

	/**
	 * 2D Gaussian function placed in the middle
	 */
	public static EvPixels genGaussian2D(double sigmaX, double sigmaY, int w, int h)
		{
		EvPixels p=new EvPixels(EvPixelsType.DOUBLE,w,h);
		double[] aPixels=p.getArrayDouble();
		
		//double mul1=1/(sigmaX*sigmaY*Math.sqrt(2*Math.PI));
		double mul2x=-1/(2*sigmaX*sigmaX);
		double mul2y=-1/(2*sigmaY*sigmaY);
		
		double midx=w/2;
		double midy=h/2;
		
		//If exp is expensive then it need only be O(max(w,h)) times.
		//This comes at others costs however
		
		double sum=0;
		for(int y=0;y<h;y++)
			{
			int base=y*w;
			double dy2=y-midy;
			dy2=dy2*dy2*mul2y;
			for(int x=0;x<w;x++)
				{
				double dx2=x-midx;
				dx2=dx2*dx2*mul2x;
				double t=Math.exp(dx2+dy2);
				sum+=t;
				aPixels[base+x]=t;
				}
			}
		sum=1/sum;
		for(int i=0;i<aPixels.length;i++)
			aPixels[i]*=sum;
			
		return p;
		}


	
	/**
	 * 3D Gaussian function placed in the middle
	 */
	public static EvStack genGaussian3D(ProgressHandle progh, double sigmaX, double sigmaY, double sigmaZ, int w, int h, int d)
		{
		EvStack s=new EvStack();
		s.setTrivialResolution();
		
//		double mul1=1/(sigmaX*sigmaY*sigmaZ*Math.sqrt(2*Math.PI));
		double mul2x=-1/(2*sigmaX*sigmaX);
		double mul2y=-1/(2*sigmaY*sigmaY);
		double mul2z=-1/(2*sigmaZ*sigmaZ);
		
		double midx=EvStack.calcMidCoordinate(w);
		double midy=EvStack.calcMidCoordinate(h);
		double midz=EvStack.calcMidCoordinate(d);
		
		//Could generate a single plane and multiply by Math.exp(mul2 dz2) if it makes any difference
		
		double sum=0;
		for(int curd=0;curd<d;curd++)
		//int curd=0;
		//for(EvDecimal decd:template.keySet())
			{
			EvPixels p=new EvPixels(EvPixelsType.DOUBLE,w,h);
			double[] aPixels=p.getArrayDouble();
			
			double dz2=curd-midz;
			dz2=dz2*dz2*mul2z;
			
			for(int y=0;y<h;y++)
				{
				int base=y*w;
				double dy2=y-midy;
				dy2=dy2*dy2*mul2y;
				double dyz2=dy2+dz2;
				for(int x=0;x<w;x++)
					{
					double dx2=x-midx;
					dx2=dx2*dx2;
					double t=Math.exp(mul2x*dx2+dyz2);
					aPixels[base+x]=t;
					sum+=t;
					}
				}
			
			s.putInt(curd, new EvImage(p));
			}
		sum=1.0/sum;
		for(EvPixels p:s.getPixels(progh))
			{
			double[] aPixels=p.getArrayDouble();
			for(int i=0;i<aPixels.length;i++)
				aPixels[i]*=sum;
			}

		
		return s;
		}
	
	
	
	
	/**
	 * 2D filled circle
	 * TODO make faster
	 * TODO unfilled? maybe faster and good enough for conv
	 */
	public static EvPixels genFilledCircle(double r, int w, int h)
		{
		EvPixels p=new EvPixels(EvPixelsType.INT,w,h);
		int[] aPixels=p.getArrayInt();
		
		double midx=w/2;
		double midy=h/2;
		double r2=r*r;
		
		for(int y=0;y<h;y++)
			{
			int base=y*w;
			double dy2=y-midy;
			dy2=dy2*dy2;
			for(int x=0;x<w;x++)
				{
				double dx2=x-midx;
				dx2=dx2*dx2;
				if(dx2+dy2<=r2)
					aPixels[base+x]=1;
				}
			}
		return p;
		}

	
	/**
	 * Common kernels? laplace etc
	 */

	
	
	/**
	 * Copy an image over several focal planes. Adapts size according to a template stack, likely
	 * the stack the new stack will be combined with 
	 */
	public static EvStack repeatImageZ(EvImage im, EvStack template)
		{
		EvStack s=new EvStack();
		s.getMetaFrom(template);
		//for(EvDecimal d:template.keySet())
		for(int az=0;az<template.getDepth();az++)
			s.putInt(az, im.makeShadowCopy());
		return s;
		}
	
	
	/**
	 * Copy an image over several focal planes. Adapts size according to a template stack, likely
	 * the stack the new stack will be combined with 
	 */
	public static EvStack repeatImageZ(EvPixels p, EvStack template)
		{
		EvImage im=new EvImage();
		im.setPixelsReference(p);
		return repeatImageZ(im, template);
		}
	
	
	
	}
