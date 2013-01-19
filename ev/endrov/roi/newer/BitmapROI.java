/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.roi.newer;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdom.Element;

import endrov.data.EvContainer;
import endrov.data.EvObject;
import endrov.roi.LineIterator;
import endrov.roi.ROI;
import endrov.typeImageset.EvChannel;
import endrov.typeImageset.EvImagePlane;
import endrov.typeImageset.EvStack;
import endrov.util.ProgressHandle;
import endrov.util.math.EvDecimal;

/**
 * ROI based on a bitmap
 * 
 * @author Johan Henriksson
 *
 */
public class BitmapROI extends ROI
	{
	private static final String metaType="ROI_bitmap";
	

	//Should maybe ROIs only refer to a specific channel and not an imageset?
	
	public EvChannel bitmap;
	
	/**
	 * The value in the bitmap that is This ROI
	 */
	public int thisValue;

	@Override
	public LineIterator getLineIterator(ProgressHandle progh, EvStack stack, EvImagePlane im,
			String channel, EvDecimal frame, double z)
		{
		// TODO Auto-generated method stub
		return null;
		}

	@Override
	public Handle getPlacementHandle1(){return null;}
	@Override
	public Handle getPlacementHandle2(){return null;}
	@Override
	public void initPlacement(String chan, EvDecimal frame, EvDecimal z){}
	@Override
	public Handle[] getHandles(){return new Handle[]{};}


	@Override
	public String getROIDesc()
		{
		return "Bitmap ROI";
		}

	@Override
	public JComponent getROIWidget()
		{
		return new JPanel();
		}

	/*
	@Override
	public Set<Integer> getSlice(Imageset rec, String channel, EvDecimal frame)
		{
		return bitmap.imageLoader.get(frame).keySet();
		}*/

	@Override
	public boolean imageInRange(String channel, EvDecimal frame, double z)
		{
		if(bitmap.getFrames().contains(frame))
			{
			return true;
			
			}
		//TODO z?
		return false;
		}
	

	
	@Override
	public Set<String> getChannels(EvContainer rec)
		{
		//TODO
		return new HashSet<String>(rec.getIdObjects(EvChannel.class).keySet());
		}

	@Override
	public Set<EvDecimal> getFrames(EvContainer rec, String channel)
		{
		return bitmap.getFrames();
		}


	@Override
	public void loadMetadata(Element e)
		{
		}

	@Override
	public String saveMetadata(Element e)
		{
		return metaType;
		}

	@Override
	public boolean pointInRange(String channel,	EvDecimal frame, double x, double y, double z)
		{
		// TODO Auto-generated method stub
		return false;
		}

	@Override
	public EvObject cloneEvObject()
		{
		return cloneUsingSerialize();
		}

	}
