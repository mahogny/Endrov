package endrov.nuc;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
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
import endrov.lineageWindow.FateDialog;
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
	private JMenuItem miNewNuc=new JMenuItem("Create empty nucleus");
	private JMenuItem miMerge=new JMenuItem("Merge nuclei");
	private JMenuItem miPC=new JMenuItem("Associate parent");
	private JMenuItem miUnparent=new JMenuItem("Unassociate from parent");
	private JMenuItem miSwapChildren=new  JMenuItem("Swap children names*");
	private JMenuItem miFate=new JMenuItem("Set fate");
	private JMenuItem miSetDesc=new JMenuItem("Set description");
	private JMenuItem miStartFrame=new JMenuItem("Set override start frame");
	private JMenuItem miEndFrame=new JMenuItem("Set override end frame");
	private JMenuItem miDeleteNucleus=new JMenuItem("Delete nucleus");
	private JMenuItem miSelectChildren=new JMenuItem("Select children");
	private JMenuItem miSelectParents=new JMenuItem("Select parents");
	private JMenuItem miSelectAll=new JMenuItem("Select all in this lineage");
	private JMenuItem miSelectAllName=new JMenuItem("Select all w/ the same name");

	
	private JComponent parent;
	
	public NucCommonUI(JComponent parent)
		{
		this.parent=parent;
		}
	
	public void addToMenu(JMenu menuLineage, boolean addAccel)
		{


		
		miRename.addActionListener(this);
		miNewNuc.addActionListener(this);
		miMerge.addActionListener(this);
		miPC.addActionListener(this);
		miUnparent.addActionListener(this);
		miSwapChildren.addActionListener(this);
		miFate.addActionListener(this);
		miSetDesc.addActionListener(this);
		miStartFrame.addActionListener(this);
		miEndFrame.addActionListener(this);
		miDeleteNucleus.addActionListener(this);
		miSelectChildren.addActionListener(this);
		miSelectParents.addActionListener(this);
		miSelectAll.addActionListener(this);
		miSelectAllName.addActionListener(this);

		
		menuLineage.add(miSelectChildren);
		menuLineage.add(miSelectParents);
//		menuLineage.add(miSelectAll);
		menuLineage.add(miSelectAllName);

		menuLineage.add(miRename);
		menuLineage.add(miNewNuc);
		menuLineage.add(miMerge);
		menuLineage.add(miPC);
		menuLineage.add(miUnparent);
		menuLineage.add(miSwapChildren);
		menuLineage.add(miFate);
		menuLineage.add(miSetDesc);
		menuLineage.add(miStartFrame);
		menuLineage.add(miEndFrame);
		menuLineage.add(miDeleteNucleus);
		menuLineage.add(NucCommonUI.makeSetColorMenu());
		//menuLineage.addSeparator();

		if(addAccel)
			{
			miRename.setAccelerator(KeyStroke.getKeyStroke("R"));  //'R',Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
			miPC.setAccelerator(KeyStroke.getKeyStroke("P"));  //'P',Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
			miDeleteNucleus.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
			}
		}
	
	
	public void actionPerformed(ActionEvent e)
		{
		if(e.getSource()==miMerge)
			{
			HashSet<NucSel> selectedNuclei=NucLineage.getSelectedNuclei();
			if(!selectedNuclei.isEmpty())
				{
				Iterator<NucSel> nucit=selectedNuclei.iterator();
				NucSel target=nucit.next();
				NucLineage theLineage=target.fst();
//				Vector<String> nucToMerge=new Vector<String>();
//				nucToMerge.add(target.getRight());
//				String suggestName=target.getRight();
				while(nucit.hasNext())
					{
					NucSel source=nucit.next();
					if(theLineage==source.fst())
						{
//						nucToMerge.add(source.getRight());
						if(!target.snd().startsWith(":"))
							{
							NucSel temp=target;
							target=source;
							source=temp;
							}
						target.fst().mergeNuclei(source.snd(), target.snd());
						}
					}
//				suggestName=JOptionPane.showInputDialog("Give name of merged nucleus:", suggestName);
//				if(suggestName)
				BasicWindow.updateWindows();
				}
			else
				JOptionPane.showMessageDialog(parent, "Must select nuclei first");
			}
		else if(e.getSource()==miPC)
			{
			HashSet<NucSel> selectedNuclei=NucLineage.getSelectedNuclei();
			if(!selectedNuclei.isEmpty())
				NucLineage.createParentChildSelected();
			BasicWindow.updateWindows();
			}
		else if(e.getSource()==miUnparent)
			{
			for(Tuple<NucLineage,String> nuc:NucLineage.getSelectedNuclei())
				(nuc.fst()).removeParentReference(nuc.snd());
			BasicWindow.updateWindows();
			}
		else if(e.getSource()==miSwapChildren)
			{
			
			}
		else if(e.getSource()==miFate)
			{
			HashSet<NucSel> selectedNuclei=NucLineage.getSelectedNuclei();
			if(selectedNuclei.size()==1)
//				new FateDialog(evw, NucLineage.selectedNuclei.iterator().next()); need Frame
				new FateDialog(null, selectedNuclei.iterator().next());
			else
				JOptionPane.showMessageDialog(parent, "Select 1 nucleus first");
			}
		else if(e.getSource()==miSetDesc)
			actionSetDesc(NucLineage.getSelectedNuclei());
		else if(e.getSource()==miStartFrame)
			actionSetStartFrame(NucLineage.getSelectedNuclei());
		else if(e.getSource()==miEndFrame)
			actionSetEndFrame(NucLineage.getSelectedNuclei());
		else if(e.getSource()==miDeleteNucleus)
			actionRemove(NucLineage.getSelectedNuclei());
		else if(e.getSource()==miSelectChildren)
			{
			Set<NucSel> parents=new HashSet<NucSel>(NucLineage.getSelectedNuclei());
			for(NucSel p:parents)
				recursiveSelectChildren(p.fst(), p.snd());
			BasicWindow.updateWindows();
			}
		else if(e.getSource()==miSelectParents)
			{
			Set<NucSel> children=new HashSet<NucSel>(NucLineage.getSelectedNuclei());
			for(NucSel p:children)
				recursiveSelectParent(p.fst(), p.snd());
			BasicWindow.updateWindows();
			}
		/*else if(e.getSource()==miSelectAll)
			{
			if(view.currentLin!=null)
				selectAll(view.currentLin);
			BasicWindow.updateWindows();
			}*/
		else if(e.getSource()==miSelectAllName)
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
			
			
		}
	

	
	/**
	 * Show position of selected nuclei in console
	 * @param frame
	 */
	public static void actionShowPos(EvDecimal frame)
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
	public static void calcAngle(EvDecimal frame)
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
		
		/*
		for (final EvColor c : EvColor.colorList)
			{
			BufferedImage bim=new BufferedImage(16,16,BufferedImage.TYPE_INT_BGR);
			Graphics g=bim.getGraphics();
			g.setColor(c.c);
			g.fillRect(0, 0, 16, 16);
			
			JMenuItem mi = new JMenuItem(c.name,new ImageIcon(bim));
			
			mi.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
						{
						for (NucPair p : selectedNuclei)
							p.fst().nuc.get(p.snd()).colorNuc = c.c;
						BasicWindow.updateWindows();
						}
				});
			m.add(mi);
			}
		*/
		
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

	/**
	 * Set override end frame of nuclei
	 */
	public static void actionSetEndFrame(Collection<NucSel> nucs)
		{
		if(!nucs.isEmpty())
			{
			String sFrame=JOptionPane.showInputDialog("End frame or empty for none");
			if(sFrame!=null)
				{
				for(NucSel nucPair:nucs)
					{
					NucLineage.Nuc n=nucPair.getNuc();
					if(n!=null)
						{
						if(sFrame.equals(""))
							n.overrideEnd=null;
						else
							{
							EvDecimal frame=new EvDecimal(sFrame);
							n.overrideEnd=frame;
							nucPair.fst().removePosAfter(NucLineage.currentHover.snd(), frame, false);
							}
						}
					}
				BasicWindow.updateWindows();
				}
			}
		}

	/**
	 * Set override start frame
	 */
	public static void actionSetStartFrame(Collection<NucSel> nucs)
		{
		if(!nucs.isEmpty())
			{
			String sFrame=JOptionPane.showInputDialog("Start frame or empty for none");
			if(sFrame!=null)
				{
				for(NucSel nucPair:nucs)
					{
					NucLineage.Nuc n=nucPair.getNuc();
					if(n!=null)
						{
						if(sFrame.equals(""))
							n.overrideStart=null;
						else
							{
							EvDecimal frame=FrameControl.parseTime(sFrame);
							n.overrideStart=frame;
							nucPair.fst().removePosAfter(NucLineage.currentHover.snd(), frame, false);
							}
						}
					}
				BasicWindow.updateWindows();
				}
			}
		}
	
	public static void actionRemove(Collection<NucSel> nucs)
		{
		if(!nucs.isEmpty())
			{
			String nucNames="";
			for(NucSel nucName:nucs)
				nucNames=nucNames+nucName.snd()+" ";
			int option = JOptionPane.showConfirmDialog(null, "Really want to delete: "+nucNames, "Remove?", JOptionPane.YES_NO_OPTION);
			if (option == JOptionPane.YES_OPTION)
				for(NucSel nucPair:nucs)
					nucPair.fst().removeNuc(nucPair.snd());
			BasicWindow.updateWindows();
			}

		}
	
	public static void actionSetDesc(Collection<NucSel> nucs)
		{
		for(NucSel nucPair:nucs)
			{
			String nucName=nucPair.snd();
			NucLineage.Nuc n=nucPair.getNuc();
			if(n!=null)
				{
				String newDesc=JOptionPane.showInputDialog("Description for "+nucName);
				if(newDesc!=null)
					{
					if(newDesc.equals(""))
						newDesc=null;
					n.description=newDesc;
					}
				}
			}
		BasicWindow.updateWindows();
		}

	
	

	/**
	 * Recursively select children
	 */
	public static void recursiveSelectChildren(NucLineage lin, String nucName)
		{
		NucLineage.Nuc nuc=lin.nuc.get(nucName);
		//NucLineage.selectedNuclei.add(new NucPair(lin, nucName));
		EvSelection.select(new NucSel(lin, nucName));
		for(String childName:nuc.child)
			recursiveSelectChildren(lin, childName);
		}
	
	/**
	 * Recursively select parent
	 */
	public static void recursiveSelectParent(NucLineage lin, String nucName)
		{
		String pname=lin.nuc.get(nucName).parent;
		if(pname!=null)
			{
			//NucLineage.selectedNuclei.add(new NucPair(lin, pname));
			EvSelection.select(new NucSel(lin, pname));
			recursiveSelectParent(lin, pname);
			}
		}

	public static void selectAll(NucLineage lin)
		{
		for(String s:lin.nuc.keySet())
			EvSelection.select(new NucSel(lin,s));
		}
	
	}
