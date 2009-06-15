package endrov.unsortedImageFilters;

import java.util.*;

import javax.vecmath.Vector3d;



import endrov.basicWindow.BasicWindow;
import endrov.data.EvData;
import endrov.flow.std.logic.EvOpImageGreaterThanScalar;
import endrov.flow.std.math.EvOpImageAxpy;
import endrov.flow.std.math.EvOpImageSubImage;
import endrov.flowAveraging.AveragingFilter;
import endrov.flowAveraging.EvOpMovingAverage;
import endrov.flowAveraging.EvOpMovingSum;
import endrov.flowProjection.EvOpAverageZ;
import endrov.imageset.EvChannel;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.nuc.NucLineage;
import endrov.util.EvDecimal;
import endrov.util.Vector3i;

/**
 * Automatic tracking of nuclei from spots
 * 
 * TODO try a tophat
 * also, code a tophat special case for square and filled tophats? do so for all cases?
 * top hat 1:  =A(?)B-A = min_B(max_B(A))-A
 * top hat 2:  =A-A(??)B = max_B(min_B(A))-A
 * 
 * squared max/min can be made fast. actually, decomposable dimensions!!!
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class AutoLineage
	{
	public static void run()
		{
		System.out.println("1");
		
		//TODO: virtual channels through lazy eval in EvImage
		
		new Thread(){
			public void run()
				{
				for(EvData data:EvData.openedData)
					{
					for(Imageset im:data.getIdObjectsRecursive(Imageset.class).values())
						{
						NucLineage lin=new NucLineage();
						im.metaObject.put("auto", lin);
						String channelName="RFP";
						EvChannel ch=im.getChannel(channelName);
						if(ch!=null)
							{
							
							//TODO lazily eval moving average
							
							
							
							/////Old method
							
							Set<EvDecimal> frames=ch.imageLoader.keySet();
							
							for(EvDecimal f:frames)
								//if(f.less(new EvDecimal("15000")))
									{
									
									System.out.println("frame "+f);
									
									//For all image planes
									
									AutoLineage.run(lin,im,channelName,f);
									
									
									BasicWindow.updateWindows();
									}
							
							}
						
						}
					}
					
				}
		
		}.start();
		
		
		
		//endrov.unsortedImageFilters.AutoLineage2.run();
		
		
		}
	// /Volumes/TBU_main03/ost4dgood/TB2167_080416.ost
	//endrov.unsortedImageFilters.AutoLineage.run2();
	
	public static void run2()
		{
		for(EvData data:EvData.openedData)
			{
			for(Imageset im:data.getIdObjectsRecursive(Imageset.class).values())
				{
				//im.metaObject.put("MA", movingAverage(im.getChannel("RFP"), 30, 30));
				
				im.metaObject.put("avgz", new EvOpAverageZ().exec1(im.getChannel("RFP")));
				
				//Two input channels, one out
	//			EvPixels c2=ImageMath.minus(in, average);
				//im.metaObject.put("minus", minus(im.getChannel("RFP"), im.getChannel("MA")));
				im.metaObject.put("minus", new EvOpImageSubImage().exec1(im.getChannel("RFP"), im.getChannel("MA")));

				//im.metaObject.put("minusAvgz", minus(im.getChannel("RFP"), im.getChannel("avgz")));
				im.metaObject.put("minusAvgz", new EvOpImageSubImage().exec1(im.getChannel("RFP"), im.getChannel("avgz")));
//				im.metaObject.put("minusAvgz#", axpy(im.getChannel("minusAvgz"),0.5,10));
				im.metaObject.put("minusAvgz#", new EvOpImageAxpy(0.5,10).exec1(im.getChannel("minusAvgz")));

				//im.metaObject.put("MAavgz", movingAverage(im.getChannel("minusAvgz"), 30, 30));
				//im.metaObject.put("minus2", minus(im.getChannel("minusAvgz"), im.getChannel("MAavgz")));
				im.metaObject.put("minus2", new EvOpImageSubImage().exec1(im.getChannel("minusAvgz"), im.getChannel("MAavgz")));

				
				//EvPixels spotpixels=CompareImage.greater(c2, 2);
				im.metaObject.put("spotpixels", new EvOpImageGreaterThanScalar(2).exec1(im.getChannel("minus")));
				im.metaObject.put("spotpixels2", new EvOpImageGreaterThanScalar(2).exec1(im.getChannel("minus2")));

//				im.metaObject.put("MA15", movingAverage(im.getChannel("RFP"), 5, 5));
				//im.metaObject.put("MA30-MA15", minus(ImageMath.times(im.getChannel("MA15"),2), im.getChannel("MA")));

				
				//EvPixels out=CompareImage.greater(MiscFilter.movingSum(spotpixels, 2, 2), 15);
				im.metaObject.put("MS", new EvOpMovingSum(2,2).exec1(im.getChannel("spotpixels")));
				im.metaObject.put("MSg", new EvOpImageGreaterThanScalar(15).exec1(im.getChannel("MS")));

				im.metaObject.put("MS2", new EvOpMovingSum(2,2).exec1(im.getChannel("spotpixels2")));
				im.metaObject.put("MSg2", new EvOpImageGreaterThanScalar(15).exec1(im.getChannel("MS2")));

				
				/*
				 * how to do image-avg_z(image)? z averages will be in the wrong locations.
				 * solution 1: store average for each z, use shadow images
				 * solution 2: make avg of much lower z resolution "big block"
				 * solution 3: expander, solution 1 but manually applied
				 * 
				 * * how to avoid saving duplicates with 1-like solution?
				 * * if not 1, what is the new z? 
				 * * parallel computation will be confused by shadow images?
				 * 
				 * 
				 * 
				 * 
				 */
				
				
