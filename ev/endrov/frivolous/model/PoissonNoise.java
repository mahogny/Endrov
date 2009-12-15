package endrov.frivolous.model;

import java.util.Random;

public class PoissonNoise {
	static double max_lambda = -1;
	static private Random rnd;
	
	public static ComplexArray addRealNoise(ComplexArray input, Settings settings){
		double lambda = settings.parameter_noise_lambda; 
		rnd = new Random();
		
		float[] real = new float[input.length];
				
		for(int i=0;i<input.length;i++){
				real[i] = input.real[i]+(float)poisson(lambda);
				if(real[i]>255) real[i] =255;
				else if(real[i]<0) real[i] =0;
			}
	
		return new ComplexArray(real, input.imag, input.width, input.height);
	}

	private static double poisson(double lambda){
	if(lambda < 10){
		/* Make the real poisson calculation (very time consuming for large lambda) */
		double L = Math.exp(-lambda);
		int k = 0;
		double p = 1;
		do{
			p = p * Math.random();
			k++;
		} while (p > L);
		
		return k - 1;} else {
		/* Approximating with normal distribution using built in Gaussian function when lambda > 10 */

		double n = rnd.nextGaussian();
		double p = n*Math.sqrt(lambda)+lambda;

		return p;
		}
	}
}
