#include <math.h>
#include <stdlib.h>
#include <string.h>
#include "mex.h"


#define INITSPACEPERVAL 99; //initial bytes allocated per grey level value


//%%%%%%%%%%%%%%%%%%%%%%%%%%%%
//% author: Keith Forbes     %
//% e-mail: keith@umpire.com %
//% tel: +27 21 674 3345     %
//%%%%%%%%%%%%%%%%%%%%%%%%%%%%

int inim(
		 unsigned int x,
		 unsigned int y,
		 unsigned int xmax,
		 unsigned int ymax
		 )
{
 return ((x>=0)&&(x<xmax))&&((y>=0)&&(y<ymax));
}


static void watershed(
					  double imgout[],
					   double g[],
					   double marker[],
					  int storemult,
					  unsigned int ymax,
					  unsigned int xmax
					  ) //form the watershed transform from a gradient image and a marker image
{

		
	long count[256];
	long startq[256];
	long endq[256];
	int * xq[256];
	int * yq[256];
	int * temp;
	unsigned int x,y;
	int currentq=0;
	int xcurr, ycurr;
	int qnum;
	long loop;
	long numelements;
	int i;

	numelements=(long)(ymax*xmax);

	for (loop=0;loop<numelements;loop++)
		imgout[loop]=marker[loop];  //copy the marker image into the output image

	for (i=0;i<=255;i++)
		count[i]=0;

	for (loop=0;loop<numelements;loop++)
	{
		count[(int)g[loop]]+=storemult;
		count[0]+=(int)(imgout[loop]>0.0)*storemult; //count marked pixels as zeros
	}
		

	for (i=0;i<=255;i++)
	{
		
		xq[i] = malloc(sizeof(int)*count[i]); //create a new space
		yq[i] = malloc(sizeof(int)*count[i]); //create a new space	
		startq[i] = 0;
		endq[i] = -1;
	}




	for (x=0;x<xmax;x++)
		for (y=0;y<ymax;y++)
			if ( imgout[y+x*ymax]  >0.0) //if marked flood sources
			{
				if (endq[0]+1==count[0]) //if out of queue space already
				{
					count[0]*= 2; //double the number of elements available
					
					temp = malloc(sizeof(int)*count[0]); //create a new space
					memcpy(temp,xq[0],(endq[0]+1)*sizeof(int)); //copy existing data across
					free(xq[0]);
					xq[0]=temp; //point to the new larger array

					temp = malloc(sizeof(int)*count[0]); //create a new space				
					//temp = new int [count[0]]; //create a new space

					memcpy(temp,yq[0],(endq[0]+1)*sizeof(int)); //copy existing data across
					
					free(yq[0]);
					//delete yq[0]; //free up the old space
					yq[0]=temp; //point to the new larger array

					//cout << "(initial)Realocation no." << track << ": Increased size of level " << qnum << " to " << count[qnum] << " elements" << endl;
					//track++;
				}
				
				
			endq[0]++;  //marker pixels are dealt with first
			(xq[0])[endq[0]]=x;
			(yq[0])[endq[0]]=y;
				
			}




	  //MAIN WATERSHED LOOP

		while (currentq<=255)
		{

			if (endq[currentq]>=startq[currentq])
			{
				xcurr=(xq[currentq])[startq[currentq]];
				ycurr=(yq[currentq])[startq[currentq]];
				startq[currentq]++;

					if (inim(xcurr+1,ycurr,xmax,ymax)) //RIGHT
					{
						if (imgout[ycurr+(xcurr+1)*ymax]==0.0) //not yet dealt with
						{
							//(*this).setval(xcurr+1,ycurr,(*this).getval(xcurr,ycurr)); //flood the pixel
							imgout[ycurr+(xcurr+1)*ymax]=imgout[ycurr+(xcurr)*ymax];

							//qnum=g.getval(xcurr+1,ycurr);
							qnum=(int)g[ycurr+(xcurr+1)*ymax];
							   if (qnum<currentq)
								   qnum=currentq; //%if q is closed put on current q

							if (endq[qnum]+1==count[qnum]) //if out of queue space 
								{
								count[qnum]*=2; //double the number of elements available
								
								temp = malloc(sizeof(int)*count[qnum]); //create a new space
								//temp = new int [count[qnum]]; //create a new space

								memcpy(temp,xq[qnum],(endq[qnum]+1)*sizeof(int)); //copy existing data across

								free(xq[qnum]);
								//delete xq[qnum]; //free up the old space
								xq[qnum]=temp; //point to the new larger array


								temp = malloc(sizeof(int)*count[qnum]); //create a new space
								//temp = new int [count[qnum]]; //create a new space

								memcpy(temp,yq[qnum],(endq[qnum]+1)*sizeof(int)); //copy existing data across

								free(yq[qnum]);
								//delete yq[qnum]; //free up the old space
								yq[qnum]=temp; //point to the new larger array

								//cout << "(right)Realocation no." << track << ": Increased size of level " << qnum << " to " << count[qnum] << " elements" << endl;
								//track++;
								}
							
							endq[qnum]++;
							(xq[qnum])[endq[qnum]]=xcurr+1;
							(yq[qnum])[endq[qnum]]=ycurr;

						}
					}


					if (inim(xcurr-1,ycurr,xmax,ymax)) ///%LEFT
					{
						if (imgout[ycurr+(xcurr-1)*ymax]==0.0) //not yet dealt with
						{
							//(*this).setval(xcurr+1,ycurr,(*this).getval(xcurr,ycurr)); //flood the pixel
							imgout[ycurr+(xcurr-1)*ymax]=imgout[ycurr+(xcurr)*ymax];

							//qnum=g.getval(xcurr+1,ycurr);
							qnum=(int)g[ycurr+(xcurr-1)*ymax];
							   if (qnum<currentq)
								   qnum=currentq; //%if q is closed put on current q

							if (endq[qnum]+1==count[qnum]) //if out of queue space 
								{
								count[qnum]*=2; //double the number of elements available
								
								temp = malloc(sizeof(int)*count[qnum]); //create a new space
								//temp = new int [count[qnum]]; //create a new space

								memcpy(temp,xq[qnum],(endq[qnum]+1)*sizeof(int)); //copy existing data across

								free(xq[qnum]);
								//delete xq[qnum]; //free up the old space
								xq[qnum]=temp; //point to the new larger array


								temp = malloc(sizeof(int)*count[qnum]); //create a new space
								//temp = new int [count[qnum]]; //create a new space

								memcpy(temp,yq[qnum],(endq[qnum]+1)*sizeof(int)); //copy existing data across

								free(yq[qnum]);
								//delete yq[qnum]; //free up the old space
								yq[qnum]=temp; //point to the new larger array

								//cout << "(right)Realocation no." << track << ": Increased size of level " << qnum << " to " << count[qnum] << " elements" << endl;
								//track++;
								}
							
							endq[qnum]++;
							(xq[qnum])[endq[qnum]]=xcurr-1;
							(yq[qnum])[endq[qnum]]=ycurr;

						}
					}



					if (inim(xcurr,ycurr+1,xmax,ymax)) //BELOW
					{
						if (imgout[ycurr+1+(xcurr)*ymax]==0.0) //not yet dealt with
						{
							//(*this).setval(xcurr+1,ycurr,(*this).getval(xcurr,ycurr)); //flood the pixel
							imgout[ycurr+1+(xcurr)*ymax]=imgout[ycurr+(xcurr)*ymax];

							//qnum=g.getval(xcurr+1,ycurr);
							qnum=(int)g[ycurr+1+(xcurr)*ymax];
							   if (qnum<currentq)
								   qnum=currentq; //%if q is closed put on current q

							if (endq[qnum]+1==count[qnum]) //if out of queue space 
								{
								count[qnum]*=2; //double the number of elements available
								
								temp = malloc(sizeof(int)*count[qnum]); //create a new space
								//temp = new int [count[qnum]]; //create a new space

								memcpy(temp,xq[qnum],(endq[qnum]+1)*sizeof(int)); //copy existing data across

								free(xq[qnum]);
								//delete xq[qnum]; //free up the old space
								xq[qnum]=temp; //point to the new larger array


								temp = malloc(sizeof(int)*count[qnum]); //create a new space
								//temp = new int [count[qnum]]; //create a new space

								memcpy(temp,yq[qnum],(endq[qnum]+1)*sizeof(int)); //copy existing data across

								free(yq[qnum]);
								//delete yq[qnum]; //free up the old space
								yq[qnum]=temp; //point to the new larger array

								//cout << "(right)Realocation no." << track << ": Increased size of level " << qnum << " to " << count[qnum] << " elements" << endl;
								//track++;
								}
							
							endq[qnum]++;
							(xq[qnum])[endq[qnum]]=xcurr;
							(yq[qnum])[endq[qnum]]=ycurr+1;

						}
					}


						if (inim(xcurr,ycurr-1,xmax,ymax)) //ABOVE
					{
						if (imgout[ycurr-1+(xcurr)*ymax]==0.0) //not yet dealt with
						{
							//(*this).setval(xcurr+1,ycurr,(*this).getval(xcurr,ycurr)); //flood the pixel
							imgout[ycurr-1+(xcurr)*ymax]=imgout[ycurr+(xcurr)*ymax];

							//qnum=g.getval(xcurr+1,ycurr);
							qnum=(int)g[ycurr-1+(xcurr)*ymax];
							   if (qnum<currentq)
								   qnum=currentq; //%if q is closed put on current q

							if (endq[qnum]+1==count[qnum]) //if out of queue space 
								{
								count[qnum]*=2; //double the number of elements available
								
								temp = malloc(sizeof(int)*count[qnum]); //create a new space
								//temp = new int [count[qnum]]; //create a new space

								memcpy(temp,xq[qnum],(endq[qnum]+1)*sizeof(int)); //copy existing data across

								free(xq[qnum]);
								//delete xq[qnum]; //free up the old space
								xq[qnum]=temp; //point to the new larger array


								temp = malloc(sizeof(int)*count[qnum]); //create a new space
								//temp = new int [count[qnum]]; //create a new space

								memcpy(temp,yq[qnum],(endq[qnum]+1)*sizeof(int)); //copy existing data across

								free(yq[qnum]);
								//delete yq[qnum]; //free up the old space
								yq[qnum]=temp; //point to the new larger array

								//cout << "(right)Realocation no." << track << ": Increased size of level " << qnum << " to " << count[qnum] << " elements" << endl;
								//track++;
								}
							
							endq[qnum]++;
							(xq[qnum])[endq[qnum]]=xcurr;
							(yq[qnum])[endq[qnum]]=ycurr-1;

						}
					}
			}

			if (startq[currentq]>endq[currentq])
				currentq++;

		}

	for (loop=0;loop<=255;loop++)
	{
		free(xq[loop]); //clean up by freeing used space otherwise memory leak is created
		free(yq[loop]);
	}
}

