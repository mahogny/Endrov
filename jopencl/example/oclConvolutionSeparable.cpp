/*
 * Copyright 1993-2009 NVIDIA Corporation.  All rights reserved.
 *
 * NOTICE TO USER:   
 *
 * This source code is subject to NVIDIA ownership rights under U.S. and 
 * international Copyright laws.  Users and possessors of this source code 
 * are hereby granted a nonexclusive, royalty-free license to use this code 
 * in individual and commercial software.
 *
 * NVIDIA MAKES NO REPRESENTATION ABOUT THE SUITABILITY OF THIS SOURCE 
 * CODE FOR ANY PURPOSE.  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR 
 * IMPLIED WARRANTY OF ANY KIND.  NVIDIA DISCLAIMS ALL WARRANTIES WITH 
 * REGARD TO THIS SOURCE CODE, INCLUDING ALL IMPLIED WARRANTIES OF 
 * MERCHANTABILITY, NONINFRINGEMENT, AND FITNESS FOR A PARTICULAR PURPOSE.
 * IN NO EVENT SHALL NVIDIA BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL, 
 * OR CONSEQUENTIAL DAMAGES, OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS 
 * OF USE, DATA OR PROFITS,  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE 
 * OR OTHER TORTIOUS ACTION,  ARISING OUT OF OR IN CONNECTION WITH THE USE 
 * OR PERFORMANCE OF THIS SOURCE CODE.  
 *
 * U.S. Government End Users.   This source code is a "commercial item" as 
 * that term is defined at  48 C.F.R. 2.101 (OCT 1995), consisting  of 
 * "commercial computer  software"  and "commercial computer software 
 * documentation" as such terms are  used in 48 C.F.R. 12.212 (SEPT 1995) 
 * and is provided to the U.S. Government only as a commercial end item.  
 * Consistent with 48 C.F.R.12.212 and 48 C.F.R. 227.7202-1 through 
 * 227.7202-4 (JUNE 1995), all U.S. Government End Users acquire the 
 * source code with only those rights set forth herein. 
 *
 * Any use of this source code in individual and commercial software must 
 * include, in the user documentation and internal comments to the code,
 * the above Disclaimer and U.S. Government End Users Notice.
 */

// standard utilities and systems includes
#include <oclUtils.h>

////////////////////////////////////////////////////////////////////////////////
// Reference CPU code
////////////////////////////////////////////////////////////////////////////////
extern "C" void convolutionRowCPU(
    float *h_Dst,
    float *h_Src,
    float *h_Kernel,
    int imageW,
    int imageH,
    int kernelR
);

extern "C" void convolutionColumnCPU(
    float *h_Dst,
    float *h_Src,
    float *h_Kernel,
    int imageW,
    int imageH,
    int kernelR
);

////////////////////////////////////////////////////////////////////////////////
// OpenCL launchers for row and column filter kernels
////////////////////////////////////////////////////////////////////////////////
const unsigned int         KERNEL_RADIUS = 8;
const unsigned int KERNEL_RADIUS_ALIGNED = 16;
const unsigned int         KERNEL_LENGTH = 2 * KERNEL_RADIUS + 1;

unsigned int iDivUp(unsigned int dividend, unsigned int divisor){
    return (dividend % divisor == 0) ? (dividend / divisor) : (dividend / divisor + 1);
}

extern "C" void convolutionRowsOCL(
    cl_command_queue cqCommandQue,
    cl_kernel ckConvolutionRows,
    cl_mem d_Dst,
    cl_mem d_Src,
    cl_mem d_Kernel,
    cl_uint imageH,
    cl_uint imageW
);
extern "C" void convolutionColumnsOCL(
    cl_command_queue cqCommandQue,
    cl_kernel ckConvolutionColumns,
    cl_mem d_Dst,
    cl_mem d_Src,
    cl_mem d_Kernel,
    cl_uint imageH,
    cl_uint imageW
);

