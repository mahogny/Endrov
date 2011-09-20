/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.particle;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.vecmath.Vector3d;

import endrov.basicWindow.BasicWindow;
import endrov.basicWindow.EvColor;
import endrov.basicWindow.FrameControl;
import endrov.basicWindow.TimedDataWindow;
import endrov.basicWindow.EvColor.ColorMenuListener;
import endrov.consoleWindow.ConsoleWindow;
import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvPath;
import endrov.data.EvSelection;
import endrov.data.GuiEvDataIO;
import endrov.ev.EvLog;
import endrov.particle.Lineage.Particle;
import endrov.particle.Lineage.InterpolatedParticle;
import endrov.particle.expression.ParticleDialogIntegrate;
import endrov.particle.util.LineageMergeUtil;
import endrov.util.EvDecimal;
import endrov.util.EvGeomUtil;
import endrov.util.Tuple;

/**
 * Common GUI functions for Lineage, mainly menus
 * @author Johan Henriksson
 *
 */
public class LineageCommonUI implements ActionListener
	{
	private JMenuItem miRename=new JMenuItem("Rename particle");
	private JMenuItem miCreateEmptyParticle=new JMenuItem("Create empty particle");
	private JMenuItem miMergeParticles=new JMenuItem("Merge particle positions");
	private JMenuItem miAssocParent=new JMenuItem("Associate parent");
	private JMenuItem miUnassocParent=new JMenuItem("Unassociate from parent");
	private JMenuItem miSwapChildren=new  JMenuItem("Swap children names*");
	private JMenuItem miSetFate=new JMenuItem("Set fate");
	private JMenuItem miSetDesc=new JMenuItem("Set description");
	private JMenuItem miSetOverrideStartFrame=new JMenuItem("Set override start frame");
	private JMenuItem miSetOverrideEndFrame=new JMenuItem("Set override end frame");
	private JMenuItem miDeleteParticle=new JMenuItem("Delete particle");
	private JMenuItem miSelectChildren=new JMenuItem("Select children");
	private JMenuItem miSelectParents=new JMenuItem("Select parents");
	private JMenuItem miSelectAll=new JMenuItem("Select all in this lineage");
	private JMenuItem miSelectAllSameName=new JMenuItem("Select all w/ the same name");

	public JMenuItem miPrintAngle=new JMenuItem("Print angles");  
	public JMenuItem miPrintPos=new JMenuItem("Print positions");  
	public JMenuItem miPrintCountParticlesAtFrame=new JMenuItem("Print particle count in frame");  
	public JMenuItem miPrintCountParticlesUpToFrame=new JMenuItem("Print particle count up to frame");  

	private JMenuItem miIntegrate=new JMenuItem("Integrate expression");
	
	private final JComponent parentComponent;
	private final TimedDataWindow fc;
	
	/**
	 * Currently hidden particles. currently no sample. needed? 
	 */
	public static HashSet<LineageSelParticle> hiddenParticles=new HashSet<LineageSelParticle>();
	
	/**
	 * There is only one empty selection. This allows == for checking
	 */
	public static final LineageSelParticle emptyHover=new LineageSelParticle(null,"");
	
	/**
	 * Currently hovered cell
	 */
	public static LineageSelParticle currentHover=LineageCommonUI.emptyHover;
	
	
	
	public LineageCommonUI(JComponent parent, TimedDataWindow fc)
		{
		this.parentComponent=parent;
		this.fc=fc;
		}
	
	public void addToMenu(JComponent menuLineage, boolean addAccel)
		{
		miRename.addActionListener(this);
		miCreateEmptyParticle.addActionListener(this);
		miMergeParticles.addActionListener(this);
		miAssocParent.addActionListener(this);
		miUnassocParent.addActionListener(this);
		miPrintAngle.addActionListener(this);
		miPrintPos.addActionListener(this);
		miPrintCountParticlesAtFrame.addActionListener(this);
		miPrintCountParticlesUpToFrame.addActionListener(this);
		miSwapChildren.addActionListener(this);
		miSetFate.addActionListener(this);
		miSetDesc.addActionListener(this);
		miSetOverrideStartFrame.addActionListener(this);
		miSetOverrideEndFrame.addActionListener(this);
		miDeleteParticle.addActionListener(this);
		miSelectChildren.addActionListener(this);
		miSelectParents.addActionListener(this);
		miSelectAll.addActionListener(this);
		miSelectAllSameName.addActionListener(this);

		miIntegrate.addActionListener(this);
		//miMapModel.addActionListener(this);
		
		
		menuLineage.add(miSelectChildren);
		menuLineage.add(miSelectParents);
//		menuLineage.add(miSelectAll);
		menuLineage.add(miSelectAllSameName);

		menuLineage.add(miAssocParent);
		menuLineage.add(miCreateEmptyParticle);
		menuLineage.add(miDeleteParticle);
		menuLineage.add(miMergeParticles);
		menuLineage.add(miRename);
		menuLineage.add(LineageCommonUI.makeSetColorMenu());
		menuLineage.add(miSetDesc);
		menuLineage.add(miSetFate);
		menuLineage.add(miSetOverrideStartFrame);
		menuLineage.add(miSetOverrideEndFrame);
		menuLineage.add(miSwapChildren);
		menuLineage.add(miUnassocParent);
		//menuLineage.addSeparator();
		menuLineage.add(miPrintAngle);
		menuLineage.add(miPrintPos);
		menuLineage.add(miPrintCountParticlesAtFrame);
		menuLineage.add(miPrintCountParticlesUpToFrame);
		
		menuLineage.add(miIntegrate);
		//menuLineage.add(miMapModel);
		


		if(addAccel)
			{
			//Need ctrl+ or something. Collided with combobox in linw!
			
			miRename.setAccelerator(KeyStroke.getKeyStroke('R',Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			miAssocParent.setAccelerator(KeyStroke.getKeyStroke('P',Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			}
		}
	
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==miMergeParticles)
			actionMergeNuclei();
		else if(e.getSource()==miAssocParent)
			actionAssocParent();
		else if(e.getSource()==miUnassocParent)
			actionUnassocParent();
		else if(e.getSource()==miPrintAngle)
			{
			EvDecimal frame=fc.getFrame();
			LineageCommonUI.actionPrintAngle(frame);
			}
		else if(e.getSource()==miPrintPos)
			{
			EvDecimal frame=fc.getFrame();
			LineageCommonUI.actionPrintPos(frame);
			}
		else if(e.getSource()==miPrintCountParticlesAtFrame)
			{
			EvDecimal frame=fc.getFrame();
			//TODO replace with visible set
			for(Map.Entry<EvPath, Lineage> entry:fc.getSelectedData().getIdObjectsRecursive(Lineage.class).entrySet())
				EvLog.printLog(entry.getKey().toString()+" numberOfParticles: "+entry.getValue().countParticlesAtFrame(frame));
			}
		else if(e.getSource()==miPrintCountParticlesUpToFrame)
			{
			EvDecimal frame=fc.getFrame();
			//TODO replace with visible set
			for(Map.Entry<EvPath, Lineage> entry:fc.getSelectedData().getIdObjectsRecursive(Lineage.class).entrySet())
				EvLog.printLog(entry.getKey().toString()+" numberOfParticles: "+entry.getValue().countParticlesUpTo(frame));
			}
		else if(e.getSource()==miSwapChildren)
			actionSwapChildren();
		else if(e.getSource()==miSetFate)
			{
			HashSet<LineageSelParticle> selectedNuclei=LineageCommonUI.getSelectedParticles();
			if(selectedNuclei.size()==1)
				new LineageFateDialog(null, selectedNuclei.iterator().next());
			else
				JOptionPane.showMessageDialog(parentComponent, "Select 1 particle first");
			}
		else if(e.getSource()==miSetDesc)
			actionSetDesc(LineageCommonUI.getSelectedParticles());
		else if(e.getSource()==miSetOverrideStartFrame)
			actionSetStartFrameDialog(LineageCommonUI.getSelectedParticles());
		else if(e.getSource()==miSetOverrideEndFrame)
			actionSetEndFrameDialog(LineageCommonUI.getSelectedParticles());
		else if(e.getSource()==miDeleteParticle)
			actionRemove(LineageCommonUI.getSelectedParticles());
		else if(e.getSource()==miSelectChildren)
			{
			actionRecursiveSelectChildren();
			}
		else if(e.getSource()==miSelectParents)
			{
			Set<LineageSelParticle> children=new HashSet<LineageSelParticle>(LineageCommonUI.getSelectedParticles());
			for(LineageSelParticle p:children)
				actionSecursiveSelectParents(p.fst(), p.snd());
			BasicWindow.updateWindows();
			}
		else if(e.getSource()==miSelectAllSameName)
			{
			HashSet<String> names=new HashSet<String>();
			for(LineageSelParticle p:LineageCommonUI.getSelectedParticles())
				names.add(p.snd());
			for(EvData data:EvData.openedData)
				for(Lineage lin:data.getObjects(Lineage.class))
					for(String n:names)
						if(lin.particle.containsKey(n))
							EvSelection.select(new LineageSelParticle(lin,n));
			BasicWindow.updateWindows();
			}
		else if(e.getSource()==miRename)
			{
			LineageRenameParticleDialog.run(EvSelection.getSelected(LineageSelParticle.class), null);
			BasicWindow.updateWindows();
			}
		else if(e.getSource()==miIntegrate)
			{
			new ParticleDialogIntegrate();
			}
		/*else if(e.getSource()==miMapModel)
			{
			mapModel();
			}*/
			
		}
	
	
	/**
		 * Selection of particles by mouse and keyboard
		 * @param sel Which particles, never null
		 * @param shift True if shift-key held
		 */
		public static void mouseSelectParticle(LineageSelParticle sel, boolean shift)
			{
			String nucname=sel.snd();
			//Shift-key used to select multiple
			if(shift)
				{
				if(!nucname.equals(""))
					{
					if(EvSelection.isSelected(sel))
						EvSelection.unselect(sel);
					else
						EvSelection.select(sel);
					}
				}
			else
				{
				EvSelection.unselectAll();
				if(!nucname.equals(""))
					EvSelection.select(sel);
				}
			BasicWindow.updateWindows();
			}

	/**
	 * Make sure objects are not modified - at the moment there is no semantic difference compared to the other method
	 * but this might change in the future. it guarantees that the selection set is really a copy and will not be modified
	 * outside the scope
	 */
	public static HashSet<LineageSelParticle> getSelectedParticleClone()
		{
		HashSet<LineageSelParticle> set=new HashSet<LineageSelParticle>();
		for(LineageSelParticle s:EvSelection.getSelected(LineageSelParticle.class))
			if(s.fst().particle.containsKey(s.snd()))
				set.add(s.clone());
		return set;
		}

	/**
	 * Get selected particles. Also ensures that no invalid selections are included (nuclei that does not exist).
	 * If a cell is hovered then 
	 */
	public static HashSet<LineageSelParticle> getSelectedParticles()
		{
		HashSet<LineageSelParticle> sels=new HashSet<LineageSelParticle>();
			for(LineageSelParticle sel:EvSelection.getSelected(LineageSelParticle.class))
				if(sel.fst().particle.containsKey(sel.snd()))
					sels.add(sel);
		return sels;
		}
	
	public static HashSet<LineageSelParticle> getSelectedOrHoveredParticle()
		{
		HashSet<LineageSelParticle> sels=new HashSet<LineageSelParticle>();
		if(currentHover!=emptyHover)
			{
			LineageSelParticle sel=currentHover;
			if(sel.fst().particle.containsKey(sel.snd()))
				sels.add(sel);
			}
		else
			{
			for(LineageSelParticle sel:EvSelection.getSelected(LineageSelParticle.class))
				if(sel.fst().particle.containsKey(sel.snd()))
					sels.add(sel);
			}
		return sels;
		}
	

	/**
	 * Divide a particle at the specified frame. Only works if the cell to divide has no children
	 */
	public static void actionDivideParticle(Lineage lin, String parentName, EvDecimal frame)
		{
		lin.removePosAfter(parentName, frame, true);
		Particle n=lin.particle.get(parentName);
		if(n!=null && n.child.isEmpty())
			{
			String c1n=lin.getUniqueParticleName();
			Particle c1=lin.getCreateParticle(c1n);
			String c2n=lin.getUniqueParticleName();
			Particle c2=lin.getCreateParticle(c2n);
			n.child.add(c1n);
			n.child.add(c2n);
			c1.parent=parentName;
			c2.parent=parentName;
			
			Lineage.ParticlePos pos=n.pos.get(n.pos.lastKey());
			Lineage.ParticlePos c1p=pos.clone();
			Lineage.ParticlePos c2p=pos.clone();
			c1p.x-=pos.r/2;
			c2p.x+=pos.r/2;
			c1.pos.put(frame, c1p);
			c2.pos.put(frame, c2p);
			}
		lin.setMetadataModified();
		BasicWindow.updateWindows();
		}

	/**
	 * Unassociate parent 
	 */
	public static void actionUnassocParent()
		{
		final Collection<LineageSelParticle> sel=LineageCommonUI.getSelectedParticleClone();
		
		new UndoOpLineageReplaceSomeParticle("Unassociate parent")
			{
			public void redo()
				{
				for(LineageSelParticle childSel:sel)
					{
					String parentName=childSel.getNuc().parent;
					if(parentName!=null)
						{
						//Store away parent and child
						Lineage lin=childSel.fst();
						keep(lin, parentName);
						keep(lin, childSel.snd());
						
						//Now modify both
						lin.removeParentReference(childSel.snd());
						}
					BasicWindow.updateWindows();
					}
				}
			}.execute();
		}

	/**
	 * Associate parent
	 */
	public static void actionAssocParent()
		{
		HashSet<LineageSelParticle> selectedNuclei=LineageCommonUI.getSelectedParticles();
		if(selectedNuclei.size()>1)
			{
			String parentName=null;
			EvDecimal parentFrame=new EvDecimal(0);
			Lineage.Particle parent=null;
			final Lineage lin=selectedNuclei.iterator().next().fst();
			
			//Decide which is the parent
			for(LineageSelParticle childPair:selectedNuclei)
				if(childPair.fst()==lin)
					{
					String childName=childPair.snd();
					Lineage.Particle n=lin.particle.get(childName);
					EvDecimal firstFrame=n.getFirstFrame();
					if(parentName==null || firstFrame.less(parentFrame))
						{
						parentFrame=firstFrame;
						parentName=childName;
						parent=n;
						}
					}
			
			if(parent==null)
				JOptionPane.showMessageDialog(null, "Could not decide which particle is the parent");
			else
				{
				//Get children
				final List<String> childNames=new LinkedList<String>();
				for(LineageSelParticle childPair:selectedNuclei)
					if(childPair.fst()==lin)
						{
						String childName=childPair.snd();
						Lineage.Particle childNuc=lin.particle.get(childName);
						if(!childName.equals(parentName) && childNuc.parent==null)
							childNames.add(childName);
						}
				if(childNames.isEmpty())
					JOptionPane.showMessageDialog(null, "Couldn't find any children to assign to "+parent);
				else
					{
					//Carry out operation
					final String fParentName=parentName;
					new UndoOpLineageReplaceSomeParticle("Associate parent")
						{
						public void redo()
							{
							keep(lin, fParentName);
							for(String childName:childNames)
								{
								//NucLineage.Nuc n=lin.nuc.get(childName);
								keep(lin, childName);
								
								lin.associateParentChildCheckNoLoop(fParentName, childName);
								}
							lin.setMetadataModified();
							}
						}.execute();
					}
				}
			}
		else
			JOptionPane.showMessageDialog(null, "Select at least two particles from the same lineage");
		BasicWindow.updateWindows();
		}

	
	/**
	 * Merge the frames etc of two particles
	 */
	public static void actionMergeNuclei()
		{
		HashSet<LineageSelParticle> selectedNuclei=LineageCommonUI.getSelectedParticles();
		if(selectedNuclei.size()==2)
			{
			Iterator<LineageSelParticle> nucit=selectedNuclei.iterator();
			LineageSelParticle target=nucit.next();
			LineageSelParticle source=nucit.next();
			if(target.fst()==source.fst())
				{
				//If one particle has a name that starts with : then it is the anonymous particle and will be merged-in. Otherwise it is arbitrary which name the new cell gets
				if(!target.snd().startsWith(":"))
					{
					LineageSelParticle temp=target;
					target=source;
					source=temp;
					}
				
				//This will not restore the selection however!
				final Lineage lin=source.fst();
				final String fTarget=target.snd();
				final String fSource=source.snd();
				new UndoOpLineageReplaceAllParticle("Merge particles", source.fst())
					{
					public void redo()
						{
						lin.mergeParticles(fSource, fTarget);
						}
					}.execute();
				}
			else
				JOptionPane.showMessageDialog(null, "Selected particles are not from the same lineage");
			BasicWindow.updateWindows();
			}
		else
			JOptionPane.showMessageDialog(null, "2 particles must be selected");
		}

	
	
	/**
	 * Swap the names of two children (but not the tree structure)
	 */
	public static void actionSwapChildren()
		{
		LinkedList<Tuple<Lineage,String>> selnucs=new LinkedList<Tuple<Lineage,String>>(LineageCommonUI.getSelectedParticles());  //getSelected could return nuc that is hovered!!!!
		if(selnucs.size()==1)
			{
			final Lineage lin=selnucs.get(0).fst();
			final String parentName=selnucs.get(0).snd();
			Lineage.Particle parentNuc=lin.particle.get(parentName);
			if(parentNuc.child.size()==2)
				{
				new UndoOpLineageReplaceAllParticle("Swap children",lin)
					{
					public void redo()
						{
						//Get the children
						Lineage.Particle parentNuc=lin.particle.get(parentName);
						Iterator<String> itChild=parentNuc.child.iterator();
						String childNameA=itChild.next();
						String childNameB=itChild.next();
						Lineage.Particle nucA=lin.particle.get(childNameA);
						Lineage.Particle nucB=lin.particle.get(childNameB);
						
						//Swap names
						lin.particle.remove(childNameA);
						lin.particle.remove(childNameB);
						lin.particle.put(childNameA, nucB);
						lin.particle.put(childNameB, nucA);
						
						//Update parent references to these children
						for(String childName:nucA.child)
							lin.particle.get(childName).parent=childNameB;
						for(String childName:nucB.child)
							lin.particle.get(childName).parent=childNameA;
						
						BasicWindow.updateWindows();
						}
					}.execute();
				
				}
			else
				JOptionPane.showMessageDialog(null, "Selected particle does not have 2 children");
			}
		else
			JOptionPane.showMessageDialog(null, "Select 1 particle first");
		}

	

	
	
	/**
	 * Set override end frame of particle
	 */
	public static void actionSetEndFrameDialog(Collection<LineageSelParticle> nucs)
		{
		if(!nucs.isEmpty())
			{
			final String sFrame=JOptionPane.showInputDialog("End frame, or empty for none");
			if(sFrame!=null)
				{
				final EvDecimal frame;
				if(sFrame.equals(""))
					frame=null;
				else
					frame=FrameControl.parseTime(sFrame);
				actionSetEndFrame(nucs, frame);
				}
			}
		}
	
	public static void actionSetEndFrame(final Collection<LineageSelParticle> nucs, final EvDecimal frame)
		{
		new UndoOpLineageReplaceSomeParticle("Set end frame")
			{
			public void redo()
				{
				for(LineageSelParticle nucPair:nucs)
					{
					Lineage.Particle n=nucPair.getNuc();
					if(n!=null)
						{
						keep(nucPair.fst(), nucPair.snd());
						n.overrideEnd=frame;
						if(frame!=null)
							nucPair.fst().removePosAfter(LineageCommonUI.currentHover.snd(), frame, false); 
						}
					}
				BasicWindow.updateWindows();
				}
			}.execute();		
		}

	
	/**
	 * Set override start frame of particle
	 */
	public static void actionSetStartFrameDialog(final Collection<LineageSelParticle> nucs)
		{
		if(!nucs.isEmpty())
			{
			final String sFrame=JOptionPane.showInputDialog("Start frame, or empty for none");
			if(sFrame!=null)
				{
				final EvDecimal frame;
				if(sFrame.equals(""))
					frame=null;
				else
					frame=FrameControl.parseTime(sFrame);
				actionSetStartFrame(nucs, frame);
				}
			}
		}
	
	public static void actionSetStartFrame(final Collection<LineageSelParticle> nucs, final EvDecimal frame)
		{
		new UndoOpLineageReplaceSomeParticle("Set start frame")
			{
			public void redo()
				{
				for(LineageSelParticle nucPair:nucs)
					{
					Lineage.Particle n=nucPair.getNuc();
					if(n!=null)
						{
						keep(nucPair.fst(), nucPair.snd());
						n.overrideStart=frame;
						if(frame!=null)
							nucPair.fst().removePosBefore(LineageCommonUI.currentHover.snd(), frame, false);  
						}
					}
				BasicWindow.updateWindows();
				}
			}.execute();
		}
	
		
	/**
	 * Remove selected particles
	 */
	public static void actionRemove(final Collection<LineageSelParticle> nucs)
		{
		if(!nucs.isEmpty())
			{
			String nucNames="";
			for(LineageSelParticle nucName:nucs)
				nucNames=nucNames+nucName.snd()+" ";
			int option = JOptionPane.showConfirmDialog(null, "Really want to delete: "+nucNames, "Remove?", JOptionPane.YES_NO_OPTION);
			if (option == JOptionPane.YES_OPTION)
				{
				new UndoOpLineageReplaceSomeParticle("Delete "+nucNames)
					{
					public void redo()
						{
						for(LineageSelParticle nucPair:nucs)
							{
							Lineage.Particle thisNuc=nucPair.getNuc();
							Lineage lin=nucPair.fst();
							
							//Store away parent, this particle, and children
							keep(lin, nucPair.snd());
							if(thisNuc.parent!=null)
								keep(lin,thisNuc.parent);
							for(String childName:thisNuc.child)
								keep(lin,childName);
							
							lin.removeParticle(nucPair.snd());
							}
						BasicWindow.updateWindows();
						}
					}.execute();
				}
			BasicWindow.updateWindows();
			}

		}

	
	/**
	 * Set description of particle
	 */
	public static void actionSetDesc(Collection<LineageSelParticle> nucs)
		{
		for(LineageSelParticle nucPair:nucs)
			{
			final Lineage lin=nucPair.fst();
			final String nucName=nucPair.snd();
			String newDesc=JOptionPane.showInputDialog("Description for "+nucName);
			if(newDesc!=null)
				{
				if(newDesc.equals(""))
					newDesc=null;
				final String fNewDesc=newDesc;
				
				new UndoOpLineageReplaceSomeParticle("Set description")
					{
					public void redo()
						{
						keep(lin, nucName);
						lin.particle.get(nucName).description=fNewDesc;
						}
					}.execute();
				
				}
			}
		BasicWindow.updateWindows();
		}



	/**
	 * Recursively select children of selected parents.
	 * This is never more than O(number of particles in lineages) 
	 */
	public static void actionRecursiveSelectChildren()
		{
		Set<LineageSelParticle> alreadySelected=new HashSet<LineageSelParticle>();
		for(LineageSelParticle p:new HashSet<LineageSelParticle>(LineageCommonUI.getSelectedParticles()))
			recursiveSelectChildren(p, alreadySelected);
		BasicWindow.updateWindows();
		}
	private static void recursiveSelectChildren(LineageSelParticle thisSel, Set<LineageSelParticle> alreadySelected)
		{
		if(!alreadySelected.contains(thisSel))
			{
			alreadySelected.add(thisSel);
			EvSelection.select(thisSel);
			for(String childName:thisSel.getNuc().child)
				recursiveSelectChildren(new LineageSelParticle(thisSel.fst(),childName), alreadySelected);
			}
		}

	/**
	 * Recursively select parents
	 */
	public static void actionSecursiveSelectParents(Lineage lin, String nucName)
		{
		String pname=lin.particle.get(nucName).parent;
		if(pname!=null)
			{
			EvSelection.select(new LineageSelParticle(lin, pname));
			actionSecursiveSelectParents(lin, pname);
			}
		}

	public static void actionSelectAll(Lineage lin)
		{
		for(String s:lin.particle.keySet())
			EvSelection.select(new LineageSelParticle(lin,s));
		}

	

	/**
	 * Show position of selected particles in console
	 * @param frame
	 */
	public static void actionPrintPos(EvDecimal frame)
		{
		for(LineageSelParticle p:LineageCommonUI.getSelectedParticles())
			{
			Lineage.ParticlePos npos=p.getNuc().interpolatePos(frame).pos;
			ConsoleWindow.openConsole();
			EvLog.printLog("pos "+p.snd()+": "+npos.x+" , "+npos.y+" , "+npos.z+"  r: "+npos.r);
			}
			
		}

	/**
	 * Calculate angles between selected particles 
	 * @param frame
	 */
	public static void actionPrintAngle(EvDecimal frame)
		{
		ConsoleWindow.openConsole();
		HashSet<LineageSelParticle> selectedNuclei=LineageCommonUI.getSelectedParticles();
		if(selectedNuclei.size()==3)
			{
			Iterator<LineageSelParticle> it=selectedNuclei.iterator();
			LineageSelParticle nucpA=it.next();
			LineageSelParticle nucpB=it.next();
			LineageSelParticle nucpC=it.next();
			Vector3d pA=nucpA.getNuc().interpolatePos(frame).pos.getPosCopy();
			Vector3d pB=nucpB.getNuc().interpolatePos(frame).pos.getPosCopy();
			Vector3d pC=nucpC.getNuc().interpolatePos(frame).pos.getPosCopy();
			
			double scale=360/(2*Math.PI);
			
			EvLog.printLog("angles "+nucpB.snd()+"-"+nucpC.snd()+"-"+nucpA.snd()+"  "+
					(scale*EvGeomUtil.midAngle(pA, pB, pC))+" "+
					(scale*EvGeomUtil.midAngle(pB, pC, pA))+" "+
					(scale*EvGeomUtil.midAngle(pC, pA, pB)));
			}
		else
			{
			EvLog.printLog("Select 3 particles first");
			for(LineageSelParticle p:selectedNuclei)
				EvLog.printLog(p.toString());
			}
		
		}

	/**
	 * Generate a menu for setting color on particles
	 */
	public static JMenu makeSetColorMenu(final EvColor... exclude)
		{
		JMenu m = new JMenu("Set color");
	
		JMenuItem miRemove = new JMenuItem("<Remove>");
		miRemove.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
					{
					for (LineageSelParticle p : LineageCommonUI.getSelectedParticles())
						p.getNuc().color = null;
					BasicWindow.updateWindows();
					}
			});
		m.add(miRemove);
		
		JMenuItem miRainbow = new JMenuItem("<Rainbow>");
		miRainbow.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
					{
					ArrayList<EvColor> colors = new ArrayList<EvColor>(Arrays
							.asList(EvColor.colorList));
					for (EvColor c : exclude)
						colors.remove(c);
					colors.remove(EvColor.black);
					for (LineageSelParticle p : LineageCommonUI.getSelectedParticles())
						{
						int pi = Math.abs(p.snd().hashCode())%colors.size();
						p.getNuc().color = colors.get(pi).c;
						}
					BasicWindow.updateWindows();
					}
			});
		m.add(miRainbow);
	
		
		JMenuItem miCustom = new JMenuItem("<Enter RGB>");
		miCustom.addActionListener(new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
				{
				String sr=JOptionPane.showInputDialog("Red 0-255");
				if(sr==null) return;
				String sg=JOptionPane.showInputDialog("Green 0-255");
				if(sg==null) return;
				String sb=JOptionPane.showInputDialog("Blue 0-255");
				if(sb==null) return;
				
				Color awtc=new Color(Integer.parseInt(sr),Integer.parseInt(sg),Integer.parseInt(sb));
				EvColor c=new EvColor("Custom",awtc);
				
				for (LineageSelParticle p : LineageCommonUI.getSelectedParticles())
					p.getNuc().color = c.c;
				BasicWindow.updateWindows();
				}
			});
		m.add(miCustom);
		
		
		EvColor.addColorMenuEntries(m, new ColorMenuListener(){
			public void setColor(EvColor c)
				{
				for (LineageSelParticle p : LineageCommonUI.getSelectedParticles())
					p.getNuc().color = c.c;
				BasicWindow.updateWindows();
				}
		});
		
		
		return m;
		}
	
	
	
	/**
	 * Return a copy of the position at a frame, interpolate if needed
	 */
	public static Lineage.ParticlePos getOrInterpolatePosCopy(Lineage lin, String name, EvDecimal frame)
		{
		Lineage.Particle n=lin.particle.get(name);
		if(n==null)
			return null;
		Lineage.ParticlePos pos=n.pos.get(frame);
		if(pos!=null)
			return pos.clone();
		else
			{
			InterpolatedParticle interp=n.interpolatePos(frame);
			if(interp==null)
				return null;
			pos=new Lineage.ParticlePos();
			pos.x=interp.pos.x;
			pos.y=interp.pos.y;
			pos.z=interp.pos.z;
			pos.r=interp.pos.r;
			//Anything else here?
			
			}
		return pos;
		}
	
	
	/**
	 * Map e.g. the c.elegans model onto this lineage
	 */
	public static void mapModel(EvContainer con, Lineage lin)
		{
		EvData modelData=GuiEvDataIO.loadFileDialog("Choose c.elegans model");
		if(modelData!=null)
			{
			Iterator<Lineage> lins=modelData.getIdObjectsRecursive(Lineage.class).values().iterator();
			if(lins.hasNext())
				{
				Lineage modelLin=lins.next();
				Lineage mappedLin=LineageMergeUtil.mapModelToRec(lin, modelLin);
				con.metaObject.put("estcell", mappedLin);
				BasicWindow.updateWindows();
				}
			else
				BasicWindow.showErrorDialog("No lineage in file");
			}
		}


	}
