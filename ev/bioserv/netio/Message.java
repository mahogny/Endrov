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
	
	public static void main(String[] arg)
		{
		try
			{
			new Message(new Serializable[]{new SmallInteger(5)},null);
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}
	

	//public Message(byte b[],Callback cb) throws IOException
	
	
	//Problem: not runnable. need to take an argument!
	public Message(Serializable arg[],Callback cb) throws IOException
		{
		this.cb=cb;
		//s.getOutputStream().write(b, off, len)
	
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		ObjectOutputStream oos=new ObjectOutputStream(bos);
	
		//array should have less overhead than a list
		for(Object o:arg)
			{
			//it is also possible to write char etc without object info. can save space
			if(o instanceof Integer)
				oos.writeInt((Integer)o);
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
	
		System.out.println("----");
	//	for(int i=0;i<bos.size())
		for(byte b:bos.toByteArray())
			System.out.println("- "+(char)b);
		System.out.println("----" +bos.size()+" ");
	
	
		
		}
	
	public Callback getCallback()
		{
		return cb;
		}
	
	
	
	
	public Object[] unpack(Class<?> arg[]) throws Exception
		{
		ByteArrayInputStream bi=new ByteArrayInputStream(new byte[]{});
		ObjectInputStream is=new ObjectInputStream(bi);
		Object[] ser=new Object[arg.length];
		for(int i=0;i<arg.length;i++)
			{
			if(arg[i]==Integer.class)
				ser[i]=is.readInt();
			else if(arg[i]==Long.class)
				ser[i]=is.readLong();
			else if(arg[i]==Float.class)
				ser[i]=is.readFloat();
			else if(arg[i]==Double.class)
				ser[i]=is.readDouble();
			else if(arg[i]==Byte.class)
				ser[i]=is.readByte();
			else if(arg[i]==Boolean.class)
				ser[i]=is.readBoolean();
			else if(arg[i]==String.class)
				ser[i]=is.readUTF();
			else
				ser[i]=is.readObject();
			}
		return ser;
		}
	

	//Measurement of size of plain serialize:
	//"foo" - 10 bytes
	//Integer(5) - 81 bytes
	//SmallInteger - 55 bytes. 26 due to the name
	//writeInt(5) - 4 bytes

	}
