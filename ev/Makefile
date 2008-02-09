all:

backup:
	###!!!! FROM TO
	#./run.sh util.Backup /Volumes/TBU_xeon01_500GB01/final_recordings/ /Volumes/TBU_500GBusb01/final_recordings/ 

clean:
	rm -f binaries/VWB.app/Contents/Resources/Java/*

jni:
	javah

uploaddoc:
	scp -r ../javadoc mahogny@celegans.sh.se:~/Sites/

linecount:
	wc -l evgui/*.java evplugin/*/*.java evplugin/*/*.txt evplugin/*/cmd/*.java evplugin/roi/primitive/*.java evplugin/roi/window/*.java \
	evplugin/modelWindow/isosurf/*.java evplugin/modelWindow/voxel/*.java evplugin/modelWindow/slice3d/*.java

starters:
	jar cfvm startOSTdaemon.jar evgui/MANIFEST.STARTOST evgui/StartOSTdaemon.class OSTdaemon/*.class
	jar cfvm startEndrov.jar evgui/MANIFEST.STARTGUI evgui/StartGUI.class
	du -hc start*.jar

jnlp:
	jar cfvm ../ev.jar evgui/MANIFEST.WEBSTART evgui/*class evplugin/*/*class evplugin/*/*.png evplugin/*/cmd/*class evplugin/script/parser/*class evplugin/roi/primitive/*.class evplugin/roi/window/*.class \
			evplugin/modelWindow/isosurf/*.class evplugin/modelWindow/voxel/*.class evplugin/modelWindow/slice3d/*.class \
			evplugin/*/*.png evplugin/roi/window/*.png evplugin/roi/primitive/*.png \
			evplugin/ev/pluginlist.txt
	jarsigner -keystore myKeys ../ev.jar jdc
	du -hc ../ev.jar
	scp ../ev.jar tbuadmin@celegans.sh.se:~/MAMPsite/download/webstart/
	rm ../ev.jar


#keytool -genkey -keystore myKeys -alias jdc


hpj:	
#	java -agentlib:jmeter  -Dcom.apple.laf.useScreenMenuBar=true -Xdock:name=EV -cp .:libs/mac/postgresql-8.2-505.jdbc3.jar:libs/mac/gluegen-rt.jar:libs/mac/jogl.jar:libs/mac/sqlitejdbc-v033-native.jar:libs/jcommon-1.0.9.jar:libs/jfreechart-1.0.5.jar:libs/servlet.jar:libs/gnujaxp.jar:libs/itext-2.0.1.jar:libs/vecmath.jar:libs/junit.jar:libs/gnujaxp.jar:libs/jdom.jar:libs/jaxen-core.jar:libs/jaxen-jdom.jar:libs/saxpath.jar:libs/xalan.jar:libs/xerces.jar:libs/xml-apis.jar:libs/bio-formats.jar -Xmx700M -Djava.library.path=libs/mac/ evgui.GUI
	java -Xrunhprof:heap=all,cutoff=0  -Dcom.apple.laf.useScreenMenuBar=true -Xdock:name=EV -cp .:libs/mac/postgresql-8.2-505.jdbc3.jar:libs/mac/gluegen-rt.jar:libs/mac/jogl.jar:libs/mac/sqlitejdbc-v033-native.jar:libs/jcommon-1.0.9.jar:libs/jfreechart-1.0.5.jar:libs/servlet.jar:libs/gnujaxp.jar:libs/itext-2.0.1.jar:libs/vecmath.jar:libs/junit.jar:libs/gnujaxp.jar:libs/jdom.jar:libs/jaxen-core.jar:libs/jaxen-jdom.jar:libs/saxpath.jar:libs/xalan.jar:libs/xerces.jar:libs/xml-apis.jar:libs/bio-formats.jar -Xmx700M -Djava.library.path=libs/mac/ evgui.GUI

cpuprof:
	java -Xrunhprof:cpu=samples,thread=y,depth=12,cutoff=0  -Dcom.apple.laf.useScreenMenuBar=true -Xdock:name=EV -cp .:libs/mac/postgresql-8.2-505.jdbc3.jar:libs/mac/gluegen-rt.jar:libs/mac/jogl.jar:libs/mac/sqlitejdbc-v033-native.jar:libs/jcommon-1.0.9.jar:libs/jfreechart-1.0.5.jar:libs/servlet.jar:libs/gnujaxp.jar:libs/itext-2.0.1.jar:libs/vecmath.jar:libs/junit.jar:libs/gnujaxp.jar:libs/jdom.jar:libs/jaxen-core.jar:libs/jaxen-jdom.jar:libs/saxpath.jar:libs/xalan.jar:libs/xerces.jar:libs/xml-apis.jar:libs/bio-formats.jar -Xmx700M -Djava.library.path=libs/mac/ util.BenchmarkOST

memprof:
	java -Xrunhprof:heap=dump  -Dcom.apple.laf.useScreenMenuBar=true -Xdock:name=EV -cp .:libs/mac/postgresql-8.2-505.jdbc3.jar:libs/mac/gluegen-rt.jar:libs/mac/jogl.jar:libs/mac/sqlitejdbc-v033-native.jar:libs/jcommon-1.0.9.jar:libs/jfreechart-1.0.5.jar:libs/servlet.jar:libs/gnujaxp.jar:libs/itext-2.0.1.jar:libs/vecmath.jar:libs/junit.jar:libs/gnujaxp.jar:libs/jdom.jar:libs/jaxen-core.jar:libs/jaxen-jdom.jar:libs/saxpath.jar:libs/xalan.jar:libs/xerces.jar:libs/xml-apis.jar:libs/bio-formats.jar -Xmx700M -Djava.library.path=libs/mac/ evgui.GUI
#	java -Xrunhprof:heap=dump  -Dcom.apple.laf.useScreenMenuBar=true -Xdock:name=EV -cp .:libs/mac/postgresql-8.2-505.jdbc3.jar:libs/mac/gluegen-rt.jar:libs/mac/jogl.jar:libs/mac/sqlitejdbc-v033-native.jar:libs/jcommon-1.0.9.jar:libs/jfreechart-1.0.5.jar:libs/servlet.jar:libs/gnujaxp.jar:libs/itext-2.0.1.jar:libs/vecmath.jar:libs/junit.jar:libs/gnujaxp.jar:libs/jdom.jar:libs/jaxen-core.jar:libs/jaxen-jdom.jar:libs/saxpath.jar:libs/xalan.jar:libs/xerces.jar:libs/xml-apis.jar:libs/bio-formats.jar -Xmx700M -Djava.library.path=libs/mac/ util.BenchmarkOST

