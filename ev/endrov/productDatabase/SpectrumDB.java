/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.productDatabase;

import java.util.HashMap;


/**
 * 
 * @author Johan Henriksson
 * 
 * 
 * 
 * 
http://info.med.yale.edu/genetics/ward/tavi/FISHdyes2.html

http://www.online-spectra.com/applet.html

http://home.earthlink.net/~geomcnamara/spectra_links.htm

http://lamp3.tugraz.at/~fluorbase/

http://turmac13.chem.columbia.edu/fretview/test.dal

http://www.jacksonimmuno.com/technical/DyLight.asp
http://www.jacksonimmuno.com/technical/f-fitc.asp
http://www.jacksonimmuno.com/technical/spectratext.asp

http://www.mcb.arizona.edu/ipc/fret/   has MSACCESS db


chroma


 *
 */
public class SpectrumDB
	{

	// fluorophore -> emission, absorbance
	
	// filter OR camera or lens -> transmission
	
	
	
	
	
	public static HashMap<Double,Double> transmit(HashMap<Double,Double> in, HashMap<Double,Double> transmit)
		{
		return in; //TODO
		}

	
	//Competitive absorbtion
	
	/*
	public static HashMap<Double,Double> emit(HashMap<Double,Double> in, HashMap<Double,Double> absorb, HashMap<Double,Double> emit)
		{
		return in; //TODO
		}
		*/
	
	
	
	
	}
