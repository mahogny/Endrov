function [dx,dy,err]=trycorner(part1,part2)
%Displacement of part2 relative to part1 
%Parts must be of equal size

fim1=fft2(part1);
fim2=fft2(part2);
v=fim1.*conj(fim2);
v=v./abs(v);
out=ifft2(v);

[minrow,rowi] = max(out);
[mincol,coli] = max(minrow);

dy=rowi(coli);
dx=coli;

%because of fft, dx,dy>=0. FFT assumes periodicity so
%im(x)=im(x+n*w). hence dx-w,dy-h are other candidates.

dx=dx-1; %no idea why -1 here
dy=dy-1;

[h,w]=size(part1);

err1=overlaperr(part1,part2,dx,dy);
err2=overlaperr(part1,part2,dx-w,dy);
err3=overlaperr(part1,part2,dx,dy-h);
err4=overlaperr(part1,part2,dx-w,dy-h);

allerr=[...
    err1,dy,dx;...
    err2,dy,dx-w;...
    err3,dy-h,dx;...
    err4,dy-h,dx-w];
allerr=sortrows(allerr,1);

%allerr

err=allerr(1,1);
dy=allerr(1,2);
dx=allerr(1,3);