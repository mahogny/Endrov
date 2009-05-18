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
package endrov.deconvolution.spectral.gtik;

import optimization.DoubleFmin;
import optimization.DoubleFmin_methods;
import cern.colt.matrix.AbstractMatrix2D;
import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix2D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import cern.jet.math.tdcomplex.DComplexFunctions;
import cern.jet.math.tdouble.DoubleFunctions;
import endrov.deconvolution.iterative.DoubleCommon2D;
import endrov.deconvolution.spectral.AbstractDoubleSpectralDeconvolver2D;
import endrov.deconvolution.spectral.SpectralEnums.PaddingType;
import endrov.deconvolution.spectral.SpectralEnums.ResizingType;
import endrov.imageset.EvPixels;

/**
 * 2D Generalized Tikhonov with periodic boundary conditions.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class DoublePeriodicGeneralizedTikhonov2D extends AbstractDoubleSpectralDeconvolver2D {

    private AbstractMatrix2D Pd;

    private AbstractMatrix2D Sa;

    private AbstractMatrix2D Sd;

    private DComplexMatrix2D ConjSa;

    private DComplexMatrix2D ConjSd;

    /**
     * Creates new instance of DoubleGTikFFT2D
     * 
     * @param imB
     *            blurred image
     * @param imPSF
     *            Point Spread Function image
     * @param stencil
     *            3-by-3 stencil for regularization operator
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
    public DoublePeriodicGeneralizedTikhonov2D(EvPixels imB, EvPixels imPSF, DoubleMatrix2D stencil, ResizingType resizing, boolean showPadded, double regParam, double threshold) {
        super("Generalized Tikhonov", imB, imPSF, resizing, PaddingType.PERIODIC, regParam, threshold);
        if ((stencil.rows() != 3) || (stencil.columns() != 3)) {
            throw new IllegalArgumentException("Illegal stencil for regularization operator");
        }
        Pd = new DenseDoubleMatrix2D(bRowsPad, bColumnsPad);
        ((DoubleMatrix2D) Pd).viewPart(0, 0, 3, 3).assign(stencil);
    }

    public EvPixels internalDeconvolve(EvPixels imB) {
        log(name + ": deconvolving");
        Sa = DoubleCommon2D.circShift((DoubleMatrix2D) PSF, psfCenter);
        Sa = ((DenseDoubleMatrix2D) Sa).getFft2();
        B = ((DenseDoubleMatrix2D) B).getFft2();
        Sd = DoubleCommon2D.circShift((DoubleMatrix2D) Pd, new int[] { 1, 1 });
        Sd = ((DenseDoubleMatrix2D) Sd).getFft2();
        if (regParam == -1) {
            log(name + ": computing regularization parameter");
            regParam = gcvGTikFFT2D((DComplexMatrix2D) Sa, (DComplexMatrix2D) Sd, (DComplexMatrix2D) B);
        }
        log(name + ": deconvolving");
        ConjSa = ((DComplexMatrix2D) Sa).copy();
        ConjSa.assign(DComplexFunctions.conj);
        ConjSd = ((DComplexMatrix2D) Sd).copy();
        ConjSd.assign(DComplexFunctions.conj);
        ConjSd.assign((DComplexMatrix2D) Sd, DComplexFunctions.mult);
        Pd = ConjSd.copy();
        ((DComplexMatrix2D) Pd).assign(DComplexFunctions.mult(regParam * regParam));
        PSF = ConjSa.copy();
        ((DComplexMatrix2D) PSF).assign((DComplexMatrix2D) Sa, DComplexFunctions.mult);
        Sd = ((DComplexMatrix2D) PSF).copy();
        ((DComplexMatrix2D) PSF).assign((DComplexMatrix2D) Pd, DComplexFunctions.plus);
        ((DComplexMatrix2D) B).assign(ConjSa, DComplexFunctions.mult);
        Sa = ((DComplexMatrix2D) B).copy();
        ((DComplexMatrix2D) Sa).assign((DComplexMatrix2D) PSF, DComplexFunctions.div);
        ((DenseDComplexMatrix2D) Sa).ifft2(true);
        log(name + ": finalizing");
        if (threshold == -1.0) {
            if (isPadded) {
                return DoubleCommon2D.assignPixelsToProcessorPadded((DComplexMatrix2D) Sa, bRows, bColumns, bRowsOff, bColumnsOff);
            } else {
                return DoubleCommon2D.assignPixelsToProcessor((DComplexMatrix2D) Sa);
            }
        } else {
            if (isPadded) {
                return DoubleCommon2D.assignPixelsToProcessorPadded((DComplexMatrix2D) Sa, bRows, bColumns, bRowsOff, bColumnsOff, threshold);
            } else {
                return DoubleCommon2D.assignPixelsToProcessor((DComplexMatrix2D) Sa, threshold);
            }
        }
    }

    /*
    public EvPixels update(double regParam, double threshold, EvPixels imX) {
        log(name + ": updating");
        PSF = ConjSd.copy();
        ((DComplexMatrix2D) PSF).assign(DComplexFunctions.mult(regParam * regParam));
        Pd = ((DComplexMatrix2D) Sd).copy();
        ((DComplexMatrix2D) Pd).assign((DComplexMatrix2D) PSF, DComplexFunctions.plus);
        Sa = ((DComplexMatrix2D) B).copy();
        ((DComplexMatrix2D) Sa).assign((DComplexMatrix2D) Pd, DComplexFunctions.div);
        ((DenseDComplexMatrix2D) Sa).ifft2(true);
        log(name + ": finalizing");
        if (threshold == -1.0) {
            if (isPadded) {
                return DoubleCommon2D.assignPixelsToProcessorPadded((DComplexMatrix2D) Sa, bRows, bColumns, bRowsOff, bColumnsOff);
            } else {
                return DoubleCommon2D.assignPixelsToProcessor((DComplexMatrix2D) Sa);
            }
        } else {
            if (isPadded) {
                return DoubleCommon2D.assignPixelsToProcessorPadded((DComplexMatrix2D) Sa, bRows, bColumns, bRowsOff, bColumnsOff, threshold);
            } else {
                return DoubleCommon2D.assignPixelsToProcessor((DComplexMatrix2D) Sa, threshold);
            }
        }
        //TODO return ip, otherwise imx was updated
        
        //imX.setProcessor(imX.getTitle(), ip);
    }*/

    private double gcvGTikFFT2D(DComplexMatrix2D Sa, DComplexMatrix2D Sd, DComplexMatrix2D Bhat) {
        AbstractMatrix2D sa = Sa.copy();
        sa = ((DComplexMatrix2D) sa).assign(DComplexFunctions.abs).getRealPart();
        AbstractMatrix2D sd = Sd.copy();
        sd = ((DComplexMatrix2D) sd).assign(DComplexFunctions.abs).getRealPart();
        AbstractMatrix2D bhat = Bhat.copy();
        bhat = ((DComplexMatrix2D) bhat).assign(DComplexFunctions.abs).getRealPart();
        double[] tmp = ((DoubleMatrix2D) sa).getMinLocation();
        double smin = tmp[0];
        tmp = ((DoubleMatrix2D) sa).getMaxLocation();
        double smax = tmp[0];
        ((DoubleMatrix2D) sa).assign(DoubleFunctions.square);
        ((DoubleMatrix2D) sd).assign(DoubleFunctions.square);
        GTikFmin2D fmin = new GTikFmin2D((DoubleMatrix2D) sa, (DoubleMatrix2D) sd, (DoubleMatrix2D) bhat);
        return (double) DoubleFmin.fmin(smin, smax, fmin, DoubleCommon2D.FMIN_TOL);
    }

    private class GTikFmin2D implements DoubleFmin_methods {
        DoubleMatrix2D sasquare;

        DoubleMatrix2D sdsquare;

        DoubleMatrix2D bhat;

        public GTikFmin2D(DoubleMatrix2D sasquare, DoubleMatrix2D sdsquare, DoubleMatrix2D bhat) {
            this.sasquare = sasquare;
            this.sdsquare = sdsquare;
            this.bhat = bhat;
        }

        public double f_to_minimize(double regParam) {
            DoubleMatrix2D sdloc = sdsquare.copy();
            DoubleMatrix2D denom = sdloc.copy();

            denom.assign(DoubleFunctions.mult(regParam * regParam));
            denom.assign(sasquare, DoubleFunctions.plus);
            sdloc.assign(denom, DoubleFunctions.div);
            denom = bhat.copy();
            denom.assign(sdloc, DoubleFunctions.mult);
            denom.assign(DoubleFunctions.square);
            double sphi_d = sdloc.zSum();
            return denom.zSum() / (sphi_d * sphi_d);
        }

    }

}
