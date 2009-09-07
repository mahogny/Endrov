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

		
//		public EvColor traceColor=EvColor.redMedium;
		public EvColor traceColor=null;
		
		//public JCheckBoxMenuItem miShowSmallNuclei=new JCheckBoxMenuItem("Nuclei 50% size"); 

		public JCheckBoxMenuItem miShowDiv=new JCheckBoxMenuItem("Show division lines", true); 

		public JCheckBoxMenuItem miShowDelaunay=new JCheckBoxMenuItem("Show delaunay neighbours", false);
		
		public JMenuItem miPrintAngle=new JMenuItem("Print angles");  
		public JMenuItem miPrintPos=new JMenuItem("Print positions");  

		public JMenuItem miPrintCountNucAtFrame=new JMenuItem("Print nuclei count in frame");  
		public JMenuItem miPrintCountNucUpTo=new JMenuItem("Print nuclei count up to frame");  

		
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

			mShowNucSize.add(miShowNucSize0);
			mShowNucSize.add(miShowNucSize25);
			mShowNucSize.add(miShowNucSize50);
			mShowNucSize.add(miShowNucSize75);
			mShowNucSize.add(miShowNucSize100);
			mShowNucSize.add(miShowNucSizeCustom);


			//miNuc.add(NucCommonUI.makeSetColorMenu());
			miNuc.add(mShowNames);
			miNuc.add(mShowNucSize);
			miNuc.add(mShowTrace);
			miNuc.add(mTraceColor);
			miNuc.add(miSetTraceWidth);
			miNuc.add(miShowSimpleTraces);
			miNuc.add(miShowSelectedNuc);
			miNuc.add(miHideSelectedNuc);
			
			
			
			miNuc.add(miShowDiv);
			miNuc.add(miShowDelaunay);
			miNuc.add(miPrintAngle);
			miNuc.add(miPrintPos);
			miNuc.add(miPrintCountNucAtFrame);
			miNuc.add(miPrintCountNucUpTo);
			
			miNuc.add(miSelectVisible);
			
			miNuc.addSeparator();
			new NucCommonUI(w).addToMenu(miNuc, false);

			
			w.menuModel.add(miNuc);

			
			
	//		miSaveColorScheme.addActionListener(this);
		//	miLoadColorScheme.addActionListener(this);
			miShowNamesNone.addActionListener(this);
			miShowNamesSelected.addActionListener(this);
			miShowNamesAll.addActionListener(this);
			miShowSelectedNuc.addActionListener(this);
			miHideSelectedNuc.addActionListener(this);
			miShowTraceSel.addActionListener(this);
			miShowTraceAll.addActionListener(this);
			miShowDiv.addActionListener(this);
			miShowDelaunay.addActionListener(this);
			miPrintAngle.addActionListener(this);
			miPrintPos.addActionListener(this);
			miPrintCountNucAtFrame.addActionListener(this);
			miPrintCountNucUpTo.addActionListener(this);
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
						NucLineage.mouseSelectNuc(NucLineage.currentHover, (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK)!=0);
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
		
		public void datachangedEvent()
			{
			//Update list of expression patterns
			Set<String> v=new TreeSet<String>();
			for(NucLineage lin:NucLineage.getLineages(w.getSelectedData()))
				v.addAll(lin.getAllExpNames());
			for(ModwPanelExpPattern panel:expsettings)
				panel.setAvailableExpressions(v);
			}
		
		public void actionPerformed(ActionEvent e)
			{
			if(e.getSource()==miShowSelectedNuc)
				{
				for(endrov.nuc.NucSel p:NucLineage.getSelectedNuclei())
					NucLineage.hiddenNuclei.remove(p);
				}
			else if(e.getSource()==miHideSelectedNuc)
				{
				for(endrov.nuc.NucSel p:NucLineage.getSelectedNuclei())
					NucLineage.hiddenNuclei.add(p);
				}
//			else if(e.getSource()==miSaveColorScheme)
			else if(e.getSource()==miPrintAngle)
				{
				EvDecimal frame=w.frameControl.getFrame();
				NucCommonUI.calcAngle(frame);
				}
			else if(e.getSource()==miPrintPos)
				{
				EvDecimal frame=w.frameControl.getFrame();
				NucCommonUI.actionShowPos(frame);
				}
			else if(e.getSource()==miPrintCountNucAtFrame)
				{
				EvDecimal frame=w.frameControl.getFrame();
				//TODO replace with visible set
				for(Map.Entry<EvPath, NucLineage> entry:w.getSelectedData().getIdObjectsRecursive(NucLineage.class).entrySet())
					EvLog.printLog(entry.getKey().toString()+" numberOfNuclei: "+entry.getValue().countNucAtFrame(frame));
				}
			else if(e.getSource()==miPrintCountNucUpTo)
				{
				EvDecimal frame=w.frameControl.getFrame();
				//TODO replace with visible set
				for(Map.Entry<EvPath, NucLineage> entry:w.getSelectedData().getIdObjectsRecursive(NucLineage.class).entrySet())
					EvLog.printLog(entry.getKey().toString()+" numberOfNuclei: "+entry.getValue().countNucUpTo(frame));
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
				EvDecimal frame=w.frameControl.getFrame();
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
				expsettings.add(new ModwPanelExpPattern(this));
				w.updateToolPanels();
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
		
		/**
		 * Prepare for rendering
		 */
		public void displayInit(GL gl)
			{
			selectColorMap.clear();

			interpNuc.clear();
			for(NucLineage lin:getLineages())
				interpNuc.add(lin.getInterpNuc(w.frameControl.getFrame()));
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
		private void renderTrace(GL gl, NucLineage.Nuc nuc, boolean simple, Color col)
			{
			if(!nuc.pos.isEmpty())
				{
				gl.glLineWidth(traceWidth);
				float colR=(float)traceColor.getRedDouble();
				float colG=(float)traceColor.getGreenDouble();
				float colB=(float)traceColor.getBlueDouble();
				gl.glColor3d(colR,colG,colB);
//				gl.glColor3d(1, 1, 1);
				if(simple)
					{
					gl.glBegin(GL.GL_LINE_STRIP);
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
		      gl.glEnable(GL.GL_LIGHTING);
					w.view.renderArrowHead(gl, pos2.getPosCopy(), direction, colR, colG, colB);
		      gl.glDisable(GL.GL_LIGHTING);
					}
				else
					{
					gl.glBegin(GL.GL_LINE_STRIP);
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
		
		private Color colorForNuc(NucSel pair)
			{
			Color col=null;
			if(traceColor!=null)
				col=traceColor.c;
			if(col==null)
				col=NucLineage.representativeColor(pair.getNuc().colorNuc);
			return col;
			}
		
		/**
		 * Render graphics
		 */
		public void displayFinal(GL gl,List<TransparentRender> transparentRenderers)
			{
			initDrawSphere(gl);
			gl.glPushAttrib(GL.GL_ALL_ATTRIB_BITS);
			
			EvDecimal curFrame=w.frameControl.getFrame();

			boolean traceCur=miShowTraceAll.isSelected();
			boolean traceSel=miShowTraceSel.isSelected();
			boolean tracesSimple=miShowSimpleTraces.isSelected();
			
		
			//Set suitable scaling for expression patterns
			for(ModwPanelExpPattern panel:expsettings)
				if(panel.scale1==null)
					{
					//Find lineage with this expression pattern
					String expName=panel.getSelectedExp();
					for(NucLineage lin:NucLineage.getLineages(w.getSelectedData()))
						if(lin.getAllExpNames().contains(expName))
							{
							Tuple<Double,Double> maxMin1=lin.getMaxMinExpLevel(expName);
							if(maxMin1!=null)
								{
								double absmax=Math.max(Math.abs(maxMin1.fst()), Math.abs(maxMin1.snd()));
								panel.scale1=1.0/absmax;
								}
							break;
							}
					
					}
			
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
								gl.glBegin(GL.GL_LINE_LOOP);
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
							gl.glBegin(GL.GL_LINES);
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
						Color col=colorForNuc(nucPair);
						renderTrace(gl,nucPair.getNuc(), tracesSimple, col);
						}
					
					//Draw connecting line
					if(nucPair.snd().equals(NucLineage.connectNuc[0]))
						for(NucSel nucPair2:inter.keySet())
							if(nucPair2.snd().equals(NucLineage.connectNuc[1]))
								{
								NucInterp n=inter.get(nucPair);
								NucInterp m=inter.get(nucPair2);
								gl.glBegin(GL.GL_LINES);
								gl.glColor3d(1, 1, 1);
								gl.glVertex3d(n.pos.x,n.pos.y,n.pos.z);
								gl.glVertex3d(m.pos.x,m.pos.y,m.pos.z);
								gl.glEnd();
								}
					}
			
				//Render nuclei text
				for(NucSel nucPair:inter.keySet())
					renderNucLabel(gl,transparentRenderers,nucPair, inter.get(nucPair));
				}
			
			if(traceSel)
				for(NucSel pair:NucLineage.getSelectedNuclei())
					{
					NucLineage.Nuc nuc=pair.getNuc();
					renderTrace(gl,nuc, tracesSimple, colorForNuc(pair));
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
	
									gl.glBegin(GL.GL_LINES);
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
			lastHover=NucLineage.currentHover;
			NucLineage.currentHover=new NucSel();
			}
		/** Called when nucleus hovered */
		public void hover(int pixelid)
			{
			NucLineage.currentHover=selectColorMap.get(pixelid);
			System.out.println("New hover: "+NucLineage.currentHover);
			System.out.println("Last hover: "+lastHover);
			//Propagate hover. Avoid infinite recursion.
			if(!NucLineage.currentHover.equals(lastHover))
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

		
		/**
		 * Render body of one nucleus
		 */
		private void renderNuc(GL gl, NucSel nucPair, NucLineage.NucInterp nuc, EvDecimal curFrame)
			{
			//Visibility rule
			if(!nuc.isVisible())
				return;
			
			gl.glEnable(GL.GL_CULL_FACE);

			//Save world coordinate
	    gl.glPushMatrix();
	    
			//Move to cell center = local coordinate
	    gl.glTranslated(nuc.pos.x,nuc.pos.y,nuc.pos.z);

	    double showRadius=nuc.pos.r*nucMagnification;

	    //Decide color based on if the nucleus is selected
	    Color repColor=NucLineage.representativeColor(nuc.colorNuc);
			float nucColor[];
			if(!expsettings.isEmpty())
				{
				//Add color from expression patterns
				nucColor=new float[]{0.1f,0.1f,0.1f};
	    	for(ModwPanelExpPattern panel:expsettings)
	    		{
	    		String expName=panel.getSelectedExp();
	    		NucExp n=nucPair.getNuc().exp.get(expName);
	    		if(n!=null)
	    			{
	    			double level=n.interpolateLevel(curFrame);
	    			double scale=panel.scale1;
	    			nucColor[0]+=(float)panel.colR*level*scale;
	    			nucColor[1]+=(float)panel.colG*level*scale;
	    			nucColor[2]+=(float)panel.colB*level*scale;
	    			//System.out.println("here"+level+" ");
	    			}
	    		else
	    			{
	    			System.out.println("no "+expName);
	    			}
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
//    gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, nucColor, 0);   
	    	
			
			
	    if(NucLineage.hiddenNuclei.contains(nucPair))
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


		
		
		/**
		 * Generate vertex lists for spheres
		 */
		private void initDrawSphere(GL gl)
			{
			if(!madeDisplayLists)
				{
				madeDisplayLists=true;
				GLU glu=new GLU();
				GLUquadric q=glu.gluNewQuadric();
				
				displayListVisibleSphere = gl.glGenLists(1);
				gl.glNewList(displayListVisibleSphere, GL.GL_COMPILE);
				glu.gluSphere(q,1.0,NUC_SHOW_DIV,NUC_SHOW_DIV);
				//drawSphereSolid(gl, 1.0, NUC_SHOW_DIV,NUC_SHOW_DIV);
				gl.glEndList();
				
				displayListSelectSphere = gl.glGenLists(1);
				gl.glNewList(displayListSelectSphere, GL.GL_COMPILE);
				glu.gluSphere(q,1.0,NUC_SELECT_DIV,NUC_SELECT_DIV);
				gl.glEndList();

				glu.gluQuadricDrawStyle(q, GLU.GLU_LINE);
				
				displayListHiddenSphere = gl.glGenLists(1);
				gl.glNewList(displayListHiddenSphere, GL.GL_COMPILE);
				glu.gluSphere(q,1.0,NUC_HIDE_DIV,NUC_HIDE_DIV);
				gl.glEndList();
				
				glu.gluDeleteQuadric(q);
				}
			}
		private boolean madeDisplayLists=false;
		private int displayListVisibleSphere;
		private int displayListHiddenSphere;
		private int displayListSelectSphere;
		
		
		private void drawVisibleSphere(GL gl, double r, boolean selected, float colR, float colG, float colB)
			{
    	double ir=1.0/r;
			gl.glScaled(r,r,r);
			
			if(selected)
				{
	    	gl.glColor3d(1,0,1);
				gl.glLineWidth(5);
				gl.glPolygonMode(GL.GL_BACK, GL.GL_LINE);
				gl.glCullFace(GL.GL_FRONT);
				gl.glDepthFunc(GL.GL_LEQUAL);
	    	gl.glCallList(displayListVisibleSphere);
				gl.glCullFace(GL.GL_BACK);
				gl.glDepthFunc(GL.GL_LESS);
				gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
				gl.glLineWidth(1);
				}
			
      gl.glEnable(GL.GL_LIGHTING);
    	gl.glColor3d(1,1,1);
    	gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, new float[]{colR,colG,colB}, 0);
//    	gl.glColor3d(colR,colG,colB);
    	gl.glCallList(displayListVisibleSphere);
      gl.glDisable(GL.GL_LIGHTING);

    	gl.glScaled(ir,ir,ir);
			}
		private void drawHiddenSphere(GL gl, double r)
			{
    	double ir=1.0/r;
			gl.glScaled(r,r,r);
    	gl.glCallList(displayListHiddenSphere);
    	gl.glScaled(ir,ir,ir);
			}
		
		public void drawSelectSphere(GL gl, double r)
			{
    	double ir=1.0/r;
			gl.glScaled(r,r,r);
    	gl.glCallList(displayListSelectSphere);
    	gl.glScaled(ir,ir,ir);
			}
		
		
		/**
		 * Render labe of one nucleus
		 */
		private void renderNucLabel(GL gl, List<TransparentRender> transparentRenderers, NucSel nucPair, NucLineage.NucInterp nuc)
			{
			//Visibility rule
			if(nuc.frameBefore==null)
				return;

			//Save world coordinate
	    gl.glPushMatrix();
	    
			//Move to cell center = local coordinate
	    gl.glTranslated(nuc.pos.x,nuc.pos.y,nuc.pos.z);
			
      gl.glScalef(-1,-1,-1); //remove later

			
	    //Unrotate camera, then move a bit closer to the camera
	    if(NucLineage.currentHover.equals(nucPair) 
	    		|| miShowNamesAll.isSelected() 
	    		|| (EvSelection.isSelected(nucPair) && miShowNamesSelected.isSelected()))
	    	{
	    	w.view.camera.unrotateGL(gl);
	    
	    	gl.glRotated(180,   0.0, 0.0, 1.0);
	    	gl.glTranslated(0.0, 0.0, -nuc.pos.r*1.05);
	    	//it would look better if it was toward the camera *center*
	    	//also consider setting size such that it does not vary with distance
	    	//3d text at all? overlay rendering should be faster
				String nucName=nucPair.snd();
	    	w.view.renderString(gl, transparentRenderers, (float)(0.005*nuc.pos.r), nucName);
	    	}
	    
	    //Go back to world coordinates
	    gl.glPopMatrix();
			}	
		
		
		/**
		 * Render nucleus in the invisible selection channel
		 */
		private void renderNucSel(GL gl, NucSel nucPair, NucLineage.NucInterp nuc, double nucMagnification)
			{    
			gl.glEnable(GL.GL_CULL_FACE);
			
			//Save world coordinate && Move to cell center = local coordinate
	    gl.glPushMatrix();
	    gl.glTranslated(nuc.pos.x,nuc.pos.y,nuc.pos.z);
	  	//If visible cell
	    if(!NucLineage.hiddenNuclei.contains(nucPair))
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
				Map<NucSel, NucLineage.NucInterp> interpNuc=lin.getInterpNuc(w.frameControl.getFrame());
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
		public Collection<Double> autoCenterRadius(Vector3d mid, double FOV)
			{
			//Calculate maximum radius
			double maxr=0;
			boolean any=false;
			for(NucLineage lin:getLineages())
				{
				Map<NucSel, NucLineage.NucInterp> interpNuc=lin.getInterpNuc(w.frameControl.getFrame());
				any=true;
				for(NucLineage.NucInterp nuc:interpNuc.values())
					{
					double dx=nuc.pos.x-mid.x;
					double dy=nuc.pos.y-mid.y;
					double dz=nuc.pos.z-mid.z;
					double r=Math.sqrt(dx*dx+dy*dy+dz*dz)+nuc.pos.r;
					if(maxr<r)
						maxr=r;
					}
				if(EV.debugMode)
					System.out.println("center radius from nuc: "+(maxr/Math.sin(FOV)));

				}
			//Find how far away the camera has to be
			if(any)
				return Collections.singleton((Double)maxr/Math.sin(FOV));
			else
				return Collections.emptySet();
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
	}


