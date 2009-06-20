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



#define         KERNEL_RADIUS 8
#define KERNEL_RADIUS_ALIGNED 16
#define         KERNEL_LENGTH (2 * KERNEL_RADIUS + 1)




#define     ROWS_OUTPUT_WIDTH 128

__kernel void convolutionRows(
    __global float *d_Dst,
    __global float *d_Src,
    __constant float *d_Kernel,
    __local float l_Data[KERNEL_RADIUS_ALIGNED + ROWS_OUTPUT_WIDTH + KERNEL_RADIUS],
    unsigned int H,
    unsigned int W
){
    int globalPosX = get_group_id(0) * ROWS_OUTPUT_WIDTH + (get_local_id(0) - KERNEL_RADIUS_ALIGNED);
    int globalPosY = get_group_id(1);

    l_Data[get_local_id(0)] = (globalPosX >= 0 && globalPosX < W) ? d_Src[globalPosY * W + globalPosX] : 0;

    barrier(CLK_LOCAL_MEM_FENCE);
    if(globalPosX >= 0 && globalPosX < W && get_local_id(0) >= KERNEL_RADIUS_ALIGNED && get_local_id(0) < (KERNEL_RADIUS_ALIGNED + ROWS_OUTPUT_WIDTH)){
        float sum = 0;
        for(int i = -KERNEL_RADIUS; i <= KERNEL_RADIUS; i++)
            sum += l_Data[get_local_id(0) + i] * d_Kernel[KERNEL_RADIUS - i];

        d_Dst[globalPosY * W + globalPosX] = sum;
    }
}



#define     COLUMNS_BLOCKDIMX 16
#define     COLUMNS_BLOCKDIMY 16
#define COLUMNS_OUTPUT_HEIGHT 128

__kernel void convolutionColumns(
    __global float *d_Dst,
    __global float *d_Src,
    __constant float *d_Kernel,
    __local float l_Data[KERNEL_RADIUS + ROWS_OUTPUT_WIDTH + KERNEL_RADIUS][COLUMNS_BLOCKDIMX],
    unsigned int H,
    unsigned int W
){
    int globalPosX = get_global_id(0);
    int globalPosY = get_group_id(1) * COLUMNS_OUTPUT_HEIGHT + (get_local_id(1) - KERNEL_RADIUS);

    for(int ly = get_local_id(1), gy = globalPosY; ly < COLUMNS_OUTPUT_HEIGHT + 2 * KERNEL_RADIUS; ly += COLUMNS_BLOCKDIMY, gy += COLUMNS_BLOCKDIMY)
        l_Data[ly][get_local_id(0)] = (gy >= 0 && gy < H) ? d_Src[gy * W + globalPosX] : 0;

    barrier(CLK_LOCAL_MEM_FENCE);
    for(int ly = get_local_id(1), gy = globalPosY; ly < COLUMNS_OUTPUT_HEIGHT + 2 * KERNEL_RADIUS; ly += COLUMNS_BLOCKDIMY, gy += COLUMNS_BLOCKDIMY)
        if(gy >= 0 && gy < H && ly >= KERNEL_RADIUS && ly < KERNEL_RADIUS + COLUMNS_OUTPUT_HEIGHT){
            float sum = 0;
            for(int i = -KERNEL_RADIUS; i <= KERNEL_RADIUS; i++)
                sum += l_Data[ly + i][get_local_id(0)] * d_Kernel[KERNEL_RADIUS - i];
            d_Dst[gy * W + globalPosX] = sum;
        }
}


