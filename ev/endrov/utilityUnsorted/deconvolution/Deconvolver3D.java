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
package endrov.utilityUnsorted.deconvolution;

import endrov.core.log.EvLog;
import endrov.flow.EvOpStack1;
import endrov.typeImageset.EvImagePlane;
import endrov.typeImageset.EvStack;
import endrov.util.ProgressHandle;

/**
 * Deconvolver 2D.
 * 
 * @author Johan Henriksson
 * 
 */
public abstract class Deconvolver3D extends EvOpStack1
	{
	
	public static void log(String s)
		{
		EvLog.printLog(s);
		}

	/**
	 * At the moment, the returned stack is rather fucked up. has to
	 * be fixed before it is returned, done so by exec
	 */
	 protected abstract DeconvPixelsStack internalDeconvolve(EvStack s);

  /**
   * The only argument is the image to deconvolve
   */
	public EvStack exec1(ProgressHandle ph, EvStack... p)
		{
		EvStack imB=p[0];
		
		DeconvPixelsStack d=internalDeconvolve(imB);
   	
   	EvStack s=new EvStack();
   	s.copyMetaFrom(imB);
   	//Iterator<EvDecimal> itz=imB.keySet().iterator();
   	for(int i=0;i<d.p.size();i++)
   		{
   		EvImagePlane im=new EvImagePlane();
   		im.setPixelsReference(d.p.get(i));
   		//EvDecimal z=itz.next();
   		s.putPlane(i, im);
   		}
   	
   	return s;
		
		}
   
   
   
   
	}
