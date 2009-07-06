package endrov.nucAutoJH;

import javax.swing.JComponent;
import javax.swing.JPanel;

import endrov.imageset.EvChannel;
import endrov.nuc.NucLineage;
import endrov.nucImage.LineagingAlgorithm;
import endrov.nucImage.LineagingAlgorithm.LineageAlgorithmDef;
import endrov.util.EvDecimal;

/**
 * Autolineage algorithm
 * 
 * Meant to be used with his::rfp or equivalent marker
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class AutolineageJH1 extends LineageAlgorithmDef
	{
	public static void initPlugin() {}
	static
		{
		LineageAlgorithmDef.listAlgorithms.add(new AutolineageJH1());
		}
	
	@Override
	public LineagingAlgorithm getInstance()
		{
		return new Algo();
		}

	@Override
	public String getName()
		{
		return "JH1";
		}

	
	
	private static class Algo implements LineagingAlgorithm
		{
		public JComponent getComponent()
			{
			JPanel p=new JPanel();
			
			//Move channel selection to here
			
			
			return p;
			}
		
		
		public void init(EvChannel chan, NucLineage lin)
			{
			// TODO Auto-generated method stub
			
			}

		public void run(EvDecimal startFrame, LineageListener listener)
			{
			// TODO Auto-generated method stub
			
			}

		public void stop()
			{
			// TODO Auto-generated method stub
			
			}
		
		}
	}
