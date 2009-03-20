function [err,curdy,curdx]=fit2(im1,im2)

s2=size(im2);
h2=s2(1);w2=s2(2);

s1=size(im1);
h1=s1(1);w1=s1(2);

cs=min([size(im1);size(im2)]);

%Optimization: trade some area to get a power-of-2 size. This means DFT can
%be replaced by much faster FFT later on.
%newcs=2.^floor(log(cs));
%newcs
%cs=newcs;

csy=cs(1);
csx=cs(2);


%Try all four corners. Assume no stupid cases (which would add 4 more)
%These additional cases are cheaper though, FFTs can be reused.

cases=[];
rx=(1-csx):0;
ry=(1-csy):0;

%For unequal sizes, have to test corners
part1=im1(   1:csy , 1:csx);
part2=im2(   h2+ry , w2+rx );  
dispx=w2-csx;
dispy=h2-csy;
[dx,dy,err]=trycorner(part1,part2)
cases=[cases;err,dy-dispy,dx-dispx,1];

part1=im1(   h1+ry  ,  w1+rx );
part2=im2(   1:csy  ,  1:csx ); 
dispx=w1-csx;
dispy=h1-csy;
[dx,dy,err]=trycorner(part1,part2)
cases=[cases;err,dy+dispy,dx+dispx,2]; 

part1=im1(   1:csy  ,  w1+rx );
part2=im2(   h2+ry  ,  1:csx ); 
dispx=w1-csx;
dispy=h2-csy;
[dx,dy,err]=trycorner(part1,part2)
cases=[cases;err,dy-dispy,dx+dispx,3]; 

part1=im1(   h1+ry  ,  1:csx );
part2=im2(   1:csy  ,  w2+rx ); 
dispx=w2-csx;
dispy=h1-csy;
[dx,dy,err]=trycorner(part1,part2)
cases=[cases;err,dy+dispy,dx-dispx,4]; 

cases=sortrows(cases,1);
curdy=cases(1,2);
curdx=cases(1,3);
err=cases(1,1);
