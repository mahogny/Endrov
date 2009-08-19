package endrov.flowLevelsets;

import java.util.HashSet;
import java.util.Map;

import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

import endrov.flow.EvOpStack;
import endrov.imageset.EvPixelsType;
import endrov.imageset.EvStack;
import endrov.util.Tuple;
import endrov.util.Vector3i;

/**
 * 3D levelsets distance calculation by fast marching.
 * 
 * As EvOp, give speed, then starting points as mask (!=0 is a starting point).
 * returns distance and origin color.
 * 
 * @author Gabriel Peyr (original code, BSD license)
 * @author Johan Henriksson (java conversion, BSD license)
 */
public class EvOpLevelsetsFastMarching3D extends EvOpStack
	{
	public final static byte kDead=-1; //Computed
	public final static byte kOpen=0;  //Being computed
	public final static byte kFar=1;   //Not computed


	@Override
	public EvStack[] exec(EvStack... s)
		{
		EvStack speed=s[0];
		EvStack mask=s[1];
		Tuple<EvStack, Vector3i[][]> ret=runLevelset(speed,mask);
		return new EvStack[]{mask,collectIntensities(mask,ret.snd())};
		}

	@Override
	public int getNumberChannels()
		{
		return 2;
		}

	
	/**
	 * Get intensity of origin
	 */
	public static EvStack collectIntensities(EvStack s, Vector3i[][] origin)
		{
		double[][] inArr=s.getReadOnlyArraysDouble();
		int w=s.getWidth();
		int h=s.getHeight();
		int d=s.getDepth();
		EvStack out=new EvStack();
		out.allocate(w, h, d, EvPixelsType.DOUBLE, s);
		double[][] outArr=out.getOrigArraysDouble();
		
		for(int z=0;z<d;z++)
			for(int y=0;y<h;y++)
				for(int x=0;x<w;x++)
					{
					int i=y*w+x;
					Vector3i v=origin[z][i];
					if(v!=null)
						outArr[z][i]=inArr[v.z][v.y*w+v.x];
					}
		return out;
		}
	
	
	/**
	 * Run level sets on stacks. Returns distance and origin location
	 * 
	 * @param stackSpeed Every pixel is speed
	 * @param stackStartPoints Non-zero pixels are start points
	 */
	@SuppressWarnings("unchecked")
	public static Tuple<EvStack, Vector3i[][]> runLevelset(EvStack stackSpeed, EvStack stackStartPoints)
		{
		int width=stackSpeed.getWidth();
		int height=stackSpeed.getHeight();
		int depth=stackSpeed.getDepth();

		double[][] startPointArray=stackStartPoints.getReadOnlyArraysDouble();
		double[][] W=stackSpeed.getReadOnlyArraysDouble();
		
		EvStack stackDist=new EvStack();
		stackDist.allocate(width, height, depth, EvPixelsType.DOUBLE, stackSpeed);
		double[][] D=stackDist.getOrigArraysDouble();
		
		byte[][] S=new byte[depth][width*height]; //State of point
		Vector3i[][] Q=new Vector3i[depth][width*height];
		
		double[][] H=null; //heuristic (distance that remains to goal)
		double[][] L=null; //Limit exploration, max distance
		
		FibonacciHeapNode<Vector3i>[][] heap_pool=new FibonacciHeapNode[depth][width*height];
		FibonacciHeap<Vector3i> open_heap = new FibonacciHeap<Vector3i>();
	
		// initialize points
		for( int k=0; k<depth; ++k )
			for( int j=0; j<height; ++j )
				for( int i=0; i<width; ++i )
					{
					D[k][j*width+i] = Double.MAX_VALUE;
					S[k][j*width+i] = kFar;
					Q[k][j*width+i] = null;
					}
	
		// initalize open list
		for( int z=0; z<depth; ++z )
			for( int y=0; y<height; ++y )
				for( int x=0; x<width; ++x )
					{
					double val=startPointArray[z][y*width+x];
					if(val!=0)
						{
						Vector3i v=new Vector3i(x,y,z);
						
						double key=getKeyFor(D, H, width, v);
						FibonacciHeapNode<Vector3i> pt = new FibonacciHeapNode<Vector3i>(v, key);
						
						heap_pool[z][y*width+x] = pt;
						open_heap.insert(pt, key);
						double dist=0;
						D[z][y*width+x] = dist;
						S[z][y*width+x] = kOpen;
						Q[z][y*width+x] = v; //index into start point list
						}
					}
		

		runLevelsetInternal(width, height, depth, D, S, Q, W, H, L, heap_pool, open_heap, new HashSet<Vector3i>());

		return Tuple.make(stackDist, Q);
		}


	/**
	 * Run fast marching on general input
	 * 
	 * @param width
	 * @param height
	 * @param depth
	 * @param startPoints Start points and distance to them (normally 0)
	 * @param D Distances (returned)
	 * @param S State of calculation (returned)
	 * @param Q Origin of shortest path (returned)
	 * @param W Speed
	 * @param endPointsHash End points, stop calculation if reached
	 */
	@SuppressWarnings("unchecked")
	public static void runLevelset(int width, int height, int depth, Map<Vector3i,Double> startPoints, 
			double[][] D, byte[][] S, Vector3i[][] Q, double[][] W, HashSet<Vector3i> endPointsHash)
		{ 
		double[][] H=null; //heuristic (distance that remains to goal)
		double[][] L=null; //Limit exploration, max distance
		
		FibonacciHeapNode<Vector3i>[][] heap_pool=new FibonacciHeapNode[depth][width*height];
		FibonacciHeap<Vector3i> open_heap = new FibonacciHeap<Vector3i>();
	
		// initialize points
		for( int k=0; k<depth; ++k )
			for( int j=0; j<height; ++j )
				for( int i=0; i<width; ++i )
					{
					D[k][j*width+i] = Double.MAX_VALUE;
					S[k][j*width+i] = kFar;
					Q[k][j*width+i] = null;
					}
	
	
		// initalize open list
		for(Map.Entry<Vector3i,Double> e:startPoints.entrySet())
			{
			Vector3i v=e.getKey();
			int x = v.x;
			int y = v.y;
			int z = v.z;
	
			if( D[z][y*width+x]==0 ) //Debug
				throw new RuntimeException("start_points should not contain duplicates.");
	
			double key=getKeyFor(D, H, width, v);
			FibonacciHeapNode<Vector3i> pt = new FibonacciHeapNode<Vector3i>(v, key);
			
			heap_pool[z][y*width+x] = pt;
			open_heap.insert(pt, key);
			
			D[z][y*width+x] = e.getValue();	//Let value be 0 normally
			S[z][y*width+x] = kOpen;
			Q[z][y*width+x] = v; //index into start point list
			}

		runLevelsetInternal(width, height, depth, D, S, Q, W, H, L, heap_pool, open_heap, endPointsHash);
		}
		
	/**
	 * 
	 * @param in
	 * @param startPoints Points and initial distance to them
	 * @param W speed (was inversed)
	 */
	private static void runLevelsetInternal(int width, int height, int depth, 
			double[][] D, byte[][] S, Vector3i[][] Q, double[][] W, double[][] H, double[][] L,
			FibonacciHeapNode<Vector3i>[][] heap_pool,
			FibonacciHeap<Vector3i> open_heap,
			HashSet<Vector3i> endPointsHash)
		{ 
		
		// perform the front propagation
		int num_iter = 0;
		int nb_iter_max=10000000;
		while( !open_heap.isEmpty() && num_iter<nb_iter_max)
			{
			num_iter++;
	
			// remove from open list and set up state to dead
			Vector3i cur_point = open_heap.removeMin().getData();
			int i = cur_point.x;
			int j = cur_point.y;
			int k = cur_point.z;
			heap_pool[k][j*width+i] = null;
			S[k][j*width+i] = kDead;
			
			System.out.println("Doing point "+cur_point+" Q "+Q[k][j*width+i]);
			
			//Can stop already here if we are at the goal
			if(endPointsHash.contains(cur_point))
				break;
	
			// recurse on each neighbor
			int[] nei_i = new int[]{i+1,i,i-1,i,i,i};
			int[] nei_j = new int[]{j,j+1,j,j-1,j,j};
			int[] nei_k = new int[]{k,k,k,k,k-1,k+1};
			for( int s=0; s<6; ++s )
				{
				int ii = nei_i[s];
				int jj = nei_j[s];
				int kk = nei_k[s];
	
				boolean bInsert = true;
				//bInsert = callback_insert_node(i,j,k,ii,jj,kk);
				//This can be used to implement early stopping
	
				if( ii>=0 && jj>=0 && ii<width && jj<height && kk>=0 && kk<depth && bInsert )
					{
					double P = W[kk][jj*width+ii]; //no longer inversed speed
					//double P = 1.0/W[kk][jj*width+ii];
					//double P = h/W[kk][jj*width+ii];
					// compute its neighboring values
					double a1 = Double.MAX_VALUE;
					if( ii<width-1 )
						a1 = D[kk][jj*width+(ii+1)];
					if( ii>0 )
						a1 = Math.min( a1, D[kk][jj*width+(ii-1)] );
					double a2 = Double.MAX_VALUE;
					if( jj<height-1 )
						a2 = D[kk][(jj+1)*width+ii];
					if( jj>0 )
						a2 = Math.min( a2, D[kk][(jj-1)*width+ii] );
					double a3 = Double.MAX_VALUE;
					if( kk<depth-1 )
						a3 = D[kk+1][jj*width+ii];
					if( kk>0 )
						a3 = Math.min( a3, D[kk-1][jj*width+ii] );
	
					// order so that a1<a2<a3
					if(a2>a3)
						{
						double tmp=a3;
						a3=a2;
						a2=tmp;
						}
					if(a1>a2)
						{
						double tmp=a1;
						a1=a2;
						a2=tmp;
						}
					if(a2>a3)
						{
						double tmp=a2;
						a2=a3;
						a3=tmp;
						}
					
					if(a1>a2 || a2>a3 || a1>a3)
						System.out.println("Sort error"); //Debug
					
	
					// update its distance
					// now the equation is   (a-a1)^2+(a-a2)^2+(a-a3)^2 - P^2 = 0, with a >= a3 >= a2 >= a1.
					// =>    3*a^2 - 2*(a2+a1+a3)*a - P^2 + a1^2 + a3^2 + a2^2
					// => delta = (a2+a1+a3)^2 - 3*(a1^2 + a3^2 + a2^2 - P^2)
					double delta = (a2+a1+a3)*(a2+a1+a3) - 3*(a1*a1 + a2*a2 + a3*a3 - P*P);
					double A1 = 0;
					if( delta>=0 )
						A1 = ( a2+a1+a3 + Math.sqrt(delta) )/3.0;
					if( A1<=a3 )
						{
						// at least a3 is too large, so we have
						// a >= a2 >= a1  and  a<a3 so the equation is 
						//		(a-a1)^2+(a-a2)^2 - P^2 = 0
						//=> 2*a^2 - 2*(a1+a2)*a + a1^2+a2^2-P^2
						// delta = (a2+a1)^2 - 2*(a1^2 + a2^2 - P^2)
						delta = (a2+a1)*(a2+a1) - 2*(a1*a1 + a2*a2 - P*P);
						A1 = 0;
						if( delta>=0 )
							A1 = 0.5 * ( a2+a1 +Math.sqrt(delta) );
						if( A1<=a2 )
							A1 = a1 + P;
						}
					// update the value
					if( S[kk][jj*width+ii] == kDead )
						{
						if( A1<D[kk][jj*width+ii] )	// should not happen for FM
							{
							D[kk][jj*width+ii] = A1;
							Q[kk][jj*width+ii] = Q[k][j*width+i];
							}
						}
					else if( S[kk][jj*width+ii] == kOpen )
						{
						// check if this way is closer
						if( A1<D[kk][jj*width+ii] )
							{
							D[kk][jj*width+ii] = A1;
							Q[kk][jj*width+ii] = Q[k][j*width+i];
							// Modify the value in the heap
							FibonacciHeapNode<Vector3i> cur_el = heap_pool[kk][jj*width+ii];
							if( cur_el==null ) //Debug
								throw new RuntimeException("Error in heap pool allocation."); 							
							open_heap.decreaseKey(cur_el, getKeyFor(D, H, width, cur_el.getData()));
							}
						}
					else if( S[kk][jj*width+ii] == kFar )
						{
						if( D[kk][jj*width+ii]!=Double.MAX_VALUE ) //Debug
							throw new RuntimeException("Distance must be initialized to Inf");  
						if( L==null || A1<=L[kk][jj*width+ii] )
							{
							S[kk][jj*width+ii] = kOpen;
							// distance must have change.
							D[kk][jj*width+ii] = A1;
							Q[kk][jj*width+ii] = Q[k][j*width+i];
							// add to open list
							Vector3i v=new Vector3i(ii,jj,kk);
							double key=getKeyFor(D, H, width, v);
							FibonacciHeapNode<Vector3i> pt = new FibonacciHeapNode<Vector3i>(v, key);
							heap_pool[kk][jj*width+ii] = pt;
							open_heap.insert(pt, key); 
							}
						}
					else 
						throw new RuntimeException("Unknown state"); 
					}	
				}		
			}			
		}

	
	/**
	 * Get key (distance) for a pixel
	 */
	private static double getKeyFor(double[][] D, double[][] H, int width, Vector3i v)
		{
		if( H==null )
			return D[v.z][v.y*width+v.x];
		else
			return D[v.z][v.y*width+v.x]+H[v.z][v.y*width+v.x];
		}


	}
