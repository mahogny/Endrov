dat=importdata('/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost/data/numneigh.txt');

curframe=dat(:,1);
numcont=dat(:,2);

%temp, replace later. or? cell has been restricted more
%curframe does not match up between the two!!
dat=importdata('/Volumes/TBU_main02/ost4dgood/stdcelegansNew.ost/data/henriksson/volstats.txt');
numcell=dat(:,2);
g=length(numcell);

curframe=curframe(1:g);
numcont=numcont(1:g);

N=curframe;
X=numcont./numcell;

%TODO frame to time
%TODO shift in time

%write dat-file
%out=[N,X];
fp=fopen('series.dat','wt');
for i=1:length(N)
    fprintf(fp,'%f\t%f\n',N(i),X(i));
end
fclose(fp);