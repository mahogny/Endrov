package bioserv.netio;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Map;

import bioserv.netio.RMImanager.RegMethod;


/**
 * Message to be transferred over network
 * @author Johan Henriksson
 */
public abstract class SMessage 
	{
	//Potential DoS: must remove callback if parse failed
	
	private Callback cb;
	private String command;
	private byte[] args;
	
	

	//public Message(byte b[],Callback cb) throws IOException
	
	
	//Problem: not runnable. need to take an argument!
	/**
	 * Compose message. Return message has command null
	 */
	protected SMessage(Serializable arg[],String command,Callback cb) throws IOException
		{
		this.cb=cb;
		this.command=command;
		//s.getOutputStream().write(b, off, len)
	
		
		
		ByteArrayOutputStream bos=new ByteArrayOutputStream(128);
		ObjectOutputStream oos=new ObjectOutputStream(bos);
		
		if(command!=null)
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
	
/*		System.out.println("----");
		for(byte b:bos.toByteArray())
			System.out.println("- "+(char)b);
		System.out.println("----" +bos.size()+" ");*/
	
/*		System.out.println("----");
		for(byte b:bos.toByteArray())
			System.out.println("- "+b);
		System.out.println("----" +bos.size()+" ");*/
	
		args=bos.toByteArray();
		}

	
	
	public Callback getCallback()
		{
		return cb;
		}
	public String getCommand()
		{
		return command;
		}
	public int getBytesLength()
		{
		return args.length;
		}
	
	public void write(OutputStream os, int offset, int length) throws IOException
		{
		os.write(args, offset, length);
		}
	
	
	
	
	public void unpackAndInvoke(Map<String, RegMethod> regfunc, Map<Integer, RegMethod> cb,int messageID) throws Exception
		{
		ByteArrayInputStream bi=new ByteArrayInputStream(args);
		ObjectInputStream is=new ObjectInputStream(bi);
		System.out.println("alen "+args.length);
		
		String funcName=is.readUTF(); 
		
		Class<?> arg[]; 
		Method method;
		if(funcName.equals(""))
			{
			//Return of call
		//TODO
			RegMethod rm=cb.get(messageID);
			method=rm.m;
			arg=rm.c;
			
			
			}
		else
			{
			//Call
			RegMethod rm=regfunc.get(funcName);
			method=rm.m;
			arg=rm.c;
			}
		
		
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
				System.out.println("type is "+arg[i].getCanonicalName()+" "+Integer.class.getCanonicalName());
				ser[i]=is.readObject();
				}
			}
		
		method.invoke(null, ser);
		}
	
	
	
	

	//Measurement of size of plain serialize:
	//"foo" - 10 bytes
	//Integer(5) - 81 bytes
	//SmallInteger - 55 bytes. 26 due to the name
	//writeInt(5) - 4 bytes

	//empty object stream 10 bytes
	
	}
