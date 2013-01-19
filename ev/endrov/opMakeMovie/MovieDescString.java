/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.opMakeMovie;

import java.io.StringReader;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;

import endrov.util.EvDecimal;

/**
 * Generate descriptions for channels in movies
 * @author Johan Henriksson
 */
public class MovieDescString
	{
	private String desc;
	
	
	public MovieDescString(String s)
		{
		desc=s;
		}
	
	/**
	 * Check if string at least parses
	 */
	public boolean isValidXML()
		{
    try 
    	{
  		SAXBuilder saxBuilder = new SAXBuilder();
  		saxBuilder.build(new StringReader("<a>"+desc+"</a>"));
  		return true;
    	} 
    catch (Exception e) 
    	{
    	return false;
    	} 
		}

	/**
	 * Decode string
	 */
	public String decode(/*Imageset ost, */String currentChannel, EvDecimal currentFrame)
		{
		StringBuffer sb=new StringBuffer();
    try 
    	{
  		SAXBuilder saxBuilder = new SAXBuilder();
  		Document document = saxBuilder.build(new StringReader("<a>"+desc+"</a>"));
  		Element element = document.getRootElement();
  		for(Object o:element.getContent())
  			{
  			if(o instanceof Element)
  				{
  				Element e=(Element)o;
  				
  				if(e.getName().equals("channel"))
  					sb.append(currentChannel);
  				else if(e.getName().equals("frame"))
  					sb.append(currentFrame);
  				else if(e.getName().equals("time"))
  					{
/*  					double startf=0;
  					if(e.getAttribute("sf")!=null)
  						startf=e.getAttribute("sf").getDoubleValue();*/
  					EvDecimal startt=EvDecimal.ZERO;
  					if(e.getAttribute("st")!=null)
  						startt=new EvDecimal(e.getAttributeValue("st"));
  					
  					double secs=currentFrame.subtract(startt).doubleValue();
  					int mins=(int)(secs/60);
  					secs-=mins*60;
  					
  					sb.append(mins);
  					sb.append("m ");
  					sb.append(secs);
  					sb.append("s");
  					}
  				else
  					sb.append("<unknown tag>");
  				}
  			else if(o instanceof Text)
  				{
  				sb.append(((Text)o).getText());
  				}
  			}
    	}
    catch (Exception e) 
    	{
    	e.printStackTrace();
    	sb.append(e.getMessage());
    	} 
		
		return sb.toString();
		}
	
	public static void main(String[] foo)
		{
//		ChannelDescString cs=new ChannelDescString("{channel} ({frame}) {time,1000=0}");
//		System.out.println("-"+cs.decode()+"-");
		MovieDescString cs=new MovieDescString("<channel/> (<frame/>) <> <time sf=\"1000\" st=\"0\"/>");
		System.out.println(cs.isValidXML());
//		System.out.println("-"+cs.decode()+"-");
		}
	
	}
