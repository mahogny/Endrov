package logobar;


public class LogoEntry implements Comparable {
    
    public LogoEntry(char AA, int height, double freq){
	iAA = AA;
	iHeight = height;
	iFreq = freq;
    }
    
    public char getAA(){
	return iAA;
    }
    
    public int  getHeight(){
	return iHeight;
    }

    public double getFreq(){return iFreq;}
    
    public int compareTo(Object rhs){
	int rhs_height = ((LogoEntry)rhs).getHeight();
	if(iHeight < rhs_height)
	    return -1;
	else if(iHeight == rhs_height)
	    return 0;
	else
	    return 1;
    }

    private int iHeight;
    private char iAA;
    private double iFreq;

}
