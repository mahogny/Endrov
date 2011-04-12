/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.nuc.integrate;

import java.util.*;



import endrov.imageset.*;
import endrov.nuc.*;
import endrov.util.*;

/**
 * All integrations are done at the same time to reduce disk I/O. Images not
 * needed are discarded through lazy evaluation. Background is subtracted. This
 * only affects the first frame, but sets a proper 0. COULD put background into
 * first frame of correction instead. Is this better? The cubemap contains too
 * much data for the lineage window. 20x20x20=8000 tracks, impossible. also EATS
 * space, especially if stored as XML. So, it has to be stored as images.
 * Originally as channel "mod-GFP". Overlap is not possible due to
 * reorientation. Frame-time remap will not be done until assembly.
 * 
 * @author Johan Henriksson
 */
public class IntegrateExp
	{

	/**
	 * Integrator of expression. Integrating every type at the same time saves a lot of I/O
	 * 
	 * @author Johan Henriksson
	 */
	public interface Integrator
		{
		public void integrateStackStart(IntegrateExp images);
		public void integrateImage(IntegrateExp images);
		public void integrateStackDone(IntegrateExp images);
		}
	
	public interface IntegratorCallback
		{
		/**
		 * Return true if to continue
		 */
		public boolean status(IntegrateExp integrator);
		public void fail(Exception e);
		}

	public static NucLineage refLin = null;
	public EvDecimal frame;
	public int curZint;
	public EvStack stack;
	public EvImage im;
	public EvPixels pixels;
	public int[] pixelsLine;
	public String expName;
	public EvChannel ch;
	public Imageset imset;

	
	
	/**
	 * Lazily load images
	 */
	public void ensureImageLoaded()
		{
		if (pixels==null)
			{
			pixels = im.getPixels().getReadOnly(EvPixelsType.INT);
			pixelsLine = pixels.getArrayInt();
			}
		}

	public IntegrateExp(Imageset imset, EvChannel ch, String expName)
		{
		this.imset=imset;
		this.expName = expName;
		this.ch=ch;
		}

	/**
	 * Run all integrators
	 */
	public void integrateAll(IntegratorCallback callback, Collection<Integrator> ints)
		{
		integrateAll(callback, ints.toArray(new Integrator[] {}));
		}

	/**
	 * Run all integrators
	 */
	public void integrateAll(IntegratorCallback callback, Integrator... ints)
		{
		
		try
			{
			// For all frames
			System.out.println("num frames: "+ch.getFrames().size());

			//EvDecimal firstframe = ch.getFirstFrame();
			EvDecimal lastFrame = ch.getLastFrame();

			//lastFrame=new EvDecimal("14400");  ///temp!!!
			
			if(refLin!=null)
				{
				NucLineage.Nuc nuc=refLin.nuc.get("lastframe");
				if(nuc!=null)
					{
					lastFrame=nuc.pos.firstKey();
					}
				}
			
			
			for (EvDecimal frame : ch.getFrames())
				if(frame.lessEqual(lastFrame))
					{
					this.frame = frame;

					if(callback!=null && !callback.status(this))
						return;

					for (Integrator i : ints)
						i.integrateStackStart(this);

					// For all z
					stack = ch.getStack(frame);
					EvImage[] imArr = stack.getImages();
					for (int az = 0; az<imArr.length; az++)
						{
						// Load images lazily
						curZint = az;
						im = imArr[az];
						pixels = null;

						for (Integrator i : ints)
							i.integrateImage(this);
						}

					for (Integrator i : ints)
						i.integrateStackDone(this);

					}
			}
		catch (Exception e)
			{
			e.printStackTrace();
			callback.fail(e);
			}

		}
	
	
	///////////////////////////////////////////////
	///////////////////////////////////////////////
	///////////////////////////////////////////////

	/**
	 * Integrate single-cell for one recording
	 */
	public static void integrateSingleCell(NucLineage lin, Imageset imset, EvChannel ch, String expName, IntegratorCallback cb, boolean useNucleiRadius)
		{
		int numSubDiv = 20;
	
		// Decide on integrators
		LinkedList<Integrator> integrators = new LinkedList<Integrator>();
	
		// boolean hasShell=!data.getIdObjects(Shell.class).isEmpty();
	
		//Order of integrators matters!
		IntegrateExp integrator = new IntegrateExp(imset, ch, expName);
	
		// AP-level expression. Needed because it generates background correction information.
		// I think it would be nice to have this separate!
		IntegratorSliceAP intAP = new IntegratorSliceAP(integrator, numSubDiv, null);
		integrators.add(intAP);
	
		// Cell level expression if there is a lineage
		IntegratorCellClosest intC = new IntegratorCellClosest(integrator, lin, intAP.bg, useNucleiRadius);
		integrators.add(intC);
	
		// Run integrators
		integrator.integrateAll(cb, integrators);
	
		// Use common correction factors for exposure
		intAP.done(integrator, null);
		intC.done(integrator, intAP.correctedExposure);
		}

	}
