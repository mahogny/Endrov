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
import cern.colt.matrix.AbstractMatrix3D;
import cern.colt.matrix.tdcomplex.DComplexMatrix3D;
import cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix3D;
import cern.colt.matrix.tdouble.DoubleMatrix3D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix3D;
import cern.jet.math.tdcomplex.DComplexFunctions;
import cern.jet.math.tdouble.DoubleFunctions;
import endrov.deconvolution.DeconvPixelsStack;
import endrov.deconvolution.iterative.DoubleCommon2D;
import endrov.deconvolution.iterative.DoubleCommon3D;
import endrov.deconvolution.spectral.AbstractDoubleSpectralDeconvolver3D;
import endrov.deconvolution.spectral.SpectralEnums.SpectralPaddingType;
import endrov.deconvolution.spectral.SpectralEnums.SpectralResizingType;
import endrov.imageset.EvStack;

/**
 * 3D Generalized Tikhonov with periodic boundary conditions.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class DoublePeriodicGeneralizedTikhonov3D extends AbstractDoubleSpectralDeconvolver3D {
    private AbstractMatrix3D Pd;

    private AbstractMatrix3D Sa;

    private AbstractMatrix3D Sd;

    private DComplexMatrix3D ConjSa;

    private DComplexMatrix3D ConjSd;

    /**
     * Creates new instance of DoubleGTikFFT3D
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
    public DoublePeriodicGeneralizedTikhonov3D(EvStack imPSF, DoubleMatrix3D stencil, SpectralResizingType resizing, double regParam, double threshold) {
        super("Generalized Tikhonov", imPSF, resizing, SpectralPaddingType.PERIODIC, regParam, threshold);
        if ((stencil.slices() != 3) || (stencil.rows() != 3) || (stencil.columns() != 3)) {
            throw new IllegalArgumentException("Illegal stencil for regularization operator");
        }
        Pd = new DenseDoubleMatrix3D(bSlicesPad, bRowsPad, bColumnsPad);
        ((DoubleMatrix3D) Pd).viewPart(0, 0, 0, 3, 3, 3).assign(stencil);
    }

    /**
     * synchronized makes it impossible to overwrite parameters if class used in multiple instances. hack for the moment
     */
    public synchronized DeconvPixelsStack internalDeconvolve(EvStack imB) {
    later(imB);
        Sa = DoubleCommon3D.circShift((DoubleMatrix3D) PSF, psfCenter);
        Sd = DoubleCommon3D.circShift((DoubleMatrix3D) Pd, psfCenter);
        Sa = ((DenseDoubleMatrix3D) Sa).getFft3();
        Sd = ((DenseDoubleMatrix3D) Sd).getFft3();
        B = ((DenseDoubleMatrix3D) B).getFft3();
        if (ragParam == -1) {
            log(name + ": computing regularization parameter");
            ragParam = gcvGTikFFT3D((DComplexMatrix3D) Sa, (DComplexMatrix3D) Sd, (DComplexMatrix3D) B);
        }
        log(name + ": deconvolving");
        ConjSa = ((DComplexMatrix3D) Sa).copy();
        ConjSa.assign(DComplexFunctions.conj);
        ConjSd = ((DComplexMatrix3D) Sd).copy();
        ConjSd.assign(DComplexFunctions.conj);
        ConjSd.assign((DComplexMatrix3D) Sd, DComplexFunctions.mult);
        Pd = ConjSd.copy();
        ((DComplexMatrix3D) Pd).assign(DComplexFunctions.mult(ragParam * ragParam));
        PSF = ConjSa.copy();
        ((DComplexMatrix3D) PSF).assign((DComplexMatrix3D) Sa, DComplexFunctions.mult);
        Sd = ((DComplexMatrix3D) PSF).copy();
        ((DComplexMatrix3D) PSF).assign((DComplexMatrix3D) Pd, DComplexFunctions.plus);
        ((DComplexMatrix3D) B).assign(ConjSa, DComplexFunctions.mult);
        Sa = ((DComplexMatrix3D) B).copy();
        ((DComplexMatrix3D) Sa).assign((DComplexMatrix3D) PSF, DComplexFunctions.div);
        ((DenseDComplexMatrix3D) Sa).ifft3(true);
        log(name + ": finalizing");
        DeconvPixelsStack stackOut=new DeconvPixelsStack();
        if (threshold == -1.0) {
            if (isPadded) {
                DoubleCommon3D.assignPixelsToStackPadded(stackOut, (DComplexMatrix3D) Sa, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff);
            } else {
                DoubleCommon3D.assignPixelsToStack(stackOut, (DComplexMatrix3D) Sa);
            }
        } else {
            if (isPadded) {
                DoubleCommon3D.assignPixelsToStackPadded(stackOut, (DComplexMatrix3D) Sa, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, threshold);
            } else {
                DoubleCommon3D.assignPixelsToStack(stackOut, (DComplexMatrix3D) Sa, threshold);
            }
        }
        return stackOut;
    }

    /*
    public void update(double regParam, double threshold, ImagePlus imX) {
        log(name + ": updating");
        PSF = ConjSd.copy();
        ((DComplexMatrix3D) PSF).assign(DComplexFunctions.mult(regParam * regParam));
        Pd = ((DComplexMatrix3D) Sd).copy();
        ((DComplexMatrix3D) Pd).assign((DComplexMatrix3D) PSF, DComplexFunctions.plus);
        Sa = ((DComplexMatrix3D) B).copy();
        ((DComplexMatrix3D) Sa).assign((DComplexMatrix3D) Pd, DComplexFunctions.div);
        ((DenseDComplexMatrix3D) Sa).ifft3(true);
        log(name + ": finalizing");
        ImageStack stackOut = new ImageStack(bColumns, bRows);
        if (threshold == -1.0) {
            if (isPadded) {
                DoubleCommon3D.assignPixelsToStackPadded(stackOut, (DComplexMatrix3D) Sa, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY);
            } else {
                DoubleCommon3D.assignPixelsToStack(stackOut, (DComplexMatrix3D) Sa, cmY);
            }
        } else {
            if (isPadded) {
                DoubleCommon3D.assignPixelsToStackPadded(stackOut, (DComplexMatrix3D) Sa, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                DoubleCommon3D.assignPixelsToStack(stackOut, (DComplexMatrix3D) Sa, cmY, threshold);
            }
        }
        imX.setStack(imX.getTitle(), stackOut);
        DoubleCommon3D.convertImage(imX, output);
    }*/

    private static double gcvGTikFFT3D(DComplexMatrix3D Sa, DComplexMatrix3D Sd, DComplexMatrix3D Bhat) {
        AbstractMatrix3D sa = Sa.copy();
        AbstractMatrix3D sd = Sd.copy();
        AbstractMatrix3D bhat = Bhat.copy();
        sa = ((DComplexMatrix3D) sa).assign(DComplexFunctions.abs).getRealPart();
        sd = ((DComplexMatrix3D) sd).assign(DComplexFunctions.abs).getRealPart();
        bhat = ((DComplexMatrix3D) bhat).assign(DComplexFunctions.abs).getRealPart();

        double[] tmp = ((DoubleMatrix3D) sa).getMinLocation();
        double smin = tmp[0];
        tmp = ((DoubleMatrix3D) sa).getMaxLocation();
        double smax = tmp[0];
        ((DoubleMatrix3D) sa).assign(DoubleFunctions.square);
        ((DoubleMatrix3D) sd).assign(DoubleFunctions.square);
        GTikFmin3D fmin = new GTikFmin3D((DoubleMatrix3D) sa, (DoubleMatrix3D) sd, (DoubleMatrix3D) bhat);
        return (double) DoubleFmin.fmin(smin, smax, fmin, DoubleCommon2D.FMIN_TOL);
    }

    private static class GTikFmin3D implements DoubleFmin_methods {
        DoubleMatrix3D sasquare;

        DoubleMatrix3D sdsquare;

        DoubleMatrix3D bhat;

        public GTikFmin3D(DoubleMatrix3D sasquare, DoubleMatrix3D sdsquare, DoubleMatrix3D bhat) {
            this.sasquare = sasquare;
            this.sdsquare = sdsquare;
            this.bhat = bhat;
        }

        public double f_to_minimize(double regParam) {
            DoubleMatrix3D sdloc = sdsquare.copy();
            DoubleMatrix3D denom = sdloc.copy();

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
