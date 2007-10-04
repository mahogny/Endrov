all:

backup:
	#./run.sh util.Backup /Volumes/TBU_xeon01_500GB01/daemonoutput_mirror/ /Volumes/sata_mainshare/070727/daemonoutput_mirror/ > backuplog.txt
	#./run.sh util.Backup /Volumes/TBU_Maxtor/imageset/ /Volumes/sata_mainshare/070730/maxtor/imageset/ > backuplog.txt

	###!!!! FROM TO
	./run.sh util.Backup /Volumes/TBU_xeon01_500GB01/final_recordings/ /Volumes/TBU_500GBusb01/final_recordings/ 

clean:
	rm -f binaries/VWB.app/Contents/Resources/Java/*

jni:
	javah

uploaddoc:
	scp -r ../javadoc mahogny@celegans.sh.se:~/Sites/

linecount:
	wc -l evgui/*.java evplugin/*/*.java evplugin/*/*.txt

webstart:
#	jar cfvm ev.jar evgui/MANIFEST.WEBSTART evgui/*class evplugin/*/*class evplugin/*/*/*class
#	jar cfvm ev.jar evgui/MANIFEST.WEBSTART evgui/* evplugin/*/* evplugin/*/*/*
	jar cfvm ev.jar evgui/MANIFEST.WEBSTART evgui/*class evplugin/*/*class evplugin/*/cmd/*class evplugin/*/parser/*class evplugin/ev/pluginlist.txt
	du -hc ev.jar
