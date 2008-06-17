package imserv;

//http://java.sun.com/j2se/1.5.0/docs/api/javax/rmi/ssl/package-summary.html
//http://java.sun.com/j2se/1.5.0/docs/guide/rmi/socketfactory/SSLInfo.html
//http://java.sun.com/j2se/1.5.0/docs/guide/security/jsse/samples/index.html

import java.rmi.server.UnicastRemoteObject;


public class ImservImpl extends UnicastRemoteObject implements ImservIF 
	{
	public static final long serialVersionUID=0;

	
	public ImservImpl() throws Exception 
		{
		super(Imserv.PORT,	new RMISSLClientSocketFactory(),	new RMISSLServerSocketFactory());
		}
	
	public DataIF getData(String name) throws Exception 
		{
		return new DataImpl(name);
		}
	
	public String sayHello() 
		{
		return "Hello World!";
		}
	
	}
	
