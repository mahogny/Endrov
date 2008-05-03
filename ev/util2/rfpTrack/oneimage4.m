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


im2 = imresize(v,1/scalefactor);
im2(im2<0)=0;

im(:,:,z)=im2;

