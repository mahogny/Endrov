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
package endrov.utilityUnsorted.deconvolution.spectral;

import cern.colt.matrix.AbstractMatrix2D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import edu.emory.mathcs.utils.ConcurrencyUtils;
import endrov.core.log.EvLog;
import endrov.typeImageset.EvPixels;
import endrov.utilityUnsorted.deconvolution.iterative.DoubleCommon2D;
import endrov.utilityUnsorted.deconvolution.spectral.SpectralEnums.SpectralPaddingType;
import endrov.utilityUnsorted.deconvolution.spectral.SpectralEnums.SpectralResizingType;

/**
 * 2D abstract spectral deconvolver.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public abstract class AbstractDoubleSpectralDeconvolver2D  {

		
		public static void log(String s)
			{
			EvLog.printLog(s);
			}


    /**
     * Blurred image
     */
    protected AbstractMatrix2D B;

    /**
     * Point Spread Function
     */
    protected AbstractMatrix2D PSF;


    /**
     * Number of columns in the padded blurred image.
     */
    protected int bColumnsPad;

    /**
     * Number of rows in the padded blurred image.
     */
    protected int bRowsPad;

    /**
     * Number of columns in the blurred image.
     */
    protected int bColumns;

    /**
     * Number of rows in the blurred image.
     */
    protected int bRows;

    /**
     * Offset for columns in the padded blurred image.
     */
    protected int bColumnsOff;

    /**
     * Offset for rows in the padded blurred image.
     */
    protected int bRowsOff;

    /**
     * The center of the PSF image.
     */
    protected int[] psfCenter;

    /**
     * True only if the blurred image is padded.
     */
    protected boolean isPadded = false;

    /**
     * the smallest positive value assigned to the restored image, all the
     * values less than the threshold are set to zero.
     */
    protected double threshold;

    /**
     * regularization parameter.
     */
    protected double regParam;

    /**
     * The name of a deconvolution algorithm.
     */
    protected String name;

    /**
     * Creates new instance of AbstractDoubleSpectralDeconvolver2D
     * 
     * @param name
     *            name of a deconvolution algorithm
     * @param imB
     *            blurred image
     * @param imPSF
     *            Point Spread Function image
     * @param resizing
     *            type of resizing
     * @param output
     *            type of output
     * @param padding
     *            type of padding
     * @param showPadded
     *            if true, then a padded image is displayed
     * @param regParam
     *            regularization parameter
     * @param threshold
     *            the smallest positive value assigned to the restored image
     */
    public AbstractDoubleSpectralDeconvolver2D(String name, EvPixels imB, EvPixels imPSF, SpectralResizingType resizing, SpectralPaddingType padding, double regParam, double threshold) {
        log(name + ": initializing");
        this.name = name;
        EvPixels ipB = imB;
        EvPixels ipPSF = imPSF;
        int kCols = ipPSF.getWidth();
        int kRows = ipPSF.getHeight();
        bColumns = ipB.getWidth();
        bRows = ipB.getHeight();
        if ((kRows > bRows) || (kCols > bColumns)) {
            throw new IllegalArgumentException("The PSF image cannot be larger than the blurred image.");
        }
        switch (resizing) {
        case NEXT_POWER_OF_TWO:
            if (ConcurrencyUtils.isPowerOf2(bRows)) {
                bRowsPad = bRows;
            } else {
                isPadded = true;
                bRowsPad = ConcurrencyUtils.nextPow2(bRows);
            }
            if (ConcurrencyUtils.isPowerOf2(bColumns)) {
                bColumnsPad = bColumns;
            } else {
                isPadded = true;
                bColumnsPad = ConcurrencyUtils.nextPow2(bColumns);
            }
            break;
        case NONE:
            bColumnsPad = bColumns;
            bRowsPad = bRows;
            break;
        default:
            throw new IllegalArgumentException("Unsupported resizing type.");
        }
        
        B = new DenseDoubleMatrix2D(bRows, bColumns);
        DoubleCommon2D.assignPixelsToMatrix((DoubleMatrix2D) B, ipB);
        if (isPadded) {
            switch (padding) {
            case PERIODIC:
                B = DoubleCommon2D.padPeriodic((DoubleMatrix2D) B, bColumnsPad, bRowsPad);
                break;
            case REFLEXIVE:
                B = DoubleCommon2D.padReflexive((DoubleMatrix2D) B, bColumnsPad, bRowsPad);
                break;
            default:
                throw new IllegalArgumentException("Unsupported padding type.");
            }
            bColumnsOff = (bColumnsPad - bColumns + 1) / 2;
            bRowsOff = (bRowsPad - bRows + 1) / 2;
        }
        PSF = new DenseDoubleMatrix2D(kRows, kCols);
        DoubleCommon2D.assignPixelsToMatrix((DoubleMatrix2D) PSF, ipPSF);
        double[] maxAndLoc = ((DoubleMatrix2D) PSF).getMaxLocation();
        psfCenter = new int[] { (int) maxAndLoc[1], (int) maxAndLoc[2] };
        ((DoubleMatrix2D) PSF).normalize();
        if ((kCols != bColumnsPad) || (kRows != bRowsPad)) {
            PSF = DoubleCommon2D.padZero(((DoubleMatrix2D) PSF), bColumnsPad, bRowsPad);
        }
        psfCenter[0] += (bRowsPad - kRows + 1) / 2;
        psfCenter[1] += (bColumnsPad - kCols + 1) / 2;
        this.regParam = regParam;
        this.threshold = threshold;
    }
    

}
