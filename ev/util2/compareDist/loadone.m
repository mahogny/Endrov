function distarr=loadone(path)


%subset=[1,4,8,3,9,6,8];
distarr=load([path, '/data/neighdist.txt']);

nume=size(distarr,1);

onedistarr=reshape(distarr,1,nume^2);
%hist(reshape(distarr,1,575^2),200)

%cutoff=10;
cutoff=prctile(onedistarr(onedistarr>0),10);

%distarr(distarr>cutoff)=0;




