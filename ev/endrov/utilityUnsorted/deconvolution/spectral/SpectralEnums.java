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

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.DoubleMatrix3D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix3D;
import cern.colt.matrix.tfloat.FloatMatrix2D;
import cern.colt.matrix.tfloat.FloatMatrix3D;
import cern.colt.matrix.tfloat.impl.DenseFloatMatrix2D;
import cern.colt.matrix.tfloat.impl.DenseFloatMatrix3D;

/**
 * Enumerations used in spectral algorithms.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class SpectralEnums {

    public enum SpectralResizingType {
        NONE, NEXT_POWER_OF_TWO
    };

    public enum SpectralPaddingType {
        PERIODIC, REFLEXIVE
    };

    public enum MethodType {
        GTIK_REFLEXIVE, GTIK_PERIODIC, TIK_REFLEXIVE, TIK_PERIODIC, TSVD_REFLEXIVE, TSVD_PERIODIC
    };

    public enum DoubleStencil2DType {
        IDENTITY(new DenseDoubleMatrix2D(3, 3).assign(new double[] { 0, 0, 0, 0, 1, 0, 0, 0, 0 })), FIRST_DERIVATIVE_COLUMNS(new DenseDoubleMatrix2D(3, 3).assign(new double[] { 0, 1, 0, 0, -1, 0, 0, 0, 0 })), SECOND_DERIVATIVE_COLUMNS(new DenseDoubleMatrix2D(3, 3).assign(new double[] { 0, 1, 0, 0,
                -2, 0, 0, 1, 0 })), FIRST_DERIVATIVE_ROWS(new DenseDoubleMatrix2D(3, 3).assign(new double[] { 0, 0, 0, 1, -1, 0, 0, 0, 0 })), SECOND_DERIVATIVE_ROWS(new DenseDoubleMatrix2D(3, 3).assign(new double[] { 0, 0, 0, 1, -2, 1, 0, 0, 0 })), LAPLACIAN(new DenseDoubleMatrix2D(3, 3)
                .assign(new double[] { 0, 1, 0, 1, -4, 1, 0, 1, 0 }));

        public final DoubleMatrix2D stencil;

        private DoubleStencil2DType(DoubleMatrix2D stencil) {
            this.stencil = stencil;
        }
    };

    public enum FloatStencil2DType {
        IDENTITY(new DenseFloatMatrix2D(3, 3).assign(new float[] { 0, 0, 0, 0, 1, 0, 0, 0, 0 })), FIRST_DERIVATIVE_COLUMN(new DenseFloatMatrix2D(3, 3).assign(new float[] { 0, 1, 0, 0, -1, 0, 0, 0, 0 })), SECOND_DERIVATIVE_COLUMNS(new DenseFloatMatrix2D(3, 3).assign(new float[] { 0, 1, 0, 0, -2, 0,
                0, 1, 0 })), FIRST_DERIVATIVE_ROWS(new DenseFloatMatrix2D(3, 3).assign(new float[] { 0, 0, 0, 1, -1, 0, 0, 0, 0 })), SECOND_DERIVATIVE_ROWS(new DenseFloatMatrix2D(3, 3).assign(new float[] { 0, 0, 0, 1, -2, 1, 0, 0, 0 })), LAPLACIAN(new DenseFloatMatrix2D(3, 3).assign(new float[] {
                0, 1, 0, 1, -4, 1, 0, 1, 0 }));

        public final FloatMatrix2D stencil;

        private FloatStencil2DType(FloatMatrix2D stencil) {
            this.stencil = stencil;
        }
    };

    public enum DoubleStencil3DType {
        IDENTITY(new DenseDoubleMatrix3D(3, 3, 3).assign(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 })), FIRST_DRIVATIVE_SLICES(new DenseDoubleMatrix3D(3, 3, 3).assign(new double[] { 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0 })), SECOND_DERIVATIVE_SLICES(new DenseDoubleMatrix3D(3, 3, 3).assign(new double[] { 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, -2, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0 })), FIRST_DERIVATIVE_ROWS(new DenseDoubleMatrix3D(3, 3, 3).assign(new double[] { 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 })), SECOND_DERIVATIVE_ROWS(new DenseDoubleMatrix3D(3, 3, 3).assign(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 })), FIRST_DERIVATIVE_COLUMNS(new DenseDoubleMatrix3D(3,
                3, 3).assign(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 })), SECOND_DERIVATIVE_COLUMNS(new DenseDoubleMatrix3D(3, 3, 3).assign(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, -2, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 })), LAPLACIAN(
                new DenseDoubleMatrix3D(3, 3, 3).assign(new double[] { 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, -6, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0 }));

        public final DoubleMatrix3D stencil;

        private DoubleStencil3DType(DoubleMatrix3D stencil) {
            this.stencil = stencil;
        }
    };

    public enum FloatStencil3DType {
        IDENTITY(new DenseFloatMatrix3D(3, 3, 3).assign(new float[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 })), FIRST_DERIVATIVE_SLICES(new DenseFloatMatrix3D(3, 3, 3).assign(new float[] { 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0 })), SECOND_DERIVATIVE_SLICES(new DenseFloatMatrix3D(3, 3, 3).assign(new float[] { 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, -2, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0 })), FIRST_DERIVATIVE_ROWS(new DenseFloatMatrix3D(3, 3, 3).assign(new float[] { 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 })), SECOND_DERIVATIVE_ROWS(new DenseFloatMatrix3D(3, 3, 3).assign(new float[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 })), FIRST_DERIVATIVE_COLUMNS(new DenseFloatMatrix3D(3, 3, 3)
                .assign(new float[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 })), SECOND_DERIVATIVE_COLUMNS(new DenseFloatMatrix3D(3, 3, 3).assign(new float[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, -2, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 })), LAPLACIAN(
                new DenseFloatMatrix3D(3, 3, 3).assign(new float[] { 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, -6, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0 }));

        public final FloatMatrix3D stencil;

        private FloatStencil3DType(FloatMatrix3D stencil) {
            this.stencil = stencil;
        }
    };
}
