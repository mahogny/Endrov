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
package endrov.deconvolution.spectral.tsvd;

import cern.colt.function.tint.IntComparator;
import cern.colt.matrix.AbstractMatrix1D;
import cern.colt.matrix.AbstractMatrix3D;
import cern.colt.matrix.tdcomplex.DComplexMatrix1D;
import cern.colt.matrix.tdcomplex.DComplexMatrix3D;
import cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix1D;
import cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix3D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix3D;
import cern.colt.matrix.tdouble.algo.DoubleSorting;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix3D;
import cern.jet.math.tdcomplex.DComplexFunctions;
import endrov.deconvolution.DeconvPixelsStack;
import endrov.deconvolution.iterative.DoubleCommon3D;
import endrov.deconvolution.spectral.AbstractDoubleSpectralDeconvolver3D;
import endrov.deconvolution.spectral.SpectralEnums.SpectralPaddingType;
import endrov.deconvolution.spectral.SpectralEnums.SpectralResizingType;
import endrov.imageset.EvStack;

/**
 * 3D Truncated SVD with periodic boundary conditions.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class DoublePeriodicTruncatedSVD3D extends AbstractDoubleSpectralDeconvolver3D {
    private AbstractMatrix3D S;

    /**
     * Creates new instance of DoubleTsvdFFT3D
     * 
     * @param imB
     *            blurred image
     * @param imPSF
     *            Point Spread Function image
     * @param resizing
     *            type of resizing
     * @param output
     *            type of output
     * @param showPadded
     *            if true, then a padded image is displayed
     * @param regParam
     *            regularization parameter. If regParam == -1 then the
     *            regularization parameter is computed by Generalized Cross
     *            Validation.
     * @param threshold
     *            the smallest positive value assigned to the restored image,
     *            all the values less than the threshold are set to zero. To
     *            disable thresholding use threshold = -1.
     */
    public DoublePeriodicTruncatedSVD3D(EvStack imPSF, SpectralResizingType resizing, double regParam, double threshold) {
        super("TSVD", imPSF, resizing, SpectralPaddingType.PERIODIC, regParam, threshold);
    }

    public DeconvPixelsStack internalDeconvolve(EvStack imB) {
    later(imB);
        S = DoubleCommon3D.circShift((DoubleMatrix3D) PSF, psfCenter);
        S = ((DenseDoubleMatrix3D) S).getFft3();
        B = ((DenseDoubleMatrix3D) B).getFft3();
        if (ragParam == -1) {
            log(name + ": computing regularization parameter");
            ragParam = gcvTsvdFFT3D((DComplexMatrix3D) S, (DComplexMatrix3D) B);
        }
        log(name + ": deconvolving");
        DComplexMatrix3D Sfilt = DoubleCommon3D.createFilter((DComplexMatrix3D) S, ragParam);
        PSF = ((DComplexMatrix3D) B).copy();
        ((DComplexMatrix3D) PSF).assign(Sfilt, DComplexFunctions.mult);
        ((DenseDComplexMatrix3D) PSF).ifft3(true);
        log(name + ": deconvolving");
        DeconvPixelsStack stackOut=new DeconvPixelsStack();
        if (threshold == -1) {
            if (isPadded) {
                DoubleCommon3D.assignPixelsToStackPadded(stackOut, (DComplexMatrix3D) PSF, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff);
            } else {
                DoubleCommon3D.assignPixelsToStack(stackOut, (DComplexMatrix3D) PSF);
            }
        } else {
            if (isPadded) {
                DoubleCommon3D.assignPixelsToStackPadded(stackOut, (DComplexMatrix3D) PSF, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, threshold);
            } else {
                DoubleCommon3D.assignPixelsToStack(stackOut, (DComplexMatrix3D) PSF, threshold);
            }
        }
        return stackOut;
    }
