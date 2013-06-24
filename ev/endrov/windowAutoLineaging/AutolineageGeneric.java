/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.windowAutoLineaging;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import endrov.data.EvContainer;
import endrov.gui.EvSwingUtil;
import endrov.gui.component.EvComboObjectOne;
import endrov.typeImageset.EvChannel;
import endrov.typeLineage.Lineage;
import endrov.util.*;
import endrov.util.math.EvDecimal;
import endrov.windowAutoLineaging.LineagingAlgorithm;
import endrov.windowAutoLineaging.LineagingAlgorithm.LineageAlgorithmDef;

/**
 * General particle tracking algorithm. Assumes the segmentation is already done
 * 
 * @author Johan Henriksson
 *
 */
public class AutolineageGeneric extends LineageAlgorithmDef
	{	
	@Override
	public LineagingAlgorithm getInstance()
		{
		return new Algo();
		}

	@Override
	public String getName()
		{
		return "Generic tracker";
		}

	
	/**
	 * Instance of the algorithm
	 * @author Johan Henriksson
	 *
	 */
	public static class Algo implements LineagingAlgorithm
		{
		
		
		private EvComboObjectOne<EvChannel> comboChannel=new EvComboObjectOne<EvChannel>(new EvChannel(), false, false);
		
				private boolean isStopping=false;
		
		
		private JCheckBox cbAllowSplit=new JCheckBox("Allow division");
		private JCheckBox cbAllowFusion=new JCheckBox("Allow fusion");
		
		/**
		 * Set if to stop the algorithm prematurely
		 */
		public void setStopping(boolean b)
			{
			isStopping=b;
			}
		
		/**
		 * Get custom GUI component
		 */
		public JComponent getComponent()
			{
			JComponent p=EvSwingUtil.layoutTableCompactWide(
					new JLabel("Channel"),comboChannel
					);

			
			
			return EvSwingUtil.layoutCompactVertical(
					p,
					//,bReassChildren,bReestParameters,
					cbAllowSplit,
					cbAllowFusion
					);
			}
		
		
		
		/**
		 * Lineage one frame
		 */
		public void run(final ProgressHandle ph, LineageSession session)
			{
			EvDecimal frame=session.getStartFrame();
			Lineage lin=session.getLineage();
			EvChannel channelHis=comboChannel.getSelectedObject();
			EvContainer parentContainer=session.getEvContainer();
			
			
				
				//Prepare to do next frame
//				session.finishedAndNowAtFrame(channelHis.closestFrameAfter(frame));
	//			}
			}
		
		

		public void dataChangedEvent()
			{
			comboChannel.updateList();
			}
		
		}
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		LineageAlgorithmDef.listAlgorithms.add(new AutolineageGeneric());
		}

	}
