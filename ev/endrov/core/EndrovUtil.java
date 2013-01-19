package endrov.core;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.jdom.Element;

import endrov.starter.EvSystemUtil;
import endrov.util.EvDecimal;

public class EndrovUtil
	{

	/**
	 * http://en.wikipedia.org/wiki/ISO_8601
	 */
	public static Date parseISO8601Date(String s)
		{
		return javax.xml.bind.DatatypeConverter.parseDateTime(s).getTime();
		/*
		//Here assuming date contains :. This is an incorrect assumption, should be checked
		try
			{
			int year=Integer.parseInt(s.substring(0,4));
			int month=Integer.parseInt(s.substring(5,7));
			int day=Integer.parseInt(s.substring(8,10));
			int hour=Integer.parseInt(s.substring(11,13));
			int minute=Integer.parseInt(s.substring(14,16));
			int second=Integer.parseInt(s.substring(17,19));
			Date d=new Date(year-1900,month-1,day,hour,minute,second);
			return new EvDecimal(d.getTime());
			}
		catch (Exception e)
			{
			e.printStackTrace();
			return null;
			}
			*/
		}

	/**
	 * Check equality, handles null objects
	 */
	public static <E> boolean equalsHandlesNull(E a, E b)
		{
		if(a==null)
			return b==null;
		else if(b==null)
			return false;
		else
			return a.equals(b);
		}

	/**
	 * For debugging
	 */
	public static void generateStackTrace(String msg)
		{
		try
			{
			throw new Exception("tracing: "+msg);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}

	/**
	 * Cast iterable to whatever type
	 */
	public static Iterable<Element> castIterableElement(final Iterable<?> o)
		{
		return new Iterable<Element>(){
			public Iterator<Element> iterator(){
				return new Iterator<Element>(){
					Iterator<?> it=o.iterator();
					public boolean hasNext(){return it.hasNext();}
					@SuppressWarnings("all") public Element next(){return (Element)it.next();}
					public void remove(){it.remove();}
				};
			}
		};
		}

	/**
	 * Cast iterable to whatever type
	 */
	public static<E> Iterable<E> castIterable(Class<E> cl, final Iterable<?> o)
		{
		return new Iterable<E>(){
			public Iterator<E> iterator(){
				return new Iterator<E>(){
					Iterator<?> it=o.iterator();
					public boolean hasNext(){return it.hasNext();}
					@SuppressWarnings("all") public E next(){return (E)it.next();}
					public void remove(){it.remove();}
				};
			}
		};
		}

	/**
	 * Format a number to a certain number of digits. Append to existing buffer.
	 * @param n The number
	 * @param len The length of the final string
	 */
	public static void pad(int n, int len, StringBuffer sb)
		{
		String s=Integer.toString(n);
		String topad="0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
		int slen=s.length();
		len-=slen;
		if(slen<=0)
			sb.append(s);
		else if(len<100)
			{
			sb.append(topad.substring(0,len));
			sb.append(s);
			}
		else
			{
			while(len>0)
				{
				sb.append('0');
				len--;
				}
			sb.append(s);
			}
		}

	/**
	 * Format a number to a certain number of digits.
	 * @param n The number
	 * @param len The length of the final string
	 */
	public static String pad(int n, int len)
		{
		String s=Integer.toString(n);
		String topad="0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
		int slen=s.length();
		len-=slen;
		if(slen<=0)
			return s;
		else if(len<100)
			return topad.substring(0,len)+s;
		else
			{
			StringBuffer sb=new StringBuffer(slen+len+10);
			while(len>0)
				{
				sb.append('0');
				len--;
				}
			sb.append(s);
			return sb.toString();
			}
		}

	public static void pad(EvDecimal d, int len, StringBuffer sb)
			{
			String s=d.toString();
			String topad="0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
			int slen=s.indexOf(".");
			if(slen==-1)
				slen=s.length();
			len-=slen;
			if(slen<=0)
				sb.append(s);
	//			return s;
			else if(len<100)
				sb.append(topad.substring(0,len)+s);
	//			return topad.substring(0,len)+s;
			else
				{
	//			StringBuffer sb=new StringBuffer(slen+len+10);
				while(len>0)
					{
					sb.append('0');
					len--;
					}
				sb.append(s);
	//			return sb.toString();
				}
			}

	public static String pad(EvDecimal d, int len)
	{
	StringBuffer sb=new StringBuffer();
	pad(d,len,sb);
	return sb.toString();
	}

	public static void openExternalProgram(File f)
	{
	try
		{
		if(EvSystemUtil.isMac())
			Runtime.getRuntime().exec(new String[]{"/usr/bin/open",f.getAbsolutePath()});
		else if(EvSystemUtil.isLinux())
			{
			Runtime.getRuntime().exec(new String[]{"/usr/bin/xdg-open",f.getAbsolutePath()});
			}
		else
			{
			//TODO JAVA6
			/*
			if(Desktop.isDesktopSupported())
				Desktop.getDesktop().open(f);
			else*/
				JOptionPane.showMessageDialog(null, "Feature not supported on this platform");
			}
		}
	catch (IOException e)
		{
		e.printStackTrace();
		}
	}

	}
