package endrov.ev;

import java.util.*;

import javax.swing.JOptionPane;


/**
 * Internationalization for Endrov. Java has internationalization built in but it suffers from several problems:
 * 1. A ton of cluttering classes are needed, per plugin
 * 2. It requires editing code, meaning many end-users will not contribute translations 
 * 
 * Full modularity requires that each plugin has its own file. This is broken with a tradeoff to make everything
 * simpler - it is ok with one file for everything. 
 * 
 * @author Johan Henriksson
 *
 */
public class EvLang
	{
	//private static HashSet<String> untranslated=new HashSet<String>();
	
	public static boolean showUntranslated=false;
	public static boolean askTranslation=false;
	
	private static HashMap<String,String> translations=new HashMap<String, String>();
	
	private static String language="sv";//Locale.getDefault().getLanguage();
	
	public EvLang()
		{
		System.out.println("evlang "+language);
		}
	
	
	public static class EvFormatter
		{
		private Formatter f;
		private String s;
		
		public EvFormatter(Formatter f, String s)
			{
			this.f = f;
			this.s = s;
			}
		
		public void format(Object... args)
			{
			f.format(s, args);
			f.flush();
			}
		
		}
	
	/*
	public String printf(Class<?> cl, String s, Object... args)
		{
		Formatter f=translations.get(s);
		if(f!=null)
			return f.format(s, args).toString();
		else
			{
			StringBuffer sb=new StringBuffer();
			f=new Formatter(s);
			
			
			}
		}*/
	

	/**
	 * Format a string
	 * 
	 * @param format	Format. See Formatter
	 * @format args		Arguments to format
	 */
	public static String printf(/*Class<?> cl,*/ String format, Object... args)
		{
		StringBuffer sb=new StringBuffer();
		EvFormatter f=get(sb, format);
		f.format(format, args);
		return sb.toString();
		}
	
	
	
	/**
	 * Return a formatter. More efficient for repeated formatting.
	 *
	 * @param ap			Where output should go
	 * @param format	Format. See Formatter
	 */
	public static EvFormatter get(Appendable ap, /*Class<?> cl, */String format)
		{
		String f=translations.get(format);
		if(f==null)
			{
			//English is the default language
			f=format;
			if(!language.equals("en"))
				{
				if(askTranslation)
					{
					//Ask for a translation if the user thinks it is ok
					String t=JOptionPane.showInputDialog("Provide ("+language+") translation for \""+format+"\"");
					if(t!=null)
						{
						translations.put(format,t);
						f=t;
						}
					}
				}
			}
		
		//Set up formatter
		EvFormatter ff=new EvFormatter(new Formatter(ap),f);
		return ff;
		}
	}
