/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.nuc.modw;

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
import endrov.modelWindow.*;
import endrov.nuc.NucCommonUI;
import endrov.nuc.NucExp;
import endrov.nuc.NucLineage;
import endrov.nuc.NucSel;
import endrov.nuc.NucVoronoi;
import endrov.nuc.NucLineage.NucInterp;
import endrov.util.*;


/**
 * Extension to Model Window: shows nuclei
 * @author Johan Henriksson
 */
public class NucModelExtension implements ModelWindowExtension
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
		private final HashMap<Integer,NucSel> selectColorMap=new HashMap<Integer,NucSel>();
		private Vector<Map<NucSel, NucLineage.NucInterp>> interpNuc=new Vector<Map<NucSel, NucLineage.NucInterp>>();
		final ModelWindow w;
		public void fillModelWindowMenus()
			{
			w.sidePanelItems.add(bAddExpPattern);
			for(ModwPanelExpPattern ti:expsettings)
				w.sidePanelItems.add(ti);

			}
		
		public double nucMagnification=1;
		
	//	public JCheckBoxMenuItem miShowAllNucNames=new JCheckBoxMenuItem("Names: Show all");
//		public JCheckBoxMenuItem miShowSelectedNucNames=new JCheckBoxMenuItem("Names: Show for selected");

		public JMenu mShowNames=new JMenu("Show names");
		public JRadioButtonMenuItem miShowNamesNone=new JRadioButtonMenuItem("None",true);
		public JRadioButtonMenuItem miShowNamesSelected=new JRadioButtonMenuItem("Selected");
		public JRadioButtonMenuItem miShowNamesAll=new JRadioButtonMenuItem("All");
		public ButtonGroup mShowNamesGroup=new ButtonGroup();
		
		public JMenu mShowExp=new JMenu("Show expression as");
		public JRadioButtonMenuItem miShowExpColorMod=new JRadioButtonMenuItem("Colored nuclei");
		public JRadioButtonMenuItem miShowExpColorAND=new JRadioButtonMenuItem("Colored nuclei AND");
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

		public JMenuItem miSelectVisible=new JMenuItem("Select visible nuclei"); 


		/**
		 * How to show expression patterns
		 */
		public static enum ShowExp {CellColor, MarkerColor, MarkerSize, CellColorAND};
		public ShowExp showExpAs=ShowExp.MarkerColor;

		/**
		 * Color of the movement trace of nuclei
		 */
		public EvColor traceColor=null;

		public JCheckBoxMenuItem miShowDiv=new JCheckBoxMenuItem("Show division lines", true); 

		public JCheckBoxMenuItem miShowDelaunay=new JCheckBoxMenuItem("Show delaunay neighbours", false);
		

		
		Vector<ModwPanelExpPattern> expsettings=new Vector<ModwPanelExpPattern>();
		private JButton bAddExpPattern=new JButton("Add exp.pattern");

		
		private void setTraceColor(EvColor c)
			{
			traceColor=c;
			}
		
		public NucModelWindowHook(ModelWindow w)
			{
			this.w=w;
			
			JMenu miNuc=new JMenu("Nuclei/Lineage");

			JMenu mTraceColor=new JMenu("Set trace color");
			JMenuItem miColorSame=new JMenuItem("Same color as nucleus");
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
			new NucCommonUI(w, w).addToMenu(miNuc, false);

			
			w.menuModel.add(miNuc);

			
			
	//		miSaveColorScheme.addActionListener(this);
		//	miLoadColorScheme.addActionListener(this);
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
					//Clicking a nucleus selects it
					if(SwingUtilities.isLeftMouseButton(e))
						NucCommonUI.mouseSelectNuc(NucCommonUI.currentHover, (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK)!=0);
					}
				public boolean mouseDragged(MouseEvent e, int dx, int dy){return false;}
				public void mouseEntered(MouseEvent e){}
				public void mouseExited(MouseEvent e){}
				public void mouseMoved(MouseEvent e){}
				public void mousePressed(MouseEvent e){}
				public void mouseReleased(MouseEvent e){}
			});
			}
		
		public void readPersonalConfig(Element e){}
		public void savePersonalConfig(Element e){}
		
		private Collection<String> getAllExpPatterns()
			{
			//Update list of expression patterns
			Set<String> v=new TreeSet<String>();
			for(NucLineage lin:NucLineage.getLineages(w.getSelectedData()))
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
				for(endrov.nuc.NucSel p:NucCommonUI.getSelectedNuclei())
					NucCommonUI.hiddenNuclei.remove(p);
				}
			else if(e.getSource()==miHideSelectedNuc)
				{
				for(endrov.nuc.NucSel p:NucCommonUI.getSelectedNuclei())
					NucCommonUI.hiddenNuclei.add(p);
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
				for(Map.Entry<EvPath, NucLineage> entry:w.getSelectedData().getIdObjectsRecursive(NucLineage.class).entrySet())
					{
					for(NucSel i:entry.getValue().getInterpNuc(frame).keySet())
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
			return ob instanceof NucLineage;
			}

		
		public Collection<NucLineage> getLineages()
			{
			Vector<NucLineage> v=new Vector<NucLineage>();
			for(NucLineage lin:NucLineage.getLineages(w.getSelectedData()))
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

			interpNuc.clear();
			for(NucLineage lin:getLineages())
				interpNuc.add(lin.getInterpNuc(w.getFrame()));
			}
		
		/**
		 * Render for selection
		 */
		public void displaySelect(GL gl)
			{
			//boolean showSmallNuc=miShowSmallNuclei.isSelected();
			if(EV.debugMode)
				System.out.println("#nuc to render: "+interpNuc.size());
			for(Map<NucSel, NucLineage.NucInterp> inter:interpNuc)
				for(Map.Entry<NucSel, NucLineage.NucInterp> entry:inter.entrySet())
					if(entry.getValue().isVisible())
						{
						int rawcol=w.view.reserveSelectColor(this);
						selectColorMap.put(rawcol, entry.getKey());
						w.view.setReserveColor(gl, rawcol);
						renderNucSel(gl,entry.getKey(), entry.getValue(), nucMagnification);
						}
			}
		
		/**
		 * Render movement trace of nuc
		 */
		private void renderTrace(GL2 gl, NucLineage.Nuc nuc, boolean simple, Color col)
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
					NucLineage.NucPos pos1=nuc.pos.get(f1);
					NucLineage.NucPos pos2=nuc.pos.get(f2);
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
					for(NucLineage.NucPos pos:nuc.pos.values())
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
		
		private Color colorForNuc(NucLineage.Nuc nuc)
			{
			Color col=null;
			if(traceColor!=null)
				col=traceColor.c;
			if(col==null)
				col=NucLineage.representativeColor(nuc.colorNuc);
			return col;
			}
		
		public Color getTraceColor(NucLineage.Nuc nuc)
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
			for(NucLineage lin:NucLineage.getLineages(w.getSelectedData()))
				v.addAll(lin.getAllExpNames());
			for(ModwPanelExpPattern panel:expsettings)
				panel.setAvailableExpressions(v);

			
			for(Map<NucSel, NucLineage.NucInterp> inter:interpNuc)
				{
				//Draw neighbours. Need be calculated in the background and cached
				if(miShowDelaunay.isSelected())
					{
					try
						{
						///////////////////////////// TODO TODO TODO  BAD
						double r=3000; //300 is about the embryo. embryo is not centered in reality.
						
//						r=600; //TODO consistent voronoi calc
						
						Map<NucSel, NucLineage.NucInterp> interX=new HashMap<NucSel, NucInterp>(inter);
						
						NucLineage.NucInterp i1=new NucLineage.NucInterp();
						i1.pos=new NucLineage.NucPos();
						i1.frameBefore=EvDecimal.ZERO;
						i1.pos.x=r;
						NucLineage.NucInterp i2=new NucLineage.NucInterp();
						i2.pos=new NucLineage.NucPos();
						i2.frameBefore=EvDecimal.ZERO;
						i2.pos.x=-r;
						NucLineage.NucInterp i3=new NucLineage.NucInterp();
						i3.pos=new NucLineage.NucPos();
						i3.frameBefore=EvDecimal.ZERO;
						i3.pos.y=-r;
						NucLineage.NucInterp i4=new NucLineage.NucInterp();
						i4.pos=new NucLineage.NucPos();
						i4.frameBefore=EvDecimal.ZERO;
						i4.pos.y=-r;

						interX.put(new NucSel(null,":::1"), i1);
						interX.put(new NucSel(null,":::2"), i2);
						interX.put(new NucSel(null,":::3"), i3);
						interX.put(new NucSel(null,":::4"), i4);
						///////////////////////////// TODO TODO TODO  BAD

						
						NucVoronoi nvor=new NucVoronoi(interX,false);
						
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
				
				
				
				for(NucSel nucPair:inter.keySet())
					{
					//Render nuc body
					renderNuc(gl, nucPair, inter.get(nucPair), curFrame);
					
					if(traceCur && !traceSel && inter.get(nucPair).isVisible())
						{
						Color col=colorForNuc(nucPair.getNuc());
						renderTrace(gl,nucPair.getNuc(), tracesSimple, col);
						}
					
					//Draw connecting line
					if(nucPair.snd().equals(NucLineage.connectNuc[0]))
						for(NucSel nucPair2:inter.keySet())
							if(nucPair2.snd().equals(NucLineage.connectNuc[1]))
								{
								NucInterp n=inter.get(nucPair);
								NucInterp m=inter.get(nucPair2);
								gl.glBegin(GL2.GL_LINES);
								gl.glColor3d(1, 1, 1);
								gl.glVertex3d(n.pos.x,n.pos.y,n.pos.z);
								gl.glVertex3d(m.pos.x,m.pos.y,m.pos.z);
								gl.glEnd();
								}
					}
			
				//Render nuclei text etc
				gl.glDisable(GL2.GL_LIGHTING);
				for(NucSel nucPair:inter.keySet())
					renderNucOverlay(gl,transparentRenderers,nucPair, inter.get(nucPair), curFrame);
				}
			
			if(traceSel)
				for(NucSel pair:NucCommonUI.getSelectedNuclei())
					{
					NucLineage.Nuc nuc=pair.getNuc();
					renderTrace(gl,nuc, tracesSimple, colorForNuc(pair.getNuc()));
					}
			
			//Cell divisions
			if(miShowDiv.isSelected())
				{
				gl.glLineWidth(3);
				for(NucLineage lin:getLineages())
					{
					for(NucLineage.Nuc nuc:lin.nuc.values())
						if(!nuc.pos.isEmpty() && nuc.parent!=null)
							{
							EvDecimal tframe=nuc.getFirstFrame();
							NucLineage.Nuc pnuc=lin.nuc.get(nuc.parent);
							if(!pnuc.pos.isEmpty())
								{
								EvDecimal pframe=pnuc.pos.lastKey();
								if(curFrame.greaterEqual(pframe) && curFrame.lessEqual(tframe))
									{
									NucLineage.NucPos npos=nuc.pos.get(tframe);
									NucLineage.NucPos ppos=pnuc.pos.get(pframe);
	
									gl.glBegin(GL2.GL_LINES);
									gl.glColor3d(1, 1, 0);
									gl.glVertex3d(npos.x,npos.y,npos.z);
									gl.glVertex3d(ppos.x,ppos.y,ppos.z);
									gl.glEnd();
									}
								}
							}
					}
				gl.glLineWidth(1);
				}
			gl.glPopAttrib();
			}

		
		/** Keep track of what hover was before hover test started */
		private NucSel lastHover=null;
		/** Called when hover test starts */
		public void hoverInit(int pixelid)
			{
			//Update hover
			lastHover=NucCommonUI.currentHover;
			NucCommonUI.currentHover=new NucSel();
			}
		/** Called when nucleus hovered */
		public void hover(int pixelid)
			{
			NucCommonUI.currentHover=selectColorMap.get(pixelid);
			System.out.println("New hover: "+NucCommonUI.currentHover);
			System.out.println("Last hover: "+lastHover);
			//Propagate hover. Avoid infinite recursion.
			if(!NucCommonUI.currentHover.equals(lastHover))
				{
				System.out.println("nuc rerend");
				BasicWindow.updateWindows(w);
				}
			}

		
		/**
		 * Adjust the scale
		 */
		public Collection<Double> adjustScale()
			{
			int count=0;
			for(Map<NucSel, NucLineage.NucInterp> i:interpNuc)
				count+=i.size();
			if(count>=2)
				{
				double maxx=-1000000,maxy=-1000000,maxz=-1000000;
				double minx= 1000000,miny= 1000000,minz= 1000000;

				//Calculate bounds
				for(Map<NucSel, NucLineage.NucInterp> inter:interpNuc)
					for(NucLineage.NucInterp nuc:inter.values())
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
				return Collections.singleton((Double)dist);
				}
			else
				return Collections.emptySet();
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
		 * Render body of one nucleus
		 */
		private void renderNuc(GL2 gl, NucSel nucPair, NucLineage.NucInterp nuc, EvDecimal curFrame)
			{
			//Visibility rule
			if(!nuc.isVisible())
				return;

			
			gl.glEnable(GL2.GL_CULL_FACE);

			//Save world coordinate
	    gl.glPushMatrix();
	    
			//Move to cell center = local coordinate
	    gl.glTranslated(nuc.pos.x,nuc.pos.y,nuc.pos.z);

	    
    	//System.out.println("bla "+nuc.pos.x+ nuc.pos.y+ nuc.pos.z+ " "+nuc.pos.r);
	    
	    double showRadius=nuc.pos.r*nucMagnification;

	    //Decide color based on if the nucleus is selected
	    Color repColor=NucLineage.representativeColor(nuc.colorNuc);
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
		    		NucExp n=nucPair.getNuc().exp.get(expName);
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
		    		NucExp n=nucPair.getNuc().exp.get(expName);
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
			else if(nuc.colorNuc!=null)
	    	nucColor=new float[]{repColor.getRed()/255.0f,repColor.getGreen()/255.0f,repColor.getBlue()/255.0f};
	    else
	    	nucColor=new float[]{1,1,1};
			
    	
			
//			float lightAmbient[] = { nucColor[0]*0.3f, nucColor[1]*0.3f, nucColor[2]*0.3f, 0.0f };
//    gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, nucColor, 0);   
	    	

			
	    if(NucCommonUI.hiddenNuclei.contains(nucPair))
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
		 * Render label of one nucleus. Also render exp pattern
		 */
		private void renderNucOverlay(GL2 gl, List<TransparentRender> transparentRenderers, NucSel nucPair, NucLineage.NucInterp nuc, EvDecimal curFrame)
			{
			//Visibility rule. TODO. needed?
			if(nuc.frameBefore==null)
				return;

			//Save world coordinate
	    gl.glPushMatrix();
	    
			//Move to cell center = local coordinate
	    gl.glTranslated(nuc.pos.x,nuc.pos.y,nuc.pos.z);
			
      gl.glScalef(-1,-1,-1); //remove later

			
      boolean shouldDrawText=NucCommonUI.currentHover.equals(nucPair) 
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
  	    		NucExp n=nucPair.getNuc().exp.get(expName);
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
		 * Render nucleus in the invisible selection channel
		 */
		private void renderNucSel(GL glin, NucSel nucPair, NucLineage.NucInterp nuc, double nucMagnification)
			{    
			GL2 gl=glin.getGL2();
			gl.glEnable(GL2.GL_CULL_FACE);
			
			//Save world coordinate && Move to cell center = local coordinate
	    gl.glPushMatrix();
	    gl.glTranslated(nuc.pos.x,nuc.pos.y,nuc.pos.z);
	  	//If visible cell
	    if(!NucCommonUI.hiddenNuclei.contains(nucPair))
	    	{
	    	double showRadius=nuc.pos.r*nucMagnification;
	    	drawSelectSphere(gl, showRadius);
	    	}
	    //Go back to world coordinates
	    gl.glPopMatrix();
			}
		

		
		/**
		 * Give suitable center of all objects
		 */
		public Collection<Vector3d> autoCenterMid()
			{
			//Calculate center
			double meanx=0, meany=0, meanz=0;
			int num=0;
			for(NucLineage lin:getLineages())
				{
				Map<NucSel, NucLineage.NucInterp> interpNuc=lin.getInterpNuc(w.getFrame());
				num+=interpNuc.size();
				for(NucLineage.NucInterp nuc:interpNuc.values()) //what about non-existing ones?
					{
					meanx+=nuc.pos.x;
					meany+=nuc.pos.y;
					meanz+=nuc.pos.z;
					}
				}
			if(num==0)
				return Collections.emptySet();
			else
				{
				meanx/=num;
				meany/=num;
				meanz/=num;
				return Collections.singleton(new Vector3d(meanx,meany,meanz));
				}
			}
		
		
		/**
		 * Given a middle position, figure out radius required to fit objects
		 */
		public double autoCenterRadius(Vector3d mid)
			{
			//Calculate maximum radius
			double maxr=0;
			for(NucLineage lin:getLineages())
				{
				Map<NucSel, NucLineage.NucInterp> interpNuc=lin.getInterpNuc(w.getFrame());
				for(NucLineage.NucInterp nuc:interpNuc.values())
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
			for(NucLineage lin:w.getVisibleObjects(NucLineage.class))
				{
				EvDecimal f=lin.firstFrameOfLineage().fst();
				if(f!=null && (first==null || f.less(first)))
					first=f;
				}
			return first;
			}
		public EvDecimal getLastFrame()
			{
			EvDecimal last=null;
			for(NucLineage lin:w.getVisibleObjects(NucLineage.class))
				{
				EvDecimal f=lin.lastFrameOfLineage().fst();
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
		ModelWindow.modelWindowExtensions.add(new NucModelExtension());
		}

	}


