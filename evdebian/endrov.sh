#!/bin/bash

#* should copy java env here

java -jar /usr/share/endrov/startEndrov.jar \
	--basedir /usr/share/endrov
	--config $(HOME)/.endrov/config.xml \
	--cp2 $(HOME)/.endrov/ \
	--cp2 `build-classpath DEBPACKAGES` \
	--javaenv $(HOME)/.endrov/javaenv.txt \
	$@
	

#* startEndrov needs to know basedir to find jars
#* user needs a private plugin directory for development. this is .endrov.
#* the java registry doesn't work well on linux so a config file should
#  always be used
#* -arguments go to JVM, -- to main()


#TODO put MMConfig.cfg in a sensible location, probably in .endrov/

#TODO with next version of Imserv, java.policy can be removed (needs basedir)

#TODO what about env file?

#http://bugs.debian.org/cgi-bin/bugreport.cgi?bug=212863

