package endrov.unsortedImageFilters;


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
	
	//Use functions in imagegenerator
	
	}
