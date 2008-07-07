#cflags = -I/usr/lib/jvm/java-6-sun-1.6.0.06/include/ -I/usr/lib/jvm/java-6-sun-1.6.0.06/include/linux/
cflags = -fPIC -I/usr/lib/jvm/java-6-sun-1.6.0.06/include/ -I/usr/lib/jvm/java-6-sun-1.6.0.06/include/linux/
lflags = -shared -Wl,-soname,libqhullj -Llinux32 -lqhull -o libqhullj.so

linux:
	gcc -g -c -std=c99 ${cflags} -Isrc/src myvoronoi.c
	gcc -g -c -std=c99 ${cflags} -Isrc/src user_eg.c
	gcc -g -c -std=c99 ${cflags} -Isrc/src user_eg2.c 
	gcc -g -c -std=c99 ${cflags} -Isrc/src user.c 
#	gcc -fPIC -g -c -std=c99 -I/usr/lib/jvm/java-6-sun-1.6.0.06/include/ -I/usr/lib/jvm/java-6-sun-1.6.0.06/include/linux/ -Isrc/src myvoronoi.c
	gcc myvoronoi.o user_eg.o ${lflags}
	gcc myvoronoi.o user_eg2.o -o testapp -Llinux32 -lqhull -lm
#-shared -Wl,-soname,libqhullj -o libqhullj.so myvoronoi.o -Llinux32 -lqhull

foo:
	gcc -dynamiclib -std=c99 -I/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Headers/ -Isrc/src -c myvoronoi.c -o libnativelib.dylib -Lmac -lqhull -static -lc 
#	gcc -Wall -o libnativelib.so -shared -Wl,-soname,libnative.so -I/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Headers/ -Isrc/src -std=c99 myvoronoi.c  -Lmac -lqhull -static -lc
#-shared

all:
	javah qhull.QHull

run:
	java -cp vecmath.jar:. Test
