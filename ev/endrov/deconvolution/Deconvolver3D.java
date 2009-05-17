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

import java.util.Iterator;

import endrov.ev.Log;
import endrov.imageset.EvImage;
import endrov.imageset.EvStack;
import endrov.unsortedImageFilters.newcore.StackOp;
import endrov.util.EvDecimal;

/**
 * Deconvolver 2D.
 * 
 * @author Johan Henriksson
 * 
 */
public abstract class Deconvolver3D extends StackOp
	{
	
	public static void log(String s)
		{
		Log.printLog(s);
		}

	
	 protected abstract DeconvPixelsStack internalDeconvolve(EvStack s);
   
	 /*
   public EvStack deconvolve(EvStack imB)
   	{
   	DeconvPixelsStack d=internalDeconvolve();
   	
   	EvStack s=new EvStack();
   	s.getMetaFrom(imB);
   	Iterator<EvDecimal> itz=imB.keySet().iterator();
   	for(int i=0;i<d.p.size();i++)
   		{
   		EvImage im=new EvImage();
   		im.setPixelsReference(d.p.get(i));
   		EvDecimal z=itz.next();
   		s.put(z, im);
   		}
   	
   	return s;
   	}
*/

  /**
   * The only argument is the image to deconvolve
   */
	public EvStack exec(EvStack... p)
		{
		EvStack imB=p[0];
		
		DeconvPixelsStack d=internalDeconvolve(imB);
   	
   	EvStack s=new EvStack();
   	s.getMetaFrom(imB);
   	Iterator<EvDecimal> itz=imB.keySet().iterator();
   	for(int i=0;i<d.p.size();i++)
   		{
   		EvImage im=new EvImage();
   		im.setPixelsReference(d.p.get(i));
   		EvDecimal z=itz.next();
   		s.put(z, im);
   		}
   	
   	return s;
		
		}
   
   
   
   
	}