////////////////////////////////////////////////////////////////////////////////
// Main program
////////////////////////////////////////////////////////////////////////////////
int main(int argc, const char **argv){
    cl_context                             cxGPUContext; //OpenCL context
    cl_command_queue                        cqCommandQue; //OpenCL command que
    cl_program                                 cpProgram; //OpenCL program
    cl_kernel    ckConvolutionRows, ckConvolutionColumns; //OpenCL kernel
    cl_mem         d_Kernel, d_Input, d_Buffer, d_Output; //OpenCL memory buffer objects
    cl_float *h_Kernel, *h_Input, *h_Buffer, *h_OutputCPU, *h_OutputGPU;

    size_t dataBytes, kernelLength;
    cl_int errcode;

    const unsigned int imageW = 2048;
    const unsigned int imageH = 2048;

    // set logfile name and start logs
    shrSetLogFileName ("oclConvolutionSeparable.txt");
    shrLog(LOGBOTH, 0.0, "%s Starting...\n\n", argv[0]); 

    shrLog(LOGBOTH, 0.0, "Allocating and initializing host memory...\n");
        h_Kernel    = (cl_float *)malloc(KERNEL_LENGTH * sizeof(cl_float));
        h_Input     = (cl_float *)malloc(imageW * imageH * sizeof(cl_float));
        h_Buffer    = (cl_float *)malloc(imageW * imageH * sizeof(cl_float));
        h_OutputCPU = (cl_float *)malloc(imageW * imageH * sizeof(cl_float));
        h_OutputGPU = (cl_float *)malloc(imageW * imageH * sizeof(cl_float));

        srand(2009);
        for(unsigned int i = 0; i < KERNEL_LENGTH; i++)
            h_Kernel[i] = (cl_float)rand() / (cl_float)RAND_MAX;

        for(unsigned int i = 0; i < imageW * imageH; i++)
            h_Input[i] = (cl_float)rand() / (cl_float)RAND_MAX;

    shrLog(LOGBOTH, 0.0, "Initializing OpenCL...\n");
        cxGPUContext = clCreateContextFromType(0, CL_DEVICE_TYPE_GPU, NULL, NULL, &errcode);
        shrCheckError(errcode, CL_SUCCESS);

        // get the list of GPU devices associated with context
        errcode = clGetContextInfo(cxGPUContext, CL_CONTEXT_DEVICES, 0, NULL, &dataBytes);
        cl_device_id *cdDevices = (cl_device_id *)malloc(dataBytes);
        errcode |= clGetContextInfo(cxGPUContext, CL_CONTEXT_DEVICES, dataBytes, cdDevices, NULL);
        shrCheckError(errcode, CL_SUCCESS);

        //Create a command-queue
        cqCommandQue = clCreateCommandQueue(cxGPUContext, cdDevices[0], 0, &errcode);
        shrCheckError(errcode, CL_SUCCESS);

    shrLog(LOGBOTH, 0.0, "Creating OpenCL memory objects...\n");
        d_Kernel = clCreateBuffer(cxGPUContext, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, KERNEL_LENGTH * sizeof(cl_float), h_Kernel, &errcode);
        shrCheckError(errcode, CL_SUCCESS);
        d_Input = clCreateBuffer(cxGPUContext, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, imageW * imageH * sizeof(cl_float), h_Input, &errcode);
        shrCheckError(errcode, CL_SUCCESS);
        d_Buffer = clCreateBuffer(cxGPUContext, CL_MEM_READ_WRITE, imageW * imageH * sizeof(cl_float), NULL, &errcode);
        shrCheckError(errcode, CL_SUCCESS);
        d_Output = clCreateBuffer(cxGPUContext, CL_MEM_WRITE_ONLY, imageW * imageH * sizeof(cl_float), NULL, &errcode);
        shrCheckError(errcode, CL_SUCCESS);

    shrLog(LOGBOTH, 0.0, "Loading OpenCL program from file...\n");
        char *cPathAndName = shrFindFilePath("ConvolutionSeparable.cl", argv[0]);
        shrCheckError(cPathAndName != NULL, shrTRUE);
        char *cConvolutionSeparable = oclLoadProgSource(cPathAndName, "// My comment\n", &kernelLength);
        shrCheckError(cConvolutionSeparable != NULL, shrTRUE);

    shrLog(LOGBOTH, 0.0, "Creating program...\n");
        cpProgram = clCreateProgramWithSource(cxGPUContext, 1, (const char **)&cConvolutionSeparable, &kernelLength, &errcode);
        shrCheckError(errcode, CL_SUCCESS);

    shrLog(LOGBOTH, 0.0, "Building program...\n");
        errcode = clBuildProgram(cpProgram, 0, NULL, NULL, NULL, NULL);
        shrCheckError(errcode, CL_SUCCESS);

    shrLog(LOGBOTH, 0.0, "Creating kernels...\n");
        ckConvolutionRows = clCreateKernel(cpProgram, "convolutionRows", &errcode);
        shrCheckError(errcode, CL_SUCCESS);
        ckConvolutionColumns = clCreateKernel(cpProgram, "convolutionColumns", &errcode);
        shrCheckError(errcode, CL_SUCCESS);

    shrLog(LOGBOTH, 0.0, "Applying row filter to %u x %u image...\n", imageH, imageW);
        convolutionRowsOCL(
            cqCommandQue,
            ckConvolutionRows,
            d_Buffer,
            d_Input,
            d_Kernel,
            imageH,
            imageW
        );

    shrLog(LOGBOTH, 0.0, "Applying column filter to %u x %u image...\n", imageH, imageW);
        convolutionColumnsOCL(
            cqCommandQue,
            ckConvolutionColumns,
            d_Output,
            d_Buffer,
            d_Kernel,
            imageH,
            imageW
            );

    shrLog(LOGBOTH, 0.0, "Reading back OpenCL results...\n\n");
        errcode = clEnqueueReadBuffer(cqCommandQue, d_Output, CL_TRUE, 0, imageW * imageH * sizeof(cl_float), h_OutputGPU, 0, NULL, NULL);
        shrCheckError(errcode, CL_SUCCESS);

    shrLog(LOGBOTH, 0.0, "Comparing against Host/C++ computation...\n"); 
        convolutionRowCPU(h_Buffer, h_Input, h_Kernel, imageW, imageH, KERNEL_RADIUS);
        convolutionColumnCPU(h_OutputCPU, h_Buffer, h_Kernel, imageW, imageH, KERNEL_RADIUS);
        double sum = 0, delta = 0;
        double L2norm;
        for(unsigned int i = 0; i < imageW * imageH; i++){
            delta += (h_OutputCPU[i] - h_OutputGPU[i]) * (h_OutputCPU[i] - h_OutputGPU[i]);
            sum += h_OutputCPU[i] * h_OutputCPU[i];
        }
        L2norm = sqrt(delta / sum);
        shrLog(LOGBOTH, 0.0, "Relative L2 norm: %.3e\n\n", L2norm);

    shrLog(LOGBOTH, 0.0, (L2norm < 1E-6) ? "TEST PASSED\n\n" : "TEST FAILED !!!\n\n");

    // cleanup
    errcode  = clReleaseMemObject(d_Output);
    errcode |= clReleaseMemObject(d_Buffer);
    errcode |= clReleaseMemObject(d_Input);
    errcode |= clReleaseMemObject(d_Kernel);
    errcode |= clReleaseKernel(ckConvolutionColumns);
    errcode |= clReleaseKernel(ckConvolutionRows);
    errcode |= clReleaseProgram(cpProgram);
    errcode |= clReleaseCommandQueue(cqCommandQue);
    errcode |= clReleaseContext(cxGPUContext);
    shrCheckError(errcode, CL_SUCCESS);

    free(cdDevices);
    free(cConvolutionSeparable);
    free(h_OutputGPU);
    free(h_OutputCPU);
    free(h_Buffer);
    free(h_Input);
    free(h_Kernel);

    // finish
    shrEXIT(argc, argv);
}