void mexFunction( int nlhs, mxArray *plhs[], 
		  int nrhs, const mxArray*prhs[] )
     
{ 
    double *marker, *g, *imgout; 
	unsigned int m1,n1,m2,n2; 
	unsigned int initspace;

	initspace=INITSPACEPERVAL;
    
	
    /* Check for proper number of arguments */
    
    if (nrhs != 2) { 
	mexErrMsgTxt("Two input arguments required."); 
    } else if (nlhs > 1) {
	mexErrMsgTxt("Too many output arguments."); 
    } 

	if (!mxIsClass(prhs[0],"double"))
		mexErrMsgTxt("input image must be of type double (use im2double or double)");
    
	if (!mxIsClass(prhs[1],"double"))
		mexErrMsgTxt("input image must be of type double (use im2double or double)");

	if (mxGetNumberOfDimensions(prhs[0])==3)
		mexErrMsgTxt("Not in tended for 3 band RGB images - only single band images."); 


	if (mxGetNumberOfDimensions(prhs[0])>3)
		mexErrMsgTxt("Not in tended for high dimensional data"); 

	if (mxGetNumberOfDimensions(prhs[1])==3)
		mexErrMsgTxt("Not in tended for 3 band RGB images - only single band images."); 


	if (mxGetNumberOfDimensions(prhs[1])>3)
		mexErrMsgTxt("Not in tended for high dimensional data"); 


    
    
    
    m1 = mxGetM(prhs[0]); 
    n1 = mxGetN(prhs[0]);
    m2 = mxGetM(prhs[1]); 
    n2 = mxGetN(prhs[1]);
    

    if ((m1 != m2)||(n1 != n2)) 
		mexErrMsgTxt("Gradient and marker images must be the same size."); 
    

	
    
    g = mxGetPr(prhs[0]); 
	marker = mxGetPr(prhs[1]); 

	if (!(mxIsDouble(prhs[0])))
		printf("%dx%d gradient image is not of type double\n",m1,n1);

	if (!(mxIsDouble(prhs[1])))
		printf("%dx%d marker image is not of type double\n",m2,n2);

	

	if ((!(mxIsDouble(prhs[0])))||(!(mxIsDouble(prhs[1])))) 
		mexErrMsgTxt("Gradient and marker images must be of type double.\n"); 
    

	plhs[0] = mxCreateDoubleMatrix(m1, n1, mxREAL); 
	
	imgout = mxGetPr(plhs[0]);

	
        
	//mexPrintf("hello");
    
	watershed(imgout,g,marker,initspace, m1, n1);
		
    
    return;
    
}


