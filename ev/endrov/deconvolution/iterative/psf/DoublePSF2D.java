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

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import endrov.imageset.EvPixels;

/**
 * This class keeps information about 2D PSF images.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class DoublePSF2D {
    private DoubleMatrix2D[][] image;

    private int[][][] center;

    
    
    
    /**
     * Creates a new instance of DoublePSF2D.
     * 
     * @param imPSF
     *            array of PSF images
     */
    public DoublePSF2D(EvPixels[][] imPSF) {
        image = new DoubleMatrix2D[imPSF.length][imPSF[0].length];
        center = new int[imPSF.length][imPSF[0].length][2];

        for (int i = 0; i < imPSF.length; i++) {
            for (int j = 0; j < imPSF[0].length; j++) {
                EvPixels ipPSF = imPSF[i][j];
                image[i][j] = new DenseDoubleMatrix2D(ipPSF.getHeight(), ipPSF.getWidth());
                image[i][j].assign(ipPSF.convertToDouble(true).getArrayFloat());
                double[] tmp = image[i][j].getMaxLocation();
                center[i][j][0] = (int) tmp[1];
                center[i][j][1] = (int) tmp[2];
            }
        }
    }
    

    /**
     * Returns centers of all PSFs.
     * 
     * @return centers of all PSFs
     */
    public int[][][] getCenter() {
        return center;
    }

    /**
     * Returns all images that represent PSFs.
     * 
     * @return all images that represent PSFs
     */
    public DoubleMatrix2D[][] getImage() {
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
        int[] size = new int[2];
        if (image.length > 1 || image[0].length > 1) {
            int maxRowSize = image[0][0].rows();
            int maxColSize = image[0][0].columns();
            for (int i = 0; i < image.length; i++) {
                for (int j = 0; j < image[0].length; j++) {
                    if (image[i][j].rows() > maxRowSize) {
                        maxRowSize = image[i][j].rows();
                    }
                    if (image[i][j].columns() > maxColSize) {
                        maxColSize = image[i][j].columns();
                    }
                }
            }
            size[0] = maxRowSize;
            size[1] = maxColSize;
        } else {
            size[0] = image[0][0].rows();
            size[1] = image[0][0].columns();
        }
        return size;
    }

}
