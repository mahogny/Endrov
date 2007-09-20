#!/bin/sh

#-agentlib:hprof
#-verbosegc

### General settings

memory=700M

### Detect OS
UNAME=$(uname)
if [[ "$UNAME" = Darwin ]]; then
	echo Mac/Darwin detected
	libdir='libs/mac/'
#	javaexe='java -Dcom.apple.macos.useScreenMenuBar=true -Xdock:name=run.sh'
	javaexe='java -Dcom.apple.laf.useScreenMenuBar=true -Xdock:name=run.sh'
else
	echo "Assuming Linux"
	libdir='libs/linux/'
	cmd='export LD_LIBRARY_PATH=libs/linux'
	echo $cmd
	$cmd
	javaexe='java'
fi


### Command to run java
runjava=\
$javaexe' -cp .:'$libdir'postgresql-8.2-505.jdbc3.jar:'$libdir'gluegen-rt.jar:'$libdir'jogl.jar:'$libdir'sqlitejdbc-v033-native.jar:'\
'libs/jcommon-1.0.9.jar:libs/jfreechart-1.0.5.jar:libs/servlet.jar:libs/gnujaxp.jar:libs/itext-2.0.1.jar:libs/vecmath.jar:'\
'libs/junit.jar:libs/gnujaxp.jar:libs/jdom.jar:libs/jaxen-core.jar:libs/jaxen-jdom.jar:libs/saxpath.jar:libs/xalan.jar:libs/xerces.jar:libs/xml-apis.jar:libs/bio-formats.jar'


### Run
if [ $# -eq 0 ]
then
        echo "Running default (GUI)"
        cmd=$runjava' -Xmx'$memory' -Djava.library.path='$libdir'  evgui.GUI'
else
        cmd=$runjava' -Xmx'$memory' -Djava.library.path='$libdir' '$*
fi
echo $cmd
$cmd
