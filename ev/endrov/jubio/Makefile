############ For dynamic linking
#LFLAGS = -L../../lib/jubio/ -ljubio \
#	/sw/lib/static/libtiff.a /sw/lib/static/libjpeg.a -lz \
#	-framework Carbon -framework QuickTime \


LFLAGS = -L../../../lib/jubio/ -ljubio \
	libtiff.a libjpeg.a -lz \
	-framework Carbon -framework QuickTime \


############ For static linking
#LFLAGS = -L../../lib/jubio/ -ljubio \
#	../../lib/macos/libtiff.a  ../../lib/macos/libjpeg.a -lz \
#	-framework Carbon -framework QuickTime \


############ Common

INCLUDE = -I../../../lib/jubio/ \
	-I/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Headers \
	-I/sw/include

CFLAGS = -Wall -g -O3 -arch i686 -arch ppc


all:
	g++ native.cpp -bundle -o ../../libjubio.jnilib $(CFLAGS) $(LFLAGS) $(INCLUDE)


jni:
	javah -classpath ../.. evplugin.jubio.Jubio

clean:
	rm *.class evplugin_jubio_Jubio.h *.o

tiff:
	lipo -create -o libtiff.a libtiff386.a libtiffmac.a 
