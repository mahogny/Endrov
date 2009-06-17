package javax.opencl;

/**
 * OpenCL Exception
 * @author Johan Henriksson
 *
 */
public class CLException extends RuntimeException
	{
	private static final long serialVersionUID = 1L;
	
	private final int ret;
	
	
	public CLException(int ret)
		{
		this.ret=ret;
		}
	
	public CLException()
		{
		this.ret=666;
		}
	
	public int getCode()
		{
		return ret;
		}
	
	public String toString()
		{
		return "OpenCL error, code "+ret;
		}
	}
