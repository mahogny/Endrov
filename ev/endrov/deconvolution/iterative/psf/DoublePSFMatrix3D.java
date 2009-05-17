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

import cern.colt.matrix.AbstractMatrix3D;
import cern.colt.matrix.tdcomplex.DComplexMatrix3D;
import cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix3D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix3D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix3D;
import cern.jet.math.tdcomplex.DComplexFunctions;
import cern.jet.math.tdouble.DoubleFunctions;
import edu.emory.mathcs.utils.ConcurrencyUtils;
import endrov.deconvolution.iterative.DoubleCommon3D;
import endrov.deconvolution.iterative.IterativeEnums.BoundaryType;
import endrov.deconvolution.iterative.IterativeEnums.PSFType;
import endrov.deconvolution.iterative.IterativeEnums.PaddingType;
import endrov.deconvolution.iterative.IterativeEnums.ResizingType;
import endrov.imageset.EvStack;

/**
 * This class is used for matrix-vector multiplications involving PSF images.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class DoublePSFMatrix3D {

    private static final long serialVersionUID = -7503394849184235286L;

    private BoundaryType boundary;

    private ResizingType resizing;

    private PSFType type;

    private DoublePSF3D PSF;

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

    private DComplexMatrix3D[][][] matdata;

    /**
     * Creates a new instance of DoublePSFMatrix3D.
     * 
     * @param imPSF
     *            array of PSF images
     * @param boundary
     *            type of boundary conditions
     */
    public DoublePSFMatrix3D(EvStack[][][] imPSF, BoundaryType boundary, ResizingType resizing, int[] imSize) {
        PSF = new DoublePSF3D(imPSF);
        this.boundary = boundary;
        this.resizing = resizing;
        this.imSize = new int[3];
        this.imSize[0] = imSize[0];
        this.imSize[1] = imSize[1];
        this.imSize[2] = imSize[2];
        if (PSF.getNumberOfImages() > 1) {
            type = PSFType.VARIANT;
            varPadSize = new int[3];
            varPsfSize = PSF.getSize();
            varPadSize[0] = (int) Math.round((double) varPsfSize[0] / 2.0);
            varPadSize[1] = (int) Math.round((double) varPsfSize[1] / 2.0);
            varPadSize[2] = (int) Math.round((double) varPsfSize[2] / 2.0);
            constructMatrix();
            varNregions = new int[3];
            varNregions[0] = matdata.length;
            varNregions[1] = matdata[0].length;
            varNregions[2] = matdata[0][0].length;
            varRsize = new int[3];
            varRsize[0] = (int) Math.ceil((double) imSize[0] / (double) varNregions[0]);
            varRsize[1] = (int) Math.ceil((double) imSize[1] / (double) varNregions[1]);
            varRsize[2] = (int) Math.ceil((double) imSize[2] / (double) varNregions[2]);
            varPadSize1 = new int[3];
            varPadSize1[0] = varRsize[0] * varNregions[0] - imSize[0];
            varPadSize1[1] = varRsize[1] * varNregions[1] - imSize[1];
            varPadSize1[2] = varRsize[2] * varNregions[2] - imSize[2];
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

            for (int i = 0; i < varRiTr.kidx.length; i++) {
                for (int j = 0; j < varRiTr.kidx[0].length; j++) {
                    varRiTr.kidx[i][j] = varRiTr.kidx[i][j] + varPadSize[2];
                }
            }
            varRiTr.kidx[0][0] = 0;
            varRiTr.kidx[varRiTr.kidx.length - 1][varRiTr.kidx[0].length - 1] = varRiTr.kidx[varRiTr.kidx.length - 1][varRiTr.kidx[0].length - 1] + varPadSize[2];

            varEpadSize = new int[3];
            varEpadSize[0] = 2 * varPadSize[0];
            varEpadSize[1] = 2 * varPadSize[1];
            varEpadSize[2] = 2 * varPadSize[2];
            varEri = eregionIndices(varRi.iidx, varRi.jidx, varRi.kidx, varNregions, varEpadSize);
            varTri = tregionIndices(varRiTr.iidx, varRiTr.jidx, varRiTr.kidx, varNregions, varPadSize);

        } else {
            type = PSFType.INVARIANT;
            invPsfSize = PSF.getSize();
            int[] minimal = new int[3];
            minimal[0] = invPsfSize[0] + imSize[0];
            minimal[1] = invPsfSize[1] + imSize[1];
            minimal[2] = invPsfSize[2] + imSize[2];
            switch (resizing) {
            case AUTO:
                int[] nextPowTwo = new int[3];
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
                if (!ConcurrencyUtils.isPowerOf2(minimal[2])) {
                    nextPowTwo[2] = ConcurrencyUtils.nextPow2(minimal[2]);
                } else {
                    nextPowTwo[2] = minimal[2];
                }
                if ((nextPowTwo[0] >= 1.5 * minimal[0]) || (nextPowTwo[1] >= 1.5 * minimal[1]) || (nextPowTwo[2] >= 1.5 * minimal[2])) {
                    //use minimal padding
                    invPsfSize[0] = minimal[0];
                    invPsfSize[1] = minimal[1];
                    invPsfSize[2] = minimal[2];
                } else {
                    invPsfSize[0] = nextPowTwo[0];
                    invPsfSize[1] = nextPowTwo[1];
                    invPsfSize[2] = nextPowTwo[2];
                }
                break;
            case MINIMAL:
                invPsfSize[0] = minimal[0];
                invPsfSize[1] = minimal[1];
                invPsfSize[2] = minimal[2];
                break;
            case NEXT_POWER_OF_TWO:
                invPsfSize[0] = minimal[0];
                invPsfSize[1] = minimal[1];
                invPsfSize[2] = minimal[2];
                if (!ConcurrencyUtils.isPowerOf2(invPsfSize[0])) {
                    invPsfSize[0] = ConcurrencyUtils.nextPow2(invPsfSize[0]);
                }
                if (!ConcurrencyUtils.isPowerOf2(invPsfSize[1])) {
                    invPsfSize[1] = ConcurrencyUtils.nextPow2(invPsfSize[1]);
                }
                if (!ConcurrencyUtils.isPowerOf2(invPsfSize[2])) {
                    invPsfSize[2] = ConcurrencyUtils.nextPow2(invPsfSize[2]);
                }
                break;
            }
            invPadSize = new int[3];
            if (imSize[0] < invPsfSize[0]) {
                invPadSize[0] = (invPsfSize[0] - imSize[0] + 1) / 2;
            }
            if (imSize[1] < invPsfSize[1]) {
                invPadSize[1] = (invPsfSize[1] - imSize[1] + 1) / 2;
            }
            if (imSize[2] < invPsfSize[2]) {
                invPadSize[2] = (invPsfSize[2] - imSize[2] + 1) / 2;
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
        DoubleMatrix3D B = null;
        if (b.isView()) {
            B = new DenseDoubleMatrix3D(imSize[0], imSize[1], imSize[2], (double[]) b.copy().elements(), 0, 0, 0, imSize[1] * imSize[2], imSize[2], 1, false);
        } else {
            B = new DenseDoubleMatrix3D(imSize[0], imSize[1], imSize[2], (double[]) b.elements(), 0, 0, 0, imSize[1] * imSize[2], imSize[2], 1, false);
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
    public DoubleMatrix3D times(DoubleMatrix3D B, boolean transpose) {
        DoubleMatrix3D Bpad = null;
        switch (type) {
        case INVARIANT:
            switch (boundary) {
            case ZERO:
                Bpad = DoubleCommon3D.padZero(B, invPsfSize[0], invPsfSize[1], invPsfSize[2]);
                break;
            case PERIODIC:
                Bpad = DoubleCommon3D.padPeriodic(B, invPsfSize[0], invPsfSize[1], invPsfSize[2]);
                break;
            case REFLEXIVE:
                Bpad = DoubleCommon3D.padReflexive(B, invPsfSize[0], invPsfSize[1], invPsfSize[2]);
                break;
            }
            return invariantMultiply(Bpad, transpose);
        case VARIANT:
            switch (boundary) {
            case ZERO:
                Bpad = DoubleCommon3D.padZero(B, imSize[0] + 2 * varPadSize[0], imSize[1] + 2 * varPadSize[1], imSize[2] + 2 * varPadSize[2]);
                break;
            case REFLEXIVE:
                Bpad = DoubleCommon3D.padReflexive(B, imSize[0] + 2 * varPadSize[0], imSize[1] + 2 * varPadSize[1], imSize[2] + 2 * varPadSize[2]);
                break;
            case PERIODIC:
                Bpad = DoubleCommon3D.padPeriodic(B, imSize[0] + 2 * varPadSize[0], imSize[1] + 2 * varPadSize[1], imSize[2] + 2 * varPadSize[2]);
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
    public DoublePSF3D getPSF() {
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
        psfSize[2] = psfSize[2] * psfSize[2];
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

    private DoubleMatrix3D invariantMultiply(AbstractMatrix3D Bpad, boolean transpose) {
        Bpad = ((DenseDoubleMatrix3D) Bpad).getFft3();
        if (transpose) {
            ((DComplexMatrix3D) Bpad).assign(matdata[0][0][0], DComplexFunctions.multConjSecond);
        } else {
            ((DComplexMatrix3D) Bpad).assign(matdata[0][0][0], DComplexFunctions.mult);
        }
        ((DenseDComplexMatrix3D) Bpad).ifft3(true);
        return ((DComplexMatrix3D) Bpad).viewPart(invPadSize[0], invPadSize[1], invPadSize[2], imSize[0], imSize[1], imSize[2]).getRealPart();
    }

    private DoubleMatrix3D variantMultiply(DoubleMatrix3D Bpad) {
        if ((varPadSize1[0] > 0) || (varPadSize1[1] > 0)) {
            Bpad = DoubleCommon3D.padZero(Bpad, varPadSize1, PaddingType.POST);
        }
        DoubleMatrix3D Y = new DenseDoubleMatrix3D(Bpad.slices(), Bpad.rows(), Bpad.columns());
        for (int i = 0; i < varNregions[0]; i++) {
            for (int j = 0; j < varNregions[1]; j++) {
                for (int k = 0; k < varNregions[2]; k++) {
                    DoubleMatrix3D Xt = Bpad.viewPart(varEri.iidx[i][0], varEri.jidx[j][0], varEri.kidx[k][0], varEri.iidx[i][1] - varEri.iidx[i][0] + 1, varEri.jidx[j][1] - varEri.jidx[j][0] + 1, varEri.kidx[k][1] - varEri.kidx[k][0] + 1);
                    DoubleMatrix3D Yt = variantMultiplyOnePsf(i, j, k, Xt, false);
                    Y.viewPart(varRi.iidx[i][0], varRi.jidx[j][0], varRi.kidx[k][0], varRi.iidx[i][1] - varRi.iidx[i][0] + 1, varRi.jidx[j][1] - varRi.jidx[j][0] + 1, varRi.kidx[k][1] - varRi.kidx[k][0] + 1).assign(Yt);
                }
            }
        }
        Y = Y.viewPart(0, 0, 0, imSize[0], imSize[1], imSize[2]).copy();
        return Y;
    }

    private DoubleMatrix3D variantTransposeMultiply(DoubleMatrix3D Bpad) {
        if ((varPadSize1[0] > 0) || (varPadSize1[1] > 0)) {
            Bpad = DoubleCommon3D.padZero(Bpad, varPadSize1, PaddingType.POST);
        }
        DoubleMatrix3D Y = new DenseDoubleMatrix3D(Bpad.slices(), Bpad.rows(), Bpad.columns());
        int[] eregionSize = new int[3];
        for (int i = 0; i < varNregions[0]; i++) {
            for (int j = 0; j < varNregions[1]; j++) {
                for (int k = 0; k < varNregions[2]; k++) {
                    eregionSize[0] = varEri.iidx[i][1] - varEri.iidx[i][0] + 1;
                    eregionSize[1] = varEri.jidx[j][1] - varEri.jidx[j][0] + 1;
                    eregionSize[2] = varEri.kidx[k][1] - varEri.kidx[k][0] + 1;
                    DoubleMatrix3D Xt = new DenseDoubleMatrix3D(eregionSize[0], eregionSize[1], eregionSize[2]);
                    Xt.viewPart(varTri.iidx[i][0], varTri.jidx[j][0], varTri.kidx[k][0], varTri.iidx[i][1] - varTri.iidx[i][0] + 1, varTri.jidx[j][1] - varTri.jidx[j][0] + 1, varTri.kidx[k][1] - varTri.kidx[k][0] + 1).assign(
                            Bpad.viewPart(varRiTr.iidx[i][0], varRiTr.jidx[j][0], varRiTr.kidx[k][0], varRiTr.iidx[i][1] - varRiTr.iidx[i][0] + 1, varRiTr.jidx[j][1] - varRiTr.jidx[j][0] + 1, varRiTr.kidx[k][1] - varRiTr.kidx[k][0] + 1));
                    Xt = DoubleCommon3D.padZero(Xt, varPadSize, PaddingType.BOTH);
                    DoubleMatrix3D Yt = variantMultiplyOnePsf(i, j, k, Xt, true);
                    Y.viewPart(varEri.iidx[i][0], varEri.jidx[j][0], varEri.kidx[k][0], varEri.iidx[i][1] - varEri.iidx[i][0] + 1, varEri.jidx[j][1] - varEri.jidx[j][0] + 1, varEri.kidx[k][1] - varEri.kidx[k][0] + 1).assign(
                            Y.viewPart(varEri.iidx[i][0], varEri.jidx[j][0], varEri.kidx[k][0], varEri.iidx[i][1] - varEri.iidx[i][0] + 1, varEri.jidx[j][1] - varEri.jidx[j][0] + 1, varEri.kidx[k][1] - varEri.kidx[k][0] + 1).assign(Yt, DoubleFunctions.plus));
                }
            }
        }
        int[][] idx = new int[2][3];
        idx[0][0] = varPadSize[0];
        idx[0][1] = varPadSize[1];
        idx[0][2] = varPadSize[2];
        idx[1][0] = varPadSize[0] + imSize[0];
        idx[1][1] = varPadSize[1] + imSize[1];
        idx[1][2] = varPadSize[2] + imSize[2];
        Y = Y.viewPart(idx[0][0], idx[0][1], idx[0][2], idx[1][0] - idx[0][0], idx[1][1] - idx[0][1], idx[1][2] - idx[0][2]).copy();
        return Y;
    }

    private DoubleMatrix3D variantMultiplyOnePsf(int slice, int row, int col, DoubleMatrix3D X, boolean transpose) {
        int[] imSize = new int[3];
        imSize[0] = X.slices() - 2 * varPadSize[0];
        imSize[1] = X.rows() - 2 * varPadSize[1];
        imSize[2] = X.columns() - 2 * varPadSize[2];

        PartitionInfo pi = partitionInfo(imSize, varPadSize);
        int[] padSize1 = new int[3];
        padSize1[0] = pi.rsize[0] * pi.nregions[0] - imSize[0];
        padSize1[1] = pi.rsize[1] * pi.nregions[1] - imSize[1];
        padSize1[2] = pi.rsize[2] * pi.nregions[2] - imSize[2];
        if ((padSize1[0] > 0) || padSize1[1] > 0 || padSize1[2] > 0) {
            X = DoubleCommon3D.padZero(X, padSize1, PaddingType.POST);
        }
        RegionIndices ri = regionIndices(pi.nregions, pi.rsize);
        int[] epadSize = new int[3];
        epadSize[0] = 2 * varPadSize[0];
        epadSize[1] = 2 * varPadSize[1];
        epadSize[2] = 2 * varPadSize[2];
        RegionIndices eri = eregionIndices(ri.iidx, ri.jidx, ri.kidx, pi.nregions, epadSize);
        int[][] tidx = new int[2][3];
        tidx[0][0] = varPadSize[0];
        tidx[0][1] = varPadSize[1];
        tidx[0][2] = varPadSize[2];
        tidx[1][0] = varPadSize[0] + pi.rsize[0] - 1;
        tidx[1][1] = varPadSize[1] + pi.rsize[1] - 1;
        tidx[1][2] = varPadSize[2] + pi.rsize[2] - 1;

        DoubleMatrix3D Y = new DenseDoubleMatrix3D(X.slices(), X.rows(), X.columns());
        for (int i = 0; i < pi.nregions[0]; i++) {
            for (int j = 0; j < pi.nregions[1]; j++) {
                for (int k = 0; k < pi.nregions[2]; k++) {
                    DoubleMatrix3D Xt = X.viewPart(eri.iidx[i][0], eri.jidx[j][0], eri.kidx[k][0], eri.iidx[i][1] - eri.iidx[i][0] + 1, eri.jidx[j][1] - eri.jidx[j][0] + 1, eri.kidx[k][1] - eri.kidx[k][0] + 1);
                    DoubleMatrix3D Yt = multiplyOneRegion(slice, row, col, Xt, transpose);
                    Y.viewPart(ri.iidx[i][0], ri.jidx[j][0], ri.kidx[k][0], ri.iidx[i][1] - ri.iidx[i][0] + 1, ri.jidx[j][1] - ri.jidx[j][0] + 1, ri.kidx[k][1] - ri.kidx[k][0] + 1).assign(
                            Yt.viewPart(tidx[0][0], tidx[0][1], tidx[0][2], tidx[1][0] - tidx[0][0] + 1, tidx[1][1] - tidx[0][1] + 1, tidx[1][2] - tidx[0][2] + 1));
                }
            }
        }
        Y = Y.viewPart(0, 0, 0, imSize[0], imSize[1], imSize[2]);
        return Y;

    }

    private DoubleMatrix3D multiplyOneRegion(int slice, int row, int col, AbstractMatrix3D X, boolean transpose) {
        int slicesX = X.slices();
        int rowsX = X.rows();
        int colsX = X.columns();
        int[] padSize = new int[3];
        padSize[0] = matdata[slice][row][col].slices() - slicesX;
        padSize[1] = matdata[slice][row][col].rows() - rowsX;
        padSize[2] = matdata[slice][row][col].columns() - colsX;
        if ((padSize[0] > 0) || padSize[1] > 0 || padSize[2] > 0) {
            X = DoubleCommon3D.padZero((DoubleMatrix3D) X, padSize, PaddingType.POST);
        } else {
            if (X.isView()) {
                X = ((DoubleMatrix3D) X).copy();
            }
        }
        X = ((DenseDoubleMatrix3D) X).getFft3();
        if (transpose) {
            ((DComplexMatrix3D) X).assign(matdata[slice][row][col], DComplexFunctions.multConjSecond);
        } else {
            ((DComplexMatrix3D) X).assign(matdata[slice][row][col], DComplexFunctions.mult);
        }
        ((DenseDComplexMatrix3D) X).ifft3(true);
        X = ((DComplexMatrix3D) X).viewPart(0, 0, 0, slicesX, rowsX, colsX).getRealPart();
        return (DoubleMatrix3D) X;
    }

    private PartitionInfo partitionInfo(int[] imSize, int[] padSize) {
        int[] psfSize = new int[3];
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
        if (padSize[2] == 0) {
            psfSize[2] = 1;
        } else {
            psfSize[2] = 2 * padSize[2];
        }
        PartitionInfo pi = new PartitionInfo();
        pi.rsize[0] = Math.min(psfSize[0], imSize[0]);
        pi.rsize[1] = Math.min(psfSize[1], imSize[1]);
        pi.rsize[2] = Math.min(psfSize[2], imSize[2]);
        pi.nregions[0] = (int) Math.ceil((double) imSize[0] / (double) pi.rsize[0]);
        pi.nregions[1] = (int) Math.ceil((double) imSize[1] / (double) pi.rsize[1]);
        pi.nregions[2] = (int) Math.ceil((double) imSize[2] / (double) pi.rsize[2]);
        return pi;
    }

    private class PartitionInfo {
        public final int[] nregions = new int[3];

        public final int[] rsize = new int[3];
    }

    private RegionIndices regionIndices(int[] nregions, int[] rsize) {
        RegionIndices ri = new RegionIndices(nregions);
        ri.iidx[0][0] = 0;
        ri.iidx[0][1] = rsize[0] - 1;
        ri.jidx[0][0] = 0;
        ri.jidx[0][1] = rsize[1] - 1;
        ri.kidx[0][0] = 0;
        ri.kidx[0][1] = rsize[2] - 1;
        for (int i = 1; i < nregions[0]; i++) {
            ri.iidx[i][0] = ri.iidx[i - 1][1] + 1;
            ri.iidx[i][1] = ri.iidx[i][0] + rsize[0] - 1;
        }
        for (int i = 1; i < nregions[1]; i++) {
            ri.jidx[i][0] = ri.jidx[i - 1][1] + 1;
            ri.jidx[i][1] = ri.jidx[i][0] + rsize[1] - 1;
        }
        for (int i = 1; i < nregions[2]; i++) {
            ri.kidx[i][0] = ri.kidx[i - 1][1] + 1;
            ri.kidx[i][1] = ri.kidx[i][0] + rsize[2] - 1;
        }
        return ri;
    }

    private RegionIndices eregionIndices(int[][] iidx, int[][] jidx, int[][] kidx, int[] nregions, int[] rsize) {
        RegionIndices eri = new RegionIndices(nregions);
        for (int i = 0; i < nregions[0]; i++) {
            eri.iidx[i][0] = iidx[i][0];
            eri.iidx[i][1] = iidx[i][1] + 2 * (int) Math.floor((double) rsize[0] / 2.0);
        }
        for (int i = 0; i < nregions[1]; i++) {
            eri.jidx[i][0] = jidx[i][0];
            eri.jidx[i][1] = jidx[i][1] + 2 * (int) Math.floor((double) rsize[1] / 2.0);
        }
        for (int i = 0; i < nregions[2]; i++) {
            eri.kidx[i][0] = kidx[i][0];
            eri.kidx[i][1] = kidx[i][1] + 2 * (int) Math.floor((double) rsize[2] / 2.0);
        }
        return eri;
    }

    private RegionIndices tregionIndices(int[][] iidx, int[][] jidx, int[][] kidx, int[] nregions, int[] padSize) {
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
        for (int i = 1; i < nregions[2]; i++) {
            tri.kidx[i][0] = padSize[2];
        }
        for (int i = 0; i < nregions[2] - 1; i++) {
            tri.kidx[i][1] = kidx[0][1];
        }
        if (nregions[2] > 1) {
            tri.kidx[nregions[2] - 1][1] = kidx[0][1] + padSize[2];
        } else {
            tri.kidx[nregions[2] - 1][1] = kidx[0][1];
        }
        return tri;
    }

    private class RegionIndices {

        public final int[][] iidx;

        public final int[][] jidx;

        public final int[][] kidx;

        public RegionIndices(int[] nregions) {
            iidx = new int[nregions[0]][2];
            jidx = new int[nregions[1]][2];
            kidx = new int[nregions[2]][2];
        }

    }

    private void constructMatrix() {
        DoubleMatrix3D[][][] image = PSF.getImage();
        int[][][][] center = PSF.getCenter();
        switch (type) {
        case INVARIANT:
            matdata = new DComplexMatrix3D[1][1][1];
            matdata[0][0][0] = onePsfMatrixInvariant(image[0][0][0], center[0][0][0]);
            break;
        case VARIANT:
            matdata = new DComplexMatrix3D[image.length][image[0].length][image[0][0].length];
            for (int i = 0; i < matdata.length; i++) {
                for (int j = 0; j < matdata[0].length; j++) {
                    for (int k = 0; k < matdata[0][0].length; k++) {
                        matdata[i][j][k] = onePsfMatrixVariant(image[i][j][k], center[i][j][k]);
                    }
                }
            }
            break;
        default:
            return;
        }
    }

    private DComplexMatrix3D onePsfMatrixInvariant(DoubleMatrix3D image, int[] center) {
        image.normalize();
        int slices = image.slices();
        int rows = image.rows();
        int columns = image.columns();
        AbstractMatrix3D matdata = new DenseDoubleMatrix3D(invPsfSize[0], invPsfSize[1], invPsfSize[2]);
        ((DoubleMatrix3D) matdata).viewPart(0, 0, 0, slices, rows, columns).assign(image);
        matdata = DoubleCommon3D.circShift((DoubleMatrix3D) matdata, center);
        matdata = ((DenseDoubleMatrix3D) matdata).getFft3();
        return (DComplexMatrix3D) matdata;
    }

    private DComplexMatrix3D onePsfMatrixVariant(DoubleMatrix3D image, int[] center) {
        image.normalize();

        int slices = 2 * image.slices();
        int rows = 2 * image.rows();
        int columns = 2 * image.columns();

        switch (resizing) {
        case AUTO:
            int[] nextPowTwo = new int[3];
            if (!ConcurrencyUtils.isPowerOf2(slices)) {
                nextPowTwo[0] = ConcurrencyUtils.nextPow2(slices);
            } else {
                nextPowTwo[0] = slices;
            }
            if (!ConcurrencyUtils.isPowerOf2(rows)) {
                nextPowTwo[1] = ConcurrencyUtils.nextPow2(rows);
            } else {
                nextPowTwo[1] = rows;
            }
            if (!ConcurrencyUtils.isPowerOf2(columns)) {
                nextPowTwo[2] = ConcurrencyUtils.nextPow2(columns);
            } else {
                nextPowTwo[2] = columns;
            }

            if ((nextPowTwo[0] < 1.5 * slices) && (nextPowTwo[1] < 1.5 * rows) && (nextPowTwo[2] < 1.5 * columns)) {
                slices = nextPowTwo[0];
                rows = nextPowTwo[1];
                columns = nextPowTwo[2];
            }
            break;
        case NEXT_POWER_OF_TWO:
            if (!ConcurrencyUtils.isPowerOf2(slices)) {
                rows = ConcurrencyUtils.nextPow2(slices);
            }
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
        AbstractMatrix3D matdata = new DenseDoubleMatrix3D(slices, rows, columns);
        ((DoubleMatrix3D) matdata).viewPart(0, 0, 0, image.slices(), image.rows(), image.columns()).assign(image);
        matdata = DoubleCommon3D.circShift((DoubleMatrix3D) matdata, center);
        matdata = ((DenseDoubleMatrix3D) matdata).getFft3();
        return (DComplexMatrix3D) matdata;
    }
}
