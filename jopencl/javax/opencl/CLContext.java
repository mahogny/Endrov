package javax.opencl;

/**
 * OpenCL context
 * 
 * @author Johan Henriksson
 *
 */
public class CLContext extends OpenCL
	{
	//Store context id here, int?
	
	int context;
	
	/**
	 * Create an OpenCL context
	 * @param deviceType Any of CL_DEVICE_TYPE_*
	 */
	protected CLContext(int deviceType)
		{
		int ret=_createContext(deviceType);
		assertSuccess(ret);
		}
	
	
	public void retain()
		{
		int ret=_retainContext();
		assertSuccess(ret);
		}
	
	public void release()
		{
		int ret=_releaseContext();
		assertSuccess(ret);
		}

	private native int _createContext(int deviceType) ;
	private native int _retainContext();
	private native int _releaseContext();
	

	
	
	/*
	
	extern  cl_context 
	clCreateContext(cl_context_properties * properties,
	                cl_uint                 num_devices,
	                const cl_device_id *    devices,
	                void (*pfn_notify)(const char *, const void *, size_t, void *), 
	                void *                  user_data,
	                cl_int *                errcode_ret) ;

	extern  cl_context 
	clCreateContextFromType(cl_context_properties * properties,
	                        cl_device_type          device_type,
	                        void (*pfn_notify)(const char *, const void *, size_t, void *) ,
	                        void *                  user_data,
	                        cl_int *                errcode_ret) ;

*/
	
	public int[] getContextDevices()
		{
		int[] d=_getContextDevices();
		if(d==null)
			throw new CLException();
		return d;
		}
	
	private native int[] _getContextDevices();
	
	/**
	 *                      cl_uint                 Return the context reference count.
CL_CONTEXT_REFERENCE_
COUNT6
                    cl_device_id[]          Return the list of devices in context.
CL_CONTEXT_DEVICES
                    cl_context_properties[] Return the properties argument
CL_CONTEXT_PROPERTIES
                                            specified in clCreateContext.

	 */
	/*

	extern  cl_int 
	clGetContextInfo(cl_context         context, 
	                 cl_context_info    param_name, 
	                 size_t             param_value_size, 
	                 void *             param_value, 
	                 size_t *           param_value_size_ret) ;
*/
	
	
	
	
	public CLCommandQueue createCommandQueue(int deviceID)
		{
		return new CLCommandQueue(this, deviceID);
		}
	
	
	}
