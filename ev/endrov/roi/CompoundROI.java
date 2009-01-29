package endrov.roi;

import java.util.*;

import endrov.basicWindow.BasicWindow;
import endrov.data.*;

/**
 * Compound ROI (Region of interest). This is a ROI that depend on other ROI's ie has children
 * 
 * @author Johan Henriksson
 */
public abstract class CompoundROI extends ROI
	{
	/**
	 * Get immediate children ROIs. List ordered by name.
	 */
	public List<ROI> getSubRoi()
		{
		ArrayList<ROI> alist=new ArrayList<ROI>();
		alist.addAll(getObjects(ROI.class));
		return alist;
		}

	/**
	 * Take selected ROIs and push them all into a compound ROI
	 */
	public static void makeCompoundROI(EvContainer data, CompoundROI croi)
		{
		Set<ROI> rois=CompoundROI.collectRecursiveROI(data);
		data.addMetaObject(croi);
		int i=0;
		for(ROI roi:rois)
			{
			String name;
			do
				{
				name=""+i;
				i++;
				} while(croi.metaObject.containsKey(name));
			croi.metaObject.put(name, roi);
			}
		BasicWindow.updateWindows();
		}
	

	public SortedMap<String,ROI> getImmediateSubROI()
		{
		//TODO Here it is VERY important that it means to take a subclass
		return getIdObjects(ROI.class);
		}
	
	/**
	 * Remove all selected ROIs recursively from their parents and return them
	 */
	private static Set<ROI> collectRecursiveROI(EvContainer parent)
		{
		HashSet<ROI> hs=new HashSet<ROI>();
//		if(parent instanceof EvContainer)
			{
			EvContainer data=(EvContainer)parent;
			Set<String> toremove=new HashSet<String>();
			for(String key:data.metaObject.keySet())
				{
				EvObject ob=data.metaObject.get(key);
				if(ob instanceof ROI)
					{
					if(ROI.isSelected((ROI)ob))
						{
						toremove.add(key);
						hs.add((ROI)ob);
						}
					else
						hs.addAll(collectRecursiveROI(ob));
					}
				}
			for(String key:toremove)
				data.metaObject.remove(key);
			}
		/*
		else if(parent instanceof CompoundROI)
			{
			Set<ROI> toremove=new HashSet<ROI>();
			for(ROI roi:((CompoundROI)parent).subRoi)
				{
				if(ROI.isSelected(roi))
					{
					toremove.add(roi);
					hs.add((ROI)roi);
					}
				else
					hs.addAll(collectRecursiveROI(roi));
				}
			((CompoundROI)parent).subRoi.removeAll(toremove);
			}*/
		return hs;
		}
	
	
	
	public static class CompoundHandle implements Handle
		{
		private int id;
		private Handle h;
		public CompoundHandle(int id, Handle h)
			{
			this.h=h;
			this.id=id;
			}
		public String getID()
			{
			return ""+id+"_"+h.getID();
			}
		public double getX()
			{
			return h.getX();
			}
		public double getY()
			{
			return h.getY();
			}
		public void setPos(double x, double y)
			{
			h.setPos(x, y);
			}
		}
	
	
	
	
	/**
	 * Get handles for corners
	 */
	protected Handle[] getCompoundHandles()  //should make a set instead, or linked list
		{
		int id=0;
		LinkedList<Handle> h=new LinkedList<Handle>();
		for(ROI roi:getSubRoi())
			for(Handle th:roi.getHandles())
				h.add(new CompoundHandle(id++,th));
//	return (Handle[])h.toArray();
		Handle[] hh=new Handle[h.size()];
		int i=0;
		for(Handle th:h)
			{
			hh[i]=th;
			i++;
			}
		return hh;
		}
	
	
	/*
	protected void saveCompoundMetadata(String name,Element e)
		{
		e.setName(name);
		for(ROI r:subRoi)
			{
			Element e2=new Element("TEMPNAME");
			r.saveMetadata(e2);
			e.addContent(e2);
			}
		}*/
	
	/*
	protected void loadCompoundMetadata(Element e)
		{
		Vector<EvObject> cobs=EvData.getChildObXML(e);
		for(EvObject r:cobs)
			subRoi.add((ROI)r);
		}*/
	
//function to write/read all children as xml	
	}
