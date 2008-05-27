load ('/Volumes/TBU_main03/ost4dgood/AnglerUnixCoords/data/newneigh.mat')
names1=names;
ncount1=ncount;

load ('/Volumes/TBU_main03/ost4dgood/N2_071116/data/newneigh.mat')
names2=names;
ncount2=ncount;

load ('/Volumes/TBU_main03/ost4dgood/TB2167_0804016/data/newneigh.mat')
names3=names;
ncount3=ncount;


for i=1:length(names1);
    fn=strcat(names1(i),'_neigh.htm');
    f=fopen(fn,'w')
    fprintf(f,'<html><body><h1>%s</h1><table border="1">',names1(1);
    if ncount1>1
        fprintf
    end
    fprintf(f,'</table></body></html>');
    close(f)
end
