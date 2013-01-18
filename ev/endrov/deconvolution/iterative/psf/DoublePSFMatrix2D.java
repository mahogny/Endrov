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

package endrov.deconvolution.iterative.psf;

import cern.colt.matrix.AbstractMatrix2D;
import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import cern.jet.math.tdcomplex.DComplexFunctions;
import cern.jet.math.tdouble.DoubleFunctions;
import edu.emory.mathcs.utils.ConcurrencyUtils;
import endrov.deconvolution.iterative.DoubleCommon2D;
import endrov.deconvolution.iterative.IterativeEnums.BoundaryType;
import endrov.deconvolution.iterative.IterativeEnums.PSFType;
import endrov.deconvolution.iterative.IterativeEnums.PaddingType;
import endrov.deconvolution.iterative.IterativeEnums.ResizingType;
import endrov.imageset.EvPixels;

/**
 * This class is used for matrix-vector multiplications involving PSF images.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class DoublePSFMatrix2D {

    private BoundaryType boundary;

    private ResizingType resizing;

    private PSFType type;

    private DoublePSF2D PSF;

    private int[] invPsfSize;

    private int[] invPadSize;

    private int[] varPsfSize;

    private int[] varPadSize;

    private int[] varNregions;

    private int[] varRsize;

    private int[] varPadSize1;

    private RegionIndices varRi;

    private RegionIndices varRiTr;

    private int[] imSize;

    private int[] varEpadSize;

    private RegionIndices varEri;

    private RegionIndices varTri;

    private DComplexMatrix2D[][] matdata;

    /**
     * Creates a new instance of DoublePSFMatrix2D.
     * 
     * @param imPSF
     *            array of PSF images
     * @param boundary
     *            type of boundary conditions
     */
    public DoublePSFMatrix2D(EvPixels[][] imPSF, BoundaryType boundary, ResizingType resizing, int[] imSize) {
        PSF = new DoublePSF2D(imPSF);
        this.boundary = boundary;
        this.resizing = resizing;
        this.imSize = new int[2];
        this.imSize[0] = imSize[0];
        this.imSize[1] = imSize[1];
        if (PSF.getNumberOfImages() > 1) {
            type = PSFType.VARIANT;
            varPadSize = new int[2];
            varPsfSize = PSF.getSize();
            varPadSize[0] = (int) Math.round((double) varPsfSize[0] / 2.0);
            varPadSize[1] = (int) Math.round((double) varPsfSize[1] / 2.0);
            constructMatrix();
            varNregions = new int[2];
            varNregions[0] = matdata.length;
            varNregions[1] = matdata[0].length;
            varRsize = new int[2];
            varRsize[0] = (int) Math.ceil((double) imSize[0] / (double) varNregions[0]);
            varRsize[1] = (int) Math.ceil((double) imSize[1] / (double) varNregions[1]);
            varPadSize1 = new int[2];
            varPadSize1[0] = varRsize[0] * varNregions[0] - imSize[0];
            varPadSize1[1] = varRsize[1] * varNregions[1] - imSize[1];
            varRi = regionIndices(varNregions, varRsize);
            varRiTr = regionIndices(varNregions, varRsize);
            for (int i = 0; i < varRiTr.iidx.length; i++) {
                for (int j = 0; j < varRiTr.iidx[0].length; j++) {
                    varRiTr.iidx[i][j] = varRiTr.iidx[i][j] + varPadSize[0];
                }
            }
            varRiTr.iidx[0][0] = 0;
            varRiTr.iidx[varRiTr.iidx.length - 1][varRiTr.iidx[0].length - 1] = varRiTr.iidx[varRiTr.iidx.length - 1][varRiTr.iidx[0].length - 1] + varPadSize[0];
            for (int i = 0; i < varRiTr.jidx.length; i++) {
                for (int j = 0; j < varRiTr.jidx[0].length; j++) {
                    varRiTr.jidx[i][j] = varRiTr.jidx[i][j] + varPadSize[1];
                }
            }
            varRiTr.jidx[0][0] = 0;
            varRiTr.jidx[varRiTr.jidx.length - 1][varRiTr.jidx[0].length - 1] = varRiTr.jidx[varRiTr.jidx.length - 1][varRiTr.jidx[0].length - 1] + varPadSize[1];
            varEpadSize = new int[2];
            varEpadSize[0] = 2 * varPadSize[0];
            varEpadSize[1] = 2 * varPadSize[1];
            varEri = eregionIndices(varRi.iidx, varRi.jidx, varNregions, varEpadSize);
            varTri = tregionIndices(varRiTr.iidx, varRiTr.jidx, varNregions, varPadSize);

        } else {
            type = PSFType.INVARIANT;
            invPsfSize = PSF.getSize();
            int[] minimal = new int[2];
            minimal[0] = invPsfSize[0] + imSize[0];
            minimal[1] = invPsfSize[1] + imSize[1];
            switch (resizing) {
            case AUTO:
                int[] nextPowTwo = new int[2];
                if (!ConcurrencyUtils.isPowerOf2(minimal[0])) {
                    nextPowTwo[0] = ConcurrencyUtils.nextPow2(minimal[0]);
                } else {
                    nextPowTwo[0] = minimal[0];
                }
                if (!ConcurrencyUtils.isPowerOf2(minimal[1])) {
                    nextPowTwo[1] = ConcurrencyUtils.nextPow2(minimal[1]);
                } else {
                    nextPowTwo[1] = minimal[1];
                }
                if ((nextPowTwo[0] >= 1.5 * minimal[0]) || (nextPowTwo[1] >= 1.5 * minimal[1])) {
                    //use minimal padding
                    invPsfSize[0] = minimal[0];
                    invPsfSize[1] = minimal[1];
                } else {
                    invPsfSize[0] = nextPowTwo[0];
                    invPsfSize[1] = nextPowTwo[1];
                }
                break;
            case MINIMAL:
                invPsfSize[0] = minimal[0];
                invPsfSize[1] = minimal[1];
                break;
            case NEXT_POWER_OF_TWO:
                invPsfSize[0] = minimal[0];
                invPsfSize[1] = minimal[1];
                if (!ConcurrencyUtils.isPowerOf2(invPsfSize[0])) {
                    invPsfSize[0] = ConcurrencyUtils.nextPow2(invPsfSize[0]);
                }
                if (!ConcurrencyUtils.isPowerOf2(invPsfSize[1])) {
                    invPsfSize[1] = ConcurrencyUtils.nextPow2(invPsfSize[1]);
                }
                break;
            }
            invPadSize = new int[2];
            if (imSize[0] < invPsfSize[0]) {
                invPadSize[0] = (invPsfSize[0] - imSize[0] + 1) / 2;
            }
            if (imSize[1] < invPsfSize[1]) {
                invPadSize[1] = (invPsfSize[1] - imSize[1] + 1) / 2;
            }
            constructMatrix();
        }
    }

    /**
     * Computes matrix-vector multiplication: A*b or A'*b.
     * 
     * @param b
     *            vector
     * @param transpose
     *            if true then A'*b is computed
     * @return A*b or A'*b
     */
    public DoubleMatrix1D times(DoubleMatrix1D b, boolean transpose) {
        DoubleMatrix2D B = null;
        if (b.isView()) {
            B = new DenseDoubleMatrix2D(imSize[0], imSize[1], (double[]) b.copy().elements(), 0, 0, imSize[1], 1, false);
        } else {
            B = new DenseDoubleMatrix2D(imSize[0], imSize[1], (double[]) b.elements(), 0, 0, imSize[1], 1, false);
        }
        B = times(B, transpose);
        return new DenseDoubleMatrix1D(B.size(), (double[]) B.elements(), 0, 1, false);
    }

    /**
     * Computes matrix-vector multiplication: A*B or A'*B.
     * 
     * @param B
     *            vector
     * @param transpose
     *            if true then A'*B is computed
     * @return A*B or A'*B
     */
    public DoubleMatrix2D times(DoubleMatrix2D B, boolean transpose) {
        DoubleMatrix2D Bpad = null;
        switch (type) {
        case INVARIANT:
            switch (boundary) {
            case ZERO:
                Bpad = DoubleCommon2D.padZero(B, invPsfSize[0], invPsfSize[1]);
                break;
            case PERIODIC:
                Bpad = DoubleCommon2D.padPeriodic(B, invPsfSize[0], invPsfSize[1]);
                break;
            case REFLEXIVE:
                Bpad = DoubleCommon2D.padReflexive(B, invPsfSize[0], invPsfSize[1]);
                break;
            }
            return invariantMultiply(Bpad, transpose);
        case VARIANT:
            switch (boundary) {
            case ZERO:
                Bpad = DoubleCommon2D.padZero(B, imSize[0] + 2 * varPadSize[0], imSize[1] + 2 * varPadSize[1]);
                break;
            case REFLEXIVE:
                Bpad = DoubleCommon2D.padReflexive(B, imSize[0] + 2 * varPadSize[0], imSize[1] + 2 * varPadSize[1]);
                break;
            case PERIODIC:
                Bpad = DoubleCommon2D.padPeriodic(B, imSize[0] + 2 * varPadSize[0], imSize[1] + 2 * varPadSize[1]);
                break;
            }
            if (transpose) {
                return variantTransposeMultiply(Bpad);
            } else {
                return variantMultiply(Bpad);
            }
        default:
            return null;
        }
    }

    /**
     * Returns information about PSF images.
     * 
     * @return information about PSF images
     */
    public DoublePSF2D getPSF() {
        return PSF;
    }

    /**
     * Returns the size of PSF.
     * 
     * @return the size of PSF
     */
    public int[] getSize() {
        int[] psfSize = PSF.getSize();
        psfSize[0] = psfSize[0] * psfSize[0];
        psfSize[1] = psfSize[1] * psfSize[1];
        return psfSize;
    }

    /**
     * Returns the type of boundary conditions.
     * 
     * @return the type of boundary conditions
     */
    public BoundaryType getBoundary() {
        return boundary;
    }

    /**
     * Returns the spatially invariant PSF size (after padding).
     * 
     * @return the spatially invariant PSF size (after padding)
     */
    public int[] getInvPsfSize() {
        return invPsfSize;
    }

    /**
     * Returns the padding size for spatially invariant PSF.
     * 
     * @return the padding size for spatially invariant PSF
     */
    public int[] getInvPadSize() {
        return invPadSize;
    }

    /**
     * Returns the type of resizing.
     * 
     * @return the type of resizing
     */
    public ResizingType getResizing() {
        return resizing;
    }

    /**
     * Returns the type of PSF
     * 
     * @return the type of PSF
     */
    public PSFType getType() {
        return type;
    }

    private DoubleMatrix2D invariantMultiply(AbstractMatrix2D Bpad, boolean transpose) {
        Bpad = ((DenseDoubleMatrix2D) Bpad).getFft2();
        if (transpose) {
            ((DComplexMatrix2D) Bpad).assign(matdata[0][0], DComplexFunctions.multConjSecond);
        } else {
            ((DComplexMatrix2D) Bpad).assign(matdata[0][0], DComplexFunctions.mult);
        }
        ((DenseDComplexMatrix2D) Bpad).ifft2(true);
        return ((DComplexMatrix2D) Bpad).viewPart(invPadSize[0], invPadSize[1], imSize[0], imSize[1]).getRealPart();
    }

    private DoubleMatrix2D variantMultiply(DoubleMatrix2D Bpad) {
        if ((varPadSize1[0] > 0) || (varPadSize1[1] > 0)) {
            Bpad = DoubleCommon2D.padZero(Bpad, varPadSize1, PaddingType.POST);
        }
        DoubleMatrix2D Y = new DenseDoubleMatrix2D(Bpad.rows(), Bpad.columns());
        for (int i = 0; i < varNregions[0]; i++) {
            for (int j = 0; j < varNregions[1]; j++) {
                DoubleMatrix2D Xt = Bpad.viewPart(varEri.iidx[i][0], varEri.jidx[j][0], varEri.iidx[i][1] - varEri.iidx[i][0] + 1, varEri.jidx[j][1] - varEri.jidx[j][0] + 1);
                DoubleMatrix2D Yt = variantMultiplyOnePsf(i, j, Xt, false);
                Y.viewPart(varRi.iidx[i][0], varRi.jidx[j][0], varRi.iidx[i][1] - varRi.iidx[i][0] + 1, varRi.jidx[j][1] - varRi.jidx[j][0] + 1).assign(Yt);
            }
        }
        Y = Y.viewPart(0, 0, imSize[0], imSize[1]).copy();
        return Y;
    }

    private DoubleMatrix2D variantTransposeMultiply(DoubleMatrix2D Bpad) {
        if ((varPadSize1[0] > 0) || (varPadSize1[1] > 0)) {
            Bpad = DoubleCommon2D.padZero(Bpad, varPadSize1, PaddingType.POST);
        }
        DoubleMatrix2D Y = new DenseDoubleMatrix2D(Bpad.rows(), Bpad.columns());
        int[] eregionSize = new int[2];
        for (int i = 0; i < varNregions[0]; i++) {
            for (int j = 0; j < varNregions[1]; j++) {
                eregionSize[0] = varEri.iidx[i][1] - varEri.iidx[i][0] + 1;
                eregionSize[1] = varEri.jidx[j][1] - varEri.jidx[j][0] + 1;
                DoubleMatrix2D Xt = new DenseDoubleMatrix2D(eregionSize[0], eregionSize[1]);
                Xt.viewPart(varTri.iidx[i][0], varTri.jidx[j][0], varTri.iidx[i][1] - varTri.iidx[i][0] + 1, varTri.jidx[j][1] - varTri.jidx[j][0] + 1).assign(
                        Bpad.viewPart(varRiTr.iidx[i][0], varRiTr.jidx[j][0], varRiTr.iidx[i][1] - varRiTr.iidx[i][0] + 1, varRiTr.jidx[j][1] - varRiTr.jidx[j][0] + 1));
                Xt = DoubleCommon2D.padZero(Xt, varPadSize, PaddingType.BOTH);
                DoubleMatrix2D Yt = variantMultiplyOnePsf(i, j, Xt, true);
                Y.viewPart(varEri.iidx[i][0], varEri.jidx[j][0], varEri.iidx[i][1] - varEri.iidx[i][0] + 1, varEri.jidx[j][1] - varEri.jidx[j][0] + 1).assign(
                        Y.viewPart(varEri.iidx[i][0], varEri.jidx[j][0], varEri.iidx[i][1] - varEri.iidx[i][0] + 1, varEri.jidx[j][1] - varEri.jidx[j][0] + 1).assign(Yt, DoubleFunctions.plus));
            }
        }
        int[][] idx = new int[2][2];
        idx[0][0] = varPadSize[0];
        idx[0][1] = varPadSize[1];
        idx[1][0] = varPadSize[0] + imSize[0];
        idx[1][1] = varPadSize[1] + imSize[1];
        Y = Y.viewPart(idx[0][0], idx[0][1], idx[1][0] - idx[0][0], idx[1][1] - idx[0][1]).copy();
        return Y;
    }

    private DoubleMatrix2D variantMultiplyOnePsf(int row, int col, DoubleMatrix2D X, boolean transpose) {
        int[] imSize = new int[2];
        imSize[0] = X.rows() - 2 * varPadSize[0];
        imSize[1] = X.columns() - 2 * varPadSize[1];
        PartitionInfo pi = partitionInfo(imSize, varPadSize);
        int[] padSize1 = new int[2];
        padSize1[0] = pi.rsize[0] * pi.nregions[0] - imSize[0];
        padSize1[1] = pi.rsize[1] * pi.nregions[1] - imSize[1];
        if ((padSize1[0] > 0) || padSize1[1] > 0) {
            X = DoubleCommon2D.padZero(X, padSize1, PaddingType.POST);
        }
        RegionIndices ri = regionIndices(pi.nregions, pi.rsize);
        int[] epadSize = new int[2];
        epadSize[0] = 2 * varPadSize[0];
        epadSize[1] = 2 * varPadSize[1];
        RegionIndices eri = eregionIndices(ri.iidx, ri.jidx, pi.nregions, epadSize);
        int[][] tidx = new int[2][2];
        tidx[0][0] = varPadSize[0];
        tidx[0][1] = varPadSize[1];
        tidx[1][0] = varPadSize[0] + pi.rsize[0] - 1;
        tidx[1][1] = varPadSize[1] + pi.rsize[1] - 1;

        DoubleMatrix2D Y = new DenseDoubleMatrix2D(X.rows(), X.columns());
        for (int i = 0; i < pi.nregions[0]; i++) {
            for (int j = 0; j < pi.nregions[1]; j++) {
                DoubleMatrix2D Xt = X.viewPart(eri.iidx[i][0], eri.jidx[j][0], eri.iidx[i][1] - eri.iidx[i][0] + 1, eri.jidx[j][1] - eri.jidx[j][0] + 1);
                DoubleMatrix2D Yt = multiplyOneRegion(row, col, Xt, transpose);
                Y.viewPart(ri.iidx[i][0], ri.jidx[j][0], ri.iidx[i][1] - ri.iidx[i][0] + 1, ri.jidx[j][1] - ri.jidx[j][0] + 1).assign(Yt.viewPart(tidx[0][0], tidx[0][1], tidx[1][0] - tidx[0][0] + 1, tidx[1][1] - tidx[0][1] + 1));
            }
        }
        Y = Y.viewPart(0, 0, imSize[0], imSize[1]);
        return Y;

    }

    private DoubleMatrix2D multiplyOneRegion(int row, int col, AbstractMatrix2D X, boolean transpose) {
        int rowsX = X.rows();
        int colsX = X.columns();
        int[] padSize = new int[2];
        padSize[0] = matdata[row][col].rows() - rowsX;
        padSize[1] = matdata[row][col].columns() - colsX;
        if ((padSize[0] > 0) || padSize[1] > 0) {
            X = DoubleCommon2D.padZero((DoubleMatrix2D) X, padSize, PaddingType.POST);
        } else {
            if (X.isView()) {
                X = ((DoubleMatrix2D) X).copy();
            }
        }
        X = ((DenseDoubleMatrix2D) X).getFft2();
        if (transpose) {
            ((DComplexMatrix2D) X).assign(matdata[row][col], DComplexFunctions.multConjSecond);
        } else {
            ((DComplexMatrix2D) X).assign(matdata[row][col], DComplexFunctions.mult);
        }
        ((DenseDComplexMatrix2D) X).ifft2(true);
        X = ((DComplexMatrix2D) X).getRealPart();
        X = ((DoubleMatrix2D) X).viewPart(0, 0, rowsX, colsX);
        return (DoubleMatrix2D) X;
    }

    private PartitionInfo partitionInfo(int[] imSize, int[] padSize) {
        int[] psfSize = new int[2];
        if (padSize[0] == 0) {
            psfSize[0] = 1;
        } else {
            psfSize[0] = 2 * padSize[0];
        }
        if (padSize[1] == 0) {
            psfSize[1] = 1;
        } else {
            psfSize[1] = 2 * padSize[1];
        }
        PartitionInfo pi = new PartitionInfo();
        pi.rsize[0] = Math.min(psfSize[0], imSize[0]);
        pi.rsize[1] = Math.min(psfSize[1], imSize[1]);
        pi.nregions[0] = (int) Math.ceil((double) imSize[0] / (double) pi.rsize[0]);
        pi.nregions[1] = (int) Math.ceil((double) imSize[1] / (double) pi.rsize[1]);
        return pi;
    }

    private class PartitionInfo {
        public final int[] nregions = new int[2];

        public final int[] rsize = new int[2];
    }

    private RegionIndices regionIndices(int[] nregions, int[] rsize) {
        RegionIndices ri = new RegionIndices(nregions);
        ri.iidx[0][0] = 0;
        ri.iidx[0][1] = rsize[0] - 1;
        ri.jidx[0][0] = 0;
        ri.jidx[0][1] = rsize[1] - 1;
        for (int i = 1; i < nregions[0]; i++) {
            ri.iidx[i][0] = ri.iidx[i - 1][1] + 1;
            ri.iidx[i][1] = ri.iidx[i][0] + rsize[0] - 1;
        }
        for (int i = 1; i < nregions[1]; i++) {
            ri.jidx[i][0] = ri.jidx[i - 1][1] + 1;
            ri.jidx[i][1] = ri.jidx[i][0] + rsize[1] - 1;
        }
        return ri;
    }

    private RegionIndices eregionIndices(int[][] iidx, int[][] jidx, int[] nregions, int[] rsize) {
        RegionIndices eri = new RegionIndices(nregions);
        for (int i = 0; i < nregions[0]; i++) {
            eri.iidx[i][0] = iidx[i][0];
            eri.iidx[i][1] = iidx[i][1] + 2 * (int) Math.floor((double) rsize[0] / 2.0);
        }
        for (int i = 0; i < nregions[1]; i++) {
            eri.jidx[i][0] = jidx[i][0];
            eri.jidx[i][1] = jidx[i][1] + 2 * (int) Math.floor((double) rsize[1] / 2.0);
        }
        return eri;
    }

    private RegionIndices tregionIndices(int[][] iidx, int[][] jidx, int[] nregions, int[] padSize) {
        RegionIndices tri = new RegionIndices(nregions);
        for (int i = 1; i < nregions[0]; i++) {
            tri.iidx[i][0] = padSize[0];
        }
        for (int i = 0; i < nregions[0] - 1; i++) {
            tri.iidx[i][1] = iidx[0][1];
        }
        if (nregions[0] > 1) {
            tri.iidx[nregions[0] - 1][1] = iidx[0][1] + padSize[0];
        } else {
            tri.iidx[nregions[0] - 1][1] = iidx[0][1];
        }
        for (int i = 1; i < nregions[1]; i++) {
            tri.jidx[i][0] = padSize[1];
        }
        for (int i = 0; i < nregions[1] - 1; i++) {
            tri.jidx[i][1] = jidx[0][1];
        }
        if (nregions[1] > 1) {
            tri.jidx[nregions[1] - 1][1] = jidx[0][1] + padSize[1];
        } else {
            tri.jidx[nregions[1] - 1][1] = jidx[0][1];
        }
        return tri;
    }

    private class RegionIndices {

        public final int[][] iidx;

        public final int[][] jidx;

        public RegionIndices(int[] nregions) {
            iidx = new int[nregions[0]][2];
            jidx = new int[nregions[1]][2];
        }

    }

    private void constructMatrix() {
        DoubleMatrix2D[][] image = PSF.getImage();
        int[][][] center = PSF.getCenter();
        switch (type) {
        case INVARIANT:
            matdata = new DComplexMatrix2D[1][1];
            matdata[0][0] = onePsfMatrixInvariant(image[0][0], center[0][0]);
            break;
        case VARIANT:
            matdata = new DComplexMatrix2D[image.length][image[0].length];
            for (int i = 0; i < matdata.length; i++) {
                for (int j = 0; j < matdata[0].length; j++) {
                    matdata[i][j] = onePsfMatrixVariant(image[i][j], center[i][j]);
                }
            }
            break;
        default:
            return;
        }
    }

    private DComplexMatrix2D onePsfMatrixInvariant(DoubleMatrix2D image, int[] center) {
        image.normalize();
        int rows = image.rows();
        int columns = image.columns();
        AbstractMatrix2D matdata = new DenseDoubleMatrix2D(invPsfSize[0], invPsfSize[1]);
        ((DoubleMatrix2D) matdata).viewPart(0, 0, rows, columns).assign(image);
        matdata = DoubleCommon2D.circShift((DoubleMatrix2D) matdata, center);
        matdata = ((DenseDoubleMatrix2D) matdata).getFft2();
        return (DComplexMatrix2D) matdata;
    }

    private DComplexMatrix2D onePsfMatrixVariant(DoubleMatrix2D image, int[] center) {
        image.normalize();
        int columns = 2 * image.columns();
        int rows = 2 * image.rows();

        switch (resizing) {
        case AUTO:
            int[] nextPowTwo = new int[2];
            if (!ConcurrencyUtils.isPowerOf2(rows)) {
                nextPowTwo[0] = ConcurrencyUtils.nextPow2(rows);
            } else {
                nextPowTwo[0] = rows;
            }
            if (!ConcurrencyUtils.isPowerOf2(columns)) {
                nextPowTwo[1] = ConcurrencyUtils.nextPow2(columns);
            } else {
                nextPowTwo[1] = columns;
            }
            if ((nextPowTwo[0] < 1.5 * rows) && (nextPowTwo[1] < 1.5 * columns)) {
                rows = nextPowTwo[0];
                columns = nextPowTwo[1];
            }
            break;
        case NEXT_POWER_OF_TWO:
            if (!ConcurrencyUtils.isPowerOf2(rows)) {
                rows = ConcurrencyUtils.nextPow2(rows);
            }
            if (!ConcurrencyUtils.isPowerOf2(columns)) {
                columns = ConcurrencyUtils.nextPow2(columns);
            }
            break;
        case MINIMAL:
            break;
        }
        AbstractMatrix2D matdata = new DenseDoubleMatrix2D(rows, columns);
        ((DoubleMatrix2D) matdata).viewPart(0, 0, image.rows(), image.columns()).assign(image);
        matdata = DoubleCommon2D.circShift((DoubleMatrix2D) matdata, center);
        matdata = ((DenseDoubleMatrix2D) matdata).getFft2();
        return (DComplexMatrix2D) matdata;
    }
}
