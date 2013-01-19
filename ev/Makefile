all:
	javac -cp `java -jar startEndrov.jar --printcp` \
	endrov/*/*.java \
	endrov/*/*/*.java \
	endrov/*/*/*/*.java \
	util/*.java \
	util/*/*.java
#will not work if any of the files fail. have to exclude mac here!


gitaddall:
	git add \
	endrov/*/*.java \
	endrov/*/*/*.java \
	endrov/*/*/*/*.java \
	util/*.java \
	util/*/*.java \
	util2/*/*.java \
	util2/*/*/*.java \
	util2/*/*/*/*.java \
	endrov/*/*.png \
	endrov/*/*/*.png \
	endrov/*/*.txt \
	endrov/*/*/*.txt \
	endrov/*/*/*.glsl \
	endrov/*/*.png \
	endrov/*/*/*.png

backup:
	###!!!! FROM TO
	#./run.sh util.Backup /Volumes/TBU_xeon01_500GB01/final_recordings/ /Volumes/TBU_500GBusb01/final_recordings/ 

clean:
	rm -f binaries/VWB.app/Contents/Resources/Java/*

jni:
	javah

uploaddoc:
	scp -r ../javadoc mahogny@celegans.sh.se:~/Sites/

starters:
	jar cfvm startOSTdaemon.jar OSTdaemon/MANIFEST.STARTOST OSTdaemon/*.class endrov/core/EvBuild.class
	jar cfvm startEndrov.jar endrov/starter/MANIFEST.STARTGUI endrov/starter/*.class endrov/core/EvBuild.class
	jar cfvm startViewer.jar endrov/starter/MANIFEST.STARTCEV endrov/starter/*.class endrov/core/EvBuild.class
	du -hc start*.jar

#macstarters:
	#should only be run on a mac
#	java -jar startEndrov.jar --macstarter

macplugin:
	jar cfv libs/bin_mac/macbinding.jar endrov/macBinding/*.class

#applet:
#	jar cfv evapplet/applet.jar evapplet/*.class
#	#jarsigner -keystore util/myKeys evapplet/applet.jar jdc
#	#keytool -export -keystore myKeys -alias jdc -file evapplet/johanhenriksson.cer



#### TODO: makerelease should handle this
#run in unzipped dist, with mykey
totaljar:
	jar cfvm ../ev.jar endrov/starter/MANIFEST.WEBSTART evplugin/ev/pluginlist.txt \
		*/*class */*/*class */*/*/*class \
		*/*/*png */*/*/*png \
		*/*/*/*glsl 
	jarsigner -keystore util/myKeys ../ev.jar jdc
	du -hc ../ev.jar

jnlp:	totaljar
	scp ../ev.jar tbuadmin@celegans.sh.se:~/MAMPsite/download/webstart/
	rm ../ev.jar


#sevenjar:
#	jar cfv sevenzip.jar */*class */*/*/*class

#keytool -genkey -keystore util/myKeys -alias jdc


hpj:	
#	java -agentlib:jmeter  -Dcom.apple.laf.useScreenMenuBar=true -Xdock:name=EV -cp .:libs/mac/postgresql-8.2-505.jdbc3.jar:libs/mac/gluegen-rt.jar:libs/mac/jogl.jar:libs/mac/sqlitejdbc-v033-native.jar:libs/jcommon-1.0.9.jar:libs/jfreechart-1.0.5.jar:libs/servlet.jar:libs/gnujaxp.jar:libs/itext-2.0.1.jar:libs/vecmath.jar:libs/junit.jar:libs/gnujaxp.jar:libs/jdom.jar:libs/jaxen-core.jar:libs/jaxen-jdom.jar:libs/saxpath.jar:libs/xalan.jar:libs/xerces.jar:libs/xml-apis.jar:libs/bio-formats.jar -Xmx700M -Djava.library.path=libs/mac/ evgui.GUI
	java -Xrunhprof:heap=all,cutoff=0  -Dcom.apple.laf.useScreenMenuBar=true -Xdock:name=EV -cp .:libs/mac/postgresql-8.2-505.jdbc3.jar:libs/mac/gluegen-rt.jar:libs/mac/jogl.jar:libs/mac/sqlitejdbc-v033-native.jar:libs/jcommon-1.0.9.jar:libs/jfreechart-1.0.5.jar:libs/servlet.jar:libs/gnujaxp.jar:libs/itext-2.0.1.jar:libs/vecmath.jar:libs/junit.jar:libs/gnujaxp.jar:libs/jdom.jar:libs/jaxen-core.jar:libs/jaxen-jdom.jar:libs/saxpath.jar:libs/xalan.jar:libs/xerces.jar:libs/xml-apis.jar:libs/bio-formats.jar -Xmx700M -Djava.library.path=libs/mac/ evgui.GUI

cpuprof:
	java -Xrunhprof:cpu=samples,thread=y,depth=12,cutoff=0  -Dcom.apple.laf.useScreenMenuBar=true -Xdock:name=EV -cp .:libs/mac/postgresql-8.2-505.jdbc3.jar:libs/mac/gluegen-rt.jar:libs/mac/jogl.jar:libs/mac/sqlitejdbc-v033-native.jar:libs/jcommon-1.0.9.jar:libs/jfreechart-1.0.5.jar:libs/servlet.jar:libs/gnujaxp.jar:libs/itext-2.0.1.jar:libs/vecmath.jar:libs/junit.jar:libs/gnujaxp.jar:libs/jdom.jar:libs/jaxen-core.jar:libs/jaxen-jdom.jar:libs/saxpath.jar:libs/xalan.jar:libs/xerces.jar:libs/xml-apis.jar:libs/bio-formats.jar -Xmx700M -Djava.library.path=libs/mac/ util.BenchmarkOST

memprof:
	java -Xrunhprof:heap=dump  -Dcom.apple.laf.useScreenMenuBar=true -Xdock:name=EV -cp .:libs/mac/postgresql-8.2-505.jdbc3.jar:libs/mac/gluegen-rt.jar:libs/mac/jogl.jar:libs/mac/sqlitejdbc-v033-native.jar:libs/jcommon-1.0.9.jar:libs/jfreechart-1.0.5.jar:libs/servlet.jar:libs/gnujaxp.jar:libs/itext-2.0.1.jar:libs/vecmath.jar:libs/junit.jar:libs/gnujaxp.jar:libs/jdom.jar:libs/jaxen-core.jar:libs/jaxen-jdom.jar:libs/saxpath.jar:libs/xalan.jar:libs/xerces.jar:libs/xml-apis.jar:libs/bio-formats.jar -Xmx700M -Djava.library.path=libs/mac/ evgui.GUI
#	java -Xrunhprof:heap=dump  -Dcom.apple.laf.useScreenMenuBar=true -Xdock:name=EV -cp .:libs/mac/postgresql-8.2-505.jdbc3.jar:libs/mac/gluegen-rt.jar:libs/mac/jogl.jar:libs/mac/sqlitejdbc-v033-native.jar:libs/jcommon-1.0.9.jar:libs/jfreechart-1.0.5.jar:libs/servlet.jar:libs/gnujaxp.jar:libs/itext-2.0.1.jar:libs/vecmath.jar:libs/junit.jar:libs/gnujaxp.jar:libs/jdom.jar:libs/jaxen-core.jar:libs/jaxen-jdom.jar:libs/saxpath.jar:libs/xalan.jar:libs/xerces.jar:libs/xml-apis.jar:libs/bio-formats.jar -Xmx700M -Djava.library.path=libs/mac/ util.BenchmarkOST

githash:
	echo `git rev-parse HEAD` > endrov/core/githash.txt

