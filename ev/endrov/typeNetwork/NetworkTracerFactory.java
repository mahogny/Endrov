package endrov.typeNetwork;



public interface NetworkTracerFactory
	{
	public String tracerName();
	public NetworkTracerInterface create();
	}
