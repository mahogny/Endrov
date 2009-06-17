#include <stdio.h>
#include <stdlib.h>
#include <CL/cl.h>
#include <jni.h>

#include "javax_opencl_CLCommandQueue.h"
#include "javax_opencl_CLContext.h"
#include "javax_opencl_CLKernel.h"
#include "javax_opencl_CLMem.h"
#include "javax_opencl_CLProgram.h"


/////////////////////// kernel ////////////////////


cl_kernel getKernel(JNIEnv *env, jobject cls)
	{
	jfieldID fid=(*env)->GetFieldID(env, cls, "kernel",  "I");
	return (cl_kernel)(*env)->GetIntField(env, cls, fid);
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLKernel__1retainKernel(JNIEnv *jenv, jobject cls)
	{
	return clRetainKernel(getKernel(jenv, cls));
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLKernel__1releaseKernel(JNIEnv *jenv, jobject cls)
	{
	return clReleaseKernel(getKernel(jenv, cls));
	}


/////////////////////// context ////////////////////

cl_context getContext(JNIEnv *env, jobject cls)
	{
	jfieldID fid=(*env)->GetFieldID(env, cls, "context",  "I");
	return (cl_context)(*env)->GetIntField(env, cls, fid);
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLContext__1createContext(JNIEnv *jenv, jobject cls, jint deviceType)
	{
	//jsize devslen = (*env)->GetArrayLength(env, devs);
//	jint *devsbody = (*env)->GetIntArrayElements(env, devsarr, 0);
	cl_int ret;
	cl_context context = clCreateContextFromType(0, deviceType, NULL, NULL, NULL);
//	(*env)->ReleaseIntArrayElements(env, devsarr, devsbody, 0);
	return ret;
	}


JNIEXPORT jint JNICALL Java_javax_opencl_CLContext__1retainContext(JNIEnv *jenv, jobject cls)
	{
	return clRetainContext(getContext(jenv, cls));
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLContext__1releaseContext(JNIEnv *jenv, jobject cls)
	{
	return clReleaseContext(getContext(jenv, cls));
	}

/////////////////////// mem ////////////////////

cl_mem getMem(JNIEnv *env, jobject cls)
	{
	jfieldID fid=(*env)->GetFieldID(env, cls, "mem",  "I");
	return (cl_mem)(*env)->GetIntField(env, cls, fid);
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLMem__1retainMem(JNIEnv *jenv, jobject cls)
	{
	return clRetainMemObject(getMem(jenv, cls));
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLMem__1releaseMem(JNIEnv *jenv, jobject cls)
	{
	return clReleaseMemObject(getMem(jenv, cls));
	}
