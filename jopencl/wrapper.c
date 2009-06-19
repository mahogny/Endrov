#include <stdio.h>
#include <stdlib.h>
#include <string.h>
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
	jfieldID fid=(*env)->GetFieldID(env, cls, "id",  "I");
	return (cl_kernel)(*env)->GetIntField(env, cls, fid);
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLKernel__1createKernel(JNIEnv *jenv, jobject cls, jint pid, jstring name)
	{
	cl_int ret;
	const jbyte *str = (*jenv)->GetStringUTFChars( jenv, name, NULL );
	cl_kernel kernel=clCreateKernel ((cl_program)pid, str, &ret);
	(*jenv)->ReleaseStringUTFChars(jenv, name, str);
	jfieldID fid=(*jenv)->GetFieldID(jenv, cls, "id",  "I");
	(*jenv)->SetIntField(jenv, cls, fid, (jint)kernel);
	return ret;
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLKernel__1retainKernel(JNIEnv *jenv, jobject cls, jint kid)
	{
	return clRetainKernel((cl_kernel)kid);
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLKernel__1releaseKernel(JNIEnv *jenv, jobject cls, jint kid)
	{
	return clReleaseKernel((cl_kernel)kid);
	}


JNIEXPORT jint JNICALL Java_javax_opencl_CLKernel__1setKernelArg4(JNIEnv *jenv, jobject cls, jint kid, jint index, jint value)
	{
	cl_int setval=value;
	cl_int ret=clSetKernelArg ((cl_kernel)kid, index, sizeof(cl_int), &setval);
	return ret;
	}


/////////////////////// context ////////////////////

cl_context getContext(JNIEnv *env, jobject cls)
	{
	jfieldID fid=(*env)->GetFieldID(env, cls, "id",  "I");
	return (cl_context)(*env)->GetIntField(env, cls, fid);
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLContext__1createContext(JNIEnv *jenv, jobject cls, jint deviceType)
	{
	cl_int ret;
	cl_context context = clCreateContextFromType(0, deviceType, NULL, NULL, &ret);
	jfieldID fid=(*jenv)->GetFieldID(jenv, cls, "id",  "I");
	(*jenv)->SetIntField(jenv, cls, fid, (jint)context);
	return ret;
	}


JNIEXPORT jint JNICALL Java_javax_opencl_CLContext__1retainContext(JNIEnv *jenv, jobject cls, jint cid)
	{
	return clRetainContext((cl_context)cid);
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLContext__1releaseContext(JNIEnv *jenv, jobject cls, jint cid)
	{
	return clReleaseContext((cl_context)cid);
	}



JNIEXPORT jintArray JNICALL Java_javax_opencl_CLContext__1getContextInfoDevices(JNIEnv *jenv, jobject cls, jint cid)
	{
	cl_context context=(cl_context)cid;

	size_t cb;
	clGetContextInfo(context, CL_CONTEXT_DEVICES, 0, NULL, &cb);
	cl_device_id *devices = malloc(cb);
	clGetContextInfo(context, CL_CONTEXT_DEVICES, cb, devices, NULL);
	int numElem=cb/sizeof(cl_device_id);

	jintArray ret=(jintArray)(*jenv)->NewIntArray(jenv, numElem);
	for(int i=0;i<numElem;i++)
		{
		jint foo=(jint)devices[i]; //Stew value into an integer
		(*jenv)->SetIntArrayRegion(jenv,ret,i,1,&foo);
		}
	free(devices);
	return ret;
	}


/////////////////////// mem ////////////////////

cl_mem getMem(JNIEnv *env, jobject cls)
	{
	jfieldID fid=(*env)->GetFieldID(env, cls, "id",  "I");
	return (cl_mem)(*env)->GetIntField(env, cls, fid);
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLMem__1createBufferFromInt(JNIEnv *jenv, jobject cls, jint cid, jint memFlags, jintArray initData)
	{
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLMem__1createBuffer(JNIEnv *jenv, jobject cls, jint cid, jint memFlags, jint size)
	{
	cl_int ret;
	cl_mem mid=clCreateBuffer((cl_context)cid, memFlags,size,NULL,&ret);
	jfieldID fid=(*jenv)->GetFieldID(jenv, cls, "id",  "I");
	(*jenv)->SetIntField(jenv, cls, fid, (jint)mid);
	return ret;
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLMem__1retainMem(JNIEnv *jenv, jobject cls, jint mid)
	{
	return clRetainMemObject((cl_mem)mid);
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLMem__1releaseMem(JNIEnv *jenv, jobject cls, jint mid)
	{
	return clReleaseMemObject((cl_mem)mid);
	}





/////////////////// program //////////////////////


JNIEXPORT jint JNICALL Java_javax_opencl_CLProgram__1createProgram
  (JNIEnv *jenv, jobject cls, jint cid, jstring src)
	{
	cl_int ret;
	const jbyte *str = (*jenv)->GetStringUTFChars( jenv, src, NULL );
	cl_program pid=clCreateProgramWithSource((cl_context)cid,1,(const char **)str,NULL,&ret);
	(*jenv)->ReleaseStringUTFChars(jenv, src, str);
	jfieldID fid=(*jenv)->GetFieldID(jenv, cls, "id",  "I");
	(*jenv)->SetIntField(jenv, cls, fid, (jint)pid);
	return ret;
	}

////////////////////// cq ////////////////////////////

JNIEXPORT jint JNICALL Java_javax_opencl_CLCommandQueue__1createCommandQueue(JNIEnv *jenv, jobject cls, jint cid, jint deviceID)
	{
	cl_int ret;
	cl_command_queue cqid=clCreateCommandQueue((cl_context)cid, (cl_device_id)deviceID, 0, &ret);
	jfieldID fid=(*jenv)->GetFieldID(jenv, cls, "id",  "I");
	(*jenv)->SetIntField(jenv, cls, fid, (jint)cqid);
	return ret;
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLCommandQueue__1retain
  (JNIEnv *jenv, jobject cls, jint qid)
	{
	return clRetainCommandQueue((cl_command_queue)qid);
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLCommandQueue__1release
  (JNIEnv *jenv, jobject cls, jint qid)
	{
	return clReleaseCommandQueue((cl_command_queue)qid);
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLCommandQueue__1flush
  (JNIEnv *jenv, jobject cls, jint qid)
	{
	return clFlush((cl_command_queue)qid);
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLCommandQueue__1finish(JNIEnv *jenv, jobject cls, jint qID)
	{
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLCommandQueue__1enqueueReadBuffer
  (JNIEnv *jenv, jobject cls, jint qID, jint mid, jboolean blocking, jint offset, jint numElem, jintArray buffer, jintArray waitforID)
	{
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLCommandQueue__1enqueueNDRangeKernel
  (JNIEnv *jenv, jobject cls, jint cqid, jint kid, jint numDim, jintArray globalOffset, jintArray globalSize, jintArray localSize, jintArray waitforID)
	{
	}






















/*
extern  cl_int 
clEnqueueNDRangeKernel(cl_command_queue command_queue,
                      cl_kernel        kernel,
                      cl_uint          work_dim,
                      const size_t *   global_work_offset,
                      const size_t *   global_work_size,
                      const size_t *   local_work_size,
                      cl_uint          num_events_in_wait_list,
                      const cl_event * event_wait_list,
                      cl_event *       event) ;
*/


	//jsize devslen = (*env)->GetArrayLength(env, devs);
//	jint *devsbody = (*env)->GetIntArrayElements(env, devsarr, 0);
//	(*env)->ReleaseIntArrayElements(env, devsarr, devsbody, 0);
