#Prepare the app for release. Run from ./

#decide on a name
name="ev"`date +%g%m%d`
echo $name

cd ..
cd ..
cp -r ev $name
cd $name

#private
rm -Rf evplugin2 util2 myKeys   

#make sure these are up to date
make starters

#remove crap
rm -Rf CVS */CVS */*/CVS */*/*/CVS */*/*/*/CVS
rm -Rf .cvsignore */.cvsignore */*/.cvsignore */*/*/.cvsignore */*/*/*/.cvsignore
rm -Rf .project .classpath .metadata .settings
rm *.log

#compress
cd ..
zip -r $name $name
mv $name.zip release/

#linecount
wc -l $name/*/*.java $name/*/*/*.java $name/*/*/*/*.java  $name/*/*/*/*.glsl
du -hc --max-depth=2 $name

#delete
rm -Rf $name
