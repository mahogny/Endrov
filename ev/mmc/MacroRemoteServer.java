package mmc;

import java.io.*;
import java.net.*;
import javax.imageio.ImageIO;


/**
 * Macro over network: running on microscope side
 * java -jar startEndrov.jar mmc.MacroRemoteServer
 * @author Johan Henriksson
 */
public class MacroRemoteServer implements Runnable
	{
	private Macro macro=new MacroLocal();
	private ObjectInputStream in;
	private ObjectOutputStream out;
	
	public MacroRemoteServer(Socket socket)
		{
		try
			{
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			}
		catch (IOException ex)
			{
			ex.printStackTrace();
			}
		}
	
	
	/**
	 * Event loop
	 */
	public void run()
		{
		try
			{
			while (true)
				{
				String e = in.readUTF();
				if(e.equals("quit"))
					{
					in.close();
					out.close();
					System.out.println("Got quit");
					return;
					}
				else if(e.equals("getClipboard"))
					out.writeUTF(macro.getClipboard());
				else if(e.equals("setClipboard"))
					macro.setClipboard(in.readUTF());
				else if(e.equals("kt"))
					macro.keyType(in.readInt(), in.readBoolean());
				else if(e.equals("mouseClick"))
					macro.mouseClick(in.readInt(), in.readInt(), in.readInt());
				else if(e.equals("screenshot"))
					{
					ByteArrayOutputStream baos = new ByteArrayOutputStream( 1000 );
					ImageIO.write(macro.getScreenshot(), "png", baos);
					baos.flush();
					byte[] rimage=baos.toByteArray();
					System.out.println("length "+rimage.length);
					out.writeInt(rimage.length);
					out.write(rimage);
					out.flush();
					System.gc();
					System.out.println(""+System.currentTimeMillis()+" sent image");
					}
				else
					System.out.println("Unknown command: "+e);
				}

			}
		catch (IOException ex)
			{
			ex.printStackTrace();
			}
		}
	
	
	
	
	
	/**
	 * Entry point
	 */
	public static void main(String[] arg)
		{
		System.out.println("Running server");
		try
			{
			ServerSocket s = new ServerSocket(1166);
			while (true)
				{
				Socket socket = s.accept();
				MacroRemoteServer t = new MacroRemoteServer(socket);
				Thread thread = new Thread(t);
				thread.start();
				System.out.println("Got connection");
				}
			}
		catch (IOException ex)
			{
			ex.printStackTrace();
			}
		}
	}
