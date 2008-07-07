package bioserv.imserv;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * List of tags, Tag-like element and parsing
 * 
 * @author Johan Henriksson
 */
public class TagExpr
	{
	public final static int TAG=1,ATTR=4,MATCHALL=5,MATCHNONE=6;
	public final static int AND=10,OR=11,NOT=12;
	
	public int type;
	public String name;
	private String value;
	private TagExpr a,b;
	
	
	
	
	public static TagExpr makeTag(String s)
		{
		TagExpr item=new TagExpr();
		item.type=TAG;
		item.name=s;
		return item;
		}
	public static TagExpr makeAttr(String name,String value)
		{
		TagExpr item=new TagExpr();
		item.type=ATTR;
		item.name=name;
		item.value=value;
		return item;
		}
	public static TagExpr makeMatchAll()
		{
		TagExpr item=new TagExpr();
		item.type=MATCHALL;
		return item;
		}
	public static TagExpr makeMatchNone()
		{
		TagExpr item=new TagExpr();
		item.type=MATCHNONE;
		return item;
		}
	public static TagExpr makeNot(TagExpr a)
		{
		TagExpr item=new TagExpr();
		item.type=NOT;
		item.a=a;
		return item;
		}
	public static TagExpr makeOr(TagExpr a,TagExpr b)
		{
		TagExpr item=new TagExpr();
		item.type=OR;
		item.a=a;
		item.b=b;
		return item;
		}
	public static TagExpr makeAnd(TagExpr a,TagExpr b)
		{
		TagExpr item=new TagExpr();
		item.type=AND;
		item.a=a;
		item.b=b;
		return item;
		}
	

	private static String escapeString(String s)
		{
		return '"'+s+'"';
		}
	public static String escapeStringIfNeeded(String s)
		{
		if(s.indexOf(' ')==-1)
			return s;
		else
			return escapeString(s);
		}
	
	/**
	 * Pretty-printer
	 */
	public String toString()
		{
		switch(type)
			{
			case MATCHALL: return "*";
			case MATCHNONE: return "!";
			case TAG: return escapeStringIfNeeded(name);
			case ATTR: return escapeStringIfNeeded(name)+"="+escapeStringIfNeeded(value);
			case AND: return "("+a.toString()+") and ("+b.toString()+")";
			case OR: return "("+a.toString()+") or ("+b.toString()+")";
			case NOT: return "not ("+a.toString()+")";
			default: return "<err>"; 
			}
		}
	
	/**
	 * 
	 * Packrat parser, syntax:
	 */
	public static TagExpr parse(String s)
		{
		LinkedList<Character> sl=new LinkedList<Character>();
		for(char c:s.toCharArray())
			sl.add((Character)c);
		if(sl.isEmpty())
			return makeMatchAll();
		try
			{
			TagExpr item=parseTop(sl);
			if(sl.isEmpty())
				return item;
			else
				throw new Exception("Not consumed: "+restAsString(sl));
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
	 * top = atom [("and"|"or") top]
	 */
	private static TagExpr parseTop(LinkedList<Character> list) throws Exception
		{
		TagExpr itemA=parseAtom(list);
		if(tryTake(list, " and "))
			{
			TagExpr itemB=parseTop(list);
			return makeAnd(itemA,itemB);
			}
		else if(tryTake(list, " or "))
			{
			TagExpr itemB=parseTop(list);
			return makeOr(itemA,itemB);
			}
		return itemA;
		}

	/**
	 * Parser helper.
	 * atom = "not" atom | "tag:" s | "chan:" s | "obj:" s | "attr:" s "=" s | "(" top ")" 
	 */
	private static TagExpr parseAtom(LinkedList<Character> list) throws Exception
		{
		if(tryTake(list, "not "))
			return makeNot(parseAtom(list));
		else if(tryTake(list, "*"))
			return makeMatchAll();
		else if(tryTake(list, "!"))
			return makeMatchNone();
		/*else if(tryTake(list, "attr:"))
			{
			String s=takeString(list);
			int i=s.indexOf('=');
			if(i==-1)
				throw new Exception("Attr without =: "+s);
			return makeAttr(s.substring(0,i),s.substring(i+1));
			}*/
		else if(tryTake(list, "("))
			{
			TagExpr item=parseTop(list);
			if(tryTake(list, ")"))
				return item;
			else
				throw new Exception("Parse error before: "+restAsString(list));
			}
		else
			{
			String rest=takeString(list);
			int i=rest.indexOf('=');
			if(i==-1)
				return makeTag(rest);
			else
				{
				String name=rest.substring(0,i);
				String value=rest.substring(i+1);
				return makeAttr(name,value);
				}
			//TODO: escaping of attr name
			}
//			throw new Exception("Parse error before: "+restAsString(list));
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
			else if(c=='"')
				{
				//Until next "
				for(;;)
					{
					c=list.poll();
					if(c==null)
						break;
					else if(c=='"')
						break;
					bf.append(c);
					}
				}
			else if(c==')' || c==' ')
				{
				list.addFirst(c);
				break;
				}
			else
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
	public void filter(ImservImpl imserv, Map<String,DataIF> map)
		{
		if(type==MATCHALL)
			;
		else if(type==MATCHNONE)
			map.clear();
		else if(type==TAG)
			{
			Set<DataIF> datas=imserv.tags.get(name);
			if(datas==null)
				map.clear();
			else
				map.values().retainAll(datas);
			}
		else if(type==ATTR)
			{
			//Rough elimination
			Set<DataIF> datas=imserv.tags.get(name);
			if(datas==null)
				map.clear();
			else
				map.values().retainAll(datas);

			//Go through every entry, match
			try
				{
				Pattern p = Pattern.compile(value);
				Set<String> keep=new HashSet<String>();
				for(Map.Entry<String, DataIF> entry:map.entrySet())
					{
					//quick hack
					DataImpl data=(DataImpl)entry.getValue();
					Tag tag=data.tags.get(name);
					Matcher m = p.matcher(tag.value);
					if(m.matches())
						{
						keep.add(entry.getKey());
						System.out.println("--match: "+tag.value+" for "+value+"--");
						}
					else
						System.out.println("--no match: "+tag.value+" for "+value+"--");
					}
				map.keySet().retainAll(keep);
				}
			catch (RuntimeException e)
				{
				map.clear();
				e.printStackTrace();
				}
			}
		else if(type==AND)
			{
			a.filter(imserv, map);
			b.filter(imserv, map);
			}
		else if(type==OR)
			{
			HashMap<String,DataIF> mapa=new HashMap<String, DataIF>(map);
			HashMap<String,DataIF> mapb=new HashMap<String, DataIF>(map);
			a.filter(imserv, mapa);
			b.filter(imserv, mapb);
			HashMap<String,DataIF> mapor=new HashMap<String, DataIF>(mapa);
			mapor.putAll(mapb);
			map.values().retainAll(mapor.values());
			}
		else if(type==NOT)
			{
			HashMap<String,DataIF> mapnot=new HashMap<String, DataIF>(map);
			a.filter(imserv, mapnot);
			map.values().removeAll(mapnot.values());
			}
		}
	
	
	public static void main(String[] arg)
		{
//		System.out.println(parse("tag:foo"));
		System.out.println(parse("foo=bar"));
		
		
		}
	
	
	
	}
