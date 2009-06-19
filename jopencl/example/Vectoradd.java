package example;

import javax.opencl.*;

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
    int[] hostVector1=new int[SIZE];
    int[] hostVector2=new int[SIZE];
    
    
    
    // Lets initialize them with some interesting repeating data
    for(int c = 0;c<SIZE;c++)
    	{
      hostVector1[c] = InitialData1[c%14];
      hostVector2[c] = InitialData2[c%14];
    	}
		
    System.out.println("foo0");
    
		CLContext clc=OpenCL.createContext(OpenCL.CL_DEVICE_TYPE_GPU);		
		
		System.out.println("foo1");
		
		// Then we can get the list of GPU devices associated with this context
		CLDevice[] GPUdevices=clc.getContextDevices();
		
		// Create a command-queue on the first GPU device
		CLCommandQueue cq=clc.createCommandQueue(GPUdevices[0]);
		
		// Allocate GPU memory for the source vectors and initialize with the CPU memory
		CLMem gpuVector1=clc.createBuffer(OpenCL.CL_MEM_READ_ONLY | OpenCL.CL_MEM_COPY_HOST_PTR, hostVector1); //size from array
		CLMem gpuVector2=clc.createBuffer(OpenCL.CL_MEM_READ_ONLY | OpenCL.CL_MEM_COPY_HOST_PTR, hostVector2);
		
		// Allocate output memory on GPU
		CLMem gpuOutputVector=clc.createBuffer(OpenCL.CL_MEM_WRITE_ONLY, Integer.class, SIZE);

		
		// We are now ready to run the kernel code on the GPU.
		// OpenCL supports runtime code compilation. First we build the OpenCL program from source code
		CLProgram clp=clc.createProgram(OpenCLSource); 
		clp.build(); //Note: Blocking call
		
		
		// Then we can create a handle to the compiled OpenCL function (Kernel)
		CLKernel kernelVectorAdd=clp.createKernel("VectorAdd");  
		
		// In the next step we associate the GPU memory with the Kernel arguments
		kernelVectorAdd.setKernelArg(0, gpuOutputVector);
		kernelVectorAdd.setKernelArg(1, gpuVector1);
		kernelVectorAdd.setKernelArg(2, gpuVector2);
		
		// Then we launch the Kernel on the GPU
		cq.enqueueNDRangeKernel(kernelVectorAdd, 
				1, null, new int[]{SIZE}, null,
				new CLEvent[]{}); 
		
		// Copy the output in GPU memory back to CPU memory
		int hostOutputVector[]=new int[SIZE];
		cq.enqueueReadBuffer(gpuOutputVector, true, 0, SIZE, hostOutputVector, new CLEvent[]{});	

	  // We are finished with GPU memory so we can free it
		gpuVector1.release();
		gpuVector2.release();
		gpuOutputVector.release();

	  // Print out the results for fun.
	  for(int c = 0; c < 305;c++)
      System.out.print(hostOutputVector[c]+" ");
	  System.out.println();
		}
	
	}
