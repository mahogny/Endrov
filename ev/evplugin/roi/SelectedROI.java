package evplugin.roi;

import evplugin.imageset.*;

/**
 * Selected ROI: a pair of the ROI itself and the imageset it belongs to. Imageset can be null.
 */
public class SelectedROI
	{
	private final Imageset imageset;
	private final ROI roi;
	
	public ROI getROI() 
		{
		return roi;
		}

	public Imageset getImageset() 
		{
		return imageset;
		}

	public SelectedROI(final Imageset left, final ROI right) 
		{
		this.imageset = left;
		this.roi = right;
		}

	public static SelectedROI create(Imageset left, ROI right) 
		{
		return new SelectedROI(left, right);
		}

	public final boolean equals(Object o) 
		{
		if (!(o instanceof SelectedROI))
			return false;

		final SelectedROI other = (SelectedROI) o;
		return equal(getImageset(), other.getImageset()) && equal(getROI(), other.getROI());
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
		int hLeft = getImageset() == null ? 0 : getImageset().hashCode();
		int hRight = getROI() == null ? 0 : getROI().hashCode();
		return hLeft + (57 * hRight);
		}
}