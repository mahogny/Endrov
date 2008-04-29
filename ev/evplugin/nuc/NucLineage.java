package evplugin.nuc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.vecmath.Vector3d;
import org.jdom.*;

import evplugin.basicWindow.BasicWindow;
import evplugin.basicWindow.EvColor;
import evplugin.imageWindow.ImageWindow;
import evplugin.imageWindow.ImageWindowExtension;
import evplugin.keyBinding.KeyBinding;
import evplugin.modelWindow.ModelWindow;
import evplugin.script.*;
import evplugin.data.*;
import evplugin.ev.*;

/**
 * Meta object: Nuclei, a lineage and expression info
 * @author Johan Henriksson
 */
public class NucLineage extends EvObject implements Cloneable
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	private static final String metaType="nuclineage";

	/** Currently hidden nuclei. currently no sample. needed? */
	public static HashSet<NucPair> hiddenNuclei=new HashSet<NucPair>();
	/** Currently selected nuclei. currently no sample. needed? */
	public static HashSet<NucPair> selectedNuclei=new HashSet<NucPair>();
	
	public static NucPair currentHover=new NucPair(null,"");

	public static final int KEY_TRANSLATE=KeyBinding.register(new KeyBinding("Nuclei/Lineage","Translate",'z'));
	public static final int KEY_CHANGE_RADIUS=KeyBinding.register(new KeyBinding("Nuclei/Lineage","Change radius",'c'));
	public static final int KEY_SETZ=KeyBinding.register(new KeyBinding("Nuclei/Lineage","Set Z",'x'));
	public static final int KEY_DIVIDENUC=KeyBinding.register(new KeyBinding("Nuclei/Lineage","Divide nucleus",'v'));
	public static final int KEY_SETEND=KeyBinding.register(new KeyBinding("Nuclei/Lineage","Set end frame",'b'));
	public static final int KEY_MAKEPARENT=KeyBinding.register(new KeyBinding("Nuclei/Lineage","Make parent",'g'));
	public static final int KEY_SETPARENT=KeyBinding.register(new KeyBinding("Nuclei/Lineage","Associate parent",'p'));
	
	//A generalization would be nice
	public static final String[] connectNuc=new String[]{"post","ant"};
	
	public static void initPlugin() {}
	static
		{
		Script.addCommand("nus", new CmdNucs(true,true));
		Script.addCommand("nua", new CmdNucs(false,true));
		Script.addCommand("nud", new CmdNucs(false,true));
		Script.addCommand("nuda", new CmdNucda());
		Script.addCommand("nuren", new CmdNucren());
		Script.addCommand("nurend", new CmdNucrend());
		Script.addCommand("nusnap", new CmdNucsnap());
		Script.addCommand("nuhide", new CmdNuchide());
		Script.addCommand("nushow", new CmdNucshow());
//		Script.addCommand("nun", new CmdNucName());

		ModelWindow.modelWindowExtensions.add(new NucModelExtension());
		
		EvData.extensions.put(metaType,new EvObjectType()
			{
			public EvObject extractObjects(Element e)
				{
				NucLineage meta=new NucLineage();
				try
					{
					for(Element nuce:EV.castIterableElement(e.getChildren()))
						{
						String nucName=nuce.getAttributeValue("name");
						String ends=nuce.getAttributeValue("end");
						Nuc n=meta.getNucCreate(nucName);
						if(ends!=null)
							n.end=Integer.parseInt(ends);

						for(Element pose:EV.castIterableElement(nuce.getChildren()))
							{
							if(pose.getName().equals("pos"))
								{
								int frame=pose.getAttribute("f").getIntValue();
								double posx=pose.getAttribute("x").getDoubleValue();
								double posy=pose.getAttribute("y").getDoubleValue();
								double posz=pose.getAttribute("z").getDoubleValue();
								double posr=pose.getAttribute("r").getDoubleValue();
								NucPos pos=new NucPos();
								pos.setPosCopy(new Vector3d(posx,posy,posz));
								pos.r=posr;
								n.pos.put(frame, pos);
								}
							else if(pose.getName().equals("child"))
								{
								String child=pose.getAttributeValue("name");
								n.child.add(child);
								}
							else if(pose.getName().equals("exp"))
								{
								String expName=pose.getAttributeValue("name");
								NucExp exp=new NucExp();
								n.exp.put(expName, exp);
								for(Element expe:EV.castIterableElement(pose.getChildren()))
									{
									int frame=expe.getAttribute("f").getIntValue();
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
				for(String parentName:meta.nuc.keySet())
					{
					Nuc parent=meta.nuc.get(parentName);
					for(String childName:parent.child)
						{
						Nuc child=meta.nuc.get(childName);
						if(child==null)
							Log.printError("Missing child: "+childName, null);
						child.parent=parentName;
						}
					}
				
				return meta;
				}
			});
		ImageWindow.addImageWindowExtension(new ImageWindowExtension()
			{
			public void newImageWindow(ImageWindow w)
				{
				NucImageRenderer r=new NucImageRenderer(w);
				w.imageWindowTools.add(new ToolMakeNuc(w,r));
				w.imageWindowRenderers.add(r);
				}
			});
		}

	/** Additions to the object-specific menu */
	public void buildMetamenu(JMenu menu)
		{
		}

	
	
	/**
	 * Selection of nuclei by mouse and keyboard
	 * @param nucPair Which nucleus, never null
	 * @param shift True if shift-key held
	 */
	public static void mouseSelectNuc(NucPair nucPair, boolean shift)
		{
		NucLineage lin=nucPair.fst();
		String nucname=nucPair.snd();
		//Shift-key used to select multiple
		if(shift)
			{
			if(!nucname.equals(""))
				{
				if(selectedNuclei.contains(nucPair))
					selectedNuclei.remove(nucPair);
				else
					selectedNuclei.add(new NucPair(lin,nucname));
				}
			}
		else
			{
			selectedNuclei.clear();				
			if(!nucname.equals(""))
				selectedNuclei.add(new NucPair(lin,nucname));
			}
		BasicWindow.updateWindows();
		}

	
	/**
	 * Get _one_ lineage object or null. Maybe remove/refine later 
	 */
	public static Collection<NucLineage> getLineages(EvData meta)
		{
		if(meta==null)
			return new Vector<NucLineage>();
		else
			return meta.getObjects(NucLineage.class);
		}

	

	/**
	 * Get select lineage object (console reference)
	 */
	public static NucLineage getSelectedLineage()
		{
		EvData m=EvData.getSelectedMetadata();
		if(m!=null)
			{
			EvObject ob=m.getSelectedMetaobject();
			if(ob instanceof NucLineage)
				return (NucLineage)ob;
			else
				{
				List<NucLineage> l=m.getObjects(NucLineage.class);
				if(l.isEmpty())
					return null;
				else
					return l.get(0);
				}
			}
		return null;
		}

	
	/**
	 * Create parent-children relation based on selected nuclei
	 */
	public void createParentChild(String parent, String child)
		{
		Nuc parentn=nuc.get(parent);
		Nuc childn=nuc.get(child);
		if(parentn!=null && childn!=null)
			{
			if(childn.parent!=null)
				nuc.get(childn.parent).child.remove(child);
			childn.parent=parent;
			parentn.child.add(child);
			}
		metaObjectModified=true;
		}
		
		
	/**
	 * Create parent-children relation based on selected nuclei
	 */
	public static void createParentChildSelected()
		{
		if(NucLineage.selectedNuclei.isEmpty())
			{
			JOptionPane.showMessageDialog(null, "No nuclei selected");
			return;
			}
		String parentName=null;
		int parentFrame=0;
		NucLineage.Nuc parent=null;
		NucLineage lin=NucLineage.selectedNuclei.iterator().next().fst();
		//Decide which is the parent
		for(NucPair childPair:NucLineage.selectedNuclei)
			if(childPair.fst()==lin)
				{
				
				String childName=childPair.snd();
				NucLineage.Nuc n=lin.nuc.get(childName);
				int firstFrame=n.pos.firstKey();
				if(parentName==null || firstFrame<parentFrame)
					{
					parentFrame=firstFrame;
					parentName=childName;
					parent=n;
					}
				}
		boolean assignedChild=false;
		if(parent==null)
			JOptionPane.showMessageDialog(null, "Could not decide on a parent");
		else
			for(NucPair childPair:NucLineage.selectedNuclei)
				if(childPair.fst()==lin)
					{
					String childName=childPair.snd();
					if(!childName.equals(parentName))
						{
						NucLineage.Nuc n=lin.nuc.get(childName);
						n.parent=parentName;
						parent.child.add(childName);
						Log.printLog("new PC, parent: "+parentName+"child: "+childName);
						assignedChild=true;
						}
					}
		if(!assignedChild)
			JOptionPane.showMessageDialog(null, "Found no children to assign to parent");
		lin.metaObjectModified=true;
		}

	
	/******************************************************************************************************
	 *                               Instance NucLineage                                                  *
	 *****************************************************************************************************/
	
	public HashMap<String, Nuc> nuc=new HashMap<String, Nuc>();

	
	
	
	
	/**
	 * Make a deep copy
	 */
	public Object clone()
		{
		NucLineage lin=new NucLineage();
		for(String nkey:nuc.keySet())
			lin.nuc.put(nkey, (Nuc)nuc.get(nkey).clone());
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
	public void saveMetadata(Element e)
		{
		e.setName(metaType);
		for(String nucName:nuc.keySet())
			{
			Nuc n=nuc.get(nucName);
			Element nuce=new Element("nuc");
			e.addContent(nuce);
			nuce.setAttribute("name", nucName);
			if(n.end!=null)
				nuce.setAttribute("end", ""+n.end);

			for(int frame:n.pos.keySet())
				{
				NucPos pos=n.pos.get(frame);
				Element pose=new Element("pos");
				nuce.addContent(pose);
				pose.setAttribute("f", ""+frame);
				pose.setAttribute("x", ""+pos.x);
				pose.setAttribute("y", ""+pos.y);
				pose.setAttribute("z", ""+pos.z);
				pose.setAttribute("r", ""+pos.r);
				}

			for(String child:n.child)
				{
				Element childe=new Element("child");
				childe.setAttribute("name", child);
				nuce.addContent(childe);
				}
			
			for(Map.Entry<String, NucExp> entry:n.exp.entrySet())
				{
				Element childe=new Element("exp");
				childe.setAttribute("name", entry.getKey());
				nuce.addContent(childe);
				for(Map.Entry<Integer, Double> ve:entry.getValue().level.entrySet())
					{
					Element value=new Element("v");
					value.setAttribute("f",""+ve.getKey());
					value.setAttribute("l",""+ve.getValue());
					childe.addContent(value);
					}
				}
			
			}
		}
	
	
	/** 
	 * Get a nucleus. Create if needed 
	 */
	public Nuc getNucCreate(String name)
		{
		Nuc n=nuc.get(name);
		if(n==null)
			nuc.put(name,n=new Nuc());
		return n;
		}
	


	
	/**
	 * Divide a nucleus at the specified frame
	 */
	public void divide(String parentName, int frame)
		{
		removePosAfterEqual(parentName, frame);
		Nuc n=nuc.get(parentName);
		Log.printLog("divide:"+parentName);
		if(n!=null)
			{
			String c1n=getUniqueNucName();
			Nuc c1=getNucCreate(c1n);
			String c2n=getUniqueNucName();
			Nuc c2=getNucCreate(c2n);
			n.child.add(c1n);
			n.child.add(c2n);
			c1.parent=parentName;
			c2.parent=parentName;
			
			NucPos pos=n.pos.get(n.pos.lastKey());
			NucPos c1p=new NucPos(pos);
			NucPos c2p=new NucPos(pos);
			c1p.x-=pos.r/2;
			c2p.x+=pos.r/2;
			c1.pos.put(frame, c1p);
			c2.pos.put(frame, c2p);
			}
		metaObjectModified=true;
		}

	/**
	 * Delete all positions after or equal to the current frame. If there are no more positions,
	 * remove the nucleus as well
	 */
	public void removePosAfterEqual(String nucName, int frame)
		{
		Nuc n=nuc.get(nucName);
		if(n!=null)
			{
			List<Integer> todel=new LinkedList<Integer>();
			
			for(int f:n.pos.keySet())
				if(f>=frame)
					todel.add(f);
			for(int f:todel)
				n.pos.remove(f);
			
			if(n.pos.isEmpty())
				removeNuc(nucName);
			}
		metaObjectModified=true;
		}

	/**
	 * Remove a nucleus. Cleans up child references
	 */
	public void removeNuc(String nucName)
		{
		//Can also just do parent. but this will automatically fix problems if there is a glitch
		nuc.remove(nucName);
		for(Nuc n:nuc.values())
			{
			n.child.remove(nucName);
			if(n.parent!=null && n.parent.equals(nucName))
				n.parent=null;
			}
		metaObjectModified=true;
		}
	

	/**
	 * Get all interpolated nuclei
	 */
	public Map<NucPair, NucInterp> getInterpNuc(double frame)
		{
		HashMap<NucPair, NucInterp> nucs=new HashMap<NucPair, NucInterp>();
		for(String nucName:nuc.keySet())
			{
			Nuc n=nuc.get(nucName);
			NucInterp inter=n.interpolatePos(frame);
			if(inter!=null)
				nucs.put(new NucPair(this, nucName), inter);
			}
		return nucs;
		}

	/**
	 * Get a name for a nucleus that has not been used yet
	 */
	public String getUniqueNucName()
		{
		int i=0;
		while(nuc.get(":"+i)!=null)
			i++;
		return ":"+i;
		}
	
	/**
	 * Rename nucleus
	 */
	public boolean renameNucleus(String oldName, String newName)
		{
		Nuc n=nuc.get(oldName);
		if(n==null || (nuc.get(newName)!=null && !oldName.equals(newName)))
			return false;
		else
			{
			nuc.remove(oldName);
			nuc.put(newName, n);
			updateNameReference(oldName, newName);
			metaObjectModified=true;
			return true;
			}
		}
	
	/**
	 * Merge nuclei
	 */	
	public void mergeNuclei(String sourceName, String targetName)
		{
		Nuc ns=nuc.get(sourceName);
		Nuc nt=nuc.get(targetName);
		ns.end=null;
		nuc.remove(targetName);
//		for(int frame:nt.pos.keySet())
//			ns.pos.put(frame,nt.pos.get(frame));
		ns.pos.putAll(nt.pos);
//		for(String child:nt.child)
//			ns.child.add(child);
		ns.child.addAll(nt.child);
		updateNameReference(targetName,sourceName);
		ns.child.remove(sourceName);
		metaObjectModified=true;
		}

	
	
	
	/**
	 * Remove parent reference from nucleus
	 */
	public void removeParentReference(String nucName)
		{
		Nuc child=nuc.get(nucName);
		String parentName=child.parent;
		child.parent=null;
		if(parentName!=null)
			{
			Nuc parent=nuc.get(parentName);
			parent.child.remove(nucName);
			}
		metaObjectModified=true;
		}

	
	/**
	 * Update a reference to a nucleus name. Does not touch the corresponding nucleus.
	 */
	private void updateNameReference(String oldName, String newName)
		{
		for(Nuc on:nuc.values())
			{
			if(on.parent!=null && on.parent.equals(oldName))
				on.parent=newName;
			if(on.child.contains(oldName))
				{
				on.child.remove(oldName);
				on.child.add(newName);
				}
			
			NucPair oldref=new NucPair(this,oldName);
			if(selectedNuclei.contains(oldref))
				{
				selectedNuclei.remove(oldref);
				selectedNuclei.add(new NucPair(this,newName));
				}
			}
		metaObjectModified=true;
		}
	
	

	
	/******************************************************************************************************
	 *                               Class NucPos                                                         *
	 *****************************************************************************************************/

	/**
	 * Generate a menu for setting color on nuclei
	 */
	public static JMenu makeSetColorMenu()
		{
		JMenu m=new JMenu("Set color");
		
		JMenuItem mir=new JMenuItem("<Remove>");
		mir.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0)
				{
				for(NucPair p:selectedNuclei)
					p.fst().nuc.get(p.snd()).colorNuc=null;
				BasicWindow.updateWindows();
				}
		});
		m.add(mir);
		for(final EvColor c:EvColor.colorList)
			{
			JMenuItem mi=new JMenuItem(c.name);
			mi.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0)
					{
					for(NucPair p:selectedNuclei)
						p.fst().nuc.get(p.snd()).colorNuc=c.c;
					BasicWindow.updateWindows();
					}
			});
			m.add(mi);
			}
		return m;
		}




	/**
	 * Position key frame
	 */
	public static class NucPos
		{
		public double x,y,z,r;
		public NucPos(){}
		public NucPos(NucPos p)
			{
			setPosCopy(p.getPosCopy());
			r=p.r;
			}
		public Vector3d getPosCopy()
			{
			return new Vector3d(x,y,z);
			}
		public void setPosCopy(Vector3d v)
			{
			x=v.x;
			y=v.y;
			z=v.z;
			}
		}
	
	/******************************************************************************************************
	 *                               Class NucInterp                                                      *
	 *****************************************************************************************************/

	/**
	 * Interpolated nuclei, contains additional information
	 */
	public static class NucInterp
		{
		public NucPos pos;
		public Integer frameBefore;
		public Integer frameAfter;
		public java.awt.Color colorNuc;
		public boolean isKeyFrame(double frame)
			{
			//double vs int ==. probably a bad idea
			if(frameBefore==null || frameAfter==null)
				return false;
			else return frameBefore==frame || frameAfter==frame;
			}
		boolean isEnd;
		boolean hasParent;
		}

	/******************************************************************************************************
	 *                               Class NucExp                                                         *
	 *****************************************************************************************************/

	public static class NucExp implements Cloneable
		{
		public SortedMap<Integer,Double> level=new TreeMap<Integer, Double>();
		public java.awt.Color expColor=java.awt.Color.RED; //Not stored to disk, but kept here so the color is the same in all windows
		
		
		/**
		 * Make a deep copy 
		 */
		public Object clone()
			{
			NucExp exp=new NucExp();
			for(Map.Entry<Integer, Double> e:level.entrySet())
				exp.level.put(e.getKey(),e.getValue());
			exp.expColor=expColor;
			return exp;
			}

		/**
		 * Get highest level for any frame
		 */
		public Double getMaxLevel()
			{
			Double max=null;
			for(Double d:level.values())
				if(max==null || max>d)
					max=d;
			return max;
			}

		/**
		 * Interpolate level for a certain frame
		 */
		public Double interpolateLevel(double frame)
			{
			if(frame<level.firstKey())
				return level.get(level.firstKey());
			else if(frame>level.lastKey())
				return level.get(level.lastKey());
			else
				{
				SortedMap<Integer,Double> hlevel=level.headMap((int)frame);
				SortedMap<Integer,Double> tlevel=level.tailMap((int)frame);
				int frameBefore=hlevel.lastKey();
				int frameAfter=tlevel.firstKey();
				double levelBefore=hlevel.get(frameBefore);
				double levelAfter=tlevel.get(frameAfter);
				double s=(frame-frameBefore)/(frameAfter-frameBefore);
				return levelAfter*s+levelBefore*(1-s);
				}
			}
		
		
		}
	
	
	/******************************************************************************************************
	 *                               Class Nuc                                                            *
	 *****************************************************************************************************/

	/**
	 * One nucleus
	 */
	public class Nuc implements Cloneable
		{
		/** Name of children */
		public final TreeSet<String> child=new TreeSet<String>();
		/** Name of parent */
		public String parent=null;
		/** Pos key frames */
		public final SortedMap<Integer, NucPos> pos=new TreeMap<Integer, NucPos>();
		/** Expression key frames */
		public final SortedMap<String, NucExp> exp=new TreeMap<String, NucExp>();
		/** Color for nuc */
		public java.awt.Color colorNuc=null; //Not stored to disk, but kept here so the color is the same in all windows

		
		//idea: reserve x,y,z,r as special keywords, use expression system for all interpol?
		
		/** Final frame of existence */
		public Integer end;
		/** Fate of nucleus */
		public String fate="";
		
		/** Make a deep copy */
		public Object clone()
			{
			Nuc n=new Nuc();
			n.child.addAll(child);
			n.parent=parent;
			n.end=end;
			n.fate=fate;
			for(int i:pos.keySet())
				n.pos.put(i, new NucPos(pos.get(i)));
			for(Map.Entry<String, NucExp> e:exp.entrySet())
				n.exp.put(e.getKey(), (NucExp)e.getValue().clone());
			return n;
			}
		
		/** Get position frame <= */
		public Integer getPosFrameBefore(int frame)
			{
			NucPos exact=pos.get(frame);
			if(exact!=null)
				return frame;
			SortedMap<Integer, NucPos> part=pos.headMap(frame); 
			if(part.size()==0)
				return null;
			else
				return part.lastKey();
			}
		
		/** Get position frame >= */
		public Integer getPosFrameAfter(int frame)
			{
			SortedMap<Integer, NucPos> part=pos.tailMap(frame); 
			if(part.size()==0)
				return null;
			else
				return part.firstKey();
			} 
		
		/** Get position, create if it does not exist */
		public NucPos getPosCreate(int frame)
			{
			NucPos npos=pos.get(frame);
			if(npos==null)
				{
				npos=new NucPos();
				pos.put(frame,npos);
				}
			metaObjectModified=true;
			return npos;
			}

		/** Get expression level, create if it does not exist */
		public NucExp getExpCreate(String n)
			{
			NucExp e=exp.get(n);
			if(e==null)
				exp.put(n, e=new NucExp());
			return e;
			}
		
		
		/**
		 * Get the last frame accounting for end
		 */
		public int lastFrame()
			{
			if(end!=null)
				return end;
			else
				{
				int lastFrame=pos.lastKey();
				Integer cfirstFrame=null;
				for(String cName:child)
					{
					NucLineage.Nuc c=nuc.get(cName);
					if(c.pos.isEmpty())
						{
						System.out.println("Error: no positions for "+cName);
						}
					else if(cfirstFrame==null || cfirstFrame>c.pos.firstKey())
						cfirstFrame=c.pos.firstKey();
					}
				if(cfirstFrame!=null && cfirstFrame>lastFrame)
					lastFrame=cfirstFrame;
				return lastFrame;
				}
			}
		
		
		
		private NucInterp posToInterpol(int frame, Integer frameBefore, Integer frameAfter)
			{
			NucInterp inter=new NucInterp();
			inter.pos=pos.get(frame);//TODO: copy?
			inter.frameAfter=frameAfter;
			inter.frameBefore=frameBefore;
			inter.isEnd = end!=null && (int)frame==(int)end;
			inter.hasParent=parent!=null;
			inter.colorNuc=colorNuc;
			return inter;
			}
		
		
		/**
		 * Interpolate frame information
		 */
		public NucInterp interpolatePos(double frame)
			{
			//If there are no frames, abort early. This is only to get interpolation working
			//while the set is being edited.
			if(pos.isEmpty())
				return null;
			
			Integer frameBefore=getPosFrameBefore((int)frame);
			Integer frameAfter=getPosFrameAfter((int)Math.ceil(frame));
			
			//This nucleus only continues until there is a child
			for(String childName:child)
				{
				Nuc n=nuc.get(childName);
				if(!n.pos.isEmpty() && frame>=n.pos.firstKey())
					return null;
				}

			//This nucleus does not start until the parent is gone
			if(parent!=null)
				{
				Nuc p=nuc.get(parent);
				if(p.pos.isEmpty() || p.pos.lastKey()>frame)
					return null;
				}
			
			if(frameBefore==null)
				{
				if(frameAfter==null)
					return null;
				else
					return posToInterpol(frameAfter, frameBefore, frameAfter);
				}
			else if(frameAfter==null || (int)frameBefore==(int)frameAfter)
				{
				NucInterp inter=posToInterpol(frameBefore, frameBefore, frameAfter);
				if(end!=null && end==(int)frame)
					inter.isEnd=true;
				else if(end!=null && end<frame)
					return null;
				return inter;
				}
			else
				{
				NucPos before=pos.get(frameBefore);
				NucPos after=pos.get(frameAfter);
				
				double frac=(frame-frameBefore)/(frameAfter-frameBefore);
				double frac1=1.0-frac;
				
				NucInterp inter=new NucInterp();
				inter.pos=new NucPos();
				inter.pos.x=before.x*frac1 + after.x*frac;
				inter.pos.y=before.y*frac1 + after.y*frac;
				inter.pos.z=before.z*frac1 + after.z*frac;
				inter.pos.r=before.r*frac1 + after.r*frac;
				inter.frameBefore=frameBefore;
				inter.frameAfter=frameAfter;
				inter.hasParent=parent!=null;
				inter.colorNuc=colorNuc;
				return inter;
				}
			}
		
		
		}
	
	}
