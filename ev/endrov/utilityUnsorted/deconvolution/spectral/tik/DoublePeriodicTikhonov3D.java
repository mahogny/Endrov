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
package endrov.utilityUnsorted.deconvolution.spectral.tik;

import optimization.DoubleFmin;
import optimization.DoubleFmin_methods;
import cern.colt.matrix.AbstractMatrix3D;
import cern.colt.matrix.tdcomplex.DComplexMatrix3D;
import cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix3D;
import cern.colt.matrix.tdouble.DoubleMatrix3D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix3D;
import cern.jet.math.tdcomplex.DComplexFunctions;
import cern.jet.math.tdouble.DoubleFunctions;
import endrov.imageset.EvStack;
import endrov.utilityUnsorted.deconvolution.DeconvPixelsStack;
import endrov.utilityUnsorted.deconvolution.iterative.DoubleCommon2D;
import endrov.utilityUnsorted.deconvolution.iterative.DoubleCommon3D;
import endrov.utilityUnsorted.deconvolution.spectral.AbstractDoubleSpectralDeconvolver3D;
import endrov.utilityUnsorted.deconvolution.spectral.SpectralEnums.SpectralPaddingType;
import endrov.utilityUnsorted.deconvolution.spectral.SpectralEnums.SpectralResizingType;