/*
    public void update(double regParam, double threshold, ImagePlus imX) {
        log(name + ": updating");
        DComplexMatrix3D Sfilt = DoubleCommon3D.createFilter((DComplexMatrix3D) S, regParam);
        PSF = ((DComplexMatrix3D) B).copy();
        ((DComplexMatrix3D) PSF).assign(Sfilt, DComplexFunctions.mult);
        ((DenseDComplexMatrix3D) PSF).ifft3(true);
        log(name + ": finalizing");
        ImageStack stackOut = new ImageStack(bColumns, bRows);
        if (threshold == -1) {
            if (isPadded) {
                DoubleCommon3D.assignPixelsToStackPadded(stackOut, (DComplexMatrix3D) PSF, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY);
            } else {
                DoubleCommon3D.assignPixelsToStack(stackOut, (DComplexMatrix3D) PSF, cmY);
            }
        } else {
            if (isPadded) {
                DoubleCommon3D.assignPixelsToStackPadded(stackOut, (DComplexMatrix3D) PSF, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                DoubleCommon3D.assignPixelsToStack(stackOut, (DComplexMatrix3D) PSF, cmY, threshold);
            }
        }
        imX.setStack(imX.getTitle(), stackOut);
        DoubleCommon3D.convertImage(imX, output);
    }*/

    private static double gcvTsvdFFT3D(DComplexMatrix3D S, DComplexMatrix3D Bhat) {
        int length = S.slices() * S.rows() * S.columns();
        AbstractMatrix1D s = new DenseDComplexMatrix1D(length);
        AbstractMatrix1D bhat = new DenseDComplexMatrix1D(length);
        System.arraycopy(((DenseDComplexMatrix3D) S).elements(), 0, ((DenseDComplexMatrix1D) s).elements(), 0, 2 * length);
        System.arraycopy(((DenseDComplexMatrix3D) Bhat).elements(), 0, ((DenseDComplexMatrix1D) bhat).elements(), 0, 2 * length);
        s = ((DComplexMatrix1D) s).assign(DComplexFunctions.abs).getRealPart();
        bhat = ((DComplexMatrix1D) bhat).assign(DComplexFunctions.abs).getRealPart();
        final double[] svalues = (double[]) ((DenseDoubleMatrix1D) s).elements();
        IntComparator compDec = new IntComparator() {
            public int compare(int a, int b) {
                if (svalues[a] != svalues[a] || svalues[b] != svalues[b])
                    return compareNaN(svalues[a], svalues[b]); // swap NaNs to
                // the end
                return svalues[a] < svalues[b] ? 1 : (svalues[a] == svalues[b] ? 0 : -1);
            }
        };
        int[] indices = DoubleSorting.quickSort.sortIndex((DoubleMatrix1D) s, compDec);
        s = ((DoubleMatrix1D) s).viewSelection(indices);
        bhat = ((DoubleMatrix1D) bhat).viewSelection(indices);
        int n = s.size();
        double[] rho = new double[n - 1];
        rho[n - 2] = ((DoubleMatrix1D) bhat).getQuick(n - 1) * ((DoubleMatrix1D) bhat).getQuick(n - 1);
        DoubleMatrix1D G = new DenseDoubleMatrix1D(n - 1);
        double[] elemsG = (double[]) G.elements();
        elemsG[n - 2] = rho[n - 2];
        double bhatel, temp1;
        for (int k = n - 2; k > 0; k--) {
            bhatel = ((DoubleMatrix1D) bhat).getQuick(k);
            rho[k - 1] = rho[k] + bhatel * bhatel;
            temp1 = n - k;
            temp1 = temp1 * temp1;
            elemsG[k - 1] = rho[k - 1] / temp1;
        }
        for (int k = 0; k < n - 3; k++) {
            if (((DoubleMatrix1D) s).getQuick(k) == ((DoubleMatrix1D) s).getQuick(k + 1)) {
                elemsG[k] = Double.POSITIVE_INFINITY;
            }
        }
        return ((DoubleMatrix1D) s).getQuick((int) G.getMinLocation()[1]);
    }

    private static final int compareNaN(double a, double b) {
        if (a != a) {
            if (b != b)
                return 0; // NaN equals NaN
            else
                return 1; // e.g. NaN > 5
        }
        return -1; // e.g. 5 < NaN
    }
}
