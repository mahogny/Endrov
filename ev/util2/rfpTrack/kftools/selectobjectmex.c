#include <math.h>
#include <malloc.h>
#include "mex.h"
#define DEBUG 0
#define DEBUG2 0
#define DEBUG3 0
#define INITSPACEPERVAL 221992; //initial bytes allocated per array - speed memory trade off


//Modified to fix bug 5/12/2001. Now for n objects, selecting the (n+1)th returns an empty image (zeros)

// SELECTOBJECTMEX(im,n) returns an image containing only the nth largest object

//%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//% author: Keith Forbes     %
//% e-mail: keith@umpire.com %
//% tel: +27 21 674 3345     %
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%


// 22 September 2000
// use zero and nonzero as background and foreground respectively.
// foreground is 4-connected
// background is 8-connected

int IsInImage(
		 unsigned int x,
		 unsigned int y,
		 unsigned int xmax,
		 unsigned int ymax
		 )
{
	return ((x>=0)&&(x<xmax))&&((y>=0)&&(y<ymax));
}


void cclabel(
			 double  imgOut[],
			 double	img[],
			 unsigned int ymax,
			 unsigned int xmax,
			 long *regionCount[],
			 unsigned int * regionArraySizePtr
			 )
{
	long startq=0;
	long endq=-1;
	unsigned int * xq;
	unsigned int * yq;
	long * temp;
	unsigned int x,y;
	long currentRegionLabel=0;
	unsigned int loop;	
	long histArrayCount=INITSPACEPERVAL;
	
	
	
	if (DEBUG) printf("inside cclabel\n");
	xq = malloc(sizeof(unsigned int)*xmax*ymax); //create a new space
	if (xq == NULL)
		mexErrMsgTxt("Could not allocate memory for xq."); 
	
	yq = malloc(sizeof(unsigned int)*xmax*ymax); //create a new space
	if (yq == NULL)
		mexErrMsgTxt("Could not allocate memory for yq."); 
	
	(*regionCount) = malloc(sizeof(long)*histArrayCount); 
	
	if (DEBUG2) printf("(regionCount) %p \n\n",(regionCount));
	
	
	if ((*regionCount) == NULL)
		mexErrMsgTxt("Could not allocate memory for (*regionCount)."); 
	
	
	if (DEBUG) printf("initial memory allocated\n");
	
	
	(*regionCount)[0]=0; //no background so far
	
	if (DEBUG) printf("about to change what regionArraySizePtr is pointing to...\n");
	
	*regionArraySizePtr=1; //no other regions yet
	
	if (DEBUG) printf("about to enter imgOut initialization loop...\n");
	
	for (x=0;x<xmax;x++)	
		for (y=0;y<ymax;y++)
			imgOut[y+x*ymax]=0.0; //initialize to zero
		
		if (DEBUG) printf("imgOut initialised\n");
		
		
		for (x=0;x<xmax;x++)	
			for (y=0;y<ymax;y++)
			{
				if (DEBUG) printf("x:%3d of %d y:%3d of %d \n",x,xmax,y,ymax);
				
				if (img[y+x*ymax]!=0.0)
				{
					if (DEBUG) printf("nonzero\n");
					if (imgOut[y+x*ymax]==0.0) // if not dealt with yet
					{
						if (DEBUG) printf("not dealt with\n");
						currentRegionLabel++; //start new region to flood
						if (DEBUG) printf("currentRegionLabel %d",currentRegionLabel);
						*regionArraySizePtr=currentRegionLabel+1;
						
						if (currentRegionLabel==histArrayCount) //if out of queue space 
						{
							if (DEBUG) printf("Allocating more space\n");
							histArrayCount*=2; //double the number of elements available								
							temp = malloc(sizeof(long)*histArrayCount); //create a new space							
							memcpy(temp,(*regionCount),currentRegionLabel*sizeof(long)); //copy existing data across
							free((*regionCount));
							(*regionCount)=temp; //point to the new larger array
						}
						
						//(*regionCount)[currentRegionLabel]=0; //no pixels counted yet						
						if (DEBUG) printf("startq :%d endq:%d \n",startq,endq);
						
						
						//assert(startq==endq);
						if (DEBUG) printf("startq :%d endq:%d \n",startq,endq);
						
						endq++; 									
						xq[endq]=x;
						yq[endq]=y;
						imgOut[yq[startq]+xq[startq]*ymax]=(double)currentRegionLabel;
						(*regionCount)[currentRegionLabel]=1; //first pixel of region counted			
						
						
						if (DEBUG) printf("startq :%d endq:%d \n",startq,endq);
						
						
						if (DEBUG) printf("about to queue");
						while (startq<=endq)
						{
							//deal with first element in queue
							if (DEBUG) printf("startq :%d endq:%d \n",startq,endq);
							
							//imgOut[yq[startq]+xq[startq]*ymax]=(double)currentRegionLabel;
							//regionCount[currentRegionLabel]++;
							
							if (IsInImage(xq[startq],yq[startq]+1,xmax,ymax)) //down
							{
								if ((img[(yq[startq]+1)+xq[startq]*ymax]!=0.0)&&
									(imgOut[(yq[startq]+1)+xq[startq]*ymax]==0.0))							
								{
									endq++;
									xq[endq]=xq[startq];
									yq[endq]=yq[startq]+1;
									imgOut[(yq[startq]+1)+xq[startq]*ymax]=(double)currentRegionLabel;
									(*regionCount)[currentRegionLabel]++;															
								}
							}	
							
							
							if (IsInImage(xq[startq],yq[startq]-1,xmax,ymax)) //up
							{
								if ((img[(yq[startq]-1)+xq[startq]*ymax]!=0.0)&&
									(imgOut[(yq[startq]-1)+xq[startq]*ymax]==0.0))										
								{
									endq++;
									xq[endq]=xq[startq];
									yq[endq]=yq[startq]-1;
									imgOut[(yq[startq]-1)+xq[startq]*ymax]=(double)currentRegionLabel;
									(*regionCount)[currentRegionLabel]++;															
									
								}
							}
							
							if (IsInImage(xq[startq]+1,yq[startq],xmax,ymax)) //right
							{
								if ((img[yq[startq]+(xq[startq]+1)*ymax]!=0.0)&&
									(imgOut[yq[startq]+(xq[startq]+1)*ymax]==0.0))											
								{
									endq++;
									xq[endq]=xq[startq]+1;
									yq[endq]=yq[startq];
									imgOut[yq[startq]+(xq[startq]+1)*ymax]=(double)currentRegionLabel;
									(*regionCount)[currentRegionLabel]++;															
									
									
								}
								
							}
							
							
							if (IsInImage(xq[startq]-1,yq[startq],xmax,ymax)) //left
							{
								if ((img[yq[startq]+(xq[startq]-1)*ymax]!=0.0)&&
									(imgOut[yq[startq]+(xq[startq]-1)*ymax]==0.0))												
								{
									endq++;
									xq[endq]=xq[startq]-1;
									yq[endq]=yq[startq];
									imgOut[yq[startq]+(xq[startq]-1)*ymax]=(double)currentRegionLabel;
									(*regionCount)[currentRegionLabel]++;															
									
									
								}
							}		
							//if (DEBUG) printf("region %d: count %d \n",currentRegionLabel,(*regionCount)[currentRegionLabel]);
							startq++;
							
						}
					}
				}
				else
					(*regionCount)[0]++;
			}
			
			free(xq);
			free(yq);
			
			if (DEBUG2)
			{
				for (loop=0;loop<(*regionArraySizePtr);loop++)
					printf("region: %d count: %d \n",loop,(*regionCount)[loop]);
				
			}
			
			
			if (DEBUG) printf("Done cclabel \n");
			
}


