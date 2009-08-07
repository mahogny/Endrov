um/px  for everything
=> has to change resxy





tighter integration with image server to get things out like 
<Project
Name=" xsd:string [0..1]"
ID=" ProjectID [1]">
<Description> ... </Description> [0..1]
<ExperimenterRef> ... </ExperimenterRef> [0..1]
<GroupRef> ... </GroupRef> [0..1]
</Project>

because this should be in common for many images, so share reference?
* must also be possible to store in file
* must be editable in GUI





OME makes a difference between lot number and serial number






========================

purely informative types vs active types.
binning is informative, it will no longer be used in calculations


========================

unrestricted options.
e.g. LaserMedia is from a list. Endrov will not enforce list, rather it will offer a list
of premade values according to ome.

see http://cvs.openmicroscopy.org.uk/svn/specification/Documentation/Generated/OME-2008-09/ome.xsd.html
at the end for "simple types".


========================

screen plates well

http://www.ome-xml.org/wiki/ScreenPlateWell

* several reagent in one well
* one reagent can be in several wells
* one plate can be in several screens


all of this goes into a EvChannel, optionally. does not deal with changes over time,
Cytometry needs more stuff


spw:Plate
	ID	This is used by the system to identify the plate.
	Name	This is chosen by the user to identify the plate.
	Description	A free text description
	Status	The current state of the plate in the experiment work-flow.
	ExternalIdentifier 	This is a identifier for the plate used by an external system. This may be a barcode printed on the plate by the manufacturer.
spw:Reagent
	ID	This is used by the system to identify the reagent
	Description	A long description for the reagent
	Name	A short name for the reagent
	ReagentIdentifier	This is a reference to an external (to OME) representation of the Reagent. It serves as a foreign key into an external database.
spw:Screen
	ID	This is used by the system to identify the screen
	Name	This is chosen by the user to identify the screen
	ProtocolIdentifier	A pointer to an externally defined protocol, usually in a screening database.
	ProtocolDescription	A description of the screen protocol; may contain very detailed information to reproduce some of that found in a screening database.
	ReagentSetDescription	A description of the set of reagents; may contain very detailed information to reproduce some of that information found in a screening database.
	ReagentSetIdentifier	A pointer to an externally defined set of reagents, usually in a screening database/automation database.
	Type	A human readable identifier for the screen type; e.g. RNAi, cDNA, SiRNA, etc.
spw:Well		A Well is a component of the Well/Plate/Screen construct to describe screening applications. A Well has a number of WellSample elements that link to the Images collected in this well. The ReagentRef links any Reagents that were used in this Well. A well is part of one or more Plates. The origin for the row and column identifiers is the top left corner of the plate starting at zero.
	ID
	Column	This is the column index of the well, the origin is the top left corner of the plate with the first column of cells being column zero. i.e top left is (0,0)
	ExternalDescription	A description of the externally defined identifier for this plate.
	ExternalIdentifier	A pointer to an externally defined identifier for this plate.
	Row	This is the row index of the well, the origin is the top left corner of the plate with the first row of wells being row zero. i.e top left is (0,0)
	Type	A human readable identifier for the screening status. e.g. empty, positive control, negative control, control, experimental, etc. This string is likely to become an enumeration in future releases.
	--[JH] type belongs in an external database

/*

redundant

WellSample		WellSample is an individual image that has been captured within a Well.
	ID
	PosX	The X position of the image within the well
	PosY	The Y position of the image within the well
	Timepoint	The time-point at which the image started to be collected 

redundant

ScreenAcquisition		ScreenAcquisition is used to describe a single acquisition run for a screen. Since Screens are abstract, this object is used to record the set of images acquired in a single acquisition run. The Images for this run are linked to ScreenAcquisition through WellSample.
	ID
	EndTime	Time when the last image of this acquisition was collected
	StartTime	Time when the first image of this acquisition was collected
*/





========================

type ostblobid=...
  data/ <--- keep everything here. allows subobjects to be loaded, but this object skipped. no interference with future schema
  subobjects/
    ....

    
imageset:
  [Experimenter]
  [Filter]
  Group
  Instrument?
  [Laser]
  [Lightsource]
  [Filament]
  [Arc]
  [LightEmittingDiode]
  Microscope
  [Objective]
  [Project]
    
evchannel:
  overridable {
  resx, resy, resz, dispx, dispy, dispz
  informativeBinning
  ObjectiveRef, *Ref <----- collect details from parent
  <SPW tags>
  }
  
  frame f=.../       actual time, EvDecimal
  	{override if needed}
    slice i=.../     index into z
      {override if needed}
  
  

    