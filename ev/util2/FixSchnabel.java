package util2;

import java.io.File;
import java.util.*;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import endrov.data.*;
import endrov.ev.*;
import endrov.nuc.NucLineage;
import endrov.nuc.NucPair;
import endrov.nuc.NucLineage.NucInterp;


//Do not use rigid transforms, use point dist.

//in fitting all to one: possible to store individual rots, average, invert on assembly and hope it cancels

/**
 * Assemble c.e model
 * @author Johan Henriksson
 */
public class FixSchnabel
	{


	
	
	
	
	
	/**
	 * Entry point
	 */
	public static void main(String[] args)
		{
		Log.listeners.add(new StdoutLog());
		EV.loadPlugins();

		
		EvData ref=new EvDataXML("/Volumes/TBU_main02/ostxml/model/stdcelegansNew2.ostxml");

		EvData ost=new EvDataXML("/Volumes/TBU_main02/ostxml/model/asdasdasdsdasd.ostxml");

		
		NucLineage reflin=ref.getObjects(NucLineage.class).iterator().next();
		
		NucLineage lin=ost.getObjects(NucLineage.class).iterator().next();
		
		/*
		//Save reference
		EvDataXML output=new EvDataXML(outputName);
		output.metaObject.clear();
		output.addMetaObject(combinedLin);
		output.saveMeta();
		*/
		
		}

	}
	