package endrov.unsortedImageFilters;

import java.util.*;

import javax.vecmath.Vector3d;



import endrov.basicWindow.BasicWindow;
import endrov.data.EvData;
import endrov.imageset.EvChannel;
import endrov.imageset.EvIOImage;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.nuc.NucLineage;
import endrov.util.EvDecimal;
import endrov.util.Memoize;
import endrov.util.Vector3i;

/**
 * Automatic tracking of nuclei from spots
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
				im.channelImages.put("MA", movingAverage(im.channelImages.get("RFP"), 30, 30));
				
				im.channelImages.put("avgz", avgZ(im.channelImages.get("RFP")));
				
				//Two input channels, one out
	//			EvPixels c2=ImageMath.minus(in, average);
				im.channelImages.put("minus", minus(im.channelImages.get("RFP"), im.channelImages.get("MA")));

				im.channelImages.put("minusAvgz", minus(im.channelImages.get("RFP"), im.channelImages.get("avgz")));
				im.channelImages.put("minusAvgz#", axpy(im.channelImages.get("minusAvgz"),0.5,10));

				im.channelImages.put("MAavgz", movingAverage(im.channelImages.get("minusAvgz"), 30, 30));
				im.channelImages.put("minus2", minus(im.channelImages.get("minusAvgz"), im.channelImages.get("MAavgz")));

				
				//EvPixels spotpixels=CompareImage.greater(c2, 2);
				im.channelImages.put("spotpixels", greater(im.channelImages.get("minus"),2));
				im.channelImages.put("spotpixels2", greater(im.channelImages.get("minus2"),2));

				im.channelImages.put("MA15", movingAverage(im.channelImages.get("RFP"), 5, 5));
				im.channelImages.put("MA30-MA15", minus(times(im.channelImages.get("MA15"),2), im.channelImages.get("MA")));

				
				//EvPixels out=CompareImage.greater(MiscFilter.movingSum(spotpixels, 2, 2), 15);
				im.channelImages.put("MS", movingSum(im.channelImages.get("spotpixels"), 2, 2));
				im.channelImages.put("MSg", greater(im.channelImages.get("MS"),15));

				im.channelImages.put("MS2", movingSum(im.channelImages.get("spotpixels2"), 2, 2));
				im.channelImages.put("MSg2", greater(im.channelImages.get("MS2"),15));

				
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
	
	
	public interface SliceOp
		{
		public EvPixels exec(EvPixels... p);
		}

	public interface StackOp
		{
		//By necessity, stack operators have to deal with laziness manually.
		//Example: avgZ only computes one slice and then duplicates it. other operands compute entire
		//stack. cannot fit together. possible to make functions beneath this.
		public EvStack exec(EvStack... p);
		}

	
	public static EvChannel avgZ(EvChannel ch)
		{
		return applyStackOp(new EvChannel[]{ch},new StackOp(){
		public EvStack exec(EvStack... p)
			{
			return eagerAvgZ(p[0]);
			}
		});
		}
	
	public static EvStack eagerAvgZ(EvStack in)
		{
		EvImage proto=in.firstEntry().snd();
		
		EvStack out=new EvStack();


		EvPixels ptot=new EvPixels(EvPixels.TYPE_INT,proto.getPixels().getWidth(),proto.getPixels().getHeight());
		int numZ=in.size();
		for(Map.Entry<EvDecimal, EvImage> plane:in.entrySet())
			ptot=ImageMath.plus(ptot, plane.getValue().getPixels());

		ptot=ImageMath.div(ptot,numZ);
		
		EvImage imout=new EvImage();
		imout.getMetaFrom(proto);
		imout.setPixelsReference(ptot);
		
		//Lazy stack op will use all planes!
		
		
		
		for(Map.Entry<EvDecimal, EvImage> plane:in.entrySet())
			out.put(plane.getKey(), imout.makeShadowCopy());
			
		return out;
		}
	
	/**
	 * Lazy Moving average.
	 */
	public static EvChannel movingAverage(EvChannel ch, final int pw, final int ph)
		{
		return applySliceOp(new EvChannel[]{ch},new SliceOp(){
			public EvPixels exec(EvPixels... p)
				{
				return MiscFilter.movingAverage(p[0], pw, ph);
				}
		});
		}
	
	
	/**
	 * Lazy Axpy: ch*b+c
	 */
	public static EvChannel axpy(EvChannel ch, final double b, final double c)
		{
		return applySliceOp(new EvChannel[]{ch},new SliceOp(){
			public EvPixels exec(EvPixels... p)
				{
				return ImageMath.axpy(p[0], b, c);
				}
		});
		}
	
	/**
	 * Lazy Minus.
	 */
	public static EvChannel minus(EvChannel ch1, EvChannel ch2)
		{
		return applySliceOp(new EvChannel[]{ch1, ch2},new SliceOp(){
			public EvPixels exec(EvPixels... p)
				{
				return ImageMath.minus(p[0], p[1]);
				}
		});
		}
	
	/**
	 * Lazy Greater.
	 */
	public static EvChannel greater(EvChannel ch1, EvChannel ch2)
		{
		return applySliceOp(new EvChannel[]{ch1, ch2},new SliceOp(){
			public EvPixels exec(EvPixels... p)
				{
				return CompareImage.greater(p[0], p[1]);
				}
		});
		}
	public static EvChannel greater(EvChannel ch, final int b)
		{
		return applySliceOp(new EvChannel[]{ch},new SliceOp(){
			public EvPixels exec(EvPixels... p)
				{
				return CompareImage.greater(p[0], b);
				}
		});
		}
	
	
	public static EvChannel times(EvChannel ch, final int b)
		{
		return applySliceOp(new EvChannel[]{ch},new SliceOp(){
			public EvPixels exec(EvPixels... p)
				{
				return ImageMath.times(p[0], b);
				}
		});
		}
	
	/**
	 * Lazy Moving sum.
	 */
	public static EvChannel movingSum(EvChannel ch, final int pw, final int ph)
		{
		return applySliceOp(new EvChannel[]{ch},new SliceOp(){
			public EvPixels exec(EvPixels... p)
				{
				return MiscFilter.movingSum(p[0], pw, ph);
				}
		});
		}
	
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
	
	/**
	 * Lazily create a channel using an operator that combines input channels
	 */
	public static EvChannel applySliceOp(EvChannel[] ch, final SliceOp op)
		{
		//Not quite final: what if changes should go back into the channel? how?
		EvChannel newch=new EvChannel();
		
		//How to combine channels? if A & B, B not exist, make B black?
		
		//Currently operates on common subset of channels
		
		for(Map.Entry<EvDecimal, EvStack> se:ch[0].imageLoader.entrySet())
			{
			EvStack newstack=new EvStack();
			EvStack stack=se.getValue();
			for(Map.Entry<EvDecimal, EvImage> pe:stack.entrySet())
				{
				final EvImage evim=pe.getValue();
				EvImage newim=new EvImage();
				newim.getMetaFrom(evim);
				newstack.put(pe.getKey(), newim);
				
				//TODO register lazy operation
				
				//TODO lazy stack operations would take us out of this mess.
				//it would however force lazy slices to be in lazy stacks because the latter requires
				//keys to be evaluated.
				//if resolution goes into stack then no keys need be evaluated, but other things still.
				
				final EvImage[] imlist=new EvImage[ch.length];
				int ci=0;
				for(EvChannel cit:ch)
					{
					imlist[ci]=cit.imageLoader.get(se.getKey()).get(pe.getKey());
					ci++;
					}
				
				final Memoize<EvPixels> m=new Memoize<EvPixels>(){
				protected EvPixels eval()
					{
					EvPixels[] plist=new EvPixels[imlist.length];
					for(int i=0;i<plist.length;i++)
						plist[i]=imlist[i].getPixels();
					return op.exec(plist);
					//return op.exec(evim.getPixels());
					}};
					
				newim.io=new EvIOImage(){public EvPixels loadJavaImage(){return m.get();}};
				
				newim.registerLazyOp(m);		
						
				}
			newch.imageLoader.put(se.getKey(), newstack);
			}
		return newch;
		}
	
	
	/**
	 * Lazily create a channel using an operator that combines input channels
	 */
	
	public static EvChannel applyStackOp(EvChannel[] ch, final StackOp op)
		{
		//Not quite final: what if changes should go back into the channel? how?
		EvChannel newch=new EvChannel();
		
		//How to combine channels? if A & B, B not exist, make B black?
		
		//Currently operates on common subset of channels
		
		for(Map.Entry<EvDecimal, EvStack> se:ch[0].imageLoader.entrySet())
			{
			EvStack newstack=new EvStack();
			EvStack stack=se.getValue();
			
			

			//TODO register lazy operation
			
			final EvStack[] imlist=new EvStack[ch.length];
			int ci=0;
			for(EvChannel cit:ch)
				{
				imlist[ci]=cit.imageLoader.get(se.getKey());
				ci++;
				}
			
			final Memoize<EvStack> ms=new Memoize<EvStack>(){
			protected EvStack eval()
				{
				return op.exec(imlist);
				}};
			
			//TODO without lazy stacks, prior stacks are forced to be evaluated.
			//only fix is if the laziness is added directly at the source.
			
			for(Map.Entry<EvDecimal, EvImage> pe:stack.entrySet())
				{
				
				final EvImage evim=pe.getValue();
				EvImage newim=new EvImage();
				newim.getMetaFrom(evim);
				newstack.put(pe.getKey(), newim);
				
				final EvDecimal z=pe.getKey();
					
				newim.io=new EvIOImage(){public EvPixels loadJavaImage(){return ms.get().get(z).getPixels();}};
				
				newim.registerLazyOp(ms);		
						
				}
			newch.imageLoader.put(se.getKey(), newstack);
			}
		return newch;
		}
		
	
	public static void run(NucLineage lin, Imageset imageset, String channelName, EvDecimal frame)
		{
		
		EvStack origStack=imageset.getChannel(channelName).imageLoader.get(frame);
		

		//Classify pixels
		EvStack newStack=new EvStack();
		for(Map.Entry<EvDecimal, EvImage> e:origStack.entrySet())
			{
			EvPixels in=e.getValue().getPixels();
			EvPixels average=MiscFilter.movingAverage(in, 30, 30);
			EvImage evim=e.getValue().makeShadowCopy();
			
			
			EvPixels c2=ImageMath.minus(in, average);
			
			EvPixels spotpixels=CompareImage.greater(c2, 2);
			
			/*
			EvPixels binmask=new EvPixels(EvPixels.TYPE_INT,3,3);
			double[] binmaskp=binmask.getArrayDouble();
			binmaskp[1]=binmaskp[0+3]=binmaskp[1+3]=binmaskp[2+3]=binmaskp[1+3*2]=1;
			*/
			
			EvPixels out=CompareImage.greater(MiscFilter.movingSum(spotpixels, 2, 2), 15);
			
			
			
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
		EvImage exampleIm=ch.imageLoader.get(ch.imageLoader.firstKey()).firstEntry().snd();
		
		Iterator<EvDecimal> zit=imageset.getChannel(channelName).imageLoader.get(frame).keySet().iterator();
		EvDecimal z0=zit.next();
		EvDecimal z1=zit.next();
		for(EvDecimal d:imageset.getChannel(channelName).imageLoader.get(frame).keySet())
			System.out.println("plane "+d);
		
		
		//Problem: uneven resolution and distances
		double dz=z1.subtract(z0).doubleValue();
		double dv=exampleIm.binning*exampleIm.binning*dz*1.0/(exampleIm.resX*exampleIm.resY);
		
		//dz 140!!!
		
		System.out.println("Scaling");
		System.out.println(exampleIm.binning/exampleIm.resX);
		System.out.println(exampleIm.binning/exampleIm.resY);
		System.out.println(dz);
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
				
				pos.x=center.x*exampleIm.binning/exampleIm.resX;
				pos.y=center.y*exampleIm.binning/exampleIm.resY;
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
