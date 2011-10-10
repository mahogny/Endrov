/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.lineage.modw;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import javax.swing.*;
import javax.vecmath.Vector3d;

import org.jdom.Element;

import endrov.basicWindow.BasicWindow;
import endrov.basicWindow.EvColor;
import endrov.basicWindow.EvColor.ColorMenuListener;
import endrov.data.EvObject;
import endrov.data.EvPath;
import endrov.data.EvSelection;
import endrov.ev.*;
import endrov.lineage.Lineage;
import endrov.lineage.LineageCommonUI;
import endrov.lineage.LineageExp;
import endrov.lineage.LineageSelParticle;
import endrov.lineage.Lineage.InterpolatedParticle;
import endrov.lineage.util.LineageVoronoi;
import endrov.mesh3d.Mesh3D;
import endrov.mesh3d.Mesh3dModelExtension;
import endrov.modelWindow.*;
import endrov.undo.UndoOpBasic;
import endrov.util.*;


/**
 * Lineage extension to Model Window
 * @author Johan Henriksson
 */
public class LineageModelExtension implements ModelWindowExtension
	{
  private static int NUC_SHOW_DIV=15;
  private static int NUC_SELECT_DIV=6;
  private static int NUC_HIDE_DIV=6;

  public void newModelWindow(ModelWindow w)
		{
		w.modelWindowHooks.add(new NucModelWindowHook(w));
		}
	
	static class NucModelWindowHook implements ModelWindowHook, ActionListener, ModelView.GLSelectListener
		{
		private final HashMap<Integer,LineageSelParticle> selectColorMap=new HashMap<Integer,LineageSelParticle>();
		//private final HashMap<LineageSelParticle, Integer> selectColorMap2=new HashMap<LineageSelParticle, Integer>();
		private Vector<Map<LineageSelParticle, Lineage.InterpolatedParticle>> interpNuc=new Vector<Map<LineageSelParticle, Lineage.InterpolatedParticle>>();
		final ModelWindow w;
		public void fillModelWindowMenus()
			{
			w.sidePanelItems.add(bAddExpPattern);
			for(ModwPanelExpPattern ti:expsettings)
				w.sidePanelItems.add(ti);

			}
		
		public double nucMagnification=1;
		
		public JMenu mShowNames=new JMenu("Show names");
		public JRadioButtonMenuItem miShowNamesNone=new JRadioButtonMenuItem("None",true);
		public JRadioButtonMenuItem miShowNamesSelected=new JRadioButtonMenuItem("Selected");
		public JRadioButtonMenuItem miShowNamesAll=new JRadioButtonMenuItem("All");
		public ButtonGroup mShowNamesGroup=new ButtonGroup();
		
		public JMenu mShowExp=new JMenu("Show expression as");
		public JRadioButtonMenuItem miShowExpColorMod=new JRadioButtonMenuItem("Colored particles");
		public JRadioButtonMenuItem miShowExpColorAND=new JRadioButtonMenuItem("Colored particles AND");
		public JRadioButtonMenuItem miShowExpMarkerColor=new JRadioButtonMenuItem("Colored markers",true);
		public JRadioButtonMenuItem miShowExpMarkerSize=new JRadioButtonMenuItem("Marker size",true);
		public ButtonGroup mShowExpGroup=new ButtonGroup();
		
		public JMenuItem miShowSelectedNuc=new JMenuItem("Nuclei: Unhide selected"); 
		public JMenuItem miHideSelectedNuc=new JMenuItem("Nuclei: Hide selected"); 

		public JMenu mShowTrace=new JMenu("Show traces");
		public JRadioButtonMenuItem miShowTraceNone=new JRadioButtonMenuItem("None",true); 
		public JRadioButtonMenuItem miShowTraceSel=new JRadioButtonMenuItem("Selected"); 
		public JRadioButtonMenuItem miShowTraceAll=new JRadioButtonMenuItem("All"); 
		public ButtonGroup mShowTraceGroup=new ButtonGroup();
		
		public JCheckBoxMenuItem miShowSimpleTraces=new JCheckBoxMenuItem("Straight traces"); 

		private float traceWidth=3;
		
		public JMenuItem miSetTraceWidth=new JMenuItem("Set trace width");
		
		public JMenu mShowNucSize=new JMenu("Nuclei size");
		
		public JMenuItem miShowNucSize0=new JMenuItem("0%");
		public JMenuItem miShowNucSize25=new JMenuItem("25%");
		public JMenuItem miShowNucSize50=new JMenuItem("50%");
		public JMenuItem miShowNucSize75=new JMenuItem("75%");
		public JMenuItem miShowNucSize100=new JMenuItem("100%");
		public JMenuItem miShowNucSizeCustom=new JMenuItem("Custom");

		public JMenuItem miSelectVisible=new JMenuItem("Select visible particles"); 


		/**
		 * How to show expression patterns
		 */
		public static enum ShowExp {CellColor, MarkerColor, MarkerSize, CellColorAND};
		public ShowExp showExpAs=ShowExp.MarkerColor;

		/**
		 * Color of the movement trace of particles
		 */
		public EvColor traceColor=null;

		public JCheckBoxMenuItem miShowDiv=new JCheckBoxMenuItem("Show division lines", true); 

		public JCheckBoxMenuItem miShowDelaunay=new JCheckBoxMenuItem("Show delaunay neighbours", false);
		
		
		Vector<ModwPanelExpPattern> expsettings=new Vector<ModwPanelExpPattern>();
		private JButton bAddExpPattern=new JButton("Add exp.pattern");

		//For modifying particles
		private Lineage.Particle currentOrigNuc=null;
		private LineageSelParticle currentModifying=null;
		private boolean hasReallyModified=false;
		private ModState modifyingState=null;
		
		private enum ModState
			{
			Dragging, Resizing
			}
		
		/**
		 * Currently modified particles is finalized. Commit changes.
		 * 
		 * Only for dragging at the moment
		 */
		public void commitModifyingNuc()
			{
			
			//Only commit if something has changed
			if(hasReallyModified)
				{
				hasReallyModified=false;
				final Lineage lin=currentModifying.fst();
				final String name=currentModifying.snd();
				final Lineage.Particle currentNuc=currentModifying.getParticle().clone();
				final Lineage.Particle lastNuc=currentOrigNuc; 
		
				new UndoOpBasic("Modify keyframe for "+currentModifying.snd())
					{
					public void redo()
						{
						lin.particle.put(name, currentNuc);
						BasicWindow.updateWindows();
						}
		
					public void undo()
						{
						lin.particle.put(name, lastNuc);
						BasicWindow.updateWindows();
						}
					}.execute();
				}

			hasReallyModified=false;
			currentModifying=null;
			currentOrigNuc=null;
			modifyingState=null;
			}
		
		private void setTraceColor(EvColor c)
			{
			traceColor=c;
			}
		
		public NucModelWindowHook(final ModelWindow w)
			{
			this.w=w;
			
			JMenu miNuc=new JMenu("Lineage");

			JMenu mTraceColor=new JMenu("Set trace color");
			JMenuItem miColorSame=new JMenuItem("Same color as particle");
			mTraceColor.add(miColorSame);
			miColorSame.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){setTraceColor(null);}});
			EvColor.addColorMenuEntries(mTraceColor, new ColorMenuListener(){
				public void setColor(EvColor c){setTraceColor(c);}
			});
			
			mShowTrace.add(miShowTraceNone);
			mShowTrace.add(miShowTraceSel);
			mShowTrace.add(miShowTraceAll);
			mShowTraceGroup.add(miShowTraceNone);
			mShowTraceGroup.add(miShowTraceSel);
			mShowTraceGroup.add(miShowTraceAll);
			
			mShowNames.add(miShowNamesNone);
			mShowNames.add(miShowNamesSelected);
			mShowNames.add(miShowNamesAll);
			mShowNamesGroup.add(miShowNamesNone);
			mShowNamesGroup.add(miShowNamesSelected);
			mShowNamesGroup.add(miShowNamesAll);

			mShowExp.add(miShowExpColorMod);
			mShowExp.add(miShowExpColorAND);
			mShowExp.add(miShowExpMarkerColor);
			mShowExp.add(miShowExpMarkerSize);
			mShowExpGroup.add(miShowExpColorMod);
			mShowExpGroup.add(miShowExpColorAND);
			mShowExpGroup.add(miShowExpMarkerColor);
			mShowExpGroup.add(miShowExpMarkerSize);
			
			mShowNucSize.add(miShowNucSize0);
			mShowNucSize.add(miShowNucSize25);
			mShowNucSize.add(miShowNucSize50);
			mShowNucSize.add(miShowNucSize75);
			mShowNucSize.add(miShowNucSize100);
			mShowNucSize.add(miShowNucSizeCustom);


			//miNuc.add(NucCommonUI.makeSetColorMenu());
			miNuc.add(mShowNames);
			miNuc.add(mShowExp);
			miNuc.add(mShowNucSize);
			miNuc.add(mShowTrace);
			miNuc.add(mTraceColor);
			miNuc.add(miSetTraceWidth);
			miNuc.add(miShowSimpleTraces);
			miNuc.add(miShowSelectedNuc);
			miNuc.add(miHideSelectedNuc);
			
			
			
			miNuc.add(miShowDiv);
			miNuc.add(miShowDelaunay);
			
			miNuc.add(miSelectVisible);
			
			miNuc.addSeparator();
			new LineageCommonUI(w, w).addToMenu(miNuc, false);

			
			w.menuModel.add(miNuc);

			
			
			miShowNamesNone.addActionListener(this);
			miShowNamesSelected.addActionListener(this);
			miShowNamesAll.addActionListener(this);
			miShowExpColorMod.addActionListener(this);
			miShowExpColorAND.addActionListener(this);
			miShowExpMarkerColor.addActionListener(this);
			miShowExpMarkerSize.addActionListener(this);
			miShowSelectedNuc.addActionListener(this);
			miHideSelectedNuc.addActionListener(this);
			miShowTraceSel.addActionListener(this);
			miShowTraceAll.addActionListener(this);
			miShowDiv.addActionListener(this);
			miShowDelaunay.addActionListener(this);
			miShowNucSize0.addActionListener(this);
			miShowNucSize25.addActionListener(this);
			miShowNucSize50.addActionListener(this);
			miShowNucSize75.addActionListener(this);
			miShowNucSize100.addActionListener(this);
			miShowNucSizeCustom.addActionListener(this);
			miSetTraceWidth.addActionListener(this);

			miSelectVisible.addActionListener(this);
			bAddExpPattern.addActionListener(this);

			
			w.addModelWindowMouseListener(new ModelWindowMouseListener(){
				public void mouseClicked(MouseEvent e)
					{
					//Left-clicking a particle selects it
					if(SwingUtilities.isLeftMouseButton(e))
						LineageCommonUI.mouseSelectParticle(LineageCommonUI.currentHover, (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK)!=0);
					else if(SwingUtilities.isRightMouseButton(e))
						{
						if(LineageCommonUI.currentHover!=LineageCommonUI.emptyHover)
							{
							//Popup menu
							JPopupMenu menu=new JPopupMenu();
							new LineageCommonUI(w, w).addToMenu(menu, false);
							w.createPopupMenu(menu, e);
							}
						}
					}
				
				
				public boolean mouseDragged(MouseEvent e, int dx, int dy)
					{
					if(modifyingState==ModState.Dragging || modifyingState==ModState.Resizing)
						{
						hasReallyModified=true;
						
						//Get nuc coordinate
						EvDecimal curFrame=w.getFrame();
						Lineage.Particle nuc=currentModifying.getParticle();
						Lineage.InterpolatedParticle interp=nuc.interpolatePos(curFrame);
						
						if(interp!=null)
							{
							if(modifyingState==ModState.Dragging)
								{
								//Find movement vector
								Vector3d moveVecWorld=w.view.getMouseMoveVector(dx, dy, interp.pos.getPosCopy());
								
								//Move particle
								Lineage.ParticlePos nucPos=interp.pos.clone();
								nucPos.x+=moveVecWorld.x;
								nucPos.y+=moveVecWorld.y;
								nucPos.z+=moveVecWorld.z;
								nuc.pos.put(curFrame, nucPos);
								
								return true;
								}
							else if(modifyingState==ModState.Resizing)
								{
								//Resize particle
								Lineage.ParticlePos nucPos=interp.pos.clone();
								nucPos.r*=Math.exp(dy*0.01);
								nuc.pos.put(curFrame, nucPos);
								
								return true;
								}
							}
						return false;
						}
					else
						return false;
					}
				public void mouseEntered(MouseEvent e){}
				public void mouseExited(MouseEvent e){}
				public void mouseMoved(MouseEvent e){}
				
				
				public void mousePressed(MouseEvent e)
					{
					if(LineageCommonUI.currentHover!=LineageCommonUI.emptyHover && LineageCommonUI.currentHover!=null)
						{
						if(SwingUtilities.isLeftMouseButton(e))
							{
							//Start dragging
							currentModifying=LineageCommonUI.currentHover;
							currentOrigNuc=currentModifying.getParticle().clone();
							hasReallyModified=false;
							modifyingState=ModState.Dragging;
							}
						else if(SwingUtilities.isRightMouseButton(e))
							{
							//Start resizing
							currentModifying=LineageCommonUI.currentHover;
							currentOrigNuc=currentModifying.getParticle().clone();
							hasReallyModified=false;
							modifyingState=ModState.Resizing;
							}

						}
					}
				public void mouseReleased(MouseEvent e)
					{
					commitModifyingNuc();
					}
			});
			}
		
		public void readPersonalConfig(Element e){}
		public void savePersonalConfig(Element e){}
		
		private Collection<String> getAllExpPatterns()
			{
			//Update list of expression patterns
			Set<String> v=new TreeSet<String>();
			for(Lineage lin:Lineage.getParticles(w.getSelectedData()))
				v.addAll(lin.getAllExpNames());
			return v;
			}
		
		public void datachangedEvent()
			{
			//Update list of expression patterns
			Collection<String> v=getAllExpPatterns();
			for(ModwPanelExpPattern panel:expsettings)
				panel.setAvailableExpressions(v);
			}
		
		public void actionPerformed(ActionEvent e)
			{
			if(e.getSource()==miShowSelectedNuc)
				{
				for(endrov.lineage.LineageSelParticle p:LineageCommonUI.getSelectedParticles())
					LineageCommonUI.hiddenParticles.remove(p);
				}
			else if(e.getSource()==miHideSelectedNuc)
				{
				for(endrov.lineage.LineageSelParticle p:LineageCommonUI.getSelectedParticles())
					LineageCommonUI.hiddenParticles.add(p);
				}
			else if(e.getSource()==miShowNucSize0)
				nucMagnification=0;
			else if(e.getSource()==miShowNucSize25)
				nucMagnification=0.25;
			else if(e.getSource()==miShowNucSize50)
				nucMagnification=0.5;
			else if(e.getSource()==miShowNucSize75)
				nucMagnification=0.75;
			else if(e.getSource()==miShowNucSize100)
				nucMagnification=1;
			else if(e.getSource()==miShowNucSizeCustom)
				{
				String inp=BasicWindow.showInputDialog("Enter magnification in percent", "100");
				if(inp!=null)
					nucMagnification=Double.parseDouble(inp)/100;
				}
			else if(e.getSource()==miSetTraceWidth)
				{
				String inp=BasicWindow.showInputDialog("Set trace width", ""+traceWidth);
				if(inp!=null)
					traceWidth=(float)Double.parseDouble(inp);
				}
			else if(e.getSource()==miSelectVisible)
				{
				EvDecimal frame=w.getFrame();
				//TODO replace with visible set
				for(Map.Entry<EvPath, Lineage> entry:w.getSelectedData().getIdObjectsRecursive(Lineage.class).entrySet())
					{
					for(LineageSelParticle i:entry.getValue().interpolateParticles(frame).keySet())
						EvSelection.select(i);
					BasicWindow.updateWindows();
					}
				}
			else if(e.getSource()==bAddExpPattern)
				{
				ModwPanelExpPattern p=new ModwPanelExpPattern(this);
				p.setAvailableExpressions(getAllExpPatterns());
				expsettings.add(p);
				w.updateToolPanels();
				}
			else if(e.getSource()==miShowExpColorMod)
				{
				showExpAs=ShowExp.CellColor;
				}
			else if(e.getSource()==miShowExpColorAND)
				{
				showExpAs=ShowExp.CellColorAND;
				}
			else if(e.getSource()==miShowExpMarkerColor)
				{
				showExpAs=ShowExp.MarkerColor;
				}
			else if(e.getSource()==miShowExpMarkerSize)
				{
				showExpAs=ShowExp.MarkerSize;
				}
			
			w.view.repaint(); //TODO modw repaint
			}
		
		
		public boolean canRender(EvObject ob)
			{
			return ob instanceof Lineage;
			}

		
		public Collection<Lineage> getVisibleLineages()
			{
			Vector<Lineage> v=new Vector<Lineage>();
			for(Lineage lin:Lineage.getParticles(w.getSelectedData()))
				if(w.showObject(lin))
					v.add(lin);
			return v;
			}
		
		public void initOpenGL(GL gl)
			{
			initDrawSphere(gl.getGL2());
			}

		
		/**
		 * Prepare for rendering
		 */
		public void displayInit(GL gl)
			{
			selectColorMap.clear();
			//selectColorMap2.clear();

			interpNuc.clear();
			for(Lineage lin:getVisibleLineages())
				interpNuc.add(lin.interpolateParticles(w.getFrame()));
			}
		
		/*
		private int getSelectColor(LineageSelParticle sel)
			{
			Integer selectColor=selectColorMap2.get(sel);
			if(selectColor==null)
				{
				selectColor=w.view.reserveSelectColor(this);
				selectColorMap.put(selectColor, sel);
				//selectColorMap2.put(sel, selectColor);
				}
			
			
			selectColorMap.put(selectColor, sel);
			w.view.setReserveColor(gl, selectColor);
			}*/
		
		/**
		 * Render for selection
		 */
		public void displaySelect(GL gl)
			{
			EvDecimal curFrame=w.getFrame();
			
			//boolean showSmallNuc=miShowSmallNuclei.isSelected();
			if(EV.debugMode)
				System.out.println("#nuc to render: "+interpNuc.size());
			for(Map<LineageSelParticle, Lineage.InterpolatedParticle> inter:interpNuc)
				for(Map.Entry<LineageSelParticle, Lineage.InterpolatedParticle> entry:inter.entrySet())
					if(entry.getValue().isVisible())
						{
						//Reserve color
						int selectColor=w.view.reserveSelectColor(this);
						selectColorMap.put(selectColor, entry.getKey());
						w.view.setReserveColor(gl, selectColor);
						
						//Render
						renderParticleSelection(gl,entry.getKey(), entry.getValue(), nucMagnification);

						}
			
			//Meshs
			for(Lineage lin:getVisibleLineages())
				for(String name:lin.particle.keySet())
					{
					Lineage.Particle p=lin.particle.get(name);
					Mesh3D mesh=p.meshs.get(curFrame);
					if(mesh!=null)
						{
						//Reserve select color
						LineageSelParticle sel=new LineageSelParticle(lin, name);
						int selectColor=w.view.reserveSelectColor(this);
						selectColorMap.put(selectColor, sel);
						w.view.setReserveColor(gl, selectColor);
						
						//Render mesh
						Mesh3dModelExtension.displayMeshSelect(w.view, gl, mesh, selectColor);
						}
					
					
					
					}
				
				
			
			}
		
		/**
		 * Render movement trace of nuc
		 */
		private void renderTrace(GL2 gl, Lineage.Particle nuc, boolean simple, Color col)
			{
			if(!nuc.pos.isEmpty())
				{
				gl.glLineWidth(traceWidth);
				
				Color thisTraceColor=getTraceColor(nuc);
				float colR=(float)thisTraceColor.getRed()/255.0f;
				float colG=(float)thisTraceColor.getGreen()/255.0f;
				float colB=(float)thisTraceColor.getBlue()/255.0f;
				gl.glColor3d(colR,colG,colB);
//				gl.glColor3d(1, 1, 1);
				if(simple)
					{
					gl.glBegin(GL2.GL_LINE_STRIP);
					EvDecimal f1=nuc.pos.firstKey();
					EvDecimal f2=nuc.pos.lastKey();
					Lineage.ParticlePos pos1=nuc.pos.get(f1);
					Lineage.ParticlePos pos2=nuc.pos.get(f2);
					Vector3d v=new Vector3d(pos2.x-pos1.x,pos2.y-pos1.y,pos2.z-pos1.z);
					double len=v.length();
					v.scale((len-w.view.getArrowLength())/len);
					//v.normalize();
					gl.glVertex3d(pos1.x,pos1.y,pos1.z);
//					gl.glVertex3d(pos2.x,pos2.y,pos2.z);
					gl.glVertex3d(pos1.x+v.x,pos1.y+v.y,pos1.z+v.z);
					gl.glEnd();
					
					Vector3d direction=pos2.getPosCopy();
					direction.sub(pos1.getPosCopy());
		      gl.glEnable(GL2.GL_LIGHTING);
					w.view.renderArrowHead(gl, pos2.getPosCopy(), direction, colR, colG, colB);
		      gl.glDisable(GL2.GL_LIGHTING);
					}
				else
					{
					gl.glBegin(GL2.GL_LINE_STRIP);
					//Vector3d last=null, secondLast=null;
					for(Lineage.ParticlePos pos:nuc.pos.values())
						{
						gl.glVertex3d(pos.x,pos.y,pos.z);
						/*secondLast=last;
						last=pos.getPosCopy();*/
						}
					gl.glEnd();
					/*
					if(secondLast!=null)
						{
						Vector3d direction=new Vector3d(last);
						direction.sub(secondLast);
						w.view.renderArrowHead(gl, last, direction);
						}
						*/
					}
				}
			}
		
		private Color colorForNuc(Lineage.Particle nuc)
			{
			Color col=null;
			if(traceColor!=null)
				col=traceColor.c;
			if(col==null)
				col=Lineage.representativeColor(nuc.color);
			return col;
			}
		
		public Color getTraceColor(Lineage.Particle nuc)
			{
			if(traceColor!=null)
				return traceColor.getAWTColor();
			else
				return colorForNuc(nuc);
			}
		
		
		/**
		 * Render graphics
		 */
		public void displayFinal(GL glin,List<TransparentRender> transparentRenderers)
			{
			GL2 gl=glin.getGL2();

			//initDrawSphere(gl);
			gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
			
			EvDecimal curFrame=w.getFrame();

			boolean traceCur=miShowTraceAll.isSelected();
			boolean traceSel=miShowTraceSel.isSelected();
			boolean tracesSimple=miShowSimpleTraces.isSelected();
			
		
			//Set suitable scaling for expression patterns
			for(ModwPanelExpPattern panel:expsettings)
				if(panel.scale1==null)
					ModwPanelExpPattern.adjustExpPatternScale(w, panel);
			
			//Update list of expression patterns
			Set<String> v=new TreeSet<String>();
			for(Lineage lin:Lineage.getParticles(w.getSelectedData()))
				v.addAll(lin.getAllExpNames());
			for(ModwPanelExpPattern panel:expsettings)
				panel.setAvailableExpressions(v);

			//Render each lineage
			for(Map<LineageSelParticle, Lineage.InterpolatedParticle> inter:interpNuc)
				{
				//Draw neighbours. Need be calculated in the background and cached
				if(miShowDelaunay.isSelected())
					{
					try
						{
						///////////////////////////// TODO TODO TODO  BAD
						double r=3000; //300 is about the embryo. embryo is not centered in reality.

						//						r=600; //TODO consistent voronoi calc

						Map<LineageSelParticle, Lineage.InterpolatedParticle> interX=new HashMap<LineageSelParticle, InterpolatedParticle>(inter);

						Lineage.InterpolatedParticle i1=new Lineage.InterpolatedParticle();
						i1.pos=new Lineage.ParticlePos();
						i1.frameBefore=EvDecimal.ZERO;
						i1.pos.x=r;
						Lineage.InterpolatedParticle i2=new Lineage.InterpolatedParticle();
						i2.pos=new Lineage.ParticlePos();
						i2.frameBefore=EvDecimal.ZERO;
						i2.pos.x=-r;
						Lineage.InterpolatedParticle i3=new Lineage.InterpolatedParticle();
						i3.pos=new Lineage.ParticlePos();
						i3.frameBefore=EvDecimal.ZERO;
						i3.pos.y=-r;
						Lineage.InterpolatedParticle i4=new Lineage.InterpolatedParticle();
						i4.pos=new Lineage.ParticlePos();
						i4.frameBefore=EvDecimal.ZERO;
						i4.pos.y=-r;

						interX.put(new LineageSelParticle(null,":::1"), i1);
						interX.put(new LineageSelParticle(null,":::2"), i2);
						interX.put(new LineageSelParticle(null,":::3"), i3);
						interX.put(new LineageSelParticle(null,":::4"), i4);
						///////////////////////////// TODO TODO TODO  BAD


						LineageVoronoi nvor=new LineageVoronoi(interX,false);

						/*
						for(int[] facelist:nvor.vor.vface)
	//					int size=nvor.nucnames.size();
		//				for(int i=0;i<size;i++)
							{
							boolean isfinite=true;
							for(int i:facelist)
								if(i==-1)
									isfinite=false;
							if(isfinite)
								{
								gl.glBegin(GL2.GL_LINE_LOOP);
								gl.glColor3d(1, 0, 0);
								for(int i:facelist)
									{
									Vector3d v=nvor.vor.vvert.get(i);
									System.out.println(""+i+" "+v);
									gl.glVertex3d(v.x, v.y, v.z);
									}
								gl.glEnd();
								}
							}
						 */

						//Lines between neighbours
						//TODO The ::: is really ugly
						if(miShowDelaunay.isSelected())
							{
							gl.glBegin(GL2.GL_LINES);
							gl.glColor3d(1, 0, 0);
							int size=nvor.nucnames.size();
							for(int i=0;i<size;i++)
								if(!nvor.nucnames.get(i).startsWith(":::"))
									{
									Vector3d from=nvor.nmid.get(i);
									for(int j:nvor.vneigh.dneigh.get(i))
										if(j>i && j!=-1)
											if(!nvor.nucnames.get(j).startsWith(":::"))
												{
												Vector3d to=nvor.nmid.get(j);
												gl.glVertex3d(from.x, from.y, from.z);
												gl.glVertex3d(to.x, to.y, to.z);
												}
									}
							gl.glEnd();
							}

						}
					catch (Exception e)
						{
						e.printStackTrace();
						}
					}

				
				//Render all particles
				for(LineageSelParticle nucPair:inter.keySet())
					{
					//Render particle body
					renderParticle(gl, nucPair, inter.get(nucPair), curFrame);
					
					if(traceCur && !traceSel && inter.get(nucPair).isVisible())
						{
						Color col=colorForNuc(nucPair.getParticle());
						renderTrace(gl,nucPair.getParticle(), tracesSimple, col);
						}
					
					//Draw connecting line
					if(nucPair.snd().equals(Lineage.connectNuc[0]))
						for(LineageSelParticle nucPair2:inter.keySet())
							if(nucPair2.snd().equals(Lineage.connectNuc[1]))
								{
								InterpolatedParticle n=inter.get(nucPair);
								InterpolatedParticle m=inter.get(nucPair2);
								gl.glBegin(GL2.GL_LINES);
								gl.glColor3d(1, 1, 1);
								gl.glVertex3d(n.pos.x,n.pos.y,n.pos.z);
								gl.glVertex3d(m.pos.x,m.pos.y,m.pos.z);
								gl.glEnd();
								}
					}
			
				//Render text etc
				gl.glDisable(GL2.GL_LIGHTING);
				for(LineageSelParticle nucPair:inter.keySet())
					renderParticleOverlay(gl,transparentRenderers,nucPair, inter.get(nucPair), curFrame);
				
				/*
				//Render meshs
				for(LineageSelParticle s:inter.keySet())
					{
					Lineage.Particle p=s.getParticle();
					Mesh3D mesh=p.meshs.get(curFrame);
					
					if(mesh!=null)
						{
						Mesh3dModelExtension.displayMeshFinal(w.view, gl, mesh);
						}
					
					}
*/
				}
			
			

			//Meshs
			for(Lineage lin:getVisibleLineages())
				{
				for(String name:lin.particle.keySet())
					{
					Lineage.Particle p=lin.particle.get(name);

					Mesh3D mesh=p.meshs.get(curFrame);
					if(mesh!=null)
						{
						//Reserve select color
						LineageSelParticle sel=new LineageSelParticle(lin, name);
						int selectColor=w.view.reserveSelectColor(this);
						selectColorMap.put(selectColor, sel);
						w.view.setReserveColor(gl, selectColor);
						
						//Render mesh
						Mesh3dModelExtension.displayMeshFinal(w.view, gl, mesh);
						}
					}
				}
			
			
			
			if(traceSel)
				for(LineageSelParticle pair:LineageCommonUI.getSelectedParticles())
					{
					Lineage.Particle nuc=pair.getParticle();
					renderTrace(gl,nuc, tracesSimple, colorForNuc(pair.getParticle()));
					}
			
			//Cell divisions
			if(miShowDiv.isSelected())
				{
				gl.glLineWidth(3);
				for(Lineage lin:getVisibleLineages())
					{
					for(Lineage.Particle nuc:lin.particle.values())
						if(!nuc.pos.isEmpty() && !nuc.parents.isEmpty())
							{
							EvDecimal tframe=nuc.getFirstFrame();

							for(String parentName:nuc.parents)
								{
								Lineage.Particle pnuc=lin.particle.get(parentName);
								if(!pnuc.pos.isEmpty())
									{
									EvDecimal pframe=pnuc.pos.lastKey();
									if(curFrame.greaterEqual(pframe) && curFrame.lessEqual(tframe))
										{
										Lineage.ParticlePos npos=nuc.pos.get(tframe);
										Lineage.ParticlePos ppos=pnuc.pos.get(pframe);
		
										gl.glBegin(GL2.GL_LINES);
										gl.glColor3d(1, 1, 0);
										gl.glVertex3d(npos.x,npos.y,npos.z);
										gl.glVertex3d(ppos.x,ppos.y,ppos.z);
										gl.glEnd();
										}
									}
								}
							
							
							}
					}
				gl.glLineWidth(1);
				}
			gl.glPopAttrib();
			}

		
		/** Keep track of what hover was before hover test started */
		private LineageSelParticle lastHover=null;
		
		/** 
		 * Called when hover test starts
		 */
		public void hoverInit(int pixelid)
			{
			//Update hover
			lastHover=LineageCommonUI.currentHover;
			LineageCommonUI.currentHover=LineageCommonUI.emptyHover;
			}
		/**
		 * Called when particle hovered 
		 */
		public void hover(int pixelid)
			{
			LineageCommonUI.currentHover=selectColorMap.get(pixelid);
			if(!LineageCommonUI.currentHover.equals(lastHover))
				{
				System.out.println("nuc rerend");
				BasicWindow.updateWindows(w);
				}
			}

		

		
		private double clamp0(double x)
			{
			if(x<0)
				return 0;
			else
				return x;
			}
		
		private double clamp1(double x)
			{
			if(x>1)
				return 1;
			else
				return x;
			}
		
		/**
		 * Render body of one particle
		 */
		private void renderParticle(GL2 gl, LineageSelParticle nucPair, Lineage.InterpolatedParticle nucInterpol, EvDecimal curFrame)
			{
			//Visibility rule
			if(!nucInterpol.isVisible())
				return;

			
			gl.glEnable(GL2.GL_CULL_FACE);

			//Save world coordinate
	    gl.glPushMatrix();
	    
			//Move to cell center = local coordinate
	    gl.glTranslated(nucInterpol.pos.x,nucInterpol.pos.y,nucInterpol.pos.z);

	    
    	//System.out.println("bla "+nuc.pos.x+ nuc.pos.y+ nuc.pos.z+ " "+nuc.pos.r);
	    
	    double showRadius=nucInterpol.pos.r*nucMagnification;

	    //Decide color based on if the particle is selected
	    Color repColor=Lineage.representativeColor(nucInterpol.colorNuc);
			float nucColor[];
			if(!expsettings.isEmpty() && (showExpAs==ShowExp.CellColor || showExpAs==ShowExp.CellColorAND))
				{
				//Add color from expression patterns
				if(showExpAs==ShowExp.CellColor)
					{
					nucColor=new float[]{0.1f,0.1f,0.1f};
					for(ModwPanelExpPattern panel:expsettings)
		    		{
		    		String expName=panel.getSelectedExp();
		    		LineageExp n=nucPair.getParticle().exp.get(expName);
		    		if(n!=null && !n.level.isEmpty())
		    			{
		    			double level=n.interpolateLevel(curFrame);
		    			double scale=panel.scale1;
		    			double add=panel.add1;

		    			nucColor[0]+=(float)panel.colR*(clamp0(level*scale+add));
		    			nucColor[1]+=(float)panel.colG*(clamp0(level*scale+add));
		    			nucColor[2]+=(float)panel.colB*(clamp0(level*scale+add));

		    			//System.out.println("here"+level+" ");
		    			}
		    		else
		    			{
		    			System.out.println("no exp like \""+expName+"\"");
		    			}
		    		}
					
					}
				else //if(showExpAs==ShowExp.CellColorAND)
					{
					double totLevel=1;
					
					for(ModwPanelExpPattern panel:expsettings)
		    		{
		    		String expName=panel.getSelectedExp();
		    		LineageExp n=nucPair.getParticle().exp.get(expName);
		    		if(n!=null && !n.level.isEmpty())
		    			{
		    			double level=n.interpolateLevel(curFrame);
		    			double scale=panel.scale1;
		    			double add=panel.add1;

		    			totLevel*=clamp0(level*scale+add);
		    			/*
		    			nucColor[0]*=(float)panel.colR*(clamp0(level*scale+add));
		    			nucColor[1]*=(float)panel.colG*(clamp0(level*scale+add));
		    			nucColor[2]*=(float)panel.colB*(clamp0(level*scale+add));*/

		    			//System.out.println("here"+level+" ");
		    			}
		    		else
		    			{
		    			System.out.println("no exp like \""+expName+"\"");
		    			}
		    		}
					//Use color from first expression
					nucColor=new float[]{
							(float)(totLevel*expsettings.get(0).colR),
							(float)(totLevel*expsettings.get(0).colG),
							(float)(totLevel*expsettings.get(0).colB)
							};					
					}
				
				
	    	
	    	for(int i=0;i<3;i++)
	    		if(nucColor[i]>1f)
	    			nucColor[i]=1f;
				}
			else if(nucInterpol.colorNuc!=null)
	    	nucColor=new float[]{repColor.getRed()/255.0f,repColor.getGreen()/255.0f,repColor.getBlue()/255.0f};
	    else
	    	nucColor=new float[]{1,1,1};
			
    	
			
//			float lightAmbient[] = { nucColor[0]*0.3f, nucColor[1]*0.3f, nucColor[2]*0.3f, 0.0f };
//    gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, nucColor, 0);   
	    	

			
	    if(LineageCommonUI.hiddenParticles.contains(nucPair))
	    	{
		    if(EvSelection.isSelected(nucPair))
		    	nucColor=new float[]{1,0,1};
		    
	    	//Hidden cell
	    	gl.glColor3d(nucColor[0], nucColor[1], nucColor[2]);
	    	drawHiddenSphere(gl, showRadius);
	    	}
	    else
	    	{
	    	//Visible cell
	    	drawVisibleSphere(gl, showRadius, EvSelection.isSelected(nucPair),nucColor[0], nucColor[1], nucColor[2]);
	    	}
	    
	    //Go back to world coordinates
	    gl.glPopMatrix();
			}


		

		private void invokeDrawListHidden(GL2 gl)
			{
			//gl.glCallList(displayListHiddenSphere);
			GLU glu=new GLU();
			GLUquadric q=glu.gluNewQuadric();
			glu.gluQuadricDrawStyle(q, GLU.GLU_LINE);
			glu.gluSphere(q,1.0,NUC_HIDE_DIV,NUC_HIDE_DIV);
			glu.gluDeleteQuadric(q);
			}
		
		private void invokeDrawListVisible(GL2 gl)
			{
//  	gl.glCallList(displayListVisibleSphere);
			GLU glu=new GLU();
			GLUquadric q=glu.gluNewQuadric();
			glu.gluSphere(q,1.0,NUC_SHOW_DIV,NUC_SHOW_DIV);
			glu.gluDeleteQuadric(q);
			}

		private void invokeDrawListSelect(GL2 gl)
			{
    	//gl.glCallList(displayListSelectSphere);
			//select sphere
			GLU glu=new GLU();
			GLUquadric q=glu.gluNewQuadric();
			glu.gluSphere(q,1.0,NUC_SELECT_DIV,NUC_SELECT_DIV);
			glu.gluDeleteQuadric(q);
  		}
		
		/**
		 * Generate vertex lists for spheres
		 */
		private void initDrawSphere(GL2 gl)
			{
			/*
			GLU glu=new GLU();
			GLUquadric q=glu.gluNewQuadric();

			displayListVisibleSphere = gl.glGenLists(1);
			gl.glNewList(displayListVisibleSphere, GL2.GL_COMPILE);
			glu.gluSphere(q,1.0,NUC_SHOW_DIV,NUC_SHOW_DIV);
			//drawSphereSolid(gl, 1.0, NUC_SHOW_DIV,NUC_SHOW_DIV);
			gl.glEndList();

			displayListSelectSphere = gl.glGenLists(1);
			gl.glNewList(displayListSelectSphere, GL2.GL_COMPILE);
			glu.gluSphere(q,1.0,NUC_SELECT_DIV,NUC_SELECT_DIV);
			gl.glEndList();

			glu.gluQuadricDrawStyle(q, GLU.GLU_LINE);

			displayListHiddenSphere = gl.glGenLists(1);
			gl.glNewList(displayListHiddenSphere, GL2.GL_COMPILE);
			glu.gluSphere(q,1.0,NUC_HIDE_DIV,NUC_HIDE_DIV);
			gl.glEndList();

			glu.gluDeleteQuadric(q);
			*/
			}

