package endrov.unsortedImageFilters;

/**
 * Warping transform: overlay a grid and map x,y,z to x',y',z' using coordinate interpolation.
 * 
 * This can be useful for normalizing gel band images etc
 * 
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class UnWarp
	{

	/*
	 * ImageJ has a plugin called bUnwarpJ
	 * 
	 * http://biocomp.cnb.uam.es/~iarganda/bUnwarpJ/
	 * 
	 * uses splines
	 * http://bigwww.epfl.ch/thevenaz/UnwarpJ/
	 * 
	 * inverting a spline is a mess. find equations.
	 * http://en.wikipedia.org/wiki/B-spline
	 * 3rd order, t^2  http://www.google.com/url?sa=t&source=web&ct=res&cd=5&url=http%3A%2F%2Fwww.cad.zju.edu.cn%2Fhome%2Fzhx%2FGM%2F007%2F00-bscs2.pdf&ei=_8_8SZzONcSF_QbIvcizBA&usg=AFQjCNG1mAX10TSssw5nShwqDGi_Cm-W1w&sig2=S7kF2ySg3jrHdfpdVdq92Q
	 * wikipedia, t^3
	 * 
	 * http://upload.wikimedia.org/math/c/a/0/ca03153859eff1142aa9a1c1b580b716.png
	 * first solve for [t^3 .. 1] (I see a problem with 1). then solve for t.
	 * 1-problem can be solved: subtract base points?
	 * 
	 * NO! solution: map the other direction only. SOLVED. may want to do interpolation.
	 * interpol: as many translations. integrate quad over other image.
	 * 
	 * best library: http://www.sintef.no/Informasjons--og-kommunikasjonsteknologi-IKT/Anvendt-matematikk/Fagomrader/Geometri/Prosjekter/The-SISL-Nurbs-Library/SISL-Homepage/
	 * SISL is in C but very strong(?). 140k lines.
	 * 
	 * https://jgeom.dev.java.net/
	 * good start. files are from 2005-2007
	 * 200902.. license changed
	 * cutting algorithms are on polygon level, not nurbs level
	 * VERY SLOW cutting
	 * 
	 * 
	 */
	
	}
