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
package endrov.deconvolution.iterative;

import cern.colt.matrix.tdouble.algo.DoubleAlgebra;
import endrov.deconvolution.Deconvolver3D;
import endrov.deconvolution.iterative.IterativeEnums.BoundaryType;
import endrov.deconvolution.iterative.IterativeEnums.PreconditionerType;
import endrov.deconvolution.iterative.IterativeEnums.ResizingType;
import endrov.imageset.EvStack;

/**
 * Abstract iterative deconvolver 3D.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public abstract class DoubleIterativeDeconvolver3D extends Deconvolver3D {



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

    /**
     * If true, then the convergence information is displayed after each
     * iteration.
     */
    protected boolean logConvergence;

    /**
     * The name of a deconvolution algorithm.
     */
    protected String name;

    /**
     * Creates a new instance of AbstractDoubleIterativeDeconvolver3D
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
    protected DoubleIterativeDeconvolver3D(String name, EvStack[][][] imPSF, PreconditionerType preconditioner, double preconditionerTol, BoundaryType boundary, ResizingType resizing, boolean useThreshold, double threshold, int maxIters,
            boolean logConvergence) {
        this.name = name;
        
        this.useThreshold = useThreshold;
        this.threshold = threshold;
        this.maxIters = maxIters;
        this.logConvergence = logConvergence;
        this.preconditioner=preconditioner;
        this.preconditionerTol=preconditionerTol;
        this.boundary=boundary;
        this.resizing=resizing;
        this.imPSF=imPSF;

    }

    protected final PreconditionerType preconditioner;
    protected final double preconditionerTol;
    protected final BoundaryType boundary;
    protected final ResizingType resizing;
    protected final EvStack[][][] imPSF;
    
    /*
    public void later(EvStack imB)
    	{
      EvStack isB=imB;
      EvStack ipB = imB;
      this.bSlices = isB.getDepth();
      this.bColumns = ipB.getWidth();
      this.bRows = ipB.getHeight();
      B = new DenseDoubleMatrix3D(this.bSlices, this.bRows, this.bColumns);
      A = new DoublePSFMatrix3D(imPSF, boundary, resizing, new int[] { this.bSlices, this.bRows, this.bColumns });
      DoubleCommon3D.assignPixelsToMatrix(isB, B);
      switch (preconditioner) {
      case FFT:
          this.P = new FFTDoublePreconditioner3D(A, B, preconditionerTol);
          break;
      case NONE:
          this.P = null;
          break;
      default:
          throw new IllegalArgumentException("Unsupported preconditioner type.");
      }
    	}
    */
    
    /**
     * Returns 3D preconditioner.
     * 
     * @return preconditioner
     */
    /*
    public DoublePreconditioner3D getPreconditioner() {
        return P;
    }
    */
    
   
    
}
