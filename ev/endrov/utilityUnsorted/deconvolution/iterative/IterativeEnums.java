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
package endrov.utilityUnsorted.deconvolution.iterative;

/**
 * Enumerations used in iterative algorithms.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class IterativeEnums {

    public enum PaddingType {
        BOTH, POST, PRE
    }

    public enum ResizingType {
        AUTO, MINIMAL, NEXT_POWER_OF_TWO
    }

    public enum BoundaryType {
        REFLEXIVE, PERIODIC, ZERO
    }

    public enum MethodType {
        MRNSD, WPL, CGLS, HyBR
    };

    public enum PSFType {
        INVARIANT, VARIANT
    }

    public enum PreconditionerType {
        FFT, NONE
    }

}
