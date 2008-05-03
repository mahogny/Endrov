#include <math.h>
#include <malloc.h>
#include "mex.h"


// returns the polygon set of points for a polygon in a binary image

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//% author: Keith Forbes     %
//% e-mail: keith@umpire.com %
//% tel: +27 21 674 3345     %
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%

// 16 Sept 2002: add check for type double
//updated 22 September 2000 to use zero and nonzero as background and foreground respectively.
// foreground is 4-connected
// background is 8-connected

#define STARTSPACE 999 //initial space allocation in bytes

int inim(
		 unsigned int x,
		 unsigned int y,
		 unsigned int xmax,
		 unsigned int ymax
		 )
{
	return ((x>=0)&&(x<xmax))&&((y>=0)&&(y<ymax));
}



static void getboundary(
						double	*pytemp[],		   
						double	*pxtemp[],		   		   
						unsigned int *plength,
						unsigned int *pmax,
						double	img[],
						unsigned int ymax,
						unsigned int xmax
						)
{
	enum Direction
	{
			up,
			down,
			left,
			right
	}direct;
	enum Direction newdirect;
    double x;
	double y;
	double xstart;
	double ystart;	
	double newx;
	double newy;
	int ne,nw,se,sw;
	double *temp;
	int foundStart=0;
	unsigned int loop=0;
	long count=0;
	int canMove[4];
	
	
	x=-0.5;
	y=-1.5;
	
	
	while (foundStart==0)
	{
		
		
		y+=1.0;
		
		
		if (y>ymax)
		{
			y=-0.5;
			x+=1.0;
		}
		
		if (x>=xmax)
			mexErrMsgTxt("Could not find pixel inside object for starting point. Must be binary (b&w) image with single 4-connected object and at least one pixel 4-connected to foreground pixels."); 
		

		if (inim((unsigned int)ceil(x),(unsigned int)ceil(y),xmax,ymax))
			se=(img[(int)(ceil(y)+ceil(x)*ymax)]!=0);
		else
			se=0;

		if (se) 
			foundStart=1;
		
	}
	
	
	
	direct=up;
	xstart=x;
	ystart=y;
	
	//Found starting point - now the main loop:
	
	
	do
	{
		count++;
		if (count==(long)((xmax+2)*(ymax+2)))
			mexErrMsgTxt("Ambiguous case - could not locate boundary.Ensure that \
			image contains only one four-connected object (A foreground of ones and background of zeros)."); 
		
		
		if (inim((unsigned int)ceil(x),(unsigned int)floor(y),xmax,ymax))
			ne=(img[(int)(floor(y)+ceil(x)*ymax)]!=0);
		else
			ne=0;

		if (inim((unsigned int)floor(x),(unsigned int)floor(y),xmax,ymax))
			nw=(img[(int)(floor(y)+floor(x)*ymax)]!=0);
		else
			nw=0;

		if (inim((unsigned int)ceil(x),(unsigned int)ceil(y),xmax,ymax))
			se=(img[(int)(ceil(y)+ceil(x)*ymax)]!=0);
		else
			se=0;

		if (inim((unsigned int)floor(x),(unsigned int)ceil(y),xmax,ymax))
			sw=(img[(int)(ceil(y)+floor(x)*ymax)]!=0);
		else
			sw=0;


		canMove[up]=0;
		canMove[down]=0;
		canMove[left]=0;
		canMove[right]=0;
		
		
		if ((direct!=right)&&(nw!=sw)) //can we move left?
			canMove[left]=1;
		
		
		if ((direct!=left)&&(ne!=se)) //can we move right?
			canMove[right]=1;
		
		
		if ((direct!=up)&&(se!=sw)) //can we move down?
			canMove[down]=1;
		
		
		
		if ((direct!=down)&&(ne!=nw)) ////can we move up?
			canMove[up]=1;
		
		
		
		
		if (canMove[up]+canMove[down]+canMove[left]+canMove[right]==1) // can only move in one direction
		{
			if (canMove[left])
			{
				newx=x-1.0;
				newy=y;
				newdirect=left;
			}
			
			if (canMove[right])
			{
				newx=x+1.0;
				newy=y;
				newdirect=right;
			}
			
			if (canMove[down])
			{
				newx=x;
				newy=y+1.0;
				newdirect=down;
			}
						
			
			if (canMove[up])
			{
				newx=x;
				newy=y-1.0;
				newdirect=up;
			}
						
		}
		else //there is more than one direction to go - choose the one that continues in the 4-connected sense
		{
			if ((direct==up)&&(sw))
			{
				newx=x-1.0;
				newy=y;
				newdirect=left;
			}

			if ((direct==up)&&(se))
			{
				newx=x+1.0;
				newy=y;
				newdirect=right;
			}

			if ((direct==down)&&(nw))
			{
				newx=x-1.0;
				newy=y;
				newdirect=left;
			}

			if ((direct==down)&&(ne))
			{
				newx=x+1.0;
				newy=y;
				newdirect=right;
			}

			if ((direct==up)&&(sw))
			{
				newx=x-1.0;
				newy=y;
				newdirect=left;
			}

			if ((direct==right)&&(sw))
			{
				newx=x;
				newy=y+1.0;
				newdirect=down;
			}

			if ((direct==right)&&(nw))
			{
				newx=x;
				newy=y-1.0;
				newdirect=up;
			}

			if ((direct==left)&&(se))
			{
				newx=x;
				newy=y+1.0;
				newdirect=down;
			}

			if ((direct==left)&&(ne))
			{
				newx=x;
				newy=y-1.0;
				newdirect=up;
			}



		}

		if (loop==(*pmax)) //Time to allocate some more memory!
		{
			(*pmax)=(*pmax)*2; //double available space
			temp = malloc(sizeof(double)*(*pmax)); //create some new space
			if (temp==NULL)
				mexErrMsgTxt("Out of memory in getboundary"); 
			
			memcpy(temp,*pxtemp,(loop)*sizeof(double)); //copy existing data across
			free(*pxtemp); //free up old data
			*pxtemp=temp; //point to new array
			temp = malloc(sizeof(double)*(*pmax)); //create some new space
			if (temp==NULL)
				mexErrMsgTxt("Out of memory in getboundary"); 
			
			memcpy(temp,*pytemp,(loop)*sizeof(double)); //copy existing data across
			free(*pytemp); //free up old data
			*pytemp=temp; //point to new array
		}
		
		(*pxtemp)[loop]=x+1.0; //the extra one is because MATLAB has image origin at (1,1) and C uses (0,0)
		(*pytemp)[loop]=y+1.0;
		x=newx;
		y=newy;
		direct=newdirect;
		
		loop++;
	}
	while((x!=xstart)||(y!=ystart));
	
	
	
	*plength=loop;
	
	
}

