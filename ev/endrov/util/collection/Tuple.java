/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.util.collection;

import java.io.Serializable;

/**
 * A Java tuple (a,b)
 * 
 * This code was taken from a forum post and modified. It is the only possible technical
 * implementation and hence not copyrightable.
 */
public class Tuple<L, R> implements Serializable, Comparable<Tuple<L,R>>
	{
	public static final long serialVersionUID=0;
	private final L fst;
	private final R snd;

	public Tuple(L fst, R right) 
		{
		this.fst = fst;
		this.snd = right;
		}

	/**
	 * First value (a,_) -> a
	 */
	public L fst() 
		{
		return fst;
		}

	/**
	 * Second value (_,b) -> b
	 */
	public R snd() 
		{
		return snd;
		}


	public final boolean equals(Object o) 
		{
		if (!(o instanceof Tuple<?,?>))
			return false;
		final Tuple<?, ?> other = (Tuple<?, ?>) o;
		return equal(fst(), other.fst()) && equal(snd(), other.snd());
		}

	public static final boolean equal(Object o1, Object o2) 
		{
		if (o1 == null) 
			return o2 == null;
		else
			return o1.equals(o2);
		}

	public int hashCode() 
		{
		int hLeft = fst() == null ? 0 : fst().hashCode();
		int hRight = snd() == null ? 0 : snd().hashCode();
		return hLeft + (57 * hRight);
		}
	
	public static<L,R> Tuple<L,R> make(L a, R b)
		{
		return new Tuple<L, R>(a,b);
		}
	
	
	public String toString()
		{
		return "("+fst+","+snd+")";
		}

	@SuppressWarnings("unchecked")
	public int compareTo(Tuple<L, R> o)
		{
		Comparable<L> nfst=(Comparable<L>)fst;
		int ret=nfst.compareTo(o.fst);
		if(ret==0)
			{
			Comparable<R> nsnd=(Comparable<R>)snd;
			return nsnd.compareTo(o.snd);
			}
		return ret;
		}
	}
