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
package endrov.deconv.iterative;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DoubleAlgebra;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import endrov.deconv.Deconvolver2D;
import endrov.deconv.iterative.IterativeEnums.BoundaryType;
import endrov.deconv.iterative.IterativeEnums.PreconditionerType;
import endrov.deconv.iterative.IterativeEnums.ResizingType;
import endrov.deconv.iterative.preconditioner.DoublePreconditioner2D;
import endrov.deconv.iterative.preconditioner.FFTDoublePreconditioner2D;
import endrov.deconv.iterative.psf.DoublePSFMatrix2D;
import endrov.ev.Log;
import endrov.imageset.EvPixels;

/**
 * Abstract iterative deconvolver 2D.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public abstract class DoubleIterativeDeconvolver2D extends Deconvolver2D{

		public static void log(String s)
			{
			Log.printLog(s);
			
			}


    /**
     * Algebra.
     */
    protected static final DoubleAlgebra alg = DoubleAlgebra.DEFAULT;

    /**
     * Blurred image.
     */
    protected DoubleMatrix2D B;

    /**
     * Point Spread Function.
     */
    protected DoublePSFMatrix2D A;

    /**
     * Preconditioner.
     */
    protected DoublePreconditioner2D P;


    /**
     * Number of columns in the blurred image.
     */
    protected int bColumns;

    /**
     * Number of rows in the blurred image.
     */
    protected int bRows;

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

    /**
     * If true, then the restored image is displayed after each iteration.
     */
    protected boolean showIteration;

    /**
     * If true, then the convergence information is displayed after each
     * iteration.
     */
    protected boolean logConvergence;

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
    protected DoubleIterativeDeconvolver2D(String name, /*EvPixels ipB, EvPixels[][] imPSF,*/ PreconditionerType preconditioner, double preconditionerTol, BoundaryType boundary, ResizingType resizing, boolean useThreshold, double threshold, int maxIters,
            boolean showIteration, boolean logConvergence) {
            
            this.name=name;
        this.useThreshold = useThreshold;
        this.threshold = threshold;
        this.maxIters = maxIters;
        this.showIteration = showIteration;
        this.logConvergence = logConvergence;
        this.preconditionerTol=preconditionerTol;
        this.boundary=boundary;
        this.resizing=resizing;
        this.preconditioner=preconditioner;
        
        //later(ipB, imPSF);
    }
    
    
    public void later(EvPixels ipB, EvPixels[][] imPSF)
    	{
      this.bColumns = ipB.getWidth();
      this.bRows = ipB.getHeight();
      B = new DenseDoubleMatrix2D(this.bRows, this.bColumns);
      A = new DoublePSFMatrix2D(imPSF, boundary, resizing, new int[] { this.bRows, this.bColumns });
      DoubleCommon2D.assignPixelsToMatrix(B, ipB);
      switch (preconditioner) {
      case FFT:
          this.P = new FFTDoublePreconditioner2D(A, B, preconditionerTol);
          break;
      case NONE:
          this.P = null;
          break;
      default:
          throw new IllegalArgumentException("Unsupported preconditioner type.");
      }
    	
    	
    	}
    
    

    /**
     * Returns 2D preconditioner.
     * 
     * @return preconditioner
     */
    public DoublePreconditioner2D getPreconditioner() {
        return P;
    }

}
