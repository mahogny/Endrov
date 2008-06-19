package imserv;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;


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
	
	
	public static ListDescItem makeTag(String s)
		{
		ListDescItem item=new ListDescItem();
		item.type=TAG;
		item.name=s;
		return item;
		}
	public static ListDescItem makeChan(String s)
		{
		ListDescItem item=new ListDescItem();
		item.type=CHAN;
		item.name=s;
		return item;
		}
	public static ListDescItem makeObj(String s)
		{
		ListDescItem item=new ListDescItem();
		item.type=OBJ;
		item.name=s;
		return item;
		}
	public static ListDescItem makeAttr(String name,String value)
		{
		ListDescItem item=new ListDescItem();
		item.type=ATTR;
		item.name=name;
		item.value=value;
		return item;
		}
	public static ListDescItem makeMatchAll()
		{
		ListDescItem item=new ListDescItem();
		item.type=MATCHALL;
		return item;
		}
	public static ListDescItem makeNot(ListDescItem a)
		{
		ListDescItem item=new ListDescItem();
		item.type=NOT;
		item.a=a;
		return item;
		}
	public static ListDescItem makeOr(ListDescItem a,ListDescItem b)
		{
		ListDescItem item=new ListDescItem();
		item.type=OR;
		item.a=a;
		item.b=b;
		return item;
		}
	public static ListDescItem makeAnd(ListDescItem a,ListDescItem b)
		{
		ListDescItem item=new ListDescItem();
		item.type=AND;
		item.a=a;
		item.b=b;
		return item;
		}
	
	
	/*
	private ListDescItem(){}
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
	*/
	
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
			return makeMatchAll();
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
			return makeNot(parseTop(list));
		else
			{
			ListDescItem itemA=parseAtom(list);
			if(tryTake(list, " and "))
				{
				ListDescItem itemB=parseTop(list);
				return makeAnd(itemA,itemB);
				}
			else if(tryTake(list, " or "))
				{
				ListDescItem itemB=parseTop(list);
				return makeOr(itemA,itemB);
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
			return makeTag(takeString(list));
		else if(tryTake(list, "chan:"))
			return makeChan(takeString(list));
		else if(tryTake(list, "obj:"))
			return makeObj(takeString(list));
		else if(tryTake(list, "*"))
			return makeMatchAll();
		else if(tryTake(list, "attr:"))
			{
			String s=takeString(list);
			int i=s.indexOf('=');
			if(i==-1)
				throw new Exception("Attr without =: "+s);
			return makeAttr(s.substring(0,i),s.substring(i+1));
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
			Character c=list.poll();
			if(c==null)
				break;
			else if(c==')' || c==' ')
				{
				list.addFirst(c);
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
	
	
	/**
	 * Remove entries from the map that does not fulfill boolean criteria
	 */
	public void filter(Daemon daemon, Map<String,DataIF> map)
		{
		if(type==TAG)
			{
			Set<DataIF> datas=daemon.tags.get(name);
			if(datas==null)
				map.clear();
			else
				map.values().retainAll(datas);
			}
		else if(type==CHAN)
			{
			Set<DataIF> datas=daemon.channels.get(name);
			if(datas==null)
				map.clear();
			else
				map.values().retainAll(datas);
			}
		else if(type==OBJ)
			{
			Set<DataIF> datas=daemon.objs.get(name);
			if(datas==null)
				map.clear();
			else
				map.values().retainAll(datas);
			}
		else if(type==AND)
			{
			a.filter(daemon, map);
			b.filter(daemon, map);
			}
		else if(type==OR)
			{
			HashMap<String,DataIF> mapa=new HashMap<String, DataIF>(map);
			HashMap<String,DataIF> mapb=new HashMap<String, DataIF>(map);
			a.filter(daemon, mapa);
			b.filter(daemon, mapb);
			HashMap<String,DataIF> mapor=new HashMap<String, DataIF>(mapa);
			mapor.putAll(mapb);
			map.values().retainAll(mapor.values());
			}
		else if(type==NOT)
			{
			HashMap<String,DataIF> mapnot=new HashMap<String, DataIF>(map);
			a.filter(daemon, mapnot);
			map.values().removeAll(mapnot.values());
			}
		}
	

	/*
	public static ListDescItem multiAnd(Collection<ListDescItem> list)
		{
		ListDescItem base=new ListDescItem(MATCHALL,null);
		for(ListDescItem item:list)
			base=new ListDescItem(AND,base,item);
		return base;
		}
		*/
	
	
/*	
	public static void main(String[] arg)
		{
//		System.out.println(parse("tag:foo"));
		System.out.println(parse("tag:foo and tag:bar or not chan:el"));
		
		
		}
	*/
	
	
	}
