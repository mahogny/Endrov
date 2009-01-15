#!/bin/bash

#using ls * is not the optimal way of starting up, it slows down startup

java -jar /usr/share/endrov/startEndrov.jar endrov.starter.MW \
	--basedir /usr/share/endrov \
	--cp2 $HOME/.endrov/ \
	--javaenv $HOME/.endrov/javaenv.txt --libpath2 /usr/lib/jni \
	$@

#	--config $HOME/.endrov/config.xml \

#	--cp2 `ls -d -1 /usr/share/java/*.jar | tr '\n' ':'`$HOME/.endrov/ \

#* startEndrov needs to know basedir to find jars
#* user needs a private plugin directory for development. this is .endrov.
#* the java registry doesn't work well on linux so a config file should
#  always be used
#* -arguments go to JVM, -- to main()


#TODO with next version of Imserv, java.policy can be removed (needs basedir)

#http://bugs.debian.org/cgi-bin/bugreport.cgi?bug=212863

