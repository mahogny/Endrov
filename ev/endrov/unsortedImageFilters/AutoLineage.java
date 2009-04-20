package endrov.unsortedImageFilters;

import java.util.*;

import javax.vecmath.Vector3d;


import endrov.basicWindow.BasicWindow;
import endrov.data.EvData;
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
 * @author Johan Henriksson
 *
 */
public class AutoLineage
	{
	public static void run()
		{
		System.out.println("2");
		
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
		
		
		EvImage exampleIm=imageset.getChannel(channelName).imageLoader.firstEntry().getValue().firstEntry().getValue();
		
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
