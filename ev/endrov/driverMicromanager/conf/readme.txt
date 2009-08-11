These files are taken from Micro-manager studio. The license file is the one included in Micro-manager.
small modifications made by Johan Henriksson. These are made available under the same license. 


in microscopemodel.java:

   public static final String DEVLIST_FILE_NAME = "endrov/driverMicromanager/conf/MMDeviceList.txt";

should modify micromanager to keep this file in a better location

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