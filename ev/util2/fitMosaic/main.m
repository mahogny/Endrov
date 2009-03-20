im1=imread('1.jpg');
im2=imread('2.jpg');
im1=im1(:,:,1);
im2=im2(:,:,1);

s2=size(im2);
h2=s2(1);w2=s2(2);

s1=size(im1);
h1=s1(1);w1=s1(2);

%figure(1)
%colormap('gray')
%imshow(part1)
%figure(2)
%colormap('gray')
%imshow(part2)

%size(im1)
%size(im2)

cs=min([size(im1);size(im2)]);
csy=cs(1);csx=cs(2);
diff=abs(size(im1)-size(im2));

%Try all four corners. Assume no stupid cases (which would add 4 more)

cases=[];
rx=(1-csx):0;
ry=(1-csy):0;

part1=im1(   1:csy    , 1:csx);
part2=im2(   h2+ry , w2+rx );  
dispx=w2-csx;
dispy=h2-csy;
[dx,dy,err]=trycorner(part1,part2,dispx,dispy)
cases=[cases;err,dy-dispy,dx-dispx,1];


part1=im1(   h1+ry  ,  w1+rx );
part2=im2(   1:csy  ,  1:csx ); 
dispx=w1-csx;
dispy=h1-csy;
[dx,dy,err]=trycorner(part1,part2,dispx,dispy)
cases=[cases;err,dy+dispy,dx+dispx,2]; 


part1=im1(   1:csy  ,  w1+rx );
part2=im2(   h2+ry  ,  1:csx ); 
dispx=w1-csx;
dispy=h2-csy;
[dx,dy,err]=trycorner(part1,part2,dispx,dispy)
cases=[cases;err,dy-dispy,dx+dispx,3]; 

part1=im1(   h1+ry  ,  1:csx );
part2=im2(   1:csy  ,  w2+rx ); 
dispx=w2-csx;
dispy=h1-csy;
[dx,dy,err]=trycorner(part1,part2,dispx,dispy)
cases=[cases;err,dy+dispy,dx-dispx,4]; 



%im2(x,y)=im1(x-dx,y-dy)  => im2(x+dx,y+dx)=im1(x,y)
%out=delta(x-dx,y-dy)


cases=sortrows(cases,1);
curdy=cases(1,2);
curdx=cases(1,3);

zerox=0;
zeroy=0;
collage=uint8([]);
%[zerox,zeroy,collage]=imcombine(im1,0,0,zerox,zeroy,collage);
%[zerox,zeroy,collage]=imcombine(im2,dx,dy,zerox,zeroy,collage);

[zerox,zeroy,collage]=imcombine(im1,0,0,zerox,zeroy,collage);
[zerox,zeroy,collage]=imcombine(im2,curdx,curdy,zerox,zeroy,collage);

figure(5)
imshow(collage)

cases
