package imserv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.net.ssl.SSLSocket;

public class ClientSession extends Thread
	{
	SSLSocket socket;
	public ClientSession(SSLSocket socket)
		{
		this.socket=socket;
		}

	public void run()
		{
		try
			{
			InputStream inputstream = socket.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

			String string = null;
			while ((string = bufferedreader.readLine()) != null)
				{
				System.out.println(string);
				System.out.flush();
				}
			}
		catch (IOException e)
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		}
	
	
	
	
	}
