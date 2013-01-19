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

package endrov.utilityUnsorted.deconvolution.iterative.psf;

import cern.colt.matrix.tdouble.DoubleMatrix3D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix3D;
import endrov.typeImageset.EvStack;
import endrov.utilityUnsorted.deconvolution.iterative.DoubleCommon3D;

/**
 * This class keeps information about 3D PSF images.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class DoublePSF3D {
    private DoubleMatrix3D[][][] image;

    private int[][][][] center;

    /**
     * Creates a new instance of DoublePSF3D.
     * 
     * @param imPSF
     *            array of PSF images
     */
    public DoublePSF3D(EvStack[][][] imPSF) {
        image = new DoubleMatrix3D[imPSF.length][imPSF[0].length][imPSF[0][0].length];
        center = new int[imPSF.length][imPSF[0].length][imPSF[0][0].length][3];

        for (int i = 0; i < imPSF.length; i++) {
            for (int j = 0; j < imPSF[0].length; j++) {
                for (int k = 0; k < imPSF[0][0].length; k++) {
                    EvStack isPSF = imPSF[i][j][k];
                    image[i][j][k] = new DenseDoubleMatrix3D(isPSF.getDepth(), isPSF.getHeight(), isPSF.getWidth());
                    DoubleCommon3D.assignPixelsToMatrix(isPSF, image[i][j][k]);
                    double[] tmp = image[i][j][k].getMaxLocation();
                    center[i][j][k][0] = (int) tmp[1];
                    center[i][j][k][1] = (int) tmp[2];
                    center[i][j][k][2] = (int) tmp[3];
                }
            }
        }
    }

    /**
     * Returns centers of all PSFs.
     * 
     * @return centers of all PSFs
     */
    public int[][][][] getCenter() {
        return center;
    }

    /**
     * Returns all images that represent PSFs.
     * 
     * @return all images that represent PSFs
     */
    public DoubleMatrix3D[][][] getImage() {
        return image;
    }

    /**
     * Returns the number of PSFs.
     * 
     * @return the number of PSFs
     */
    public int getNumberOfImages() {
        return image.length;
    }

    /**
     * Returns the max size of all the PSFs.
     * 
     * @return the max size of all the PSFs
     */
    public int[] getSize() {
        int[] size = new int[3];
        if (image.length > 1 || image[0].length > 1 || image[0][0].length > 1) {
            int maxSliceSize = image[0][0][0].slices();
            int maxRowSize = image[0][0][0].rows();
            int maxColSize = image[0][0][0].columns();
            for (int i = 0; i < image.length; i++) {
                for (int j = 0; j < image[0].length; j++) {
                    for (int k = 0; k < image[0][0].length; k++) {
                        if (image[i][j][k].slices() > maxSliceSize) {
                            maxSliceSize = image[i][j][k].slices();
                        }
                        if (image[i][j][k].rows() > maxRowSize) {
                            maxRowSize = image[i][j][k].rows();
                        }
                        if (image[i][j][k].columns() > maxColSize) {
                            maxColSize = image[i][j][k].columns();
                        }
                    }
                }
            }
            size[0] = maxSliceSize;
            size[1] = maxRowSize;
            size[2] = maxColSize;
        } else {
            size[0] = image[0][0][0].slices();
            size[1] = image[0][0][0].rows();
            size[2] = image[0][0][0].columns();
        }
        return size;
    }

}
