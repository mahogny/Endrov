package endrov.gui;
/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */


import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Piecewise continuous histogram transform 
 * @author Johan Henriksson
 *
 */
public class HistogramTransform_notdone
	{

	/**
	 * From color, to color.
	 * Map is never inverted
	 */
	
	/**
	 * Never allowed to be empty
	 */
	public SortedMap<Double, Double> points=new TreeMap<Double, Double>();
	
	
	public HistogramTransform_notdone()
		{
		//Maybe separate mapping from the widget? can do later
		
		points.put(0.0,0.0);
		points.put(256.0, 256.0);
		
		
		}
	}