////////////////////////////////////////////////////////////////////////////////
// OpenCL launchers for row and column filter kernels
////////////////////////////////////////////////////////////////////////////////


extern "C" void convolutionRowsOCL(
    cl_command_queue cqCommandQue,
    cl_kernel ckConvolutionRows,
    cl_mem d_Dst,
    cl_mem d_Src,
    cl_mem d_Kernel,
    cl_uint imageH,
    cl_uint imageW
){
    cl_int errcode;
    size_t localWorkSize[2], globalWorkSize[2];

    const unsigned int ROWS_OUTPUT_WIDTH = 128;
    errcode  = clSetKernelArg(ckConvolutionRows, 0, sizeof(cl_mem),       (void*)&d_Dst);
    errcode |= clSetKernelArg(ckConvolutionRows, 1, sizeof(cl_mem),       (void*)&d_Src);
    errcode |= clSetKernelArg(ckConvolutionRows, 2, sizeof(cl_mem),       (void*)&d_Kernel);
    errcode |= clSetKernelArg(ckConvolutionRows, 3, (KERNEL_RADIUS_ALIGNED + ROWS_OUTPUT_WIDTH + KERNEL_RADIUS) * sizeof(cl_float), NULL);
    errcode |= clSetKernelArg(ckConvolutionRows, 4, sizeof(unsigned int), (void*)&imageH);
    errcode |= clSetKernelArg(ckConvolutionRows, 5, sizeof(unsigned int), (void*)&imageW);
    shrCheckError(errcode, CL_SUCCESS);

    localWorkSize[0] = KERNEL_RADIUS_ALIGNED + ROWS_OUTPUT_WIDTH + KERNEL_RADIUS;
    localWorkSize[1] = 1;
    globalWorkSize[0] = iDivUp(imageW, ROWS_OUTPUT_WIDTH) * localWorkSize[0];
    globalWorkSize[1] = imageH;

    errcode = clEnqueueNDRangeKernel(cqCommandQue, ckConvolutionRows, 2, NULL, globalWorkSize, localWorkSize, 0, NULL, NULL);
    shrCheckError(errcode, CL_SUCCESS);
}

