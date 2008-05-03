function mi=locmax1(im,s)
[h,w]=size(im);

mi=zeros(size(im));
for i=1:h
    for j=1:w
        window=[max(1,j-s):min(w,j+s)];
        mi(i,j)=max(im(i,window));
    end
end

