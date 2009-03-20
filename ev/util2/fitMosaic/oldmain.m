im1=imread('1.jpg');
im2=imread('2.jpg');
im1=im1(:,:,1);
im2=im2(:,:,1);

%figure(1)
%colormap('gray')
%imshow(part1)
%figure(2)
%colormap('gray')
%imshow(part2)

%size(im1)
%size(im2)

sw=min([size(im1);size(im2)]);

part1=im1(1:sw(1),1:sw(2));
part2=im2(1:sw(1),1:sw(2));

fim1=fft2(part1);
fim2=fft2(part2);

v=fim1.*conj(fim2);
v=v./abs(v);

out=ifft2(v);

[minrow,rowi] = max(out);
[mincol,coli] = max(minrow);

dy=rowi(coli);
dx=coli;

%im2(x,y)=im1(x-dx,y-dy)  => im2(x+dx,y+dx)=im1(x,y)
%out=delta(x-dx,y-dy)


zerox=0;
zeroy=0;
collage=uint8([]);
[zerox,zeroy,collage]=imcombine(im1,0,0,zerox,zeroy,collage);
[zerox,zeroy,collage]=imcombine(im2,dx,dy,zerox,zeroy,collage);
imshow(collage)