void mexFunction( int nlhs, mxArray *plhs[], 
				 int nrhs, const mxArray*prhs[] )
				 
{ 
    double *pytemp;
	double *pxtemp;
	double *pout;
	double *img; 
	unsigned int plength, pmax;
    unsigned int m,n; 
    
	pmax=STARTSPACE;
	pytemp = malloc(sizeof(double)*pmax); //create a new space
	pxtemp = malloc(sizeof(double)*pmax); //create a new space
    
    /* Check for proper number of arguments */
    
    if (nrhs != 1) { 
		mexErrMsgTxt("One input arguments required."); 
    } else if (nlhs > 1) {
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
    
    img = mxGetPr(prhs[0]); 
	
    /* Do the actual computations in a subroutine */
    
	getboundary(&pytemp,&pxtemp,&plength, &pmax,img,m,n); 
    
	/* Create a matrix for the return argument */ 
    plhs[0] = mxCreateDoubleMatrix(plength, 2, mxREAL); 
	/* Assign pointers to the various parameters */ 
    
	pout = mxGetPr(plhs[0]);
	
	
    memcpy(pout,pxtemp,(plength)*sizeof(double)); //copy existing data across
	free(pxtemp); //free up the old space
	
	
	memcpy(pout+plength,pytemp,(plength)*sizeof(double)); //copy existing data across
	free(pytemp); //free up the old space
	
    
    return;
    
}


