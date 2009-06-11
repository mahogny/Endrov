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
package endrov.deconvolution.iterative.mrnsd;

import cern.colt.list.tdouble.DoubleArrayList;
import cern.colt.list.tint.IntArrayList;
import cern.colt.matrix.tdouble.DoubleMatrix3D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix3D;
import cern.jet.math.tdouble.DoubleFunctions;
import endrov.deconvolution.DeconvPixelsStack;
import endrov.deconvolution.iterative.DoubleCommon2D;
import endrov.deconvolution.iterative.DoubleCommon3D;
import endrov.deconvolution.iterative.DoubleIterativeDeconvolver3D;
import endrov.deconvolution.iterative.IterativeEnums.BoundaryType;
import endrov.deconvolution.iterative.IterativeEnums.PreconditionerType;
import endrov.deconvolution.iterative.IterativeEnums.ResizingType;
import endrov.deconvolution.iterative.preconditioner.DoublePreconditioner3D;
import endrov.deconvolution.iterative.preconditioner.FFTDoublePreconditioner3D;
import endrov.deconvolution.iterative.psf.DoublePSFMatrix3D;
import endrov.imageset.EvStack;

/**
 * Modified Residual Norm Steepest Descent 3D. This is a nonnegatively
 * constrained steepest descent method.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class EvOpDeconvolveMRNSDDoubleIterative3D extends DoubleIterativeDeconvolver3D {

    /**
     * If true, then the stopping tolerance is computed automatically.
     */
    protected boolean autoStoppingTol;

    /**
     * Stopping tolerance.
     */
    protected double stoppingTol;

    /**
     * Creates a new instance of MRNSDDoubleIterativeDeconvolver2D
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
     *            MRNSD options
     */
    public EvOpDeconvolveMRNSDDoubleIterative3D(EvStack imB, EvStack[][][] imPSF, PreconditionerType preconditioner, double preconditionerTol, BoundaryType boundary, ResizingType resizing, int maxIters, boolean showIteration, MRNSDOptions options) {
        super("MRNSD", imPSF, preconditioner, preconditionerTol, boundary, resizing, options.getUseThreshold(), options.getThreshold(), maxIters, options.getLogConvergence());
        this.autoStoppingTol = options.getAutoStoppingTol();
        this.stoppingTol = options.getStoppingTol();
    }

    public DeconvPixelsStack internalDeconvolve(EvStack imB) {

				//Set up data
		    EvStack isB=imB;
		    EvStack ipB = imB;
		    int bSlices = isB.getDepth();
		    int bColumns = ipB.getWidth();
		    int bRows = ipB.getHeight();
		    DoubleMatrix3D B = new DenseDoubleMatrix3D(bSlices, bRows, bColumns);
		    DoublePSFMatrix3D A = new DoublePSFMatrix3D(imPSF, boundary, resizing, new int[] { bSlices, bRows, bColumns });
		    DoubleCommon3D.assignPixelsToMatrix(isB, B);
		    DoublePreconditioner3D P;
		    switch (preconditioner) {
		    case FFT:
		        P = new FFTDoublePreconditioner3D(A, B, preconditionerTol);
		        break;
		    case NONE:
		        P = null;
		        break;
		    default:
		        throw new IllegalArgumentException("Unsupported preconditioner type.");
		    }
		
		
				//Run algorithm
        double alpha;
        double gamma;
        double theta;
        double rnrm;
        DoubleMatrix3D r, s, u, w;
        IntArrayList sliceList, rowList, columnList;
        DoubleArrayList valueList;
        double tau = DoubleCommon2D.sqrteps;
        double sigsq = tau;
        double[] minAndLoc = B.getMinLocation();
        double minB = minAndLoc[0];
        if (minB < 0) {
            B.assign(DoubleFunctions.plus(-minB + sigsq));
        }

        if (autoStoppingTol) {
            stoppingTol = DoubleCommon2D.sqrteps * alg.vectorNorm2(A.times(B, true));
        }
        r = A.times(B, false);
        r.assign(B, DoubleFunctions.plusMultFirst(-1));
        if (P != null) {
            r = P.solve(r, false);
            r = P.solve(r, true);
            r = A.times(r, true);
            r.assign(DoubleFunctions.neg);
            gamma = B.aggregate(r, DoubleFunctions.plus, DoubleFunctions.multSquare);
            rnrm = alg.vectorNorm2(r);
        } else {
            r = A.times(r, true);
            r.assign(DoubleFunctions.neg);
            gamma = B.aggregate(r, DoubleFunctions.plus, DoubleFunctions.multSquare);
            rnrm = Math.sqrt(gamma);
        }
        //ImagePlus imX = null;
        //ImageStack is = new ImageStack(bColumns, bRows);
        DeconvPixelsStack is=new DeconvPixelsStack();
        
        /*
        if (showIteration == true) {
            DoubleCommon3D.assignPixelsToStack(is, B, cmY);
            imX = new ImagePlus("(deblurred)", is);
            imX.show();
        }
        */
        int k;
        sliceList = new IntArrayList(B.size() / 2);
        rowList = new IntArrayList(B.size() / 2);
        columnList = new IntArrayList(B.size() / 2);
        valueList = new DoubleArrayList(B.size() / 2);
        for (k = 0; k < maxIters; k++) {
            if (rnrm <= stoppingTol) {
                log("MRNSD converged after " + k + "iterations.");
                break;
            }
            log(name + " iteration: " + (k + 1) + "/" + maxIters);
            s = B.copy();
            s.assign(r, DoubleFunctions.multNeg);
            u = A.times(s, false);
            if (P != null) {
                u = P.solve(u, false);
            }
            theta = gamma / u.aggregate(DoubleFunctions.plus, DoubleFunctions.square);
            s.getNegativeValues(sliceList, rowList, columnList, valueList);
            w = B.copy();
            w.assign(s, DoubleFunctions.divNeg, sliceList, rowList, columnList);
            alpha = Math.min(theta, w.aggregate(DoubleFunctions.min, DoubleFunctions.identity, sliceList, rowList, columnList));
            B.assign(s, DoubleFunctions.plusMultSecond(alpha));
            if (P != null) {
                w = P.solve(u, true);
                w = A.times(w, true);
                r.assign(w, DoubleFunctions.plusMultSecond(alpha));
                gamma = B.aggregate(r, DoubleFunctions.plus, DoubleFunctions.multSquare);
                rnrm = alg.vectorNorm2(r);
            } else {
                w = A.times(u, true);
                r.assign(w, DoubleFunctions.plusMultSecond(alpha));
                gamma = B.aggregate(r, DoubleFunctions.plus, DoubleFunctions.multSquare);
                rnrm = Math.sqrt(gamma);
            }
            if (logConvergence) {
                log((k + 1) + ".  Norm of the residual = " + rnrm);
            }
            /*
            if (showIteration == true) {
                if (useThreshold) {
                    DoubleCommon3D.updatePixelsInStack(is, B, cmY, threshold);
                } else {
                    DoubleCommon3D.updatePixelsInStack(is, B, cmY);
                }
                ImageProcessor ip1 = imX.getProcessor();
                ip1.setMinAndMax(0, 0);
                ip1.setColorModel(cmY);
                imX.updateAndDraw();
            }
            */
        }
        if (logConvergence && k == maxIters) {
            log("MRNSD didn't converge. Reason: maximum number of iterations performed.");
        }
            if (useThreshold) {
                DoubleCommon3D.assignPixelsToStack(is, B, threshold);
            } else {
                DoubleCommon3D.assignPixelsToStack(is, B);
            }
            return is;
    }
}
