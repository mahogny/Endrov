package util2.nucTracker;

import java.util.LinkedList;
import java.util.List;

public class AdaBoost
	{
	public List<SimpleClassifier> classifiers=new LinkedList<SimpleClassifier>();
	public List<Double> alpha=new LinkedList<Double>();
	
	
	public void addClassifier(double alphat, SimpleClassifier classifier)
		{
		alpha.add(alphat);
		classifiers.add(classifier);
		}
	
	public void dropLastClassifier()
		{
		alpha.remove(alpha.size()-1);
		classifiers.remove(classifiers.size()-1);
		}
	
	
	
	public double eval(TImage tim)
		{
		return eval(tim, tim.w,0,0);
		}
	
	public double eval(TImage tim, int size, int x, int y)
		{
		double sum=0;
		for(int i=0;i<classifiers.size();i++)
			sum+=alpha.get(i)*classifiers.get(i).eval(tim, size, x, y);
		if(sum>0)
			return 1;
		else
			return -1;
		}
	
	public String toString()
		{
		String s="";
		for(int i=0;i<classifiers.size();i++)
			s+=""+alpha.get(i)+": {"+classifiers.get(i).toString()+"}\n";
		return s;
		}
	}