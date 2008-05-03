#include <math.h>
#include <malloc.h>
#include "mex.h"
//#include <memory.h>


//%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//% author: Keith Forbes     %
//% e-mail: keith@umpire.com %
//% tel: +27 21 674 3345     %
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%

//updated 22/9/2000 to output in 0 to 1 range instead of 0 to 255 range

static void palette(
		   double	img[],
		   double	imgrgb[],
		   unsigned int m,
		   unsigned int n
		   )
{
	unsigned int r;
	unsigned int c;

for (r=0;r<m;r++)
	for (c=0;c<n;c++)
	{
		if (img[r+c*m]==1.0) //red
		{
			imgrgb[r+c*m+0*(m*n)]=255.0/255.0;
			imgrgb[r+c*m+1*(m*n)]=0.0/255.0;
			imgrgb[r+c*m+2*(m*n)]=0.0/255.0;
		}

		if (img[r+c*m]==2.0) //green
		{
			imgrgb[r+c*m+0*(m*n)]=0.0/255.0;
			imgrgb[r+c*m+1*(m*n)]=255.0/255.0;
			imgrgb[r+c*m+2*(m*n)]=0.0/255.0;
		}

		if (img[r+c*m]==3.0) //blue
		{
			imgrgb[r+c*m+0*(m*n)]=0.0/255.0;
			imgrgb[r+c*m+1*(m*n)]=0.0/255.0;
			imgrgb[r+c*m+2*(m*n)]=255.0/255.0;
		}

		if (img[r+c*m]==4.0) //yellow
		{
			imgrgb[r+c*m+0*(m*n)]=255.0/255.0;
			imgrgb[r+c*m+1*(m*n)]=255.0/255.0;
			imgrgb[r+c*m+2*(m*n)]=0.0/255.0;
		}

		if (img[r+c*m]==5.0) //cyan
		{
			imgrgb[r+c*m+0*(m*n)]=0.0/255.0;
			imgrgb[r+c*m+1*(m*n)]=255.0/255.0;
			imgrgb[r+c*m+2*(m*n)]=255.0/255.0;
		}

		if (img[r+c*m]==6.0) //magenta
		{
			imgrgb[r+c*m+0*(m*n)]=255.0/255.0;
			imgrgb[r+c*m+1*(m*n)]=0.0/255.0;
			imgrgb[r+c*m+2*(m*n)]=255.0/255.0;
		}

		if (img[r+c*m]==7.0) //grey
		{
			imgrgb[r+c*m+0*(m*n)]=128.0/255.0;
			imgrgb[r+c*m+1*(m*n)]=128.0/255.0;
			imgrgb[r+c*m+2*(m*n)]=128.0/255.0;
		}

		if (img[r+c*m]==8.0) //orange
		{
			imgrgb[r+c*m+0*(m*n)]=255.0/255.0;
			imgrgb[r+c*m+1*(m*n)]=128.0/255.0;
			imgrgb[r+c*m+2*(m*n)]=0.0/255.0;
		}

		if (img[r+c*m]==9.0) //pink
		{
			imgrgb[r+c*m+0*(m*n)]=255.0/255.0;
			imgrgb[r+c*m+1*(m*n)]=114.0/255.0;
			imgrgb[r+c*m+2*(m*n)]=200.0/255.0;
		}

		if (img[r+c*m]==10.0) //brown
		{
			imgrgb[r+c*m+0*(m*n)]=110.0/255.0;
			imgrgb[r+c*m+1*(m*n)]=50.0/255.0;
			imgrgb[r+c*m+2*(m*n)]=50.0/255.0;
		}

		if (img[r+c*m]==11.0) //purple
		{
			imgrgb[r+c*m+0*(m*n)]=155.0/255.0;
			imgrgb[r+c*m+1*(m*n)]=0.0/255.0;
			imgrgb[r+c*m+2*(m*n)]=200.0/255.0;
		}

		if (img[r+c*m]==12.0) //dark blue
		{
			imgrgb[r+c*m+0*(m*n)]=0.0/255.0;
			imgrgb[r+c*m+1*(m*n)]=0.0/255.0;
			imgrgb[r+c*m+2*(m*n)]=110.0/255.0;
		}

		if (img[r+c*m]==13.0) //dark green
		{
			imgrgb[r+c*m+0*(m*n)]=0.0/255.0;
			imgrgb[r+c*m+1*(m*n)]=110.0/255.0;
			imgrgb[r+c*m+2*(m*n)]=0.0/255.0;
		}

		if (img[r+c*m]==14.0) //dark red
		{
			imgrgb[r+c*m+0*(m*n)]=110.0/255.0;
			imgrgb[r+c*m+1*(m*n)]=0.0/255.0;
			imgrgb[r+c*m+2*(m*n)]=0.0/255.0;
		}

		if (img[r+c*m]==15.0) //mustard
		{
			imgrgb[r+c*m+0*(m*n)]=255.0/255.0;
			imgrgb[r+c*m+1*(m*n)]=190.0/255.0;
			imgrgb[r+c*m+2*(m*n)]=0.0/255.0;
		}

		if (img[r+c*m]==16.0) //light grey
		{
			imgrgb[r+c*m+0*(m*n)]=180.0/255.0;
			imgrgb[r+c*m+1*(m*n)]=180.0/255.0;
			imgrgb[r+c*m+2*(m*n)]=180.0/255.0;
		}

		
		if (img[r+c*m]==17.0) //mauve
		{
			imgrgb[r+c*m+0*(m*n)]=200.0/255.0;
			imgrgb[r+c*m+1*(m*n)]=200.0/255.0;
			imgrgb[r+c*m+2*(m*n)]=255.0/255.0;
		}

		
		if (img[r+c*m]==18.0) //sea green
		{
			imgrgb[r+c*m+0*(m*n)]=51.0/255.0;
			imgrgb[r+c*m+1*(m*n)]=200.0/255.0;
			imgrgb[r+c*m+2*(m*n)]=150.0/255.0;
		}


		if (img[r+c*m]>=19.0) //white
		{
			imgrgb[r+c*m+0*(m*n)]=255.0/255.0;
			imgrgb[r+c*m+1*(m*n)]=255.0/255.0;
			imgrgb[r+c*m+2*(m*n)]=255.0/255.0;
		}
	}
		   		
}


void mexFunction( int nlhs, mxArray *plhs[], 
		  int nrhs, const mxArray*prhs[] )
     
{ 
    double *img, *imgrgb; 
	unsigned int m,n; 
	int dims[3];
    
	

	if (mxGetNumberOfDimensions(prhs[0])==3)
		mexErrMsgTxt("Not in tended for 3 band RGB images - only single band images."); 


	if (mxGetNumberOfDimensions(prhs[0])>3)
		mexErrMsgTxt("Not in tended for high dimensional data"); 


    if (nrhs != 1) { 
	mexErrMsgTxt("One input arguments required."); 
    } else if (nlhs > 1) {
	mexErrMsgTxt("Too many output arguments."); 
    } 
    
    
    m = mxGetM(prhs[0]); 
    n = mxGetN(prhs[0]);
    
	

    
    img = mxGetPr(prhs[0]); 
    
    dims[0]=m;
	dims[1]=n;
	dims[2]=3;

	//printf("m %d n %d",m,n);

    plhs[0] = mxCreateNumericArray(3,dims,mxDOUBLE_CLASS, mxREAL); 
	
	imgrgb=mxGetPr(plhs[0]);

	palette(img,imgrgb,m,n);
	
	
	return;
    
}


