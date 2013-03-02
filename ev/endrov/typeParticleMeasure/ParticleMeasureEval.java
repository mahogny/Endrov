package endrov.typeParticleMeasure;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import endrov.typeImageset.EvChannel;
import endrov.typeImageset.EvStack;
import endrov.typeParticleMeasure.ParticleMeasure.Frame;
import endrov.typeParticleMeasure.ParticleMeasure.Well;
import endrov.typeParticleMeasure.calc.MeasureProperty;
import endrov.typeParticleMeasure.calc.MeasurePropertyType;
import endrov.util.ProgressHandle;
import endrov.util.math.EvDecimal;

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
				pm.addParticleColumn(col);

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
						{
						MeasurePropertyType measure=MeasureProperty.measures.get(s);
						if(measure==null)
							throw new RuntimeException("The measure is null - implementation problem");
						EvChannel chMask=weakChMask.get();
						if(chMask==null)
							throw new RuntimeException("The channel mask is null - implementation problem");
						measure.analyze(progh, weakStackValue.get(), chMask.getStack(frame),weakInfo.get());
						}
					}
				});
			
			well.setFrame(frame, info);
			}
		
		return pm;
		}
	

	}
