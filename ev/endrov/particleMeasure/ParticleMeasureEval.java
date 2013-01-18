package endrov.particleMeasure;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import endrov.imageset.EvChannel;
import endrov.imageset.EvStack;
import endrov.particleMeasure.ParticleMeasure.Frame;
import endrov.particleMeasure.ParticleMeasure.Well;
import endrov.particleMeasure.calc.MeasureProperty;
import endrov.util.EvDecimal;
import endrov.util.ProgressHandle;

/**
 * Functions to measure an image, and generate a particle measure lazily 
 * 
 * @author Johan Henriksson
 *
 */
public class ParticleMeasureEval
	{
	/**
	 * Prepare all lazy evaluations. Measures should have been decided by this point
	 */
	public static ParticleMeasure prepareEvaluate(
			final ProgressHandle progh, 
			String wellName, 
			EvChannel chValue, final EvChannel chMask, List<String> use)
		{
		ParticleMeasure pm=new ParticleMeasure();
		final Set<String> useMeasures=new HashSet<String>(use);

		//Figure out columns
		for(String s:useMeasures)
			for(String col:MeasureProperty.measures.get(s).getColumns())
				pm.addColumn(col);

		//Make a new well (any reason to write into an existing one?)
		Well well=new Well();
		pm.setWell(wellName, well);
		
		for(final EvDecimal frame:chValue.getFrames())
			{
			Frame info=new Frame();
			EvStack thestack=chValue.getStack(frame);
			
			final WeakReference<EvStack> weakStackValue=new WeakReference<EvStack>(thestack);
			final WeakReference<EvChannel> weakChMask=new WeakReference<EvChannel>(chMask);
			final WeakReference<Frame> weakInfo=new WeakReference<Frame>(info);
			
			info.registerLazyCalculation(
			new Runnable()
				{
				public void run()
					{
					//Run each measure
					for(String s:useMeasures)
						MeasureProperty.measures.get(s).analyze(progh, weakStackValue.get(), weakChMask.get().getStack(frame),weakInfo.get());
					}
				});
			
			well.setFrame(frame, info);
			}
		
		return pm;
		}
	

	}
