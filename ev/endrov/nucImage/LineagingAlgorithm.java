package endrov.nucImage;

import java.util.Vector;

import javax.swing.JComponent;

import endrov.data.EvContainer;
import endrov.nuc.NucLineage;
import endrov.util.EvDecimal;

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
		public NucLineage getLineage();
		public EvContainer getContainer();
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
	public void run(LineageSession listener);
	
	
	public void dataChangedEvent();
	
	}
