/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package bioserv.seqserv.io;

public class SeqTools
	{
	private char complement(char c)
		{
		if(c=='a')			return 't';
		else if(c=='t')	return 'a';
		
		else if(c=='c')	return 'g';
		else if(c=='g') return 'c';
		
		else return c;
		}
	
	
	public String reverseComplement(String s)
		{
		//to benchmark: convert all of it, reverse pairwise?
		
		int len=s.length();
		char n[]=new char[len];
		for(int i=0;i<len;i++)
			n[len-i-1]=complement(s.charAt(i));
		return new String(n);
		}
	
	
	
	}
