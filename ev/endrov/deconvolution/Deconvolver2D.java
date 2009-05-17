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
package endrov.deconvolution;

import endrov.ev.Log;
import endrov.imageset.EvPixels;
import endrov.unsortedImageFilters.newcore.SliceOp;

/**
 * Deconvolver 2D.
 * 
 * @author Johan Henriksson
 * 
 */
public abstract class Deconvolver2D extends SliceOp
	{
	
	public static void log(String s)
		{
		Log.printLog(s);
		}
	
	public abstract EvPixels internalDeconvolve(EvPixels ipB);

	
	/**
	 * Only one argument: the image to deconvolve
	 */
	public EvPixels exec(EvPixels... p)
		{
		EvPixels out=internalDeconvolve(p[0]);
		return out;
		}
	
	
	}
