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

package endrov.utilityUnsorted.deconvolution.iterative.preconditioner;


import java.util.concurrent.Future;

import cern.colt.function.tint.IntComparator;
import cern.colt.matrix.AbstractMatrix3D;
import cern.colt.matrix.tdcomplex.DComplexMatrix3D;
import cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix3D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix3D;
import cern.colt.matrix.tdouble.algo.DoubleSorting;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix3D;
import cern.jet.math.tdcomplex.DComplexFunctions;
import cern.jet.math.tdouble.DoubleFunctions;
import edu.emory.mathcs.utils.ConcurrencyUtils;
import endrov.core.log.EvLog;
import endrov.utilityUnsorted.deconvolution.iterative.DoubleCommon3D;
import endrov.utilityUnsorted.deconvolution.iterative.IterativeEnums.BoundaryType;
import endrov.utilityUnsorted.deconvolution.iterative.IterativeEnums.PSFType;
import endrov.utilityUnsorted.deconvolution.iterative.IterativeEnums.PaddingType;
import endrov.utilityUnsorted.deconvolution.iterative.psf.DoublePSFMatrix3D;

/**
 * 3D preconditioner based on the Fast Fourier Transform.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class FFTDoublePreconditioner3D implements DoublePreconditioner3D {
    private AbstractMatrix3D matdata;

    private double tol;

    private BoundaryType boundary;

    private int[] imSize;

    private int[] psfSize;

    private int[] padSize;

    /**
     * Creates a new instance of DoubleFFTPreconditioner3D.
     * 
     * @param PSFMatrix
     *            PSF matrix
     * @param B
     *            blurred image
     * @param tol
     *            tolerance
     */
    public FFTDoublePreconditioner3D(DoublePSFMatrix3D PSFMatrix, DoubleMatrix3D B, double tol) {
        this.tol = tol;
        this.boundary = PSFMatrix.getBoundary();
        this.imSize = new int[3];
        imSize[0] = B.slices();
        imSize[1] = B.rows();
        imSize[2] = B.columns();
        if (PSFMatrix.getType() == PSFType.INVARIANT) {
            this.psfSize = PSFMatrix.getInvPsfSize();
            this.padSize = PSFMatrix.getInvPadSize();
        } else {
            this.psfSize = PSFMatrix.getPSF().getSize();
            int[] minimal = new int[3];
            minimal[0] = psfSize[0] + imSize[0];
            minimal[1] = psfSize[1] + imSize[1];
            minimal[2] = psfSize[2] + imSize[2];
            switch (PSFMatrix.getResizing()) {
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
                    psfSize[0] = minimal[0];
                    psfSize[1] = minimal[1];
                    psfSize[2] = minimal[2];
                } else {
                    psfSize[0] = nextPowTwo[0];
                    psfSize[1] = nextPowTwo[1];
                    psfSize[2] = nextPowTwo[2];
                }
                break;
            case MINIMAL:
                psfSize[0] = minimal[0];
                psfSize[1] = minimal[1];
                psfSize[2] = minimal[2];
                break;
            case NEXT_POWER_OF_TWO:
                psfSize[0] = minimal[0];
                psfSize[1] = minimal[1];
                psfSize[2] = minimal[2];
                if (!ConcurrencyUtils.isPowerOf2(psfSize[0])) {
                    psfSize[0] = ConcurrencyUtils.nextPow2(psfSize[0]);
                }
                if (!ConcurrencyUtils.isPowerOf2(psfSize[1])) {
                    psfSize[1] = ConcurrencyUtils.nextPow2(psfSize[1]);
                }
                if (!ConcurrencyUtils.isPowerOf2(psfSize[2])) {
                    psfSize[2] = ConcurrencyUtils.nextPow2(psfSize[2]);
                }
                break;
            }
            padSize = new int[3];
            if (imSize[0] < psfSize[0]) {
                padSize[0] = (psfSize[0] - imSize[0] + 1) / 2;
            }
            if (imSize[1] < psfSize[1]) {
                padSize[1] = (psfSize[1] - imSize[1] + 1) / 2;
            }
            if (imSize[2] < psfSize[2]) {
                padSize[2] = (psfSize[2] - imSize[2] + 1) / 2;
            }
        }
        constructMatrix(PSFMatrix.getPSF().getImage(), B, PSFMatrix.getPSF().getCenter());
    }

    public double getTolerance() {
        return tol;
    }

    public DoubleMatrix1D solve(DoubleMatrix1D b, boolean transpose) {
        DoubleMatrix3D B = null;
        if (b.isView()) {
            B = new DenseDoubleMatrix3D(imSize[0], imSize[1], imSize[2], (double[]) b.copy().elements(), 0, 0, 0, imSize[1] * imSize[2], imSize[2], 1, false);
        } else {
            B = new DenseDoubleMatrix3D(imSize[0], imSize[1], imSize[2], (double[]) b.elements(), 0, 0, 0, imSize[1] * imSize[2], imSize[2], 1, false);
        }
        B = solve(B, transpose);
        return new DenseDoubleMatrix1D(B.size(), (double[]) B.elements(), 0, 1, false);
    }

    public DoubleMatrix3D solve(AbstractMatrix3D B, boolean transpose) {
        switch (boundary) {
        case ZERO:
            B = DoubleCommon3D.padZero((DoubleMatrix3D) B, psfSize[0], psfSize[1], psfSize[2]);
            break;
        case PERIODIC:
            B = DoubleCommon3D.padPeriodic((DoubleMatrix3D) B, psfSize[0], psfSize[1], psfSize[2]);
            break;
        case REFLEXIVE:
            B = DoubleCommon3D.padReflexive((DoubleMatrix3D) B, psfSize[0], psfSize[1], psfSize[2]);
            break;
        }
        B = ((DenseDoubleMatrix3D) B).getFft3();
        if (transpose) {
            ((DComplexMatrix3D) B).assign((DComplexMatrix3D) matdata, DComplexFunctions.multConjSecond);
        } else {
            ((DComplexMatrix3D) B).assign((DComplexMatrix3D) matdata, DComplexFunctions.mult);
        }
        ((DenseDComplexMatrix3D) B).ifft3(true);
        return ((DComplexMatrix3D) B).viewPart(padSize[0], padSize[1], padSize[2], imSize[0], imSize[1], imSize[2]).getRealPart();
    }

    private void constructMatrix(DoubleMatrix3D[][][] PSFs, DoubleMatrix3D B, int[][][][] center) {
        matdata = PSFs[0][0][0].like();
        int[] center1 = center[0][0][0];
        int slices = PSFs.length;
        int rows = PSFs[0].length;
        int columns = PSFs[0][0].length;
        int size = slices * rows * columns;
        for (int s = 0; s < slices; s++) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    ((DoubleMatrix3D) matdata).assign(PSFs[s][r][c], DoubleFunctions.plus);
                }
            }
        }
        if (size != 1) {
            ((DoubleMatrix3D) matdata).assign(DoubleFunctions.div(size));
        }
        switch (boundary) {
        case ZERO:
            B = DoubleCommon3D.padZero(B, psfSize[0], psfSize[1], psfSize[2]);
            break;
        case PERIODIC:
            B = DoubleCommon3D.padPeriodic(B, psfSize[0], psfSize[1], psfSize[2]);
            break;
        case REFLEXIVE:
            B = DoubleCommon3D.padReflexive(B, psfSize[0], psfSize[1], psfSize[2]);
            break;
        }
        precMatrixOnePsf(center1, B);
    }

    private void precMatrixOnePsf(int[] center, DoubleMatrix3D Bpad) {
        int[] padSize = new int[3];
        padSize[0] = Bpad.slices() - matdata.slices();
        padSize[1] = Bpad.rows() - matdata.rows();
        padSize[2] = Bpad.columns() - matdata.columns();
        if ((padSize[0] > 0) || (padSize[1] > 0) || (padSize[2] > 0)) {
            matdata = DoubleCommon3D.padZero((DoubleMatrix3D) matdata, padSize, PaddingType.POST);
        }
        matdata = DoubleCommon3D.circShift((DoubleMatrix3D) matdata, center);
        matdata = ((DenseDoubleMatrix3D) matdata).getFft3();
        AbstractMatrix3D E = ((DComplexMatrix3D) matdata).copy();
        ((DComplexMatrix3D) E).assign(DComplexFunctions.abs);
        E = ((DComplexMatrix3D) E).getRealPart();
        double[] maxAndLoc = ((DoubleMatrix3D) E).getMaxLocation();
        final double maxE = maxAndLoc[0];

        if (tol == -1) { 
            EvLog.printLog("Computing tolerance for preconditioner..."); //TODO status
            double[] minAndLoc = ((DoubleMatrix3D) E).getMinLocation();
            double minE = minAndLoc[0];
            if (maxE / minE < 100) {
                tol = 0;
            } else {
                tol = defaultTol2(((DoubleMatrix3D) E), Bpad);
            }
            EvLog.printLog("Computing tolerance for preconditioner...done.");
        }

        final double[] one = new double[] { 1, 0 };
        if (maxE != 1.0) {
            ((DComplexMatrix3D) matdata).assign(DComplexFunctions.div(new double[] { maxE, 0 }));
        }
        final int slices = E.slices();
        final int rows = E.rows();
        final int cols = E.columns();
        final double[] elementsE = (double[]) ((DoubleMatrix3D) E).elements();
        final int zeroE = (int) ((DoubleMatrix3D) E).index(0, 0, 0);
        final int sliceStrideE = ((DoubleMatrix3D) E).sliceStride();
        final int rowStrideE = ((DoubleMatrix3D) E).rowStride();
        final int columnStrideE = ((DoubleMatrix3D) E).columnStride();
        final double[] elementsM = (double[]) ((DComplexMatrix3D) matdata).elements();
        final int zeroM = (int) ((DComplexMatrix3D) matdata).index(0, 0, 0);
        final int sliceStrideM = ((DComplexMatrix3D) matdata).sliceStride();
        final int rowStrideM = ((DComplexMatrix3D) matdata).rowStride();
        final int columnStrideM = ((DComplexMatrix3D) matdata).columnStride();

        int np = ConcurrencyUtils.getNumberOfThreads();
        if ((np > 1) && (slices * rows * cols >= ConcurrencyUtils.getThreadsBeginN_3D())) {
            Future<?>[] futures = new Future[np];
            int k = slices / np;
            for (int j = 0; j < np; j++) {
                final int startslice = j * k;
                final int stopslice;
                if (j == np - 1) {
                    stopslice = slices;
                } else {
                    stopslice = startslice + k;
                }
                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        double[] elem = new double[2];
                        if (maxE != 1.0) {
                            for (int s = startslice; s < stopslice; s++) {
                                for (int r = 0; r < rows; r++) {
                                    for (int c = 0; c < cols; c++) {
                                        int idxE = zeroE + s * sliceStrideE + r * rowStrideE + c * columnStrideE;
                                        int idxM = zeroM + s * sliceStrideM + r * rowStrideM + c * columnStrideM;
                                        elem[0] = elementsM[idxM];
                                        elem[1] = elementsM[idxM + 1];
                                        if (elementsE[idxE] >= tol) {
                                            if (elem[1] != 0.0) {
                                                double scalar;
                                                if (Math.abs(elem[0]) >= Math.abs(elem[1])) {
                                                    scalar = 1.0 / (elem[0] + elem[1] * (elem[1] / elem[0]));
                                                    elem[0] = scalar;
                                                    elem[1] = scalar * (-elem[1] / elem[0]);
                                                } else {
                                                    scalar = 1.0 / (elem[0] * (elem[0] / elem[1]) + elem[1]);
                                                    elem[0] = scalar * (elem[0] / elem[1]);
                                                    elem[1] = -scalar;
                                                }
                                            } else {
                                                elem[0] = 1 / elem[0];
                                                elem[1] = 0;
                                            }
                                            elem[0] *= maxE;
                                            elem[1] *= maxE;
                                            elementsM[idxM] = elem[0];
                                            elementsM[idxM + 1] = elem[1];
                                        } else {
                                            elementsM[idxM] = one[0];
                                            elementsM[idxM + 1] = one[1];
                                        }
                                    }
                                }
                            }
                        } else {
                            for (int s = startslice; s < stopslice; s++) {
                                for (int r = 0; r < rows; r++) {
                                    for (int c = 0; c < cols; c++) {
                                        int idxE = zeroE + s * sliceStrideE + r * rowStrideE + c * columnStrideE;
                                        int idxM = zeroM + s * sliceStrideM + r * rowStrideM + c * columnStrideM;
                                        elem[0] = elementsM[idxM];
                                        elem[1] = elementsM[idxM + 1];

                                        if (elementsE[idxE] >= tol) {
                                            if (elem[1] != 0.0) {
                                                double scalar;
                                                if (Math.abs(elem[0]) >= Math.abs(elem[1])) {
                                                    scalar = 1.0 / (elem[0] + elem[1] * (elem[1] / elem[0]));
                                                    elem[0] = scalar;
                                                    elem[1] = scalar * (-elem[1] / elem[0]);
                                                } else {
                                                    scalar = 1.0 / (elem[0] * (elem[0] / elem[1]) + elem[1]);
                                                    elem[0] = scalar * (elem[0] / elem[1]);
                                                    elem[1] = -scalar;
                                                }
                                            } else {
                                                elem[0] = 1 / elem[0];
                                                elem[1] = 0;
                                            }
                                            elementsM[idxM] = elem[0];
                                            elementsM[idxM + 1] = elem[1];
                                        } else {
                                            elementsM[idxM] = one[0];
                                            elementsM[idxM + 1] = one[1];
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            double[] elem = new double[2];
            if (maxE != 1.0) {
                for (int s = 0; s < slices; s++) {
                    for (int r = 0; r < rows; r++) {
                        for (int c = 0; c < cols; c++) {
                            int idxE = zeroE + s * sliceStrideE + r * rowStrideE + c * columnStrideE;
                            int idxM = zeroM + s * sliceStrideM + r * rowStrideM + c * columnStrideM;
                            elem[0] = elementsM[idxM];
                            elem[1] = elementsM[idxM + 1];
                            if (elementsE[idxE] >= tol) {
                                if (elem[1] != 0.0) {
                                    double scalar;
                                    if (Math.abs(elem[0]) >= Math.abs(elem[1])) {
                                        scalar = 1.0 / (elem[0] + elem[1] * (elem[1] / elem[0]));
                                        elem[0] = scalar;
                                        elem[1] = scalar * (-elem[1] / elem[0]);
                                    } else {
                                        scalar = 1.0 / (elem[0] * (elem[0] / elem[1]) + elem[1]);
                                        elem[0] = scalar * (elem[0] / elem[1]);
                                        elem[1] = -scalar;
                                    }
                                } else {
                                    elem[0] = 1 / elem[0];
                                    elem[1] = 0;
                                }
                                elem[0] *= maxE;
                                elem[1] *= maxE;
                                elementsM[idxM] = elem[0];
                                elementsM[idxM + 1] = elem[1];
                            } else {
                                elementsM[idxM] = one[0];
                                elementsM[idxM + 1] = one[1];
                            }
                        }
                    }
                }
            } else {
                for (int s = 0; s < slices; s++) {
                    for (int r = 0; r < rows; r++) {
                        for (int c = 0; c < cols; c++) {
                            int idxE = zeroE + s * sliceStrideE + r * rowStrideE + c * columnStrideE;
                            int idxM = zeroM + s * sliceStrideM + r * rowStrideM + c * columnStrideM;
                            elem[0] = elementsM[idxM];
                            elem[1] = elementsM[idxM + 1];

                            if (elementsE[idxE] >= tol) {
                                if (elem[1] != 0.0) {
                                    double scalar;
                                    if (Math.abs(elem[0]) >= Math.abs(elem[1])) {
                                        scalar = 1.0 / (elem[0] + elem[1] * (elem[1] / elem[0]));
                                        elem[0] = scalar;
                                        elem[1] = scalar * (-elem[1] / elem[0]);
                                    } else {
                                        scalar = 1.0 / (elem[0] * (elem[0] / elem[1]) + elem[1]);
                                        elem[0] = scalar * (elem[0] / elem[1]);
                                        elem[1] = -scalar;
                                    }
                                } else {
                                    elem[0] = 1 / elem[0];
                                    elem[1] = 0;
                                }
                                elementsM[idxM] = elem[0];
                                elementsM[idxM + 1] = elem[1];
                            } else {
                                elementsM[idxM] = one[0];
                                elementsM[idxM + 1] = one[1];
                            }
                        }
                    }
                }
            }
        }
    }

    private double defaultTol2(DoubleMatrix3D E, DoubleMatrix3D B) {
        DoubleMatrix1D s = new DenseDoubleMatrix1D(E.size());
        System.arraycopy((double[]) E.elements(), 0, (double[]) s.elements(), 0, s.size());
        final double[] evalues = (double[]) s.elements();
        IntComparator compDec = new IntComparator() {
            public int compare(int a, int b) {
                if (evalues[a] != evalues[a] || evalues[b] != evalues[b])
                    return compareNaN(evalues[a], evalues[b]); // swap NaNs to
                // the end
                return evalues[a] < evalues[b] ? 1 : (evalues[a] == evalues[b] ? 0 : -1);
            }
        };
        int[] indices = DoubleSorting.quickSort.sortIndex(s, compDec);
        s = s.viewSelection(indices);
        AbstractMatrix3D Bhat = ((DenseDoubleMatrix3D) B).getFft3();
        ((DComplexMatrix3D) Bhat).assign(DComplexFunctions.abs);
        Bhat = ((DComplexMatrix3D) Bhat).getRealPart();
        DoubleMatrix1D bhat = new DenseDoubleMatrix1D(Bhat.size(), (double[]) ((DoubleMatrix3D) Bhat).elements(), 0, 1, false);
        bhat = bhat.viewSelection(indices);
        bhat.assign(DoubleFunctions.div((double) Math.sqrt(B.size())));
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
