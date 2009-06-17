package example;

import javax.opencl.CLCommandQueue;
import javax.opencl.CLContext;
import javax.opencl.CLKernel;
import javax.opencl.CLMem;
import javax.opencl.CLProgram;
import javax.opencl.OpenCL;

/**
 * Example: adding vectors
 * @author Johan Henriksson
 *
 */
public class Vectoradd
	{
	
	public static String OpenCLSource =
      "__kernel void VectorAdd(__global int* c, __global int* a,\n"+
      "                         __global int* b)\n"+
      "{\n"+
      "      // Index of the elements to add\n"+
      "      unsigned int n = get_global_id(0);\n"+
      "      // Sum the nth element of vectors a and b and store in c\n"+
      "      c[n] = a[n] + b[n];\n"+
      "}\n";

	
//Some interesting data for the vectors
	public static int[] InitialData1 = new int[]{37,50,54,50,56,12,37,45,77,81,92,56,-22,-4};
	public static int[] InitialData2 = new int[]{35,51,54,58,55,32,-5,42,34,33,16,44,55,14};
	public static int SIZE=2048;   // number of elements in the vectors to be added

	
	public static void main(String[] args)
		{
    // Here's two source vectors in CPU (Host) memory
    int[] HostVector1=new int[SIZE];
    int[] HostVector2=new int[SIZE];
    // Lets initialize them with some interesting repeating data
    for(int c = 0;c<SIZE;c++)
    	{
      HostVector1[c] = InitialData1[c%14];
      HostVector2[c] = InitialData2[c%14];
    	}
		
    
		CLContext clc=OpenCL.createContext(OpenCL.CL_DEVICE_TYPE_GPU);
		
		
		// Then we can get the list of GPU devices associated with this context
		

		/*
	 * 
	size_t ParmDataBytes;
	clGetContextInfo(GPUContext, CL_CONTEXT_DEVICES, 0, NULL, &ParmDataBytes);
	
	cl_device_id* GPUDevices = (cl_device_id*)malloc(ParmDataBytes);
	
	clGetContextInfo(GPUContext, CL_CONTEXT_DEVICES, ParmDataBytes,
	      GPUDevices, NULL);
	      */

		int[] GPUdevices;
		
		
		// Create a command-queue on the first GPU device
		CLCommandQueue cq=clc.createCommandQueue(GPUdevices[0]);
//		cl_command_queue GPUCommandQueue = clCreateCommandQueue(GPUContext, GPUDevices[0], 0, NULL);
		
		
		// Allocate GPU memory for the source vectors and initialize with the CPU memory
		CLMem GPUVector1=clc.createBuffer(OpenCL.CL_MEM_READ_ONLY | OpenCL.CL_MEM_COPY_HOST_PTR, HostVector1); //size from array
		CLMem GPUVector2=clc.createBuffer(OpenCL.CL_MEM_READ_ONLY | OpenCL.CL_MEM_COPY_HOST_PTR, HostVector2);
		
		// Allocate output memory on GPU
		CLMem GPUOutputVector=clc.createBuffer(OpenCL.CL_MEM_WRITE_ONLY, "has to specify size here", sizeofINT * SIZE);
		//createBufferInt? numElem
		
		// We are now ready to run the kernel code on the GPU.
		// OpenCL supports runtime code compilation. First we build the OpenCL
		// program from source code
		CLProgram clp=clc.createProgram(OpenCLSource); //clCreateProgramWithSource
		clp.build();
		//Note: Blocking call without function pointer
		
		
		// Then we can create a handle to the compiled OpenCL function (Kernel)
		CLKernel OpenCLVectorAdd=clp.createKernel("VectorAdd");    //cl_kernel OpenCLVectorAdd = clCreateKernel(OpenCLProgram, "VectorAdd",  NULL);
		
		
		// In the next step we associate the GPU memory with the Kernel arguments
		/*
		clSetKernelArg(OpenCLVectorAdd, 0, sizeof(cl_mem), (void*)&GPUOutputVector);
		clSetKernelArg(OpenCLVectorAdd, 1, sizeof(cl_mem), (void*)&GPUVector1);
		clSetKernelArg(OpenCLVectorAdd, 2, sizeof(cl_mem), (void*)&GPUVector2);
		*/
		
		// Then we launch the Kernel on the GPU
		/*
		size_t WorkSize[1] = {SIZE};
		clEnqueueNDRangeKernel(GPUCommandQueue, OpenCLVectorAdd, 1, NULL,
		      WorkSize, NULL, 0, NULL, NULL);
		      */
		
		
		// Here’s a vector in CPU memory the output
		int HostOutputVector[]=new int[SIZE];
		/*
		// Copy the output in GPU memory back to CPU memory
		clEnqueueReadBuffer(GPUCommandQueue, GPUOutputVector, CL_TRUE, 0,
		      SIZE*sizeof(int), HostOutputVector, 0, NULL, NULL);
*/

		  // We are finished with GPU memory so we can free it
		GPUVector1.release();
		GPUVector2.release();
		GPUOutputVector.release();

	  // Print out the results for fun.
	  for(int c = 0; c < 305;c++)
      System.out.print(HostOutputVector[c]+" ");
	  System.out.println();
		
		
		
		
		}
	
	}
