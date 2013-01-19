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
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import cern.jet.math.tdouble.DoubleFunctions;
import endrov.imageset.EvPixels;
import endrov.utilityUnsorted.deconvolution.iterative.DoubleCommon2D;
import endrov.utilityUnsorted.deconvolution.spectral.AbstractDoubleSpectralDeconvolver2D;
import endrov.utilityUnsorted.deconvolution.spectral.SpectralEnums.SpectralPaddingType;
import endrov.utilityUnsorted.deconvolution.spectral.SpectralEnums.SpectralResizingType;

/**
 * 2D Tikhonov with reflexive boundary conditions.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class DoubleReflexiveTikhonov2D extends AbstractDoubleSpectralDeconvolver2D {

    private DoubleMatrix2D E1;

    private DoubleMatrix2D S;

    /**
     * Creates new instance of DoubleTikDCT2D
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
    public DoubleReflexiveTikhonov2D(EvPixels imB, EvPixels imPSF, SpectralResizingType resizing, double regParam, double threshold) {
        super("Tikhonov", imB, imPSF, resizing, SpectralPaddingType.REFLEXIVE, regParam, threshold);
    }

    public EvPixels internalDeconvolve() {
        log(name + ": deconvolving");
        E1 = new DenseDoubleMatrix2D(bRowsPad, bColumnsPad);
        E1.setQuick(0, 0, 1);
        ((DenseDoubleMatrix2D) B).dct2(true);
        S = DoubleCommon2D.dctShift((DoubleMatrix2D) PSF, psfCenter);
        ((DenseDoubleMatrix2D) S).dct2(true);
        ((DenseDoubleMatrix2D) E1).dct2(true);
        S.assign(E1, DoubleFunctions.div);
        if (regParam == -1) {
            log(name + ": computing regularization parameter");
            regParam = gcvTikDCT2D(S, (DoubleMatrix2D) B);
        }
        log(name + ": deconvolving");
        PSF = S.copy();
        ((DoubleMatrix2D) PSF).assign(DoubleFunctions.square);
        E1 = ((DoubleMatrix2D) PSF).copy();
        ((DoubleMatrix2D) PSF).assign(DoubleFunctions.plus(regParam * regParam));
        ((DoubleMatrix2D) B).assign(S, DoubleFunctions.mult);
        S = ((DoubleMatrix2D) B).copy();
        S.assign((DoubleMatrix2D) PSF, DoubleFunctions.div);
        ((DenseDoubleMatrix2D) S).idct2(true);
        log(name + ": finalizing");
        if (threshold == -1) {
            if (isPadded) {
                return DoubleCommon2D.assignPixelsToProcessorPadded(S, bRows, bColumns, bRowsOff, bColumnsOff);
            } else {
                return DoubleCommon2D.assignPixelsToProcessor(bRows, bColumns, S);
            }
        } else {
            if (isPadded) {
                return DoubleCommon2D.assignPixelsToProcessorPadded(S, bRows, bColumns, bRowsOff, bColumnsOff, threshold);
            } else {
                return DoubleCommon2D.assignPixelsToProcessor(S, threshold);
            }
        }

    }
/*
    public void update(double regParam, double threshold, EvPixels imX) {
        log(name + ": updating");
        PSF = E1.copy();
        ((DoubleMatrix2D) PSF).assign(DoubleFunctions.plus(regParam * regParam));
        S = ((DoubleMatrix2D) B).copy();
        S.assign((DoubleMatrix2D) PSF, DoubleFunctions.div);
        ((DenseDoubleMatrix2D) S).idct2(true);
        log(name + ": finalizing");
        FloatProcessor ip = new FloatProcessor(bColumns, bRows);
        if (threshold == -1) {
            if (isPadded) {
                DoubleCommon2D.assignPixelsToProcessorPadded(ip, S, bRows, bColumns, bRowsOff, bColumnsOff, cmY);
            } else {
                DoubleCommon2D.assignPixelsToProcessor(ip, S, cmY);
            }
        } else {
            if (isPadded) {
                DoubleCommon2D.assignPixelsToProcessorPadded(ip, S, bRows, bColumns, bRowsOff, bColumnsOff, cmY, threshold);
            } else {
                DoubleCommon2D.assignPixelsToProcessor(ip, S, cmY, threshold);
            }
        }
        imX.setProcessor(imX.getTitle(), ip);
        DoubleCommon2D.convertImage(imX, output);

    }*/

    private static double gcvTikDCT2D(DoubleMatrix2D S, DoubleMatrix2D Bhat) {
        DoubleMatrix2D s = S.copy();
        DoubleMatrix2D bhat = Bhat.copy();
        s.assign(DoubleFunctions.abs);
        bhat.assign(DoubleFunctions.abs);
        double[] tmp = s.getMinLocation();
        double smin = tmp[0];
        tmp = s.getMaxLocation();
        double smax = tmp[0];
        s.assign(DoubleFunctions.square);
        TikFmin2D fmin = new TikFmin2D(s, bhat);
        return (double) DoubleFmin.fmin(smin, smax, fmin, DoubleCommon2D.FMIN_TOL);
    }

    private static class TikFmin2D implements DoubleFmin_methods {
        DoubleMatrix2D ssquare;

        DoubleMatrix2D bhat;

        public TikFmin2D(DoubleMatrix2D ssquare, DoubleMatrix2D bhat) {
            this.ssquare = ssquare;
            this.bhat = bhat;
        }

        public double f_to_minimize(double regParam) {
            DoubleMatrix2D sloc = ssquare.copy();
            DoubleMatrix2D bhatloc = bhat.copy();

            sloc.assign(DoubleFunctions.plus(regParam * regParam));
            sloc.assign(DoubleFunctions.inv);
            bhatloc.assign(sloc, DoubleFunctions.mult);
            bhatloc.assign(DoubleFunctions.square);
            double ss = sloc.zSum();
            return bhatloc.zSum() / (ss * ss);
        }

    }

}
