

z=14;

thresholdcolor=30;


am=squeeze(mean(shiftdim(vox,2)));

%This deletes spatially varying background
v=((vox(:,:,z)-am)*5);


%Background is rather poisson distributed (looks that way in graphs and can
%be physically motivated). mean lambda. can be approximated by median which
%is stable from the data ("outliers")
%http://en.wikipedia.org/wiki/Poisson_distribution

pixels=reshape(v,prod(size(v)),1);
pixelsv=pixels;
%v=v-median(pixels);

%Might as well cut away more background. cannot say anything anyway based
%on such low values.
v=v-prctile(pixels,70);



%%
%negv=v;
v(v<0)=0;
ha=fspecial('average',25);
v=v-imfilter(v,ha);


%%

%v(v<0)=0; %can be good to save this until after mean value; will it delete more pixels?


scalefactor=2; %6; %8

im = imresize(v,1/scalefactor);
im(im<0)=0;

%find local maximas
[h,w]=size(im);
maximas=[];
for y=2:(h-1)
    for x=2:(w-1)
        %Note the strict >. It removes all background candidates because
        %these are all 0 and only >=.
        %Update: only in the simpler cases (early stage)
        if im(y,x)>thresholdcolor && im(y,x)>im(y,x-1) && im(y,x)>im(y,x+1) && im(y,x)>im(y-1,x) && im(y,x)>im(y+1,x)
            maximas=[maximas;x,y,z,im(y,x)];
        end
    end
end

maximas=sortrows(maximas,4);

image(im)

%%

%magical cut-off used but I think one can do without it.

%Can obtain an unbiased image for adjusting position
v2=vox(:,:,z);
im = imresize(v2,1/scalefactor);
wsize=0;
[h,w]=size(im);
for i=1:size(maximas,1)
    x=maximas(i,1);
    y=maximas(i,2);
    
%     [px,py]=meshgrid(max(1,x-wsize):min(w,x+wsize), ...
%         max(1,y-wsize):min(h,y+wsize));
%     subim=im(py(:,1),px(1,:));
%     totsub=sum(sum(subim));    
%    maximas(i,1)=x+sum(sum(subim.*(px-x)))/totsub;
 %   maximas(i,2)=y+sum(sum(subim.*(py-y)))/totsub;
    
 %this code might not work as well as it once did   
 
    px=[-1:1;-1:1;-1:1];
    py=px';
    subim=im((y-1):(y+1),(x-1:x+1));
    totsub=sum(sum(subim));
    %maximas(i,1)=x+sum(sum(subim.*px))/totsub;
    %maximas(i,2)=y+sum(sum(subim.*py))/totsub;
    
end
    

maximas(:,1:2)=maximas(:,1:2)*scalefactor;

%%

scalefactor=2;
im = imresize(v,1/scalefactor);


for i=1:size(maximas,1)
%for i=floor(size(maximas,1)*0.85):size(maximas,1)
    ty=round(maximas(i,2)/scalefactor);
    tx=round(maximas(i,1)/scalefactor);
    if tx>0 && ty>0 && tx<size(im,2) && ty<size(im,1)
        im(ty,tx)=256;
    end
end
image(im)

%pixels=reshape(im,prod(size(im)),1);
%hist(pixels)

