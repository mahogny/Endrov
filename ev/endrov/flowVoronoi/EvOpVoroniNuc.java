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
import endrov.lineage.Lineage;
import endrov.lineage.LineageSelParticle;
import endrov.util.EvDecimal;
import endrov.util.MemoizeX;
import endrov.util.ProgressHandle;
import endrov.util.Tuple;

/**
 * Calculate Voronoi regions from particles - assign points to closest particles 
 * 
 * @author Johan Henriksson
 *
 */
public class EvOpVoroniNuc
	{
	private Lineage lin;
	
	public EvOpVoroniNuc(Lineage lin)
		{
		this.lin=lin;
		}

	//Voronoi for pixels: level sets would be perfect!
	
	public Tuple<Map<String, Integer>,Map<Integer,String>> getMaps(EvDecimal frame)
		{
		Map<String, Integer> mapTo=new HashMap<String, Integer>();
		Map<Integer, String> mapFrom=new HashMap<Integer, String>();
		
		Map<LineageSelParticle,Lineage.InterpolatedParticle> i=lin.interpolateParticles(frame);
		int count=1;
		for(Map.Entry<LineageSelParticle,Lineage.InterpolatedParticle> e:i.entrySet())
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
	
	
	public EvChannel exec(ProgressHandle progh, EvChannel ch)
		{
		EvChannel chout=new EvChannel();
		
		for(final EvDecimal frame:ch.getFrames())
			{
			EvStack oldstack=ch.getStack(progh, frame);
			final EvStack newstack=new EvStack(); 
			newstack.getMetaFrom(oldstack);
			chout.putStack(frame, newstack);
			
			final int w=oldstack.getWidth();
			final int h=oldstack.getHeight();
			
			final MemoizeX<List<PointList>> interLazy=new MemoizeX<List<PointList>>()
				{
				protected List<PointList> eval(ProgressHandle progh)
					{
					ProgressHandle.checkStop(progh);
					Map<LineageSelParticle,Lineage.InterpolatedParticle> inter=lin.interpolateParticles(frame);
					List<PointList> list=new ArrayList<PointList>();
					
					int count=1;
					for(Map.Entry<LineageSelParticle,Lineage.InterpolatedParticle> e:inter.entrySet())
						{
						if(e.getValue().isVisible())
							{
							Lineage.ParticlePos pos=e.getValue().pos;
							
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
					public EvPixels eval(ProgressHandle progh)
						{
						List<PointList> list=interLazy.get(progh);
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
				evim.io.dependsOn(interLazy);
				newstack.putInt(taz, evim);
				}
			
			}
		
		return chout;
		}
	
	
	}
