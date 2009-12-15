package endrov.frivolous.model;

class Diffusion implements Runnable {

	// Stencil: wensc
	private enum Stencil {
		S00000, S00022, S00112, S00202, S02002, S02024, S02114, S02204, S11002, S11024, S11114, S11204, S20002, S20024, S20114, S20204
	}

	private float[] diffusion;
	private int height, width;
	private float dt = 0.25f;
	private float dx, dy;
	private float my;
	private int[] alpha;
	private Stencil[] stencils;
	private int cnt = 0;
	private int[] area;
	private float diffusion_factor = 1f;
	private float bleach_factor = 0f;

	public Diffusion(ComplexArray input, int[] alpha_array, float speed) {
		width = input.width;
		height = input.height;
		diffusion = input.real;
		alpha = alpha_array;
		diffusion_factor = speed;
		area = new int[width * height];
		stencils = new Stencil[width * height];
		int mask;
		for (int ay = 1; ay < height - 1; ay++)
			for (int ax = 1; ax < width - 1; ax++) {
				int idx = getIdx(ax, ay);
				mask = alpha[idx];
				if (mask != 0) {
					area[cnt] = idx;
					cnt++;

					if (alpha[idx - 1] != mask && alpha[idx + 1] != mask) {
						if (alpha[idx - width] != mask && alpha[idx + width] != mask)
							stencils[idx] = Stencil.S00000;
						else if (alpha[idx - width] != mask)
							stencils[idx] = Stencil.S00022;
						else if (alpha[idx + width] != mask)
							stencils[idx] = Stencil.S00202;
						else
							stencils[idx] = Stencil.S00112;
					} else if (alpha[idx - 1] != mask) {
						if (alpha[idx - width] != mask && alpha[idx + width] != mask)
							stencils[idx] = Stencil.S02002;
						else if (alpha[idx - width] != mask)
							stencils[idx] = Stencil.S02024;
						else if (alpha[idx + width] != mask)
							stencils[idx] = Stencil.S02204;
						else
							stencils[idx] = Stencil.S02114;
					} else if (alpha[idx + 1] != mask) {
						if (alpha[idx - width] != mask && alpha[idx + width] != mask)
							stencils[idx] = Stencil.S20002;
						else if (alpha[idx - width] != mask)
							stencils[idx] = Stencil.S20024;
						else if (alpha[idx + width] != mask)
							stencils[idx] = Stencil.S20204;
						else
							stencils[idx] = Stencil.S20114;
					} else {
						if (alpha[idx - width] != mask && alpha[idx + width] != mask)
							stencils[idx] = Stencil.S11002;
						else if (alpha[idx - width] != mask)
							stencils[idx] = Stencil.S11024;
						else if (alpha[idx + width] != mask)
							stencils[idx] = Stencil.S11204;
						else
							stencils[idx] = Stencil.S11114;
					}

				} else stencils[idx] = Stencil.S00000;

			}
		dx = 1f;
		dy = dx;
		my = dt / (dx * dy) * diffusion_factor;
	}

