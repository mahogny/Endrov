all:
	#gcc -g --std=c99 -c wrapper.c -I/usr/lib/jvm/java-6-openjdk/include/ -I ./include -I ~/NVIDIA_GPU_Computing_SDK/OpenCL/common/inc/
	gcc -g --std=c99 -c wrapper.c -I/usr/lib/jvm/java-6-openjdk/include/ -I ./include -I ~/NVIDIA_GPU_Computing_SDK/OpenCL/common/inc/
	#ld -G wrapper.o -L /home/mahogny/NVIDIA_GPU_Computing_SDK/OpenCL/common/lib/Linux32/ -lOpenCL -o libjopencl.so
	ld -G wrapper.o -L ~/NVIDIA_GPU_Computing_SDK/OpenCL/common/lib/Linux32/ -lOpenCL -o libjopencl.so

#	ld -G wrapper.o  -o libjopencl.so
#-lOpenCL

javah:
	javah -d include -jni javax.opencl.OpenCL
	javah -d include -jni javax.opencl.CLKernel
	javah -d include -jni javax.opencl.CLContext
	javah -d include -jni javax.opencl.CLDevice
	javah -d include -jni javax.opencl.CLMem
	javah -d include -jni javax.opencl.CLProgram
	javah -d include -jni javax.opencl.CLPlatform
	javah -d include -jni javax.opencl.CLCommandQueue


#all:
#	swig -java -package opencl -outdir opencl opencl.i
##	swig -java opencl.i

lib:
	gcc -c opencl_wrap.c  -I/usr/lib/jvm/java-6-openjdk/include/
	ld -G opencl_wrap.o  -o libopencl.so


ex:
	g++ -L ~/NVIDIA_GPU_Computing_SDK/OpenCL/common/lib/Linux32/ -I ~/NVIDIA_GPU_Computing_SDK/OpenCL/common/inc/ -lOpenCL vectoradd.cpp -o vectoradd

run:
	java -Djava.library.path=/home/mahogny/javaproj/jopencl example.ExVectoradd

#java -Djava.library.path=/home/mahogny/javaproj/jopencl:/home/mahogny/NVIDIA_GPU_Computing_SDK/OpenCL/common/lib/Linux32/ example.Vectoradd

