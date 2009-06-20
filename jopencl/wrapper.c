#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <CL/cl.h>
#include <jni.h>

#include "javax_opencl_CLCommandQueue.h"
#include "javax_opencl_CLContext.h"
#include "javax_opencl_CLDevice.h"
#include "javax_opencl_CLKernel.h"
#include "javax_opencl_CLMem.h"
#include "javax_opencl_CLPlatform.h"
#include "javax_opencl_CLProgram.h"

#define GETCLASS jclass cls=(*jenv)->GetObjectClass(jenv, jobj);
#define SETID(X) {jfieldID fid=(*jenv)->GetFieldID(jenv, cls, "id",  "I"); (*jenv)->SetIntField(jenv, jobj, fid, (jint)X);}

//I believe it is safe to assume cl_ scalar values==j*. Pointers might be different.


/////////////////////// device ////////////////////

JNIEXPORT jstring JNICALL Java_javax_opencl_CLDevice__1getDeviceInfoString
  (JNIEnv *jenv, jobject jobj, jint did, jint param_name)
	{
	size_t size;
	clGetDeviceInfo((cl_device_id)did, param_name, 0, NULL, &size);
	char *str=malloc(size);
	clGetDeviceInfo((cl_device_id)did, param_name, size, str, NULL);
	jstring ret=(*jenv)->NewStringUTF(jenv, str);
	free(str);
	return ret;
	}



/////////////////////// platform ////////////////////

JNIEXPORT jintArray JNICALL Java_javax_opencl_OpenCL__1getPlatforms(JNIEnv *jenv, jclass cls)
	{
	cl_uint numPlatforms;
	clGetPlatformIDs(0,NULL,&numPlatforms);
	cl_platform_id *platforms=malloc(sizeof(cl_platform_id)*numPlatforms);
	clGetPlatformIDs(numPlatforms,platforms,&numPlatforms);
	jintArray ret=(jintArray)(*jenv)->NewIntArray(jenv, numPlatforms);
	for(int i=0;i<numPlatforms;i++)
		{
		jint foo=(jint)platforms[i]; //Stew value into an integer
		(*jenv)->SetIntArrayRegion(jenv,ret,i,1,&foo);
		}
	free(platforms);
	return ret;
	}


JNIEXPORT jstring JNICALL Java_javax_opencl_CLPlatform__1getPlatformInfoString
  (JNIEnv *jenv, jobject jobj, jint plid, jint param_name)
	{
	size_t size;
	clGetPlatformInfo((cl_platform_id)plid, param_name, 0, NULL, &size);
	char *str=malloc(size);
	clGetPlatformInfo((cl_platform_id)plid, param_name, size, str, NULL);
	jstring ret=(*jenv)->NewStringUTF(jenv, str);
	free(str);
	return ret;
	}


/////////////////////// kernel ////////////////////


