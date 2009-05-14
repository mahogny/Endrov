dat=load('/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost/data/contactdurNEW2.txt')

dat=[dat(:,1)/60,dat(:,3)];

dat2=dat(dat(:,2)==1,1); %has child
dat3=dat(dat(:,2)==0,1); %no child

hist(dat(:,1),50)