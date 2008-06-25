package evplugin.ev;

import java.io.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

/**
 * Additional routines for XML data
 * @author Johan Henriksson
 */
public class EvXMLutils
	{

	public static Document readXML(File filename) throws Exception
		{
//		FileInputStream fileInputStream = new FileInputStream(filename);
		SAXBuilder saxBuilder = new SAXBuilder();
		Document document = saxBuilder.build(filename);
		return document;
		}

	/**
	 * Checks if name and attributes equal, not content
	 */
	public static boolean elementsEqual(Element a, Element b)
		{
		if(a.getName().equals(b.getName()))
			{
			for(Object o:a.getAttributes())
				{
				Attribute attra=(Attribute)o;
				Attribute attrb=b.getAttribute(attra.getName());
				if(attrb==null)
					return false;
				if(!attra.getValue().equals(attrb.getValue()))
					return false;
				}
			for(Object o:b.getAttributes())
				{
				Attribute attrb=(Attribute)o;
				Attribute attra=a.getAttribute(attrb.getName());
				if(attra==null)
					return false;
				if(!attrb.getValue().equals(attra.getValue()))
					return false;
				}
			return true;
			}
		else
			return false;
		}

	/**
	 * Merge two XML-documents
	 * @param to
	 * @param from
	 */
	public static void mergeXML(Element to, Element from)
		{
		//For every element to be added
		for(Object o:from.getChildren())
			{
			Element frome=(Element)o;
			
			//Find matching element in destination
			Element match=null;
			for(Object o2:to.getChildren())
				{
				Element toe=(Element)o2;
				if(elementsEqual(frome, toe))
					{
					match=toe;
					break;
					}
				}
	
			//Add content
			if(match==null)
				to.addContent((Element)frome.clone());
			else
				mergeXML(match, frome);
			}
		}

	/**
	 * Write XML-document to disk
	 */
	public static void writeXmlData(Document doc, File file) throws Exception
		{
		Format format=Format.getPrettyFormat();
		XMLOutputter outputter = new XMLOutputter(format);
		FileWriter writer = new FileWriter(file);
		FileOutputStream writer2=new FileOutputStream(file);
		outputter.output(doc, writer2);
		writer.close();
		}

	
	/**
	 * Write XML-document to disk
	 */
	public static String xmlToString(Document doc) throws Exception
		{
		Format format=Format.getPrettyFormat();
		XMLOutputter outputter = new XMLOutputter(format);
		StringWriter writer = new StringWriter();
		outputter.output(doc, writer);
		return writer.toString();
		}
	
	
	public static Element stringToXml(String s) throws Exception
		{
		SAXBuilder saxBuilder = new SAXBuilder();
		Document document = saxBuilder.build(new StringReader(s));
		Element element = document.getRootElement();
		return element;
		}

	
	}
