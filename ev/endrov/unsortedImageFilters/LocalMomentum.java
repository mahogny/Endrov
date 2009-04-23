package endrov.unsortedImageFilters;

import endrov.imageset.EvPixels;

/**
 * Local momentum. Can be used to find local orientation.
 * 
 * From the theory of angular momentum:
 * 
 * Iij=sum (i - Ei) (j - Ej) rho(x,y)
 *
 * Angular momentum matrix:
 * I=[Ixx Ixy]
 *   [Ixy Iyy]
 *   
 * By diagonalization of matrix, principal directions are found.
 * Ovality is lambda1/lambda2 as sorted eigenvalues.
 *
 *
 *
 * Alternative method to find orientation: Divide window into 4 sub-windows. 
 * p'x=right windows-left windows
 * p'y=bottom windows-top windows
 * |p'x|-|p'y| gives an idea of ovality. better mathematical treatment probably possible
 *   
 **/
public class LocalMomentum
	{

	
	
	
	
	
	/**
	 * Calculation done by decomposition:
	 * Iij=sum ij rho(x,y) - (Ei+Ej) sum rho + Ei Ej sum rho 
	 * Ei=sum i rho(x,y)
	 * These operations can be done efficiently using cumsums and image math, independent of window size 
	 * 
	 * O(w*h)
	 * 
	 * @return Ixx, Iyy, Ixy
	 * 
	 */
	/*public static Tuple<EvPixels,Tuple<EvPixels, EvPixels>> localMomentum(EvPixels p, int kw, int kh)
		{
		
		
		
		}*/

	/**
	 * Return an image of given size
	 * 1 2 3
	 * 1 2 3
	 * 1 2 3
	 */
	public static EvPixels genIncX(int w, int h)
		{
		EvPixels p=new EvPixels(EvPixels.TYPE_INT,w,h);
		int[] aPixels=p.getArrayInt();
		for(int y=0;y<h;y++)
			{
			int base=y*w;
			for(int x=0;x<w;x++)
				aPixels[base+x]=x;
			}
		return p;
		}
	
	/**
	 * Return an image of given size:
	 * 1 1 1
	 * 2 2 2
	 * 3 3 3
	 */
	public static EvPixels genIncY(int w, int h)
		{
		EvPixels p=new EvPixels(EvPixels.TYPE_INT,w,h);
		int[] aPixels=p.getArrayInt();
		for(int y=0;y<h;y++)
			{
			int base=y*w;
			for(int x=0;x<w;x++)
				aPixels[base+x]=y;
			}
		return p;
		}
	
	
	}
