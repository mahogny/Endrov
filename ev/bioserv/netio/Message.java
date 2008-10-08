package bioserv.netio;

import java.io.IOException;
import java.io.Serializable;

public class Message extends SMessage
	{

	/**
	 * Pack message. Command can not be null
	 */
	public Message(Serializable arg[],String command,Callback cb) throws IOException
		{
		super(arg,command,cb);
		if(command==null || command.equals(""))
			throw new IOException("command was null");
		}
	}
