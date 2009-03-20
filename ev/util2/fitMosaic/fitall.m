im1=imread('1.jpg');
im2=imread('2.jpg');
im1=im1(:,:,1);
im2=im2(:,:,1);

[err,curdy,curdx]=fit2(im1,im2);

zerox=0;
zeroy=0;
collage=uint8([]);
[zerox,zeroy,collage]=imcombine(im1,0,0,zerox,zeroy,collage);
[zerox,zeroy,collage]=imcombine(im2,curdx,curdy,zerox,zeroy,collage);


imshow(collage)
