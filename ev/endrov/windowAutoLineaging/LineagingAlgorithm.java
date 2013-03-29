/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowAutoLineaging;

import java.util.Vector;

import javax.swing.JComponent;

import endrov.data.EvContainer;
import endrov.typeLineage.Lineage;
import endrov.util.ProgressHandle;
import endrov.util.math.EvDecimal;

/**
 * General interface to automatic lineaging algorithm. 
 * 
 * @author Johan Henriksson
 *
 */
public interface LineagingAlgorithm
	{
	
	
	
	/**
	 * Called by algorithm for user feedback
	 * @author Johan Henriksson
	 */
	public interface LineageSession
		{
		public void finishedAndNowAtFrame(EvDecimal f);
		public EvDecimal getStartFrame();
		public Lineage getLineage();
		public EvContainer getEvContainer();
		}
	
	/**
	 * Instantiation and description of algorithms
	 * @author Johan Henriksson
	 *
	 */
	public static abstract class LineageAlgorithmDef
		{
		public abstract String getName();
		public abstract LineagingAlgorithm getInstance(); 
		
		public String toString()
			{
			return getName();
			}
			
		
		public static Vector<LineageAlgorithmDef> listAlgorithms=new Vector<LineageAlgorithmDef>();

		public static void registerAlgorithm(LineageAlgorithmDef def)
			{
			listAlgorithms.add(def);
			}

		}
	
	/**
	 * Get component to show in GUI
	 */
	public JComponent getComponent();

	/**
	 * User-enforced stop of lineaging algorithm thread
	 */
	public void setStopping(boolean b);
	
	/**
	 * Run algorithm
	 */
	public void run(ProgressHandle ph, LineageSession listener);
	
	
	public void dataChangedEvent();
	
	}
