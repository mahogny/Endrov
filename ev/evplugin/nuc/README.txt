Nuclei model
======================================================================



metatype "nuclineage"

<nuc name="..." end=FRAME>
<pos f=FRAME x= y= z= EXTRA />  //unit [um]
.
.
.
<child name= />
</nuc>



A nucleus exists from min(f) to min(min(nuc.c.frame),nucend)

EXTRA includes apoptotic information. this is yet to be specified.