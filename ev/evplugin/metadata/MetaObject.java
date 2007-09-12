package evplugin.metadata;

import org.jdom.*;

public abstract class MetaObject
	{
	public boolean metaObjectModified=false;
	public abstract void saveMetadata(Element e);
	public abstract String getMetaTypeDesc();
	}
