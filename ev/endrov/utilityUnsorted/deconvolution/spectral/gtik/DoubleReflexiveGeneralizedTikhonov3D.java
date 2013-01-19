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
package endrov.utilityUnsorted.deconvolution.spectral.gtik;

import optimization.DoubleFmin;
import optimization.DoubleFmin_methods;
import cern.colt.matrix.tdouble.DoubleMatrix3D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix3D;
import cern.jet.math.tdouble.DoubleFunctions;
import endrov.typeImageset.EvStack;
import endrov.utilityUnsorted.deconvolution.DeconvPixelsStack;
import endrov.utilityUnsorted.deconvolution.iterative.DoubleCommon2D;
import endrov.utilityUnsorted.deconvolution.iterative.DoubleCommon3D;
import endrov.utilityUnsorted.deconvolution.spectral.AbstractDoubleSpectralDeconvolver3D;
import endrov.utilityUnsorted.deconvolution.spectral.SpectralEnums.SpectralPaddingType;
import endrov.utilityUnsorted.deconvolution.spectral.SpectralEnums.SpectralResizingType;

/**
 * 3D Generalized Tikhonov with reflexive boundary conditions.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class DoubleReflexiveGeneralizedTikhonov3D extends AbstractDoubleSpectralDeconvolver3D {

    private DoubleMatrix3D Pd;

    private DoubleMatrix3D E1;

    private DoubleMatrix3D Sa;

    private DoubleMatrix3D Sd;

    /**
     * Creates new instance of DoubleGTikDCT3D
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
    public DoubleReflexiveGeneralizedTikhonov3D(EvStack imPSF, DoubleMatrix3D stencil, SpectralResizingType resizing, double regParam, double threshold) {
        super("Generalized Tikhonov", imPSF, resizing, SpectralPaddingType.REFLEXIVE, regParam, threshold);
        if ((stencil.slices() != 3) || (stencil.rows() != 3) || (stencil.columns() != 3)) {
            throw new IllegalArgumentException("Illegal stencil for regularization operator");
        }
        Pd = new DenseDoubleMatrix3D(bSlicesPad, bRowsPad, bColumnsPad);
        Pd.viewPart(0, 0, 0, 3, 3, 3).assign(stencil);
    }

    public DeconvPixelsStack internalDeconvolve(EvStack imB) {
    later(imB);
        E1 = new DenseDoubleMatrix3D(bSlicesPad, bRowsPad, bColumnsPad);
        E1.setQuick(0, 0, 0, 1);
        ((DenseDoubleMatrix3D) E1).dct3(true);
        Sa = DoubleCommon3D.dctShift((DoubleMatrix3D) PSF, psfCenter);
        ((DenseDoubleMatrix3D) Sa).dct3(true);
        Sa.assign(E1, DoubleFunctions.div);
        ((DenseDoubleMatrix3D) B).dct3(true);
        Sd = DoubleCommon3D.dctShift(Pd, new int[] { 1, 1, 1 });
        ((DenseDoubleMatrix3D) Sd).dct3(true);
        Sd.assign(E1, DoubleFunctions.div);
        if (ragParam == -1) {
            log(name + ": computing regularization parameter");
            ragParam = gcvGTikDCT3D(Sa, Sd, (DoubleMatrix3D) B);
        }
        log(name + ": deconvolving");
        Sd.assign(DoubleFunctions.square);
        PSF = Sa.copy();
        Pd = Sd.copy();
        Pd.assign(DoubleFunctions.mult(ragParam * ragParam));
        ((DoubleMatrix3D) PSF).assign(DoubleFunctions.square);
        E1 = ((DoubleMatrix3D) PSF).copy();
        ((DoubleMatrix3D) PSF).assign(Pd, DoubleFunctions.plus);
        ((DoubleMatrix3D) B).assign(Sa, DoubleFunctions.mult);
        Sa = ((DoubleMatrix3D) B).copy();
        Sa.assign((DoubleMatrix3D) PSF, DoubleFunctions.div);
        ((DenseDoubleMatrix3D) Sa).idct3(true);
        log(name + ": finalizing");
        DeconvPixelsStack stackOut = new DeconvPixelsStack();
        if (threshold == -1.0) {
            if (isPadded) {
                DoubleCommon3D.assignPixelsToStackPadded(stackOut, Sa, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff);
            } else {
                DoubleCommon3D.assignPixelsToStack(stackOut, Sa);
            }
        } else {
            if (isPadded) {
                DoubleCommon3D.assignPixelsToStackPadded(stackOut, Sa, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, threshold);
            } else {
                DoubleCommon3D.assignPixelsToStack(stackOut, Sa, threshold);
            }
        }
        return stackOut;
    }

    /*
    public void update(double regParam, double threshold, EvStack imX) {
        log(name + ": updating");
        Pd = Sd.copy();
        Pd.assign(DoubleFunctions.mult(regParam * regParam));
        PSF = E1.copy();
        ((DoubleMatrix3D) PSF).assign(Pd, DoubleFunctions.plus);
        Sa = ((DoubleMatrix3D) B).copy();
        Sa.assign((DoubleMatrix3D) PSF, DoubleFunctions.div);
        ((DenseDoubleMatrix3D) Sa).idct3(true);
        log(name + ": finalizing");
        ImageStack stackOut = new ImageStack(bColumns, bRows);
        if (threshold == -1.0) {
            if (isPadded) {
                DoubleCommon3D.assignPixelsToStackPadded(stackOut, Sa, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY);
            } else {
                DoubleCommon3D.assignPixelsToStack(stackOut, Sa, cmY);
            }
        } else {
            if (isPadded) {
                DoubleCommon3D.assignPixelsToStackPadded(stackOut, Sa, bSlices, bRows, bColumns, bSlicesOff, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                DoubleCommon3D.assignPixelsToStack(stackOut, Sa, cmY, threshold);
            }
        }
        imX.setStack(imX.getTitle(), stackOut);
        DoubleCommon3D.convertImage(imX, output);
    }*/

    private static double gcvGTikDCT3D(DoubleMatrix3D Sa, DoubleMatrix3D Sd, DoubleMatrix3D Bhat) {
        DoubleMatrix3D sa = Sa.copy();
        DoubleMatrix3D sd = Sd.copy();
        DoubleMatrix3D bhat = Bhat.copy();
        sa.assign(DoubleFunctions.abs);
        bhat.assign(DoubleFunctions.abs);
        double[] tmp = sa.getMinLocation();
        double smin = tmp[0];
        tmp = sa.getMaxLocation();
        double smax = tmp[0];
        sa = sa.assign(DoubleFunctions.square);
        sd = sd.assign(DoubleFunctions.square);
        GTikFmin3D fmin = new GTikFmin3D(sa, sd, bhat);
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
