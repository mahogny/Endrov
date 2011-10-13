package endrov.network;



public interface NetworkTracerFactory
	{
	public String tracerName();
	public NetworkTracerInterface create();
	}
