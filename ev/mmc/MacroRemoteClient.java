package mmc;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import javax.imageio.ImageIO;


/**
 * Macro over network: running on user side
 * @author Johan Henriksson
 */
public class MacroRemoteClient extends Macro
	{
	private ObjectInputStream in;
	private ObjectOutputStream out;
	
	public MacroRemoteClient(String serverName)
		{
		try
			{
			Socket s = new Socket(serverName, 1166);
			out = new ObjectOutputStream(s.getOutputStream());
			in = new ObjectInputStream(s.getInputStream());
			}
		catch (UnknownHostException ex)
			{
			ex.printStackTrace();
			}
		catch (IOException ex)
			{
			ex.printStackTrace();
			}
		}

	
	
	@Override
	public String getClipboard()
		{
		try
			{
			out.writeUTF("getClipboard");
			out.flush();
			return in.readUTF();
			}
		catch (IOException e)
			{
			e.printStackTrace();
			return null;
			}
		}

	@Override
	public void keyType(int keycode, boolean shift)
		{
		try
			{
			out.writeUTF("kt");
			out.writeInt(keycode);
			out.writeBoolean(shift);
			out.flush();
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}

	@Override
	public void mouseClick(int x, int y, int buttons)
		{
		try
			{
			out.writeUTF("mouseClick");
			out.writeInt(x);
			out.writeInt(y);
			out.writeInt(buttons);
			out.flush();
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}

	@Override
	public void setClipboard(String s)
		{
		try
			{
			out.writeUTF("setClipboard");
			out.writeUTF(s);
			out.flush();
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}

	
	public BufferedImage getScreenshot()
		{
		try
			{
			out.writeUTF("screenshot");
			out.flush();
			int len=in.readInt();
			System.out.println("length "+len);
			byte[] rimage=new byte[len];
			in.readFully(rimage);
			return ImageIO.read(new ByteArrayInputStream(rimage));
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		return null;
		}
	
	public void quit()
		{
		try
			{
			out.writeUTF("quit");
			out.flush();
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}
	
	
	}
