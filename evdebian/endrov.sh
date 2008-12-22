#!/bin/bash

#using ls * is not the optimal way of starting up, it slows down startup

java -jar /usr/share/endrov/startEndrov.jar endrov.starter.MW \
	--basedir /usr/share/endrov \
	--config $HOME/.endrov/config.xml \
	--cp2 `ls -d -1 /usr/share/java/*.jar | tr '\n' ':'`$HOME/.endrov/ \
	--javaenv $HOME/.endrov/javaenv.txt \
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