/**
 * 3D Tikhonov with periodic boundary conditions.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class DoublePeriodicTikhonov3D extends AbstractDoubleSpectralDeconvolver3D {
    private AbstractMatrix3D S;

    private DComplexMatrix3D ConjS;

    /**
     * Creates new instance of DoubleTikFFT2D
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
    public DoublePeriodicTikhonov3D(EvStack imPSF, SpectralResizingType resizing,  double regParam, double threshold) {
        super("Tikhonov", imPSF, resizing, SpectralPaddingType.PERIODIC, regParam, threshold);
    }

    public DeconvPixelsStack internalDeconvolve(EvStack imB){
    later(imB);
        S = DoubleCommon3D.circShift((DoubleMatrix3D) PSF, psfCenter);
        S = ((DenseDoubleMatrix3D) S).getFft3();
        B = ((DenseDoubleMatrix3D) B).getFft3();
        if (ragParam == -1) {
            log(name + ": computing regularization parameter");
            ragParam = gcvTikFFT3D((DComplexMatrix3D) S, (DComplexMatrix3D) B);
        }
        log(name + ": deconvolving");
        ConjS = ((DComplexMatrix3D) S).copy();
        ConjS.assign(DComplexFunctions.conj);
        PSF = ConjS.copy();
        ((DComplexMatrix3D) PSF).assign((DComplexMatrix3D) S, DComplexFunctions.mult);
        S = ((DComplexMatrix3D) PSF).copy();
        ((DComplexMatrix3D) PSF).assign(DComplexFunctions.plus(new double[] { ragParam * ragParam, 0 }));
        ((DComplexMatrix3D) B).assign(ConjS, DComplexFunctions.mult);
        ConjS = ((DComplexMatrix3D) B).copy();
        ConjS.assign((DComplexMatrix3D) PSF, DComplexFunctions.div);
        ((DenseDComplexMatrix3D) ConjS).ifft3(true);
        log(name + ": finalizing");
        DeconvPixelsStack stackOut=new DeconvPixelsStack();
        if (threshold == -1.0) {
            if (isPadded) {
                DoubleCommon3D.assignPixelsToStackPadded(stackOut, ConjS, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff);
            } else {
                DoubleCommon3D.assignPixelsToStack(stackOut, ConjS);
            }
        } else {
            if (isPadded) {
                DoubleCommon3D.assignPixelsToStackPadded(stackOut, ConjS, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, threshold);
            } else {
                DoubleCommon3D.assignPixelsToStack(stackOut, ConjS, threshold);
            }
        }
        return stackOut;
    }

    public DeconvPixelsStack deblur(double regParam, double threshold) {
        log(name + ": deblurring");
        S = DoubleCommon3D.circShift((DoubleMatrix3D) PSF, psfCenter);
        S = ((DenseDoubleMatrix3D) S).getFft3();
        B = ((DenseDoubleMatrix3D) B).getFft3();
        ConjS = ((DComplexMatrix3D) S).copy();
        ConjS.assign(DComplexFunctions.conj);
        PSF = ConjS.copy();
        ((DComplexMatrix3D) PSF).assign((DComplexMatrix3D) S, DComplexFunctions.mult);
        S = ((DComplexMatrix3D) PSF).copy();
        ((DComplexMatrix3D) PSF).assign(DComplexFunctions.plus(new double[] { regParam * regParam, 0 }));
        ((DComplexMatrix3D) B).assign(ConjS, DComplexFunctions.mult);
        ConjS = ((DComplexMatrix3D) B).copy();
        ConjS.assign((DComplexMatrix3D) PSF, DComplexFunctions.div);
        ((DenseDComplexMatrix3D) ConjS).ifft3(true);
        log(name + ": finalizing");
        DeconvPixelsStack stackOut=new DeconvPixelsStack();
        if (threshold == -1.0) {
            if (isPadded) {
                DoubleCommon3D.assignPixelsToStackPadded(stackOut, ConjS, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff);
            } else {
                DoubleCommon3D.assignPixelsToStack(stackOut, ConjS);
            }
        } else {
            if (isPadded) {
                DoubleCommon3D.assignPixelsToStackPadded(stackOut, ConjS, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, threshold);
            } else {
                DoubleCommon3D.assignPixelsToStack(stackOut, ConjS, threshold);
            }
        }
        return stackOut;
    }
/*
    public void update(double regParam, double threshold, ImagePlus imX) {
        log(name + ": updating");
        PSF = ((DComplexMatrix3D) S).copy();
        ((DComplexMatrix3D) PSF).assign(DComplexFunctions.plus(new double[] { regParam * regParam, 0 }));
        ConjS = ((DComplexMatrix3D) B).copy();
        ((DComplexMatrix3D) ConjS).assign((DComplexMatrix3D) PSF, DComplexFunctions.div);
        ((DenseDComplexMatrix3D) ConjS).ifft3(true);
        log(name + ": finalizing");
        ImageStack stackOut = new ImageStack(bColumns, bRows);
        if (threshold == -1.0) {
            if (isPadded) {
                DoubleCommon3D.assignPixelsToStackPadded(stackOut, ConjS, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY);
            } else {
                DoubleCommon3D.assignPixelsToStack(stackOut, ConjS, cmY);
            }
        } else {
            if (isPadded) {
                DoubleCommon3D.assignPixelsToStackPadded(stackOut, ConjS, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                DoubleCommon3D.assignPixelsToStack(stackOut, ConjS, cmY, threshold);
            }
        }
        imX.setStack(imX.getTitle(), stackOut);
        DoubleCommon3D.convertImage(imX, output);
    }
*/
    private static double gcvTikFFT3D(DComplexMatrix3D S, DComplexMatrix3D Bhat) {
        AbstractMatrix3D s = S.copy();
        AbstractMatrix3D bhat = Bhat.copy();
        s = ((DComplexMatrix3D) s).assign(DComplexFunctions.abs).getRealPart();
        bhat = ((DComplexMatrix3D) bhat).assign(DComplexFunctions.abs).getRealPart();
        double[] tmp = ((DoubleMatrix3D) s).getMinLocation();
        double smin = tmp[0];
        tmp = ((DoubleMatrix3D) s).getMaxLocation();
        double smax = tmp[0];
        ((DoubleMatrix3D) s).assign(DoubleFunctions.square);
        TikFmin3D fmin = new TikFmin3D((DoubleMatrix3D) s, (DoubleMatrix3D) bhat);
        return (double) DoubleFmin.fmin(smin, smax, fmin, DoubleCommon2D.FMIN_TOL);
    }

    private static class TikFmin3D implements DoubleFmin_methods {
        DoubleMatrix3D ssquare;

        DoubleMatrix3D bhat;

        public TikFmin3D(DoubleMatrix3D ssquare, DoubleMatrix3D bhat) {
            this.ssquare = ssquare;
            this.bhat = bhat;
        }

        public double f_to_minimize(double regParam) {
            DoubleMatrix3D sloc = ssquare.copy();
            DoubleMatrix3D bhatloc = bhat.copy();

            sloc.assign(DoubleFunctions.plus(regParam * regParam));
            sloc.assign(DoubleFunctions.inv);
            bhatloc.assign(sloc, DoubleFunctions.mult);
            bhatloc.assign(DoubleFunctions.square);
            double ss = sloc.zSum();
            return bhatloc.zSum() / (ss * ss);
        }

    }

}
