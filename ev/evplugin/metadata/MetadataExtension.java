package evplugin.metadata;

import javax.swing.*;

public interface MetadataExtension
	{
	public void buildOpen(JMenu menu);
	public void buildSave(JMenu menu, Metadata meta);
	}
