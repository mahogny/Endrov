package javax.opencl;

/**
 * OpenCL memory object
 * 
 * @author Johan Henriksson
 *
 */
public class CLMem extends OpenCL
	{
	int id;
	
	final CLContext context;
	
	//int elementSize;
	
	CLMem(CLContext context, int memFlags, int[] initData)
		{
		this.context=context;
		int ret=_createBufferFromInt(context.id, memFlags, initData);
		if(ret!=CL_SUCCESS)
			throw new CLException(ret);
		}
		
	CLMem(CLContext context, int memFlags, Class<?> c, int numElem)
		{
		this.context=context;
		int elsize;
		if(c==Integer.class)
			elsize=4;
		else
			throw new CLException("Unsupported memory type");
	//	elementSize=elsize;
		
		int ret=_createBuffer(context.id, memFlags, elsize*numElem);
		if(ret!=CL_SUCCESS)
			throw new CLException(ret);
		}
	
	
	private native int _createBufferFromInt(int contextID, int memFlags, int[] initData);

	private native int _createBuffer(int contextID, int memFlags, int size);

	
	
	
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
		int ret=_retainMem(id);
		assertSuccess(ret);
		}
	
	public void release()
		{
		int ret=_releaseMem(id);
		assertSuccess(ret);
		}
	
	
	
	private native int _retainMem(int mid);
	private native int _releaseMem(int mid);
	
	}
