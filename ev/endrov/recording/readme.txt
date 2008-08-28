umanager to java map of devices:
	Device ->
	Camera ->
	Shutter ->
	Stage & XYStage -> Stage (n-dim)
	State "State device API, e.g. filter wheel, objective turret, etc."
	Serial -> 
	Autofocus?
	ImageStreamer?
	ImageProcessor?
	SignalIO? "ADC and DAC interface"
	Magnifier? "Devices that can change magnification of the system"
	

	
==== MMDevice.h	
class Core has goodies to directly interact with devices
	virtual Device* GetDevice(const Device* caller, const char* label) = 0;
	virtual MM::ImageProcessor* GetImageProcessor(const MM::Device* caller) = 0;
	virtual MM::State* GetStateDevice(const MM::Device* caller, const char* deviceName) = 0;
	virtual std::vector<std::string> GetLoadedDevicesOfType(const Device* caller, MM::DeviceType devType) = 0;
	
	-- AnyType :: enum DeviceType
	
class Device
	virtual void GetName(char* name) const = 0;
	virtual DeviceType GetType() const = 0;

==== MMCore.h
	private MM::Core* callback_;                 // core services for devices
	
	
---- CoreCallback?
CircularBuffer?

can use hacked MMCore or Core right away. need modification to use MMCore, only additional bindings
for Core. need additional c++ either way. config file routines are in MMCore.

can a friend read out private data? -> MMCore with no mods
	
can live without mods, with a state manager at EV side to select the right device all the time.
awful code, but no need to distribute special um.
	
	
stages: best if all related axis set at the same time, so locate all stages, with named axis, 
and map them into one super interface. if set value the same, this is the noop. 