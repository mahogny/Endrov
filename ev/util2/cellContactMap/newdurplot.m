dat=load('/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost/data/contactdurNEW2.txt');

hist(dat(:,1)/60,50)
%4331 wrong!

dat2=dat(:,1)/60;

length(dat)
length(dat2(dat2>2.5))
% 3473
% 3053


%dat=[dat(:,1)/60,dat(:,3)];

%dat2=dat(dat(:,2)==1,1); %has child
%dat3=dat(dat(:,2)==0,1); %no child

%figure(2)
%hist(dat2,50);

%figure(3)
%hist(dat3,50);


X=1:3:70;
N=hist(dat2,X);

%write dat-file
fp=fopen('newdurhist.dat','wt');
for i=1:length(N)
	if mod(i,2)==1
	    fprintf(fp,'%f\t%d\n',N(i),X(i));
    else
	    fprintf(fp,'%f\t\n',N(i));    	
    end
end
fclose(fp);