static void selectobject(
						 double  imgOut[],
						 unsigned int ymax,
						 unsigned int xmax,
						 unsigned int n,
						 long *regionCount[],						 
						 unsigned int regionArraySize
						 )
{
	unsigned int loop;
	unsigned int order, currentMax, indMax;
	long ind;
	
	
	if (DEBUG3)
	{
		for (loop=0;loop<(regionArraySize);loop++)
			printf("region: %d count: %d \n",loop,(*regionCount)[loop]);
		
	}
	
	
	
	if (DEBUG3) printf("Inside selectobject and seeking region number %d \n",n);
	
	if (DEBUG3) printf("n:%d regionArraySize: %d \n",n,regionArraySize);

	if (n>(regionArraySize-1))
	{
		for (ind=0; ind<(long)(xmax*ymax); ind++)
			
			imgOut[ind]=0.0; //nth largest object does not exist
							 if (DEBUG) printf("There are only %d regions in the image.\
								 Region number %d doesn't exist\n",regionArraySize,n);
							 
	}
	
	else
		
	{
		if (DEBUG3) printf("About to try and find nth largest...\n");							 
		for (order=1;order<=n;order++)
		{
			currentMax=0;
			for (loop=1;loop<regionArraySize;loop++) //start from 1 'cos we're not interested in the background
				if ((*regionCount)[loop]>currentMax)
				{
					currentMax=(*regionCount)[loop];
					indMax=loop;
				}
				(*regionCount)[indMax]=0; //set the biggest to zero
		}
		
		
		if (DEBUG3) printf("Region number %d is labelled %d\n",n,indMax);
		
		

		for (ind=0;ind<(long)(xmax*ymax);ind++)
			imgOut[ind]=(imgOut[ind]==(double)(indMax)); //set output image to one or zero depending on whether it matches the nth largest region
		
	}
	
	if (DEBUG3) printf("Finished in static void selectobject\n");
}


