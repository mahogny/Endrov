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

package endrov.deconvolution.iterative.preconditioner;

import cern.colt.matrix.AbstractMatrix3D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix3D;

/**
 * Interface for a 3D preconditioner.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public interface DoublePreconditioner3D {

    /**
     * Computes left matrix division: P \ b or P' \ b.
     * 
     * @param b
     *            matrix
     * @param transpose
     *            if true then P' \ b is computed
     * @return solution
     */
    public DoubleMatrix1D solve(DoubleMatrix1D b, boolean transpose);

    /**
     * Computes left matrix division: P \ B or P' \ B.
     * 
     * @param B
     *            matrix
     * @param transpose
     *            if true then P' \ B is computed
     * @return solution
     */
    public DoubleMatrix3D solve(AbstractMatrix3D B, boolean transpose);

    /**
     * Returns the tolerance for a preconditioner.
     * 
     * @return the tolerance for a preconditioner
     */
    public double getTolerance();

}
