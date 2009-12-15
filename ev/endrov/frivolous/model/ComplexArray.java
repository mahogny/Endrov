package endrov.frivolous.model;

public class ComplexArray{
	public int width;
	public int height;
	public float[] real;
	public float[] imag;
	public int length;
	
	public ComplexArray(float[] realArray, float[] imagArray, int w, int h){
		if(imagArray != null && realArray.length != imagArray.length) 
      throw new IllegalArgumentException("Array sizes must agree!");
		if(imagArray != null && realArray.length != w*h)
      throw new IllegalArgumentException("Array sizes must be w * h!");
		real = realArray;
		imag = (imagArray != null ? imagArray : new float[w*h]);
		width=w;
		height=h;
		length=w*h;
	}
}