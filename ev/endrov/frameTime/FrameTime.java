/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.frameTime;

import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JMenu;

import endrov.data.*;
import endrov.util.EvDecimal;
import endrov.util.Tuple;

import org.jdom.*;


public class FrameTime extends EvObject
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	public final static ImageIcon icon=new ImageIcon(FrameTime.class.getResource("iconWindow.png"));
	
	private static final String metaType="frametime";
	
	private static final String metaElement="ft";
	

	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	//(Frame,Time), the real list
	public final Vector<Tuple<EvDecimal,EvDecimal>> list=new Vector<Tuple<EvDecimal,EvDecimal>>();

	//Maps in two directions, cached versions of list
	public final SortedMap<EvDecimal, EvDecimal> mapFrame2time=new TreeMap<EvDecimal, EvDecimal>();
	public final SortedMap<EvDecimal, EvDecimal> mapTime2Frame=new TreeMap<EvDecimal, EvDecimal>();
	
	public String getMetaTypeDesc()
		{
		return metaType;
		}
	
	/**
	 * Save down data
	 */
	public String saveMetadata(Element e)
		{
		for(Tuple<EvDecimal,EvDecimal> p:list)
			{
			Element f=new Element(metaElement);
			f.setAttribute("frame", ""+p.fst());
			f.setAttribute("time",  ""+p.snd());
			e.addContent(f);
			}
		
		return metaType;
		}

	public void loadMetadata(Element e)
		{
		for(Object oframetime:e.getChildren())
			{
			Element e2=(Element)oframetime;
			EvDecimal frame=new EvDecimal(e2.getAttribute("frame").getValue());
			EvDecimal frametime=new EvDecimal(e2.getAttribute("time").getValue());
			list.add(new Tuple<EvDecimal,EvDecimal>(frame,frametime));
			}
		updateMaps();
		}


	public void buildMetamenu(JMenu menu)
		{
		}

	
	/**
	 * Add new frame/frametime to this object. Adds to maps so no need to update them.
	 */
	public void add(EvDecimal frame, EvDecimal time)
		{
		Tuple<EvDecimal,EvDecimal> p=new Tuple<EvDecimal,EvDecimal>(frame, time);
		list.add(p);
		
		mapFrame2time.put(p.fst(), p.snd());
		mapTime2Frame.put(p.snd(), p.fst());
		}
	

	public int getNumPoints()
		{
		return list.size();
		}

	/**
	 * Interpolate x given x->yTuple<EvDecimal,EvDecimal>. Richardson extrapolate
	 */
	private EvDecimal interpolate(SortedMap<EvDecimal, EvDecimal> map, EvDecimal x)
		{
		EvDecimal preciseY=map.get(x);
		if(preciseY!=null)
			return preciseY;
		
		//Actually, can assume linear but passing through this point if size==1
		if(map.isEmpty())
			return x;
		else if(map.size()==1)
			{
			//y=kx+m. assume k=1. then m=p.y/p.x. y=x+p.y/p.x
			EvDecimal px=map.firstKey();
			EvDecimal py=map.get(px);
			return x.add(py.divide(px));
			}
		else
			{
			SortedMap<EvDecimal, EvDecimal> hmap=map.headMap(x);
			SortedMap<EvDecimal, EvDecimal> tmap=map.tailMap(x);
			
			if(hmap.isEmpty())
				{
				//Take the two first points and extrapolate
				Iterator<Map.Entry<EvDecimal, EvDecimal>> it=map.entrySet().iterator();
				Map.Entry<EvDecimal, EvDecimal> first=it.next();
				Map.Entry<EvDecimal, EvDecimal> second=it.next();
				return linInterpolate(first.getKey(),second.getKey(),first.getValue(),second.getValue(),x);
				}
			else if(tmap.isEmpty())
				{
				//Take the two last points and extrapolate
				EvDecimal lastX=map.lastKey();
				EvDecimal secondX=map.headMap(lastX).lastKey();
				EvDecimal lastY=map.get(lastX);
				EvDecimal secondY=map.get(secondX);
				return linInterpolate(lastX, secondX, lastY, secondY, x);
				}
			else
				{
				EvDecimal lastX=hmap.lastKey();
				EvDecimal nextX=tmap.firstKey();
				EvDecimal lastY=hmap.get(lastX);
				EvDecimal nextY=tmap.get(nextX);
				return linInterpolate(lastX, nextX, lastY, nextY, x);
				}
			}
		}
	
	/**
	 * Linear interpolation
	 */
	private EvDecimal linInterpolate(EvDecimal lastX,EvDecimal nextX, EvDecimal lastY, EvDecimal nextY, EvDecimal x)
		{
		EvDecimal frac=x.subtract(lastX).divide(nextX.subtract(lastX));
		EvDecimal frac1=EvDecimal.ONE.subtract(frac);
		return frac1.multiply(lastY).add(
				frac.multiply(nextY));
		}
	
	
	/**
	 * Figure out time from frame
	 */
	public EvDecimal interpolateTime(EvDecimal frame)
		{
		return interpolate(mapFrame2time, frame);
		}
	
	/**
	 * Figure out time from frame
	 */
	public EvDecimal interpolateFrame(EvDecimal time)
		{
		return interpolate(mapTime2Frame, time);
		}
	
	/**
	 * Update cached maps. Has to be done if points list is manually accessed
	 */
	public void updateMaps()
		{
		mapFrame2time.clear();
		mapTime2Frame.clear();
		for(Tuple<EvDecimal,EvDecimal> p:list)
			{
			mapFrame2time.put(p.fst(), p.snd());
			mapTime2Frame.put(p.snd(), p.fst());
			}
		//TODO emit changed
		}

	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvData.supportedMetadataFormats.put(metaType,FrameTime.class);
		}

	}
