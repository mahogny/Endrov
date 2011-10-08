/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.lineage;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.vecmath.Vector3d;
import org.jdom.*;

import endrov.basicWindow.BasicWindow;
import endrov.data.*;
import endrov.ev.*;
import endrov.keyBinding.KeyBinding;
import endrov.lineage.expression.ParticleDialogIntegrate;
import endrov.mesh3d.Mesh3D;
import endrov.util.EvDecimal;
import endrov.util.EvFileUtil;
import endrov.util.EvXmlUtil;
import endrov.util.Tuple;



/**
 * Meta object: Lineage (particles) and expression info
 * @author Johan Henriksson
 */
public class Lineage extends EvObject implements Cloneable
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	private static final String metaType="nuclineage";

	public static LineageParticleGrouping cellGroups=new LineageParticleGrouping();
	
	public static final int KEY_TRANSLATE=KeyBinding.register(new KeyBinding("Lineage","Translate",'z'));
	public static final int KEY_CHANGE_RADIUS=KeyBinding.register(new KeyBinding("Lineage","Change radius",'c'));
	public static final int KEY_SETZ=KeyBinding.register(new KeyBinding("Lineage","Set Z",'x'));
	public static final int KEY_DIVIDENUC=KeyBinding.register(new KeyBinding("Lineage","Divide particle",'v'));
	public static final int KEY_SETEND=KeyBinding.register(new KeyBinding("Lineage","Set end frame",'b'));
	public static final int KEY_SETSTART=KeyBinding.register(new KeyBinding("Lineage","Set start frame",'n'));
	public static final int KEY_MAKEPARENT=KeyBinding.register(new KeyBinding("Lineage","Make parent",'g'));
	public static final int KEY_SETPARENT=KeyBinding.register(new KeyBinding("Lineage","Associate parent",'p'));
	
	//A generalization would be nice
	public static final String[] connectNuc=new String[]{"post","ant"};
	
	
	public static Collection<Lineage> getParticles(EvContainer meta)
		{
		if(meta==null)
			return new Vector<Lineage>();
		else
			return meta.getObjects(Lineage.class);
		}

	

	public HashMap<String, Particle> particle=new HashMap<String, Particle>();

	
	
	/**
	 * Create parent-children relation based on selected particles
	 */
	public void createParentChild(String parent, String child)
		{
		Particle parentn=particle.get(parent);
		Particle childn=particle.get(child);
		if(parentn!=null && childn!=null)
			{
			//if(childn.parent!=null)
				//particle.get(childn.parent).child.remove(child);
			childn.parents.add(parent);
			parentn.child.add(child);
			}
		setMetadataModified();
		}
		
		
	/**
	 * Load color scheme from a file
	 */
	public void loadColorScheme(File filename) throws Exception
		{
		Document doc=EvXmlUtil.readXML(filename);
		Element root=doc.getRootElement();
		for(Object oc:root.getChildren())
			{
			Element e=(Element)oc;
			String name=e.getAttributeValue("name");
			int r=e.getAttribute("r").getIntValue();
			int g=e.getAttribute("g").getIntValue();
			int b=e.getAttribute("b").getIntValue();
			if(particle.containsKey(name))
				particle.get(name).color=new Color(r,g,b);
			}
		}

	/**
	 * Save color scheme to a file
	 */
	public void saveColorScheme(File filename) throws Exception
		{
		Element root=new Element("nuccolor");
		Document doc=new Document(root);
		for(Map.Entry<String, Particle> entry:particle.entrySet())
			if(entry.getValue().color!=null)
				{
				Color c=entry.getValue().color;
				Element e=new Element("coloring");
				e.setAttribute("name",entry.getKey());
				e.setAttribute("r",""+c.getRed());
				e.setAttribute("g",""+c.getGreen());
				e.setAttribute("b",""+c.getBlue());
				root.addContent(e);
				}
		EvXmlUtil.writeXmlData(doc, filename);
		}

	/**
	 * Bring up dialog to save color scheme
	 */
	public void saveColorSchemeDialog(Component parent)
		{
		JFileChooser fc=new JFileChooser(EvData.getLastDataPath());
		if(fc.showSaveDialog(parent)==JFileChooser.APPROVE_OPTION)
			{
			File filename=fc.getSelectedFile();
			if(!filename.getName().endsWith(".nuccol"))
				filename=new File(filename.getParentFile(),filename.getName()+".nuccol");
			try
				{
				saveColorScheme(filename);
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			}
		}

	/**
	 * Bring up dialog to save color scheme
	 */
	public void loadColorSchemeDialog(Component parent)
		{
		JFileChooser fc=new JFileChooser(EvData.getLastDataPath());
		if(fc.showOpenDialog(parent)==JFileChooser.APPROVE_OPTION)
			{
			File filename=fc.getSelectedFile();
			try
				{
				loadColorScheme(filename);
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			}
		}

	
	
	/**
	 * Make a deep copy
	 */
	public Object clone()
		{
		Lineage lin=new Lineage();
		for(String nkey:particle.keySet())
			lin.particle.put(nkey, (Particle)particle.get(nkey).clone());
		return lin;
		}
	
	
	/**
	 * Description of this metatype 
	 */
	public String getMetaTypeDesc()
		{
		return metaType;
		}
	
	/**
	 * Save down data
	 */
	public String saveMetadata(Element e)
		{
		for(String nucName:particle.keySet())
			{
			Particle n=particle.get(nucName);
			Element nuce=new Element("nuc");
			e.addContent(nuce);
			nuce.setAttribute("name", nucName);
			if(n.overrideEnd!=null) nuce.setAttribute("end", ""+n.overrideEnd);
			if(n.overrideStart!=null) nuce.setAttribute("start", ""+n.overrideStart);
			if(n.description!=null) nuce.setAttribute("desc",n.description);
			
			for(EvDecimal frame:n.pos.keySet())
				{
				ParticlePos pos=n.pos.get(frame);
				Element pose=new Element("pos");
				nuce.addContent(pose);
				pose.setAttribute("f", ""+frame);
				pose.setAttribute("x", ""+pos.x);
				pose.setAttribute("y", ""+pos.y);
				pose.setAttribute("z", ""+pos.z);
				pose.setAttribute("r", ""+pos.r);
				}

			for(EvDecimal frame:n.events.keySet())
				{
				String eventName=n.events.get(frame);
				Element evente=new Element("event");
				nuce.addContent(evente);
				evente.setAttribute("f", ""+frame);
				evente.setAttribute("n", ""+eventName);
				}
			
			for(String child:n.child)
				{
				Element childe=new Element("child");
				childe.setAttribute("name", child);
				nuce.addContent(childe);
				}
			
			for(Map.Entry<String, LineageExp> entry:n.exp.entrySet())
				{
				Element childe=new Element("exp");
				childe.setAttribute("name", entry.getKey());
				if(entry.getValue().unit!=null)
					childe.setAttribute("unit", entry.getValue().unit);
				nuce.addContent(childe);
				for(Map.Entry<EvDecimal, Double> ve:entry.getValue().level.entrySet())
					{
					Element value=new Element("v");
					value.setAttribute("f",""+ve.getKey());
					value.setAttribute("l",""+ve.getValue());
					childe.addContent(value);
					}
				}
			
			}
		return metaType;
		}

	public void loadMetadata(Element e)
		{
		try
			{
			for(Element nuce:EV.castIterableElement(e.getChildren()))
				{
				String nucName=nuce.getAttributeValue("name");
				String ends=nuce.getAttributeValue("end");
				String starts=nuce.getAttributeValue("start");
				Particle n=getCreateParticle(nucName);
				if(ends!=null) n.overrideEnd=new EvDecimal(ends);
				if(starts!=null) n.overrideStart=new EvDecimal(starts);
				n.description=nuce.getAttributeValue("desc");	
				
				for(Element pose:EV.castIterableElement(nuce.getChildren()))
					{
					if(pose.getName().equals("pos"))
						{
						EvDecimal frame=new EvDecimal(pose.getAttribute("f").getValue());
						double posx=pose.getAttribute("x").getDoubleValue();
						double posy=pose.getAttribute("y").getDoubleValue();
						double posz=pose.getAttribute("z").getDoubleValue();
						double posr=pose.getAttribute("r").getDoubleValue();
						ParticlePos pos=new ParticlePos();
						pos.setPosCopy(new Vector3d(posx,posy,posz));
						pos.r=posr;
						n.pos.put(frame, pos);
						}
					else if(pose.getName().equals("event"))
						{
						EvDecimal frame=new EvDecimal(pose.getAttribute("f").getValue());
						String eventName=pose.getAttributeValue("n");
						n.events.put(frame, eventName);
						}
					else if(pose.getName().equals("child"))
						{
						String child=pose.getAttributeValue("name");
						n.child.add(child);
						}
					else if(pose.getName().equals("exp"))
						{
						String expName=pose.getAttributeValue("name");
						LineageExp exp=new LineageExp();
						exp.unit=pose.getAttributeValue("unit");
						n.exp.put(expName, exp);
						for(Element expe:EV.castIterableElement(pose.getChildren()))
							{
							EvDecimal frame=new EvDecimal(expe.getAttribute("f").getValue());
							double level=expe.getAttribute("l").getDoubleValue();
							exp.level.put(frame, level);
							}
						}
					}
				}
			}
		catch (DataConversionException e1)
			{
			e1.printStackTrace();
			}
		
		//Restore parent relations
		for(String parentName:particle.keySet())
			{
			Particle parent=particle.get(parentName);
			for(String childName:parent.child)
				{
				Particle child=particle.get(childName);
				if(child==null)
					EvLog.printError("Missing child: "+childName, null);
				child.parents.add(parentName);
				}
			}
		}

	
	/** 
	 * Get a particle; create if needed 
	 */
	public Particle getCreateParticle(String name)
		{
		Particle n=particle.get(name);
		if(n==null)
			particle.put(name,n=new Particle());
		return n;
		}
	


	
	/**
	 * Delete all positions after or equal to the current frame. If there are no more positions,
	 * remove the particle as well
	 */
	public void removePosAfter(String name, EvDecimal frame, boolean alsoEqual)
		{
		Particle n=particle.get(name);
		if(n!=null)
			{
			List<EvDecimal> todel=new LinkedList<EvDecimal>();
			
			for(EvDecimal f:n.pos.keySet())
				{
				if(alsoEqual)
					{
					if(f.greaterEqual(frame))
						todel.add(f);
					}
				else
					{
					if(f.greater(frame))
						todel.add(f);
					}
				}
			for(EvDecimal f:todel)
				n.pos.remove(f);
			
			if(n.pos.isEmpty())
				removeParticle(name);
			}
		setMetadataModified();
		}
	/**
	 * Delete all positions before or equal to the current frame. If there are no more positions,
	 * remove the particle as well
	 */
	public void removePosBefore(String name, EvDecimal frame, boolean alsoEqual)
		{
		Particle n=particle.get(name);
		if(n!=null)
			{
			List<EvDecimal> todel=new LinkedList<EvDecimal>();
			
			for(EvDecimal f:n.pos.keySet())
				{
				if(alsoEqual)
					{
					if(f.lessEqual(frame))
						todel.add(f);
					}
				else
					{
					if(f.less(frame))
						todel.add(f);
					
					}
				}
			for(EvDecimal f:todel)
				n.pos.remove(f);
			
			if(n.pos.isEmpty())
				removeParticle(name);
			}
		setMetadataModified();
		}

	/**
	 * Remove a particle. Cleans up child references
	 */
	public void removeParticle(String name)
		{
		//Can also just do parent. but this will automatically fix problems if there is a glitch
		particle.remove(name);
		for(Particle n:particle.values())
			{
			n.child.remove(name);
			n.parents.remove(name);
			}
		setMetadataModified();
		}
	

	/**
	 * Get all interpolated particles
	 */
	public Map<LineageSelParticle, InterpolatedParticle> interpolateParticles(EvDecimal frame)
		{
		HashMap<LineageSelParticle, InterpolatedParticle> nucs=new HashMap<LineageSelParticle, InterpolatedParticle>();
		for(String nucName:particle.keySet())
			{
			Particle n=particle.get(nucName);
			InterpolatedParticle inter=n.interpolatePos(frame);
			if(inter!=null)
				nucs.put(new LineageSelParticle(this, nucName), inter);
			}
		return nucs;
		}

	/**
	 * Get an unused name for a particle
	 */
	public String getUniqueParticleName()
		{
		int i=0;
		while(particle.get(":"+i)!=null)
			i++;
		return ":"+i;
		}
	
	/**
	 * Rename particles
	 */
	public boolean renameParticles(String oldName, String newName)
		{
		Particle n=particle.get(oldName);
		if(n==null || (particle.get(newName)!=null && !oldName.equals(newName)))
			return false;
		else
			{
			particle.remove(oldName);
			particle.put(newName, n);
			updateNameReference(oldName, newName);
			setMetadataModified();
			return true;
			}
		}
	
	/**
	 * Merge particles. The target is removed, source stays and contains everything from the target
	 * TODO also handle expression pattern?
	 */	
	public void mergeParticles(String sourceName, String targetName)
		{
		Particle ns=particle.get(sourceName);
		Particle nt=particle.get(targetName);

		//If either is a direct parent then break this link first
		if(ns.parents.contains(targetName))
			{
			ns.parents.remove(targetName);
			nt.child.remove(sourceName);
			}
		else if(ns.child.contains(targetName))
			{
			nt.parents.remove(sourceName);
			ns.child.remove(targetName);
			}
		
		//Get all parents
		Set<String> theParents=new HashSet<String>();
		theParents.addAll(ns.parents);
		theParents.addAll(nt.parents);

		//Get all children
		Set<String> theChildren=new HashSet<String>();
		theChildren.addAll(ns.child);
		theChildren.addAll(nt.child);

		//Unlink everything
		removeAllParentReference(sourceName);
		removeAllParentReference(targetName);
		for(String childName:theChildren)
			removeAllParentReference(childName);
		
		//Remove particles, add new particle with the best name
		String newName=sourceName;
		if(sourceName.startsWith(":"))
			newName=targetName;
		particle.remove(sourceName);
		particle.remove(targetName);
		particle.put(newName,ns);
		
		//Pull in coordinates
		ns.overrideEnd=null;
		ns.pos.putAll(nt.pos);
		
		//Associate parents. This cannot cause loops because there are no children yet
		ns.parents.addAll(theParents);
		for(String parentName:theParents)
			particle.get(parentName).child.add(newName);
		
		//Associate children - this can cause loops, should be checked!
		for(String childName:theChildren)
			associateParentChildCheckNoLoop(newName, childName);

		setMetadataModified();
		}

	
	public void associateParentChildCheckNoLoop(String parent, String child)
		{
		/*
		if(particle.get(child).parent!=null)
			System.out.println("Parent already exists, not associating");
		else
			{
			*/
			HashSet<String> visited=new HashSet<String>();
			visited.add(parent);
			if(!checkForLoops(visited, child))
				{
				//Safe to add
				particle.get(parent).child.add(child);
				particle.get(child).parents.add(parent);
				}
			else
				System.out.println("Loop detected, not associating");
			//}
		}
	
	private boolean checkForLoops(Set<String> visited, String current)
		{
		Particle n=particle.get(current);
		if(n==null)
			throw new RuntimeException("Particle "+current+" does not exist");
		if(visited.contains(current))
			return true;
		visited.add(current);
		for(String child:n.child)
			{
			if(particle.get(child)==null)
				throw new RuntimeException("Particle "+current+" has null child: "+child);
			if(checkForLoops(visited, child))
				return true;
			}
		return false;
		}
	
	
	/**
	 * Remove all parent references from particle
	 */
	public void removeAllParentReference(String particleName)
		{
		Particle child=particle.get(particleName);
		Set<String> parentNames=new TreeSet<String>(child.parents);
		child.parents.clear();
		for(String parentName:parentNames)
			particle.get(parentName).child.remove(particleName);
		setMetadataModified();
		}

	
	/**
	 * Update a reference to a particle name. Does not touch the corresponding particle.
	 */
	private void updateNameReference(String oldName, String newName)
		{
		for(Particle on:particle.values())
			{
			if(on.parents.contains(oldName))
				{
				on.parents.remove(oldName);
				on.parents.add(newName);
				}
			
			if(on.child.contains(oldName))
				{
				on.child.remove(oldName);
				on.child.add(newName);
				}
			
			LineageSelParticle oldref=new LineageSelParticle(this,oldName);
			if(EvSelection.isSelected(oldref))
				{
				EvSelection.unselect(oldref);
				EvSelection.select(new LineageSelParticle(this,newName));
				}
			}
		setMetadataModified();
		}
	
	

	/**
	 * Find the first keyframe and particle ever mentioned in a lineage object
	 */
	public Tuple<EvDecimal, String> firstFrameOfLineage()
		{
		Tuple<EvDecimal, String> found=null;
		for(Map.Entry<String, Particle> n:particle.entrySet())
			if(found==null || (!n.getValue().pos.isEmpty() && n.getValue().getFirstFrame().less(found.fst())))
				found=new Tuple<EvDecimal, String>(n.getValue().getFirstFrame(),n.getKey());
		return found;
		}

	/**
	 * Find the last keyframe and particle ever mentioned in a lineage object
	 */
	public Tuple<EvDecimal, String> lastFrameOfLineage()
		{
		Tuple<EvDecimal, String> found=null;
		for(Map.Entry<String, Particle> n:particle.entrySet())
			if(found==null || (!n.getValue().pos.isEmpty() && n.getValue().getLastFrame().greater(found.fst())))
				found=new Tuple<EvDecimal, String>(n.getValue().getLastFrame(),n.getKey());
		return found;
		}

	
	
	/******************************************************************************************************
	 *                               Class ParticlePos                                                    *
	 *****************************************************************************************************/

	/**
	 * Position key frame
	 */
	public static class ParticlePos
		{
		public double x,y,z,r;
		
		public double[] ovaloidAxisLength;
		public Vector3d[] ovaloidAxisVec;
		
		public Vector3d getPosCopy()
			{
			return new Vector3d(x,y,z);
			}
		
		/**
		 * Set position. Copy coordinates from v
		 */
		public void setPosCopy(Vector3d v)
			{
			x=v.x;
			y=v.y;
			z=v.z;
			}
		
		public ParticlePos clone()
			{
			ParticlePos n=new ParticlePos();
			n.x=x;
			n.y=y;
			n.z=z;
			n.r=r;
			if(ovaloidAxisLength!=null)
				{
				n.ovaloidAxisLength=new double[3];
				n.ovaloidAxisVec=new Vector3d[3];
				for(int i=0;i<3;i++)
					{
					n.ovaloidAxisLength[i]=ovaloidAxisLength[i];
					n.ovaloidAxisVec[i]=new Vector3d(ovaloidAxisVec[i]);
					}
				}
			return n;
			}
		}
	
	/******************************************************************************************************
	 *                               Class InterpolatedParticle                                           *
	 *****************************************************************************************************/

	/**
	 * Interpolated particles, contains additional information
	 */
	public static class InterpolatedParticle
		{
		public ParticlePos pos;
		public EvDecimal frameBefore;
		public EvDecimal frameAfter;
		public Color colorNuc;
		public boolean isEnd;
		public boolean hasParent;
		
		public boolean isKeyFrame(EvDecimal frame)
			{
			if(frameBefore==null || frameAfter==null)
				return false;
			else return frameBefore.equals(frame) || frameAfter.equals(frame);
			}
		
		public boolean isVisible()
			{
			return frameBefore!=null;
			}
		}

	public static Color representativeColor(Color nucColor)
		{
		if(nucColor==null)
			return Color.WHITE;
		else
			return nucColor;
		}
	
	
	/******************************************************************************************************
	 *                               Class Particle                                                       *
	 *****************************************************************************************************/

	/**
	 * One particle
	 */
	public class Particle implements Cloneable
		{
		/** Name of children */
		public final TreeSet<String> child=new TreeSet<String>();
		/** Name of parent */
//		public String parent=null;
		
		public final TreeSet<String> parents=new TreeSet<String>();
		
		/** Pos key frames */
		public final SortedMap<EvDecimal, ParticlePos> pos=new TreeMap<EvDecimal, ParticlePos>();
		/** Expression key frames */
		public final SortedMap<String, LineageExp> exp=new TreeMap<String, LineageExp>();
		/** Color */
		public java.awt.Color color=null; //Not stored to disk, but kept here so the color is the same in all windows
		/** Events */
		public SortedMap<EvDecimal, String> events=new TreeMap<EvDecimal, String>();
		
		public SortedMap<EvDecimal, Mesh3D> meshs=new TreeMap<EvDecimal, Mesh3D>();
		
		//idea: reserve x,y,z,r as special keywords, use expression system for all interpol?
		
		/** Override first frame of existence */
		public EvDecimal overrideStart;
		/** Override start frame of existence */
		public EvDecimal overrideEnd;
		/** Fate */
		public String fate="";
		/** Description of cell. null if none */
		public String description=null;
		
		
		@Override
		public String toString()
			{
			return "parent "+parents+" children "+child;
			}
		
		/**
		 * Make a deep copy 
		 */
		public Particle clone()
			{
			Particle n=new Particle();
			n.child.addAll(child);
			n.parents.addAll(parents);
			for(EvDecimal i:pos.keySet())
				n.pos.put(i, pos.get(i).clone());
			for(Map.Entry<String, LineageExp> e:exp.entrySet())
				n.exp.put(e.getKey(), e.getValue().clone());
			n.overrideStart=overrideStart;
			n.overrideEnd=overrideEnd;
			n.fate=fate;
			n.color=color;
			n.description=description;
			return n;
			}
		
		/** Get position frame <= , not including override */
		public EvDecimal getPosFrameBefore(EvDecimal frame)
			{
			ParticlePos exact=pos.get(frame);
			if(exact!=null)
				return frame;
			SortedMap<EvDecimal, ParticlePos> part=pos.headMap(frame); 
			if(part.isEmpty())
				return null;
			else
				return part.lastKey();
			}
		
		/** Get position frame >= , not including override */
		public EvDecimal getPosFrameAfter(EvDecimal frame)
			{
			SortedMap<EvDecimal, ParticlePos> part=pos.tailMap(frame); 
			if(part.isEmpty())
				return null;
			else
				return part.firstKey();
			} 
		
		/** Get position, create if it does not exist */
		public ParticlePos getCreatePos(EvDecimal frame)
			{
			ParticlePos npos=pos.get(frame);
			if(npos==null)
				pos.put(frame,npos=new ParticlePos());
			setMetadataModified();
			return npos;
			}

		/** Get expression level, create if it does not exist */
		public LineageExp getCreateExp(String n)
			{
			LineageExp e=exp.get(n);
			if(e==null)
				exp.put(n, e=new LineageExp());
			return e;
			}
		
		
		/**
		 * Get the last frame, accounting for override. null if it exists to infinity.
		 */
		public EvDecimal getLastFrame()
			{
			if(overrideEnd!=null)
				return overrideEnd;
			else
				{
				EvDecimal lastFrame=pos.isEmpty() ? null : pos.lastKey();
				EvDecimal cfirstFrame=null;
				for(String cName:child)
					{
					//Parent stop existing once there is a child
					Lineage.Particle c=particle.get(cName);
					EvDecimal thisFirstFrame=c.getFirstFrame();
					if(thisFirstFrame!=null)
						if(cfirstFrame==null || cfirstFrame.greater(thisFirstFrame))
							cfirstFrame=c.getFirstFrame();
					}
				if(cfirstFrame!=null && (lastFrame==null || cfirstFrame.greater(lastFrame)))
					lastFrame=cfirstFrame;
				return lastFrame;
				}
			}
		
		
		/**
		 * Get the first frame accounting for override
		 */
		public EvDecimal getFirstFrame()
			{
			if(overrideStart!=null)
				return overrideStart;
			else if(pos.isEmpty())
				return null;
			else
				return pos.firstKey();
			}
		
		
		/**
		 * Get interpolated position by reference to position. pos is shallow copied
		 */
		private InterpolatedParticle posToInterpol(EvDecimal frame, EvDecimal frameBefore, EvDecimal frameAfter)
			{
			InterpolatedParticle inter=new InterpolatedParticle();
			inter.pos=pos.get(frame);
			inter.frameAfter=frameAfter;
			inter.frameBefore=frameBefore;
			inter.isEnd = overrideEnd!=null && frame.equals(overrideEnd);  
			inter.hasParent=!parents.isEmpty();
			inter.colorNuc=color;
			return inter;
			}
		
		
		
		/**
		 * Interpolate frame information. If the particle does not exist at this time then null will be returned
		 */
		public InterpolatedParticle interpolatePos(EvDecimal frame)
			{
			//If there are no frames, abort early. This is only to get interpolation working
			//while the set is being edited.
			if(pos.isEmpty())
				return null;
			
			//If outside the overriden existing interval, return nothing
			if(overrideEnd!=null && frame.greater(overrideEnd))
				return null;
			else if(overrideStart!=null && frame.less(overrideStart))
				return null;

			//This particle only continues until there is a child
			for(String childName:child)
				{
				Particle n=particle.get(childName);
				EvDecimal cFirstFrame=n.getFirstFrame();
				if(cFirstFrame!=null && frame.greaterEqual(cFirstFrame))
					return null;
				}

			EvDecimal frameBefore=getPosFrameBefore(frame);
			EvDecimal frameAfter=getPosFrameAfter(frame);
			
			if(frameBefore==null)
				{
				if(frameAfter==null)
					return null;
				else
					return posToInterpol(frameAfter, frameBefore, frameAfter);
				}
			else if(frameAfter==null || frameBefore.equals(frameAfter)) 
				{
				InterpolatedParticle inter=posToInterpol(frameBefore, frameBefore, frameAfter);
				if(overrideEnd!=null && overrideEnd.equals(frame))
					inter.isEnd=true;
				return inter;
				}
			else
				{
				ParticlePos before=pos.get(frameBefore);
				ParticlePos after=pos.get(frameAfter);

				EvDecimal tdiff=frameAfter.subtract(frameBefore);
				double frac;
				try
					{
					frac=frame.subtract(frameBefore).divide(tdiff).doubleValue();
					}
				catch (ArithmeticException e)
					{
					//This can occur if tdiff is really small
					return posToInterpol(frameAfter, frameBefore, frameAfter);
					}
				double frac1=1.0-frac;

				InterpolatedParticle inter=new InterpolatedParticle();
				inter.pos=new ParticlePos();
				inter.pos.x=before.x*frac1 + after.x*frac;
				inter.pos.y=before.y*frac1 + after.y*frac;
				inter.pos.z=before.z*frac1 + after.z*frac;
				inter.pos.r=before.r*frac1 + after.r*frac;
				inter.frameBefore=frameBefore;
				inter.frameAfter=frameAfter;
				inter.hasParent=!parents.isEmpty();
				inter.colorNuc=color;

				//TODO interpolate?
				inter.pos.ovaloidAxisLength=before.ovaloidAxisLength;
				inter.pos.ovaloidAxisVec=before.ovaloidAxisVec;

				return inter;
				}
			}


		
		}
				
				
				
				
	
	/**
	 * Count how many particles exist up to and equal the frame
	 */
	public int countParticlesUpTo(EvDecimal frame)
		{
		int count=0;
		for(Lineage.Particle n:particle.values())
			{
			EvDecimal f=n.getFirstFrame();
			if(f!=null && f.lessEqual(frame))
				count++;
			}
		return count;
		}

	/**
	 * Count how many particles exist at a given frame
	 */
	public int countParticlesAtFrame(EvDecimal frame)
		{
		int num=0;
		for(InterpolatedParticle i:interpolateParticles(frame).values())
			if(i.isVisible())
				num++;
		return num;
		}
	
	
	
	/**
	 * Join single children with their parents. Useful if an algorithm generates one cell
	 * for each time point
	 */
	public void flattenSingleChildren()
		{
		for(String thisName:new LinkedList<String>(particle.keySet()))
			{
			Particle n=particle.get(thisName);
			if(n!=null)
				if(n.child.size()==1)
					{
					String childName=n.child.iterator().next();
					Particle child=particle.get(childName);
					if(child.parents.size()==1)
						{
						particle.remove(childName);
						
						n.pos.putAll(child.pos);
						n.child.clear();
						n.child.addAll(child.child);
						
						//Make children of children have this nuc as a parent instead
						for(String childchildName:child.child)
							{
							particle.get(childchildName).parents.remove(childName);
							particle.get(childchildName).parents.add(thisName);
							}
						}
					}
			}
		
		
		}

	/**
	 * Get names of all expressions mentioned
	 */
	public Set<String> getAllExpNames()
		{
		HashSet<String> expName=new HashSet<String>();
		for(Lineage.Particle n:particle.values())
			expName.addAll(n.exp.keySet());
		return expName;
		}

	
	/**
	 * Get maximum and minimum level of expression
	 */
	public Tuple<Double,Double> getMaxMinExpLevel(String expName)
		{
		boolean first=true;
		double max=0, min=0;
		for(Particle n:particle.values())
			{
			LineageExp e=n.exp.get(expName);
			if(e!=null)
				{
				for(double v:e.level.values())
					if(first)
						{
						max=min=v;
						first=false;
						}
					else
						{
						if(v>max)
							max=v;
						if(v<min)
							min=v;
						}
				}
			}
		if(first)
			return null;
		else
			return Tuple.make(max, min);
		}

	
	
	@Override
	public EvObject cloneEvObject()
		{
		return cloneUsingSerialize();
		}
	
	
	

	/** Additions to the object-specific menu */
	public void buildMetamenu(JMenu menu, final EvContainer parentObject)
		{
		JMenuItem miSaveColorScheme=new JMenuItem("Save color scheme"); 
		JMenuItem miLoadColorScheme=new JMenuItem("Load color scheme"); 
		menu.add(miSaveColorScheme);
		menu.add(miLoadColorScheme);
		
		JMenuItem miIntegrate=new JMenuItem("Integrate expression");
		menu.add(miIntegrate);
		
		final Lineage nthis=this;
		miSaveColorScheme.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){nthis.saveColorSchemeDialog(null);}
		});
		miLoadColorScheme.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
				{
				nthis.loadColorSchemeDialog(null);
				BasicWindow.updateWindows(); //TODO emit object update
				}
		});
		miIntegrate.addActionListener(new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
				{
				ParticleDialogIntegrate dia=new ParticleDialogIntegrate();
				dia.comboLin.setSelectedObject(Lineage.this);
				}
			});
		
		JMenuItem miMapModel=new JMenuItem("Map ce model");
		menu.add(miMapModel);
		miMapModel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
				{
				LineageCommonUI.mapModel(parentObject, Lineage.this);
				}
		});
		
		JMenuItem miMapExpression=new JMenuItem("Map expression to here");
		menu.add(miMapExpression);
		miMapExpression.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
				{
				new LineageDialogMapExp(Lineage.this);
				}
		});
		
		
		}


	public Set<String> getRecursiveChildNames(String name)
		{
		Set<String> names=new HashSet<String>();
		getRecursiveChildNames(name, names);
		return names;
		}
	private void getRecursiveChildNames(String name, Set<String> names)
		{
		Particle p=particle.get(name);
		//System.out.println(name);
		names.add(name);
		if(p==null)
			throw new RuntimeException("No such child: "+name);
		for(String cname:p.child)
			{
			names.add(cname);
			getRecursiveChildNames(cname, names);
			}
		}
	
	
	public Set<String> getRoots()
		{
		Set<String> names=new HashSet<String>();
		for(String n:particle.keySet())
			if(particle.get(n).parents.isEmpty())
				names.add(n);
		return names;
		}


	public Set<String> getLeafs()
		{
		Set<String> names=new HashSet<String>();
		for(String n:particle.keySet())
			if(particle.get(n).child.isEmpty())
				names.add(n);
		return names;
		}


	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvData.supportedMetadataFormats.put(metaType,Lineage.class);
		
		try
			{
			cellGroups.importXML(EvFileUtil.getFileFromURL(Lineage.class.getResource("cecellgroups.cgrp")));
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}
	
	}
