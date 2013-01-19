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


/**
 * Interface for spectral deconvolver.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.col)
 * 
 */
public interface DoubleSpectralDeconvolver {


    /**
     * Updates deconvolved image <code>imX</code> with new regularization
     * parameter <code>regParam</code>.
     * 
     * @param regParam
     *            regularization parameter
     * @param threshold
     *            the smallest positive value assigned to the restored image
     * @param imX
     *            deconvolved image
     */
    //public void update(double regParam, double threshold, ImagePlus imX);

}