void mexFunction( int nlhs, mxArray *plhs[], 
				 int nrhs, const mxArray*prhs[] )
				 
{ 
    double *imgOut;
	double *img; 
	double *n;
	unsigned int numRows,numColumns, nN, nM; 
	long *regionInit=0;
	long **regionCount=&regionInit; //  histogram array start counting at 1 zero is for background;
	unsigned int regionArraySize=0;
	unsigned int *regionArraySizePtr=&regionArraySize;
	long loop;
	
    
	/* Check for proper number of arguments */
    
	if (mxGetNumberOfDimensions(prhs[0])==3)
		mexErrMsgTxt("Not in tended for 3 band RGB images - only single band images."); 


	if (mxGetNumberOfDimensions(prhs[0])>3)
		mexErrMsgTxt("Not in tended for high dimensional data"); 

	if (!mxIsClass(prhs[0],"double"))
		mexErrMsgTxt("input image must be of type double (use im2double or double)");
    


    if (DEBUG2) printf("(regionCount) %p \n\n",(regionCount));
	
	
	if (nrhs != 2) { 
		mexErrMsgTxt("Two input arguments required."); 
    } else if (nlhs > 1) {
		mexErrMsgTxt("Too many output arguments."); 
    } 
    
    
    numRows = mxGetM(prhs[0]); 
    numColumns = mxGetN(prhs[0]);
	
	
	if (DEBUG2)
		printf("numRows %d numColumns %d \n",numRows,numColumns);
	
    
    img = mxGetPr(prhs[0]); 
	
	n = mxGetPr(prhs[1]); //which object to select in order of size 1 is largest 2 is 2nd largest etc
	
	nM = mxGetM(prhs[1]); 
    nN = mxGetN(prhs[1]);
	
	
    if (*n<1.0)
		mexErrMsgTxt("Second argument must be one or bigger."); 
	
	
	if ((nM>1)||(nN>1))
		mexErrMsgTxt("Second argument must be scalar."); 
	
	
	plhs[0] = mxCreateDoubleMatrix(numRows, numColumns, mxREAL); 
	
	imgOut=mxGetPr(plhs[0]); 
	/* Do the actual computations in a subroutine */
    
	//if (DEBUG) printf("Requested region number %d \n",(unsigned int)(*n));
	
	cclabel(imgOut,img,numRows,numColumns,regionCount,regionArraySizePtr); //first region label and get histogram
	
	if (DEBUG) printf("Requested region number %d \n",(unsigned int)(*n));
	if (DEBUG) printf("Region array size: %d \n",regionArraySize);
	
	if (DEBUG2) printf("(regionArraySize) %d \n",(regionArraySize));
	
	
	selectobject(imgOut,numRows,numColumns,(unsigned int)(*n),regionCount,regionArraySize); 
	
	
	free(*regionCount);
	
	
    return;
    
}


