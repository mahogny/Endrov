all:
	jar -cfm LogoBar.jar logobar/MANIFEST.MF logobar/*.class

dist:
	cd ..;	zip logobar.zip logobar/*jar logobar/Makefile logobar/logobar/*{java,class} logobar/testdata/*aln
	mv ../logobar.zip ../release
