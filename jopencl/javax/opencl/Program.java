package javax.opencl;

public class Program
	{
	

	
	//public Kernel createKernel(String kernelName) throws RuntimeException;
	
  /*
//Kernel Object APIs
extern  cl_kernel 
clCreateKernel(cl_program      program,
              const char *    kernel_name,
              cl_int *        errcode_ret) ;
*/
	
	/*
  
//Program Object APIs
extern  cl_program 
clCreateProgramWithSource(cl_context        context,
                         cl_uint           count,
                         const char **     strings,
                         const size_t *    lengths,
                         cl_int *          errcode_ret) ;

extern  cl_program 
clCreateProgramWithBinary(cl_context                     context,
                         cl_uint                        num_devices,
                         const cl_device_id *           device_list,
                         const size_t *                 lengths,
                         const unsigned char **         binaries,
                         cl_int *                       binary_status,
                         cl_int *                       errcode_ret) ;

extern  cl_int 
clRetainProgram(cl_program program) ;

extern  cl_int 
clReleaseProgram(cl_program program) ;

extern  cl_int 
clBuildProgram(cl_program           program,
              cl_uint              num_devices,
              const cl_device_id * device_list,
              const char *         options, 
              void (*pfn_notify)(cl_program program, void * user_data),
              void *               user_data) ;

extern  cl_int 
clUnloadCompiler(void) ;

extern  cl_int 
clGetProgramInfo(cl_program         program,
                cl_program_info    param_name,
                size_t             param_value_size,
                void *             param_value,
                size_t *           param_value_size_ret) ;

extern  cl_int 
clGetProgramBuildInfo(cl_program            program,
                     cl_device_id          device,
                     cl_program_build_info param_name,
                     size_t                param_value_size,
                     void *                param_value,
                     size_t *              param_value_size_ret) ;
                       
                       
                       
*/

	}