/*		private int displayListVisibleSphere;
		private int displayListHiddenSphere;
		private int displayListSelectSphere;*/
		
		
		private void drawVisibleSphere(GL2 gl, double r, boolean selected, float colR, float colG, float colB)
			{
    	//double ir=1.0/r;
    	gl.glPushMatrix();
			gl.glScaled(r,r,r);
			
			if(selected)
				{
	    	gl.glColor3d(1,0,1);
				gl.glLineWidth(5);
				gl.glPolygonMode(GL2.GL_BACK, GL2.GL_LINE);
				gl.glCullFace(GL2.GL_FRONT);
				gl.glDepthFunc(GL2.GL_LEQUAL);
				invokeDrawListVisible(gl);
	    	
				gl.glCullFace(GL2.GL_BACK);
				gl.glDepthFunc(GL2.GL_LESS);
				gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
				gl.glLineWidth(1);
				}
			
      gl.glEnable(GL2.GL_LIGHTING);
    	gl.glColor3d(1,1,1);
    	gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, new float[]{colR,colG,colB}, 0);
			invokeDrawListVisible(gl);
    		
    	
      gl.glDisable(GL2.GL_LIGHTING);

    	//gl.glScaled(ir,ir,ir);
    	gl.glPopMatrix();
			}
		private void drawHiddenSphere(GL2 gl, double r)
			{
    	double ir=1.0/r;
			gl.glScaled(r,r,r);
			invokeDrawListHidden(gl);
    	gl.glScaled(ir,ir,ir);
			}
		
		public void drawSelectSphere(GL2 gl, double r)
			{
    	double ir=1.0/r;
			gl.glScaled(r,r,r);
			invokeDrawListSelect(gl);
    	gl.glScaled(ir,ir,ir);
			}
		
		
		/**
		 * Render overlay of a particle: label and expression
		 */
		private void renderParticleOverlay(GL2 gl, List<TransparentRender> transparentRenderers, LineageSelParticle nucPair, Lineage.InterpolatedParticle nuc, EvDecimal curFrame)
			{
			//Visibility rule. TODO. needed?
			if(nuc.frameBefore==null)
				return;

			//Save world coordinate
	    gl.glPushMatrix();
	    
			//Move to cell center = local coordinate
	    gl.glTranslated(nuc.pos.x,nuc.pos.y,nuc.pos.z);
			
      gl.glScalef(-1,-1,-1); //remove later

			
      boolean shouldDrawText=LineageCommonUI.currentHover.equals(nucPair) 
  		|| miShowNamesAll.isSelected() 
  		|| (EvSelection.isSelected(nucPair) && miShowNamesSelected.isSelected());
      
      boolean shouldDrawExp=!expsettings.isEmpty() && !(showExpAs==ShowExp.CellColor || showExpAs==ShowExp.CellColorAND);

	    //Unrotate camera, then move a bit closer to the camera
	    if(shouldDrawText || shouldDrawExp)
	    	{
	    	w.view.camera.unrotateGL(gl);
	    
	    	gl.glRotated(180,   0.0, 0.0, 1.0);
	    	gl.glTranslated(0.0, 0.0, -nuc.pos.r*1.05);
	    	//it would look better if it was toward the camera *center*
	    	//also consider setting size such that it does not vary with distance
	    	//3d text at all? overlay rendering should be faster
	    	double textScaleFactor=0.005*nuc.pos.r;
	    	double displacement=-textScaleFactor*50;
	    	if(!(shouldDrawExp && shouldDrawText))
	    		displacement=0;
	    	if(shouldDrawText)
	    		{
					String nucName=nucPair.snd();
    			gl.glTranslated(0, -displacement, 0);
		    	w.view.renderString(gl, transparentRenderers, (float)textScaleFactor, nucName);
    			gl.glTranslated(0, +displacement, 0);
	    		}
	    	
	    	if(shouldDrawExp)
	    		{
    			gl.glTranslated(0, displacement, 0);
	    		
    			double s=50*textScaleFactor;

    			int numExp=expsettings.size();
    			for(int curExp=0;curExp<expsettings.size();curExp++)
  	    	//for(ModwPanelExpPattern panel:expsettings)
  	    		{
  	    		ModwPanelExpPattern panel=expsettings.get(curExp);
  	    		
  	    		String expName=panel.getSelectedExp();
  	    		LineageExp n=nucPair.getParticle().exp.get(expName);
  	    		if(n!=null && !n.level.isEmpty())
  	    			{
  	    			double level=n.interpolateLevel(curFrame);
  	    			double scale=panel.scale1;
  	    			double add=panel.add1;
  	    			
  	    			if(showExpAs==ShowExp.MarkerColor)
  	    				{
    	    			gl.glColor3f(
    	  	    			(float)clamp1(panel.colR*(clamp0(level*scale+add))),
    	  	    			(float)clamp1(panel.colG*(clamp0(level*scale+add))),
    	  	    			(float)clamp1(panel.colB*(clamp0(level*scale+add)))
      	    			);
      	    			
    	    			double part=2*s/numExp;
    			    	//gl.glColor3d(1, 0, 0);
    			    	gl.glBegin(GL2.GL_QUADS);
    			    	gl.glVertex2d(-s+curExp*part, -s);
    			    	gl.glVertex2d(-s+(curExp+1)*part, -s);
    			    	gl.glVertex2d(-s+(curExp+1)*part,  s);
    			    	gl.glVertex2d(-s+curExp*part,  s);
    			    	gl.glEnd();
  	    				}
  	    			else if(showExpAs==ShowExp.MarkerSize)
  	    				{
  	    				gl.glColor3d(panel.colR, panel.colG, panel.colB);
      	    			
    	    			double part=2*s/numExp;
    	    			double peak=2*s*(level*scale+add);
    			    	gl.glBegin(GL2.GL_TRIANGLES);
    			    	if(peak>0)
    			    		{
      			    	gl.glVertex2d(-s+curExp*part, -s);
      			    	gl.glVertex2d(-s+(curExp+1)*part, -s);
    			    		}
    			    	else
    			    		{
      			    	gl.glVertex2d(-s+(curExp+1)*part, -s);
      			    	gl.glVertex2d(-s+curExp*part, -s);
    			    		}
    			    	gl.glVertex2d(-s+(curExp+0.5)*part,  -s+peak);
    			    	gl.glEnd();
  	    				}
  	    			
  	    			
  	    			//System.out.println("here"+level+" ");
  	    			}
  	    		else
  	    			{
  	    			System.out.println("no exp like \""+expName+"\"");
  	    			}
  	    		}

  
    			
	    		}
	    	
	    	
	    	}
	    
	    //Go back to world coordinates
	    gl.glPopMatrix();
			}	
		
		
		/**
		 * Render particle in the invisible selection channel
		 */
		private void renderParticleSelection(GL glin, LineageSelParticle nucPair, Lineage.InterpolatedParticle nuc, double nucMagnification)
			{    
			GL2 gl=glin.getGL2();
			gl.glEnable(GL2.GL_CULL_FACE);
			
			//Save world coordinate && Move to cell center = local coordinate
	    gl.glPushMatrix();
	    gl.glTranslated(nuc.pos.x,nuc.pos.y,nuc.pos.z);
	  	//If visible cell
	    if(!LineageCommonUI.hiddenParticles.contains(nucPair))
	    	{
	    	double showRadius=nuc.pos.r*nucMagnification;
	    	drawSelectSphere(gl, showRadius);
	    	}
	    //Go back to world coordinates
	    gl.glPopMatrix();
			}
		

		

		/**
		 * Adjust the scale
		 */
		public Collection<Double> adjustScale()
			{
			List<Double> list=new LinkedList<Double>();
			/*
			//Meshs
			for(Lineage lin:getVisibleLineages())
				for(String name:lin.particle.keySet())
					{
					Lineage.Particle p=lin.particle.get(name);
					Mesh3D mesh=p.meshs.get(w.getFrame());
					if(mesh!=null)
						{
						Vector3d v=mesh.getVertexAverage();
						if(v!=null)
							list.add(v);
						}
					}*/
			
			//Lineages
			int count=0;
			for(Map<LineageSelParticle, Lineage.InterpolatedParticle> i:interpNuc)
				count+=i.size();
			if(count>=2)
				{
				double maxx=Double.MIN_VALUE,maxy=Double.MIN_VALUE,maxz=Double.MIN_VALUE;
				double minx=Double.MAX_VALUE,miny=Double.MAX_VALUE,minz=Double.MAX_VALUE;

				//Calculate bounds
				for(Map<LineageSelParticle, Lineage.InterpolatedParticle> inter:interpNuc)
					for(Lineage.InterpolatedParticle nuc:inter.values())
						{
						if(maxx<nuc.pos.x) maxx=nuc.pos.x;
						if(maxy<nuc.pos.y) maxy=nuc.pos.y;
						if(maxz<nuc.pos.z) maxz=nuc.pos.z;
						if(minx>nuc.pos.x) minx=nuc.pos.x;
						if(miny>nuc.pos.y) miny=nuc.pos.y;
						if(minz>nuc.pos.z) minz=nuc.pos.z;
						}
				double dx=maxx-minx;
				double dy=maxy-miny;
				double dz=maxz-minz;
				double dist=dx;
				if(dist<dy) dist=dy;
				if(dist<dz) dist=dz;
				list.add(dist);
				//return Collections.singleton((Double)dist);
				}
			//else
				//return Collections.emptySet();
			return list;
			}
		
		/**
		 * Give suitable center of all objects
		 */
		public Collection<Vector3d> autoCenterMid()
			{
			List<Vector3d> list=new LinkedList<Vector3d>();
			
			//Meshs
			for(Lineage lin:getVisibleLineages())
				for(String name:lin.particle.keySet())
					{
					Lineage.Particle p=lin.particle.get(name);
					Mesh3D mesh=p.meshs.get(w.getFrame());
					if(mesh!=null)
						{
						Vector3d v=mesh.getVertexAverage();
						if(v!=null)
							list.add(v);
						}
					}
			
			//Lineages
			double meanx=0, meany=0, meanz=0;
			int num=0;
			for(Lineage lin:getVisibleLineages())
				{
				Map<LineageSelParticle, Lineage.InterpolatedParticle> interpNuc=lin.interpolateParticles(w.getFrame());
				num+=interpNuc.size();
				for(Lineage.InterpolatedParticle nuc:interpNuc.values()) //what about non-existing ones?
					{
					meanx+=nuc.pos.x;
					meany+=nuc.pos.y;
					meanz+=nuc.pos.z;
					}
				}
			if(num!=0)
				{
				meanx/=num;
				meany/=num;
				meanz/=num;
				list.add(new Vector3d(meanx,meany,meanz));
				}
			return list;
			}
		
		
		/**
		 * Given a middle position, figure out radius required to fit objects
		 */
		public double autoCenterRadius(Vector3d mid)
			{
			//Calculate maximum radius
			double maxr=0;
			for(Lineage lin:getVisibleLineages())
				{
				Map<LineageSelParticle, Lineage.InterpolatedParticle> interpNuc=lin.interpolateParticles(w.getFrame());
				for(Lineage.InterpolatedParticle nuc:interpNuc.values())
					{
					double dx=nuc.pos.x-mid.x;
					double dy=nuc.pos.y-mid.y;
					double dz=nuc.pos.z-mid.z;
					double r=Math.sqrt(dx*dx+dy*dy+dz*dz)+nuc.pos.r;
					if(maxr<r)
						maxr=r;
					}
				}
			//Find how far away the camera has to be
			return maxr;
			}
		
		
		public EvDecimal getFirstFrame()
			{
			EvDecimal first=null;
			for(Lineage lin:w.getVisibleObjects(Lineage.class))
				{
				EvDecimal f=lin.firstFrameOfLineage(true).fst();
				if(f!=null && (first==null || f.less(first)))
					first=f;
				}
			return first;
			}
		public EvDecimal getLastFrame()
			{
			EvDecimal last=null;
			for(Lineage lin:w.getVisibleObjects(Lineage.class))
				{
				EvDecimal f=lin.lastFrameOfLineage(true).fst();
				if(f!=null && (last==null || f.greater(last)))
					last=f;
				}
			return last;
			}
		
		};
		
		
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		ModelWindow.modelWindowExtensions.add(new LineageModelExtension());
		}

	}


