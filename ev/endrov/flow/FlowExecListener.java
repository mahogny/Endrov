package endrov.flow;

/**
 * A connection to the outside using input/output objects
 */
public interface FlowExecListener
	{
	public Object getInputObject(String name);
	public void setOutputObject(String name, Object ob);
	}