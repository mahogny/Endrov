foo:
	gcc -dynamiclib -std=c99 -I/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Headers/ -Isrc/src -c myvoronoi.c -o libnativelib.dylib -Lmac -lqhull -static -lc 
#	gcc -Wall -o libnativelib.so -shared -Wl,-soname,libnative.so -I/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Headers/ -Isrc/src -std=c99 myvoronoi.c  -Lmac -lqhull -static -lc
#-shared

all:
	javah qhull.QHull

