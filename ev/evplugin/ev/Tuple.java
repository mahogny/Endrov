package evplugin.ev;

/**
 * This code was taken from a forum post. Can probably be considered public domain. Modified.
 */
public class Tuple<L, R>
	{
	private final L fst;
	private final R snd;

	public R snd() 
		{
		return snd;
		}

	public L fst() 
		{
		return fst;
		}

	public Tuple(final L left, final R right) 
		{
		this.fst = left;
		this.snd = right;
		}

	public static <A, B> Tuple<A, B> create(A left, B right) 
		{
		return new Tuple<A, B>(left, right);
		}

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
}