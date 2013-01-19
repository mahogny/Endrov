package endrov.annotationNetwork;



public interface NetworkTracerFactory
	{
	public String tracerName();
	public NetworkTracerInterface create();
	}
