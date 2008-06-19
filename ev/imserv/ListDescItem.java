package imserv;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;


/**
 * List of tags, Tag-like element and parsing
 * 
 * @author Johan Henriksson
 */
public class ListDescItem
	{
	public final static int TAG=1,CHAN=2,OBJ=3,ATTR=4,MATCHALL=5;
	public final static int AND=10,OR=11,NOT=12;
	
	private int type;
	private String name;
	private String value;
	private ListDescItem a,b;
	
	public ListDescItem(int type, String name)
		{
		this.type=type;
		this.name=name;
		}
	public ListDescItem(int type, String name, String value)
		{
		this.type=type;
		this.name=name;
		this.value=value;
		}
	public ListDescItem(int type, ListDescItem a, ListDescItem b)
		{
		this.type=type;
		this.a=a;
		this.b=b;
		}
	
	/**
	 * Pretty-printer
	 */
	public String toString()
		{
		switch(type)
			{
			case MATCHALL: return "*";
			case TAG: return "tag:"+name;
			case CHAN: return "chan:"+name;
			case OBJ: return "obj:"+name;
			case ATTR: return "attr:"+name+"="+value;
			case AND: return "("+a.toString()+") and ("+b.toString()+")";
			case OR: return "("+a.toString()+") or ("+b.toString()+")";
			case NOT: return "not ("+a.toString()+")";
			default: return "err"; 
			}
		}
	
	/**
	 * 
	 * Packrat parser, syntax:
	 */
	public static ListDescItem parse(String s)
		{
		LinkedList<Character> sl=new LinkedList<Character>();
		for(char c:s.toCharArray())
			sl.add((Character)c);
		if(sl.isEmpty())
			return new ListDescItem(MATCHALL,null);
		try
			{
			return parseTop(sl);
			}
		catch (Exception e)
			{
			System.out.println(e.getMessage());
			e.printStackTrace();
			return null;
			}
		}

	/**
	 * Parser helper.
	 * top = "not" top | atom [("and"|"or") top]
	 */
	private static ListDescItem parseTop(LinkedList<Character> list) throws Exception
		{
		if(tryTake(list, "not "))
			return new ListDescItem(NOT,parseTop(list),null);
		else
			{
			ListDescItem itemA=parseAtom(list);
			if(tryTake(list, " and "))
				{
				ListDescItem itemB=parseTop(list);
				return new ListDescItem(AND,itemA,itemB);
				}
			else if(tryTake(list, " or "))
				{
				ListDescItem itemB=parseTop(list);
				return new ListDescItem(OR,itemA,itemB);
				}
			if(list.isEmpty())
				return itemA;
			else
				throw new Exception("Not consumed: "+restAsString(list));
			}
		}

	/**
	 * Parser helper.
	 * atom = "tag:" s | "chan:" s | "obj:" s | "attr:" s "=" s | "(" top ")" 
	 */
	private static ListDescItem parseAtom(LinkedList<Character> list) throws Exception
		{
		if(tryTake(list, "tag:"))
			return new ListDescItem(TAG,takeString(list));
		else if(tryTake(list, "chan:"))
			return new ListDescItem(CHAN,takeString(list));
		else if(tryTake(list, "obj:"))
			return new ListDescItem(OBJ,takeString(list));
		else if(tryTake(list, "attr:"))
			{
			String s=takeString(list);
			int i=s.indexOf('=');
			if(i==-1)
				throw new Exception("Attr without =: "+s);
			return new ListDescItem(ATTR,s.substring(0,i),s.substring(i+1));
			}
		else if(tryTake(list, "("))
			{
			ListDescItem item=parseTop(list);
			if(tryTake(list, ")"))
				return item;
			else
				throw new Exception("Parse error before: "+restAsString(list));
			}
		else
			throw new Exception("Parse error before: "+restAsString(list));
		}

	/**
	 * Get the rest of the char list as a string
	 */
	private static String restAsString(LinkedList<Character> list)
		{
		StringBuffer bf=new StringBuffer();
		for(Character c:list)
			bf.append(c);
		return bf.toString();
		}

	/**
	 * Take string up to end or ' '/')'
	 */
	private static String takeString(LinkedList<Character> list)
		{
		StringBuffer bf=new StringBuffer();
		for(;;)
			{
			Character c=list.pollFirst();
			if(c==null)
				break;
			else if(c==')' || c==' ')
				{
				list.push(c);
				break;
				}
			bf.append(c);
			}
		return bf.toString();
		}
	
	/**
	 * Try to take characters from list. Return true and do so if possible
	 */
	private static boolean tryTake(LinkedList<Character> list, String s)
		{
		try
			{
			int len=s.length();
			Iterator<Character> it=list.iterator();
			for(int i=0;i<len;i++)
				{
				char c=it.next();
				if(s.charAt(i)!=c)
					return false;
				}
			for(int i=0;i<len;i++)
				list.removeFirst();
			return true;
			}
		catch (NoSuchElementException e)
			{
			return false;
			}
		}
	
	
	
	
	
	public static void main(String[] arg)
		{
//		System.out.println(parse("tag:foo"));
		System.out.println(parse("tag:foo and tag:bar or not chan:el"));
		
		
		}
	
	
	
	}
