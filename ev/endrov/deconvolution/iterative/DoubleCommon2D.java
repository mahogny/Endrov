/*
 *  Copyright (C) 2008-2009 Piotr Wendykier
 *  Additions (C) 2009 Johan Henriksson
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
package endrov.deconvolution.iterative;

import java.util.concurrent.Future;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import cern.jet.math.tdouble.DoubleFunctions;
import edu.emory.mathcs.utils.ConcurrencyUtils;
import endrov.deconvolution.iterative.IterativeEnums.PaddingType;
import endrov.imageset.EvPixels;

/**
 * Common methods for 2D iterative deblurring. Some code is from Bob Dougherty's Iterative
 * Deconvolve 3D.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 * 
 */
public class DoubleCommon2D {

    private DoubleCommon2D() {
    }

    /**
     * Tolerance for optimization.Fmin.fmin.
     */
    public static final double FMIN_TOL = 1.0e-4;

    /**
     * Relative accuracy for double.
     */
    public static final double eps = (double) Math.pow(2, -52);

    /**
     * Square root of the relative accuracy for double.
     */
    public static final double sqrteps = (double) Math.sqrt(eps);

    /**
     * Copies pixel values from image processor <code>ip</code> to matrix
     * <code>X</code>.
     * 
     * @param X
     *            matrix
     * @param ip
     *            image processor
     */
    public static void assignPixelsToMatrix(final DoubleMatrix2D X, final EvPixels ip) {
    	if(ip.getType()==EvPixels.TYPE_DOUBLE)
    		X.assign(ip.getArrayDouble());
    	else if(ip.getType()==EvPixels.TYPE_FLOAT)
    		X.assign(ip.getArrayFloat());
    	else
    		X.assign(ip.convertTo(EvPixels.TYPE_DOUBLE, true).getArrayDouble());
    }

    /**
     * Copies pixel values from matrix <code>x</code> to image processor
     * <code>ip</code>.
     * 
     * @param ip
     *            image processor
     * @param x
     *            matrix
     * @param cmY
     *            color model
     */
    public static void assignPixelsToProcessor(final EvPixels ip, final DoubleMatrix1D x) {
        final int rows = ip.getHeight();
        final int cols = ip.getWidth();
        final int size = rows * cols;
        final double[] px = ip.convertTo(EvPixels.TYPE_DOUBLE, true).getArrayDouble();
        int np = ConcurrencyUtils.getNumberOfThreads();
        if (x.isView()) {
            if ((np > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_2D())) {
                Future<?>[] futures = new Future[np];
                int k = size / np;
                for (int j = 0; j < np; j++) {
                    final int firstIdx = j * k;
                    final int lastIdx = (j == np - 1) ? size : firstIdx + k;
                    futures[j] = ConcurrencyUtils.submit(new Runnable() {
                        public void run() {
                            for (int i = firstIdx; i < lastIdx; i++) {
                                px[i] = (double) x.getQuick(i);
                            }
                        }
                    });
                }
                ConcurrencyUtils.waitForCompletion(futures);
            } else {
                for (int i = 0; i < size; i++) {
                    px[i] = (double) x.getQuick(i);
                }
            }
        } else {
            final double[] elems = (double[]) x.elements();
            if ((np > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_2D())) {
                Future<?>[] futures = new Future[np];
                int k = size / np;
                for (int j = 0; j < np; j++) {
                    final int firstIdx = j * k;
                    final int lastIdx = (j == np - 1) ? size : firstIdx + k;
                    futures[j] = ConcurrencyUtils.submit(new Runnable() {
                        public void run() {
                            for (int i = firstIdx; i < lastIdx; i++) {
                                px[i] = (double) elems[i];
                            }
                        }
                    });
                }
                ConcurrencyUtils.waitForCompletion(futures);
            } else {
                for (int i = 0; i < size; i++) {
                    px[i] = (double) elems[i];
                }
            }
        }
    }

