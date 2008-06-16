package imserv;

public class DataImpl implements DataIF
	{
	String name;
	
	public DataImpl(String name)
		{
		this.name=name;
		}
	
	public void print()
		{
		System.out.println("server hello");
		}
	
	}
