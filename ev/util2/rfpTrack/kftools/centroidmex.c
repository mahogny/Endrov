#include <math.h>
#include "mex.h"

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//% author: Keith Forbes     %
//% e-mail: keith@umpire.com %
//% tel: +27 21 674 3345     %
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%


static void centroid(
		   double	cpreal[],
		   double	cpimag[],		   
		   double	img[],
		   unsigned int ymax,
		   unsigned int xmax
 		   )
{
    unsigned int x;
	unsigned int y;
	double denominator=0;
	double xnumerator=0;
	double ynumerator=0;


	for ( x=0; x<xmax; x++)
		for ( y=0; y<ymax; y++)
		{
			ynumerator+=((double)y+1.0)*img[y+x*ymax];
			xnumerator+=((double)x+1.0)*img[y+x*ymax];
			denominator+=img[y+x*ymax];
			
		}


	cpreal[0]=xnumerator/denominator;
	cpimag[0]=ynumerator/denominator;
		
}

void mexFunction( int nlhs, mxArray *plhs[], 
		  int nrhs, const mxArray*prhs[] )
     
{ 
    double *cpreal;
	double *cpimag;	
    double *img; 
    unsigned int m,n; 
    
    /* Check for proper number of arguments */
    
    if (nrhs != 1)  
	{
		mexErrMsgTxt("One input arguments required."); 
	}
	else 
	{
		if (nlhs > 1) 
			mexErrMsgTxt("Too many output arguments."); 
	}
	
	if (!mxIsClass(prhs[0],"double"))
		mexErrMsgTxt("input image must be of type double (use im2double or double)");
	
	if (mxGetNumberOfDimensions(prhs[0])==3)
		mexErrMsgTxt("Not in tended for 3 band RGB images - only single band images."); 
	
	
	if (mxGetNumberOfDimensions(prhs[0])>3)
		mexErrMsgTxt("Not in tended for high dimensional data"); 
	
	
	
	m = mxGetM(prhs[0]); 
	n = mxGetN(prhs[0]);
	
	
    /* Create a matrix for the return argument */ 
    plhs[0] = mxCreateDoubleMatrix(1, 1, mxCOMPLEX); 
    
    /* Assign pointers to the various parameters */ 
    cpreal = mxGetPr(plhs[0]);
	cpimag = mxGetPi(plhs[0]);

    
    img = mxGetPr(prhs[0]); 
        
    /* Do the actual computations in a subroutine */
    centroid(cpreal,cpimag,img,m,n); 
    
    return;
    
}