    /**
     * Copies pixel values from matrix <code>x</code> to image processor
     * <code>ip</code>.
     * 
     * @param ip
     *            image processor
     * @param x
     *            matrix
     * @param cmY
     *            color model
     * @param threshold
     *            the smallest positive value assigned to the image processor,
     *            all the values less than the threshold are set to zero
     * 
     */
    public static void assignPixelsToProcessor(final EvPixels ip, final DoubleMatrix1D x, final double threshold) {
        final int rows = ip.getHeight();
        final int cols = ip.getWidth();
        final int size = rows * cols;
        final double[] px = ip.convertToDouble(true).getArrayDouble();
        int np = ConcurrencyUtils.getNumberOfThreads();
        if (x.isView()) {
            if ((np > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_2D())) {
                Future<?>[] futures = new Future[np];
                int k = size / np;
                for (int j = 0; j < np; j++) {
                    final int firstIdx = j * k;
                    final int lastIdx = (j == np - 1) ? size : firstIdx + k;
                    futures[j] = ConcurrencyUtils.submit(new Runnable() {
                        public void run() {
                            double elem;
                            for (int i = firstIdx; i < lastIdx; i++) {
                                elem = (double) x.getQuick(i);
                                if (elem >= threshold) {
                                    px[i] = elem;
                                } else {
                                    px[i] = 0;
                                }
                            }
                        }
                    });
                }
                ConcurrencyUtils.waitForCompletion(futures);
            } else {
                double elem;
                for (int i = 0; i < size; i++) {
                    elem = (double) x.getQuick(i);
                    if (elem >= threshold) {
                        px[i] = elem;
                    } else {
                        px[i] = 0;
                    }
                }
            }
        } else {
            final double[] elems = (double[]) x.elements();
            if ((np > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_2D())) {
                Future<?>[] futures = new Future[np];
                int k = size / np;
                for (int j = 0; j < np; j++) {
                    final int firstIdx = j * k;
                    final int lastIdx = (j == np - 1) ? size : firstIdx + k;
                    futures[j] = ConcurrencyUtils.submit(new Runnable() {
                        public void run() {
                            double elem;
                            for (int i = firstIdx; i < lastIdx; i++) {
                                elem = (double) elems[i];
                                if (elem >= threshold) {
                                    px[i] = (double) elem;
                                } else {
                                    px[i] = 0;
                                }
                            }
                        }
                    });
                }
                ConcurrencyUtils.waitForCompletion(futures);
            } else {
                double elem;
                for (int i = 0; i < size; i++) {
                    elem = (double) elems[i];
                    if (elem >= threshold) {
                        px[i] = (double) elem;
                    } else {
                        px[i] = 0;
                    }
                }
            }
        }
    }

