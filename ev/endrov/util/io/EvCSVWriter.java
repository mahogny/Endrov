package endrov.util.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Export CSV-like files
 * 
 * @author Johan Henriksson
 */
public class EvCSVWriter
	{
	private BufferedWriter io;
	private boolean quote;
	private String fieldDelim;
	private boolean firstEntry=true;
	
	public EvCSVWriter(Writer io, String fieldDelim, boolean quote)
		{
		this.io=new BufferedWriter(io);
		this.fieldDelim=fieldDelim;
		this.quote=quote;
		}
	
	public void writeEntry(String e) throws IOException
		{
		if(!firstEntry)
			io.append(fieldDelim);
		if(quote)
			io.append("\"");
		io.append(e);
		if(quote)
			io.append("\"");
		firstEntry=false;
		}

	public void writeEndOfLine() throws IOException
		{
		io.append("\n");
		}
	
	public void close() throws IOException
		{
		io.close();
		io=null;
		}
	
	
	@Override
	protected void finalize() throws Throwable
		{
		if(io!=null)
			throw new IOException("CSV writer was not closed properly");
		}
	
	}
