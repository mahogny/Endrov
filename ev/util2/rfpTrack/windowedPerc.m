%Windowed percentile
%w is number of steps to the left and right. 0 means only midpoint.
%same with h
%percentile 0-100
function out=windowedPerc(im, pw, ph, percentile)

[h,w]=size(im);

out=zeros(h,w);
for ay=1:h
    for ax=1:w
        minx=max(1,ax-pw);
        maxx=min(w,ax+pw);
        miny=max(1,ay-ph);
        maxy=min(h,ay+ph);
        
        sub=im(miny:maxy,minx:maxx);
        sub=reshape(sub,1,size(sub,1)*size(sub,2));
        out(ay,ax)=prctile(sub,percentile);
        
    end
end