    /**
     * Copies pixel values from matrix <code>X</code> to image processor
     * <code>ip</code>.
     * 
     * @param ip
     *            image processor
     * @param X
     *            matrix
     * @param cmY
     *            color model
     * 
     */
    public static void assignPixelsToProcessor(final EvPixels ip, final DoubleMatrix2D X) {
        final int rows = X.rows();
        final int cols = X.columns();
        final double[] px = ip.convertToDouble(true).getArrayDouble();
        int np = ConcurrencyUtils.getNumberOfThreads();
        if (X.isView()) {
            if ((np > 1) && (rows * cols >= ConcurrencyUtils.getThreadsBeginN_2D())) {
                Future<?>[] futures = new Future[np];
                int k = rows / np;
                for (int j = 0; j < np; j++) {
                    final int firstRow = j * k;
                    final int lastRow = (j == np - 1) ? rows : firstRow + k;
                    futures[j] = ConcurrencyUtils.submit(new Runnable() {
                        public void run() {
                            for (int r = firstRow; r < lastRow; r++) {
                                for (int c = 0; c < cols; c++) {
                                    px[c + cols * r] = (double) X.getQuick(r, c);
                                }
                            }
                        }
                    });
                }
                ConcurrencyUtils.waitForCompletion(futures);
            } else {
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < cols; c++) {
                        px[c + cols * r] = (double) X.getQuick(r, c);
                    }
                }
            }
        } else {
            final double[] elems = (double[]) X.elements();
            if ((np > 1) && (rows * cols >= ConcurrencyUtils.getThreadsBeginN_2D())) {
                Future<?>[] futures = new Future[np];
                int k = rows / np;
                for (int j = 0; j < np; j++) {
                    final int firstRow = j * k;
                    final int lastRow = (j == np - 1) ? rows : firstRow + k;
                    futures[j] = ConcurrencyUtils.submit(new Runnable() {
                        public void run() {
                            int idx = firstRow * cols;
                            for (int r = firstRow; r < lastRow; r++) {
                                for (int c = 0; c < cols; c++) {
                                    px[idx] = (double) elems[idx];
                                    idx++;
                                }
                            }
                        }
                    });
                }
                ConcurrencyUtils.waitForCompletion(futures);
            } else {
                int idx = 0;
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < cols; c++) {
                        px[idx] = (double) elems[idx];
                        idx++;
                    }
                }
            }
        }
    }

    /**
     * Copies pixel values from complex matrix <code>X</code> to image processor
     * <code>ip</code>
     * 
     * @param ip
     *            image processor
     * @param X
     *            padded matrix
     * @param rows
     *            original number of rows
     * @param cols
     *            original number of columns
     * @param rOff
     *            row offset
     * @param cOff
     *            column offset
     * @param cmY
     *            color model
     * 
     */
    public static void assignPixelsToProcessorPadded(final EvPixels ip, final DoubleMatrix2D X, final int rows, final int cols, final int rOff, final int cOff) {
        final double[] px = ip.convertToDouble(true).getArrayDouble();
        int np = ConcurrencyUtils.getNumberOfThreads();
        if (X.isView()) {
            if ((np > 1) && (rows * cols >= ConcurrencyUtils.getThreadsBeginN_2D())) {
                Future<?>[] futures = new Future[np];
                int k = rows / np;
                for (int j = 0; j < np; j++) {
                    final int firstRow = j * k;
                    final int lastRow = (j == np - 1) ? rows : firstRow + k;
                    futures[j] = ConcurrencyUtils.submit(new Runnable() {
                        public void run() {
                            for (int r = firstRow; r < lastRow; r++) {
                                for (int c = 0; c < cols; c++) {
                                    px[c + cols * r] = (double) X.getQuick(r + rOff, c + cOff);
                                }
                            }
                        }
                    });
                }
                ConcurrencyUtils.waitForCompletion(futures);
            } else {
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < cols; c++) {
                        px[c + cols * r] = (double) X.getQuick(r + rOff, c + cOff);
                    }
                }
            }
        } else {
            final double[] elems = (double[]) X.elements();
            final int rowStride = X.columns();
            if ((np > 1) && (rows * cols >= ConcurrencyUtils.getThreadsBeginN_2D())) {
                Future<?>[] futures = new Future[np];
                int k = rows / np;
                for (int j = 0; j < np; j++) {
                    final int firstRow = j * k;
                    final int lastRow = (j == np - 1) ? rows : firstRow + k;
                    futures[j] = ConcurrencyUtils.submit(new Runnable() {
                        public void run() {
                            int idx;
                            for (int r = firstRow; r < lastRow; r++) {
                                for (int c = 0; c < cols; c++) {
                                    idx = (r + rOff) * rowStride + (c + cOff);
                                    px[r * cols + c] = (double) elems[idx];
                                }
                            }
                        }
                    });
                }
                ConcurrencyUtils.waitForCompletion(futures);
            } else {
                int idx;
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < cols; c++) {
                        idx = (r + rOff) * rowStride + (c + cOff);
                        px[r * cols + c] = (double) elems[idx];
                        idx++;
                    }
                }
            }
        }
    }

    /**
     * Copies pixel values from complex matrix <code>X</code> to image processor
     * <code>ip</code>
     * 
     * @param ip
     *            image processor
     * @param X
     *            padded matrix
     * @param rows
     *            original number of rows
     * @param cols
     *            original number of columns
     * @param rOff
     *            row offset
     * @param cOff
     *            column offset
     * @param cmY
     *            color model
     * @param threshold
     *            the smallest positive value assigned to the image processor,
     *            all the values less than the threshold are set to zero
     */
    public static void assignPixelsToProcessorPadded(final EvPixels ip, final DoubleMatrix2D X, final int rows, final int cols, final int rOff, final int cOff, final double threshold) {
        final double[] px = ip.convertToDouble(true).getArrayDouble();
        int np = ConcurrencyUtils.getNumberOfThreads();
        if (X.isView()) {
            if ((np > 1) && (rows * cols >= ConcurrencyUtils.getThreadsBeginN_2D())) {
                Future<?>[] futures = new Future[np];
                int k = rows / np;
                for (int j = 0; j < np; j++) {
                    final int firstRow = j * k;
                    final int lastRow = (j == np - 1) ? rows : firstRow + k;
                    futures[j] = ConcurrencyUtils.submit(new Runnable() {
                        public void run() {
                            double elem;
                            for (int r = firstRow; r < lastRow; r++) {
                                for (int c = 0; c < cols; c++) {
                                    elem = (double) X.getQuick(r + rOff, c + cOff);
                                    if (elem >= threshold) {
                                        px[c + cols * r] = elem;
                                    } else {
                                        px[c + cols * r] = 0;
                                    }
                                }
                            }
                        }
                    });
                }
                ConcurrencyUtils.waitForCompletion(futures);
            } else {
                double elem;
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < cols; c++) {
                        elem = (double) X.getQuick(r + rOff, c + cOff);
                        if (elem >= threshold) {
                            px[c + cols * r] = elem;
                        } else {
                            px[c + cols * r] = 0;
                        }
                    }
                }
            }
        } else {
            final double[] elems = (double[]) X.elements();
            final int rowStride = X.columns();
            if ((np > 1) && (rows * cols >= ConcurrencyUtils.getThreadsBeginN_2D())) {
                Future<?>[] futures = new Future[np];
                int k = rows / np;
                for (int j = 0; j < np; j++) {
                    final int firstRow = j * k;
                    final int lastRow = (j == np - 1) ? rows : firstRow + k;
                    futures[j] = ConcurrencyUtils.submit(new Runnable() {
                        public void run() {
                            int idx;
                            for (int r = firstRow; r < lastRow; r++) {
                                for (int c = 0; c < cols; c++) {
                                    idx = (r + rOff) * rowStride + (c + cOff);
                                    if ((double) elems[idx] >= threshold) {
                                        px[r * cols + c] = (double) elems[idx];
                                    } else {
                                        px[r * cols + c] = 0;
                                    }
                                }
                            }
                        }
                    });
                }
                ConcurrencyUtils.waitForCompletion(futures);
            } else {
                int idx;
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < cols; c++) {
                        idx = (r + rOff) * rowStride + (c + cOff);
                        if ((double) elems[idx] >= threshold) {
                            px[r * cols + c] = (double) elems[idx];
                        } else {
                            px[r * cols + c] = 0;
                        }
                    }
                }
            }
        }
    }

    /**
     * Copies pixel values from matrix <code>X</code> to image processor
     * <code>ip</code>.
     * 
     * @param ip
     *            image processor
     * @param X
     *            matrix
     * @param cmY
     *            color model
     * @param threshold
     *            the smallest positive value assigned to the image processor,
     *            all the values less than the threshold are set to zero
     * 
     */
    public static void assignPixelsToProcessor(final EvPixels ip, final DoubleMatrix2D X, final double threshold) {
        final int rows = X.rows();
        final int cols = X.columns();
        final double[] px = ip.convertToDouble(true).getArrayDouble();
        int np = ConcurrencyUtils.getNumberOfThreads();
        if (X.isView()) {
            if ((np > 1) && (rows * cols >= ConcurrencyUtils.getThreadsBeginN_2D())) {
                Future<?>[] futures = new Future[np];
                int k = rows / np;
                for (int j = 0; j < np; j++) {
                    final int firstRow = j * k;
                    final int lastRow = (j == np - 1) ? rows : firstRow + k;
                    futures[j] = ConcurrencyUtils.submit(new Runnable() {
                        public void run() {
                            double elem;
                            for (int r = firstRow; r < lastRow; r++) {
                                for (int c = 0; c < cols; c++) {
                                    elem = (double) X.getQuick(r, c);
                                    if (elem >= threshold) {
                                        px[c + cols * r] = elem;
                                    } else {
                                        px[c + cols * r] = 0;
                                    }
                                }
                            }
                        }
                    });
                }
                ConcurrencyUtils.waitForCompletion(futures);
            } else {
                double elem;
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < cols; c++) {
                        elem = (double) X.getQuick(r, c);
                        if (elem >= threshold) {
                            px[c + cols * r] = elem;
                        } else {
                            px[c + cols * r] = 0;
                        }
                    }
                }
            }
        } else {
            final double[] elems = (double[]) X.elements();
            if ((np > 1) && (rows * cols >= ConcurrencyUtils.getThreadsBeginN_2D())) {
                Future<?>[] futures = new Future[np];
                int k = rows / np;
                for (int j = 0; j < np; j++) {
                    final int firstRow = j * k;
                    final int lastRow = (j == np - 1) ? rows : firstRow + k;
                    futures[j] = ConcurrencyUtils.submit(new Runnable() {
                        public void run() {
                            int idx = firstRow * cols;
                            for (int r = firstRow; r < lastRow; r++) {
                                for (int c = 0; c < cols; c++) {
                                    if ((double) elems[idx] >= threshold) {
                                        px[idx] = (double) elems[idx];
                                    } else {
                                        px[idx] = 0;
                                    }
                                    idx++;
                                }
                            }
                        }
                    });
                }
                ConcurrencyUtils.waitForCompletion(futures);
            } else {
                int idx = 0;
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < cols; c++) {
                        if ((double) elems[idx] >= threshold) {
                            px[idx] = (double) elems[idx];
                        } else {
                            px[idx] = 0;
                        }
                        idx++;
                    }
                }
            }
        }
    }

    /**
     * Computes the circular shift of <code>PSF</code> matrix. This method
     * computes a matrix containing first column of a blurring matrix when
     * implementing periodic boundary conditions.
     * 
     * @param PSF
     *            real matrix containing the point spread function.
     * @param center
     *            indices of center of <code>PSF</code>.
     * @return real matrix containing first column of a blurring matrix
     */
    public static DoubleMatrix2D circShift(DoubleMatrix2D PSF, int[] center) {

        int rows = PSF.rows();
        int cols = PSF.columns();
        int cr = center[0];
        int cc = center[1];
        DoubleMatrix2D P1 = new DenseDoubleMatrix2D(rows, cols);
        P1.viewPart(0, 0, rows - cr, cols - cc).assign(PSF.viewPart(cr, cc, rows - cr, cols - cc));
        P1.viewPart(0, cols - cc, rows - cr, cc).assign(PSF.viewPart(cr, 0, rows - cr, cc));
        P1.viewPart(rows - cr, 0, cr, cols - cc).assign(PSF.viewPart(0, cc, cr, cols - cc));
        P1.viewPart(rows - cr, cols - cc, cr, cc).assign(PSF.viewPart(0, 0, cr, cc));
        return P1;
    }

    /**
     * Pads matrix <code>X</code> with periodic boundary conditions.
     * 
     * @param X
     *            matrix to be padded
     * @param rowsPad
     *            number of rows in padded matrix
     * @param colsPad
     *            number of columns in padded matrix
     * @return padded matrix
     */
    public static DoubleMatrix2D padPeriodic(final DoubleMatrix2D X, final int rowsPad, final int colsPad) {
        final int rows = X.rows();
        final int cols = X.columns();
        if ((rows == rowsPad) && (cols == colsPad)) {
            return X;
        }
        final DoubleMatrix2D Xpad = new DenseDoubleMatrix2D(rowsPad, colsPad);
        final int rOff = (rowsPad - rows + 1) / 2;
        final int cOff = (colsPad - cols + 1) / 2;
        final double[] elemsXpad = (double[]) Xpad.elements();
        final int rowStrideXpad = Xpad.columns();
        int np = ConcurrencyUtils.getNumberOfThreads();

        if (X.isView()) {
            if ((np > 1) && (rows * cols >= ConcurrencyUtils.getThreadsBeginN_2D())) {
                Future<?>[] futures = new Future[np];
                int k = rowsPad / np;
                for (int j = 0; j < np; j++) {
                    final int firstIdx = -rOff + j * k;
                    final int lastIdx = (j == np - 1) ? rowsPad - rOff : firstIdx + k;
                    futures[j] = ConcurrencyUtils.submit(new Runnable() {
                        public void run() {
                            int cIn, rIn, cOut, rOut;
                            int idxXpad;
                            for (int r = firstIdx; r < lastIdx; r++) {
                                rOut = r + rOff;
                                rIn = periodic(r, rows);
                                for (int c = -cOff; c < colsPad - cOff; c++) {
                                    cOut = c + cOff;
                                    cIn = periodic(c, cols);
                                    idxXpad = rOut * rowStrideXpad + cOut;
                                    elemsXpad[idxXpad] = X.getQuick(rIn, cIn);
                                }
                            }
                        }
                    });
                }
                ConcurrencyUtils.waitForCompletion(futures);
            } else {
                int cIn, rIn, cOut, rOut;
                int idxXpad;
                for (int r = -rOff; r < rowsPad - rOff; r++) {
                    rOut = r + rOff;
                    rIn = periodic(r, rows);
                    for (int c = -cOff; c < colsPad - cOff; c++) {
                        cOut = c + cOff;
                        cIn = periodic(c, cols);
                        idxXpad = rOut * rowStrideXpad + cOut;
                        elemsXpad[idxXpad] = X.getQuick(rIn, cIn);
                    }
                }
            }
        } else {
            final double[] elemsX = (double[]) X.elements();
            final int rowStrideX = X.columns();
            if ((np > 1) && (rows * cols >= ConcurrencyUtils.getThreadsBeginN_2D())) {
                Future<?>[] futures = new Future[np];
                int k = rowsPad / np;
                for (int j = 0; j < np; j++) {
                    final int firstIdx = -rOff + j * k;
                    final int lastIdx = (j == np - 1) ? rowsPad - rOff : firstIdx + k;
                    futures[j] = ConcurrencyUtils.submit(new Runnable() {
                        public void run() {
                            int cIn, rIn, cOut, rOut;
                            int idxX;
                            int idxXpad;
                            for (int r = firstIdx; r < lastIdx; r++) {
                                rOut = r + rOff;
                                rIn = periodic(r, rows);
                                for (int c = -cOff; c < colsPad - cOff; c++) {
                                    cOut = c + cOff;
                                    cIn = periodic(c, cols);
                                    idxX = rIn * rowStrideX + cIn;
                                    idxXpad = rOut * rowStrideXpad + cOut;
                                    elemsXpad[idxXpad] = elemsX[idxX];
                                }
                            }
                        }
                    });
                }
                ConcurrencyUtils.waitForCompletion(futures);
            } else {
                int cIn, rIn, cOut, rOut;
                int idxXpad;
                int idxX;
                for (int r = -rOff; r < rowsPad - rOff; r++) {
                    rOut = r + rOff;
                    rIn = periodic(r, rows);
                    for (int c = -cOff; c < colsPad - cOff; c++) {
                        cOut = c + cOff;
                        cIn = periodic(c, cols);
                        idxX = rIn * rowStrideX + cIn;
                        idxXpad = rOut * rowStrideXpad + cOut;
                        elemsXpad[idxXpad] = elemsX[idxX];
                    }
                }
            }
        }
        return Xpad;
    }

    /**
     * Pads matrix <code>X</code> with reflexive boundary conditions.
     * 
     * @param X
     *            matrix to be padded
     * @param rowsPad
     *            number of rows in padded matrix
     * @param colsPad
     *            number of columns in padded matrix
     * @return padded matrix
     */
    public static DoubleMatrix2D padReflexive(final DoubleMatrix2D X, final int rowsPad, final int colsPad) {
        final int rows = X.rows();
        final int cols = X.columns();
        if ((rows == rowsPad) && (cols == colsPad)) {
            return X;
        }
        final DoubleMatrix2D Xpad = new DenseDoubleMatrix2D(rowsPad, colsPad);
        final int rOff = (rowsPad - rows + 1) / 2;
        final int cOff = (colsPad - cols + 1) / 2;
        final double[] elemsXpad = (double[]) Xpad.elements();
        final int rowStrideXpad = Xpad.columns();
        int np = ConcurrencyUtils.getNumberOfThreads();
        if (X.isView()) {
            if ((np > 1) && (rows * cols >= ConcurrencyUtils.getThreadsBeginN_2D())) {
                Future<?>[] futures = new Future[np];
                int k = rowsPad / np;
                for (int j = 0; j < np; j++) {
                    final int firstIdx = -rOff + j * k;
                    final int lastIdx = (j == np - 1) ? rowsPad - rOff : firstIdx + k;
                    futures[j] = ConcurrencyUtils.submit(new Runnable() {
                        public void run() {
                            int cIn, rIn, cOut, rOut;
                            int idxXpad;
                            for (int r = firstIdx; r < lastIdx; r++) {
                                rOut = r + rOff;
                                rIn = mirror(r, rows);
                                for (int c = -cOff; c < colsPad - cOff; c++) {
                                    cOut = c + cOff;
                                    cIn = mirror(c, cols);
                                    idxXpad = rOut * rowStrideXpad + cOut;
                                    elemsXpad[idxXpad] = X.getQuick(rIn, cIn);
                                }
                            }
                        }
                    });
                }
                ConcurrencyUtils.waitForCompletion(futures);
            } else {
                int cIn, rIn, cOut, rOut;
                int idxXpad;
                for (int r = -rOff; r < rowsPad - rOff; r++) {
                    rOut = r + rOff;
                    rIn = mirror(r, rows);
                    for (int c = -cOff; c < colsPad - cOff; c++) {
                        cOut = c + cOff;
                        cIn = mirror(c, cols);
                        idxXpad = rOut * rowStrideXpad + cOut;
                        elemsXpad[idxXpad] = X.getQuick(rIn, cIn);
                    }
                }
            }
        } else {
            final double[] elemsX = (double[]) X.elements();
            final int rowStrideX = X.columns();
            if ((np > 1) && (rows * cols >= ConcurrencyUtils.getThreadsBeginN_2D())) {
                Future<?>[] futures = new Future[np];
                int k = rowsPad / np;
                for (int j = 0; j < np; j++) {
                    final int firstIdx = -rOff + j * k;
                    final int lastIdx = (j == np - 1) ? rowsPad - rOff : firstIdx + k;
                    futures[j] = ConcurrencyUtils.submit(new Runnable() {
                        public void run() {
                            int cIn, rIn, cOut, rOut;
                            int idxX;
                            int idxXpad;
                            for (int r = firstIdx; r < lastIdx; r++) {
                                rOut = r + rOff;
                                rIn = mirror(r, rows);
                                for (int c = -cOff; c < colsPad - cOff; c++) {
                                    cOut = c + cOff;
                                    cIn = mirror(c, cols);
                                    idxX = rIn * rowStrideX + cIn;
                                    idxXpad = rOut * rowStrideXpad + cOut;
                                    elemsXpad[idxXpad] = elemsX[idxX];
                                }
                            }
                        }
                    });
                }
                ConcurrencyUtils.waitForCompletion(futures);
            } else {
                int cIn, rIn, cOut, rOut;
                int idxX;
                int idxXpad;
                for (int r = -rOff; r < rowsPad - rOff; r++) {
                    rOut = r + rOff;
                    rIn = mirror(r, rows);
                    for (int c = -cOff; c < colsPad - cOff; c++) {
                        cOut = c + cOff;
                        cIn = mirror(c, cols);
                        idxX = rIn * rowStrideX + cIn;
                        idxXpad = rOut * rowStrideXpad + cOut;
                        elemsXpad[idxXpad] = elemsX[idxX];
                    }
                }
            }

        }
        return Xpad;
    }

    /**
     * Pads matrix <code>X</code> with zero boundary conditions.
     * 
     * @param X
     *            matrix to be padded
     * @param rowsPad
     *            number of rows in padded matrix
     * @param colsPad
     *            number of columns in padded matrix
     * @return padded matrix
     */
    public static DoubleMatrix2D padZero(DoubleMatrix2D X, int rowsPad, int colsPad) {
        final int rows = X.rows();
        final int cols = X.columns();
        if ((rows == rowsPad) && (cols == colsPad)) {
            return X;
        }
        DoubleMatrix2D Xpad = new DenseDoubleMatrix2D(rowsPad, colsPad);
        final int rOff = (rowsPad - rows + 1) / 2;
        final int cOff = (colsPad - cols + 1) / 2;
        Xpad.viewPart(rOff, cOff, rows, cols).assign(X);
        return Xpad;
    }

    /**
     * Pads matrix <code>X</code> with zero boundary conditions.
     * 
     * @param X
     *            matrix to be padded
     * @param padSize
     *            padding size
     * @param padding
     *            type of padding
     * @return padded matrix
     */
    public static DoubleMatrix2D padZero(DoubleMatrix2D X, int[] padSize, PaddingType padding) {
        if ((padSize[0] == 0) && (padSize[1] == 0)) {
            return X;
        }
        DoubleMatrix2D Xpad = null;
        switch (padding) {
        case BOTH:
            Xpad = new DenseDoubleMatrix2D(X.rows() + 2 * padSize[0], X.columns() + 2 * padSize[1]);
            Xpad.viewPart(padSize[0], padSize[1], X.rows(), X.columns()).assign(X);
            break;
        case POST:
            Xpad = new DenseDoubleMatrix2D(X.rows() + padSize[0], X.columns() + padSize[1]);
            Xpad.viewPart(0, 0, X.rows(), X.columns()).assign(X);
            break;
        case PRE:
            Xpad = new DenseDoubleMatrix2D(X.rows() + padSize[0], X.columns() + padSize[1]);
            Xpad.viewPart(padSize[0], padSize[1], X.rows(), X.columns()).assign(X);
            break;
        }
        return Xpad;
    }

    public static DoubleMatrix2D dctShift(DoubleMatrix2D PSF, int[] center) {
        int rows = PSF.rows();
        int cols = PSF.columns();
        int cr = center[0];
        int cc = center[1];
        int k = Math.min(Math.min(Math.min(cr, rows - cr - 1), cc), cols - cc - 1);
        int frow = cr - k;
        int lrow = cr + k;
        int rowSize = lrow - frow + 1;
        int fcol = cc - k;
        int lcol = cc + k;
        int colSize = lcol - fcol + 1;

        DoubleMatrix2D PP = new DenseDoubleMatrix2D(rowSize, colSize);
        DoubleMatrix2D P1 = new DenseDoubleMatrix2D(rowSize, colSize);
        DoubleMatrix2D P2 = new DenseDoubleMatrix2D(rowSize, colSize);
        DoubleMatrix2D P3 = new DenseDoubleMatrix2D(rowSize, colSize);
        DoubleMatrix2D P4 = new DenseDoubleMatrix2D(rowSize, colSize);
        DoubleMatrix2D Ps = new DenseDoubleMatrix2D(rows, cols);
        PP.assign(PSF.viewPart(frow, fcol, rowSize, colSize));

        P1.viewPart(0, 0, rowSize - cr + frow, colSize - cc + fcol).assign(PP.viewPart(cr - frow, cc - fcol, rowSize - cr + frow, colSize - cc + fcol));
        P2.viewPart(0, 0, rowSize - cr + frow, colSize - cc + fcol - 1).assign(PP.viewPart(cr - frow, cc - fcol + 1, rowSize - cr + frow, colSize - cc + fcol - 1));
        P3.viewPart(0, 0, rowSize - cr + frow - 1, colSize - cc + fcol).assign(PP.viewPart(cr - frow + 1, cc - fcol, rowSize - cr + frow - 1, colSize - cc + fcol));
        P4.viewPart(0, 0, rowSize - cr + frow - 1, colSize - cc + fcol - 1).assign(PP.viewPart(cr - frow + 1, cc - fcol + 1, rowSize - cr + frow - 1, colSize - cc + fcol - 1));
        P1.assign(P2, DoubleFunctions.plus);
        P1.assign(P3, DoubleFunctions.plus);
        P1.assign(P4, DoubleFunctions.plus);
        Ps.viewPart(0, 0, 2 * k + 1, 2 * k + 1).assign(P1);
        return Ps.copy();
    }

    public static void swapQuadrants(final int rows, final int columns, DoubleMatrix2D X) {
        final double[] x = (double[]) X.elements();
        final int cHalf = columns / 2;
        final int rHalf = rows / 2;
        int size = columns * rows / 2;
        int np = ConcurrencyUtils.getNumberOfThreads();
        if ((np > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            Future<?>[] futures = new Future[np];
            int krow = rHalf / np;
            for (int j = 0; j < np; j++) {
                final int firstRow = j * krow;
                final int lastRow = (j == np - 1) ? rHalf : firstRow + krow;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {

                    public void run() {
                        int rP, idx1, idx2;
                        double temp;
                        for (int r = firstRow; r < lastRow; r++) {
                            rP = r + rHalf;
                            for (int c = 0; c < columns; c++) {
                                idx1 = c + columns * r;
                                idx2 = c + columns * rP;
                                temp = x[idx1];
                                x[idx1] = x[idx2];
                                x[idx2] = temp;
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            int rP, idx1, idx2;
            double temp;
            for (int r = 0; r < rHalf; r++) {
                rP = r + rHalf;
                for (int c = 0; c < columns; c++) {
                    idx1 = c + columns * r;
                    idx2 = c + columns * rP;
                    temp = x[idx1];
                    x[idx1] = x[idx2];
                    x[idx2] = temp;
                }
            }
        }
        if ((np > 1) && (size >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            Future<?>[] futures = new Future[np];
            int kcol = cHalf / np;
            for (int j = 0; j < np; j++) {
                final int firstCol = j * kcol;
                final int lastCol = (j == np - 1) ? cHalf : firstCol + kcol;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        int cP, idx1, idx2;
                        double temp;
                        for (int c = firstCol; c < lastCol; c++) {
                            cP = c + cHalf;
                            for (int r = 0; r < rows; r++) {
                                idx1 = c + columns * r;
                                idx2 = cP + columns * r;
                                temp = x[idx1];
                                x[idx1] = x[idx2];
                                x[idx2] = temp;
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            int cP, idx1, idx2;
            double temp;
            for (int c = 0; c < cHalf; c++) {
                cP = c + cHalf;
                for (int r = 0; r < rows; r++) {
                    idx1 = c + columns * r;
                    idx2 = cP + columns * r;
                    temp = x[idx1];
                    x[idx1] = x[idx2];
                    x[idx2] = temp;
                }
            }
        }
    }

    public static void convolveTransposeFD(DoubleMatrix2D H1, DoubleMatrix2D H2, DoubleMatrix2D Result) {
        final int rows = H1.rows();
        final int columns = H1.columns();
        final double[] h1 = (double[]) H1.elements();
        final double[] h2 = (double[]) H2.elements();
        final double[] result = (double[]) Result.elements();
        int cC, rC, idx1, idx2;
        double h2e, h2o;
        for (int r = 0; r < rows; r++) {
            rC = (rows - r) % rows;
            for (int c = 0; c < columns; c++) {
                cC = (columns - c) % columns;
                idx1 = c + columns * r;
                idx2 = cC + columns * rC;
                h2e = (h2[idx1] + h2[idx2]) / 2;
                h2o = (h2[idx1] - h2[idx2]) / 2;
                result[idx2] = (double) (h1[idx1] * h2e - h1[idx2] * h2o);
            }
        }
    }

    public static void convolveFD(DoubleMatrix2D H1, DoubleMatrix2D H2, DoubleMatrix2D Result) {
        final int rows = H1.rows();
        final int columns = H1.columns();
        final double[] h1 = (double[]) H1.elements();
        final double[] h2 = (double[]) H2.elements();
        final double[] result = (double[]) Result.elements();
        int np = ConcurrencyUtils.getNumberOfThreads();
        if ((np > 1) && (columns * rows >= ConcurrencyUtils.getThreadsBeginN_2D())) {
            Future<?>[] futures = new Future[np];
            int k = rows / np;
            for (int j = 0; j < np; j++) {
                final int firstRow = j * k;
                final int lastRow = (j == np - 1) ? rows : firstRow + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        int cC, rC, idx1, idx2;
                        double h2e, h2o;
                        for (int r = firstRow; r < lastRow; r++) {
                            rC = (rows - r) % rows;
                            for (int c = 0; c < columns; c++) {
                                cC = (columns - c) % columns;
                                idx1 = c + columns * r;
                                idx2 = cC + columns * rC;
                                h2e = (h2[idx1] + h2[idx2]) / 2;
                                h2o = (h2[idx1] - h2[idx2]) / 2;
                                result[idx1] = (double) (h1[idx1] * h2e + h1[idx2] * h2o);
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            int cC, rC, idx1, idx2;
            double h2e, h2o;
            for (int r = 0; r < rows; r++) {
                rC = (rows - r) % rows;
                for (int c = 0; c < columns; c++) {
                    cC = (columns - c) % columns;
                    idx1 = c + columns * r;
                    idx2 = cC + columns * rC;
                    h2e = (h2[idx1] + h2[idx2]) / 2;
                    h2o = (h2[idx1] - h2[idx2]) / 2;
                    result[idx1] = (double) (h1[idx1] * h2e + h1[idx2] * h2o);
                }
            }
        }
    }

    private static int mirror(int i, int n) {
        int ip = mod(i, 2 * n);
        if (ip < n) {
            return ip;
        } else {
            return n - (ip % n) - 1;
        }
    }

    private static int mod(int i, int n) {
        return ((i % n) + n) % n;
    }

    private static int periodic(int i, int n) {
        int ip = mod(i, 2 * n);
        if (ip < n) {
            return ip;
        } else {
            return (ip % n);
        }
    }

}
