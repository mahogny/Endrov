package evplugin.embrot;
import java.util.*;
import org.jdom.*;

import evplugin.basicWindow.*;
import evplugin.script.*;
import evplugin.metadata.*;
import evplugin.nuc.*;
import evplugin.ev.*;

/**
 * Calculate embryo rotation
 * @author Johan Henriksson
 */
public class CmdEmbrot extends Command
	{
	public static void initPlugin() {}
	static
		{
		Script.addCommand("embrot", new CmdEmbrot());
		}
	
	
	public int numArg()	{return 0;}
	public Exp exec(Vector<Exp> arg) throws Exception
		{
		NucLineage lin=NucLineage.getSelectedLineage();
		if(lin!=null)
			{
			//Make a deep copy
			NucLineage linrot=(NucLineage)lin.clone();

//			rotate(lin);

			Element e=new Element("embrot");

			//Rotate embryo to standard position
			rotate(linrot,e);
			
			
			CustomObject embrot=new CustomObject(e);
			
			
			
			
			Metadata.getSelectedMetadata().addMetaObject(embrot);
			//Metadata.getSelectedMetadata().addMetaObject(linrot);
			
			//Rotate
			
			
			
			//Write XML
			
			//actually, store new nuclin for comparison
			
			
			
			BasicWindow.updateWindows();
			}
		else
			Log.printError("Select lineage", null);
		return null;
		}
	
	
	public void rotate(NucLineage lin, Element e)
		{
		//Put posterior in center
		NucLineage.NucPos postp=lin.nuc.get("post").pos.get(lin.nuc.get("post").pos.firstKey());
		double postX=postp.x, postY=postp.y, postZ=postp.z;
		for(NucLineage.Nuc n:lin.nuc.values())
			for(NucLineage.NucPos p:n.pos.values())
				{
				p.x-=postX;
				p.y-=postY;
				p.z-=postZ;
				}

		//Remove rotation in x-y plane. This rotation is not of interest
		//y for anterior will become 0
		NucLineage.NucPos antp=lin.nuc.get("ant").pos.get(lin.nuc.get("ant").pos.firstKey());
		double angleXY=-Math.atan2(antp.y, antp.x);
		double m11=Math.cos(angleXY), m12=-Math.sin(angleXY);
		double m21=Math.sin(angleXY), m22=Math.cos(angleXY);
		for(NucLineage.Nuc n:lin.nuc.values())
			for(NucLineage.NucPos p:n.pos.values())
				{
				double x=m11*p.x+m12*p.y;
				double y=m21*p.x+m22*p.y;
				p.x=x;
				p.y=y;
				}

		//remove x-z rotation. this one is more interesting
		//z=0 for anterior
		double angleXZ=-Math.atan2(antp.z, antp.x);		
		m11=Math.cos(angleXZ); m12=-Math.sin(angleXZ);
		m21=Math.sin(angleXZ); m22=Math.cos(angleXZ);
		for(NucLineage.Nuc n:lin.nuc.values())
			for(NucLineage.NucPos p:n.pos.values())
				{
				double x=m11*p.x+m12*p.z;
				double z=m21*p.x+m22*p.z;
				p.x=x;
				p.z=z;
				}


		//Normalize size of embryo length-wise
		double embryoLength=antp.x;
		for(NucLineage.Nuc n:lin.nuc.values())
			for(NucLineage.NucPos p:n.pos.values())
				{
				p.x/=embryoLength;
				p.y/=embryoLength;
				p.z/=embryoLength;
				p.r/=embryoLength;
				}


		//Calculate angle around yz for every nuc
		double rotatedXY=-angleXY;
		double rotatedXZ=-angleXZ;
/*
		for i=1:numnuc
		        emb.dat(i,6)=atan2(emb.dat(i,5), emb.dat(i,4));
		end
*/
		
		Element exy=new Element("rotXY");
		Element exz=new Element("rotXZ");
		Element elen=new Element("length");
		exy.addContent(""+rotatedXY);
		exz.addContent(""+rotatedXZ);
		elen.addContent(""+embryoLength);
		e.addContent(exy);
		e.addContent(exz);
		e.addContent(elen);
		}
	
	}
