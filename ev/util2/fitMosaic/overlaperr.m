function err=overlaperr(part1,part2,dx,dy)
%Images has to be of the same size

yfrom=max([0,dy])+1;
yto=min([size(part1,1),dy+size(part2,1)]);

xfrom=max([0,dx])+1;
xto=min([size(part1,2),dx+size(part2,2)]);

areaw=(xto-xfrom+1);
areah=(yto-yfrom+1);

if areaw<=0 || areah<=0
    %[yfrom,yto,xfrom,xto]
    666
    666
    666
    666
    %I don't think this CAN occur
    err=100000; 
else
    
    area=areaw*areah;

    ima=part1(yfrom:yto,xfrom:xto);
    imb=part2((yfrom:yto)-dy,(xfrom:xto)-dx);

    res=ima - imb;

    %figure(3);imshow(ima)
    %figure(4);imshow(imb)
    %imshow(res)

    err=sum(sum(res.^2))/area;
end