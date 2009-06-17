package javax.opencl;


public class CLKernel extends OpenCL
	{
	int kernel;
	
	/**
	 * Can only be created through Program
	 */
	CLKernel(){};
	
	
	
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
		int ret=_retainKernel();
		assertSuccess(ret);
		}
	
	public void releaseKernel()
		{
		int ret=_releaseKernel();
		assertSuccess(ret);
		}
	
	
	
	private native int _retainKernel();
	private native int _releaseKernel();

	
	
	
	/*
extern  cl_int 
clSetKernelArg(cl_kernel    kernel,
              cl_uint      arg_index,
              size_t       arg_size,
              const void * arg_value) ;

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
