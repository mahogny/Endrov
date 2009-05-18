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

import cern.colt.matrix.tdcomplex.DComplexMatrix3D;
import cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix3D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.DoubleMatrix3D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix3D;
import cern.jet.math.tdouble.DoubleFunctions;
import edu.emory.mathcs.utils.ConcurrencyUtils;
import endrov.deconvolution.DeconvPixelsStack;
import endrov.deconvolution.iterative.IterativeEnums.PaddingType;
import endrov.imageset.EvImage;
import endrov.imageset.EvPixels;
import endrov.imageset.EvStack;

/**
 * Common methods for 3D iterative deblurring. Some code is from Bob Dougherty's Iterative
 * Deconvolve 3D.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class DoubleCommon3D {

    private DoubleCommon3D() {
    }

    /**
     * Copies pixel values from image stack <code>stack</code> to matrix
     * <code>X</code>
     * 
     * @param stack
     *            image stack
     * @param X
     *            matrix
     */
    public static void assignPixelsToMatrix(final EvStack stack, final DoubleMatrix3D X) {
        int slices = X.slices();
        int np = ConcurrencyUtils.getNumberOfThreads();
        if ((np > 1) && (X.size() >= ConcurrencyUtils.getThreadsBeginN_3D())) {
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
                        for (int s = startslice; s < stopslice; s++) {
                            EvPixels ip = stack.getPixels()[s]; //getProcessor(s + 1);  //TODO investigate +1
                            DoubleCommon2D.assignPixelsToMatrix(X.viewSlice(s), ip);
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int s = 0; s < slices; s++) {
            		EvPixels ip = stack.getPixels()[s+1];
                //ImageProcessor ip = stack.getProcessor(s + 1);
                DoubleCommon2D.assignPixelsToMatrix(X.viewSlice(s), ip);
            }
        }
    }
    
    
    

    /**
     * Copies pixel values from real matrix <code>X</code> to image stack
     * <code>stack</code>
     * 
     * @param stack
     *            image stack
     * @param X
     *            real matrix
     * @param cmY
     *            color model
     */
    public static void assignPixelsToStack(final DeconvPixelsStack stack, final DoubleMatrix3D X) {
        final int slices = X.slices();
        final int rows = X.rows();
        final int cols = X.columns();
        for (int s = 0; s < slices; s++) {
            //EvPixels ip = new EvPixels(EvPixels.TYPE_DOUBLE, cols, rows);
        	EvPixels ip=DoubleCommon2D.assignPixelsToProcessor(rows, cols, X.viewSlice(s));
            stack.addSlice(ip, s);
            
        }
    }

    /**
     * Copies pixel values from real matrix <code>X</code> to image stack
     * <code>stack</code>
     * 
     * @param stack
     *            image stack
     * @param X
     *            real matrix
     * @param cmY
     *            color model
     * @param threshold
     *            the smallest positive value assigned to the image processor,
     *            all the values less than the threshold are set to zero
     * 
     */
    public static void assignPixelsToStack(final DeconvPixelsStack stack, final DoubleMatrix3D X, double threshold) {
        final int slices = X.slices();
        //final int rows = X.rows();
        //final int cols = X.columns();
        for (int s = 0; s < slices; s++) {
            //FloatProcessor ip = new FloatProcessor(cols, rows);
            //EvPixels ip=new EvPixels(EvPixels.TYPE_DOUBLE,cols,rows);
            EvPixels ip=DoubleCommon2D.assignPixelsToProcessor(X.viewSlice(s), threshold);
            stack.addSlice(ip, s);
        }
    }

    /**
     * Copies pixel values from real padded matrix <code>X</code> to image stack
     * <code>stack</code>
     * 
     * @param stack
     *            image stack
     * @param X
     *            padded real matrix
     * @param slices
     *            original number of slices
     * @param rows
     *            original number of rows
     * @param cols
     *            original number of columns
     * @param sOff
     *            slice offset
     * @param rOff
     *            row offset
     * @param cOff
     *            column offset
     * @param cmY
     *            color model
     */
    public static void assignPixelsToStackPadded(final DeconvPixelsStack stack, final DoubleMatrix3D X, final int slices, final int rows, final int cols, final int sOff, final int rOff, final int cOff) {
    for (int s = 0; s < slices; s++) {
    			//EvPixels ip=new EvPixels(EvPixels.TYPE_DOUBLE,cols,rows);
    			EvPixels ip=DoubleCommon2D.assignPixelsToProcessorPadded(X.viewSlice(s + sOff), rows, cols, rOff, cOff);
            stack.addSlice(ip, s);
        }
    }

    /**
     * Copies pixel values from real padded matrix <code>X</code> to image stack
     * <code>stack</code>
     * 
     * @param stack
     *            image stack
     * @param X
     *            padded real matrix
     * @param slices
     *            original number of slices
     * @param rows
     *            original number of rows
     * @param cols
     *            original number of columns
     * @param sOff
     *            slice offset
     * @param rOff
     *            row offset
     * @param cOff
     *            column offset
     * @param cmY
     *            color model
     * @param threshold
     *            the smallest positive value assigned to the stack, all the
     *            values less than the threshold are set to zero
     * 
     */
    public static void assignPixelsToStackPadded(final DeconvPixelsStack stack, final DoubleMatrix3D X, final int slices, final int rows, final int cols, final int sOff, final int rOff, final int cOff, final double threshold) {
    for (int s = 0; s < slices; s++) {
            //EvPixels ip=new EvPixels(EvPixels.TYPE_DOUBLE,cols,rows);
            EvPixels ip=DoubleCommon2D.assignPixelsToProcessorPadded(X.viewSlice(s + sOff), rows, cols, rOff, cOff, threshold);
            stack.addSlice(ip, s);
        }
    }

    /**
     * Updates pixel values in image stack <code>stack</code>
     * 
     * @param stack
     *            image stack
     * @param X
     *            real matrix
     * @param cmY
     *            color model
     * 
     */
    public static void updatePixelsInStack(final EvStack stack, final DoubleMatrix3D X) {
    EvImage[] p=stack.getImages();    
    final int slices = X.slices();
        for (int s = 0; s < slices; s++) {
        		p[s].setPixelsReference(DoubleCommon2D.assignPixelsToProcessor(X.rows(),X.columns(),X.viewSlice(s)));
        }
    }

    /**
     * Updates pixel values in image stack <code>stack</code>
     * 
     * @param stack
     *            image stack
     * @param X
     *            real matrix
     * @param slices
     *            original number of slices
     * @param rows
     *            original number of rows
     * @param cols
     *            original number of columns
     * @param sOff
     *            slice offset
     * @param rOff
     *            row offset
     * @param cOff
     *            column offset
     * @param cmY
     *            color model
     * @param threshold
     *            the smallest positive value assigned to the image processor,
     *            all the values less than the threshold are set to zero
     * 
     */
    public static void updatePixelsInStackPadded(final EvStack stack, final DoubleMatrix3D X, final int slices, final int rows, final int cols, final int sOff, final int rOff, final int cOff, double threshold) {
    EvImage[] p=stack.getImages();    
        for (int s = 0; s < slices; s++) {
        	p[s].setPixelsReference(DoubleCommon2D.assignPixelsToProcessorPadded(X.viewSlice(s + sOff), rows, cols, rOff, cOff, threshold));
        }
    }

    /**
     * Updates pixel values in image stack <code>stack</code>
     * 
     * @param stack
     *            image stack
     * @param X
     *            real matrix
     * @param slices
     *            original number of slices
     * @param rows
     *            original number of rows
     * @param cols
     *            original number of columns
     * @param sOff
     *            slice offset
     * @param rOff
     *            row offset
     * @param cOff
     *            column offset
     * @param cmY
     *            color model
     */
    public static void updatePixelsInStackPadded(final EvStack stack, final DoubleMatrix3D X, final int slices, final int rows, final int cols, final int sOff, final int rOff, final int cOff) {
    EvImage[] p=stack.getImages();    
    for (int s = 0; s < slices; s++) {
            p[s].setPixelsReference(DoubleCommon2D.assignPixelsToProcessorPadded(X.viewSlice(s + sOff), rows, cols, rOff, cOff));
        }
    }

    /**
     * Updates pixel values in image stack <code>stack</code>
     * 
     * @param stack
     *            image stack
     * @param X
     *            real matrix
     * @param cmY
     *            color model
     * @param threshold
     *            the smallest positive value assigned to the image processor,
     *            all the values less than the threshold are set to zero
     * 
     */
    public static void updatePixelsInStack(final EvStack stack, final DoubleMatrix3D X, double threshold) {
    EvImage[] p=stack.getImages();    
        final int slices = X.slices();
        for (int s = 0; s < slices; s++) {
            p[s].setPixelsReference(DoubleCommon2D.assignPixelsToProcessor(X.viewSlice(s), threshold));
        }
    }

    /**
     * Copies pixel values from real matrix <code>X</code> to image stack
     * <code>stack</code>
     * 
     * @param stack
     *            image stack
     * @param X
     *            real matrix
     * @param xsize3D
     *            size of X as 3D matrix
     * @param cmY
     *            color model
     * 
     */
    public static void assignPixelsToStack(final DeconvPixelsStack stack, final DoubleMatrix1D X, int[] xsize3D) {
        final int slices = xsize3D[0];
        final int rows = xsize3D[1];
        final int cols = xsize3D[2];
        final int sliceStride = rows * cols;

        for (int s = 0; s < slices; s++) {
            //FloatProcessor ip = new FloatProcessor(cols, rows);
            //EvPixels ip=new EvPixels(EvPixels.TYPE_DOUBLE,cols,rows);
            EvPixels ip=DoubleCommon2D.assignPixelsToProcessor(rows, cols, X.viewPart(s * sliceStride, sliceStride));
            stack.addSlice(ip, s);
        }
    }

    /**
     * Copies pixel values from real matrix <code>X</code> to image stack
     * <code>stack</code>
     * 
     * @param stack
     *            image stack
     * @param X
     *            real matrix
     * @param xsize3D
     *            size of X as 3D matrix
     * @param cmY
     *            color model
     * @param threshold
     *            the smallest positive value assigned to the image processor,
     *            all the values less than the threshold are set to zero
     * 
     */
    public static void assignPixelsToStack(final DeconvPixelsStack stack, final DoubleMatrix1D X, int[] xsize3D, double threshold) {
        final int slices = xsize3D[0];
        final int rows = xsize3D[1];
        final int cols = xsize3D[2];
        final int sliceStride = rows * cols;

        for (int s = 0; s < slices; s++) {
            //FloatProcessor ip = new FloatProcessor(cols, rows);
            //EvPixels ip=new EvPixels(EvPixels.TYPE_DOUBLE,cols,rows);
            EvPixels ip=DoubleCommon2D.assignPixelsToProcessor(rows, cols, X.viewPart(s * sliceStride, sliceStride), threshold);
            //ip.setColorModel(cmY);
            //ip.setMinAndMax(0, 0);
            stack.addSlice(ip, s);
            
        }
    }

    /**
     * Updates pixel values in image stack <code>stack</code>
     * 
     * @param stack
     *            image stack
     * @param X
     *            real matrix
     * @param xsize3D
     *            size of X as 3D matrix
     * @param cmY
     *            color model
     */
    public static void updatePixelsInStack(final EvStack stack, final DoubleMatrix1D X, int[] xsize3D) {
        final int slices = xsize3D[0];
        final int rows = xsize3D[1];
        final int cols = xsize3D[2];
        final int sliceStride = rows * cols;
        EvImage[] p=stack.getImages();    

        for (int s = 0; s < slices; s++) {
            //EvPixels ip = stack.getPixels()[s + 1];
            p[s].setPixelsReference(DoubleCommon2D.assignPixelsToProcessor(rows,cols,X.viewPart(s * sliceStride, sliceStride)));
            //Not that simple
        }
    }

    /**
     * Updates pixel values in image stack <code>stack</code>
     * 
     * @param stack
     *            image stack
     * @param X
     *            real matrix
     * @param xsize3D
     *            size of X as 3D matrix
     * @param cmY
     *            color model
     * @param threshold
     *            the smallest positive value assigned to the image processor,
     *            all the values less than the threshold are set to zero
     * 
     */
    public static void updatePixelsInStack(final EvStack stack, final DoubleMatrix1D X, int[] xsize3D, double threshold) {
        final int slices = xsize3D[0];
        final int rows = xsize3D[1];
        final int cols = xsize3D[2];
        final int sliceStride = rows * cols;

        for (int s = 0; s < slices; s++) {
        		//EvPixels ip = stack.getPixels()[s + 1];
            DoubleCommon2D.assignPixelsToProcessor(rows, cols, X.viewPart(s * sliceStride, sliceStride), threshold);
//not that simple
        }
    }

    /**
     * Computes the circular shift of <code>PSF</code> matrix.
     * 
     * @param PSF
     *            real matrix containing the point spread function.
     * @param center
     *            indices of center of <code>PSF</code>.
     * @return shifted matrix
     */
    public static DoubleMatrix3D circShift(DoubleMatrix3D PSF, int[] center) {
        int slices = PSF.slices();
        int rows = PSF.rows();
        int cols = PSF.columns();
        int cs = center[0];
        int cr = center[1];
        int cc = center[2];
        DoubleMatrix3D P1 = new DenseDoubleMatrix3D(slices, rows, cols);

        P1.viewPart(0, 0, 0, slices - cs, rows - cr, cols - cc).assign(PSF.viewPart(cs, cr, cc, slices - cs, rows - cr, cols - cc));
        P1.viewPart(0, rows - cr, 0, slices - cs, cr, cols - cc).assign(PSF.viewPart(cs, 0, cc, slices - cs, cr, cols - cc));
        P1.viewPart(0, 0, cols - cc, slices - cs, rows - cr, cc).assign(PSF.viewPart(cs, cr, 0, slices - cs, rows - cr, cc));
        P1.viewPart(0, rows - cr, cols - cc, slices - cs, cr, cc).assign(PSF.viewPart(cs, 0, 0, slices - cs, cr, cc));

        P1.viewPart(slices - cs, 0, 0, cs, rows - cr, cols - cc).assign(PSF.viewPart(0, cr, cc, cs, rows - cr, cols - cc));
        P1.viewPart(slices - cs, 0, cols - cc, cs, rows - cr, cc).assign(PSF.viewPart(0, cr, 0, cs, rows - cr, cc));
        P1.viewPart(slices - cs, rows - cr, 0, cs, cr, cols - cc).assign(PSF.viewPart(0, 0, cc, cs, cr, cols - cc));
        P1.viewPart(slices - cs, rows - cr, cols - cc, cs, cr, cc).assign(PSF.viewPart(0, 0, 0, cs, cr, cc));
        return P1;
    }


    /**
     * Pads matrix <code>X</code> with periodic boundary conditions.
     * 
     * @param X
     *            matrix to be padded
     * @param slicesPad
     *            number of slices in padded matrix
     * @param rowsPad
     *            number of rows in padded matrix
     * @param colsPad
     *            number of columns in padded matrix
     * 
     * @return padded matrix
     */
    public static DoubleMatrix3D padPeriodic(final DoubleMatrix3D X, final int slicesPad, final int rowsPad, final int colsPad) {
        final int slices = X.slices();
        final int rows = X.rows();
        final int cols = X.columns();
        if ((slices == slicesPad) && (rows == rowsPad) && (cols == colsPad)) {
            return X;
        }
        final DoubleMatrix3D Xpad = new DenseDoubleMatrix3D(slicesPad, rowsPad, colsPad);
        final int sOff = (slicesPad - slices + 1) / 2;
        final int rOff = (rowsPad - rows + 1) / 2;
        final int cOff = (colsPad - cols + 1) / 2;
        final double[] elemsXpad = (double[]) Xpad.elements();
        final int slicesStrideXpad = Xpad.rows() * Xpad.columns();
        final int rowsStrideXpad = Xpad.columns();
        int np = ConcurrencyUtils.getNumberOfThreads();
        if (X.isView()) {
            if ((np > 1) && (X.size() >= ConcurrencyUtils.getThreadsBeginN_3D())) {
                Future<?>[] futures = new Future[np];
                int k = slicesPad / np;
                for (int j = 0; j < np; j++) {
                    final int startslice = -sOff + j * k;
                    final int stopslice;
                    if (j == np - 1) {
                        stopslice = slicesPad - sOff;
                    } else {
                        stopslice = startslice + k;
                    }
                    futures[j] = ConcurrencyUtils.submit(new Runnable() {

                        public void run() {
                            int cIn, rIn, sIn, cOut, rOut, sOut;
                            int idxXpad;
                            for (int s = startslice; s < stopslice; s++) {
                                sOut = s + sOff;
                                sIn = periodic(s, slices);
                                for (int r = -rOff; r < rowsPad - rOff; r++) {
                                    rOut = r + rOff;
                                    rIn = periodic(r, rows);
                                    for (int c = -cOff; c < colsPad - cOff; c++) {
                                        cOut = c + cOff;
                                        cIn = periodic(c, cols);
                                        idxXpad = sOut * slicesStrideXpad + rOut * rowsStrideXpad + cOut;
                                        elemsXpad[idxXpad] = X.getQuick(sIn, rIn, cIn);
                                    }
                                }
                            }
                        }
                    });
                }
                ConcurrencyUtils.waitForCompletion(futures);
            } else {
                int cIn, rIn, sIn, cOut, rOut, sOut;
                int idxXpad;
                for (int s = -sOff; s < slicesPad - sOff; s++) {
                    sOut = s + sOff;
                    sIn = periodic(s, slices);
                    for (int r = -rOff; r < rowsPad - rOff; r++) {
                        rOut = r + rOff;
                        rIn = periodic(r, rows);
                        for (int c = -cOff; c < colsPad - cOff; c++) {
                            cOut = c + cOff;
                            cIn = periodic(c, cols);
                            idxXpad = sOut * slicesStrideXpad + rOut * rowsStrideXpad + cOut;
                            elemsXpad[idxXpad] = X.getQuick(sIn, rIn, cIn);
                        }
                    }
                }
            }
        } else {
            final double[] elemsX = (double[]) X.elements();
            final int slicesStrideX = X.rows() * X.columns();
            final int rowsStrideX = X.columns();
            if ((np > 1) && (X.size() >= ConcurrencyUtils.getThreadsBeginN_3D())) {
                Future<?>[] futures = new Future[np];
                int k = slicesPad / np;
                for (int j = 0; j < np; j++) {
                    final int startslice = -sOff + j * k;
                    final int stopslice;
                    if (j == np - 1) {
                        stopslice = slicesPad - sOff;
                    } else {
                        stopslice = startslice + k;
                    }
                    futures[j] = ConcurrencyUtils.submit(new Runnable() {

                        public void run() {
                            int cIn, rIn, sIn, cOut, rOut, sOut;
                            int idxXpad;
                            int idxX;
                            for (int s = startslice; s < stopslice; s++) {
                                sOut = s + sOff;
                                sIn = periodic(s, slices);
                                for (int r = -rOff; r < rowsPad - rOff; r++) {
                                    rOut = r + rOff;
                                    rIn = periodic(r, rows);
                                    for (int c = -cOff; c < colsPad - cOff; c++) {
                                        cOut = c + cOff;
                                        cIn = periodic(c, cols);
                                        idxX = sIn * slicesStrideX + rIn * rowsStrideX + cIn;
                                        idxXpad = sOut * slicesStrideXpad + rOut * rowsStrideXpad + cOut;
                                        elemsXpad[idxXpad] = elemsX[idxX];
                                    }
                                }
                            }
                        }
                    });
                }
                ConcurrencyUtils.waitForCompletion(futures);
            } else {
                int cIn, rIn, sIn, cOut, rOut, sOut;
                int idxX;
                int idxXpad;
                for (int s = -sOff; s < slicesPad - sOff; s++) {
                    sOut = s + sOff;
                    sIn = periodic(s, slices);
                    for (int r = -rOff; r < rowsPad - rOff; r++) {
                        rOut = r + rOff;
                        rIn = periodic(r, rows);
                        for (int c = -cOff; c < colsPad - cOff; c++) {
                            cOut = c + cOff;
                            cIn = periodic(c, cols);
                            idxX = sIn * slicesStrideX + rIn * rowsStrideX + cIn;
                            idxXpad = sOut * slicesStrideXpad + rOut * rowsStrideXpad + cOut;
                            elemsXpad[idxXpad] = elemsX[idxX];
                        }
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
     * @param slicesPad
     *            number of slices in padded matrix
     * @param rowsPad
     *            number of rows in padded matrix
     * @param colsPad
     *            number of columns in padded matrix
     * 
     * @return padded matrix
     */
    public static DoubleMatrix3D padReflexive(final DoubleMatrix3D X, final int slicesPad, final int rowsPad, final int colsPad) {
        final int slices = X.slices();
        final int rows = X.rows();
        final int cols = X.columns();
        if ((slices == slicesPad) && (rows == rowsPad) && (cols == colsPad)) {
            return X;
        }
        final DoubleMatrix3D Xpad = new DenseDoubleMatrix3D(slicesPad, rowsPad, colsPad);
        final int sOff = (slicesPad - slices + 1) / 2;
        final int rOff = (rowsPad - rows + 1) / 2;
        final int cOff = (colsPad - cols + 1) / 2;
        final double[] elemsXpad = (double[]) Xpad.elements();
        final int slicesStrideXpad = Xpad.rows() * Xpad.columns();
        final int rowsStrideXpad = Xpad.columns();
        int np = ConcurrencyUtils.getNumberOfThreads();
        if (X.isView()) {
            if ((np > 1) && (X.size() >= ConcurrencyUtils.getThreadsBeginN_3D())) {
                Future<?>[] futures = new Future[np];
                int k = slicesPad / np;
                for (int j = 0; j < np; j++) {
                    final int startslice = -sOff + j * k;
                    final int stopslice;
                    if (j == np - 1) {
                        stopslice = slicesPad - sOff;
                    } else {
                        stopslice = startslice + k;
                    }
                    futures[j] = ConcurrencyUtils.submit(new Runnable() {

                        public void run() {
                            int cIn, rIn, sIn, cOut, rOut, sOut;
                            int idxXpad;
                            for (int s = startslice; s < stopslice; s++) {
                                sOut = s + sOff;
                                sIn = mirror(s, slices);
                                for (int r = -rOff; r < rowsPad - rOff; r++) {
                                    rOut = r + rOff;
                                    rIn = mirror(r, rows);
                                    for (int c = -cOff; c < colsPad - cOff; c++) {
                                        cOut = c + cOff;
                                        cIn = mirror(c, cols);
                                        idxXpad = sOut * slicesStrideXpad + rOut * rowsStrideXpad + cOut;
                                        elemsXpad[idxXpad] = X.getQuick(sIn, rIn, cIn);
                                    }
                                }
                            }
                        }
                    });
                }
                ConcurrencyUtils.waitForCompletion(futures);
            } else {
                int cIn, rIn, sIn, cOut, rOut, sOut;
                int idxXpad;
                for (int s = -sOff; s < slicesPad - sOff; s++) {
                    sOut = s + sOff;
                    sIn = mirror(s, slices);
                    for (int r = -rOff; r < rowsPad - rOff; r++) {
                        rOut = r + rOff;
                        rIn = mirror(r, rows);
                        for (int c = -cOff; c < colsPad - cOff; c++) {
                            cOut = c + cOff;
                            cIn = mirror(c, cols);
                            idxXpad = sOut * slicesStrideXpad + rOut * rowsStrideXpad + cOut;
                            elemsXpad[idxXpad] = X.getQuick(sIn, rIn, cIn);
                        }
                    }
                }
            }
        } else {
            final double[] elemsX = (double[]) X.elements();
            final int slicesStrideX = X.rows() * X.columns();
            final int rowsStrideX = X.columns();
            if ((np > 1) && (X.size() >= ConcurrencyUtils.getThreadsBeginN_3D())) {
                Future<?>[] futures = new Future[np];
                int k = slicesPad / np;
                for (int j = 0; j < np; j++) {
                    final int startslice = -sOff + j * k;
                    final int stopslice;
                    if (j == np - 1) {
                        stopslice = slicesPad - sOff;
                    } else {
                        stopslice = startslice + k;
                    }
                    futures[j] = ConcurrencyUtils.submit(new Runnable() {

                        public void run() {
                            int cIn, rIn, sIn, cOut, rOut, sOut;
                            int idxX;
                            int idxXpad;
                            for (int s = startslice; s < stopslice; s++) {
                                sOut = s + sOff;
                                sIn = mirror(s, slices);
                                for (int r = -rOff; r < rowsPad - rOff; r++) {
                                    rOut = r + rOff;
                                    rIn = mirror(r, rows);
                                    for (int c = -cOff; c < colsPad - cOff; c++) {
                                        cOut = c + cOff;
                                        cIn = mirror(c, cols);
                                        idxX = sIn * slicesStrideX + rIn * rowsStrideX + cIn;
                                        idxXpad = sOut * slicesStrideXpad + rOut * rowsStrideXpad + cOut;
                                        elemsXpad[idxXpad] = elemsX[idxX];
                                    }
                                }
                            }
                        }
                    });
                }
                ConcurrencyUtils.waitForCompletion(futures);
            } else {
                int cIn, rIn, sIn, cOut, rOut, sOut;
                int idxX;
                int idxXpad;
                for (int s = -sOff; s < slicesPad - sOff; s++) {
                    sOut = s + sOff;
                    sIn = mirror(s, slices);
                    for (int r = -rOff; r < rowsPad - rOff; r++) {
                        rOut = r + rOff;
                        rIn = mirror(r, rows);
                        for (int c = -cOff; c < colsPad - cOff; c++) {
                            cOut = c + cOff;
                            cIn = mirror(c, cols);
                            idxX = sIn * slicesStrideX + rIn * rowsStrideX + cIn;
                            idxXpad = sOut * slicesStrideXpad + rOut * rowsStrideXpad + cOut;
                            elemsXpad[idxXpad] = elemsX[idxX];
                        }
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
     * @param slicesPad
     *            number of slices in padded matrix
     * @param rowsPad
     *            number of rows in padded matrix
     * @param colsPad
     *            number of columns in padded matrix
     * 
     * @return padded matrix
     */
    public static DoubleMatrix3D padZero(DoubleMatrix3D X, int slicesPad, int rowsPad, int colsPad) {
        int slices = X.slices();
        int rows = X.rows();
        int cols = X.columns();
        if ((slices == slicesPad) && (rows == rowsPad) && (cols == colsPad)) {
            return X;
        }
        int kOff = (slicesPad - slices + 1) / 2;
        int jOff = (rowsPad - rows + 1) / 2;
        int iOff = (colsPad - cols + 1) / 2;
        DoubleMatrix3D Xpad = new DenseDoubleMatrix3D(slicesPad, rowsPad, colsPad);
        Xpad.viewPart(kOff, jOff, iOff, slices, rows, cols).assign(X);
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
    public static DoubleMatrix3D padZero(DoubleMatrix3D X, int[] padSize, PaddingType padding) {
        if ((padSize[0] == 0) && (padSize[1] == 0) & (padSize[2] == 0)) {
            return X;
        }
        DoubleMatrix3D Xpad = null;
        switch (padding) {
        case BOTH:
            Xpad = new DenseDoubleMatrix3D(X.slices() + 2 * padSize[0], X.rows() + 2 * padSize[1], X.columns() + 2 * padSize[2]);
            Xpad.viewPart(padSize[0], padSize[1], padSize[2], X.slices(), X.rows(), X.columns()).assign(X);
            break;
        case POST:
            Xpad = new DenseDoubleMatrix3D(X.slices() + padSize[0], X.rows() + padSize[1], X.columns() + padSize[2]);
            Xpad.viewPart(0, 0, 0, X.slices(), X.rows(), X.columns()).assign(X);
            break;
        case PRE:
            Xpad = new DenseDoubleMatrix3D(X.slices() + padSize[0], X.rows() + padSize[1], X.columns() + padSize[2]);
            Xpad.viewPart(padSize[0], padSize[1], padSize[2], X.slices(), X.rows(), X.columns()).assign(X);
            break;
        }
        return Xpad;
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
    
    
    
    
    
    
    
    
    
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    
    

    /**
     * Copies pixel values from image stack <code>stack</code> to matrix
     * <code>X</code>
     * 
     * @param stack
     *            image stack
     * @param X
     *            matrix
     */
    public static void assignPixelsToMatrix(final DeconvPixelsStack stack, final DoubleMatrix3D X) {
        int slices = X.slices();
        int np = ConcurrencyUtils.getNumberOfThreads();
        if ((np > 1) && (X.size() >= ConcurrencyUtils.getThreadsBeginN_3D())) {
            Future<?>[] futures = new Future[np];
            int k = slices / np;
            for (int j = 0; j < np; j++) {
                final int firstSlice = j * k;
                final int lastSlice = (j == np - 1) ? slices : firstSlice + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        for (int s = firstSlice; s < lastSlice; s++) {
                            ImageProcessor ip = stack.getProcessor(s + 1);
                            DoubleCommon2D.assignPixelsToMatrix(X.viewSlice(s), ip);
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            for (int s = 0; s < slices; s++) {
                ImageProcessor ip = stack.getProcessor(s + 1);
                DoubleCommon2D.assignPixelsToMatrix(X.viewSlice(s), ip);
            }
        }
    }

    /**
     * Copies pixel values from real matrix <code>X</code> to image stack
     * <code>stack</code>
     * 
     * @param stack
     *            image stack
     * @param X
     *            real matrix
     * @param cmY
     *            color model
     */
    public static void assignPixelsToStack(final DeconvPixelsStack stack, final DComplexMatrix3D X, final java.awt.image.ColorModel cmY) {
        final int slices = X.slices();
        final int rows = X.rows();
        final int cols = X.columns();
        for (int s = 0; s < slices; s++) {
            FloatProcessor ip = new FloatProcessor(cols, rows);
            DoubleCommon2D.assignPixelsToProcessor(ip, X.viewSlice(s), cmY);
            stack.addSlice(null, ip, s);
        }
    }

    /**
     * Copies pixel values from real matrix <code>X</code> to image stack
     * <code>stack</code>
     * 
     * @param stack
     *            image stack
     * @param X
     *            real matrix
     * @param cmY
     *            color model
     * @param threshold
     *            the smallest positive value assigned to the stack, all the
     *            values less than the threshold are set to zero
     * 
     */
    public static void assignPixelsToStack(final DeconvPixelsStack stack, final DComplexMatrix3D X, final java.awt.image.ColorModel cmY, final double threshold) {
        final int slices = X.slices();
        final int rows = X.rows();
        final int cols = X.columns();
        for (int s = 0; s < slices; s++) {
            FloatProcessor ip = new FloatProcessor(cols, rows);
            DoubleCommon2D.assignPixelsToProcessor(ip, X.viewSlice(s), cmY, threshold);
            stack.addSlice(null, ip, s);
        }
    }

    /**
     * Copies pixel values from real matrix <code>X</code> to image stack
     * <code>stack</code>
     * 
     * @param stack
     *            image stack
     * @param X
     *            real matrix
     * @param cmY
     *            color model
     */
    public static void assignPixelsToStack(final DeconvPixelsStack stack, final DoubleMatrix3D X, final java.awt.image.ColorModel cmY) {
        final int slices = X.slices();
        final int rows = X.rows();
        final int cols = X.columns();
        for (int s = 0; s < slices; s++) {
            FloatProcessor ip = new FloatProcessor(cols, rows);
            DoubleCommon2D.assignPixelsToProcessor(ip, X.viewSlice(s), cmY);
            stack.addSlice(null, ip, s);
        }
    }

    /**
     * Copies pixel values from real matrix <code>X</code> to image stack
     * <code>stack</code>
     * 
     * @param stack
     *            image stack
     * @param X
     *            real matrix
     * @param cmY
     *            color model
     * @param threshold
     *            the smallest positive value assigned to the stack, all the
     *            values less than the threshold are set to zero
     * 
     */
    public static void assignPixelsToStack(final DeconvPixelsStack stack, final DoubleMatrix3D X, final java.awt.image.ColorModel cmY, final double threshold) {
        final int slices = X.slices();
        final int rows = X.rows();
        final int cols = X.columns();
        for (int s = 0; s < slices; s++) {
            FloatProcessor ip = new FloatProcessor(cols, rows);
            DoubleCommon2D.assignPixelsToProcessor(ip, X.viewSlice(s), cmY, threshold);
            stack.addSlice(null, ip, s);
        }
    }

    /**
     * Copies pixel values from complex padded matrix <code>X</code> to image
     * stack <code>stack</code>
     * 
     * @param stack
     *            image stack
     * @param X
     *            padded real matrix
     * @param slices
     *            original number of slices
     * @param rows
     *            original number of rows
     * @param cols
     *            original number of columns
     * @param sOff
     *            slice offset
     * @param rOff
     *            row offset
     * @param cOff
     *            column offset
     * @param cmY
     *            color model
     */
    public static void assignPixelsToStackPadded(final DeconvPixelsStack stack, final DComplexMatrix3D X, final int slices, final int rows, final int cols, final int sOff, final int rOff, final int cOff, final java.awt.image.ColorModel cmY) {
        for (int s = 0; s < slices; s++) {
            FloatProcessor ip = new FloatProcessor(cols, rows);
            DoubleCommon2D.assignPixelsToProcessorPadded(ip, X.viewSlice(s + sOff), rows, cols, rOff, cOff, cmY);
            stack.addSlice(null, ip, s);
        }
    }

    /**
     * Copies pixel values from complex padded matrix <code>X</code> to image
     * stack <code>stack</code>
     * 
     * @param stack
     *            image stack
     * @param X
     *            padded real matrix
     * @param slices
     *            original number of slices
     * @param rows
     *            original number of rows
     * @param cols
     *            original number of columns
     * @param sOff
     *            slice offset
     * @param rOff
     *            row offset
     * @param cOff
     *            column offset
     * @param cmY
     *            color model
     * @param threshold
     *            the smallest positive value assigned to the stack, all the
     *            values less than the threshold are set to zero
     * 
     */
    public static void assignPixelsToStackPadded(final DeconvPixelsStack stack, final DComplexMatrix3D X, final int slices, final int rows, final int cols, final int sOff, final int rOff, final int cOff, final java.awt.image.ColorModel cmY, final double threshold) {
        for (int s = 0; s < slices; s++) {
            FloatProcessor ip = new FloatProcessor(cols, rows);
            DoubleCommon2D.assignPixelsToProcessorPadded(ip, X.viewSlice(s + sOff), rows, cols, rOff, cOff, cmY, threshold);
            stack.addSlice(null, ip, s);
        }
    }

    /**
     * Copies pixel values from real padded matrix <code>X</code> to image stack
     * <code>stack</code>
     * 
     * @param stack
     *            image stack
     * @param X
     *            padded real matrix
     * @param slices
     *            original number of slices
     * @param rows
     *            original number of rows
     * @param cols
     *            original number of columns
     * @param sOff
     *            slice offset
     * @param rOff
     *            row offset
     * @param cOff
     *            column offset
     * @param cmY
     *            color model
     */
    public static void assignPixelsToStackPadded(final DeconvPixelsStack stack, final DoubleMatrix3D X, final int slices, final int rows, final int cols, final int sOff, final int rOff, final int cOff, final java.awt.image.ColorModel cmY) {
        for (int s = 0; s < slices; s++) {
            FloatProcessor ip = new FloatProcessor(cols, rows);
            DoubleCommon2D.assignPixelsToProcessorPadded(ip, X.viewSlice(s + sOff), rows, cols, rOff, cOff, cmY);
            stack.addSlice(null, ip, s);
        }
    }

    /**
     * Copies pixel values from real padded matrix <code>X</code> to image stack
     * <code>stack</code>
     * 
     * @param stack
     *            image stack
     * @param X
     *            padded real matrix
     * @param slices
     *            original number of slices
     * @param rows
     *            original number of rows
     * @param cols
     *            original number of columns
     * @param sOff
     *            slice offset
     * @param rOff
     *            row offset
     * @param cOff
     *            column offset
     * @param cmY
     *            color model
     * @param threshold
     *            the smallest positive value assigned to the stack, all the
     *            values less than the threshold are set to zero
     * 
     */
    public static void assignPixelsToStackPadded(final DeconvPixelsStack stack, final DoubleMatrix3D X, final int slices, final int rows, final int cols, final int sOff, final int rOff, final int cOff, final java.awt.image.ColorModel cmY, final double threshold) {
        for (int s = 0; s < slices; s++) {
            FloatProcessor ip = new FloatProcessor(cols, rows);
            DoubleCommon2D.assignPixelsToProcessorPadded(ip, X.viewSlice(s + sOff), rows, cols, rOff, cOff, cmY, threshold);
            stack.addSlice(null, ip, s);
        }
    }

    /**
     * Creates filtered output for complex input <code>S</code>
     * 
     * @param S
     *            matrix to be filtered
     * @param regParam
     *            regularization parameter
     * @return filtered matrix
     */
    public static DComplexMatrix3D createFilter(final DComplexMatrix3D S, final double regParam) {
        final int slices = S.slices();
        final int rows = S.rows();
        final int cols = S.columns();
        DenseDComplexMatrix3D Sfilt = new DenseDComplexMatrix3D(slices, rows, cols);
        final double[] elemsSfilt = Sfilt.elements();
        int np = ConcurrencyUtils.getNumberOfThreads();
        if ((np > 1) && (S.size() >= ConcurrencyUtils.getThreadsBeginN_3D())) {
            Future<?>[] futures = new Future[np];
            int k = slices / np;
            for (int j = 0; j < np; j++) {
                final int firstSlice = j * k;
                final int lastSlice = (j == np - 1) ? slices : firstSlice + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        double elem[];
                        int idx = firstSlice * 2 * rows * cols;
                        for (int s = firstSlice; s < lastSlice; s++) {
                            for (int r = 0; r < rows; r++) {
                                for (int c = 0; c < cols; c++) {
                                    elem = S.getQuick(s, r, c);
                                    if (DComplex.abs(elem) >= regParam) {
                                        if (elem[1] != 0.0) {
                                            double tmp = (elem[0] * elem[0]) + (elem[1] * elem[1]);
                                            elem[0] = elem[0] / tmp;
                                            elem[1] = -elem[1] / tmp;
                                        } else {
                                            elem[0] = 1 / elem[0];
                                            elem[1] = 0;
                                        }
                                        elemsSfilt[idx] = elem[0];
                                        elemsSfilt[idx + 1] = elem[1];
                                    }
                                    idx += 2;
                                }
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            double elem[];
            int idx = 0;
            for (int s = 0; s < slices; s++) {
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < cols; c++) {
                        elem = S.getQuick(s, r, c);
                        if (DComplex.abs(elem) >= regParam) {
                            if (elem[1] != 0.0) {
                                double tmp = (elem[0] * elem[0]) + (elem[1] * elem[1]);
                                elem[0] = elem[0] / tmp;
                                elem[1] = -elem[1] / tmp;
                            } else {
                                elem[0] = 1 / elem[0];
                                elem[1] = 0;
                            }
                            elemsSfilt[idx] = elem[0];
                            elemsSfilt[idx + 1] = elem[1];
                        }
                        idx += 2;
                    }
                }
            }
        }
        return Sfilt;
    }

    /**
     * Creates filtered output for real input <code>S</code>
     * 
     * @param S
     *            matrix to be filtered
     * @param regParam
     *            regularization parameter
     * @return filtered matrix
     */
    public static DoubleMatrix3D createFilter(final DoubleMatrix3D S, final double regParam) {
        final int slices = S.slices();
        final int rows = S.rows();
        final int cols = S.columns();
        DenseDoubleMatrix3D Sfilt = new DenseDoubleMatrix3D(slices, rows, cols);
        final double[] elemsSfilt = Sfilt.elements();
        int np = ConcurrencyUtils.getNumberOfThreads();
        if ((np > 1) && (S.size() >= ConcurrencyUtils.getThreadsBeginN_3D())) {
            Future<?>[] futures = new Future[np];
            int k = slices / np;
            for (int j = 0; j < np; j++) {
                final int firstSlice = j * k;
                final int lastSlice = (j == np - 1) ? slices : firstSlice + k;
                futures[j] = ConcurrencyUtils.submit(new Runnable() {
                    public void run() {
                        int idx = firstSlice * rows * cols;
                        double elem;
                        for (int s = firstSlice; s < lastSlice; s++) {
                            for (int r = 0; r < rows; r++) {
                                for (int c = 0; c < cols; c++) {
                                    elem = S.getQuick(s, r, c);
                                    if (Math.abs(elem) >= regParam) {
                                        elemsSfilt[idx] = 1.0 / elem;
                                    }
                                    idx++;
                                }
                            }
                        }
                    }
                });
            }
            ConcurrencyUtils.waitForCompletion(futures);
        } else {
            double elem;
            int idx = 0;
            for (int s = 0; s < slices; s++) {
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < cols; c++) {
                        elem = S.getQuick(s, r, c);
                        if (Math.abs(elem) >= regParam) {
                            elemsSfilt[idx] = 1.0 / elem;
                        }
                        idx++;
                    }
                }
            }
        }
        return Sfilt;
    }

    /**
     * Computes the DCT shift of <code>PSF</code> matrix.
     * 
     * @param PSF
     *            matrix containing the point spread function.
     * @param center
     *            indices of center of <code>PSF</code>.
     * @return shifted matrix
     */
    public static DoubleMatrix3D dctShift(DoubleMatrix3D PSF, int[] center) {
        int slices = PSF.slices(); // must be > 1
        int rows = PSF.rows();
        int cols = PSF.columns();
        int cs = center[0];
        int cr = center[1];
        int cc = center[2];
        int k = Math.min(Math.min(Math.min(Math.min(Math.min(cs, slices - cs - 1), cr), rows - cr - 1), cc), cols - cc - 1);
        int fslice = cs - k;
        int lslice = cs + k;
        int sliceSize = lslice - fslice + 1;
        int frow = cr - k;
        int lrow = cr + k;
        int rowSize = lrow - frow + 1;
        int fcol = cc - k;
        int lcol = cc + k;
        int colSize = lcol - fcol + 1;

        DoubleMatrix3D PP = new DenseDoubleMatrix3D(sliceSize, rowSize, colSize);
        DoubleMatrix3D P1 = new DenseDoubleMatrix3D(sliceSize, rowSize, colSize);
        DoubleMatrix3D P2 = new DenseDoubleMatrix3D(sliceSize, rowSize, colSize);
        DoubleMatrix3D P3 = new DenseDoubleMatrix3D(sliceSize, rowSize, colSize);
        DoubleMatrix3D P4 = new DenseDoubleMatrix3D(sliceSize, rowSize, colSize);

        DoubleMatrix3D P5 = new DenseDoubleMatrix3D(sliceSize, rowSize, colSize);
        DoubleMatrix3D P6 = new DenseDoubleMatrix3D(sliceSize, rowSize, colSize);
        DoubleMatrix3D P7 = new DenseDoubleMatrix3D(sliceSize, rowSize, colSize);
        DoubleMatrix3D P8 = new DenseDoubleMatrix3D(sliceSize, rowSize, colSize);

        DoubleMatrix3D Ps = new DenseDoubleMatrix3D(slices, rows, cols);
        PP.assign(PSF.viewPart(fslice, frow, fcol, sliceSize, rowSize, colSize));

        P1.viewPart(0, 0, 0, sliceSize - cs + fslice, rowSize - cr + frow, colSize - cc + fcol).assign(PP.viewPart(cs - fslice, cr - frow, cc - fcol, sliceSize - cs + fslice, rowSize - cr + frow, colSize - cc + fcol));
        P2.viewPart(0, 0, 0, sliceSize - cs + fslice, rowSize - cr + frow, colSize - cc + fcol - 1).assign(PP.viewPart(cs - fslice, cr - frow, cc - fcol + 1, sliceSize - cs + fslice, rowSize - cr + frow, colSize - cc + fcol - 1));
        P3.viewPart(0, 0, 0, sliceSize - cs + fslice, rowSize - cr + frow - 1, colSize - cc + fcol).assign(PP.viewPart(cs - fslice, cr - frow + 1, cc - fcol, sliceSize - cs + fslice, rowSize - cr + frow - 1, colSize - cc + fcol));
        P4.viewPart(0, 0, 0, sliceSize - cs + fslice, rowSize - cr + frow - 1, colSize - cc + fcol - 1).assign(PP.viewPart(cs - fslice, cr - frow + 1, cc - fcol + 1, sliceSize - cs + fslice, rowSize - cr + frow - 1, colSize - cc + fcol - 1));

        P5.viewPart(0, 0, 0, sliceSize - cs + fslice - 1, rowSize - cr + frow, colSize - cc + fcol).assign(PP.viewPart(cs - fslice + 1, cr - frow, cc - fcol, sliceSize - cs + fslice - 1, rowSize - cr + frow, colSize - cc + fcol));
        P6.viewPart(0, 0, 0, sliceSize - cs + fslice - 1, rowSize - cr + frow, colSize - cc + fcol - 1).assign(PP.viewPart(cs - fslice + 1, cr - frow, cc - fcol + 1, sliceSize - cs + fslice - 1, rowSize - cr + frow, colSize - cc + fcol - 1));
        P7.viewPart(0, 0, 0, sliceSize - cs + fslice - 1, rowSize - cr + frow - 1, colSize - cc + fcol).assign(PP.viewPart(cs - fslice + 1, cr - frow + 1, cc - fcol, sliceSize - cs + fslice - 1, rowSize - cr + frow - 1, colSize - cc + fcol));
        P8.viewPart(0, 0, 0, sliceSize - cs + fslice - 1, rowSize - cr + frow - 1, colSize - cc + fcol - 1).assign(PP.viewPart(cs - fslice + 1, cr - frow + 1, cc - fcol + 1, sliceSize - cs + fslice - 1, rowSize - cr + frow - 1, colSize - cc + fcol - 1));

        P1.assign(P2, DoubleFunctions.plus);
        P1.assign(P3, DoubleFunctions.plus);
        P1.assign(P4, DoubleFunctions.plus);
        P1.assign(P5, DoubleFunctions.plus);
        P1.assign(P6, DoubleFunctions.plus);
        P1.assign(P7, DoubleFunctions.plus);
        P1.assign(P8, DoubleFunctions.plus);

        Ps.viewPart(0, 0, 0, sliceSize, rowSize, colSize).assign(P1);
        return Ps;

    }
    
}
