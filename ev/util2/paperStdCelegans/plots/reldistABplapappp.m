%only recording that has it 2008-11-18
%/Volumes/TBU_main03/ost4dgood/TB2167_080409b.ost/rmd.ostxml

posEndABpla=[28.705563792067963 28.05062095388929 11.0];
posStartABplapappp=[61.53660702323187 38.61277755779487 9.0];
posEndABplapappp  =[64.38860172963048 41.24538805600898 17.0];

posStartABplapapp=[44.47948483688589,29.398640814045265,5.5];
posEndABplapapp  =[57.97161364023321,38.28370124551796,7.5];

d1=norm(posEndABpla-posEndABplapappp);
d2=norm(posEndABplapappp-posStartABplapappp);
d3=norm(posEndABplapapp-posStartABplapapp);

d2/d1  %23%
d3/d1  %42%


%of some reason this cell is missing in our recompressed schnabel
%this is from /Volumes/TBU_main02/ostxml/N2wormbas_modified.xml

posEndABpla=[13.2 11.6 9.6];
posStartABplapappp=[49.7 34.1 11.2];
posEndABplapappp=[51.6 30.1 20.8];

posStartABplapapp=[33.2,28.3,3.2];
posEndABplapapp  =[44.0,32.3,4.8];


d1=norm(posEndABpla-posEndABplapappp);
d2=norm(posEndABplapappp-posStartABplapappp);
d3=norm(posEndABplapapp-posStartABplapapp);
d2/d1 %24%
d3/d1 %26%
