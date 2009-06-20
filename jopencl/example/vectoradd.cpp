#include <stdio.h>
#include <stdlib.h>
#include <CL/cl.h>
// OpenCL source code
const char* OpenCLSource[] = {
       "q__kernel void VectorAdd(__global int* c, __global int* a,\n",
       "                         __global int* b)\n",
       "{\n",
       "      // Index of the elements to add\n",
       "      unsigned int n = get_global_id(0);\n",
       "      // Sum the nth element of vectors a and b and store in c\n",
       "      c[n] = a[n] + b[n];\n",
       "}\n"
};
// Some interesting data for the vectors
int InitialData1[14] = {37,50,54,50,56,12,37,45,77,81,92,56,-22,-4};
int InitialData2[14] = {35,51,54,58,55,32,-5,42,34,33,16,44,55,14};
#define SIZE 2048   // number of elements in the vectors to be added




// Main function
// *********************************************************************
int main(int argc, char **argv)
{
    // Here's two source vectors in CPU (Host) memory
    int HostVector1[SIZE];
    int HostVector2[SIZE];
    // Lets initialize them with some interesting repeating data
    for(int c = 0;c<SIZE;c++) {
           HostVector1[c] = InitialData1[c%14];
           HostVector2[c] = InitialData2[c%14];
    }

// We want to run our OpenCL on our CUDA-enable NVIDIA hardware,
// so we create a GPU type context
cl_context GPUContext = clCreateContextFromType(0, CL_DEVICE_TYPE_GPU,
      NULL, NULL, NULL);
// Then we can get the list of GPU devices associated with this context
size_t ParmDataBytes;
clGetContextInfo(GPUContext, CL_CONTEXT_DEVICES, 0, NULL, &ParmDataBytes);
cl_device_id* GPUDevices = (cl_device_id*)malloc(ParmDataBytes);
clGetContextInfo(GPUContext, CL_CONTEXT_DEVICES, ParmDataBytes,
      GPUDevices, NULL);
// And create a command-queue on the first GPU device
cl_command_queue GPUCommandQueue = clCreateCommandQueue(GPUContext,
      GPUDevices[0], 0, NULL);
// Allocate GPU memory for the source vectors and initialize with the CPU
// memory
cl_mem GPUVector1 = clCreateBuffer(GPUContext, CL_MEM_READ_ONLY |
      CL_MEM_COPY_HOST_PTR, sizeof(int) * SIZE, HostVector1, NULL);
cl_mem GPUVector2 = clCreateBuffer(GPUContext, CL_MEM_READ_ONLY |
      CL_MEM_COPY_HOST_PTR, sizeof(int) * SIZE, HostVector2, NULL);
// Allocate output memory on GPU
cl_mem GPUOutputVector;
GPUOutputVector = clCreateBuffer(GPUContext, CL_MEM_WRITE_ONLY,
      sizeof(int) * SIZE, NULL, NULL);
// We are now ready to run the kernel code on the GPU
// OpenCL supports runtime code compilation. First we build the OpenCL
// program from source code
cl_program OpenCLProgram = clCreateProgramWithSource(GPUContext, 8,
      OpenCLSource, NULL, NULL);
cl_int err;
err=clBuildProgram(OpenCLProgram,0,NULL,NULL,NULL,NULL);
printf("build err %d\n",err);
// Then we can create a handle to the compiled OpenCL function (Kernel)
cl_kernel OpenCLVectorAdd = clCreateKernel(OpenCLProgram, "VectorAdd",
      &err);

printf("createk err %d\n",err);

 cl_uint args2;
        cl_int ret=clGetKernelInfo 
(OpenCLVectorAdd,CL_KERNEL_NUM_ARGS,sizeof(args2),&args2,NULL);
printf("# arg: %d\n",args2);

// In the next step we associate the GPU memory with the Kernel arguments
clSetKernelArg(OpenCLVectorAdd, 0, sizeof(cl_mem),
      (void*)&GPUOutputVector);
clSetKernelArg(OpenCLVectorAdd, 1, sizeof(cl_mem), (void*)&GPUVector1);
clSetKernelArg(OpenCLVectorAdd, 2, sizeof(cl_mem), (void*)&GPUVector2);
// Then we launch the Kernel on the GPU
size_t WorkSize[1] = {SIZE};
clEnqueueNDRangeKernel(GPUCommandQueue, OpenCLVectorAdd, 1, NULL,
      WorkSize, NULL, 0, NULL, NULL);
// Here’s a vector in CPU memory the output
int HostOutputVector[SIZE];
// Copy the output in GPU memory back to CPU memory
clEnqueueReadBuffer(GPUCommandQueue, GPUOutputVector, CL_TRUE, 0,
      SIZE*sizeof(int), HostOutputVector, 0, NULL, NULL);


  // We are finished with GPU memory so we can free it
  clReleaseMemObject(GPUVector1);
  clReleaseMemObject(GPUVector2);
  clReleaseMemObject(GPUOutputVector);
  free(GPUDevices);
  // Print out the results for fun.
  // We are simply casting the numeric result to a char
  // and printing it to the console
  for(int c = 0; c < 305;c++)
       printf("%c",(char)HostOutputVector[c]);
  return 0;
}

