package endrov.imageset;

/**
 * Swap memory for images. I would normally say that the OS should deal with this, it is much better informed
 * collecting statistics through the MMU. The problem is on 32-bit systems which are going to live for another
 * while, the address space is so small that the swap+main memory might not fit. This is solved by this high-level
 * swapper which will totally unload images and hence free up addresses.
 * 
 * @author Johan Henriksson
 *
 */
public class SwapImages
	{

	//TODO implement
	//Least-recently used. all images have to register in this queue.
	
	}