JNIEXPORT jint JNICALL Java_javax_opencl_CLKernel__1createKernel(JNIEnv *jenv, jobject jobj, jint pid, jstring name)
	{
	GETCLASS
	const jbyte *str = (*jenv)->GetStringUTFChars( jenv, name, NULL );
	//printf("-%s-\n",str);
	cl_int ret;
	cl_kernel kid=clCreateKernel ((cl_program)pid, str, &ret);
	(*jenv)->ReleaseStringUTFChars(jenv, name, str);
	SETID(kid)
//Java_javax_opencl_CLKernel__1getKernelNumArgs(jenv,jobj,kid);
	return ret;
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLKernel__1getKernelNumArgs(JNIEnv *jenv, jobject jobj, jint kid)
	{
	cl_uint args;
	cl_int ret=clGetKernelInfo ((cl_kernel)kid,CL_KERNEL_NUM_ARGS,sizeof(args),&args,NULL);
	if(ret!=CL_SUCCESS)
		printf("Error calling getKernelInfo, %d\n",ret);
	return args;
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLKernel__1getRefCount(JNIEnv *jenv, jobject jobj, jint kid)
	{
	cl_uint args;
	cl_int ret=clGetKernelInfo ((cl_kernel)kid,CL_KERNEL_REFERENCE_COUNT,sizeof(args),&args,NULL);
	if(ret!=CL_SUCCESS)
		printf("Error calling getKernelInfo, %d\n",ret);
	return args;
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLKernel__1retainKernel(JNIEnv *jenv, jobject jobj, jint kid)
	{
	return clRetainKernel((cl_kernel)kid);
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLKernel__1releaseKernel(JNIEnv *jenv, jobject jobj, jint kid)
	{
	return clReleaseKernel((cl_kernel)kid);
	}


JNIEXPORT jint JNICALL Java_javax_opencl_CLKernel__1setKernelArg4
  (JNIEnv *jenv, jobject jobj, jint kid, jint index, jint value)
	{
	cl_int setval=value;
	cl_int ret=clSetKernelArg ((cl_kernel)kid, index, sizeof(cl_int), &setval);
	return ret;
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLKernel__1setKernelArgNull
  (JNIEnv *jenv, jobject jobj, jint kid, jint index, jint size)
	{
	cl_int ret=clSetKernelArg ((cl_kernel)kid, index, size, NULL);
	return ret;
	}



/////////////////////// context ////////////////////

JNIEXPORT jint JNICALL Java_javax_opencl_CLContext__1createContext(JNIEnv *jenv, jobject jobj, jint deviceType)
	{
	GETCLASS
	cl_int ret;
	cl_context context = clCreateContextFromType(0, deviceType, NULL, NULL, &ret);
	SETID(context)
	return ret;
	}


JNIEXPORT jint JNICALL Java_javax_opencl_CLContext__1retainContext(JNIEnv *jenv, jobject jobj, jint cid)
	{
	return clRetainContext((cl_context)cid);
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLContext__1releaseContext(JNIEnv *jenv, jobject jobj, jint cid)
	{
	return clReleaseContext((cl_context)cid);
	}



JNIEXPORT jintArray JNICALL Java_javax_opencl_CLContext__1getContextInfoDevices(JNIEnv *jenv, jobject jobj, jint cid)
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


JNIEXPORT jint JNICALL Java_javax_opencl_CLMem__1createBufferFromInt(JNIEnv *jenv, jobject jobj, jint cid, jint memFlags, jintArray initData)
	{
	GETCLASS
	cl_int ret;
	jsize alen = (*jenv)->GetArrayLength(jenv, initData);
	jint *abody = (*jenv)->GetIntArrayElements(jenv, initData, 0);
	//ASSUMING!!! size of jint same as cl_int
	cl_mem mid=clCreateBuffer((cl_context)cid, memFlags,sizeof(jint)*alen,abody,&ret);
	(*jenv)->ReleaseIntArrayElements(jenv, initData, abody, 0);
	SETID(mid)
	return ret;
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLMem__1createBufferFromFloat(JNIEnv *jenv, jobject jobj, jint cid, jint memFlags, jfloatArray initData)
	{
	GETCLASS
	cl_int ret;
	jsize alen = (*jenv)->GetArrayLength(jenv, initData);
	jfloat *abody = (*jenv)->GetFloatArrayElements(jenv, initData, 0);
	cl_mem mid=clCreateBuffer((cl_context)cid, memFlags,sizeof(jfloat)*alen,abody,&ret);
	(*jenv)->ReleaseFloatArrayElements(jenv, initData, abody, 0);
	SETID(mid)
	return ret;
	}



JNIEXPORT jint JNICALL Java_javax_opencl_CLMem__1createBuffer(JNIEnv *jenv, jobject jobj, jint cid, jint memFlags, jint size)
	{
	GETCLASS
	cl_int ret;
	cl_mem mid=clCreateBuffer((cl_context)cid, memFlags,size,NULL,&ret);
	SETID(mid)
	return ret;
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLMem__1getSize(JNIEnv *jenv, jobject jobj, jint mid)
	{
	size_t size;
	int ret=clGetMemObjectInfo((cl_mem)mid,CL_MEM_SIZE,sizeof(size), &size, NULL);
	if(ret!=CL_SUCCESS)
		printf("Error calling getMemObjectInfo, %d\n",ret);
	return size;
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLMem__1retainMem(JNIEnv *jenv, jobject jobj, jint mid)
	{
	return clRetainMemObject((cl_mem)mid);
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLMem__1releaseMem(JNIEnv *jenv, jobject jobj, jint mid)
	{
	return clReleaseMemObject((cl_mem)mid);
	}





/////////////////// program //////////////////////


JNIEXPORT jint JNICALL Java_javax_opencl_CLProgram__1createProgram
  (JNIEnv *jenv, jobject jobj, jint cid, jstring src)
	{
	GETCLASS
	cl_int ret;
	const jbyte *str = (*jenv)->GetStringUTFChars( jenv, src, NULL );
	cl_program pid=clCreateProgramWithSource((cl_context)cid,1,(const char **)&str,NULL,&ret);
	(*jenv)->ReleaseStringUTFChars(jenv, src, str);
	SETID(pid)
	return ret;
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLProgram__1build
  (JNIEnv *jenv, jobject jobj, jint pid)
	{
	cl_int ret=clBuildProgram((cl_program)pid,0,NULL,NULL, NULL, NULL);
	return ret;
	}


JNIEXPORT jint JNICALL Java_javax_opencl_CLProgram__1getBuildStatus(JNIEnv *jenv, jobject jobj, jint pid, jint did)
	{
	cl_build_status size;
	int ret=clGetProgramBuildInfo((cl_program)pid,(cl_device_id)did,
		CL_PROGRAM_BUILD_STATUS,sizeof(size), &size, NULL);
	if(ret!=CL_SUCCESS)
		printf("Error calling getProgramInfo, %d\n",ret);
	return size;
	}


JNIEXPORT jint JNICALL Java_javax_opencl_CLProgram__1getNumDevices(JNIEnv *jenv, jobject jobj, jint pid)
	{
	cl_uint size;
	int ret=clGetProgramInfo((cl_program)pid,CL_PROGRAM_NUM_DEVICES,sizeof(size), &size, NULL);
	if(ret!=CL_SUCCESS)
		printf("Error calling getProgramInfo, %d\n",ret);
	return size;
	}


JNIEXPORT jint JNICALL Java_javax_opencl_CLProgram__1retain
  (JNIEnv *jenv, jobject jobj, jint pid)
	{
	return clRetainProgram((cl_program)pid);
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLProgram__1release
  (JNIEnv *jenv, jobject jobj, jint pid)
	{
	return clReleaseProgram((cl_program)pid);
	}


////////////////////// cq ////////////////////////////


cl_event *getWaitList(JNIEnv *jenv, jobject jobj, jintArray waitforID, cl_uint *len)
	{
	jsize alen = (*jenv)->GetArrayLength(jenv, waitforID);
	jint *abody = (*jenv)->GetIntArrayElements(jenv, waitforID, 0);
	if(alen==0)
		{
		(*len)=0;
		return NULL;
		}
	cl_event *events=malloc(sizeof(cl_event)*alen);
	for(int i=0;i<alen;i++)
		events[i]=(cl_event)abody[i];
	(*jenv)->ReleaseIntArrayElements(jenv, waitforID, abody, 0);
	(*len)=alen;
	return events;
	}


JNIEXPORT jint JNICALL Java_javax_opencl_CLCommandQueue__1createCommandQueue(JNIEnv *jenv, jobject jobj, jint cid, jint deviceID)
	{
	GETCLASS
	cl_int ret;
	cl_command_queue cqid=clCreateCommandQueue((cl_context)cid, (cl_device_id)deviceID, 0, &ret);
	SETID(cqid)
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLCommandQueue__1retain
  (JNIEnv *jenv, jobject jobj, jint qid)
	{
	return clRetainCommandQueue((cl_command_queue)qid);
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLCommandQueue__1release
  (JNIEnv *jenv, jobject jobj, jint qid)
	{
	return clReleaseCommandQueue((cl_command_queue)qid);
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLCommandQueue__1flush
  (JNIEnv *jenv, jobject jobj, jint qid)
	{
	return clFlush((cl_command_queue)qid);
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLCommandQueue__1finish(JNIEnv *jenv, jobject jobj, jint qid)
	{
	return clFinish((cl_command_queue)qid);
	}

cl_bool bool2cl(jboolean b)
	{
	return b;
	}


JNIEXPORT jint JNICALL Java_javax_opencl_CLCommandQueue__1enqueueBarrier
  (JNIEnv *jenv, jobject jobj, jint qid)
	{
	return clEnqueueBarrier((cl_command_queue)qid);
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLCommandQueue__1enqueueReadBufferInt
  (JNIEnv *jenv, jobject jobj, jint cqid, jint mid, jboolean blocking, jint offset, jint numElem, jintArray buffer, jintArray waitforID)
	{
	cl_uint waitlistlen=0;
	cl_event *waitlist=NULL;
	if(waitforID) waitlist=getWaitList(jenv,jobj,waitforID,&waitlistlen);

	jsize alen = (*jenv)->GetArrayLength(jenv, buffer);
	jint *abody = (*jenv)->GetIntArrayElements(jenv, buffer, 0);
	int ret=clEnqueueReadBuffer((cl_command_queue)cqid, (cl_mem)mid, bool2cl(blocking), offset, sizeof(jint)*alen, abody, waitlistlen,waitlist,NULL);

	(*jenv)->ReleaseIntArrayElements(jenv, buffer, abody, 0);

	free(waitlist);
	return ret;
	}

JNIEXPORT jint JNICALL Java_javax_opencl_CLCommandQueue__1enqueueReadBufferFloat
  (JNIEnv *jenv, jobject jobj, jint cqid, jint mid, jboolean blocking, jint offset, jint numElem, jintArray buffer, jfloatArray waitforID)
	{
	cl_uint waitlistlen=0;
	cl_event *waitlist=NULL;
	if(waitforID) waitlist=getWaitList(jenv,jobj,waitforID,&waitlistlen);

	jsize alen = (*jenv)->GetArrayLength(jenv, buffer);
	jfloat *abody = (*jenv)->GetFloatArrayElements(jenv, buffer, 0);
	int ret=clEnqueueReadBuffer((cl_command_queue)cqid, (cl_mem)mid, bool2cl(blocking), offset, sizeof(jfloat)*alen, abody, waitlistlen,waitlist,NULL);

	(*jenv)->ReleaseFloatArrayElements(jenv, buffer, abody, 0);

	free(waitlist);
	return ret;
	}



size_t *getSizetArray(JNIEnv *jenv, jobject jobj, jintArray list)
	{
	jsize alen = (*jenv)->GetArrayLength(jenv, list);
	jint *abody = (*jenv)->GetIntArrayElements(jenv, list, 0);
	size_t *out=malloc(sizeof(size_t)*alen);
	for(int i=0;i<alen;i++)
		out[i]=abody[i];
	(*jenv)->ReleaseIntArrayElements(jenv, list, abody, 0);
	//(*len)=alen;
	return out;
	}


JNIEXPORT jint JNICALL Java_javax_opencl_CLCommandQueue__1enqueueNDRangeKernel
  (JNIEnv *jenv, jobject jobj, jint cqid, jint kid, jint workDim, jintArray globalOffset, jintArray globalSize,
  jintArray localSize, jintArray waitforID)
	{
	cl_uint waitlistlen=0;
	cl_event *waitlist=NULL;
	if(waitforID) waitlist=getWaitList(jenv,jobj,waitforID,&waitlistlen);

	size_t *global_work_offset=NULL;
	size_t *global_work_size=NULL;
	size_t *local_work_size=NULL;
	if(globalOffset) global_work_offset=getSizetArray(jenv,jobj,globalOffset);
	if(globalSize) global_work_size=getSizetArray(jenv,jobj,globalSize);
	if(localSize) local_work_size=getSizetArray(jenv,jobj,localSize);
	cl_int ret=clEnqueueNDRangeKernel((cl_command_queue)cqid, (cl_kernel)kid,
			workDim, global_work_offset,global_work_size,local_work_size,
			waitlistlen,waitlist, NULL) ;
	free(waitlist);
	free(global_work_offset);
	free(global_work_size);
	free(local_work_size);

	return ret;
	}







/*
cl_kernel getKernel(JNIEnv *env, jobject jobj)
	{
	jfieldID fid=(*env)->GetFieldID(env, jobj, "id",  "I");
	return (cl_kernel)(*env)->GetIntField(env, jobj, fid);
	}
*/
