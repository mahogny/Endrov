/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.utilityUnsorted.transferFunction;

/**
 * Transfer function: Remaps intensity levels.
 * The functions here are based on GNUplot.
 * gnuplot> show palette rgbformulae
 * 
 * 
 * @author Johan Henriksson
 *
 */
public interface TransferFunction
	{
	public double[] apply(double[] arr, double scale);

	
	public static class GnuPlot4 implements TransferFunction
		{
		public double[] apply(double[] arr, double scale)
			{
			double[] out=new double[arr.length];
			for(int i=0;i<arr.length;i++)
				{
				double in=arr[i]*scale;
				out[i]=in*in;
				}
			return out;
			}
		}
	
	public static class GnuPlot5 implements TransferFunction
	{

		public double[] apply(double[] arr, double scale)
			{
			double[] out=new double[arr.length];
			for(int i=0;i<arr.length;i++)
				{
				double in=arr[i]*scale;
				in=in*in;
				in=in*in;
				out[i]=in;
				}
			return out;
			}
	}

	public static class GnuPlot6 implements TransferFunction
	{
	public double[] apply(double[] arr, double scale)
		{
		double[] out=new double[arr.length];
		for(int i=0;i<arr.length;i++)
			{
			double in=arr[i]*scale;
			out[i]=in*in*in;
			}
		return out;
		}
	}

	public static class GnuPlot7 implements TransferFunction
	{
	public double[] apply(double[] arr, double scale)
		{
		double[] out=new double[arr.length];
		for(int i=0;i<arr.length;i++)
			{
			double in=arr[i]*scale;
			out[i]=Math.sqrt(in);
			}
		return out;
		}
	}

	public static class GnuPlot8 implements TransferFunction
	{
	public double[] apply(double[] arr, double scale)
		{
		double[] out=new double[arr.length];
		for(int i=0;i<arr.length;i++)
			{
			double in=arr[i]*scale;
			out[i]=Math.sqrt(Math.sqrt(in));
			}
		return out;
		}
	}

	public static class GnuPlot9 implements TransferFunction
	{
	public double[] apply(double[] arr, double scale)
		{
		double[] out=new double[arr.length];
		for(int i=0;i<arr.length;i++)
			{
			double in=arr[i]*scale;
			out[i]=Math.sin(in*Math.PI/2.0);
			}
		return out;
		}
	}

	public static class GnuPlot10 implements TransferFunction
	{
	public double[] apply(double[] arr, double scale)
		{
		double[] out=new double[arr.length];
		for(int i=0;i<arr.length;i++)
			{
			double in=arr[i]*scale;
			out[i]=Math.cos(in*Math.PI/2.0);
			}
		return out;
		}
	}


	public static class GnuPlot11 implements TransferFunction
	{
	public double[] apply(double[] arr, double scale)
		{
		double[] out=new double[arr.length];
		for(int i=0;i<arr.length;i++)
			{
			double in=arr[i]*scale;
			out[i]=Math.abs(in-0.5);
			}
		return out;
		}
	}

	public static class GnuPlot12 implements TransferFunction
	{
	public double[] apply(double[] arr, double scale)
		{
		double[] out=new double[arr.length];
		for(int i=0;i<arr.length;i++)
			{
			double in=arr[i]*scale;
			in=2*in-1;
			out[i]=in*in;
			}
		return out;
		}
	}

	
	public static class LUT13 implements TransferFunction
	{
	public double[] apply(double[] arr, double scale)
		{
		double[] out=new double[arr.length];
		for(int i=0;i<arr.length;i++)
			{
			double in=arr[i]*scale;
			out[i]=Math.sin(in*Math.PI);
			
			}
		return out;
		}
	}

	public static class GnuPlot14 implements TransferFunction
	{
	public double[] apply(double[] arr, double scale)
		{
		double[] out=new double[arr.length];
		for(int i=0;i<arr.length;i++)
			{
			double in=arr[i]*scale;
			out[i]=Math.abs(Math.cos(in*Math.PI));
			}
		return out;
		}
	}

	public static class GnuPlot15 implements TransferFunction
	{
	public double[] apply(double[] arr, double scale)
		{
		double[] out=new double[arr.length];
		for(int i=0;i<arr.length;i++)
			{
			double in=arr[i]*scale;
			out[i]=Math.sin(in*Math.PI*2);
			}
		return out;
		}
	}

	public static class GnuPlot16 implements TransferFunction
	{
	public double[] apply(double[] arr, double scale)
		{
		double[] out=new double[arr.length];
		for(int i=0;i<arr.length;i++)
			{
			double in=arr[i]*scale;
			out[i]=Math.cos(in*Math.PI*2);
			}
		return out;
		}
	}

	public static class GnuPlot17 implements TransferFunction
	{
	public double[] apply(double[] arr, double scale)
		{
		double[] out=new double[arr.length];
		for(int i=0;i<arr.length;i++)
			{
			double in=arr[i]*scale;
			out[i]=Math.abs(Math.sin(in*Math.PI*2));
			}
		return out;
		}
	}
	
	public static class GnuPlot18 implements TransferFunction
	{
	public double[] apply(double[] arr, double scale)
		{
		double[] out=new double[arr.length];
		for(int i=0;i<arr.length;i++)
			{
			double in=arr[i]*scale;
			out[i]=Math.abs(Math.cos(in*Math.PI*2));
			}
		return out;
		}
	}
	
	public static class GnuPlot19 implements TransferFunction
	{
	public double[] apply(double[] arr, double scale)
		{
		double[] out=new double[arr.length];
		for(int i=0;i<arr.length;i++)
			{
			double in=arr[i]*scale;
			out[i]=Math.abs(Math.sin(in*Math.PI*2*720.0/360.0));
			}
		return out;
		}
	}
	
	public static class GnuPlot20 implements TransferFunction
	{
	public double[] apply(double[] arr, double scale)
		{
		double[] out=new double[arr.length];
		for(int i=0;i<arr.length;i++)
			{
			double in=arr[i]*scale;
			out[i]=Math.abs(Math.cos(in*Math.PI*2*720.0/360.0));
			}
		return out;
		}
	}
	
	
	
	
	
	/**
	 * 
	     0: 0               1: 0.5             2: 1              
	    21: 3x             22: 3x-1           23: 3x-2           
	    24: |3x-1|         25: |3x-2|         26: (3x-1)/2       
	    27: (3x-2)/2       28: |(3x-1)/2|     29: |(3x-2)/2|     
	    30: x/0.32-0.78125 31: 2*x-0.84       32: 4x;1;-2x+1.84;x/0.08-11.5
	    33: |2*x - 0.5|    34: 2*x            35: 2*x - 0.5      
	    36: 2*x - 1        
	 */
	
	}
