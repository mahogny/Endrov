package endrov.imageset;

import java.util.TreeMap;

import endrov.util.EvDecimal;

/**
 * 
 * the main reason for keeping stacks is that the stack listing, including metadata, could be generated lazily.
 * many convenience operations could be moved into stack from channel. 
 * 
 * 
 * 
 * 
 * ** old **
 * 
 * have to decide on where resolution goes. stack has res -> image does not. or a link is needed
 * which will give plenty of headache.
 * 
 * if res goes into stack then only stacks should be passed around. 
 * 
 * 
 * 
 * @author tbudev3
 *
 */
public abstract class EvStackNEW
	{
	/*
	int binning;
	double dispX, dispY;
	double resX, resY;
	
	
	public double transformImageWorldX(double c){return (c*getBinning()+getDispX())/getResX();}
	public double transformImageWorldY(double c){return (c*getBinning()+getDispY())/getResY();}			
	public double transformWorldImageX(double c){return (c*getResX()-getDispX())/getBinning();}
	public double transformWorldImageY(double c){return (c*getResY()-getDispY())/getBinning();}
	
	public double scaleImageWorldX(double c){return c/(getResX()/getBinning());}
	public double scaleImageWorldY(double c){return c/(getResY()/getBinning());}
	public double scaleWorldImageX(double c){return c*getResX()/getBinning();}
	public double scaleWorldImageY(double c){return c*getResY()/getBinning();}

	public int getBinning(){return binning;}
	public double getDispX(){return dispX;}
	public double getDispY(){return dispY;}
	public double getResX(){return resX;}
	public double getResY(){return resY;}

	
	public TreeMap<EvDecimal, EvImage> slice=new TreeMap<EvDecimal, EvImage>();*/
	
	//TODO keep track of deleted slices - NO. not here!
	
	/**
	 * 
	 * goal: EvImage cannot be abstract anymore, too costly. instead it will
	 * need access to the loader. the situation could be resolved using a function to get stacks
	 * but maybe this is too cumbersome?
	 * 
	 *  how to update loader? best with a weak pointer to Stack, then from Stack to Imageset
	 *  
	 *  this makes it dangerous to insert images. the process has to be controlled. R-O map for example.
	 * 
	 * 
	 */
	
	//public abstract void insertImage(EvDecimal z, EvImage im);
	
	
	/**
	 * optimization for stack formats:
	 * 
	 * loadAllSlices()
	 */
	
	}
