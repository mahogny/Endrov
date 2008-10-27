%set to 1 once we use OST4
timestep=10; %[s]


dat=importdata('/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost/data/traveldist.txt');
%tstart=dat.data(:,1)/timestep;
%tend=dat.data(:,2)/timestep;
dstraight=dat.data(:,3);
dfractal=dat.data(:,4);
rav=dat.data(:,5);
lifedev=dat.data(:,6); %already in %?
%/timestep;


%%

lifedev=lifedev(lifedev~=0);
%hist(lifedev,15)

bins=linspace(0,0.4,15);
%[N,X]=hist(dstraight);
[N,X]=hist(lifedev,bins);
fp=fopen('divdevhist.dat','wt');
for i=1:length(N)
    fprintf(fp,'%f\t%f\n',N(i),X(i));
end
fclose(fp);
