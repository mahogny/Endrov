#main package

#should be executed on a release instead?


ENDROV=../..
ROOT=../../../debmainroot
rm -Rf $ROOT
mkdir $ROOT

##### Meta information
mkdir $ROOT/DEBIAN
cp copyright.txt $ROOT/DEBIAN/copyright
cp control.main $ROOT/DEBIAN/control
echo "#!/bin/sh" > $ROOT/DEBIAN/postinst
chmod 0555 $ROOT/DEBIAN/postinst

##### Programs
mkdir $ROOT/usr/
mkdir $ROOT/usr/bin/
echo "#!/bin/sh" > $ROOT/usr/bin/endrov
chmod 0555 $ROOT/usr/bin/endrov

##### JAR-files
mkdir $ROOT/usr/share/
mkdir $ROOT/usr/share/endrov/

##### All dependencies

##### Put together
cd ..
dpkg-deb -b debmainroot
mv debmainroot.deb endrov.deb



.
|-- DEBIAN
|   |-- control
|   |-- postinst
|   `-- postrm
`-- usr
    |-- bin
		    endrov <shell script>
    `-- share
        |-- doc
        |   `-- endrov
        |       |-- copyright
        |-- endrov
				... <as it is now, but less plugins>
        |-- java
        		<nothing>, but depend on other libraries
        |-- man
        |   `-- man1
        |       `-- endrov.1.gz
        |-- menu
        |   `-- endrov
        `-- pixmaps
            `-- endrov.xpm
   home
      foo
         .endrov         
            