extern "C" void convolutionColumnsOCL(
    cl_command_queue cqCommandQue,
    cl_kernel ckConvolutionColumns,
    cl_mem d_Dst,
    cl_mem d_Src,
    cl_mem d_Kernel,
    cl_uint imageH,
    cl_uint imageW
){
    cl_int errcode;
    size_t localWorkSize[2], globalWorkSize[2];

    const unsigned int     COLUMNS_BLOCKDIMX = 16;
    const unsigned int     COLUMNS_BLOCKDIMY = 16;
    const unsigned int COLUMNS_OUTPUT_HEIGHT = 128;
    errcode  = clSetKernelArg(ckConvolutionColumns, 0, sizeof(cl_mem),       (void*)&d_Dst);
    errcode |= clSetKernelArg(ckConvolutionColumns, 1, sizeof(cl_mem),       (void*)&d_Src);
    errcode |= clSetKernelArg(ckConvolutionColumns, 2, sizeof(cl_mem),       (void*)&d_Kernel);
    errcode |= clSetKernelArg(ckConvolutionColumns, 3, (KERNEL_RADIUS + COLUMNS_OUTPUT_HEIGHT + KERNEL_RADIUS) * COLUMNS_BLOCKDIMX * sizeof(cl_float), NULL);
    errcode |= clSetKernelArg(ckConvolutionColumns, 4, sizeof(unsigned int), (void*)&imageH);
    errcode |= clSetKernelArg(ckConvolutionColumns, 5, sizeof(unsigned int), (void*)&imageW);
    shrCheckError(errcode, CL_SUCCESS);

    localWorkSize[0] = COLUMNS_BLOCKDIMX;
    localWorkSize[1] = COLUMNS_BLOCKDIMY;
    globalWorkSize[0] = imageW;
    globalWorkSize[1] = iDivUp(imageH, COLUMNS_OUTPUT_HEIGHT) * localWorkSize[1];

    errcode = clEnqueueNDRangeKernel(cqCommandQue, ckConvolutionColumns, 2, NULL, globalWorkSize, localWorkSize, 0, NULL, NULL);
    shrCheckError(errcode, CL_SUCCESS);
}