//				im.channelImages.put("MA", movingAverage(im.channelImages.get("RFP"), 30, 30));
				}
			}
		BasicWindow.updateWindows();
		}
	
	

	
	/**
	 * Lazy Moving average.
	 */
	/*
	public static EvChannel movingAverage(EvChannel ch, final int pw, final int ph)
		{
		return new OpSlice(){
			public EvPixels exec(EvPixels... p)
				{
				return AveragingFilter.movingAverage(p[0], pw, ph);
				}
		}.exec(ch);
		}*/
	
	
	/**
	 * Lazy Axpy: ch*b+c
	 */
	/*
	public static EvChannel axpy(EvChannel ch, final double b, final double c)
		{
		return new SliceOp(){
			public EvPixels exec(EvPixels... p)
				{
				return ImageMath.axpy(p[0], b, c);
				}
		}.exec(ch);
		}
	*/
	
	/**
	 * Lazy Minus.
	 */
	/*
	public static EvChannel minus(EvChannel ch1, EvChannel ch2)
		{
		return new SliceOp(){
			public EvPixels exec(EvPixels... p)
				{
				return ImageMath.minus(p[0], p[1]);
				}
		}.exec(ch1,ch2);
		}
	*/
	
	/**
	 * Lazy Greater.
	 */
	/*
	public static EvChannel greater(EvChannel ch1, EvChannel ch2)
		{
		return new OpSlice(){
			public EvPixels exec(EvPixels... p)
				{
				return CompareImage.greater(p[0], p[1]);
				}
		}.exec(ch1,ch2);
		}
	public static EvChannel greater(EvChannel ch, final int b)
		{
		return new OpSlice(){
			public EvPixels exec(EvPixels... p)
				{
				return CompareImage.greater(p[0], b);
				}
		}.exec(ch);
		}*/
	
	
	/**
	 * Lazy Moving sum.
	 */
	/*
	public static EvChannel movingSum(EvChannel ch, final int pw, final int ph)
		{
		return new OpSlice1(){
			public EvPixels exec1(EvPixels... p)
				{
				return AveragingFilter.movingSumQuad(p[0], pw, ph);
				}
		}.exec(ch);
		}
	*/
	/*
	public static EvChannel movingAverageOld(EvChannel ch, final int pw, final int ph)
		{
		//Not quite final: what if changes should go back into the channel? how?
		EvChannel newch=new EvChannel();
		
		for(Map.Entry<EvDecimal, EvStack> se:ch.imageLoader.entrySet())
			{
			EvStack newstack=new EvStack();
			EvStack stack=se.getValue();
			for(Map.Entry<EvDecimal, EvImage> pe:stack.entrySet())
				{
				final EvImage evim=pe.getValue();
				EvImage newim=new EvImage();
				newim.getMetaFrom(evim);
				newstack.put(pe.getKey(), newim);
				
				newim.io=new EvIOImage(){
					public EvPixels loadJavaImage()
						{
						Memoize<EvPixels> m=new Memoize<EvPixels>(){
							protected EvPixels eval()
								{
								return MiscFilter.movingAverage(evim.getPixels(), pw, ph);
								}};
						return m.get();
						}};
				}
			newch.imageLoader.put(se.getKey(), newstack);
			}
		return newch;
		}
		*/
	
	public static void run(NucLineage lin, Imageset imageset, String channelName, EvDecimal frame)
		{
		
		EvStack origStack=imageset.getChannel(channelName).imageLoader.get(frame);
		

		//Classify pixels
		EvStack newStack=new EvStack();
		for(Map.Entry<EvDecimal, EvImage> e:origStack.entrySet())
			{
			EvPixels in=e.getValue().getPixels();
			EvPixels average=new EvOpMovingAverage(30,30).exec1(in);
			EvImage evim=e.getValue().makeShadowCopy();
			
			
			//EvPixels c2=ImageMath.minus(in, average);
			EvPixels c2=new EvOpImageSubImage().exec1(in, average);
			
			EvPixels spotpixels=new EvOpImageGreaterThanScalar(2).exec1(c2);
			
			/*
			EvPixels binmask=new EvPixels(EvPixels.TYPE_INT,3,3);
			double[] binmaskp=binmask.getArrayDouble();
			binmaskp[1]=binmaskp[0+3]=binmaskp[1+3]=binmaskp[2+3]=binmaskp[1+3*2]=1;
			*/
			
			EvPixels out=new EvOpImageGreaterThanScalar(15).exec1(AveragingFilter.movingSumQuad(spotpixels, 2, 2));
			
			
			
			evim.setPixelsReference(out);
			newStack.put(e.getKey(), evim);
			}
		
		
		//Cluster
		List<EvPixels> pixels=new LinkedList<EvPixels>();
		for(Map.Entry<EvDecimal, EvImage> e:newStack.entrySet())
			pixels.add(e.getValue().getPixels());
		List<Set<Vector3i>> clusters=SpotCluster.exec3d(pixels).getPartitions();

		//Find maximum size
		int maxVolume=0;
		for(Set<Vector3i> s:clusters)
			{
			int v=s.size();
			if(v>maxVolume)
				maxVolume=v;
			}
		
		//Sort by size
		//Maybe not needed
		Collections.sort(clusters,new Comparator<Set<Vector3i>>(){
			public int compare(Set<Vector3i> o1, Set<Vector3i> o2)
				{
				return Double.compare(o1.size(), o2.size());
				}
			});
		
		EvChannel ch=imageset.getChannel(channelName);
		EvStack stack=ch.imageLoader.get(ch.imageLoader.firstKey());
		//EvImage exampleIm=stack.firstEntry().snd();
		
		Iterator<EvDecimal> zit=imageset.getChannel(channelName).imageLoader.get(frame).keySet().iterator();
		EvDecimal z0=zit.next();
		EvDecimal z1=zit.next();
		for(EvDecimal d:imageset.getChannel(channelName).imageLoader.get(frame).keySet())
			System.out.println("plane "+d);
		
		
		//Problem: uneven resolution and distances
		double dz=z1.subtract(z0).doubleValue();
		double dv=dz/(stack.getResbinX()*stack.getResbinY());
		
		//dz 140!!!
		
		//Extract candidates from clusters
		int i=0;
		for(Set<Vector3i> c:clusters)
			{
			int size=c.size();
			if(size>maxVolume/10)
				{
				i++;
				double vol=size*dv;

				//V=4*pi*r^3/3
				//=> r=(V*3/(4*PI))^(1/3)
				
				double r=Math.pow(vol*3/(4*Math.PI),1.0/3.0);
				
				NucLineage.Nuc nuc=lin.getNucCreate(""+frame+":"+i);
				
				
				NucLineage.NucPos pos=nuc.getPosCreate(frame);
				
				//Problem: uneven distances. Unsure of displacement 
				Vector3d center=SpotCluster.calculateCenter(c);
				
				//TODO use stack transform functions
				
				pos.x=center.x/stack.getResbinX();
				pos.y=center.y/stack.getResbinY();//*exampleIm.binning/exampleIm.resY;
				pos.z=center.z*dz+z0.doubleValue();
				pos.r=r;
				
				nuc.overrideEnd=frame.add(new EvDecimal("15"));
				nuc.overrideStart=frame.add(new EvDecimal("-1"));
				
				
				}
			}
		
		
		//TODO convert to lineage, from vector3i. use resolution information
		
		//public static Partitioning<Vector3i> exec3d(List<EvPixels> in)
		
		
		
		}
	}
