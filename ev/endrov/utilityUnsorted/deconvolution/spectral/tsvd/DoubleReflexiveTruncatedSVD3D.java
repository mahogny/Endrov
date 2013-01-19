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
package endrov.utilityUnsorted.deconvolution.spectral.tsvd;

import cern.colt.function.tint.IntComparator;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix3D;
import cern.colt.matrix.tdouble.algo.DoubleSorting;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix3D;
import cern.jet.math.tdouble.DoubleFunctions;
import endrov.typeImageset.EvStack;
import endrov.utilityUnsorted.deconvolution.DeconvPixelsStack;
import endrov.utilityUnsorted.deconvolution.iterative.DoubleCommon3D;
import endrov.utilityUnsorted.deconvolution.spectral.AbstractDoubleSpectralDeconvolver3D;
import endrov.utilityUnsorted.deconvolution.spectral.SpectralEnums.SpectralPaddingType;
import endrov.utilityUnsorted.deconvolution.spectral.SpectralEnums.SpectralResizingType;

/**
 * 3D Truncated SVD with reflexive boundary conditions.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class DoubleReflexiveTruncatedSVD3D extends AbstractDoubleSpectralDeconvolver3D {

    private DoubleMatrix3D E1;

    private DoubleMatrix3D S;

    /**
     * Creates new instance of DoubleTsvdDCT3D
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
    public DoubleReflexiveTruncatedSVD3D(EvStack imPSF, SpectralResizingType resizing, double regParam, double threshold) {
        super("TSVD", imPSF, resizing, SpectralPaddingType.REFLEXIVE, regParam, threshold);
    }

    public DeconvPixelsStack internalDeconvolve(EvStack imB) {
    later(imB);
        E1 = new DenseDoubleMatrix3D(bSlicesPad, bRowsPad, bColumnsPad);
        E1.setQuick(0, 0, 0, 1);
        ((DenseDoubleMatrix3D) E1).dct3(true);
        ((DenseDoubleMatrix3D) B).dct3(true);
        S = DoubleCommon3D.dctShift((DoubleMatrix3D) PSF, psfCenter);
        ((DenseDoubleMatrix3D) S).dct3(true);
        S.assign(E1, DoubleFunctions.div);
        if (ragParam == -1) {
            log(name + ": computing regularization parameter");
            ragParam = gcvTsvdDCT3D(S, (DoubleMatrix3D) B);
        }
        log(name + ": deconvolving");
        E1 = DoubleCommon3D.createFilter(S, ragParam);
        PSF = ((DoubleMatrix3D) B).copy();
        ((DoubleMatrix3D) PSF).assign(E1, DoubleFunctions.mult);
        ((DenseDoubleMatrix3D) PSF).idct3(true);
        log(name + ": finalizing");
        DeconvPixelsStack stackOut=new DeconvPixelsStack();
        if (threshold == -1) {
            if (isPadded) {
                DoubleCommon3D.assignPixelsToStackPadded(stackOut, (DoubleMatrix3D) PSF, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff);
            } else {
                DoubleCommon3D.assignPixelsToStack(stackOut, (DoubleMatrix3D) PSF);
            }
        } else {
            if (isPadded) {
                DoubleCommon3D.assignPixelsToStackPadded(stackOut, (DoubleMatrix3D) PSF, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, threshold);
            } else {
                DoubleCommon3D.assignPixelsToStack(stackOut, (DoubleMatrix3D) PSF, threshold);
            }
        }
        return stackOut;
    }
/*
    public void update(double regParam, double threshold, ImagePlus imX) {
        log(name + ": updating");
        E1 = DoubleCommon3D.createFilter(S, regParam);
        PSF = ((DoubleMatrix3D) B).copy();
        ((DoubleMatrix3D) PSF).assign(E1, DoubleFunctions.mult);
        ((DenseDoubleMatrix3D) PSF).idct3(true);
        log(name + ": finalizing");
        ImageStack stackOut = new ImageStack(bColumns, bRows);
        if (threshold == -1) {
            if (isPadded) {
                DoubleCommon3D.assignPixelsToStackPadded(stackOut, (DoubleMatrix3D) PSF, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY);
            } else {
                DoubleCommon3D.assignPixelsToStack(stackOut, (DoubleMatrix3D) PSF, cmY);
            }
        } else {
            if (isPadded) {
                DoubleCommon3D.assignPixelsToStackPadded(stackOut, (DoubleMatrix3D) PSF, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                DoubleCommon3D.assignPixelsToStack(stackOut, (DoubleMatrix3D) PSF, cmY, threshold);
            }
        }
        imX.setStack(imX.getTitle(), stackOut);
        DoubleCommon3D.convertImage(imX, output);
    }*/

    private static double gcvTsvdDCT3D(DoubleMatrix3D S, DoubleMatrix3D Bhat) {
        int length = S.slices() * S.rows() * S.columns();
        DoubleMatrix1D s = new DenseDoubleMatrix1D(length);
        DoubleMatrix1D bhat = new DenseDoubleMatrix1D(length);
        System.arraycopy(((DenseDoubleMatrix3D) S).elements(), 0, ((DenseDoubleMatrix1D) s).elements(), 0, length);
        System.arraycopy(((DenseDoubleMatrix3D) Bhat).elements(), 0, ((DenseDoubleMatrix1D) bhat).elements(), 0, length);
        s.assign(DoubleFunctions.abs);
        bhat.assign(DoubleFunctions.abs);
        final double[] svalues = (double[]) ((DenseDoubleMatrix1D) s).elements();

        IntComparator compDec = new IntComparator() {
            public int compare(int a, int b) {
                if (svalues[a] != svalues[a] || svalues[b] != svalues[b])
                    return compareNaN(svalues[a], svalues[b]); // swap NaNs to
                // the end
                return svalues[a] < svalues[b] ? 1 : (svalues[a] == svalues[b] ? 0 : -1);
            }
        };
        int[] indices = DoubleSorting.quickSort.sortIndex(s, compDec);
        s = s.viewSelection(indices);
        bhat = bhat.viewSelection(indices);
        int n = s.size();
        double[] rho = new double[n - 1];
        rho[n - 2] = bhat.getQuick(n - 1) * bhat.getQuick(n - 1);
        DoubleMatrix1D G = new DenseDoubleMatrix1D(n - 1);
        double[] elemsG = (double[]) G.elements();
        elemsG[n - 2] = rho[n - 2];
        double bhatel, temp1;
        for (int k = n - 2; k > 0; k--) {
            bhatel = bhat.getQuick(k);
            rho[k - 1] = rho[k] + bhatel * bhatel;
            temp1 = n - k;
            temp1 = temp1 * temp1;
            elemsG[k - 1] = rho[k - 1] / temp1;
        }
        for (int k = 0; k < n - 3; k++) {
            if (s.getQuick(k) == s.getQuick(k + 1)) {
                elemsG[k] = Double.POSITIVE_INFINITY;
            }
        }
        return s.getQuick((int) G.getMinLocation()[1]);
    }

  	private static final int compareNaN(double a, double b)
  		{
  		if (Double.isNaN(a))
  			{
  			if (Double.isNaN(b))
  				return 0; // NaN equals NaN
  			else
  				return 1; // e.g. NaN > 5
  			}
  		return -1; // e.g. 5 < NaN
  		}

}
