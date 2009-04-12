package endrov.unsortedImageFilters;

/**
 * Lookup tables. These have been used a lot for 8-bit images but I suspect it's time to bury them.
 * An 8-bit lookup is 256 bytes, a 16-bit lookup 128kb while 32-bit requires 16Gb. It's impossible
 * to apply them to floating point images.
 * 
 * I suggest rather that parameterized maps are used. These consume less space and trade memory I/O
 * for instructions, which are probably cheaper. It also works for floating point. 
 * 
 * @author Johan Henriksson
 *
 */
public class LUT
	{

	}
