all:
	gcc -c wrapper.c -I/usr/lib/jvm/java-6-openjdk/include/ -I ~/NVIDIA_GPU_Computing_SDK/OpenCL/common/inc/

javah:
	javah -jni javax.opencl.CLKernel
	javah -jni javax.opencl.CLContext
	javah -jni javax.opencl.CLMem
	javah -jni javax.opencl.CLProgram
	javah -jni javax.opencl.CLCommandQueue


#all:
#	swig -java -package opencl -outdir opencl opencl.i
##	swig -java opencl.i

lib:
	gcc -c opencl_wrap.c  -I/usr/lib/jvm/java-6-openjdk/include/
	ld -G opencl_wrap.o  -o libopencl.so


ex:
	g++ -L ~/NVIDIA_GPU_Computing_SDK/OpenCL/common/lib/Linux32/ -I ~/NVIDIA_GPU_Computing_SDK/OpenCL/common/inc/ -lOpenCL vectoradd.cpp -o vectoradd

