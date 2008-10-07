package bioserv.netio;

import java.io.*;


/**
 * Message to be transferred over network
 * @author Johan Henriksson
 */
public class Message 
	{
	//Potential DoS: must remove callback if parse failed
	
	private Callback cb;
	private String command;
	private byte[] args;
	
	

	//public Message(byte b[],Callback cb) throws IOException
	
	
	//Problem: not runnable. need to take an argument!
	public Message(Serializable arg[],String command,Callback cb) throws IOException
		{
		this.cb=cb;
		this.command=command;
		//s.getOutputStream().write(b, off, len)
	
		
		
		ByteArrayOutputStream bos=new ByteArrayOutputStream(128);
		ObjectOutputStream oos=new ObjectOutputStream(bos);
		
	
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
	
	
	
	public Object[] unpack(Class<?> arg[]) throws Exception
		{
		ByteArrayInputStream bi=new ByteArrayInputStream(args);
		ObjectInputStream is=new ObjectInputStream(bi);
		Object[] ser=new Object[arg.length];
		System.out.println("alen "+args.length);
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
		return ser;
		}
	
	
	
	

	//Measurement of size of plain serialize:
	//"foo" - 10 bytes
	//Integer(5) - 81 bytes
	//SmallInteger - 55 bytes. 26 due to the name
	//writeInt(5) - 4 bytes

	//empty object stream 10 bytes
	
	}
