package endrov.imageset.newmetaTODO;

public class FormData
	{
	
	//*ref ->
	public static class Ref
		{
		String type; //If LSID, needed? simplifies internal browsing
		String ID; //Will be an LSID
		//  * pattern = (urn:lsid:([\w\-\.]+\.[\w\-\.]+)+:\S+:\S+)|(\S+:\S+)
    //  * pattern = (urn:lsid:([\w\-\.]+\.[\w\-\.]+)+:Experimenter:\S+)|(Experimenter:\S+)

		}
	
	//type ->
	public static class TypeData
		{
		String fieldName;
		String fieldShortDesc; //Shown to user
		String fieldLongDesc; //Tool-tip
		//Value
		String type;
		
		//Restriction on type ID
		//  * pattern = (urn:lsid:([\w\-\.]+\.[\w\-\.]+)+:\S+:\S+)|(\S+:\S+)
    //  * pattern = (urn:lsid:([\w\-\.]+\.[\w\-\.]+)+:Experimenter:\S+)|(Experimenter:\S+)

		//Pre-defined values
		
		//Automatic suggestion, e.g. Experimenter:1
		}

	
	/**
	 * 
	 * 
	 * 
	 * what about custom entries?
	 * should lab provide an XML with new type?
	 * 
	 * 
	 */
	
	
	
	
	
	
	
	}
