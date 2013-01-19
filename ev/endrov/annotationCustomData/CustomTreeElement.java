/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.annotationCustomData;

import javax.swing.tree.*;
import org.jdom.*;
import java.util.*;

/**
 * Holder of an XML-node in the tree
 * @author Johan Henriksson
 */
public class CustomTreeElement
	{
	public Element e;
	final public CustomTreeElement parent;
	
	public CustomTreeElement(Element e, CustomTreeElement parent)
		{
		this.e=e;
		this.parent=parent;
		}
	
	public TreePath getPath()
		{
		LinkedList<Object> path=new LinkedList<Object>();
		CustomTreeElement e=this;
		while(e!=null)
			{
			path.addFirst(e);
			e=e.parent;
			}
		return new TreePath(path.toArray());
		}

	
	public boolean isLeaf()
		{
		if(e==null)
			return true;
		else
			return e.getChildren().isEmpty();
		}
	
	public String toString()
		{
		if(e==null)
			return "<empty>";
		else
			{
			StringBuffer out=new StringBuffer();
			out.append(e.getName());
			out.append(" ");
			out.append(e.getText().trim());
			
			for(Object alo:e.getAttributes())
				{
				Attribute a=(Attribute)alo;
				out.append(" ");
				out.append(a.getName());
				out.append("=");
				out.append(a.getValue());
				}
			
			return out.toString();
			}
		}
	}
