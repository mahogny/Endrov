package javax.opencl;


/**
 * OpenCL Kernel
 * @author Johan Henriksson
 *
 */
public class CLKernel extends OpenCL
	{
	int id;
	
	/**
	 * Can only be created through Program
	 */
	CLKernel(CLProgram p, String kernelName)
		{
		int ret=_createKernel(p.id, kernelName);
		assertSuccess(ret);
		}
	
	
	private native int _createKernel(int pid, String kernelName);

	
	//Maybe not needed
	/*
extern  cl_int 
clCreateKernelsInProgram(cl_program     program,
                        cl_uint        num_kernels,
                        cl_kernel *    kernels,
                        cl_uint *      num_kernels_ret) ;
*/
	
	

	public void retainKernel()
		{
		int ret=_retainKernel(id);
		assertSuccess(ret);
		}
	
	public void releaseKernel()
		{
		int ret=_releaseKernel(id);
		assertSuccess(ret);
		}
	
	
	
	private native int _retainKernel(int id);
	private native int _releaseKernel(int id);


	public void setKernelArg(int index, CLMem mem)
		{
		int ret=_setKernelArg4(id, index, mem.id);
		assertSuccess(ret);
		}
	
	
	private native int _setKernelArg4(int kernelID, int index, int value);
	
	/*

extern  cl_int 
clGetKernelInfo(cl_kernel       kernel,
               cl_kernel_info  param_name,
               size_t          param_value_size,
               void *          param_value,
               size_t *        param_value_size_ret) ;

extern  cl_int 
clGetKernelWorkGroupInfo(cl_kernel                  kernel,
                        cl_device_id               device,
                        cl_kernel_work_group_info  param_name,
                        size_t                     param_value_size,
                        void *                     param_value,
                        size_t *                   param_value_size_ret) ;

*/
	
	
	
	}
