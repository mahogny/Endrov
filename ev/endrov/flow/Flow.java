package endrov.flow;

import java.util.*;

import endrov.flow.std.FlowUnitImserv;
import endrov.flow.std.FlowUnitImservLoad;
import endrov.flow.std.FlowUnitImservQuery;
import endrov.flow.std.basic.FlowUnitConstString;
import endrov.flow.std.basic.FlowUnitGetObject;
import endrov.flow.std.basic.FlowUnitIf;
import endrov.flow.std.basic.FlowUnitInput;
import endrov.flow.std.basic.FlowUnitOutput;
import endrov.flow.std.basic.FlowUnitScript;
import endrov.flow.std.collection.FlowUnitHeadTail;
import endrov.flow.std.collection.FlowUnitMap;
import endrov.flow.std.math.FlowUnitDiv;

/**
 * Flow object - organisation of filters and work process
 * 
 * @author Johan Henriksson
 *
 */
public class Flow //container?
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	public static Vector<FlowUnitDeclaration> unitDeclarations=new Vector<FlowUnitDeclaration>();
	
	public static void initPlugin() {}
	static
		{
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Basic","If"){
		public FlowUnit createInstance(){return new FlowUnitIf();}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Basic","Script"){
		public FlowUnit createInstance(){return new FlowUnitScript();}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Basic","GetObject"){
		public FlowUnit createInstance(){return new FlowUnitGetObject();}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Basic","String"){
		public FlowUnit createInstance(){return new FlowUnitConstString("foo");}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Basic","Integer"){
		public FlowUnit createInstance(){return new FlowUnitConstString("foo");}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Basic","Double"){
		public FlowUnit createInstance(){return new FlowUnitConstString("foo");}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Basic","Input"){
		public FlowUnit createInstance(){return new FlowUnitInput("foo");}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Basic","Output"){
		public FlowUnit createInstance(){return new FlowUnitOutput("foo");}});


		Flow.unitDeclarations.add(new FlowUnitDeclaration("Math","Div"){
		public FlowUnit createInstance(){return new FlowUnitDiv();}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Math","Mul"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Math","Add"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Math","Pow"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Math","Sqrt"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Math","Sub"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Math","Modulo"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Math","Expression"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Math","Floor"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Math","Ceil"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Math","ToInteger"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Math","ToDouble"){
		public FlowUnit createInstance(){return null;}});

		Flow.unitDeclarations.add(new FlowUnitDeclaration("Stats","Average"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Stats","Variance"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Stats","Median"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Stats","t-test"){
		public FlowUnit createInstance(){return null;}});

		Flow.unitDeclarations.add(new FlowUnitDeclaration("I/O","Read CSV"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("I/O","Read XML"){
		public FlowUnit createInstance(){return null;}});

		Flow.unitDeclarations.add(new FlowUnitDeclaration("Im.Enhance","Contrast"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Im.Math","Convolve"){
		public FlowUnit createInstance(){return null;}});
		
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Collection","Size"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Collection","GetByKey"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Collection","GetBy#"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Collection","Map"){
		public FlowUnit createInstance(){return new FlowUnitMap();}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Collection","Fold1"){
		public FlowUnit createInstance(){return null;}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Collection","HeadTail"){
		public FlowUnit createInstance(){return new FlowUnitHeadTail();}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("Collection","Merge"){
		public FlowUnit createInstance(){return new FlowUnitHeadTail();}});
		
		

		Flow.unitDeclarations.add(new FlowUnitDeclaration("Line","TotalLength"){
			public FlowUnit createInstance(){return null;}});

		Flow.unitDeclarations.add(new FlowUnitDeclaration("ImServ","ImServ"){
		public FlowUnit createInstance(){return new FlowUnitImserv();}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("ImServ","Load"){
		public FlowUnit createInstance(){return new FlowUnitImservLoad();}});
		Flow.unitDeclarations.add(new FlowUnitDeclaration("ImServ","Query"){
		public FlowUnit createInstance(){return new FlowUnitImservQuery();}});

		
		}

	
	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	
	public List<FlowUnit> units=new Vector<FlowUnit>();
	public List<FlowConn> conns=new Vector<FlowConn>();
	
	
	}
