#!/bin/bash
cd ppc
for file in `ls mmgr* *jnilib *dylib` ; do
	lipo "$file" ../x86/"$file" -create -output ../"$file"
	echo "$file" 
done
cd ..

cp ppc/libk8055.dylib x86/libquaqua.jnilib ppc/libusb.dylib ppc/libusbpp.dylib x86/*jar .
