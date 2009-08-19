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
	public final static byte stateDead=-1; //Computed
	public final static byte stateOpen=0;  //Being computed
	public final static byte stateFar=1;   //Not computed


	@Override
	public EvStack[] exec(EvStack... s)
		{
		EvStack speed=s[0];
		EvStack mask=s[1];
		Tuple<EvStack, Vector3i[][]> ret=runLevelset(speed,mask);
		return new EvStack[]{ret.fst(),collectIntensities(mask,ret.snd())};
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
		double[][] Ddistance=stackDist.getOrigArraysDouble();
		
		byte[][] Sstate=new byte[depth][width*height]; //State of point
		Vector3i[][] Qorigin=new Vector3i[depth][width*height];
		
		double[][] H=null; //heuristic (distance that remains to goal)
		double[][] L=null; //Limit exploration, max distance
		
		FibonacciHeapNode<Vector3i>[][] heap_pool=new FibonacciHeapNode[depth][width*height];
		FibonacciHeap<Vector3i> open_heap = new FibonacciHeap<Vector3i>();
	
		// initialize points
		for( int k=0; k<depth; ++k )
			for( int j=0; j<height; ++j )
				for( int i=0; i<width; ++i )
					{
					Ddistance[k][j*width+i] = Double.MAX_VALUE;
					Sstate[k][j*width+i] = stateFar;
					Qorigin[k][j*width+i] = null;
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
						
						double key=getKeyFor(Ddistance, H, width, v);
						FibonacciHeapNode<Vector3i> pt = new FibonacciHeapNode<Vector3i>(v, key);
						heap_pool[z][y*width+x] = pt;
						open_heap.insert(pt, key);
						
						Ddistance[z][y*width+x] = 0;
						Sstate[z][y*width+x] = stateOpen;
						Qorigin[z][y*width+x] = v; //index into start point list
						}
					}
		

		runLevelsetInternal(width, height, depth, Ddistance, Sstate, Qorigin, W, H, L, heap_pool, open_heap, new HashSet<Vector3i>());

		return Tuple.make(stackDist, Qorigin);
		}


	/**
	 * Run fast marching on general input
	 * 
	 * @param width
	 * @param height
	 * @param depth
	 * @param startPoints Start points and distance to them (normally 0)
	 * @param Ddistance Distances (returned)
	 * @param Sstate State of calculation (returned)
	 * @param Qorigin Origin of shortest path (returned)
	 * @param Wspeed Speed
	 * @param endPointsHash End points, stop calculation if reached
	 */
	@SuppressWarnings("unchecked")
	public static void runLevelset(int width, int height, int depth, Map<Vector3i,Double> startPoints, 
			double[][] Ddistance, byte[][] Sstate, Vector3i[][] Qorigin, double[][] Wspeed, HashSet<Vector3i> endPointsHash)
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
					Ddistance[k][j*width+i] = Double.MAX_VALUE;
					Sstate[k][j*width+i] = stateFar;
					Qorigin[k][j*width+i] = null;
					}
	
	
		// initalize open list
		for(Map.Entry<Vector3i,Double> e:startPoints.entrySet())
			{
			Vector3i v=e.getKey();
			int x = v.x;
			int y = v.y;
			int z = v.z;
	
			if( Ddistance[z][y*width+x]==0 ) //Debug
				throw new RuntimeException("start_points should not contain duplicates.");
	
			double key=getKeyFor(Ddistance, H, width, v);
			FibonacciHeapNode<Vector3i> pt = new FibonacciHeapNode<Vector3i>(v, key);
			heap_pool[z][y*width+x] = pt;
			open_heap.insert(pt, key);
			
			Ddistance[z][y*width+x] = e.getValue();	
			Sstate[z][y*width+x] = stateOpen;
			Qorigin[z][y*width+x] = v; //index into start point list
			}

		runLevelsetInternal(width, height, depth, Ddistance, Sstate, Qorigin, Wspeed, H, L, heap_pool, open_heap, endPointsHash);
		}
		
	/**
	 * 
	 * @param in
	 * @param startPoints Points and initial distance to them
	 * @param W speed (was inversed)
	 */
	private static void runLevelsetInternal(int width, int height, int depth, 
			double[][] Ddistance, byte[][] Sstate, Vector3i[][] Qorigin, double[][] W, double[][] H, double[][] L,
			FibonacciHeapNode<Vector3i>[][] heapPool,
			FibonacciHeap<Vector3i> openHeap,
			HashSet<Vector3i> endPoints)
		{ 
		
		// perform the front propagation
		int numIterations = 0;
		int maxNumberIterations=10000000;
		while( !openHeap.isEmpty() && numIterations<maxNumberIterations)
			{
			numIterations++;
	
			//Take pixel with shortest distance. The idea of fast marching is that
			//we're done once and for all with this position now.
			Vector3i curPoint = openHeap.removeMin().getData();
			int i = curPoint.x;
			int j = curPoint.y;
			int k = curPoint.z;
			heapPool[k][j*width+i] = null;
			Sstate[k][j*width+i] = stateDead;
			
			//System.out.println("Doing point "+cur_point+" Q "+Qorigin[k][j*width+i]);
			
			//Can stop already here if we are at the goal
			if(endPoints.contains(curPoint))
				break;
	
			//Explore each neighbour
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
						a1 = Ddistance[kk][jj*width+(ii+1)];
					if( ii>0 )
						a1 = Math.min( a1, Ddistance[kk][jj*width+(ii-1)] );
					
					double a2 = Double.MAX_VALUE;
					if( jj<height-1 )
						a2 = Ddistance[kk][(jj+1)*width+ii];
					if( jj>0 )
						a2 = Math.min( a2, Ddistance[kk][(jj-1)*width+ii] );
					
					double a3 = Double.MAX_VALUE;
					if( kk<depth-1 )
						a3 = Ddistance[kk+1][jj*width+ii];
					if( kk>0 )
						a3 = Math.min( a3, Ddistance[kk-1][jj*width+ii] );
	
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
					
					// update its distance
					// now the equation is   (a-a1)^2+(a-a2)^2+(a-a3)^2 - P^2 = 0, with a >= a3 >= a2 >= a1.
					// =>    3*a^2 - 2*(a2+a1+a3)*a - P^2 + a1^2 + a3^2 + a2^2
					// => delta = (a2+a1+a3)^2 - 3*(a1^2 + a3^2 + a2^2 - P^2)
					double delta = (a2+a1+a3)*(a2+a1+a3) - 3*(a1*a1 + a2*a2 + a3*a3 - P*P);
					double A1;
					if( delta>=0 )
						A1 = ( a2+a1+a3 + Math.sqrt(delta) )/3.0;
					else
						A1 = 0;
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
					if( Sstate[kk][jj*width+ii] == stateDead )
						{
						// should not happen for fast marching due to sorting
						if( A1<Ddistance[kk][jj*width+ii] )
							{
							System.out.println("should not happen "+Ddistance[kk][jj*width+ii]+" => "+A1);
							Ddistance[kk][jj*width+ii] = A1; //Store new distance
							Qorigin[kk][jj*width+ii] = Qorigin[k][j*width+i]; //Propagate origin

							/*
							//BELOW IS POTENTIALLY DANGEROUS!!
							// Modify the value in the heap
							FibonacciHeapNode<Vector3i> cur_el = heapPool[kk][jj*width+ii];
							double key=getKeyFor(Ddistance, H, width, cur_el.getData());
							openHeap.decreaseKey(cur_el, key);
							*/
							}
						}
					else if( Sstate[kk][jj*width+ii] == stateOpen )
						{
						// check if this way is closer
						if( A1<Ddistance[kk][jj*width+ii] )
							{
							Ddistance[kk][jj*width+ii] = A1; //Store new distance
							Qorigin[kk][jj*width+ii] = Qorigin[k][j*width+i]; //Propagate origin
							
							// Modify the value in the heap
							FibonacciHeapNode<Vector3i> cur_el = heapPool[kk][jj*width+ii];
							double key=getKeyFor(Ddistance, H, width, cur_el.getData());
							openHeap.decreaseKey(cur_el, key);
							}
						}
					else// if( Sstate[kk][jj*width+ii] == stateFar )
						{
						//First time this node is reached. No distance comparison needed
						
						//if( Ddistance[kk][jj*width+ii]!=Double.MAX_VALUE ) //Debug
						//	throw new RuntimeException("Distance must be initialized to Inf"); //Debug  
						
						//Can use L to do heuristics
						if( L==null || A1<=L[kk][jj*width+ii] )
							{
							Sstate[kk][jj*width+ii] = stateOpen;
							// distance must have change.
							Ddistance[kk][jj*width+ii] = A1;
							Qorigin[kk][jj*width+ii] = Qorigin[k][j*width+i];
							
							// add to open list
							Vector3i v=new Vector3i(ii,jj,kk);
							double key=getKeyFor(Ddistance, H, width, v);
							FibonacciHeapNode<Vector3i> pt = new FibonacciHeapNode<Vector3i>(v, key);
							heapPool[kk][jj*width+ii] = pt;
							openHeap.insert(pt, key); 
							}
						}
					//else 
					//	throw new RuntimeException("Unknown state"); 
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
