package endrov.flowVoronoi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector3d;

import endrov.imageset.EvChannel;
import endrov.imageset.EvIOImage;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;
import endrov.nuc.NucLineage;
import endrov.nuc.NucSel;
import endrov.util.EvDecimal;
import endrov.util.Memoize;
import endrov.util.Tuple;

/**
 * Calculate Voronoi regions from nuclei - assign points to closest nucleus 
 * 
 * @author Johan Henriksson
 *
 */
public class EvOpVoroniNuc
	{
	private NucLineage lin;
	
	public EvOpVoroniNuc(NucLineage lin)
		{
		this.lin=lin;
		}

	//Voronoi for pixels: level sets would be perfect!
	
	public Tuple<Map<String, Integer>,Map<Integer,String>> getMaps(EvDecimal frame)
		{
		Map<String, Integer> mapTo=new HashMap<String, Integer>();
		Map<Integer, String> mapFrom=new HashMap<Integer, String>();
		
		Map<NucSel,NucLineage.NucInterp> i=lin.getInterpNuc(frame);
		int count=1;
		for(Map.Entry<NucSel,NucLineage.NucInterp> e:i.entrySet())
			{
			if(e.getValue().isVisible())
				{
				mapTo.put(e.getKey().snd(), count);
				mapFrom.put(count, e.getKey().snd());
				count++;
				}
			}
		
		return Tuple.make(mapTo, mapFrom);
		}
	
	
	private static class PointList
		{
		double x,y,z;
		int id; 
		}
	
	
	public EvChannel exec(EvChannel ch)
		{
		EvChannel chout=new EvChannel();
		
		for(final EvDecimal frame:ch.getFrames())
			{
			EvStack oldstack=ch.getStack(frame);
			final EvStack newstack=new EvStack(); 
			newstack.getMetaFrom(oldstack);
			chout.putStack(frame, newstack);
			
			final int w=oldstack.getWidth();
			final int h=oldstack.getHeight();
			
			final Memoize<List<PointList>> interLazy=new Memoize<List<PointList>>()
				{
				protected List<PointList> eval()
					{
					Map<NucSel,NucLineage.NucInterp> inter=lin.getInterpNuc(frame);
					List<PointList> list=new ArrayList<PointList>();
					
					int count=1;
					for(Map.Entry<NucSel,NucLineage.NucInterp> e:inter.entrySet())
						{
						if(e.getValue().isVisible())
							{
							NucLineage.NucPos pos=e.getValue().pos;
							
							PointList c=new PointList();
							c.x=pos.x;
							c.y=pos.y;
							c.z=pos.z;
							c.id=count;
							list.add(c);
							
							count++;
							}
						}
					return list;
					}
				};
			
			//Lazily do all planes 
			for(int taz=0;taz<oldstack.getDepth();taz++)
				{
				final int az=taz;
				EvImage evim=new EvImage();
				evim.io=new EvIOImage()
					{
					public EvPixels loadJavaImage()
						{
						List<PointList> list=interLazy.get();
						EvPixels p=new EvPixels(EvPixelsType.INT, w, h);
						int[] arr=p.getArrayInt();

						if(!list.isEmpty()) //Id for nothing is 0, so just do not process if empty
							{
							for(int ay=0;ay<h;ay++)
								{
								for(int ax=0;ax<w;ax++)
									{
									Vector3d ww=newstack.transformImageWorld(new Vector3d(ax,ay,az));
									
									/*double wx=newstack.transformImageWorldX(ax);
									double wy=newstack.transformImageWorldY(ay);
									double wz=newstack.transformImageWorldZ(az);*/
									PointList closestId=null;
									double closestDist=0;
									
									//Some gridding may speed up this plenty
									for(PointList c:list)
										{
										double dx=c.x-ww.x;
										double dy=c.y-ww.y;
										double dz=c.z-ww.z;
										double dist2=dx*dx+dy*dy+dz*dz;
										if(dist2<closestDist)
											{
											closestId=c;
											closestDist=dist2;
											}
										}
									arr[ay*w+ax]=closestId.id;
									}
								}
							
							
							}
						return p;
						}
					};
				newstack.putInt(taz, evim);
				}
			
			}
		
		return chout;
		}
	
	
	}
