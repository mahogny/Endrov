

fp=fopen('/Volumes/TBU_main02/ost4dgood/N2_071116/data/neighname.txt','r');
name={};
k=1;
while ~feof(fp)
    s=fgets(fp);
    name{k}=s(1:(length(s)-1));
    k=k+1;
end
fclose(fp);


arr1=loadone('/Volumes/TBU_main02/ost4dgood/N2_071116');
arr2=loadone('/Volumes/TBU_main03/ost4dgood/AnglerUnixCoords');
arr3=loadone('/Volumes/TBU_main03/ost4dgood/TB2167_0804016');

arr2=arr2/7.4579;

mean(mean(arr1))
mean(mean(arr2))

% 
% %subset=[1,4,8,3,9,6,8];
% distarr=load(/data/neighdist.txt');
% 
% onedistarr=reshape(distarr,1,575^2);
% hist(reshape(distarr,1,575^2),200)
% 
% %cutoff=10;
% cutoff=prctile(onedistarr(onedistarr>0),10);
% 
% distarr(distarr>cutoff)=0;
% 
% image(distarr)
% 
% %image(distarr(subset,subset))

colormap('gray')

clear totarr
totarr(:,:,1)=arr1;
totarr(:,:,2)=arr2;
totarr(:,:,3)=arr3;
for i=1:3
    for j=1:3
        subplot(3,3,(i-1)*3+j);
        image(abs(totarr(:,:,i)-totarr(:,:,j)));
    end
end

subplot(2,3,1);
image(abs(arr1-arr2));
set(gca,'XTickLabel',name);set(gca,'YTickLabel',name);
subplot(2,3,2);
image(abs(arr1-arr3));
set(gca,'XTickLabel',name);set(gca,'YTickLabel',name);
subplot(2,3,3);
image(abs(arr2-arr3));
set(gca,'XTickLabel',name);set(gca,'YTickLabel',name);

subplot(2,3,4);
image(q(arr1,arr2));
set(gca,'XTickLabel',name);set(gca,'YTickLabel',name);
subplot(2,3,5);
image(q(arr1,arr3));
set(gca,'XTickLabel',name);set(gca,'YTickLabel',name);
subplot(2,3,6);
image(q(arr2,arr3));
set(gca,'XTickLabel',name);set(gca,'YTickLabel',name);

%%
% 
subplot(1,1,1);
image(q(arr1,arr2));
set(gca,'XTickLabel',name);set(gca,'YTickLabel',name);
set(gca,'XTick',1:1:length(name));
set(gca,'YTick',1:1:length(name));
%set(get(gca,'XTickLabel'),'Rotation',90.0)
xticklabel_rotate([1:length(name)],90,name,'interpreter','none')


