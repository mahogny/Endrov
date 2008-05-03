d=vox(:,:,z);

im = imresize(d,scalefactor);



%find local maximas
w=size(im,2);
h=size(im,1);
maximas=[];
for y=2:(h-1)
    for x=2:(w-1)
        if im(y,x)>im(y,x-1) && im(y,x)>im(y,x+1) && im(y,x)>im(y-1,x) && im(y,x)>im(y+1,x)
            maximas=[maximas;x,y,z,im(y,x)];
        end
        
        
    end
end

maximas=sortrows(maximas,4);

%magical cut-off used but I think one can do without it.

for i=floor(size(maximas,1)*0.85):size(maximas,1)
    im(maximas(i,2),maximas(i,1))=255;
end

%%

%scalefactor2=1/4;
%im2=imresize(d,scalefactor2);

%h = fspecial('log',[7 7], 0.5);
%ed = imfilter(im2*20,h)
%ed(ed<0)=0;
%image(ed)

num_iter = 10;
delta_t = 1/7;
kappa = 30;
option = 2;
d2 = anisodiff2D(d,num_iter,delta_t,kappa,option);

scalefactor2=1/4;
im2=imresize(d2,scalefactor2);
im4=imresize(d,scalefactor2);


image(im2*5)

%%

ha = fspecial('average',5);
ed=ed-imfilter(ed,ha);

%%

h = fspecial('laplacian',1);
ed = imfilter(im2,h);
sed=size(ed);
ed=ed(10:(sed(1)-10), 10:(sed(2)-10));
im3=im2(10:(sed(1)-10), 10:(sed(2)-10));
%ed=abs(ed);
%ed(ed<0)=0;

h = fspecial('gaussian',3,1);
ed = imfilter(ed,h);

image(ed*100)

%can take all minimas here as seeds, or try to fill and see how large it
%would be



