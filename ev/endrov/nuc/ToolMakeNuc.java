package endrov.nuc;

import java.awt.*;
import java.awt.event.*;
import java.lang.ref.WeakReference;
import java.util.Map;
//import java.util.Collection;

import javax.vecmath.*;
import javax.swing.*;

import endrov.basicWindow.*;
import endrov.ev.Log;
import endrov.imageWindow.*;
import endrov.imageset.Imageset;
import endrov.keyBinding.KeyBinding;
//import evplugin.imageset.*;

/**
 * Make nuclei by dragging an area. Also move nuclei.
 *
 * @author Johan Henriksson
 */
public class ToolMakeNuc implements ImageWindowTool, ActionListener
	{
	private boolean active=false;
	private double x1,x2,y1,y2;

	private boolean holdTranslate=false;
	private boolean holdRadius=false;

	private final ImageWindow w;
	private final NucImageRenderer r;
	
	private WeakReference<NucLineage> editingLin=new WeakReference<NucLineage>(null);
	private void setEditLin(NucLineage lin)
		{
		editingLin=new WeakReference<NucLineage>(lin);
		}
	
	public ToolMakeNuc(final ImageWindow w, NucImageRenderer r)
		{
		this.w=w;
		this.r=r;
		}
	
	

	public JMenuItem getMenuItem()
		{
		JMenu menu=new JMenu("Nucleus");
		
		JMenu setColor=NucLineage.makeSetColorMenu();
		menu.add(setColor);
		
		Imageset ims=w.getImageset();
		final WeakReference<Imageset> wims=new WeakReference<Imageset>(ims);
		if(ims!=null)
			for(Map.Entry<String, NucLineage> e:ims.getIdObjects(NucLineage.class).entrySet())
				{
				JCheckBoxMenuItem miEdit=new JCheckBoxMenuItem("Edit "+e.getKey());
				miEdit.setActionCommand(e.getKey());
				miEdit.setSelected(editingLin.get()==e.getValue());
				miEdit.addActionListener(this);
				menu.add(miEdit);
				}
		JMenuItem miNew=new JMenuItem("New lineage");
		final ToolMakeNuc This=this;
		miNew.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
				{
				NucLineage lin=new NucLineage();
				wims.get().addMetaObject(lin);
				setEditLin(lin);
				w.setTool(This);
				}
		});
		menu.add(miNew);
		return menu;
		}
	public void actionPerformed(ActionEvent e)
		{
		String id=e.getActionCommand();
		setEditLin((NucLineage)w.getImageset().getMetaObject(id));
		w.setTool(this);
		}
	
	public void unselected()
		{
		}

	
	
	public void mouseClicked(MouseEvent e)
		{
		if(SwingUtilities.isLeftMouseButton(e) && !r.getVisibleLineages().isEmpty())
			NucLineage.mouseSelectNuc(NucLineage.currentHover, (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK)!=0);
		}
	
	public void mouseDragged(MouseEvent e, int dx, int dy)
		{
		if(active)
			{
			Vector2d v=w.transformS2W(new Vector2d(e.getX(),e.getY()));
			x2=v.x;
			y2=v.y;
			w.updateImagePanel();
			}
		}
	
	public void mousePressed(MouseEvent e)
		{
		if(SwingUtilities.isLeftMouseButton(e))
			{
			//Start making a nucleus
			active=true;
			Vector2d v=w.transformS2W(new Vector2d(e.getX(),e.getY()));
			x1=x2=v.x;
			y1=y2=v.y;
			}
		else if(SwingUtilities.isRightMouseButton(e))
			{
			//Cancel making nucleus
			active=false;
			w.updateImagePanel();
			}
		}

	//need to make lin somewhere
	
	public void mouseReleased(MouseEvent e)
		{
		if(SwingUtilities.isLeftMouseButton(e) && active)
			{
			//Make a nucleus if mouse has been dragged
			
			/*NucLineage lin=null;
//			Collection<NucLineage> lins=getLineages();
			if(!lins.isEmpty())
				lin=lins.iterator().next();*/
			NucLineage lin=editingLin.get();
			
			if(x1!=x2 && y1!=y2 && lin!=null && r.modifyingNucName==null)
				{
				//New name for this nucleus => null
				String nucName=lin.getUniqueNucName();
				NucLineage.Nuc n=lin.getNucCreate(nucName);
				NucLineage.NucPos pos=n.getPosCreate((int)w.frameControl.getFrame());
				pos.x=(x1+x2)/2;
				pos.y=(y1+y2)/2;
				pos.z=w.s2wz(w.frameControl.getZ());
				pos.r=Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2))/2;
				
				Vector2d so1=w.transformW2S(new Vector2d(pos.r,0));
				Vector2d so2=w.transformW2S(new Vector2d(0,0));

				if(Math.abs(so1.x-so2.x)>8)
//				if(Math.abs(w.w2sx(pos.r)-w.w2sx(0))>8)
					{
					NucLineage.selectedNuclei.clear();
					NucLineage.selectedNuclei.add(new NucPair(lin,nucName));
					BasicWindow.updateWindows();
					}
				}
				
			active=false;
			w.updateImagePanel();
			}
		}

	public void mouseMoved(MouseEvent e, int dx, int dy)
		{			
		//Handle modify-nucleus
		NucLineage.NucPos pos=r.getModifyingNucPos();
		if(pos!=null)
			{
			if(holdTranslate)
				{
				//Translate
				pos.x+=w.scaleS2w(dx);
				pos.y+=w.scaleS2w(dy);
				}
			//else if(KeyBinding.get(NucLineage.KEY_CHANGE_RADIUS).typed(e)) //KEYBIND
			else if(holdRadius)
				{
				//Change radius
				pos.r+=w.scaleS2w(dy);
				if(pos.r<w.scaleS2w(8.0))
					pos.r=w.scaleS2w(8.0);
				}			
			}
		}

	
	/*
	 * (non-Javadoc)
	 * @see client.ImageWindow.Tool#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e)
		{
		if(KeyBinding.get(NucLineage.KEY_TRANSLATE).typed(e))
			holdTranslate=true;
		if(KeyBinding.get(NucLineage.KEY_CHANGE_RADIUS).typed(e))
			holdRadius=true;
		
		int curFramei=(int)w.frameControl.getFrame();
		NucLineage lin=NucLineage.currentHover.fst();
		
		if(lin!=null && (KeyBinding.get(NucLineage.KEY_TRANSLATE).typed(e) || KeyBinding.get(NucLineage.KEY_CHANGE_RADIUS).typed(e)))
			{
			//Translate or change radius
			if(r.modifyingNucName==null && r.interpNuc.containsKey(NucLineage.currentHover)) //TODO: guarantee that every nucleus has 1 pos except during load
				r.modifyingNucName=NucLineage.currentHover;
			}
		else if(lin!=null && KeyBinding.get(NucLineage.KEY_CHANGE_RADIUS).typed(e))
			{
			//Divide nucleus
			NucLineage.Nuc n=NucLineage.currentHover.fst().nuc.get(NucLineage.currentHover.snd());
			if(n!=null && r.interpNuc.containsKey(NucLineage.currentHover))
				{
				lin.divide(NucLineage.currentHover.snd(), curFramei);
				BasicWindow.updateWindows();
				}
			}
		else if(KeyBinding.get(NucLineage.KEY_SETZ).typed(e))
			{
			//Bring nucleus to this Z
			NucPair useNuc=NucLineage.currentHover;

			if(useNuc.fst()==null)
				{
				System.out.println("foo");
				if(NucLineage.selectedNuclei.size()==1)
					useNuc=NucLineage.selectedNuclei.iterator().next();
				}
			
			NucLineage.Nuc n=null;
			if(useNuc.fst()!=null)
				n=useNuc.fst().nuc.get(useNuc.snd());
			
			if(n!=null && r.interpNuc.containsKey(useNuc))
				{
				NucLineage.NucInterp inter=r.interpNuc.get(useNuc);
				NucLineage.NucPos pos=n.getPosCreate(curFramei); 
				pos.x=inter.pos.x;
				pos.y=inter.pos.y;
				pos.z=w.s2wz(w.frameControl.getZ());
				pos.r=inter.pos.r;
				r.commitModifyingNuc();
				}
			}
		else if(lin!=null && KeyBinding.get(NucLineage.KEY_SETEND).typed(e))
			{
			//Set end frame of nucleus
			NucLineage.Nuc n=lin.nuc.get(NucLineage.currentHover.snd());
			if(n!=null)
				{
				if(n.overrideEnd!=null && n.overrideEnd==curFramei)
					n.overrideEnd=null;
				else
					{
					n.overrideEnd=curFramei;
					lin.removePosAfterEqual(NucLineage.currentHover.snd(), curFramei+1);
					}
				BasicWindow.updateWindows();
				}
			}
		else if(lin!=null && KeyBinding.get(NucLineage.KEY_SETSTART).typed(e))
			{
			//Set end frame of nucleus
			NucLineage.Nuc n=lin.nuc.get(NucLineage.currentHover.snd());
			if(n!=null)
				{
				if(n.overrideStart!=null && n.overrideStart==curFramei)
					n.overrideStart=null;
				else
					{	
					n.overrideStart=curFramei;
					lin.removePosBeforeEqual(NucLineage.currentHover.snd(), curFramei-1);
					}
				BasicWindow.updateWindows();
				}
			}
		else if(lin!=null && KeyBinding.get(NucLineage.KEY_MAKEPARENT).typed(e))
			{
			//Create parent for selected nucleus/nuclei
			String parentName=lin.getUniqueNucName();
			NucLineage.Nuc parent=lin.getNucCreate(parentName);
			
			double x=0,y=0,z=0,r=0;
			int num=0;
			int firstFrame=1000000;
			for(NucPair childPair:NucLineage.selectedNuclei)
				{
				String childName=childPair.snd();
				NucLineage.Nuc n=lin.nuc.get(childName);
				NucLineage.NucPos pos=n.pos.get(n.pos.firstKey());
				x+=pos.x;				y+=pos.y;				z+=pos.z;				r+=pos.r;
				if(n.pos.firstKey()<firstFrame)
					firstFrame=n.pos.firstKey();
				num++;
				n.parent=parentName;
				parent.child.add(childName);
				}
			x/=num;			y/=num;			z/=num;			r/=num;
			NucLineage.NucPos pos=new NucLineage.NucPos();
			pos.x=x; pos.y=y; pos.z=z; pos.r=r;
			parent.pos.put(firstFrame-1,pos);
			Log.printLog("Made parent "+parentName);
			
			this.r.w.frameControl.setFrame(firstFrame-1);
			BasicWindow.updateWindows();
			}
		else if(lin!=null && KeyBinding.get(NucLineage.KEY_SETPARENT).typed(e))
			{
			//Create parent-children relation
			NucLineage.createParentChildSelected();
			BasicWindow.updateWindows();
			}
		}

	
	public void paintComponent(Graphics g)
		{
		if(active)
			{
			g.setColor(Color.RED);
			double midx=(x2+x1)/2;
			double midy=(y2+y1)/2;
			double r=Math.sqrt((x1-midx)*(x1-midx)+(y1-midy)*(y1-midy));
			Vector2d omid=w.transformW2S(new Vector2d(midx,midy));
//			double omidx=w.w2sx(midx);
//			double omidy=w.w2sy(midy);
			double or=w.scaleW2s(r);
			g.drawOval((int)(omid.x-or),(int)(omid.y-or),(int)(or*2),(int)(or*2));
			}
		}

	
	public void keyReleased(KeyEvent e)
		{
		if(KeyBinding.get(NucLineage.KEY_TRANSLATE).typed(e))
			holdTranslate=false;
		if(KeyBinding.get(NucLineage.KEY_CHANGE_RADIUS).typed(e))
			holdRadius=false;
		
		if(KeyBinding.get(NucLineage.KEY_TRANSLATE).typed(e) || KeyBinding.get(NucLineage.KEY_CHANGE_RADIUS).typed(e))
			r.commitModifyingNuc();
		}

	public void mouseExited(MouseEvent e)
		{
		//	Delete nucleus if one is held and translating
		NucLineage lin=r.getModifyingLineage();
		NucLineage.Nuc n=r.getModifyingNuc();
		
		if(lin!=null && n!=null && holdTranslate)
			{
			//nuc temporarily flashes back here. don't know why (used to?)
			int framei=(int)w.frameControl.getFrame();
			n.pos.remove(framei);
			if(n.pos.size()==0)
				lin.removeNuc(r.modifyingNucName.snd());
			}
		r.modifyingNucName=null;
		BasicWindow.updateWindows();
		}

	
	}
