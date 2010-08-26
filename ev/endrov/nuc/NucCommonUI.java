/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.nuc;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
import endrov.basicWindow.EvColor.ColorMenuListener;
import endrov.consoleWindow.ConsoleWindow;
import endrov.data.EvData;
import endrov.data.EvSelection;
import endrov.ev.EvLog;
import endrov.undo.UndoOpBasic;
import endrov.util.EvDecimal;
import endrov.util.EvGeomUtil;
import endrov.util.Tuple;

/**
 * Common GUI functions for Nucleii/Lineage, mainly menus
 * @author Johan Henriksson
 *
 */
public class NucCommonUI implements ActionListener
	{
	private JMenuItem miRename=new JMenuItem("Rename nucleus");
	private JMenuItem miCreateEmptyNucleus=new JMenuItem("Create empty nucleus");
	private JMenuItem miMergeNuclei=new JMenuItem("Merge nuclei");
	private JMenuItem miAssocParent=new JMenuItem("Associate parent");
	private JMenuItem miUnassocParent=new JMenuItem("Unassociate from parent");
	private JMenuItem miSwapChildren=new  JMenuItem("Swap children names*");
	private JMenuItem miSetFate=new JMenuItem("Set fate");
	private JMenuItem miSetDesc=new JMenuItem("Set description");
	private JMenuItem miSetOverrideStartFrame=new JMenuItem("Set override start frame");
	private JMenuItem miSetOverrideEndFrame=new JMenuItem("Set override end frame");
	private JMenuItem miDeleteNucleus=new JMenuItem("Delete nucleus");
	private JMenuItem miSelectChildren=new JMenuItem("Select children");
	private JMenuItem miSelectParents=new JMenuItem("Select parents");
	private JMenuItem miSelectAll=new JMenuItem("Select all in this lineage");
	private JMenuItem miSelectAllSameName=new JMenuItem("Select all w/ the same name");

	
	private JComponent parentComponent;
	
	public NucCommonUI(JComponent parent)
		{
		this.parentComponent=parent;
		}
	
	public void addToMenu(JComponent menuLineage, boolean addAccel)
		{
		miRename.addActionListener(this);
		miCreateEmptyNucleus.addActionListener(this);
		miMergeNuclei.addActionListener(this);
		miAssocParent.addActionListener(this);
		miUnassocParent.addActionListener(this);
		miSwapChildren.addActionListener(this);
		miSetFate.addActionListener(this);
		miSetDesc.addActionListener(this);
		miSetOverrideStartFrame.addActionListener(this);
		miSetOverrideEndFrame.addActionListener(this);
		miDeleteNucleus.addActionListener(this);
		miSelectChildren.addActionListener(this);
		miSelectParents.addActionListener(this);
		miSelectAll.addActionListener(this);
		miSelectAllSameName.addActionListener(this);

		
		menuLineage.add(miSelectChildren);
		menuLineage.add(miSelectParents);
//		menuLineage.add(miSelectAll);
		menuLineage.add(miSelectAllSameName);

		menuLineage.add(miAssocParent);
		menuLineage.add(miCreateEmptyNucleus);
		menuLineage.add(miDeleteNucleus);
		menuLineage.add(miMergeNuclei);
		menuLineage.add(miRename);
		menuLineage.add(NucCommonUI.makeSetColorMenu());
		menuLineage.add(miSetDesc);
		menuLineage.add(miSetFate);
		menuLineage.add(miSetOverrideStartFrame);
		menuLineage.add(miSetOverrideEndFrame);
		menuLineage.add(miSwapChildren);
		menuLineage.add(miUnassocParent);
		//menuLineage.addSeparator();

		if(addAccel)
			{
			//Need ctrl+ or something. Collided with combobox in linw!
			

			//miRename.setAccelerator(KeyStroke.getKeyStroke("R"));  //'R',Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
			//miPC.setAccelerator(KeyStroke.getKeyStroke("P"));  //'P',Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
			//miDeleteNucleus.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));

			miRename.setAccelerator(KeyStroke.getKeyStroke('R',Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			miAssocParent.setAccelerator(KeyStroke.getKeyStroke('P',Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			}
		}
	
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==miMergeNuclei)
			actionMergeNuclei();
		else if(e.getSource()==miAssocParent)
			actionAssocParent();
		else if(e.getSource()==miUnassocParent)
			actionUnassocParent();
		else if(e.getSource()==miSwapChildren)
			actionSwapChildren();
		else if(e.getSource()==miSetFate)
			{
			HashSet<NucSel> selectedNuclei=NucLineage.getSelectedNuclei();
			if(selectedNuclei.size()==1)
				new FateDialog(null, selectedNuclei.iterator().next());
			else
				JOptionPane.showMessageDialog(parentComponent, "Select 1 nucleus first");
			}
		else if(e.getSource()==miSetDesc)
			actionSetDesc(NucLineage.getSelectedNuclei());
		else if(e.getSource()==miSetOverrideStartFrame)
			actionSetStartFrame(NucLineage.getSelectedNuclei());
		else if(e.getSource()==miSetOverrideEndFrame)
			actionSetEndFrame(NucLineage.getSelectedNuclei());
		else if(e.getSource()==miDeleteNucleus)
			actionRemove(NucLineage.getSelectedNuclei());
		else if(e.getSource()==miSelectChildren)
			{
			actionRecursiveSelectChildren();
			}
		else if(e.getSource()==miSelectParents)
			{
			Set<NucSel> children=new HashSet<NucSel>(NucLineage.getSelectedNuclei());
			for(NucSel p:children)
				actionSecursiveSelectParents(p.fst(), p.snd());
			BasicWindow.updateWindows();
			}
		else if(e.getSource()==miSelectAllSameName)
			{
			HashSet<String> names=new HashSet<String>();
			for(NucSel p:NucLineage.getSelectedNuclei())
				names.add(p.snd());
			for(EvData data:EvData.openedData)
				for(NucLineage lin:data.getObjects(NucLineage.class))
					for(String n:names)
						if(lin.nuc.containsKey(n))
							EvSelection.select(new NucSel(lin,n));
			BasicWindow.updateWindows();
			}
		else if(e.getSource()==miRename)
			{
			NucRenameDialog.run(EvSelection.getSelected(NucSel.class), null);
			BasicWindow.updateWindows();
			}
			
		}
	
	
	/**
	 * Unassociate parent 
	 */
	public static void actionUnassocParent()
		{
		final Collection<NucSel> sel=NucLineage.getSelectedNucleiClone();
		
		new UndoOpReplaceSomeNuclei("Unassociate parent")
			{
			public void redo()
				{
				for(NucSel childSel:sel)
					{
					String parentName=childSel.getNuc().parent;
					if(parentName!=null)
						{
						//Store away parent and child
						NucLineage lin=childSel.fst();
						keepNuc(lin, parentName);
						keepNuc(lin, childSel.snd());
						
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
		HashSet<NucSel> selectedNuclei=NucLineage.getSelectedNuclei();
		if(selectedNuclei.size()>1)
			{
			String parentName=null;
			EvDecimal parentFrame=new EvDecimal(0);
			NucLineage.Nuc parent=null;
			final NucLineage lin=selectedNuclei.iterator().next().fst();
			
			//Decide which is the parent
			for(NucSel childPair:selectedNuclei)
				if(childPair.fst()==lin)
					{
					String childName=childPair.snd();
					NucLineage.Nuc n=lin.nuc.get(childName);
					EvDecimal firstFrame=n.getFirstFrame();
					if(parentName==null || firstFrame.less(parentFrame))
						{
						parentFrame=firstFrame;
						parentName=childName;
						parent=n;
						}
					}
			
			if(parent==null)
				JOptionPane.showMessageDialog(null, "Could not decide which nucleus is the parent");
			else
				{
				//Get children
				final List<String> childNames=new LinkedList<String>();
				for(NucSel childPair:selectedNuclei)
					if(childPair.fst()==lin)
						{
						String childName=childPair.snd();
						NucLineage.Nuc childNuc=lin.nuc.get(childName);
						if(!childName.equals(parentName) && childNuc.parent==null)
							childNames.add(childName);
						}
				if(childNames.isEmpty())
					JOptionPane.showMessageDialog(null, "Couldn't find any children to assign to "+parent);
				else
					{
					//Carry out operation
					final String fParentName=parentName;
					new UndoOpReplaceSomeNuclei("Associate parent")
						{
						public void redo()
							{
							keepNuc(lin, fParentName);
							for(String childName:childNames)
								{
								NucLineage.Nuc n=lin.nuc.get(childName);
								keepNuc(lin, childName);
								n.parent=fParentName;
								lin.nuc.get(fParentName).child.add(childName);
								}
							lin.setMetadataModified();
							}
						}.execute();
					}
				}
			}
		else
			JOptionPane.showMessageDialog(null, "Select at least two nuclei from the same lineage");
		BasicWindow.updateWindows();
		}

	
	/**
	 * Merge the frames etc of two nuclei
	 */
	public static void actionMergeNuclei()
		{
		HashSet<NucSel> selectedNuclei=NucLineage.getSelectedNuclei();
		if(selectedNuclei.size()==2)
			{
			Iterator<NucSel> nucit=selectedNuclei.iterator();
			NucSel target=nucit.next();
			NucSel source=nucit.next();
			if(target.fst()==source.fst())
				{
				//If one nucleus has a name that starts with : then it is the anonymous nuclei and will be merged-in. Otherwise it is arbitrary which name the new cell gets
				if(!target.snd().startsWith(":"))
					{
					NucSel temp=target;
					target=source;
					source=temp;
					}
				
				//This will not restore the selection however!
				final NucLineage lin=source.fst();
				final String fTarget=target.snd();
				final String fSource=source.snd();
				new UndoOpReplaceAllNuclei("Merge nuclei", source.fst())
					{
					public void redo()
						{
						lin.mergeNuclei(fSource, fTarget);
						}
					}.execute();
				}
			else
				JOptionPane.showMessageDialog(null, "Selected nuclei are not from the same lineage");
			BasicWindow.updateWindows();
			}
		else
			JOptionPane.showMessageDialog(null, "2 nuclei must be selected");
		}

	
	
	/**
	 * Swap the names of two children (but not the tree structure)
	 */
	public static void actionSwapChildren()
		{
		LinkedList<Tuple<NucLineage,String>> selnucs=new LinkedList<Tuple<NucLineage,String>>(NucLineage.getSelectedNuclei());  //getSelected could return nuc that is hovered!!!!
		if(selnucs.size()==1)
			{
			final NucLineage lin=selnucs.get(0).fst();
			final String parentName=selnucs.get(0).snd();
			NucLineage.Nuc parentNuc=lin.nuc.get(parentName);
			if(parentNuc.child.size()==2)
				{
				new UndoOpReplaceAllNuclei("Swap children",lin)
					{
					public void redo()
						{
						//Get the children
						NucLineage.Nuc parentNuc=lin.nuc.get(parentName);
						Iterator<String> itChild=parentNuc.child.iterator();
						String childNameA=itChild.next();
						String childNameB=itChild.next();
						NucLineage.Nuc nucA=lin.nuc.get(childNameA);
						NucLineage.Nuc nucB=lin.nuc.get(childNameB);
						
						//Swap names
						lin.nuc.remove(childNameA);
						lin.nuc.remove(childNameB);
						lin.nuc.put(childNameA, nucB);
						lin.nuc.put(childNameB, nucA);
						
						//Update parent references to these children
						for(String childName:nucA.child)
							lin.nuc.get(childName).parent=childNameB;
						for(String childName:nucB.child)
							lin.nuc.get(childName).parent=childNameA;
						
						BasicWindow.updateWindows();
						}
					}.execute();
				
				}
			else
				JOptionPane.showMessageDialog(null, "Selected nucleus does not have 2 children");
			}
		else
			JOptionPane.showMessageDialog(null, "Select 1 nucleus first");
		}

	

	
	
	/**
	 * Set override end frame of nuclei
	 */
	public static void actionSetEndFrame(final Collection<NucSel> nucs)
		{
		if(!nucs.isEmpty())
			{
			final String sFrame=JOptionPane.showInputDialog("End frame, or empty for none");
			if(sFrame!=null)
				{
				final EvDecimal frame=FrameControl.parseTime(sFrame);
				new UndoOpBasic("Set end frame")
					{
					private HashMap<String, Tuple<NucLineage,NucLineage.Nuc>> oldnuc=new HashMap<String, Tuple<NucLineage,NucLineage.Nuc>>();  
					public void redo()
						{
						for(NucSel nucPair:nucs)
							{
							NucLineage.Nuc n=nucPair.getNuc();
							if(n!=null)
								{
								oldnuc.put(nucPair.snd(), Tuple.make(nucPair.fst(), n.clone()));
								if(sFrame.equals(""))
									n.overrideEnd=null;
								else
									{
									n.overrideEnd=frame;
									nucPair.fst().removePosAfter(NucLineage.currentHover.snd(), frame, false); 
									}
								}
							}
						BasicWindow.updateWindows();
						}

					public void undo()
						{
						for(String name:oldnuc.keySet())
							{
							NucLineage lin=oldnuc.get(name).fst();
							lin.nuc.put(name, oldnuc.get(name).snd());
							}
						}
					}.execute();
				}
			}
		}

	
	/**
	 * Set override start frame
	 */
	public static void actionSetStartFrame(final Collection<NucSel> nucs)
		{
		if(!nucs.isEmpty())
			{
			final String sFrame=JOptionPane.showInputDialog("Start frame, or empty for none");
			if(sFrame!=null)
				{
				final EvDecimal frame=FrameControl.parseTime(sFrame);
				new UndoOpBasic("Set start frame")
					{
					private HashMap<String, Tuple<NucLineage,NucLineage.Nuc>> oldnuc=new HashMap<String, Tuple<NucLineage,NucLineage.Nuc>>();  
					public void redo()
						{
						for(NucSel nucPair:nucs)
							{
							NucLineage.Nuc n=nucPair.getNuc();
							if(n!=null)
								{
								oldnuc.put(nucPair.snd(), Tuple.make(nucPair.fst(), n.clone()));
								if(sFrame.equals(""))
									n.overrideStart=null;
								else
									{
									n.overrideStart=frame;
									nucPair.fst().removePosBefore(NucLineage.currentHover.snd(), frame, false);  
									}
								}
							}
						BasicWindow.updateWindows();
						}

					public void undo()
						{
						for(String name:oldnuc.keySet())
							{
							NucLineage lin=oldnuc.get(name).fst();
							lin.nuc.put(name, oldnuc.get(name).snd());
							}
						}
					}.execute();
				}
			}
		}
	
		
	/**
	 * Remove selected nuclei
	 */
	public static void actionRemove(final Collection<NucSel> nucs)
		{
		if(!nucs.isEmpty())
			{
			String nucNames="";
			for(NucSel nucName:nucs)
				nucNames=nucNames+nucName.snd()+" ";
			int option = JOptionPane.showConfirmDialog(null, "Really want to delete: "+nucNames, "Remove?", JOptionPane.YES_NO_OPTION);
			if (option == JOptionPane.YES_OPTION)
				{
				new UndoOpReplaceSomeNuclei("Delete "+nucNames)
					{
					public void redo()
						{
						for(NucSel nucPair:nucs)
							{
							NucLineage.Nuc thisNuc=nucPair.getNuc();
							NucLineage lin=nucPair.fst();
							
							//Store away parent, this nucleus, and children
							keepNuc(lin, nucPair.snd());
							if(thisNuc.parent!=null)
								keepNuc(lin,thisNuc.parent);
							for(String childName:thisNuc.child)
								keepNuc(lin,childName);
							
							lin.removeNuc(nucPair.snd());
							}
						BasicWindow.updateWindows();
						}
					}.execute();
				}
			BasicWindow.updateWindows();
			}

		}

	
	/**
	 * Set description of cell
	 */
	public static void actionSetDesc(Collection<NucSel> nucs)
		{
		for(NucSel nucPair:nucs)
			{
			final NucLineage lin=nucPair.fst();
			final String nucName=nucPair.snd();
			String newDesc=JOptionPane.showInputDialog("Description for "+nucName);
			if(newDesc!=null)
				{
				if(newDesc.equals(""))
					newDesc=null;
				final String fNewDesc=newDesc;
				
				new UndoOpReplaceSomeNuclei("Set description")
					{
					public void redo()
						{
						keepNuc(lin, nucName);
						lin.nuc.get(nucName).description=fNewDesc;
						}
					}.execute();
				
				}
			}
		BasicWindow.updateWindows();
		}

	
	

	/**
	 * Recursively select children
	 */
	/*
	public static void recursiveSelectChildren(NucLineage lin, String nucName)
		{
		NucLineage.Nuc nuc=lin.nuc.get(nucName);
		EvSelection.select(new NucSel(lin, nucName));
		for(String childName:nuc.child)
			recursiveSelectChildren(lin, childName);
		}*/
	

	/**
	 * Recursively select children of selected parents.
	 * This is never more than O(number of nuclei in lineages) 
	 */
	public static void actionRecursiveSelectChildren()
		{
		Set<NucSel> alreadySelected=new HashSet<NucSel>();
		for(NucSel p:new HashSet<NucSel>(NucLineage.getSelectedNuclei()))
			recursiveSelectChildren(p, alreadySelected);
		BasicWindow.updateWindows();
		}
	private static void recursiveSelectChildren(NucSel thisSel, Set<NucSel> alreadySelected)
		{
		if(!alreadySelected.contains(thisSel))
			{
			alreadySelected.add(thisSel);
			EvSelection.select(thisSel);
			for(String childName:thisSel.getNuc().child)
				recursiveSelectChildren(new NucSel(thisSel.fst(),childName), alreadySelected);
			}
		}

	/**
	 * Recursively select parents
	 */
	public static void actionSecursiveSelectParents(NucLineage lin, String nucName)
		{
		String pname=lin.nuc.get(nucName).parent;
		if(pname!=null)
			{
			EvSelection.select(new NucSel(lin, pname));
			actionSecursiveSelectParents(lin, pname);
			}
		}

	public static void actionSelectAll(NucLineage lin)
		{
		for(String s:lin.nuc.keySet())
			EvSelection.select(new NucSel(lin,s));
		}

	

	/**
	 * Show position of selected nuclei in console
	 * @param frame
	 */
	public static void actionPrintPos(EvDecimal frame)
		{
		for(NucSel p:NucLineage.getSelectedNuclei())
			{
			NucLineage.NucPos npos=p.getNuc().interpolatePos(frame).pos;
			//Vector3d pos=p.fst().nuc.get(p.snd()).interpolatePos(frame).pos.getPosCopy();
			ConsoleWindow.openConsole();
			EvLog.printLog("pos "+p.snd()+": "+npos.x+" , "+npos.y+" , "+npos.z+"  r: "+npos.r);
			}
			
		}

	/**
	 * Calculate angles between selected nuclei 
	 * @param frame
	 */
	public static void actionPrintAngle(EvDecimal frame)
		{
		ConsoleWindow.openConsole();
		HashSet<NucSel> selectedNuclei=NucLineage.getSelectedNuclei();
		if(selectedNuclei.size()==3)
			{
			Iterator<NucSel> it=selectedNuclei.iterator();
			NucSel nucpA=it.next();
			NucSel nucpB=it.next();
			NucSel nucpC=it.next();
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
			EvLog.printLog("Select 3 nuclei first");
			for(NucSel p:selectedNuclei)
				EvLog.printLog(p.toString());
			}
		
		}

	/**
	 * Generate a menu for setting color on nuclei
	 */
	public static JMenu makeSetColorMenu(final EvColor... exclude)
		{
		JMenu m = new JMenu("Set nuclei color");
	
		JMenuItem miRemove = new JMenuItem("<Remove>");
		miRemove.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
					{
					for (NucSel p : NucLineage.getSelectedNuclei())
						p.getNuc().colorNuc = null;
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
					for (NucSel p : NucLineage.getSelectedNuclei())
						{
						int pi = Math.abs(p.snd().hashCode())%colors.size();
						p.getNuc().colorNuc = colors.get(pi).c;
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
				
				for (NucSel p : NucLineage.getSelectedNuclei())
					p.getNuc().colorNuc = c.c;
				BasicWindow.updateWindows();
				}
			});
		m.add(miCustom);
		
		
		EvColor.addColorMenuEntries(m, new ColorMenuListener(){
			public void setColor(EvColor c)
				{
				for (NucSel p : NucLineage.getSelectedNuclei())
					p.getNuc().colorNuc = c.c;
				BasicWindow.updateWindows();
				}
		});
		
		
		return m;
		}

	}
