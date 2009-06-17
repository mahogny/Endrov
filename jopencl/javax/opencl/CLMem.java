package javax.opencl;

/**
 * OpenCL memory object
 * 
 * @author Johan Henriksson
 *
 */
public class CLMem extends OpenCL
	{
	int mem;
	
	
/*

//Memory Object APIs
extern  cl_mem 
clCreateBuffer(cl_context   context,
              cl_mem_flags flags,
              size_t       size,
              void *       host_ptr,
              cl_int *     errcode_ret) ;

extern  cl_mem 
clCreateImage2D(cl_context              context,
               cl_mem_flags            flags,
               const cl_image_format * image_format,
               size_t                  image_width,
               size_t                  image_height,
               size_t                  image_row_pitch, 
               void *                  host_ptr,
               cl_int *                errcode_ret) ;
                       
extern  cl_mem 
clCreateImage3D(cl_context              context,
               cl_mem_flags            flags,
               const cl_image_format * image_format,
               size_t                  image_width, 
               size_t                  image_height,
               size_t                  image_depth, 
               size_t                  image_row_pitch, 
               size_t                  image_slice_pitch, 
               void *                  host_ptr,
               cl_int *                errcode_ret) ;
                       
extern  cl_int 
clGetSupportedImageFormats(cl_context           context,
                          cl_mem_flags         flags,
                          cl_mem_object_type   image_type,
                          cl_uint              num_entries,
                          cl_image_format *    image_formats,
                          cl_uint *            num_image_formats) ;
                                   
extern  cl_int 
clGetMemObjectInfo(cl_mem           memobj,
                  cl_mem_info      param_name, 
                  size_t           param_value_size,
                  void *           param_value,
                  size_t *         param_value_size_ret) ;

extern  cl_int 
clGetImageInfo(cl_mem           image,
              cl_image_info    param_name, 
              size_t           param_value_size,
              void *           param_value,
              size_t *         param_value_size_ret) ;



*/
	
	
	public void retain()
		{
		int ret=_retainMem();
		assertSuccess(ret);
		}
	
	public void release()
		{
		int ret=_releaseMem();
		assertSuccess(ret);
		}
	
	
	
	private native int _retainMem();
	private native int _releaseMem();
	
	}
