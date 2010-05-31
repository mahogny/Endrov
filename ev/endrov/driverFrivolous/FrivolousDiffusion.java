/***
 * Copyright (C) 2010 David Johansson & Arvid Johansson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.driverFrivolous;

/**
 * Simulate diffusion
 * 
 * @author David Johansson, Arvid Johansson, Johan Henriksson
 */
class FrivolousDiffusion implements Runnable
	{

	// Stencil: wensc
	private enum Stencil
		{
		S00000, S00022, S00112, S00202, S02002, S02024, S02114, S02204, S11002, S11024, S11114, S11204, S20002, S20024, S20114, S20204
		}

	private float[] diffusion;
	private int height, width;
	private float dt = 0.25f;
	private float dx, dy;
	private float my;
	private int[] alpha;  //TODO wut?
	private Stencil[] stencils;
	private int cnt = 0;
	private int[] area;
	private float diffusionFactor = 1f;

	public FrivolousDiffusion(FrivolousComplexArray input, int[] alpha, float speed)
		{
		width = input.width;
		height = input.height;
		diffusion = input.real;
		this.alpha = alpha;
		diffusionFactor = speed;
		area = new int[width*height];
		stencils = new Stencil[width*height];
		//int mask;
		
		
		//Precalculate mask
		for (int ay = 1; ay<height-1; ay++)
			for (int ax = 1; ax<width-1; ax++)
				{
				int idx = getIdx(ax, ay);
				int mask = alpha[idx];
				if (mask!=0)
					{
					area[cnt] = idx;
					cnt++;

					if (alpha[idx-1]!=mask&&alpha[idx+1]!=mask)
						{
						if (alpha[idx-width]!=mask&&alpha[idx+width]!=mask)
							stencils[idx] = Stencil.S00000;
						else if (alpha[idx-width]!=mask)
							stencils[idx] = Stencil.S00022;
						else if (alpha[idx+width]!=mask)
							stencils[idx] = Stencil.S00202;
						else
							stencils[idx] = Stencil.S00112;
						}
					else if (alpha[idx-1]!=mask)
						{
						if (alpha[idx-width]!=mask&&alpha[idx+width]!=mask)
							stencils[idx] = Stencil.S02002;
						else if (alpha[idx-width]!=mask)
							stencils[idx] = Stencil.S02024;
						else if (alpha[idx+width]!=mask)
							stencils[idx] = Stencil.S02204;
						else
							stencils[idx] = Stencil.S02114;
						}
					else if (alpha[idx+1]!=mask)
						{
						if (alpha[idx-width]!=mask&&alpha[idx+width]!=mask)
							stencils[idx] = Stencil.S20002;
						else if (alpha[idx-width]!=mask)
							stencils[idx] = Stencil.S20024;
						else if (alpha[idx+width]!=mask)
							stencils[idx] = Stencil.S20204;
						else
							stencils[idx] = Stencil.S20114;
						}
					else
						{
						if (alpha[idx-width]!=mask&&alpha[idx+width]!=mask)
							stencils[idx] = Stencil.S11002;
						else if (alpha[idx-width]!=mask)
							stencils[idx] = Stencil.S11024;
						else if (alpha[idx+width]!=mask)
							stencils[idx] = Stencil.S11204;
						else
							stencils[idx] = Stencil.S11114;
						}

					}
				else
					stencils[idx] = Stencil.S00000;

				}
		dx = 1f;
		dy = dx;
		my = dt*diffusionFactor/(dx*dy);
		}

	
	public synchronized void diffuse()
		{
		float[] temp = diffusion.clone();
		/*
		 * for(int x = 1; x<height-1; x++) for(int y = 1; y<width-1; y++){
		 */
		float dependency;
		for (int i = 0; i<cnt; i++)
			{
			int idx = area[i];// getIdx(x,y);//

			switch (stencils[idx])
				{
				case S00000:
					dependency = 0;
					break;
				case S00022:
					dependency = 2*diffusion[idx+width]-2*diffusion[idx];
					break;
				case S00112:
					dependency = diffusion[idx-width]+diffusion[idx+width]-2
							*diffusion[idx];
					break;
				case S00202:
					dependency = 2*diffusion[idx-width]-2*diffusion[idx];
					break;
				case S02002:
					dependency = 2*diffusion[idx+1]-2*diffusion[idx];
					break;
				case S02024:
					dependency = 2*diffusion[idx+1]+2*diffusion[idx+width]-4
							*diffusion[idx];
					break;
				case S02114:
					dependency = 2*diffusion[idx+1]+diffusion[idx-width]
							+diffusion[idx+width]-4*diffusion[idx];
					break;
				case S02204:
					dependency = 2*diffusion[idx+1]+2*diffusion[idx-width]-4
							*diffusion[idx];
					break;
				case S11002:
					dependency = diffusion[idx-1]+diffusion[idx+1]-2*diffusion[idx];
					break;
				case S11024:
					dependency = diffusion[idx-1]+diffusion[idx+1]+2*diffusion[idx+width]
							-4*diffusion[idx];
					break;
				case S11114:
					dependency = diffusion[idx-1]+diffusion[idx+1]+diffusion[idx-width]
							+diffusion[idx+width]-4*diffusion[idx];
					break;
				case S11204:
					dependency = diffusion[idx-1]+diffusion[idx+1]+2*diffusion[idx-width]
							-4*diffusion[idx];
					break;
				case S20002:
					dependency = 2*diffusion[idx-1]-2*diffusion[idx];
					break;
				case S20024:
					dependency = 2*diffusion[idx-1]+2*diffusion[idx+width]-4
							*diffusion[idx];
					break;
				case S20114:
					dependency = 2*diffusion[idx-1]+diffusion[idx-width]
							+diffusion[idx+width]-4*diffusion[idx];
					break;
				case S20204:
					dependency = 2*diffusion[idx-1]+2*diffusion[idx-width]-4
							*diffusion[idx];
					break;
				default:
					dependency = 0;
				}
			temp[idx] = diffusion[idx]+my*dependency;

			}

		/*
		 * float k,l; for(int i = 0; i<cnt; i++){ int idx = area[i];
		 * if(alpha[idx-1]==0) k=2diffusion[idx+1]; else if (alpha[idx+1]==0)
		 * k=2diffusion[idx-1]; else k=diffusion[idx-1]+diffusion[idx+1];
		 * if(alpha[idx-width]==0) l=2diffusion[idx+width]; else if
		 * (alpha[idx+width]==0) l=2diffusion[idx-width]; else
		 * l=diffusion[idx-width]+diffusion[idx+width];
		 * temp[idx]=diffusion[idx]+my(k+l-4diffusion[idx]); }
		 */
		diffusion = temp;
		}

	private int getIdx(int x, int y)
		{
		return y*width+x;
		}

	public FrivolousComplexArray getDiffusedArray()
		{
		return new FrivolousComplexArray(diffusion.clone(), null, width, height);
		}

	public synchronized void bleach(float bleachFactor)
		{
		// real[244+364*width] *= bleach_factor;
		// real[297+396*width] *= bleach_factor;
		// real[338+361*width] *= bleach_factor;
		for (int i = 297+768; i<338+768; i++)
			for (int j = 361+768; j<396+768; j++)
				diffusion[i+j*width] *= bleachFactor;
		for (int i = 150+768; i<170+768; i++)
			for (int j = 230+768; j<250+768; j++)
				diffusion[i+j*width] *= bleachFactor;
		}

	/**
	 * Bleach an arbitrary ROI
	 */
	public synchronized void bleach(int[] roi, int roiWidth, int roiHeight, int offsetX, int offsetY, float bleachFactor)
		{
		offsetX+=768;//temp
		offsetY+=768; 
//		System.out.println("bleach factor roi "+bleachFactor+"   "+roiWidth+" "+roiHeight+" "+offsetX+"  "+offsetY+" "+width);
		for(int y=0;y<roiHeight;y++)
			for(int x=0;x<roiWidth;x++)
				if(roi[x+y*roiWidth]!=0) //temp TODO
					diffusion[(x+offsetX) + (y+offsetY)*width] *= bleachFactor;
		}

	/**
	 * Bleach a square ROI - typically the area the camera focuses on
	 */
	public synchronized void bleach(int roiWidth, int roiHeight, int offsetX, int offsetY, float bleachFactor)
		{
		offsetX+=768;//temp
		offsetY+=768; 
//		System.out.println("bleach factor "+bleachFactor+"   "+roiWidth+" "+roiHeight+" "+offsetX+"  "+offsetY+" "+width);
		for(int y=0;y<roiHeight;y++)
			for(int x=0;x<roiWidth;x++)
				diffusion[(x+offsetX) + (y+offsetY)*width] *= bleachFactor;
		}

	/**
	 * Continuously simulate the physics
	 */
	public void run()
		{
		float time = 0;
		long start_time = System.currentTimeMillis();
		for(;;)
			{
			for (int i = 0; i<100; i++)
				{
				//float bleachFactor = 0f;
				//bleach(bleachFactor);
				diffuse();
				time += dt;
				}
			try
				{
				// System.out.println((long)(time*1000)-(System.currentTimeMillis()-start_time));
				Thread.sleep((long) (time*10)-(System.currentTimeMillis()-start_time));
				}
			catch (Exception e){}
			}
		}
	}