	public void diffuse() {
		float[] temp = diffusion.clone();
		bleach();
		float dependency;
		/*
		 * for(int x = 1; x<height-1; x++) for(int y = 1; y<width-1; y++){
		 */
		for (int i = 0; i < cnt; i++) {
			int idx = area[i];// getIdx(x,y);//

			switch (stencils[idx]) {
			case S00000:
				dependency = 0;
				break;
			case S00022:
				dependency = 2 * diffusion[idx + width] - 2 * diffusion[idx];
				break;
			case S00112:
				dependency = diffusion[idx - width] + diffusion[idx + width]
						- 2 * diffusion[idx];
				break;
			case S00202:
				dependency = 2 * diffusion[idx - width] - 2 * diffusion[idx];
				break;
			case S02002:
				dependency = 2 * diffusion[idx + 1] - 2 * diffusion[idx];
				break;
			case S02024:
				dependency = 2 * diffusion[idx + 1] + 2
						* diffusion[idx + width] - 4 * diffusion[idx];
				break;
			case S02114:
				dependency = 2 * diffusion[idx + 1] + diffusion[idx - width]
						+ diffusion[idx + width] - 4 * diffusion[idx];
				break;
			case S02204:
				dependency = 2 * diffusion[idx + 1] + 2
						* diffusion[idx - width] - 4 * diffusion[idx];
				break;
			case S11002:
				dependency = diffusion[idx - 1] + diffusion[idx + 1] - 2
						* diffusion[idx];
				break;
			case S11024:
				dependency = diffusion[idx - 1] + diffusion[idx + 1] + 2
						* diffusion[idx + width] - 4 * diffusion[idx];
				break;
			case S11114:
				dependency = diffusion[idx - 1] + diffusion[idx + 1]
						+ diffusion[idx - width] + diffusion[idx + width] - 4
						* diffusion[idx];
				break;
			case S11204:
				dependency = diffusion[idx - 1] + diffusion[idx + 1] + 2
						* diffusion[idx - width] - 4 * diffusion[idx];
				break;
			case S20002:
				dependency = 2 * diffusion[idx - 1] - 2 * diffusion[idx];
				break;
			case S20024:
				dependency = 2 * diffusion[idx - 1] + 2
						* diffusion[idx + width] - 4 * diffusion[idx];
				break;
			case S20114:
				dependency = 2 * diffusion[idx - 1] + diffusion[idx - width]
						+ diffusion[idx + width] - 4 * diffusion[idx];
				break;
			case S20204:
				dependency = 2 * diffusion[idx - 1] + 2
						* diffusion[idx - width] - 4 * diffusion[idx];
				break;
			default:
				dependency = 0;
			}
			temp[idx] = diffusion[idx] + my * dependency;

		}

		/*
		 * float k,l; for(int i = 0; i<cnt; i++){ int idx = area[i];
		 * if(alpha[idx-1]==0) k=2diffusion[idx+1]; else if (alpha[idx+1]==0)
		 * k=2diffusion[idx-1]; else k=diffusion[idx-1]+diffusion[idx+1];
		 * if(alpha[idx-width]==0) l=2diffusion[idx+width]; else if
		 * (alpha[idx+width]==0) l=2diffusion[idx-width]; else
		 * l=diffusion[idx-width]+diffusion[idx+width];
		 * 
		 * temp[idx]=diffusion[idx]+my(k+l-4diffusion[idx]); }
		 */
		diffusion = temp;
	}

	private int getIdx(int x, int y) {
		return y * width + x;
	}

	public ComplexArray getDiffusedArray() {
		return new ComplexArray(diffusion.clone(), null, width, height);
	}

	public void bleach() {
		// real[244+364*width] *= bleach_factor;
		// real[297+396*width] *= bleach_factor;
		// real[338+361*width] *= bleach_factor;
		for (int i = 297; i < 338; i++)
			for (int j = 361; j < 396; j++)
				diffusion[i + j * width] *= bleach_factor;
		for (int i = 150; i < 170; i++)
			for (int j = 230; j < 250; j++)
				diffusion[i + j * width] *= bleach_factor;
	}
	
	class RleSegment{
		public int startx;
		public int endx;
		public RleSegment(int startx, int endx){
			this.startx = startx;
			this.endx = endx;
		}
	}
	
	public void bleach(java.util.List<RleSegment>[] roi) throws IndexOutOfBoundsException{
		for(int ay=0; ay<roi.length; ay++)
			for(RleSegment seg:roi[ay])
				for(int ax=seg.startx; ax<seg.endx; ax++)
					diffusion[ax + ay * width] *= bleach_factor;
			
	}
	
	public void run() {
		float time = 0;
		long start_time = System.currentTimeMillis();
		while (true) {
			for (int i = 0; i < 100; i++) {
				diffuse();
				time += dt;
			}
			try {
				// System.out.println((long)(time*1000)-(System.currentTimeMillis()-start_time));
				Thread.sleep((long) (time * 10)
						- (System.currentTimeMillis() - start_time));
			} catch (Exception e) {/* System.out.println("Nädu"); */
			}
		}
	}
}
