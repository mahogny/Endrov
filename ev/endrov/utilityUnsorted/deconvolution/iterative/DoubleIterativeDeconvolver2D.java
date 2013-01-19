/*
 *  Copyright (C) 2008-2009 Piotr Wendykier
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package endrov.utilityUnsorted.deconvolution.iterative;

import cern.colt.matrix.tdouble.algo.DoubleAlgebra;
import endrov.core.log.EvLog;
import endrov.imageset.EvPixels;
import endrov.utilityUnsorted.deconvolution.Deconvolver2D;
import endrov.utilityUnsorted.deconvolution.iterative.IterativeEnums.BoundaryType;
import endrov.utilityUnsorted.deconvolution.iterative.IterativeEnums.PreconditionerType;
import endrov.utilityUnsorted.deconvolution.iterative.IterativeEnums.ResizingType;

/**
 * Abstract iterative deconvolver 2D.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public abstract class DoubleIterativeDeconvolver2D extends Deconvolver2D{

		public static void log(String s)
			{
			EvLog.printLog(s);
			
			}


    /**
     * Algebra.
     */
    protected static final DoubleAlgebra alg = DoubleAlgebra.DEFAULT;

    /**
     * Maximal number of iterations.
     */
    protected int maxIters;

    /**
     * If true, then the thresholding is performed.
     */
    protected boolean useThreshold;

    /**
     * The smallest nonnegative value assigned to the restored image.
     */
    protected double threshold;


    protected EvPixels[][] imPSF;

    /**
     * The name of a deconvolution algorithm.
     */
    protected final String name;

    protected final double preconditionerTol;
    
    protected final BoundaryType boundary;
    
    protected final ResizingType resizing;
    
    protected final PreconditionerType preconditioner;
    /**
     * Creates a new instance of AbstractDoubleIterativeDeconvolver2D
     * 
     * @param name
     *            name of a deconvolution algorithm
     * @param imB
     *            blurred image
     * @param imPSF
     *            Point Spread Function
     * @param preconditioner
     *            type of a preconditioner
     * @param preconditionerTol
     *            tolerance for the preconditioner
     * @param boundary
     *            type of boundary conditions
     * @param resizing
     *            type of resizing
     * @param output
     *            type of the output image
     * @param useThreshold
     *            if true, then the thresholding is performed
     * @param threshold
     *            the smallest nonnegative value assigned to the restored image
     * @param maxIters
     *            maximal number of iterations
     * @param showIteration
     *            if true, then the restored image is displayed after each
     *            iteration
     * @param logConvergence
     *            if true, then the convergence information is displayed after
     *            each iteration
     */
    protected DoubleIterativeDeconvolver2D(String name, EvPixels[][] imPSF, PreconditionerType preconditioner, double preconditionerTol, BoundaryType boundary, ResizingType resizing, boolean useThreshold, double threshold, int maxIters) 
    	{
    	this.name=name;
    	this.useThreshold = useThreshold;
    	this.threshold = threshold;
    	this.maxIters = maxIters;
    	this.preconditionerTol=preconditionerTol;
    	this.boundary=boundary;
    	this.resizing=resizing;
    	this.preconditioner=preconditioner;
    	this.imPSF=imPSF;
    	}

}
