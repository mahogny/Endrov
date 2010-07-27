dat=load('/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost/data/numneigh.txt')
numcontact=dat(:,2);

dat2=load('/Volumes/TBU_main02/ost4dgood/stdcelegansNew.ost/data/henriksson/volstats.txt');
curframe=dat2(:,1); %only ok frames
numcell=dat2(:,2);

numcontact=numcontact(1:length(curframe));




plot(curframe,numcontact./numcell)

%jurgen cuts y. only show 3+ contacts?