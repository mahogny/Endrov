am=squeeze(mean(shiftdim(vox,2)));
v=((vox(:,:,10)-am)*20);
v(v<0)=0;
mesh(v)
image(v)

%v=vox(:,:,4);
ha = fspecial('average',1+2*2);
ima=imfilter(v,ha);

imm=locmax(v,7);
ismax=v>=imm;


%filter ismax based on local average
limit=prctile(ima(ismax),95)

%ismax(ima<limit)=0;


v2=v;
v2(ismax)=256;
image(v2)




image(imresize(v,1/2))
