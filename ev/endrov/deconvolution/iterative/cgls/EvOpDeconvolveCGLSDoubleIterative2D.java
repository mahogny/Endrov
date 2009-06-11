/*
 *  Copyright (C) 2008-2009 Piotr Wendykier, Johan Henriksson
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
package endrov.deconvolution.iterative.cgls;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import cern.jet.math.tdouble.DoubleFunctions;
import endrov.deconvolution.iterative.DoubleCommon2D;
import endrov.deconvolution.iterative.DoubleIterativeDeconvolver2D;
import endrov.deconvolution.iterative.IterativeEnums.BoundaryType;
import endrov.deconvolution.iterative.IterativeEnums.PreconditionerType;
import endrov.deconvolution.iterative.IterativeEnums.ResizingType;
import endrov.deconvolution.iterative.preconditioner.DoublePreconditioner2D;
import endrov.deconvolution.iterative.preconditioner.FFTDoublePreconditioner2D;
import endrov.deconvolution.iterative.psf.DoublePSFMatrix2D;
import endrov.imageset.EvPixels;

/**
 * Conjugate Gradient for Least Squares 2D.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class EvOpDeconvolveCGLSDoubleIterative2D extends DoubleIterativeDeconvolver2D {

    /**
     * If true, then the stopping tolerance is computed automatically.
     */
    protected final boolean autoStoppingTol;

    /**
     * Stopping tolerance.
     */
    protected double stoppingTol;

    /**
     * Creates a new instance of CGLSDoubleIterativeDeconvolver2D
     * 
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
     * @param maxIters
     *            maximal number of iterations
     * @param showIteration
     *            if true, then the restored image is displayed after each
     *            iteration
     * @param options
     *            CGLS options
     */
    public EvOpDeconvolveCGLSDoubleIterative2D(EvPixels[][] imPSF, PreconditionerType preconditioner, double preconditionerTol, BoundaryType boundary, ResizingType resizing, int maxIters, CGLSOptions options) {
        super("CGLS", imPSF, preconditioner, preconditionerTol, boundary, resizing, options.getUseThreshold(), options.getThreshold(), maxIters);
        this.autoStoppingTol = options.getAutoStoppingTol();
        this.stoppingTol = options.getStoppingTol();
        //laterImage(imB, imPSF);

    }

    public EvPixels internalDeconvolve(EvPixels ipB) {
    
	    DoublePreconditioner2D P;
	    
	    int bColumns = ipB.getWidth();
	    int bRows = ipB.getHeight();
	    DoubleMatrix2D B = new DenseDoubleMatrix2D(bRows, bColumns);
	    DoublePSFMatrix2D A = new DoublePSFMatrix2D(imPSF, boundary, resizing, new int[] { bRows, bColumns });
	    DoubleCommon2D.assignPixelsToMatrix(B, ipB);
	    switch (preconditioner) 
	      {
	      case FFT:
	          P = new FFTDoublePreconditioner2D(A, B, preconditionerTol);
	          break;
	      case NONE:
	          P = null;
	          break;
	      default:
	          throw new IllegalArgumentException("Unsupported preconditioner type.");
	      }
	    
    ////
    
        DoubleMatrix2D p, q, r, s;
        double alpha;
        double beta;
        double gamma;
        double oldgamma = 0;
        double nq;
        double rnrm;

        if (autoStoppingTol) {
            stoppingTol = DoubleCommon2D.sqrteps * alg.vectorNorm2(A.times(B, true));
        }
        s = A.times(B, false);
        s.assign(B, DoubleFunctions.plusMultFirst(-1));
        r = A.times(s, true);
        rnrm = alg.vectorNorm2(r);
        if (P != null) {
            r = P.solve(r, true);
            gamma = alg.vectorNorm2(r);
            gamma *= gamma;
        } else {
            gamma = rnrm;
            gamma *= gamma;
        }
        //EvPixels ip=new EvPixels(EvPixels.TYPE_DOUBLE,bColumns,bRows);
        //ImagePlus imX = null;
        //FloatProcessor ip = new FloatProcessor(bColumns, bRows);
        /*
        if (showIteration == true) {
            DoubleCommon2D.assignPixelsToProcessor(ip, B, cmY);
            imX = new ImagePlus("(deblurred)", ip);
            imX.show();
        }
        */
        p = r.copy();
        int k;
        for (k = 0; k < maxIters; k++) {
            if (rnrm <= stoppingTol) {
                log("CGLS converged after " + k + "iterations.");
                break;
            }
            log(name + "iteration: " + (k + 1) + "/" + maxIters);
            if (k >= 1) {
                beta = gamma / oldgamma;
                p.assign(r, DoubleFunctions.plusMultFirst(beta));
            }
            if (P != null) {
                r = P.solve(p, false);
                q = A.times(r, false);
            } else {
                q = A.times(p, false);
            }
            nq = alg.vectorNorm2(q);
            nq = nq * nq;
            alpha = gamma / nq;
            if (P != null) {
                B.assign(r, DoubleFunctions.plusMultSecond(alpha));
            } else {
                B.assign(p, DoubleFunctions.plusMultSecond(alpha));
            }
            s.assign(q, DoubleFunctions.plusMultSecond(-alpha));
            r = A.times(s, true);
            rnrm = alg.vectorNorm2(r);
            if (P != null) {
                r = P.solve(r, true);
                oldgamma = gamma;
                gamma = alg.vectorNorm2(r);
                gamma *= gamma;
            } else {
                oldgamma = gamma;
                gamma = rnrm;
                gamma *= gamma;
            }
            //if (logConvergence) 
                log((k + 1) + ".  Norm of the residual = " + rnrm);
            
            /*
            if (showIteration == true) {
                if (useThreshold) {
                    DoubleCommon2D.assignPixelsToProcessor(ip, B, cmY, threshold);
                } else {
                    DoubleCommon2D.assignPixelsToProcessor(ip, B, cmY);
                }
                imX.updateAndDraw();
            }
            */
        }
        if (/*logConvergence &&*/ k == maxIters) {
            log("CGLS didn't converge. Reason: maximum number of iterations performed.");
        }
            if (useThreshold) {
                return DoubleCommon2D.assignPixelsToProcessor(B, threshold);
            } else {
                return DoubleCommon2D.assignPixelsToProcessor(bRows, bColumns, B);
            }
            //return ip;
    }

}
