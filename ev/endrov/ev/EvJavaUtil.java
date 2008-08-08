package endrov.ev;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.*;

public class EvJavaUtil
	{
	/**
	 * Work-around for a bug/faulty design in getResource().getFile(), making space %20 etc
	 */
	public static File getFileFromURL(URL urlToDecode)
		{
	  try
			{
			return new File(URLDecoder.decode(urlToDecode.getFile(),"UTF-8"));
			}
		catch (UnsupportedEncodingException e)
			{
			e.printStackTrace();
			return null;
			}
		}
	}
