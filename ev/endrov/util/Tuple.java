package endrov.util;

import java.io.Serializable;

/**
 * A Java tuple (a,b)
 * 
 * This code was taken from a forum post. It is the only possible technical
 * implementation and hence not copyrightable.
 */
public class Tuple<L, R> implements Serializable
	{
	public static final long serialVersionUID=0;
	private final L fst;
	private final R snd;

	public Tuple(L left, R right) 
		{
		this.fst = left;
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

/*	public static <A, B> Tuple<A, B> create(A left, B right) 
		{
		return new Tuple<A, B>(left, right);
		}*/

	public final boolean equals(Object o) 
		{
		if (!(o instanceof Tuple))
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
	}