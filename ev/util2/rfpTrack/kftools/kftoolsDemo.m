%This script gives an example of the usage of all of the functions
%in the KFtools toolbox. Note that the MEX files must be created
%before the script is run. This means that
%
%palettemex.c
%watershedmex.c
%getboundarymex.c
%centroidmex.c
%selectobjectmex.c
%
%must be compiled e.g. mex c:\dirname\palettemex.c at the MATLAB
%command prompt will create the MEX file palettemex.dll on a
%Windows system, where dirname is the directory in which
%palettemex.c is contained.
%
%The script makes use of the image file blob.tif which must be located
%by the user. The gradient image is calculated and used in conjunction with 
%a marker image as input to the Watershed Transform. This is used to identify
%regions one and two. The boundary coordinates of region one are
%located and then plotted. The centroid of the blob is located and
%plotted.The coordinates are written to file in a space efficient
%format and then read from file. The object is then split in two to 
%demonstrate the use of selectobjectmex to select the larger portion.
%
%Keith Forbes
%keith@umpire.com

clear
close all
disp('Please find the file blob.tif')
[filename, pathname] = uigetfile('blob.tif', 'Please find the file blob.tif');
if (filename~='blob.tif')
   error('Incorrect image')
end


disp('Loading the image...')
im=imread([pathname filename]);
if (size(im)~=[50 50])
   error('Incorrect image')
end


figure(1);
if exist('imshow')
   imshow(im);
else
   image(repmat(im,[1 1 3]))
end

set(1,'Name','Original Image')


disp('Determining the gradient image...')
[gx,gy]=gradient(double(im));
g=sqrt(gx.^2+gy.^2);

%g=round(255*(g-min(min(g)))/max(max(g))); %create a standard 8-bit grey-scale gradient image
g = round( 255 * (g - min(g(:) ) ) / ( max(g(:) - min(g(:)) ) ) ); %create a standard 8-bit grey-scale gradient image


figure(2);
if exist('imshow')
   imshow(uint8(g));
else
   image(repmat(uint8(g),[1 1 3]))
end

set(2,'Name','Gradient Image')


disp('Creating a marker image...')
marker=zeros(size(g));
marker(20:23,20:22)=1;
marker(2:4,4:43)=2; %create a marker image indicating two regions
marker(42:44,44:46)=2; %flood sources from the same regions need not be connected

figure(3);

if exist('imshow')
   imshow(palettemex(marker));
else
   image(palettemex(marker))

end
set(3,'Name','Marker Image')


%g=imresize(g,8);
%marker=imresize(marker,8);

disp('Applying watershed segmentation by flooding from marked sources...')
imout=watershedmex(g,marker);

figure(4);

if exist('imshow')
   imshow(palettemex(imout));
else
   image(palettemex(imout))
end
set(4,'Name','Two Regions Located')


disp('Forming the foreground-background boundary polygon...')
p=getboundarymex(imout==1); %get the boundary of the object consisting of a region of ones

figure(5);
hold on;
plot([p(:,1) ; p(1,1)],[p(:,2) ; p(1,2)],'r')
axis equal
axis ij

disp('Determining the image foreground centroid...')
z=centroidmex(imout==1);

plot(real(z),imag(z),'g*');
set(5,'Name','Boundary and Centroid')


bndfilename='p.bnd';

disp(['Writing the polygon to file ' bndfilename '...'])
bndwrite(p,bndfilename);

disp(['Reading the polygon from the file ' bndfilename '...'])
q=bndread(bndfilename);


figure(6);
plot([q(:,1) ; q(1,1)],[q(:,2) ; q(1,2)],'b')
axis equal
axis ij
set(6,'Name','Boundary Read from File')


disp('Splitting the object in two...')
bw2=(imout==1);
bw2(25,:)=0; %Split the object in two


figure(7);

if exist('imshow')
   imshow(bw2);
else
   image(repmat(bw2,[1 1 3]))
end


set(7,'Name','Object Split')


disp('Selecting the larger portion of the object...')
bwBig=selectobjectmex(bw2,1); %select the largest portion
figure(8);
if exist('imshow')
   imshow(bwBig);
else
   image(repmat(bwBig,[1 1 3]))
end
set(8,'Name','Larger Portion')
