dat=importdata('/Volumes/TBU_main02/ost4dgood/stdcelegansNew.ost/data/henriksson/volstats.txt');

curframe=dat(:,1);
numcell=dat(:,2);

N=curframe;
X=numcell;

%TODO frame to time

%write dat-file
%out=[N,X];
fp=fopen('series.dat','wt');
for i=1:length(N)
    fprintf(fp,'%f\t%f\n',N(i),X(i));
end
fclose(fp);