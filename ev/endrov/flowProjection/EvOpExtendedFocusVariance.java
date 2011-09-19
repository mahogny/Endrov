/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowProjection;

import endrov.flow.EvOpStack1;
import endrov.flowImageStats.EvOpVarianceRect;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;
import endrov.util.ProgressHandle;

/**
 * Extended focus i.e.
 * 
 * @author Johan Henriksson
 *
 */
public class EvOpExtendedFocusVariance extends EvOpStack1
	{
	
	@Override
	public EvStack exec1(ProgressHandle ph, EvStack... p)
		{
		return project(ph, p[0]);
		}

	
	/**
	 * Project into a single-image stack
	 * 
	 * Possible improvement: keep track of where to get best pixels in a map. Then check this map
	 * at the end. Use pixels from the locally most common choice to avoid getting pixels almost randomly.
	 * This map can be given back separately to check the result.
	 * 
	 */
	public EvStack project(ProgressHandle progh, EvStack in)
		{
		EvStack out=new EvStack();
		out.getMetaFrom(in);
		int w=in.getWidth();
		int h=in.getHeight();
		
		EvPixels[] ps=in.getPixels(progh);
		double[][] psArrs=new double[ps.length][];
		for(int i=0;i<ps.length;i++)
			{
			ps[i]=ps[i].convertToDouble(true);
			psArrs[i]=ps[i].getArrayDouble();
			}
		
		EvPixels maxIndex=new EvPixels(EvPixelsType.INT,w,h);
		int[] arrMaxIndex=maxIndex.getArrayInt();
		
		//Use the first slice by default
		EvPixels maxVariance=EvOpVarianceRect.localVarianceRect(progh, ps[0], 1, 1).convertToDouble(false);
		EvPixels outPixels=new EvPixels(EvPixelsType.DOUBLE,w,h);
		double[] arrOutPixels=outPixels.getArrayDouble();
		double[] arrMaxVariance=maxVariance.getArrayDouble();
		
		//Try to find better pixels
		for(int j=0;j<ps.length;j++)
			{
			EvPixels newPixels=ps[j];
			EvPixels newVariance=EvOpVarianceRect.localVarianceRect(progh, newPixels, 1, 1);
			double[] arrNewVariance=newVariance.getArrayDouble();

			//Heuristic: keep pixels with highest local variance
			for(int i=0;i<arrMaxVariance.length;i++)
				if(arrNewVariance[i]>arrMaxVariance[i])
					{
					arrMaxVariance[i]=arrNewVariance[i];
					arrMaxIndex[i]=j;
					}
			
			}
		
		//Heuristic: to avoid spurious selection, check how common pixels are in a 3x3 neighbourhood.
		//3*3=9. If one pixel occurs frequent enough then use this one instead.
		int[] countPixel=new int[ps.length];
		int midIndex=0;
		for(int y=0;y<h;y++)
			for(int x=0;x<w;x++)
			{
			//Count occurance. This loop is unrolled for performance.
			for(int j=0;j<countPixel.length;j++)
				countPixel[j]=0;
			if(y>0)
				{
				if(x>0)
					countPixel[arrMaxIndex[midIndex-w-1]]++;
				countPixel[arrMaxIndex[midIndex-w]]++;
				if(x<w-1)
					countPixel[arrMaxIndex[midIndex-w+1]]++;
				}
			if(x>0)
				countPixel[arrMaxIndex[midIndex-1]]++;
			countPixel[arrMaxIndex[midIndex]]++;
			if(x<w-1)
				countPixel[arrMaxIndex[midIndex+1]]++;
			if(y<h-1)
				{
				if(x>0)
					countPixel[arrMaxIndex[midIndex+w-1]]++;
				countPixel[arrMaxIndex[midIndex+w]]++;
				if(x<w-1)
					countPixel[arrMaxIndex[midIndex+w+1]]++;
				}
			
			//There can at most be one pixel with more than 6 counts. Hence this loop
			//simplifies to just find *one* count
			int takeIndex=arrMaxIndex[midIndex]; //By default, keep current choice
			for(int j=0;j<countPixel.length;j++)
				if(countPixel[j]>=6)
					{
					takeIndex=j;
					break;
					}
			
			//arrOutPixels[midIndex]=takeIndex;
			arrOutPixels[midIndex]=psArrs[takeIndex][midIndex];
			midIndex++;
			}
		
/*		
		//Use the first slice by default
		EvImage proto=in.firstEntry().snd();
		EvPixels maxVariance=EvOpVarianceRect.localVarianceRect(proto.getPixels(), 1, 1).convertToDouble(false);
		EvPixels outPixels=proto.getPixels().convertToDouble(false);
		double[] arrOutPixels=outPixels.getArrayDouble();
		double[] arrMaxVariance=maxVariance.getArrayDouble();
		
		//Try to find better pixels
		for(EvImage plane:in.getImages())
			{
			EvPixels newPixels=plane.getPixels().convertToDouble(true);
			EvPixels newVariance=EvOpVarianceRect.localVarianceRect(newPixels, 1, 1);
			double[] arrNewPixels=newPixels.getArrayDouble();
			double[] arrNewVariance=newVariance.getArrayDouble();

			//Heuristic: keep pixels with highest local variance
			for(int i=0;i<arrOutPixels.length;i++)
				if(arrNewVariance[i]>arrMaxVariance[i])
					{
					arrMaxVariance[i]=arrNewVariance[i];
					arrOutPixels[i]=arrNewPixels[i];
					}
			}
			*/
		
		EvImage imout=new EvImage();
		imout.setPixelsReference(outPixels);
		
		int numZ=in.getDepth();
		for(int cz=0;cz<numZ;cz++)
			out.putInt(cz,imout.makeShadowCopy());
		
		return out;
		}
	}
