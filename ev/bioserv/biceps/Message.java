package bioserv.biceps;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Map;


//security: improve later

/**
 * Message to be transferred over network
 * @author Johan Henriksson
 */
public class Message 
	{
	//Potential DoS: must remove callback if parse failed
	
	private RegMethod cb;
	private String command;
	private ByteArrayOutputStream bos=new ByteArrayOutputStream(128);

	

	//public Message(byte b[],Callback cb) throws IOException
	
	
	//Problem: not runnable. need to take an argument!

	
	public static Message withCallback(Serializable arg[],String command,Object cb) throws IOException
		{
		for(Method m:cb.getClass().getMethods())
			if(m.getName().equals("run"))
				return new Message(arg,command,new RegMethod(cb,m));
		throw new IOException("No run method");
		//			return new Message(arg,command,new RegMethod(cb,cb.getClass().getMethod("run", Object.class)));

		}
	/*
	public Message(Serializable arg[],String command,Callback cb) throws IOException, NoSuchMethodException
		{
		this(arg,command,new RegMethod(cb,cb.getClass().getMethod("run", Object.class)));
		}
*/
	/**
	 * Compose message. Return message has command null
	 */
	public Message(Serializable arg[],String command,RegMethod cb) throws IOException
		{
		this.cb=cb;
		this.command=command;
		//s.getOutputStream().write(b, off, len)
	
		
		
		ObjectOutputStream oos=new ObjectOutputStream(bos);
		
//		if(command!=null)
		oos.writeUTF(command);
		
		//array should have less overhead than a list
		for(Object o:arg)
			{
			//it is also possible to write char etc without object info. can save space
			System.out.println("arg "+o.getClass());
			if(o instanceof Integer)
				{
				System.out.println("wint ");
				oos.writeInt((Integer)o);
				}
			else if(o instanceof Long)
				oos.writeLong((Long)o);
			else if(o instanceof Float)
				oos.writeFloat((Float)o);
			else if(o instanceof Double)
				oos.writeDouble((Double)o);
			else if(o instanceof Byte)
				oos.writeByte((Byte)o);
			else if(o instanceof Boolean)
				oos.writeBoolean((Boolean)o);
			else if(o instanceof String)
				oos.writeUTF((String)o);
			else
				oos.writeObject(o);
			}
		oos.flush();
	
		System.out.println("send alen "+bos.size()+" #arg "+arg.length);
		
/*		System.out.println("----");
		for(byte b:bos.toByteArray())
			System.out.println("- "+(char)b);
		System.out.println("----" +bos.size()+" ");*/
	
/*		System.out.println("----");
		for(byte b:bos.toByteArray())
			System.out.println("- "+b);
		System.out.println("----" +bos.size()+" ");*/
	
//		args=bos.toByteArray();
		}
	public byte[] getBytes()
		{
		return bos.toByteArray();
		}
	
	public RegMethod getCallback()
		{
		return cb;
		}
	public String getCommand()
		{
		return command;
		}
	
	
	/**
	 * Read the contents of the message. Find the appropriate function and call it
	 */
	public static void unpackAndInvoke(byte[] args, final RMImanager rmi, Map<String, RegMethod> regfunc, Map<Short, RegMethod> cb, final short messageID) throws Exception
		{
		ByteArrayInputStream bi=new ByteArrayInputStream(args);
		ObjectInputStream is=new ObjectInputStream(bi);
		
		String funcName=is.readUTF(); 
		
		final RegMethod rm;
		boolean isReturnCall=funcName.equals("");
		Class<?> arg[];
		if(isReturnCall)
			{
			//Return of call
			rm=cb.get(messageID);
			cb.remove(messageID);
/*			Class<?> r=rm.m.getReturnType();
			if(r!=void.class)
				arg=new Class<?>[]{rm.m.getReturnType()};
			else
				arg=new Class<?>[]{};*/
			}
		else
			{
			//Call
			rm=regfunc.get(funcName);
			}
		arg=rm.m.getParameterTypes();
		

		
		System.out.println("recv alen "+args.length+" #arg "+arg.length);
		
		Object[] ser=new Object[arg.length];
		
		for(int i=0;i<arg.length;i++)
			{
			if(arg[i]==Integer.class || arg[i]==int.class)
				ser[i]=is.readInt();
			else if(arg[i]==Long.class || arg[i]==long.class)
				ser[i]=is.readLong();
			else if(arg[i]==Float.class || arg[i]==float.class)
				ser[i]=is.readFloat();
			else if(arg[i]==Double.class || arg[i]==double.class)
				ser[i]=is.readDouble();
			else if(arg[i]==Byte.class || arg[i]==byte.class)
				ser[i]=is.readByte();
			else if(arg[i]==Boolean.class || arg[i]==boolean.class)
				ser[i]=is.readBoolean();
			else if(arg[i]==String.class)
				ser[i]=is.readUTF();
			else
				{
				//int vs Integer!!!?
				System.out.println("type is "+arg[i].getCanonicalName());
				ser[i]=is.readObject();
				}
			}
		
		final Object result=rm.m.invoke(rm.mthis, ser);
		if(!isReturnCall)
			{
			if(rm.m.getReturnType()==void.class)
				rmi.send(new Message(new Serializable[]{},"",null),messageID);
			else
				rmi.send(new Message(new Serializable[]{(Serializable)result},"",null),messageID);
			System.out.println("Returning====");
			}

		
		System.out.println("here");
		}
	
	
	
	

	//Measurement of size of plain serialize:
	//"foo" - 10 bytes
	//Integer(5) - 81 bytes
	//SmallInteger - 55 bytes. 26 due to the name
	//writeInt(5) - 4 bytes

	//empty object stream 10 bytes
	
	}
