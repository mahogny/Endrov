These files are taken from Micro-manager studio. The license file is the one included in Micro-manager.
small modifications made by Johan Henriksson. These are made available under the same license. 


in microscopemodel.java:

   public static final String DEVLIST_FILE_NAME = "endrov/driverMicromanager/conf/MMDeviceList.txt";

should modify micromanager to keep this file in a better location

actually, not needed if devices work. MM queries all devices to build a new list.


-----


this is an unacceptable hack:

File fu=new File(new File(new File("."),"libs"),"umanager_inc");
pathList.add("/usr/lib/micro-manager");
if(EV.isMac())
	{
	if(EV.isPPC())
		pathList.add(new File(fu,"bin_mac/bin_ppc").getAbsolutePath());
	else
		pathList.add(new File(fu,"bin_mac/bin_x86").getAbsolutePath());
	}
if(EV.isWindows())
	pathList.add(new File(fu,"bin_mac/bin_windows").getAbsolutePath());
	
	
	
-------------------


plan: 
* every device need one primary hardwareMetadata because the state will be written to it
* every state of a state device can have multiple hardwareMetadata
  e.g. objective, filter, prism

  


---- need to modify MM -----
  
  
property block:
  dev -> propertypair
  
propertypair = property, value



PropertyBlock getStateLabelData(const char* deviceLabel, const char* stateLabel) const;


/** @name Property blocks
    * API for defining interchangeable equipment attributes
    */
   //@ {
   void definePropertyBlock(const char* blockName, const char* propertyName, const char* propertyValue);
   std::vector<std::string> getAvailablePropertyBlocks() const;
   PropertyBlock getPropertyBlockData(const char* blockName) const;
   //@ }


this API is entirelly unused. not loaded from config file.


add here MMCore:
void CMMCore::saveSystemConfiguration(const char* fileName) throw (CMMError)
void CMMCore::loadSystemConfiguration(const char* fileName) throw (CMMError)


while(is.getline(line, maxLineLength, '\n'))



---------------------- how to use PB ----

MM config file:

"d" is the device itself. otherwise it is a statenum

PropertyBlock,mydev,d,<hardware SerialNum=""/>
PropertyBlock,mydev,0,<hardware SerialNum=""/><hardware SerialNum=""/>

if multiple under d, the first is the primary association.