package evplugin.roi;

/**
 * Trivial implementation of pixel iterator given a line iterator. A direct implementation is faster but lazy plugin writers can use this
 * for a cheap pixel implementation.
 * @author Johan Henriksson
 */
public class LineToPixelIterator extends PixelIterator
	{
	private final LineIterator line;
	
	public LineToPixelIterator(LineIterator line)
		{
		this.line=line;
		x=-1;
		}
	
	public boolean next()
		{
		if(x==-1 || x==line.endX)
			{
			//Need another scanline
			if(line.next())
				{
				//almost. check where on line we are
				line.next();
				channel=line.channel;
				frame=line.frame;
				x=line.startX;
				y=line.y;
				z=line.z;
				return true;			
				}
			else
				return false;
			}
		else
			{
			x++;
			return true;
			}
		}
	
	}
