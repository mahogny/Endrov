package endrov.flow.std.basic;

import java.awt.Color;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowType;
import endrov.flow.FlowUnit;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclarationTrivial;

/**
 * Flow unit - Custom code (script). User can specify a function to apply on the data
 * @author Johan Henriksson
 *
 */
public class FlowUnitScript extends FlowUnitBasic
	{
	private static int scriptIDcnt=0;
	private static Object scriptIDlock=new Object();
	private static final String metaType="script";
	private int scriptID;
	
	private static ImageIcon icon=new ImageIcon(FlowUnitScript.class.getResource("silkScript.png"));
	
	public String code=
	"public void runFlow()\n" +
	" {\n" +
	" }\n";
	
	public static void initPlugin() {}
	static
		{
		Flow.unitDeclarations.add(new FlowUnitDeclarationTrivial("Basic","Script",metaType){
		public FlowUnit createInstance(){return new FlowUnitScript();}
		public FlowUnit fromXML(Element e)
			{
			FlowUnitScript u=new FlowUnitScript();
			u.code=e.getAttributeValue("code");
			return u;
			}
		});
		}
	
	public FlowUnitScript()
		{
		synchronized (scriptIDlock)
			{
			scriptID=scriptIDcnt++;
			}
		}
	
	
	public String getBasicShowName(){return "Script "+getScriptID();}
	public ImageIcon getIcon(){return icon;}
	
	
	public Color getBackground()
		{
		return new Color(200,255,255);
		}

	
	/** Get types of flows in */
	public SortedMap<String, FlowType> getTypesIn()
		{
		TreeMap<String, FlowType> types=new TreeMap<String, FlowType>();
		types.put("in", null);
		return types;
		}
	/** Get types of flows out */
	public SortedMap<String, FlowType> getTypesOut()
		{
		TreeMap<String, FlowType> types=new TreeMap<String, FlowType>();
		types.put("out", null);
		return types;
		}
	
	public int getScriptID()
		{
		return scriptID;
		}
	
	public String storeXML(Element e)
		{
		e.setAttribute("code", code);
		return metaType;
		}

	
	public void evaluate(Flow flow) throws Exception
		{
		}

	public void editDialog()
		{
		ScriptEditorWindow.openEditor(this);
		}

	
	}
