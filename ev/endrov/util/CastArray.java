package endrov.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Cast between arrays of different types
 * @author Johan Henriksson
 *
 */
public class CastArray
	{
	/**
	 * Cast byte to short.
	 * TODO verify code
	 */
	public static short[] toShort(byte[] b)
		{
		int len=b.length/2;
		short[] out=new short[len];
		
		try
			{
			DataInputStream dis=new DataInputStream(new ByteArrayInputStream(b));
			for(int i=0;i<len;i++)
				out[i]=dis.readShort();
			}
		catch (IOException e)
			{
			e.printStackTrace();
			System.out.println("Internal error");
			}
		/*
		int at=0;
		for(int i=0;i<len;i++)
			{
			out[i]=(short)(b[at]+(b[at+1]<<8));
			at+=2;
			}
		*/
		return out;
		}

	}
