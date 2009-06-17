package javax.opencl;

public class CLCommandQueue extends OpenCL
	{

	public CLCommandQueue(CLContext context, int deviceID)
		{
		int ret=_createCommandQueue(context.context, deviceID);
		assertSuccess(ret);
		}

	private native int _createCommandQueue(int context, int deviceID);
	
	/*
	//Command Queue APIs
	extern  cl_command_queue 
	clCreateCommandQueue(cl_context                     context, 
	                    cl_device_id                   device, 
	                    cl_command_queue_properties    properties,
	                    cl_int *                       errcode_ret) ;
	*/
	
	/*
	 * 
extern  cl_int 
clRetainCommandQueue(cl_command_queue command_queue) ;

extern  cl_int 
clReleaseCommandQueue(cl_command_queue command_queue) ;

extern  cl_int 
clGetCommandQueueInfo(cl_command_queue      command_queue,
                      cl_command_queue_info param_name,
                      size_t                param_value_size,
                      void *                param_value,
                      size_t *              param_value_size_ret) ;

extern  cl_int 
clSetCommandQueueProperty(cl_command_queue              command_queue,
                          cl_command_queue_properties   properties, 
                          cl_bool                        enable,
                          cl_command_queue_properties * old_properties) ;



	 */
	
  /*
  
//Flush and Finish APIs
extern  cl_int 
clFlush(cl_command_queue command_queue) ;

extern  cl_int 
clFinish(cl_command_queue command_queue) ;







//Enqueued Commands APIs
extern  cl_int 
clEnqueueReadBuffer(cl_command_queue    command_queue,
                   cl_mem              buffer,
                   cl_bool             blocking_read,
                   size_t              offset,
                   size_t              cb, 
                   void *              ptr,
                   cl_uint             num_events_in_wait_list,
                   const cl_event *    event_wait_list,
                   cl_event *          event) ;
                           
extern  cl_int 
clEnqueueWriteBuffer(cl_command_queue   command_queue, 
                    cl_mem             buffer, 
                    cl_bool            blocking_write, 
                    size_t             offset, 
                    size_t             cb, 
                    const void *       ptr, 
                    cl_uint            num_events_in_wait_list, 
                    const cl_event *   event_wait_list, 
                    cl_event *         event) ;
                           
extern  cl_int 
clEnqueueCopyBuffer(cl_command_queue    command_queue, 
                   cl_mem              src_buffer,
                   cl_mem              dst_buffer, 
                   size_t              src_offset,
                   size_t              dst_offset,
                   size_t              cb, 
                   cl_uint             num_events_in_wait_list,
                   const cl_event *    event_wait_list,
                   cl_event *          event) ;
                           
extern  cl_int 
clEnqueueReadImage(cl_command_queue     command_queue,
                  cl_mem               image,
                  cl_bool              blocking_read, 
                  const size_t *       origin[3],
                  const size_t *       region[3],
                  size_t               row_pitch,
                  size_t               slice_pitch, 
                  void *               ptr,
                  cl_uint              num_events_in_wait_list,
                  const cl_event *     event_wait_list,
                  cl_event *           event) ;

extern  cl_int 
clEnqueueWriteImage(cl_command_queue    command_queue,
                   cl_mem              image,
                   cl_bool             blocking_write, 
                   const size_t *      origin[3],
                   const size_t *      region[3],
                   size_t              input_row_pitch,
                   size_t              input_slice_pitch, 
                   const void *        ptr,
                   cl_uint             num_events_in_wait_list,
                   const cl_event *    event_wait_list,
                   cl_event *          event) ;

extern  cl_int 
clEnqueueCopyImage(cl_command_queue     command_queue,
                  cl_mem               src_image,
                  cl_mem               dst_image, 
                  const size_t *       src_origin[3],
                  const size_t *       dst_origin[3],
                  const size_t *       region[3], 
                  cl_uint              num_events_in_wait_list,
                  const cl_event *     event_wait_list,
                  cl_event *           event) ;

extern  cl_int 
clEnqueueCopyImageToBuffer(cl_command_queue command_queue,
                          cl_mem           src_image,
                          cl_mem           dst_buffer, 
                          const size_t *   src_origin[3],
                          const size_t *   region[3], 
                          size_t           dst_offset,
                          cl_uint          num_events_in_wait_list,
                          const cl_event * event_wait_list,
                          cl_event *       event) ;

extern  cl_int 
clEnqueueCopyBufferToImage(cl_command_queue command_queue,
                          cl_mem           src_buffer,
                          cl_mem           dst_image, 
                          size_t           src_offset,
                          const size_t *   dst_origin[3],
                          const size_t *   region[3], 
                          cl_uint          num_events_in_wait_list,
                          const cl_event * event_wait_list,
                          cl_event *       event) ;

extern  void * 
clEnqueueMapBuffer(cl_command_queue command_queue,
                  cl_mem           buffer,
                  cl_bool          blocking_map, 
                  cl_map_flags     map_flags,
                  size_t           offset,
                  size_t           cb,
                  cl_uint          num_events_in_wait_list,
                  const cl_event * event_wait_list,
                  cl_event *       event,
                  cl_int *         errcode_ret) ;

extern  void * 
clEnqueueMapImage(cl_command_queue  command_queue,
                 cl_mem            image, 
                 cl_bool           blocking_map, 
                 cl_map_flags      map_flags, 
                 const size_t *    origin[3],
                 const size_t *    region[3],
                 size_t *          image_row_pitch,
                 size_t *          image_slice_pitch,
                 cl_uint           num_events_in_wait_list,
                 const cl_event *  event_wait_list,
                 cl_event *        event,
                 cl_int *          errcode_ret) ;

extern  cl_int 
clEnqueueUnmapMemObject(cl_command_queue command_queue,
                       cl_mem           memobj,
                       void *           mapped_ptr,
                       cl_uint          num_events_in_wait_list,
                       const cl_event *  event_wait_list,
                       cl_event *        event) ;

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

extern  cl_int 
clEnqueueTask(cl_command_queue  command_queue,
             cl_kernel         kernel,
             cl_uint           num_events_in_wait_list,
             const cl_event *  event_wait_list,
             cl_event *        event) ;

extern  cl_int 
clEnqueueNativeKernel(cl_command_queue  command_queue,
					  void (*user_func)(void *), 
                     void *            args,
                     size_t            cb_args, 
                     cl_uint           num_mem_objects,
                     const cl_mem *    mem_list,
                     const void **     args_mem_loc,
                     cl_uint           num_events_in_wait_list,
                     const cl_event *  event_wait_list,
                     cl_event *        event) ;

extern  cl_int 
clEnqueueMarker(cl_command_queue    command_queue,
               cl_event *          event) ;

extern  cl_int 
clEnqueueWaitForEvents(cl_command_queue command_queue,
                      cl_uint          num_events,
                      const cl_event * event_list) ;

extern  cl_int 
clEnqueueBarrier(cl_command_queue command_queue) ;
*/
	}
